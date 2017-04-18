package com.danielcwilson.plugins.analytics;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Logger.LogLevel;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map.Entry;

public class UniversalAnalyticsPlugin extends CordovaPlugin {
    public static final String START_TRACKER = "startTrackerWithId";
    public static final String TRACK_VIEW = "trackView";
    public static final String TRACK_EVENT = "trackEvent";
    public static final String TRACK_EXCEPTION = "trackException";
    public static final String TRACK_TIMING = "trackTiming";
    public static final String TRACK_METRIC = "trackMetric";
    public static final String ADD_DIMENSION = "addCustomDimension";
    public static final String ADD_TRANSACTION = "addTransaction";
    public static final String ADD_TRANSACTION_ITEM = "addTransactionItem";

    public static final String SET_ALLOW_IDFA_COLLECTION = "setAllowIDFACollection";
    public static final String SET_USER_ID = "setUserId";
    public static final String SET_ANONYMIZE_IP = "setAnonymizeIp";
    public static final String SET_OPT_OUT = "setOptOut";
    public static final String SET_APP_VERSION = "setAppVersion";
    public static final String GET_VAR = "getVar";
    public static final String DEBUG_MODE = "debugMode";
    public static final String ENABLE_UNCAUGHT_EXCEPTION_REPORTING = "enableUncaughtExceptionReporting";

    public Boolean trackerStarted = false;
    public Boolean debugModeEnabled = false;
    public HashMap<Integer, String> customDimensions = new HashMap<Integer, String>();

    public HashMap<String, Tracker> trackers = new HashMap<String, Tracker>();

    private boolean trackerStarted() {
        return trackers.size() > 0;
    }

    private boolean hasTracker(String name) {
        return trackerStarted() && trackers.containsKey(name);
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        System.out.print("Parsing action");
        System.out.print(action);

        if (START_TRACKER.equals(action)) {
            String trackerName = args.getString(0);
            String id = args.getString(1);
            int dispatchPeriod = args.length() > 2 ? args.getInt(2) : 30;
            this.startTracker(trackerName, id, dispatchPeriod, callbackContext);
            return true;
        } else if (TRACK_VIEW.equals(action)) {
            int length = args.length();
            String trackerName = args.getString(0);
            String screen = args.getString(1);
            this.trackView(trackerName, screen, length > 2 && !args.isNull(2) ? args.getString(2) : "", length > 3 && !args.isNull(3) ? args.getBoolean(3) : false, callbackContext);
            return true;
        } else if (TRACK_EVENT.equals(action)) {
            int length = args.length();
            if (length > 0) {
                this.trackEvent(
                        args.getString(0),
                        args.getString(1),
                        length > 2 ? args.getString(2) : "",
                        length > 3 ? args.getString(3) : "",
                        length > 4 ? args.getLong(4) : 0,
                        length > 5 ? args.getBoolean(5) : false,
                        callbackContext);
            }
            return true;
        } else if (TRACK_EXCEPTION.equals(action)) {
            String trackerName = args.getString(0);
            String description = args.getString(1);
            Boolean fatal = args.getBoolean(2);
            this.trackException(trackerName, description, fatal, callbackContext);
            return true;
        } else if (TRACK_TIMING.equals(action)) {
            int length = args.length();
            if (length > 0) {
                this.trackTiming(args.getString(0), args.getString(1), length > 2 ? args.getLong(2) : 0, length > 3 ? args.getString(3) : "", length > 4 ? args.getString(4) : "", callbackContext);
            }
            return true;
        } else if (TRACK_METRIC.equals(action)) {
            int length = args.length();
            if (length > 0) {
                this.trackMetric(args.getString(0), args.getInt(1), length > 2 ? args.getString(2) : "", callbackContext);
            }
            return true;
        } else if (ADD_DIMENSION.equals(action)) {
            String trackerName = args.getString(0);
            Integer key = args.getInt(1);
            String value = args.getString(2);
            this.addCustomDimension(trackerName, key, value, callbackContext);
            return true;
        } else if (ADD_TRANSACTION.equals(action)) {
            int length = args.length();
            if (length > 0) {
                this.addTransaction(
                        args.getString(0),
                        args.getString(1),
                        length > 2 ? args.getString(2) : "",
                        length > 3 ? args.getDouble(3) : 0,
                        length > 4 ? args.getDouble(4) : 0,
                        length > 5 ? args.getDouble(5) : 0,
                        length > 6 ? args.getString(6) : null,
                        callbackContext);
            }
            return true;
        } else if (ADD_TRANSACTION_ITEM.equals(action)) {
            int length = args.length();
            if (length > 0) {
                this.addTransactionItem(
                        args.getString(0),
                        args.getString(1),
                        length > 2 ? args.getString(2) : "",
                        length > 3 ? args.getString(3) : "",
                        length > 4 ? args.getString(4) : "",
                        length > 5 ? args.getDouble(5) : 0,
                        length > 6 ? args.getLong(6) : 0,
                        length > 7 ? args.getString(7) : null,
                        callbackContext);
            }
            return true;
        } else if (SET_ALLOW_IDFA_COLLECTION.equals(action)) {
            this.setAllowIDFACollection(args.getString(0), args.getBoolean(1), callbackContext);
        } else if (SET_USER_ID.equals(action)) {
            System.out.print("Equals SET_USER_ID");

            String trackerName = args.getString(0);
            String userId = args.getString(1);
            this.setUserId(trackerName, userId, callbackContext);
        } else if (SET_ANONYMIZE_IP.equals(action)) {
            String trackerName = args.getString(0);
            boolean anonymize = args.getBoolean(1);
            this.setAnonymizeIp(trackerName, anonymize, callbackContext);
        } else if (SET_OPT_OUT.equals(action)) {
            boolean optout = args.getBoolean(0);
            this.setOptOut(optout, callbackContext);
        } else if (SET_APP_VERSION.equals(action)) {
            String trackerName = args.getString(0);
            String version = args.getString(1);
            this.setAppVersion(trackerName, version, callbackContext);
        } else if (GET_VAR.equals(action)) {
            String trackerName = args.getString(0);
            String variable = args.getString(1);
            this.getVar(trackerName, variable, callbackContext);
        } else if (DEBUG_MODE.equals(action)) {
            this.debugMode(callbackContext);
        } else if (ENABLE_UNCAUGHT_EXCEPTION_REPORTING.equals(action)) {
            String trackerName = args.getString(0);
            Boolean enable = args.getBoolean(1);
            this.enableUncaughtExceptionReporting(trackerName, enable, callbackContext);
        }
        return false;
    }

