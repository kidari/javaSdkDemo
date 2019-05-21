package com.huawei.iotplatform.BasicTest.test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import com.huawei.iotplatform.client.invokeapi.Authentication;
import com.huawei.iotplatform.client.invokeapi.DeviceManagement;
import com.huawei.iotplatform.client.NorthApiClient;
import com.huawei.iotplatform.client.NorthApiException;
import com.huawei.iotplatform.client.dto.AuthOutDTO;
import com.huawei.iotplatform.client.dto.ClientInfo;
import com.huawei.iotplatform.client.dto.ModifyDeviceInforInDTO;
import com.huawei.iotplatform.client.dto.RegDirectDeviceInDTO2;
import com.huawei.iotplatform.client.dto.RegDirectDeviceOutDTO;
import com.huawei.iotplatform.client.dto.SSLConfig;
import com.huawei.iotplatform.client.testapi.BasicTest;
import com.huawei.iotplatform.utils.PropertyUtil;

public class Test {
	
	private static volatile int a = 0;
	private static String accessToken = null;
	
	private static DeviceManagement deviceManagement;
	
	public static void main(String[] args) {
		/**---------------------test basic function of the APIs------------------------*/
		//BasicTest will help to test the basic API and find out if there's a problem or not.
		System.out.println("============test with inner certificates only============");
		
		PropertyUtil.init("./src/main/resources/application.properties");
		
//		BasicTest.beginBasicTest(PropertyUtil.getProperty("platformIp"), PropertyUtil.getProperty("platformPort"), 
//				PropertyUtil.getProperty("appId"), AuthUtil.getAesPropertyValue("secret"), null);
		
		BasicTest.beginBasicTest(PropertyUtil.getProperty("platformIp"), PropertyUtil.getProperty("platformPort"), 
				PropertyUtil.getProperty("appId"), PropertyUtil.getProperty("secret"), null);
		
		
		//use ca.jks and outgoing.CertwithKey.pkcs12 as commercial certificates, and see what happened ~ ~
		System.out.println("\n===========test with both inner certificates and outer certificates=============");
		
		SSLConfig sslconfig = new SSLConfig();
		sslconfig.setTrustCAPath("./src/main/resources/ca.jks");
		sslconfig.setTrustCAPwd("Huawei@123");
		sslconfig.setSelfCertPath("./src/main/resources/outgoing.CertwithKey.pkcs12");
		sslconfig.setSelfCertPwd("IoM@1234");
		
//		BasicTest.beginBasicTest(PropertyUtil.getProperty("platformIp"), PropertyUtil.getProperty("platformPort"), 
//				PropertyUtil.getProperty("appId"), AuthUtil.getAesPropertyValue("secret"), sslconfig);
		
		BasicTest.beginBasicTest(PropertyUtil.getProperty("platformIp"), PropertyUtil.getProperty("platformPort"), 
				PropertyUtil.getProperty("appId"), PropertyUtil.getProperty("secret"), sslconfig);
		
		/**-----------------concurrent test--------------------*/
		NorthApiClient northApiClient = new NorthApiClient();
		ClientInfo clientInfo = new ClientInfo();
		clientInfo.setAppId(PropertyUtil.getProperty("appId"));
		clientInfo.setSecret(PropertyUtil.getProperty("secret"));
		clientInfo.setPlatformIp(PropertyUtil.getProperty("platformIp"));
		clientInfo.setPlatformPort(PropertyUtil.getProperty("platformPort"));
		
		Authentication authentication = new Authentication(northApiClient);
		deviceManagement = new DeviceManagement(northApiClient);
		
		try {
			northApiClient.setClientInfo(clientInfo);
			northApiClient.initSSLConfig();
			AuthOutDTO adto = authentication.getAuthToken();
			accessToken = adto.getAccessToken();			
			
		} catch (NorthApiException e) {
			// TODO Auto-generated catch block
			System.out.println("Test, concurrent test auth exception " + e.toString());
		}
		
		for (int i = 0; i < 3000; i++) {
			
			Thread t = new Thread() {
				@Override
				public void run() {
					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.mmm");
					System.out.println(df.format(new Date()));
					a++;
					System.out.println("\n===============" + a + " " + accessToken);
					try {
						RegDirectDeviceOutDTO rddod = registerDevice(deviceManagement, accessToken, 5);
						if (rddod != null) {
							System.out.println("\n===============" + a + " " + rddod.toString());
							modifyDeviceInfo(deviceManagement, accessToken, rddod.getDeviceId(), a + "");
							
							deviceManagement.deleteDirectDevice(rddod.getDeviceId(), true, null, accessToken);		
							
						}	
					} catch (NorthApiException e) {
						System.out.println("Test, concurrent test thread exception " + e.toString());
//						if (e.getHttpMessage().contains("app throttle exceed")) {
//							//TODO
//						}
					}
					
					System.out.println(df.format(new Date()));
				}
			};
			
//			try {				
//				Random random = new Random();
//				t.sleep(random.nextInt(500) + 50);
//				
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				System.out.println("Test, concurrent test thread sleep exception " + e.getMessage());
//			}

			t.start();
		}
		
		
	}	
	
	 private static RegDirectDeviceOutDTO registerDevice(DeviceManagement deviceManagement, String accessToken, int timeout) {
    	//fill input parameters
        RegDirectDeviceInDTO2 rddid = new RegDirectDeviceInDTO2();
        Random random = new Random();	
		String nodeid = "testdemo" + (random.nextInt(9000000) + 1000000); //this is a test imei
        String verifyCode = nodeid;
        rddid.setNodeId(nodeid);
        rddid.setVerifyCode(verifyCode);
        rddid.setTimeout(timeout);
                
		try {
			RegDirectDeviceOutDTO rddod = deviceManagement.regDirectDevice(rddid, null, accessToken);
			System.out.println(rddod.toString());
			return rddod;
		} catch (NorthApiException e) {
			System.out.println(e.toString());
		}
		return null;        
    }
	
	 private static void modifyDeviceInfo(DeviceManagement deviceManagement, String accessToken, String deviceId, String deviceName) {
    	ModifyDeviceInforInDTO mdiInDTO = new ModifyDeviceInforInDTO();
        mdiInDTO.setName(deviceName);
        mdiInDTO.setDeviceType("WaterMeter");
        mdiInDTO.setManufacturerId("AAAA");
        mdiInDTO.setManufacturerName("AAAA");
        mdiInDTO.setModel("AAAA");
        mdiInDTO.setProtocolType("CoAP");
        try {
			deviceManagement.modifyDeviceInfo(mdiInDTO, deviceId, null, accessToken);
			System.out.println("modify device info succeeded");
		} catch (NorthApiException e) {
			System.out.println(e.toString());
		}
    }
}
