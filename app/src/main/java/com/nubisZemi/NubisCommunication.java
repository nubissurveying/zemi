package com.nubisZemi;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.zip.InflaterInputStream;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.widget.Toast;

public class NubisCommunication implements NubisAsyncResponse{
	
	private static Handler handler = null;
	
	public NubisDelayedAnswer delayedAnswer;
    public int serverResponseCode = -1;
    public String serverResponseMessage = "";
    public NubisDelayedAnswers delayedAnswers = new NubisDelayedAnswers();
    public SharedPreferences preferencesReader;
    String HTTPReturnString = "";
    private Context context;
    public boolean readSettingsDone = false;
    
    public NubisCommunication(){
    }
    
    public int getUnsentCount(){
    	int unsent = 0;
    	for (int i = delayedAnswers.getDelayedAnswerCount() - 1; i >= 0; i--){
            if (!delayedAnswers.getDelayedAnswer(i).sent){
              unsent ++;	
            }
    	}
        return unsent;
    	
    }


    
    public void loadDelayedAnswers(SharedPreferences preferencesReader){
  	  try {
  		this.preferencesReader = preferencesReader;
    	String serializedDataFromPreference = this.preferencesReader.getString("DelayedAnswers", null);
		if (serializedDataFromPreference != null){ // Create a new object from the serialized data with the same state
			delayedAnswers = NubisDelayedAnswers.create(serializedDataFromPreference);
		}
		else {
			delayedAnswers = new NubisDelayedAnswers();
		}
  	  }
	  catch (Exception e) {
	    e.printStackTrace();
	  }
    }

    public void storeDelayedAnswers(){
    	try {
	    	if (this.preferencesReader != null){
				// Serialize the object into a string
				String serializedData = delayedAnswers.serialize();
				// Save the serialized data into a shared preference
				SharedPreferences.Editor editor = this.preferencesReader.edit();
				editor.putString("DelayedAnswers", serializedData);
				editor.commit();
	    	}
  	    }
		catch (Exception e) {
		    e.printStackTrace();
		}
    }
    
    
	public void addNubisDelayedAnswer(NubisDelayedAnswer delayedAnswer){
		this.delayedAnswer = delayedAnswer;
	}
	
	public void sendOrStoreLocal(Context context){
		this.context = context;
		delayedAnswers.addDelayedAnswer(this.delayedAnswer);
		storeDelayedAnswers();
		checkInternet(context);
	}
	
	public void	checkInternet(Context context){
	  if ( ((NubisApplication)context.getApplicationContext()).hasInternet){
  		  //connection: send all!
		  //check if the server can be reached!
		  if (testConnectionToServer(context)){
		    // sendDelayedAnswer(context); --> this is done in the return from connection check
		  }
	  }
	}
	
	public void setHandler(Handler handler){
	    this.handler = handler;
	}
	
	public void updateFromServer(Context context, Handler handler){
	    this.handler = handler;
		NubisDelayedAnswer delayedanswer = new NubisDelayedAnswer(NubisDelayedAnswer.N_GET_READ);
		delayedanswer.addGetParameter("id", Secure.getString(context.getContentResolver(), Secure.ANDROID_ID));
		
		TelephonyManager tMgr = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		delayedanswer.addGetParameter("phonenr", tMgr.getLine1Number());
		try {
		  delayedanswer.addGetParameter("version", context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName);
		}
		catch (Exception e){
			//forget the version number..
		}		  
		delayedanswer.addGetParameter("p", "settings");
	
	    String serverResponseMessage = ((NubisApplication)context.getApplicationContext()).communication.upLoad(context, delayedanswer, false, -1, NubisHTTP.H_DOWNLOAD);
    }
	
	
	
	public void handleNoConnection(Context context){
		Toast.makeText(context,"connection failed", Toast.LENGTH_LONG).show();
	/*	 new AlertDialog.Builder(context)
         .setMessage("Server could not be contacted. Do you want to retry?")
         .setCancelable(false)
         .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
                  
             }
         })
         .setNegativeButton("No", null)
         .show();	*/
	}
	
	
	public void sendDelayedAnswer(Context context){
		this.context = context;
		//DELETE THE ONES THAT WERE SENT BEFORE: ASYNC SO NOT AFTERWARDS
		for (int i = delayedAnswers.getDelayedAnswerCount() - 1; i >= 0; i--){
          if (delayedAnswers.getDelayedAnswer(i).sent){
        	  if (delayedAnswers.getDelayedAnswer(i).getType() == NubisDelayedAnswer.N_POST_FILE){
  				File file = new File(delayedAnswers.getDelayedAnswer(i).POST_fileName);
  				file.delete();
  			  }
  			  delayedAnswers.deleteDelayedAnswer(i);
          }
		}
        storeDelayedAnswers(); ///update!
		//NOW SEND
        for (int i = delayedAnswers.getDelayedAnswerCount() - 1; i >= 0; i--){
        	if (delayedAnswers.getDelayedAnswer(i).sent == false){  //not sent already (should have been deleted above)
        	  upLoad(context, delayedAnswers.getDelayedAnswer(i), false, i, NubisHTTP.H_UPLOAD);
        	}
		}
		
	}
	
