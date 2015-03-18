/*
 *  Copyright (c) 2015, University of Oslo
 *  * All rights reserved.
 *  *
 *  * Redistribution and use in source and binary forms, with or without
 *  * modification, are permitted provided that the following conditions are met:
 *  * Redistributions of source code must retain the above copyright notice, this
 *  * list of conditions and the following disclaimer.
 *  *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *  * this list of conditions and the following disclaimer in the documentation
 *  * and/or other materials provided with the distribution.
 *  * Neither the name of the HISP project nor the names of its contributors may
 *  * be used to endorse or promote products derived from this software without
 *  * specific prior written permission.
 *  *
 *  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 *  * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.hisp.dhis2.android.sdk.controllers;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.otto.Subscribe;

import org.hisp.dhis2.android.sdk.R;
import org.hisp.dhis2.android.sdk.controllers.datavalues.DataValueController;
import org.hisp.dhis2.android.sdk.controllers.datavalues.DataValueLoader;
import org.hisp.dhis2.android.sdk.controllers.metadata.MetaDataController;
import org.hisp.dhis2.android.sdk.controllers.metadata.MetaDataLoader;
import org.hisp.dhis2.android.sdk.controllers.tasks.AuthUserTask;
import org.hisp.dhis2.android.sdk.events.BaseEvent;
import org.hisp.dhis2.android.sdk.events.LoadingEvent;
import org.hisp.dhis2.android.sdk.events.LoadingMessageEvent;
import org.hisp.dhis2.android.sdk.events.MessageEvent;
import org.hisp.dhis2.android.sdk.events.ResponseEvent;
import org.hisp.dhis2.android.sdk.network.http.ApiRequestCallback;
import org.hisp.dhis2.android.sdk.network.http.Response;
import org.hisp.dhis2.android.sdk.network.managers.NetworkManager;
import org.hisp.dhis2.android.sdk.persistence.Dhis2Application;
import org.hisp.dhis2.android.sdk.persistence.models.Program;
import org.hisp.dhis2.android.sdk.persistence.models.User;
import org.hisp.dhis2.android.sdk.services.PeriodicSynchronizer;
import org.hisp.dhis2.android.sdk.utils.APIException;
import org.hisp.dhis2.android.sdk.utils.CustomDialogFragment;
import org.hisp.dhis2.android.sdk.utils.GpsManager;

import java.io.IOException;

public final class Dhis2 {
    private static final String CLASS_TAG = "Dhis2";

    public final static String LAST_UPDATED_METADATA = "lastupdated_metadata";
    public final static String LAST_UPDATED_DATAVALUES = "lastupdated_datavalues";
    public final static String LOAD = "load";
    public static final String LOADED = "loaded";
    public static final String UPDATED = "updated";

    /* flags used to determine what data to be loaded from the server */
    public static final String LOAD_TRACKER = "load_tracker";
    public static final String LOAD_EVENTCAPTURE = "load_eventcapture";

    public static final String UPDATE_FREQUENCY = "update_frequency";
    public final static String QUEUED = "queued";
    public final static String PREFS_NAME = "DHIS2";
    private final static String USERNAME = "username";
    private final static String PASSWORD = "password";
    private final static String SERVER = "server";
    private final static String CREDENTIALS = "credentials";
    private MetaDataController metaDataController;
    private DataValueController dataValueController;
    private GpsManager gpsManager;
    private ObjectMapper objectMapper;
    private Context context; //beware when using this as it must be set explicitly
    private Activity activity;

    private boolean loadingInitial = false;
    private boolean loading = false;



    public Dhis2() {
        objectMapper = new ObjectMapper();
        gpsManager = new GpsManager();
    }

    public static Dhis2 getInstance() {
        return Dhis2Application.dhis2;
    }

    public static void activateGps(Context context) {
        getInstance().gpsManager.requestLocationUpdates(context);
    }

    public static void disableGps() {
        getInstance().gpsManager.removeUpdates();
    }

    public static Location getLocation(Context context) {
        return getInstance().gpsManager.getLocation(context);
    }

    /**
     * Returns the currently set Update Frequency
     * @param context
     * @return
     */
    public static int getUpdateFrequency(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int updateFrequency = sharedPreferences.getInt(UPDATE_FREQUENCY, 0);
        return updateFrequency;
    }

    /**
     * Sets the update frequency by an integer referencing the indexes in the update_frequencies string-array
     * @param context
     * @param frequency index of update frequencies. See update_frequencies string-array
     */
    public static void setUpdateFrequency(Context context, int frequency) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(UPDATE_FREQUENCY, frequency);
        editor.commit();
        PeriodicSynchronizer.reActivate(context);
    }

    public static void cancelPeriodicSynchronizer(Context context) {
        PeriodicSynchronizer.cancelPeriodicSynchronizer(context);
    }

    public static void activatePeriodicSynchronizer(Context context) {
        PeriodicSynchronizer.activatePeriodicSynchronizer(context, PeriodicSynchronizer.getInterval(context));
    }

    /**
     * Enables loading of different data. LOAD_EVENTCAPTURE, LOAD_TRACKER
     * @param mode
     */
    public void enableLoading(Context context, String mode) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if( mode.equals(LOAD_EVENTCAPTURE)) {
            editor.putBoolean(LOAD + MetaDataLoader.ASSIGNED_PROGRAMS, true);
            editor.putBoolean(LOAD + MetaDataLoader.TRACKED_ENTITY_ATTRIBUTES, true);
            editor.putBoolean(LOAD + MetaDataLoader.OPTION_SETS, true);
            editor.putBoolean(LOAD + MetaDataLoader.PROGRAMS, true);
            editor.putBoolean(LOAD + DataValueLoader.EVENTS, true);
            editor.putBoolean(LOAD + Program.SINGLE_EVENT_WITHOUT_REGISTRATION, true);
        } else if (mode.equals(LOAD_TRACKER)) {
            editor.putBoolean(LOAD + MetaDataLoader.ASSIGNED_PROGRAMS, true);
            editor.putBoolean(LOAD + MetaDataLoader.TRACKED_ENTITY_ATTRIBUTES, true);
            editor.putBoolean(LOAD + MetaDataLoader.OPTION_SETS, true);
            editor.putBoolean(LOAD + MetaDataLoader.PROGRAMS, true);
            editor.putBoolean(LOAD + DataValueLoader.EVENTS, true);
            editor.putBoolean(LOAD + DataValueLoader.ENROLLMENTS, true);
            editor.putBoolean(LOAD + DataValueLoader.TRACKED_ENTITY_INSTANCES, true);
            editor.putBoolean(LOAD + Program.SINGLE_EVENT_WITH_REGISTRATION, true);
            editor.putBoolean(LOAD + Program.MULTIPLE_EVENTS_WITH_REGISTRATION, true);
        }
        editor.commit();
    }

    /**
     * clears all loading flags
     * @param context
     */
    public static void clearLoadFlags(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(LOAD + MetaDataLoader.ASSIGNED_PROGRAMS, false);
        editor.putBoolean(LOAD + MetaDataLoader.TRACKED_ENTITY_ATTRIBUTES, false);
        editor.putBoolean(LOAD + MetaDataLoader.OPTION_SETS, false);
        editor.putBoolean(LOAD + MetaDataLoader.PROGRAMS, false);
        editor.putBoolean(LOAD + DataValueLoader.EVENTS, false);
        editor.putBoolean(LOAD + DataValueLoader.ENROLLMENTS, false);
        editor.putBoolean(LOAD + DataValueLoader.TRACKED_ENTITY_INSTANCES, false);
        editor.putBoolean(LOAD + Program.SINGLE_EVENT_WITHOUT_REGISTRATION, false);
        editor.commit();
    }

    /**
     * returns whether or not a load flag has been enabled.
     * @param context
     * @param flag
     * @return
     */
    public static boolean isLoadFlagEnabled(Context context, String flag) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(LOAD + flag, false);
    }

    public static boolean isInitialDataLoaded(Context context) {
        return isMetaDataLoaded(context) & isDataValuesLoaded(context);
    }

    /**
     * Returns false if some meta data flags that have been enabled have not been downloaded.
     * @param context
     * @return
     */
    public static boolean isMetaDataLoaded(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if(isLoadFlagEnabled(context, MetaDataLoader.ASSIGNED_PROGRAMS)) {
            if(!sharedPreferences.getBoolean(LOADED + MetaDataLoader.ASSIGNED_PROGRAMS, false)) return false;
        } else if(isLoadFlagEnabled(context, MetaDataLoader.OPTION_SETS)) {
            if(!sharedPreferences.getBoolean(LOADED + MetaDataLoader.OPTION_SETS, false)) return false;
        } else if(isLoadFlagEnabled(context, MetaDataLoader.PROGRAMS)) {
            if(!sharedPreferences.getBoolean(LOADED + MetaDataLoader.PROGRAMS, false)) return false;
        } else if(isLoadFlagEnabled(context, MetaDataLoader.TRACKED_ENTITY_ATTRIBUTES)) {
            if(!sharedPreferences.getBoolean(LOADED + MetaDataLoader.TRACKED_ENTITY_ATTRIBUTES, false)) return false;
        }
        return true;
    }

    /**
     * Returns false if some data value flags that have been enabled have not been downloaded.
     * @param context
     * @return
     */
    public static boolean isDataValuesLoaded(Context context) {
        Log.d(CLASS_TAG, "isdatavaluesloaded..");
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if(isLoadFlagEnabled(context, DataValueLoader.EVENTS)) {
            if(!DataValueLoader.isEventsLoaded(context)) return false;
        } else if(isLoadFlagEnabled(context, DataValueLoader.ENROLLMENTS)) {
            if(!DataValueLoader.isEnrollmentsLoaded(context)) return false;
        } else if(isLoadFlagEnabled(context, DataValueLoader.TRACKED_ENTITY_INSTANCES)) {
            if(!DataValueLoader.isTrackedEntityInstancesLoaded(context)) return false;
        }
        return true;
    }

    public ObjectMapper getObjectMapper() {
        if(objectMapper==null) objectMapper = new ObjectMapper();
        return objectMapper;
    }

    public MetaDataController getMetaDataController() {
        if(metaDataController == null) metaDataController = new MetaDataController();
        return metaDataController;
    }

    public DataValueController getDataValueController() {
        if(dataValueController == null) dataValueController = new DataValueController();
        return dataValueController;
    }

    /**
     *
     * @param serverUrl
     */
    public void setServer(String serverUrl) {
        NetworkManager.getInstance().setServerUrl(serverUrl);
    }

    public static void saveCredentials(Context context, String serverUrl, String username, String password) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(USERNAME, username);
        editor.putString(PASSWORD, password);
        editor.putString(SERVER, serverUrl);
        String credentials = null;
        if(username!=null && password!=null)
            credentials = NetworkManager.getInstance().getBase64Manager().toBase64(username, password);
        editor.putString(CREDENTIALS, credentials);
        editor.commit();
    }

    public static String getUsername(Context context) {
        if(context != null) getInstance().context = context;
        if(getInstance().context == null) return null;
        SharedPreferences prefs = getInstance().context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String username = prefs.getString(USERNAME, null);
        return username;
    }

    public static String getPassword(Context context) {
        if(context != null) getInstance().context = context;
        if(getInstance().context == null) return null;
        SharedPreferences prefs = getInstance().context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String password = prefs.getString(PASSWORD, null);
        return password;
    }

    public static String getServer(Context context) {
        if(context != null ) getInstance().context = context;
        if(getInstance().context == null) return null;
        SharedPreferences prefs = getInstance().context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String server = prefs.getString(SERVER, null);
        return server;
    }

    public static String getCredentials(Context context) {
        if(context != null ) getInstance().context = context;
        if(getInstance().context == null) return null;
        SharedPreferences prefs = getInstance().context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String credentials = prefs.getString(CREDENTIALS, null);
        return credentials;
    }

    /**
     * Tries to log in to the given DHIS 2 server
     * @param username
     * @param password
     */
    public void login(String username, String password) {
        // TODO first check if we already have User through persistence layer
        // TODO if yes, return it, if not call network
        final ResponseHolder<User> holder = new ResponseHolder<>();
        new AuthUserTask(NetworkManager.getInstance(), new ApiRequestCallback<User>() {
            @Override
            public void onSuccess(Response response) {
                holder.setResponse(response);

                try {
                    User user = objectMapper.readValue(response.getBody(), User.class);
                    holder.setItem(user);
                } catch (IOException e) {
                    e.printStackTrace();
                    holder.setApiException(APIException.conversionError(response.getUrl(), response, e));
                }
                ResponseEvent<User> event = new ResponseEvent<User>(ResponseEvent.EventType.onLogin);
                event.setResponseHolder(holder);
                Dhis2Application.bus.post(event);
            }

            @Override
            public void onFailure(APIException exception) {
                holder.setApiException(exception);
                ResponseEvent event = new ResponseEvent(ResponseEvent.EventType.onLogin);
                event.setResponseHolder(holder);
                Dhis2Application.bus.post(event);
            }
        }, username, password).execute();
    }

    /**
     * Logs out the current user
     */
    public static void logout(Context context) {
        Dhis2.getInstance().setServer(null);
        Dhis2.getInstance().saveCredentials(context, null, null, null);
        NetworkManager.getInstance().setCredentials(null);
        getInstance().getMetaDataController().resetLastUpdated(context);
        getInstance().getMetaDataController().clearMetaDataLoadedFlags(context);
        getInstance().getDataValueController().clearDataValueLoadedFlags(context);
        clearLoadFlags(context);
    }

    /**
     * initiates sending locally modified data items and loads updated items from the server.
     * @param context1
     */
    public static void sendLocalData(final Context context1) {
        if(getInstance().loading) return;
        getInstance().loading = true;
        new Thread() {
            public void run() {
                getInstance().getDataValueController().synchronizeDataValues(context1);
            }
        }.start();
    }

    /**
     * Loads initial data, defined by the enableLoading method
     */
    public static void loadInitialData(Context context) {
        if( context != null ) {
            getInstance().context = context;
            if(context instanceof Activity) getInstance().activity = (Activity) context;
        }
        if( context == null && getInstance().context == null ) return;

        getInstance().loadingInitial = true;
        getInstance().loading = true;
        loadInitialMetadata();
    }

    private static void loadInitialMetadata() {
        if(!isMetaDataLoaded(getInstance().context))
        {
            Log.d(CLASS_TAG, "init loading metadata!");
            getInstance().getMetaDataController().loadMetaData(getInstance().context);
        } else {
            loadInitialDataValues();
        }
    }

    /**
     * initiates synchronization of metadata from server
     * @param context
     */
    public static void synchronizeMetaData(Context context) {
        if(getInstance().loading) return;
        getInstance().loading = true;
        Dhis2.getInstance().getMetaDataController().synchronizeMetaData(context);
    }

    /* called either from loadInitialMetadata, or in Subscribe method*/
    private static void loadInitialDataValues() {
        Log.d(CLASS_TAG, "init loading datavalues");
        if(!isDataValuesLoaded(getInstance().context)) {
                Log.d(CLASS_TAG, "init loadig datavalues trackerEnabled init loading");
                getInstance().getDataValueController().loadDataValues(getInstance().context, false);
        } else {
            onFinishLoading();
        }
    }

    /**
     * Called after meta data and data values have finished loading
     * todo: as more options for data loading is added, expand this to something more comprehensible
     */
    private static void onFinishLoading() {
        Log.d(CLASS_TAG, "onFinishLoading");
        if( getInstance().loadingInitial ) {
            if(isMetaDataLoaded(getInstance().context) ) {
                Log.d(CLASS_TAG, "has loaded init metadatapart");
                /**
                 * Initial loading of meta data is completed successfully, continue checking if
                 * required data values was loaded successfully.
                 */

                if ( isDataValuesLoaded(getInstance().context) ) {
                    Log.d(CLASS_TAG, "full loading success");
                    MessageEvent messageEvent = new MessageEvent(BaseEvent.EventType.onLoadingInitialDataFinished);
                    Dhis2Application.bus.post(messageEvent); //could be called wherever you want, for example in your MainActivity
                } else {
                    //todo: implement re-trying of loading or sth. Could perhaps be handled further down in the process
                    //todo: implement onFailedLoadingInitialDataValues
                    Log.d(CLASS_TAG, "full loading failed loading data values. Continuing anyways..");
                    MessageEvent messageEvent = new MessageEvent(BaseEvent.EventType.onLoadingInitialDataFinished);
                    Dhis2Application.bus.post(messageEvent);
                }
            } else {
                Log.d(CLASS_TAG, "failed loading!!!");
                /**
                 * Initial loading of meta data failed. Give the user feedback. Since meta data is
                 * missing the app will most likely not function very well, but we could allow the
                 * user to continue working regardless.
                 */
                onFailedLoadingInitialMetaData();
            }

        }
        getInstance().loadingInitial = false;
        getInstance().loading = false;
    }

    /**
     * Called if initial meta data loading fails. Gives a notification to user.
     */
    private static void onFailedLoadingInitialMetaData() {
        Log.d(CLASS_TAG, "onfailedloadinginititalmetadata show the dialog");
        if(getInstance().activity != null) {
            Log.d(CLASS_TAG, "showing the dialog now");
            DialogInterface.OnClickListener retryListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    loadInitialData(getInstance().activity);
                }
            };

            DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    MessageEvent event = new MessageEvent(BaseEvent.EventType.loadInitialDataFailed); //can be subscribed to for example in MainActivity or wherever loading was initiated.
                    Dhis2Application.bus.post(event);
                }
            };
            showConfirmDialog(getInstance().activity,
                    getInstance().activity.getString(R.string.error_message),
                    getInstance().activity.getString(R.string.loading_failed_metadata),
                    getInstance().activity.getString(R.string.retry),
                    getInstance().activity.getString(R.string.cancel),
                    retryListener,
                    cancelListener);
        } else {
            MessageEvent event = new MessageEvent(BaseEvent.EventType.loadInitialDataFailed); //can be subscribed to for example in MainActivity or wherever loading was initiated.
            Dhis2Application.bus.post(event);
        }
    }

    public static void showErrorDialog(final Activity activity, final String title, final String message) {
        if(activity == null) return;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new CustomDialogFragment( title, message,
                        "OK", null ).show(activity.getFragmentManager(), title);
            }
        });
    }

    public static void showErrorDialog(final Activity activity, final String title, final String message, final DialogInterface.OnClickListener onConfirmClickListener) {
        if(activity == null) return;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new CustomDialogFragment( title, message,
                        "OK", onConfirmClickListener ).show(activity.getFragmentManager(), title);
            }
        });
    }

    public static void showConfirmDialog(final Activity activity, final String title, final String message,
                                  final String confirmOption, final String cancelOption,
                                  DialogInterface.OnClickListener onClickListener) {
        new CustomDialogFragment( title, message, confirmOption, cancelOption, onClickListener ).
                show(activity.getFragmentManager(), title);
    }
    public static void showConfirmDialog(final Activity activity, final String title, final String message,
                                          final String confirmOption, final String cancelOption,
                                          DialogInterface.OnClickListener onConfirmListener,
                                          DialogInterface.OnClickListener onCancelListener) {
        new CustomDialogFragment( title, message, confirmOption, cancelOption, onConfirmListener,
                onCancelListener ).
                show(activity.getFragmentManager(), title);
    }

    @Subscribe
    public void onResponse(LoadingEvent loadingEvent) {
        if( loadingEvent.eventType== BaseEvent.EventType.onLoadingMetaDataFinished) {
            if(!loadingEvent.success) {
                onFinishLoading();
            } else {
                if(isLoadingInitial())
                    loadInitialDataValues();
            }
        } else if (loadingEvent.eventType == BaseEvent.EventType.onUpdateMetaDataFinished ) {
            getInstance().getDataValueController().synchronizeDataValues(context);
        }
        else if (loadingEvent.eventType == BaseEvent.EventType.onLoadDataValuesFinished) {
            onFinishLoading();
        } else if(loadingEvent.eventType == BaseEvent.EventType.onUpdateDataValuesFinished) {
            onFinishLoading();
        }
    }

    /**
     * Sends an event with feedback to user on loading. Picked up in LoadingFragment.
     * @param message
     */
    public static void postProgressMessage(final String message) {
        new Thread() {
            @Override
            public void run() {
                LoadingMessageEvent event = new LoadingMessageEvent(BaseEvent.EventType.showRegisterEventFragment);
                event.message = message;
                Dhis2Application.bus.post(event);
            }
        }.start();
    }

    public static void setLoadingInitial(boolean loading) {
        getInstance().loadingInitial = loading;
    }

    public static boolean isLoadingInitial() {
        return getInstance().loadingInitial;
    }
}
