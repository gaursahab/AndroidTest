package com.ford.Service;

import java.io.IOException;

import org.apache.log4j.Logger;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;

import com.ford.R;
import com.ford.Activity.MainActivity;
import com.ford.syncV4.exception.SyncException;
import com.ford.syncV4.exception.SyncExceptionCause;
import com.ford.syncV4.proxy.IProxyListener;
import com.ford.syncV4.proxy.SyncProxyALM;
import com.ford.syncV4.proxy.interfaces.IProxyListenerALM;
import com.ford.syncV4.proxy.rpc.AddCommandResponse;
import com.ford.syncV4.proxy.rpc.AddSubMenuResponse;
import com.ford.syncV4.proxy.rpc.AlertResponse;
import com.ford.syncV4.proxy.rpc.ChangeRegistrationResponse;
import com.ford.syncV4.proxy.rpc.CreateInteractionChoiceSetResponse;
import com.ford.syncV4.proxy.rpc.DeleteCommandResponse;
import com.ford.syncV4.proxy.rpc.DeleteFileResponse;
import com.ford.syncV4.proxy.rpc.DeleteInteractionChoiceSetResponse;
import com.ford.syncV4.proxy.rpc.DeleteSubMenuResponse;
import com.ford.syncV4.proxy.rpc.EncodedSyncPDataResponse;
import com.ford.syncV4.proxy.rpc.EndAudioPassThruResponse;
import com.ford.syncV4.proxy.rpc.GenericResponse;
import com.ford.syncV4.proxy.rpc.GetDTCsResponse;
import com.ford.syncV4.proxy.rpc.GetVehicleData;
import com.ford.syncV4.proxy.rpc.GetVehicleDataResponse;
import com.ford.syncV4.proxy.rpc.ListFilesResponse;
import com.ford.syncV4.proxy.rpc.OnAppInterfaceUnregistered;
import com.ford.syncV4.proxy.rpc.OnAudioPassThru;
import com.ford.syncV4.proxy.rpc.OnButtonEvent;
import com.ford.syncV4.proxy.rpc.OnButtonPress;
import com.ford.syncV4.proxy.rpc.OnCommand;
import com.ford.syncV4.proxy.rpc.OnDriverDistraction;
import com.ford.syncV4.proxy.rpc.OnEncodedSyncPData;
import com.ford.syncV4.proxy.rpc.OnHMIStatus;
import com.ford.syncV4.proxy.rpc.OnLanguageChange;
import com.ford.syncV4.proxy.rpc.OnPermissionsChange;
import com.ford.syncV4.proxy.rpc.OnSyncPData;
import com.ford.syncV4.proxy.rpc.OnTBTClientState;
import com.ford.syncV4.proxy.rpc.OnVehicleData;
import com.ford.syncV4.proxy.rpc.PerformAudioPassThruResponse;
import com.ford.syncV4.proxy.rpc.PerformInteractionResponse;
import com.ford.syncV4.proxy.rpc.PutFileResponse;
import com.ford.syncV4.proxy.rpc.ReadDIDResponse;
import com.ford.syncV4.proxy.rpc.RegisterAppInterfaceResponse;
import com.ford.syncV4.proxy.rpc.ResetGlobalPropertiesResponse;
import com.ford.syncV4.proxy.rpc.ScrollableMessageResponse;
import com.ford.syncV4.proxy.rpc.SetAppIconResponse;
import com.ford.syncV4.proxy.rpc.SetDisplayLayoutResponse;
import com.ford.syncV4.proxy.rpc.SetGlobalPropertiesResponse;
import com.ford.syncV4.proxy.rpc.SetMediaClockTimerResponse;
import com.ford.syncV4.proxy.rpc.ShowResponse;
import com.ford.syncV4.proxy.rpc.SliderResponse;
import com.ford.syncV4.proxy.rpc.SpeakResponse;
import com.ford.syncV4.proxy.rpc.SubscribeButtonResponse;
import com.ford.syncV4.proxy.rpc.SubscribeVehicleData;
import com.ford.syncV4.proxy.rpc.SubscribeVehicleDataResponse;
import com.ford.syncV4.proxy.rpc.SyncPDataResponse;
import com.ford.syncV4.proxy.rpc.UnregisterAppInterfaceResponse;
import com.ford.syncV4.proxy.rpc.UnsubscribeButtonResponse;
import com.ford.syncV4.proxy.rpc.UnsubscribeVehicleDataResponse;
import com.ford.syncV4.proxy.rpc.enums.ButtonName;
import com.ford.syncV4.proxy.rpc.enums.HMILevel;
import com.ford.syncV4.proxy.rpc.enums.Result;
import com.ford.syncV4.proxy.rpc.enums.TextAlignment;