    private void startTracker(String trackerName, String id, int dispatchPeriod, CallbackContext callbackContext) {
        if (null != id && id.length() > 0) {
            if (!this.hasTracker(trackerName)) {
                trackers.put(trackerName, GoogleAnalytics.getInstance(this.cordova.getActivity()).newTracker(id));
                callbackContext.success("tracker started");
                GoogleAnalytics.getInstance(this.cordova.getActivity()).setLocalDispatchPeriod(dispatchPeriod);
            } else {
                callbackContext.error("tracker already exists with this name");
            }
        } else {
            callbackContext.error("tracker id is not valid");
        }
    }

    private void addCustomDimension(String trackerName, Integer key, String value, CallbackContext callbackContext) {
        if (key <= 0) {
            callbackContext.error("Expected positive integer argument for key.");
            return;
        }

        if (null == value || value.length() == 0) {
            callbackContext.error("Expected non-empty string argument for value.");
            return;
        }

        customDimensions.put(key, value);
        callbackContext.success("custom dimension started");
    }

    private <T> void addCustomDimensionsToHitBuilder(T builder) {
        //unfortunately the base HitBuilders.HitBuilder class is not public, therefore have to use reflection to use
        //the common setCustomDimension (int index, String dimension) method
        try {
            Method builderMethod = builder.getClass().getMethod("setCustomDimension", Integer.TYPE, String.class);

            for (Entry<Integer, String> entry : customDimensions.entrySet()) {
                Integer key = entry.getKey();
                String value = entry.getValue();
                try {
                    builderMethod.invoke(builder, (key), value);
                } catch (IllegalArgumentException e) {
                } catch (IllegalAccessException e) {
                } catch (InvocationTargetException e) {
                }
            }
        } catch (SecurityException e) {
        } catch (NoSuchMethodException e) {
        }
    }

    private void trackView(String trackerName, String screenname, String campaignUrl, boolean newSession, CallbackContext callbackContext) {
        if (!trackerStarted() || !hasTracker(trackerName)) {
            callbackContext.error("Tracker not started");
            return;
        }

        Tracker tracker = trackers.get(trackerName);

        if (tracker != null) {
            if (null != screenname && screenname.length() > 0) {
                tracker.setScreenName(screenname);

                HitBuilders.ScreenViewBuilder hitBuilder = new HitBuilders.ScreenViewBuilder();
                addCustomDimensionsToHitBuilder(hitBuilder);

                if(!campaignUrl.equals("")){
                    hitBuilder.setCampaignParamsFromUrl(campaignUrl);
                }

                if(!newSession) {
                    tracker.send(hitBuilder.build());
                } else {
                    tracker.send(hitBuilder.setNewSession().build());
                }

                callbackContext.success("Track Screen: " + screenname);
            } else {
                callbackContext.error("Expected one non-empty string argument.");
            }
        } else {
            callbackContext.error("Tracker not found");
        }
    }

