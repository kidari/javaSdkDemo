package com.huawei.iotplatform.DeviceManagement.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.huawei.iotplatform.client.NorthApiClient;
import com.huawei.iotplatform.client.NorthApiException;
import com.huawei.iotplatform.client.dto.AuthOutDTO;
import com.huawei.iotplatform.client.dto.ModifyDeviceInforInDTO;
import com.huawei.iotplatform.client.dto.ModifyDeviceShadowInDTO;
import com.huawei.iotplatform.client.dto.QueryDeviceRealtimeLocationInDTO;
import com.huawei.iotplatform.client.dto.QueryDeviceRealtimeLocationOutDTO;
import com.huawei.iotplatform.client.dto.QueryDeviceShadowOutDTO;
import com.huawei.iotplatform.client.dto.QueryDeviceStatusOutDTO;
import com.huawei.iotplatform.client.dto.RefreshDeviceKeyInDTO;
import com.huawei.iotplatform.client.dto.RefreshDeviceKeyOutDTO;
import com.huawei.iotplatform.client.dto.RegDirectDeviceInDTO2;
import com.huawei.iotplatform.client.dto.RegDirectDeviceOutDTO;
import com.huawei.iotplatform.client.dto.ServiceDesiredDTO;
import com.huawei.iotplatform.client.invokeapi.Authentication;
import com.huawei.iotplatform.client.invokeapi.DeviceManagement;
import com.huawei.iotplatform.utils.AuthUtil;

public class DeviceManagementTest
{
    public static void main(String args[]) throws Exception
    {
    	/**---------------------initialize northApiClient------------------------*/
    	NorthApiClient northApiClient = AuthUtil.initApiClient();
    	DeviceManagement deviceManagement = new DeviceManagement(northApiClient);
        
        /**---------------------get accessToken at first------------------------*/
        Authentication authentication = new Authentication(northApiClient);        
        AuthOutDTO authOutDTO = authentication.getAuthToken();        
        String accessToken = authOutDTO.getAccessToken();
        
        /**---------------------register a new device------------------------*/
        System.out.println("======register a new device======");
        RegDirectDeviceOutDTO rddod = registerDevice(deviceManagement, accessToken, 3000);
        
        if (rddod != null) {
        	String deviceId = rddod.getDeviceId();
        	
        	/**---------------------modify device info------------------------*/
        	// use verifyCode as the device name
        	System.out.println("\n======modify device info======");
            modifyDeviceInfo(deviceManagement, accessToken, deviceId, rddod.getVerifyCode());
            
            /**---------------------query device status------------------------*/
            System.out.println("\n======query device status======");
            QueryDeviceStatusOutDTO qdsOutDTO = deviceManagement.queryDeviceStatus(deviceId, null, accessToken);
            System.out.println(qdsOutDTO.toString());
            
            /**---------------------query device real-time location------------------------*/
            //note: querying device real-time location has several conditions, 
            //thus, this API may return error if the conditions are not matched.
            System.out.println("\n======query device real-time location======");
            queryDeviceLocation(deviceManagement, accessToken, deviceId);
            
            /**---------------------modify device shadow------------------------*/
            System.out.println("\n======modify device shadow======");
            modifyDeviceShadow(deviceManagement, accessToken, deviceId);
            
            /**---------------------query device shadow------------------------*/  
            System.out.println("\n======query device shadow======");
            QueryDeviceShadowOutDTO qdshadowOutDTO = deviceManagement.queryDeviceShadow(deviceId, null, accessToken);
            System.out.println(qdshadowOutDTO.toString());
            
            /**---------------------refresh device key------------------------*/ 
            //note: refreshing device key has several conditions, 
            //thus, this API may return error if the conditions are not matched.
            System.out.println("\n======refresh device key======");
            refreshDeviceKey(deviceManagement, accessToken, deviceId);
            
            /**---------------------delete device------------------------*/ 
            System.out.println("\n======delete device======");
            deviceManagement.deleteDirectDevice(deviceId, true, null, accessToken);
            System.out.println("delete device succeeded");
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
//        rddid.setTimeout(timeout);
                
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
        mdiInDTO.setDeviceType("Bulb");
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
    
    private static QueryDeviceRealtimeLocationOutDTO queryDeviceLocation(DeviceManagement deviceManagement, String accessToken, String deviceId) {
    	QueryDeviceRealtimeLocationInDTO qdrlInDTO = new QueryDeviceRealtimeLocationInDTO();
        qdrlInDTO.setDeviceId(deviceId);
        qdrlInDTO.setHorAcc(1000);
        QueryDeviceRealtimeLocationOutDTO qdrlOutDTO;
		try {
			qdrlOutDTO = deviceManagement.queryDeviceRealtimeLocation(qdrlInDTO, null, accessToken);
			System.out.println(qdrlOutDTO.toString());
			return qdrlOutDTO;
		} catch (NorthApiException e) {
			System.out.println(e.toString());
		}
        
        return null;
    }
    
    private static void modifyDeviceShadow(DeviceManagement deviceManagement, String accessToken, String deviceId) {
    	ModifyDeviceShadowInDTO mdsInDTO = new ModifyDeviceShadowInDTO();
        
        ServiceDesiredDTO sdDTO = new ServiceDesiredDTO();
        sdDTO.setServiceId("Brightness");        
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("brightness", 100);
        sdDTO.setDesired(map);
        
        List<ServiceDesiredDTO> serviceDesireds = new ArrayList<ServiceDesiredDTO>();
        serviceDesireds.add(sdDTO);        
        mdsInDTO.setServiceDesireds(serviceDesireds);        
        
        try {
			deviceManagement.modifyDeviceShadow(mdsInDTO, deviceId, null, accessToken);
			System.out.println("modify device shadow succeeded");
		} catch (NorthApiException e) {
			System.out.println(e.toString());
		}
    }
    
    private static void refreshDeviceKey(DeviceManagement deviceManagement, String accessToken, String deviceId) {
    	RefreshDeviceKeyInDTO rdkInDTO = new RefreshDeviceKeyInDTO();
    	Random random = new Random();	
		String nodeid = "testdemo" + (random.nextInt(9000000) + 1000000); //this is a test imei
		rdkInDTO.setNodeId(nodeid);
		rdkInDTO.setVerifyCode(nodeid);
    	rdkInDTO.setTimeout(3600);
    	
    	try {
    		RefreshDeviceKeyOutDTO rdkOutDTO = deviceManagement.refreshDeviceKey(rdkInDTO, deviceId, null, accessToken);
    		System.out.println(rdkOutDTO.toString());
		} catch (NorthApiException e) {
			System.out.println(e.toString());
		}
    }
}