public class AppLinkService extends Service implements IProxyListenerALM,IProxyListener {
	private static final Logger LOGGER = Logger.getLogger(AppLinkService.class);
	private static AppLinkService mInstance = null;
	private MediaPlayer mPlayer = null;
	private SyncProxyALM mProxy = null;
	private int mCorrelationId = 0;

	public static AppLinkService getInstance() {
		return mInstance;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if( intent != null &&BluetoothAdapter.getDefaultAdapter() != null &&BluetoothAdapter.getDefaultAdapter().isEnabled() ) {
			showInfo("Starting the Proxy Method, inside the onStartCommand");
			startProxy();
		}

		return START_STICKY;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mInstance = this;
	}

	@Override
	public void onDestroy() {
		showInfo("inside the onDestroy");
		removeSyncProxy();
		mInstance = null;
		super.onDestroy();
	}

	private void removeSyncProxy() {
		showInfo("inside removeSyncProxy method");
		if( mProxy == null )
			return;
		try {
			mProxy.dispose();
		} catch( SyncException e ) {
			LOGGER.error("Error inside the removeSyncProxy method: ",e);
		}
		mProxy = null;
	}

	public void startProxy() {
		showInfo("Inside the startProxy method");
		if( mProxy == null ) {
			showInfo("Inside the startProxy method, proxy ==null, creating the proxy");
			try {
				mProxy = new SyncProxyALM( this, getString( R.string.display_title ), true, getString( R.string.app_link_id ) );
			} catch( SyncException e ) {
				showError("Error inside the startProxy method: ",e);
				if( mProxy == null ) {
				showInfo("inside the startProxy method, catch block. mproxy == null");
					stopSelf();
				}
			}
		}
	}

	public SyncProxyALM getProxy() {
		showInfo("inside the getProxy() method..");
		return mProxy;
	}

	public void reset() {
		showInfo("inside the reset() method..");
		if( mProxy != null ) {
			try {
				showInfo("inside the reset() method..mProxy != null");
				mProxy.resetProxy();
			} catch( SyncException e ) {
				showError("Error inside the reset() method: ", e);
				if( mProxy == null ){
					stopSelf();
				}
			}
		} else {
			showInfo("inside the reset() method..mProxy == null");
			startProxy();
		}
	}

	@Override
	public void onOnHMIStatus(OnHMIStatus onHMIStatus) {
		
		LOGGER.info("inside onOnHMIStatus");
		switch( onHMIStatus.getSystemContext() ) {
			case SYSCTXT_MAIN:
				LOGGER.info("inside SYSCTXT_MAIN");

			case SYSCTXT_VRSESSION:
				LOGGER.info("inside SYSCTXT_VRSESSION");

			case SYSCTXT_MENU:
				LOGGER.info("inside SYSCTXT_MENU");

				break;
			default:
				return;
		}


		if( mProxy == null )
			return;
		
		
		LOGGER.info(" HMI status "+onHMIStatus.getHmiLevel());
		if( onHMIStatus.getHmiLevel().equals( HMILevel.HMI_FULL )) {
			try {
				LOGGER.info("insidde try1 :: HMI status "+onHMIStatus.getHmiLevel());
				sampleSubscribeVehicleData();
				mProxy.show( "Welcome1 to Fuel Signal's", "Ford AppLink Demo", TextAlignment.CENTERED, mCorrelationId++ );
			} catch( SyncException e ) {
				LOGGER.error("SyncException1....",e);
			}
		}
		else if( onHMIStatus.getHmiLevel().equals( HMILevel.HMI_BACKGROUND )) {
			try {
				LOGGER.info("insidde try2 :: HMI status "+onHMIStatus.getHmiLevel());
				sampleSubscribeVehicleData();
				mProxy.show( "Welcome2 to Fuel Signal's", "Ford AppLink Demo", TextAlignment.CENTERED, mCorrelationId++ );
			} catch( SyncException e ) {
				LOGGER.error("SyncException2....",e);
			}
		}
		else if( onHMIStatus.getHmiLevel().equals( HMILevel.HMI_NONE )) {
			//setup app with SYNC
			try {
				LOGGER.info("insidde try3 :: HMI status "+onHMIStatus.getHmiLevel());
				mProxy.show( "Welcome3 to Fuel Signal's", "Ford AppLink Demo", TextAlignment.CENTERED, mCorrelationId++ );
			} catch( SyncException e ) {
				LOGGER.error("SyncException3....",e);
			}
			//subscribeToButtons();
		}
		else if( onHMIStatus.getHmiLevel().equals( HMILevel.HMI_LIMITED )) {
			//setup app with SYNC
			try {
				LOGGER.info("insidde try4 :: HMI status "+onHMIStatus.getHmiLevel());
				sampleSubscribeVehicleData();
				mProxy.show( "Welcome4 to Fuel Signal's", "Ford AppLink Demo", TextAlignment.CENTERED, mCorrelationId++ );
			} catch( SyncException e ) {
				LOGGER.error("SyncException4....",e);
			}
			//subscribeToButtons();
		}
		
	}

