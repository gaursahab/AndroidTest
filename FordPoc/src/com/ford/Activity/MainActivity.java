package com.ford.Activity;


import org.apache.log4j.Logger;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.ford.R;
import com.ford.Service.AppLinkService;
import com.ford.syncV4.proxy.SyncProxyALM;
import de.mindpipe.android.logging.log4j.LogConfigurator;


public class MainActivity extends Activity {
	//Logging Customization......
    private final String LOG_FILE_NAME = "fuelSignalFord.log";
    private final String LOG_FILE_PATTERN = "%d - [%c] - %p : %m%n";
    private final int LOG_FILE_MAX_BACKUP_SIZE = 10;
    private final int LOG_FILE_MAX_SIZE = 1024 * 1024;
    TextView fuelTextView;
	private static final Logger LOGGER = Logger.getLogger(MainActivity.class);
	public static Handler mHandler;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fuelTextView =(TextView)findViewById(R.id.fuelTextView);
        initLog();
        mHandler = new IncomingHandler();
    }

	private void startSyncProxyService() {
		LOGGER.info("startSyncProxyService Successfully");
		try{
			boolean isPaired = false;
			BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

			if( btAdapter != null ) {
				if( btAdapter.isEnabled() && btAdapter.getBondedDevices() != null && !btAdapter.getBondedDevices().isEmpty() ) {
					for( BluetoothDevice device : btAdapter.getBondedDevices() ) {
						if( device.getName() != null && device.getName().contains( getString( R.string.device_name ) ) ) {
							isPaired = true;
							break;
						}
					}
				}

				if( isPaired ) {
					LOGGER.info("Device is paired.............");
					if( AppLinkService.getInstance() == null ) {
						LOGGER.info("AppLinkService.getInstance() == null");
						Intent appLinkServiceIntent = new Intent( this, AppLinkService.class );
						startService( appLinkServiceIntent );
					} else {
						
						SyncProxyALM proxyInstance = AppLinkService.getInstance().getProxy();
						LOGGER.info("proxyInstance"+proxyInstance);
						if( proxyInstance == null ) {
							AppLinkService.getInstance().startProxy();
						}
					}
				}
			}
		}catch(Exception e){
			LOGGER.error("Error inside startSyncProxyService method ",e);
		}

	}

	private void endSyncProxyInstance() {
		try{
			if( AppLinkService.getInstance() != null ) {
				SyncProxyALM proxy = AppLinkService.getInstance().getProxy();
				if( proxy != null ) {
					AppLinkService.getInstance().reset();
				} else {
					AppLinkService.getInstance().startProxy();
				}
			}
		}catch(Exception e){
			LOGGER.error("Error inside endSyncProxyInstance()",e);
		}
	}

	@Override
	protected void onDestroy() {
		endSyncProxyInstance();
		super.onDestroy();
	}
	
	public void onClickConnectFord(View v){
		Log.e("connect to ford" , "done");
		startSyncProxyService();
		
		
	}
	
    private void initLog(){
        /**
        * To load logging file so that we can log in a file
        */
        try {
                String userDir;
                if (android.os.Environment.getExternalStorageState().equals(
                        android.os.Environment.MEDIA_MOUNTED)) {
                    userDir = Environment.getExternalStorageDirectory().getPath() +
                            System.getProperty("file.separator");
                } else {
                    userDir = getApplicationContext().getFilesDir().getAbsolutePath() +
                            System.getProperty("file.separator");
                }
                LogConfigurator logConfigurator = new LogConfigurator();
                logConfigurator.setFileName(userDir + System.getProperty("file.separator") + LOG_FILE_NAME);
                logConfigurator.setFilePattern(LOG_FILE_PATTERN);
                logConfigurator.setMaxBackupSize(LOG_FILE_MAX_BACKUP_SIZE);
                logConfigurator.setMaxFileSize(LOG_FILE_MAX_SIZE);
                logConfigurator.configure();                                       
        } catch (Throwable e) {//Logger should not be able to break the process if any functionality is running.
                       e.printStackTrace();
        }
                    
    }
    
    public void onClickFuelLevel(View v){
    	LOGGER.info("onClickFuelLevel..");
    	LOGGER.info("Checking the fuel level.........");
    	fuelTextView.setText("Fuel Level: 0");
		
	}
    
    
	private class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			try {
				LOGGER.info("Response from service----------msg.what "
						+ msg.what);
				switch (msg.what) {
				case 1:
					fuelTextView.setText(msg.obj.toString());
					break;
				default:
					super.handleMessage(msg);
				}
			} catch (Exception e) {
				LOGGER.error(
						"Error inside the IncomingHandler class handleMessage method::",
						e);
			}
		}
	}

}