    private void trackEvent(String trackerName, String category, String action, String label, long value, boolean newSession, CallbackContext callbackContext) {
        if (!trackerStarted() || !hasTracker(trackerName)) {
            callbackContext.error("Tracker not started");
            return;
        }

        Tracker tracker = trackers.get(trackerName);

        if (tracker != null) {
            if (null != category && category.length() > 0) {
                HitBuilders.EventBuilder hitBuilder = new HitBuilders.EventBuilder();
                addCustomDimensionsToHitBuilder(hitBuilder);

                if(!newSession){
                    tracker.send(hitBuilder
                            .setCategory(category)
                            .setAction(action)
                            .setLabel(label)
                            .setValue(value)
                            .build());
                } else {
                    tracker.send(hitBuilder
                            .setCategory(category)
                            .setAction(action)
                            .setLabel(label)
                            .setValue(value)
                            .setNewSession()
                            .build());
                }

                callbackContext.success("Track Event: " + category);
            } else {
                callbackContext.error("Expected non-empty string arguments.");
            }
        } else {
            callbackContext.error("Tracker not found");
        }
    }

    private void trackMetric(String trackerName, Integer key, String value, CallbackContext callbackContext) {
        if (!trackerStarted() || !hasTracker(trackerName)) {
            callbackContext.error("Tracker not started");
            return;
        }

        Tracker tracker = trackers.get(trackerName);

        if (tracker != null) {
            if (key >= 0) {
                HitBuilders.ScreenViewBuilder hitBuilder = new HitBuilders.ScreenViewBuilder();
                tracker.send(hitBuilder
                        .setCustomMetric(key, Float.parseFloat(value))
                        .build()
                );
                callbackContext.success("Track Metric: " + key + ", value: " + value);
            } else {
                callbackContext.error("Expected integer key: " + key + ", and string value: " + value);
            }
        } else {
            callbackContext.error("Tracker not found");
        }
    }

    private void trackException(String trackerName, String description, Boolean fatal, CallbackContext callbackContext) {
        if (!trackerStarted() || !hasTracker(trackerName)) {
            callbackContext.error("Tracker not started");
            return;
        }

        Tracker tracker = trackers.get(trackerName);

        if (tracker != null) {
            if (null != description && description.length() > 0) {
                HitBuilders.ExceptionBuilder hitBuilder = new HitBuilders.ExceptionBuilder();
                addCustomDimensionsToHitBuilder(hitBuilder);

                tracker.send(hitBuilder
                        .setDescription(description)
                        .setFatal(fatal)
                        .build()
                );
                callbackContext.success("Track Exception: " + description);
            } else {
                callbackContext.error("Expected non-empty string arguments.");
            }
        } else {
            callbackContext.error("Tracker not found");
        }
    }

    private void trackTiming(String trackerName, String category, long intervalInMilliseconds, String name, String label, CallbackContext callbackContext) {
        if (!trackerStarted() || !hasTracker(trackerName)) {
            callbackContext.error("Tracker not started");
            return;
        }

        Tracker tracker = trackers.get(trackerName);

        if (tracker != null) {
            if (null != category && category.length() > 0) {
                HitBuilders.TimingBuilder hitBuilder = new HitBuilders.TimingBuilder();
                addCustomDimensionsToHitBuilder(hitBuilder);

                tracker.send(hitBuilder
                        .setCategory(category)
                        .setValue(intervalInMilliseconds)
                        .setVariable(name)
                        .setLabel(label)
                        .build()
                );
                callbackContext.success("Track Timing: " + category);
            } else {
                callbackContext.error("Expected non-empty string arguments.");
            }
        } else {
            callbackContext.error("Tracker not found");
        }
    }

    private void addTransaction(String trackerName, String id, String affiliation, double revenue, double tax, double shipping, String currencyCode, CallbackContext callbackContext) {
        if (!trackerStarted() || !hasTracker(trackerName)) {
            callbackContext.error("Tracker not started");
            return;
        }

        Tracker tracker = trackers.get(trackerName);

        if (tracker != null) {
            if (null != id && id.length() > 0) {
                HitBuilders.TransactionBuilder hitBuilder = new HitBuilders.TransactionBuilder();
                addCustomDimensionsToHitBuilder(hitBuilder);

                tracker.send(hitBuilder
                        .setTransactionId(id)
                        .setAffiliation(affiliation)
                        .setRevenue(revenue).setTax(tax)
                        .setShipping(shipping)
                        .setCurrencyCode(currencyCode)
                        .build()
                ); //Deprecated
                callbackContext.success("Add Transaction: " + id);
            } else {
                callbackContext.error("Expected non-empty ID.");
            }
        } else {
            callbackContext.error("Tracker not found");
        }
    }