		private void playAudio() {
		String url = "http://173.231.136.91:8060/";
		if( mPlayer == null )
			mPlayer = new MediaPlayer();

		try {
			mProxy.show("Loading...", "", TextAlignment.CENTERED, mCorrelationId++);
		} catch( SyncException e ) {}

		mPlayer.reset();
		mPlayer.setAudioStreamType( AudioManager.STREAM_MUSIC );
		try {
			mPlayer.setDataSource(url);
			mPlayer.setOnPreparedListener( new MediaPlayer.OnPreparedListener() {
				@Override
				public void onPrepared( MediaPlayer mediaPlayer ) {
					mediaPlayer.start();
					try {
						mProxy.show("Playing online audio", "", TextAlignment.CENTERED, mCorrelationId++);
					} catch( SyncException e ) {
						
					}
				}
			});
			mPlayer.prepare();
		} catch (IllegalArgumentException e) {
		} catch (SecurityException e) {
		} catch (IllegalStateException e) {
		} catch (IOException e) {
		}
	}

	private void stopAudio() {
		if( mPlayer == null )
			return;
		mPlayer.pause();
		try {
			mProxy.show("Press OK", "to play audio", TextAlignment.CENTERED, mCorrelationId++);
		} catch( SyncException e ) {}
	}

	private void subscribeToButtons() {
		if( mProxy == null )
			return;

		try {
			mProxy.subscribeButton( ButtonName.OK, mCorrelationId++ );
		} catch( SyncException e ) {}
	}

	@Override
	public void onOnButtonPress(OnButtonPress notification) {
		if( ButtonName.OK == notification.getButtonName() ) {
			if( mPlayer != null ) {
				if( mPlayer.isPlaying() ) {
					//stopAudio();
				} else {
					//playAudio();
				}
			}
		}
	}

	@Override
	public void onProxyClosed(String s, Exception e) {
		SyncException syncException = (SyncException) e;
		if( syncException.getSyncExceptionCause() != SyncExceptionCause.SYNC_PROXY_CYCLED  &&
			syncException.getSyncExceptionCause() != SyncExceptionCause.BLUETOOTH_DISABLED ) {
			reset();
		}
	}

	@Override
	public void onError(String s, Exception e) {

	}

	@Override
	public void onGenericResponse(GenericResponse genericResponse) {

	}

	@Override
	public void onOnCommand(OnCommand onCommand) {

	}

	@Override
	public void onAddCommandResponse(AddCommandResponse addCommandResponse) {

	}

	@Override
	public void onAddSubMenuResponse(AddSubMenuResponse addSubMenuResponse) {

	}

	@Override
	public void onCreateInteractionChoiceSetResponse(CreateInteractionChoiceSetResponse createInteractionChoiceSetResponse) {

	}

	@Override
	public void onAlertResponse(AlertResponse alertResponse) {

	}

	@Override
	public void onDeleteCommandResponse(DeleteCommandResponse deleteCommandResponse) {

	}