	public static boolean hasService(InetAddress host, int port) throws IOException{
		   boolean   status = false;
		   Socket    sock = new Socket();
		   try
		   {
		      sock.connect(new InetSocketAddress(host, port), 2000);
		      if (sock.isConnected())
		      {
		         sock.close();
		         status = true;
		      }
		   }
		   catch (Exception ex) {
			   
		   }
		   return status;
		}

	
	
	public boolean checkRuntime(String server){
		Runtime runtime = Runtime.getRuntime();
        Process proc;
        try {
            proc = runtime.exec("ping " + server + " -c 1");
            proc.waitFor();
            int exit = proc.exitValue();
            if (exit == 0) { // normal exit
              return true;

            } else { // abnormal exit, so decide that the server is not
                        // reachable


            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } // other servers, for example
        catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
	}
	
	public boolean canConnect(InetAddress address, int port) {
	    Socket socket = new Socket();
	    
	    SocketAddress socketAddress = new InetSocketAddress(address, port);
	    
	    try {
	        // Only try for 2 seconds before giving up
	        socket.connect(socketAddress, 2000);
	    }
	    catch (IOException e) {
	        // Something went wrong during the connection
	        return false; 
	    }
	    finally {
	        // Always close the socket after we're done
	        if (socket.isConnected()) {
	            try {
	                socket.close();
	            }
	            catch (IOException e) {
	                // Nothing we can do here
	                e.printStackTrace();
	            }
	        }
	    }
	    
	    return true;
	}
	
	
	public boolean testConnectionToServer(Context context){

		/*
		try {
	      String URL = ((NubisApplication)context.getApplicationContext()).settings.serverURL;
	      int slashslash = URL.indexOf("//") + 2;
	      String domain = URL.substring(slashslash, URL.indexOf('/', slashslash));
	      //InetAddress myAddress = InetAddress.getByName("128.125.142.97"); //fix for 4.4.2
	      InetAddress myAddress = InetAddress.getByAddress(new byte[] {(byte)129, (byte)125, (byte) 142, (byte) 97});
		  //return this.canConnect(myAddress, 1);
	      //return myAddress.isReachable(1000);
	      return this.checkRuntime("128.125.142.97");
		}
		catch(Exception e){
			e.printStackTrace();
            ((NubisApplication)context.getApplicationContext()).log += e.getMessage();

		}
		return false;
		*/
		
		this.context = context;
		boolean connected = false;
		HTTPReturnString = "";
		try {
			NubisDelayedAnswer delayedanswer = new NubisDelayedAnswer(NubisDelayedAnswer.N_CHECK_SERVER);
			delayedanswer.addGetParameter("id", Secure.getString(context.getContentResolver(), Secure.ANDROID_ID));
			delayedanswer.addGetParameter("version", context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName);
			delayedanswer.addGetParameter("p", "checkconnection");			
			upLoad(context, delayedanswer, true, -1, NubisHTTP.H_CHECK_SERVER);

	        connected = (HTTPReturnString.equals("SERVER UP!!!"));			
		} catch (Exception e){
			e.printStackTrace();
		}
        return connected; //return value is doing nothing!!
	}

	
	
	public String upLoad(Context context, NubisDelayedAnswer delayedAnswer, boolean wait, int deleteId, int communicationType){
		//Context context, NubisDelayedAnswer delayedAnswer, NubisAsyncResponse delegate
		this.context = context;
		try {
			NubisHTTP httpCom = new NubisHTTP(context, delayedAnswer, this, deleteId, communicationType);
			if (wait){
				httpCom.serverInstructions = "";
				httpCom.execute(); //doInBackground();//.get(210000, TimeUnit.MILLISECONDS);
				
				long startTime = System.currentTimeMillis();
				while(httpCom.serverInstructions == ""){
					 if ((System.currentTimeMillis()-startTime)>5000){ break; } //timeout!
		               // waiting until finished protected String[] doInBackground(Void... params)          
		              } 
				HTTPReturnString = httpCom.serverInstructions;
				
//				httpCom.execute().get(10000, TimeUnit.MILLISECONDS);
			/*	while (HTTPReturnString == "") {
				    try { Thread.sleep(100); }
				    catch (InterruptedException e) { e.printStackTrace(); }
				}*/
			}
			else {
				httpCom.execute();
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		/*
		Context context = tempcontent;
		NubisDelayedAnswer delayedAnswer = tempdelayedanswer;
	    //URLEncoder.encode("value1", "UTF-8");
		try {
			
			if (delayedAnswer.getType() == NubisDelayedAnswer.N_GET){
			      String upLoadServerUri = ((NubisApplication)context.getApplicationContext()).settings.serverURL + "?q=" + Encrypt(delayedAnswer.getGetString());
			      URL url = new URL(upLoadServerUri);
			      HttpURLConnection conn = (HttpURLConnection) url.openConnection();  // Open a HTTP  connection to  the URL
			      serverResponseCode = conn.getResponseCode();
			      serverResponseMessage = conn.getResponseMessage();
	              //((NubisApplication)context.getApplicationContext()).settings.readSettingsFromString(serverResponseMessage);
			      return serverResponseMessage;
			}
            if (delayedAnswer.getType() == NubisDelayedAnswer.N_GET_READ){
			      String upLoadServerUri = ((NubisApplication)context.getApplicationContext()).settings.serverURL + "?q=" + Encrypt(delayedAnswer.getGetString());
			      URL url = new URL(upLoadServerUri);
			      HttpURLConnection conn = (HttpURLConnection) url.openConnection();  // Open a HTTP  connection to  the URL
 			      conn.setConnectTimeout(5000);  //need timeout here!
			      serverResponseCode = conn.getResponseCode();
			      serverResponseMessage = conn.getResponseMessage();
			      if(!String.valueOf(serverResponseCode).startsWith("2")){
			    		//  error!!
			    	  return serverResponseMessage;
                  }			
			      else {
				      BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				      String str; String response = "";
				      while ((str = in.readLine()) != null) {
		        	    response += (str +"\n");
		              }
		              in.close();   
		              
	                  response = this.Uncompress(response); //uncompress
	                  return Unencrypt(response).trim(); //unencrypt
			      }

	              
            }
			
			if (delayedAnswer.getType() == NubisDelayedAnswer.N_POST){
				  String upLoadServerUri = ((NubisApplication)context.getApplicationContext()).settings.serverURL + "?q=" + Encrypt(delayedAnswer.getGetString());
			      URL url = new URL(upLoadServerUri);
			      HttpURLConnection conn = (HttpURLConnection) url.openConnection(); 
	              conn.setDoInput(true); // Allow Inputs
	              conn.setDoOutput(true); // Allow Outputs
	              conn.setUseCaches(false); // Don't use a Cached Copy
	              conn.setRequestMethod("POST");
	              conn.setRequestProperty("Connection", "Keep-Alive");
	              conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	               
	              byte[] outputInBytes = delayedAnswer.getPostData().getBytes("UTF-8");
	              OutputStream os = conn.getOutputStream();

	              serverResponseCode = conn.getResponseCode();
	              serverResponseMessage = conn.getResponseMessage();
	               
	               
	              os.write( outputInBytes );    
	              os.close();
	              //((NubisApplication)context.getApplicationContext()).settings.readSettingsFromString(serverResponseMessage);
	              return serverResponseMessage;
	               
			}
			if (delayedAnswer.getType() == NubisDelayedAnswer.N_POST_FILE){
				  String upLoadServerUri = ((NubisApplication)context.getApplicationContext()).settings.serverURL + "?q=" + Encrypt(delayedAnswer.getGetString());
			      URL url = new URL(upLoadServerUri);
			      HttpURLConnection conn = (HttpURLConnection) url.openConnection(); 
	              conn.setDoInput(true); // Allow Inputs
	              conn.setDoOutput(true); // Allow Outputs
	              conn.setUseCaches(false); // Don't use a Cached Copy
	              conn.setRequestMethod("POST");
	              conn.setRequestProperty("Connection", "Keep-Alive");
	              conn.setRequestProperty("ENCTYPE", "multipart/form-data");
	              conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + NubisDelayedAnswer.N_boundary);
	              conn.setRequestProperty("uploaded_file", "test"); 
				
	              DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
	               
	              dos.writeBytes(NubisDelayedAnswer.N_twoHyphens + NubisDelayedAnswer.N_boundary + NubisDelayedAnswer.N_lineEnd); 
	              dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + "test" + "\"" + NubisDelayedAnswer.N_lineEnd);
	               
	              dos.writeBytes(NubisDelayedAnswer.N_lineEnd);
	              delayedAnswer.getByteArrayOutputStream().writeTo(dos);
	               
	               // send multipart form data necesssary after file data...
	              dos.writeBytes(NubisDelayedAnswer.N_lineEnd);
	              dos.writeBytes(NubisDelayedAnswer.N_twoHyphens + NubisDelayedAnswer.N_boundary + NubisDelayedAnswer.N_twoHyphens + NubisDelayedAnswer.N_lineEnd);
	     
	               // Responses from the server (code and message)
	              serverResponseCode = conn.getResponseCode();
	              serverResponseMessage = conn.getResponseMessage();
	              
	              dos.flush();
	              dos.close();
	              
	              //((NubisApplication)context.getApplicationContext()).settings.readSettingsFromString(serverResponseMessage);
	              return serverResponseMessage;
			}
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}*/
        return null;	
	}
	
	public static String Unencrypt(String inputStr){
	    try {
			  MCrypt mcrypt = new MCrypt();
			  return new String( mcrypt.decrypt( inputStr) );
		} catch (Exception e) {
			e.printStackTrace();
		}
		return inputStr;
	}
	
	public static String Encrypt(String inputStr){
		try {
			MCrypt mcrypt = new MCrypt();
			return MCrypt.bytesToHex( mcrypt.encrypt(inputStr) );
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		return inputStr;
	}
	
	
	public static String Uncompress(String inputStr){
	    String unzipped = "";
	    try {
			byte[] zbytes = Base64.decode(inputStr, Base64.DEFAULT);
	    	// byte[] zbytes = zippedText.getBytes("ISO-8859-1");
	        // Add extra byte to array when Inflater is set to true
	        byte[] input = new byte[zbytes.length + 1];
	        System.arraycopy(zbytes, 0, input, 0, zbytes.length);
	        input[zbytes.length] = 0;
	        ByteArrayInputStream bin = new ByteArrayInputStream(input);
	        InflaterInputStream in = new InflaterInputStream(bin);
	        ByteArrayOutputStream bout = new ByteArrayOutputStream(512);
	        int b;
	        while ((b = in.read()) != -1) {
	            bout.write(b); }
	        bout.close();
	        unzipped = bout.toString();
	    }
	    catch (IOException io) {
	    
	    }
	    catch (Exception e){
	    	
	    }
	    return unzipped;
		
	}

	@Override
	public void processFinish(String output, int responseCode, String responseString, NubisDelayedAnswer delayedAnswer, int deleteId) {
		HTTPReturnString = output; 
	    
	//	this.context.notify();
		
	    if (delayedAnswer.getType() == NubisDelayedAnswer.N_GET){

		}
		else if (delayedAnswer.getType() == NubisDelayedAnswer.N_CHECK_SERVER){
	      if (HTTPReturnString != null && HTTPReturnString != ""){
			  if (HTTPReturnString.equals("SERVER UP!!!")){ //server up check!
				  sendDelayedAnswer(context); //upload delayed answers				  
			  }
			  else {
				  handleNoConnection(context); 
			  }
	      }
	      else {
	    	handleNoConnection(context);
	      }
		}
		else if (delayedAnswer.getType() == NubisDelayedAnswer.N_GET_READ){
		  if (HTTPReturnString != ""){
	          ((NubisApplication)context.getApplicationContext()).settings.readSettingsFromString(HTTPReturnString);
	  		  //save settings
	          ((NubisApplication)context.getApplicationContext()).saveSettings();
			  //update application settings
			  ((NubisApplication)context.getApplicationContext()).clear();
    		  readSettingsDone = true;
   	       }
        }	
		else if (delayedAnswer.getType() == NubisDelayedAnswer.N_POST){
             
		}
		else if (delayedAnswer.getType() == NubisDelayedAnswer.N_POST_FILE){
			
			
		}

		if (deleteId != -1){ //mark as 'sent' so next time they will get deleted.
			delayedAnswers.getDelayedAnswer(deleteId).sent = true;
			storeDelayedAnswers(); ///update!
		}
			
        if (handler != null){
       	   handler.obtainMessage(1, "done").sendToTarget();
       	   handler = null;
        }
		
	}


	
}
