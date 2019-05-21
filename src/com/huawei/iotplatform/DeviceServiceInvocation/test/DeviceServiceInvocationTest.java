package com.huawei.iotplatform.DeviceServiceInvocation.test;

import java.util.HashMap;
import java.util.Map;

import com.huawei.iotplatform.client.NorthApiClient;
import com.huawei.iotplatform.client.NorthApiException;
import com.huawei.iotplatform.client.dto.AuthOutDTO;
import com.huawei.iotplatform.client.dto.CommandDTO2;
import com.huawei.iotplatform.client.dto.CommandNA2CloudHeader;
import com.huawei.iotplatform.client.dto.InvokeDeviceServiceOutDTO;
import com.huawei.iotplatform.client.invokeapi.Authentication;
import com.huawei.iotplatform.client.invokeapi.DeviceServiceInvocation;
import com.huawei.iotplatform.utils.AuthUtil;

public class DeviceServiceInvocationTest {
	
    public static void main(String args[]) throws Exception
    {
    	/**---------------------initialize northApiClient------------------------*/
    	NorthApiClient northApiClient = AuthUtil.initApiClient();
    	DeviceServiceInvocation deviceServiceInvocation = new DeviceServiceInvocation(northApiClient);
        
        /**---------------------get accessToken at first------------------------*/
        Authentication authentication = new Authentication(northApiClient);        
        AuthOutDTO authOutDTO = authentication.getAuthToken();        
        String accessToken = authOutDTO.getAccessToken();
        
        /**---------------------invoke device service (send service command to device with agent/agentLite installed)------------------------*/
        //this is a test device with agent/agentLite installed, or is a sub-device under an agent gateway
        String deviceId = "09691354-872d-49a1-9c1c-a18512f10ae2";
        
        System.out.println("======invoke device service======");
        InvokeDeviceServiceOutDTO idsOutDTO = modifyBrightness(deviceServiceInvocation, deviceId, 86, accessToken);
        if (idsOutDTO != null) {
        	System.out.println(idsOutDTO.toString());
		}
    }
    
    private static InvokeDeviceServiceOutDTO modifyBrightness(DeviceServiceInvocation deviceServiceInvocation, String deviceId, int brightness,String accessToken) {
    	CommandDTO2 cmdDTO = new CommandDTO2();
        CommandNA2CloudHeader cmdHeader = new CommandNA2CloudHeader();
        cmdHeader.setMode("NOACK");//set mode to NOACK or ACK according to the business quest
        cmdHeader.setMethod("PUT");//"PUT" is the command name defined in the profile
        cmdDTO.setHeader(cmdHeader);
        
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("brightness", brightness);//"brightness" is the command parameter name defined in the profile
        cmdDTO.setBody(body);
        
        //"Brightness" is the serviceId defined in the profile
        InvokeDeviceServiceOutDTO idsOutDTO;
		try {
			/**---------------------invoke device service------------------------*/
			String serviceId = "Brightness";//"Brightness" is the serviceId defined in the profile
			idsOutDTO = deviceServiceInvocation.invokeDeviceService(deviceId, serviceId, cmdDTO, null, accessToken);
			return idsOutDTO;
		} catch (NorthApiException e) {
			if ("100428".equals(e.getError_code())) {
				System.out.println("please make sure the device is online");
			}
			System.out.println(e.toString());
		}
        return null;
    }
}
