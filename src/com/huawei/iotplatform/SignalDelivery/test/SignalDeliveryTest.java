package com.huawei.iotplatform.SignalDelivery.test;

import java.util.Map;

import org.apache.commons.collections.map.HashedMap;

import com.huawei.iotplatform.client.NorthApiClient;
import com.huawei.iotplatform.client.NorthApiException;
import com.huawei.iotplatform.client.dto.AuthOutDTO;
import com.huawei.iotplatform.client.dto.CommandDTOV4;
import com.huawei.iotplatform.client.dto.CreateDeviceCmdCancelTaskInDTO;
import com.huawei.iotplatform.client.dto.CreateDeviceCmdCancelTaskOutDTO;
import com.huawei.iotplatform.client.dto.PostDeviceCommandInDTO2;
import com.huawei.iotplatform.client.dto.PostDeviceCommandOutDTO2;
import com.huawei.iotplatform.client.dto.QueryDeviceCmdCancelTaskInDTO2;
import com.huawei.iotplatform.client.dto.QueryDeviceCmdCancelTaskOutDTO2;
import com.huawei.iotplatform.client.dto.QueryDeviceCommandInDTO2;
import com.huawei.iotplatform.client.dto.QueryDeviceCommandOutDTO2;
import com.huawei.iotplatform.client.dto.UpdateDeviceCommandInDTO;
import com.huawei.iotplatform.client.dto.UpdateDeviceCommandOutDTO;
import com.huawei.iotplatform.client.invokeapi.Authentication;
import com.huawei.iotplatform.client.invokeapi.SignalDelivery;
import com.huawei.iotplatform.utils.AuthUtil;

public class SignalDeliveryTest {
	
    public static void main(String args[]) throws Exception {
    	/**---------------------initialize northApiClient------------------------*/
        NorthApiClient northApiClient = AuthUtil.initApiClient();
        SignalDelivery signalDelivery = new SignalDelivery(northApiClient);
        
        /**---------------------get accessToken at first------------------------*/
        Authentication authentication = new Authentication(northApiClient);        
        AuthOutDTO authOutDTO = authentication.getAuthToken();        
        String accessToken = authOutDTO.getAccessToken();
        
        /**---------------------post an NB-IoT device command------------------------*/
        //this is a test NB-IoT device
        String deviceId = "42517443-8c1b-4a58-b65e-194c00f77d4e";
        System.out.println("======post an NB-IoT device command======");
        PostDeviceCommandOutDTO2 pdcOutDTO = postCommand(signalDelivery, deviceId, accessToken);
        if (pdcOutDTO != null) {
        	System.out.println(pdcOutDTO.toString());
        	String commandId = pdcOutDTO.getCommandId();
        	
        	/**---------------------update device command------------------------*/
        	System.out.println("\n======update device command======");
            UpdateDeviceCommandInDTO udcInDTO = new UpdateDeviceCommandInDTO();
            udcInDTO.setStatus("CANCELED");
            UpdateDeviceCommandOutDTO udcOutDTO = signalDelivery.updateDeviceCommand(udcInDTO, commandId, null, accessToken);
            System.out.println(udcOutDTO.toString());
		}
        
        /**---------------------query device commands------------------------*/
        System.out.println("\n======query device commands======");
        QueryDeviceCommandInDTO2 qdcInDTO = new QueryDeviceCommandInDTO2();
        qdcInDTO.setPageNo(0);
        qdcInDTO.setPageSize(10);               
        QueryDeviceCommandOutDTO2 qdcOutDTO = signalDelivery.queryDeviceCommand(qdcInDTO, accessToken);
        System.out.println(qdcOutDTO.toString());
        
        /**---------------------cancel all device commands of the device------------------------*/
        System.out.println("\n======cancel all device commands of the device======");
        CreateDeviceCmdCancelTaskInDTO cdcctInDTO = new CreateDeviceCmdCancelTaskInDTO();
        cdcctInDTO.setDeviceId(deviceId);        
        CreateDeviceCmdCancelTaskOutDTO cdcctOutDTO = signalDelivery.createDeviceCmdCancelTask(cdcctInDTO, null, accessToken);
        System.out.println(cdcctOutDTO.toString());
        
        /**---------------------query device command cancel tasks of the device------------------------*/
        System.out.println("\n======query device command cancel tasks of the device======");
    	QueryDeviceCmdCancelTaskInDTO2 qdcctInDTO = new QueryDeviceCmdCancelTaskInDTO2();
        qdcctInDTO.setDeviceId(deviceId);
        qdcctInDTO.setPageNo(0);
        qdcctInDTO.setPageSize(10);
        QueryDeviceCmdCancelTaskOutDTO2 qdcctOutDTO = signalDelivery.queryDeviceCmdCancelTask(qdcctInDTO, accessToken);
        System.out.println(qdcctOutDTO.toString());
        
    }
    
    @SuppressWarnings("unchecked")
	private static PostDeviceCommandOutDTO2 postCommand(SignalDelivery signalDelivery, String deviceId, String accessToken) {
    	PostDeviceCommandInDTO2 pdcInDTO = new PostDeviceCommandInDTO2();
        pdcInDTO.setDeviceId(deviceId);
        
        CommandDTOV4 cmd = new CommandDTOV4();
        cmd.setServiceId("RawData");
        cmd.setMethod("RawData"); //"PUT" is the command name defined in the profile
        Map<String, Object> cmdParam = new HashedMap();
        cmdParam.put("rawData", "AQ==");//"brightness" is the command parameter name defined in the profile
        
        cmd.setParas(cmdParam);
        pdcInDTO.setCommand(cmd);
        
        try {
			return signalDelivery.postDeviceCommand(pdcInDTO, null, accessToken);
		} catch (NorthApiException e) {
			System.out.println(e.toString());
		}
        return null;
    }
}