	@Override
	public void onDeleteInteractionChoiceSetResponse(DeleteInteractionChoiceSetResponse deleteInteractionChoiceSetResponse) {

	}

	@Override
	public void onDeleteSubMenuResponse(DeleteSubMenuResponse deleteSubMenuResponse) {

	}

	@Override
	public void onPerformInteractionResponse(PerformInteractionResponse performInteractionResponse) {

	}

	@Override
	public void onResetGlobalPropertiesResponse(ResetGlobalPropertiesResponse resetGlobalPropertiesResponse) {

	}

	@Override
	public void onSetGlobalPropertiesResponse(SetGlobalPropertiesResponse setGlobalPropertiesResponse) {

	}

	@Override
	public void onSetMediaClockTimerResponse(SetMediaClockTimerResponse setMediaClockTimerResponse) {

	}

	@Override
	public void onShowResponse(ShowResponse showResponse) {

	}

	@Override
	public void onSpeakResponse(SpeakResponse speakResponse) {

	}

	@Override
	public void onOnButtonEvent(OnButtonEvent onButtonEvent) {

	}

	@Override
	public void onSubscribeButtonResponse(SubscribeButtonResponse subscribeButtonResponse) {

	}

	@Override
	public void onUnsubscribeButtonResponse(UnsubscribeButtonResponse unsubscribeButtonResponse) {

	}

	@Override
	public void onOnPermissionsChange(OnPermissionsChange onPermissionsChange) {

	}
	
	
	

	@Override
	public void onUnsubscribeVehicleDataResponse(UnsubscribeVehicleDataResponse unsubscribeVehicleDataResponse) {

	}

	@Override
	public void onGetVehicleDataResponse(GetVehicleDataResponse getVehicleDataResponse) {

		
		showInfo("S onGetVehicleDataResponse Result success value: " +  getVehicleDataResponse.getSuccess());
		showInfo("S onGetVehicleDataResponse Result Info :  " +  getVehicleDataResponse.getInfo());

	    if (getVehicleDataResponse.getResultCode() == Result.DISALLOWED)  {
	    	
	    	showInfo("S2 getVehicleDataResponse DISALLOWED FAIL: " +  getVehicleDataResponse.getSuccess());
	    }
	    else if (getVehicleDataResponse.getResultCode() == Result.USER_DISALLOWED)  {
	    	
	    	showInfo("getVehicleDataResponse USER_DISALLOWED FAIL: " +  getVehicleDataResponse.getSuccess());

	    }
	    else {
	    	
	    	showInfo(".......getVehicleDataResponse SUCCESS.....");

	    }				
		
	}

	@Override
	public void onReadDIDResponse(ReadDIDResponse readDIDResponse) {

	}

	@Override
	public void onGetDTCsResponse(GetDTCsResponse getDTCsResponse) {

	}



	@Override
	public void onPerformAudioPassThruResponse(PerformAudioPassThruResponse performAudioPassThruResponse) {

	}

	@Override
	public void onEndAudioPassThruResponse(EndAudioPassThruResponse endAudioPassThruResponse) {

	}

	@Override
	public void onOnAudioPassThru(OnAudioPassThru onAudioPassThru) {

	}

	@Override
	public void onPutFileResponse(PutFileResponse putFileResponse) {

	}

	@Override
	public void onDeleteFileResponse(DeleteFileResponse deleteFileResponse) {

	}

	@Override
	public void onListFilesResponse(ListFilesResponse listFilesResponse) {

	}

	@Override
	public void onSetAppIconResponse(SetAppIconResponse setAppIconResponse) {

	}

	@Override
	public void onScrollableMessageResponse(ScrollableMessageResponse scrollableMessageResponse) {

	}

	@Override
	public void onChangeRegistrationResponse(ChangeRegistrationResponse changeRegistrationResponse) {

	}

	@Override
	public void onSetDisplayLayoutResponse(SetDisplayLayoutResponse setDisplayLayoutResponse) {

	}

	@Override
	public void onOnLanguageChange(OnLanguageChange onLanguageChange) {

	}

	@Override
	public void onSliderResponse(SliderResponse sliderResponse) {

	}

	@Override
	public void onOnDriverDistraction(OnDriverDistraction onDriverDistraction) {

	}

	@Override
	public void onEncodedSyncPDataResponse(EncodedSyncPDataResponse encodedSyncPDataResponse) {

	}