    private void addTransactionItem(String trackerName, String id, String name, String sku, String category, double price, long quantity, String currencyCode, CallbackContext callbackContext) {
        if (!trackerStarted() || !hasTracker(trackerName)) {
            callbackContext.error("Tracker not started");
            return;
        }

        Tracker tracker = trackers.get(trackerName);

        if (tracker != null) {
            if (null != id && id.length() > 0) {
                HitBuilders.ItemBuilder hitBuilder = new HitBuilders.ItemBuilder();
                addCustomDimensionsToHitBuilder(hitBuilder);

                tracker.send(hitBuilder
                        .setTransactionId(id)
                        .setName(name)
                        .setSku(sku)
                        .setCategory(category)
                        .setPrice(price)
                        .setQuantity(quantity)
                        .setCurrencyCode(currencyCode)
                        .build()
                ); //Deprecated
                callbackContext.success("Add Transaction Item: " + id);
            } else {
                callbackContext.error("Expected non-empty ID.");
            }
        } else {
            callbackContext.error("Tracker not found");
        }
    }

    private void setAllowIDFACollection(String trackerName, Boolean enable, CallbackContext callbackContext) {
        if (!trackerStarted() || !hasTracker(trackerName)) {
            callbackContext.error("Tracker not started");
            return;
        }

        Tracker tracker = trackers.get(trackerName);

        if (tracker != null) {
            tracker.enableAdvertisingIdCollection(enable);
            callbackContext.success("Enable Advertising Id Collection: " + enable);
        } else {
            callbackContext.error("Tracker not found");
        }
    }

    private void getVar(String trackerName, String variable, CallbackContext callbackContext) {
        if (!trackerStarted || !hasTracker(trackerName)) {
            callbackContext.error("Tracker not started");
            return;
        }

        Tracker tracker = trackers.get(trackerName);

        if (tracker != null) {
            String result = tracker.get(variable);
            callbackContext.success(result);
        } else {
            callbackContext.error("Tracker not found");
        }
    }

    private void debugMode(CallbackContext callbackContext) {
        GoogleAnalytics.getInstance(this.cordova.getActivity()).getLogger().setLogLevel(LogLevel.VERBOSE);

        this.debugModeEnabled = true;
        callbackContext.success("debugMode enabled");
    }

    private void setAnonymizeIp(String trackerName, boolean anonymize, CallbackContext callbackContext) {
        if (!trackerStarted() || !hasTracker(trackerName)) {
            callbackContext.error("Tracker not started");
            return;
        }

        Tracker tracker = trackers.get(trackerName);

        if (tracker != null) {
            tracker.setAnonymizeIp(anonymize);
            callbackContext.success("Set AnonymizeIp " + anonymize);
        } else {
            callbackContext.error("Tracker not found");
        }
    }

    private void setOptOut(boolean optout, CallbackContext callbackContext) {
        if (!trackerStarted()) {
            callbackContext.error("Tracker not started");
            return;
        }

        GoogleAnalytics.getInstance(this.cordova.getActivity()).setAppOptOut(optout);
        callbackContext.success("Set Opt-Out " + optout);
    }

    private void setUserId(String trackerName, String userId, CallbackContext callbackContext) {
        if (!trackerStarted || !hasTracker(trackerName)) {
            callbackContext.error("Tracker not started");
            return;
        }

        Tracker tracker = trackers.get(trackerName);

        if (tracker != null) {
            tracker.set("&uid", userId);
            callbackContext.success("Set user id" + userId);
        } else {
            callbackContext.error("Tracker not found");
        }
    }

    private void setAppVersion(String trackerName, String version, CallbackContext callbackContext) {
        if (!trackerStarted() || !hasTracker(trackerName)) {
            callbackContext.error("Tracker not started");
            return;
        }

        Tracker tracker = trackers.get(trackerName);

        if (tracker != null) {
            tracker.set("&av", version);
            callbackContext.success("Set app version: " + version);
        } else {
            callbackContext.error("Tracker not found");
        }
    }

    private void enableUncaughtExceptionReporting(String trackerName, Boolean enable, CallbackContext callbackContext) {
        if (!trackerStarted() || !hasTracker(trackerName)) {
            callbackContext.error("Tracker not started");
            return;
        }

        Tracker tracker = trackers.get(trackerName);

        if (tracker != null) {
            tracker.enableExceptionReporting(enable);
            callbackContext.success((enable ? "Enabled" : "Disabled") + " uncaught exception reporting");
        } else {
            callbackContext.error("Tracker not found");
        }
    }
}
