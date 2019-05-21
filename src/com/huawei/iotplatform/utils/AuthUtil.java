package com.huawei.iotplatform.utils;

import com.huawei.iotplatform.client.NorthApiClient;
import com.huawei.iotplatform.client.NorthApiException;
import com.huawei.iotplatform.client.dto.ClientInfo;

public class AuthUtil {
	
	private static NorthApiClient northApiClient = null;
	
	public static NorthApiClient initApiClient() {
		if (northApiClient != null) {
			return northApiClient;
		}
		northApiClient = new NorthApiClient();

        PropertyUtil.init("./src/main/resources/application.properties");
		
		ClientInfo clientInfo = new ClientInfo();
        clientInfo.setPlatformIp(PropertyUtil.getProperty("platformIp"));
        clientInfo.setPlatformPort(PropertyUtil.getProperty("platformPort"));
        clientInfo.setAppId(PropertyUtil.getProperty("appId"));
        clientInfo.setSecret(PropertyUtil.getProperty("secret"));
//        clientInfo.setSecret(getAesPropertyValue("secret"));
        
        try {
			northApiClient.setClientInfo(clientInfo);
			northApiClient.initSSLConfig();
		} catch (NorthApiException e) {
			System.out.println(e.toString());
		}        
        
        return northApiClient;
    }
	
	public static String getAesPropertyValue(String propertyName) {
		String aesPwd = "123987"; //this is a test AES password
        
//      String originalProperty = "2fvT3PUazXev3pv_8jW4QH6Oq2Ya";
//      byte[] temp = AesUtil.encrypt(originalProperty, aesPwd);
//      String hexStrResult = HexParser.parseByte2HexStr(temp);
//      System.out.println("==>encrypted secret hex sting is ："  + hexStrResult);
      
		PropertyUtil.init("./src/main/resources/application.properties");
		byte[] secret = HexParser.parseHexStr2Byte(PropertyUtil.getProperty(propertyName));
		return new String(AesUtil.decrypt(secret, aesPwd));
	}
}