	@Override
	public void onSyncPDataResponse(SyncPDataResponse syncPDataResponse) {

	}

	@Override
	public void onOnEncodedSyncPData(OnEncodedSyncPData onEncodedSyncPData) {

	}

	@Override
	public void onOnSyncPData(OnSyncPData onSyncPData) {

	}

	@Override
	public void onOnTBTClientState(OnTBTClientState onTBTClientState) {

	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onOnAppInterfaceUnregistered(OnAppInterfaceUnregistered arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProxyOpened() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRegisterAppInterfaceResponse(RegisterAppInterfaceResponse arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUnregisterAppInterfaceResponse(
			UnregisterAppInterfaceResponse arg0) {
		// TODO Auto-generated method stub
		
	}
	
	
	public void showInfo(String msg){
		LOGGER.info(msg);
	}
	
	public void showError(String msg,Exception e){
		LOGGER.error(msg,e);
	}
	
	//Listen for callbacks inside of the service
		//Listen for a positive or negative response to the subscription request in onSubscribeVehicleDataResponse
		@Override
		public void onSubscribeVehicleDataResponse(SubscribeVehicleDataResponse subscribeVehicleDataResponse) {
			

				showInfo("S onSubscribeVehicleDataResponse Result success value: " +  subscribeVehicleDataResponse.getSuccess());
				showInfo("S onSubscribeVehicleDataResponse Result Info :  " +  subscribeVehicleDataResponse.getInfo());

			    if (subscribeVehicleDataResponse.getResultCode() == Result.DISALLOWED)  {
			    	
			    	showInfo("S2 onSubscribeVehicleDataResponse DISALLOWED FAIL: " +  subscribeVehicleDataResponse.getSuccess());
			    }
			    else if (subscribeVehicleDataResponse.getResultCode() == Result.USER_DISALLOWED)  {
			    	
			    	showInfo("onSubscribeVehicleDataResponse USER_DISALLOWED FAIL: " +  subscribeVehicleDataResponse.getSuccess());

			    }
			    else {
			    	showInfo(".......onSubscribeVehicleDataResponse SUCCESS.....");

			    }
		}
		
	
	// After a positive response from onSubscribeVehicleDataResponse, listen for
	// Vehicle Data notifications in onOnVehicleData
	@Override
	public void onOnVehicleData(OnVehicleData onVehicleData) {
		showInfo("Inside onVehicleData method");
		
		try {
			if (onVehicleData.getFuelLevel() != null) {
				showInfo("getFuelLevel" + "\n S getFuelLevel: "
						+ onVehicleData.getFuelLevel());
				sendFuelLevelToScreen(onVehicleData.getFuelLevel()+"");
			}
		} catch (Exception e) {
			showError("Error inside onOnVehicleData", e);
		}
	} 
	
	
	public void sampleSubscribeVehicleData()
	{
		
		showInfo("Inside sampleSubscribeVehicleData method");

	    //Build Request and send to proxy object:
	    int corrId = mCorrelationId++;
	    SubscribeVehicleData msg = new SubscribeVehicleData();
	    msg.setFuelLevel(true);
	    msg.setCorrelationID(corrId);
	    try{
			showInfo("Creating Subscribe vehicle request :"+ msg);
	    	mProxy.sendRPCRequest(msg);
	    }
	    catch (SyncException e) {
	    	showError("sync exception", e);
	    }
	    

	    GetVehicleData msg2 = new GetVehicleData();
	    msg2.setFuelLevel(true);
	    msg2.setCorrelationID( mCorrelationId++);
	    
	    try {
	    	
	    	showInfo("Creating Get vehicle request :"+ msg2);
	    	mProxy.sendRPCRequest(msg2);
	    }
	    catch (SyncException e) {
	    	showError("sync exception", e);
	    }
	    
	}
	
	

	//This will sent the notification to main activity screen for the updated values
	private void sendFuelLevelToScreen(String fuelLevel) {
		try {
			showInfo("Sending fuel level to main screen :"+ fuelLevel);
			if (MainActivity.mHandler != null) {
				MainActivity.mHandler.obtainMessage(1,fuelLevel).sendToTarget();
			}
		} catch (Exception e) {
			showError("Error inside sendFuelLevelToScreen", e);
		}
	}
}
