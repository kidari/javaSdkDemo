package com.huawei.iotplatform.BatchProcess.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.huawei.iotplatform.client.NorthApiClient;
import com.huawei.iotplatform.client.NorthApiException;
import com.huawei.iotplatform.client.dto.AuthOutDTO;
import com.huawei.iotplatform.client.dto.BatchTaskCreateInDTO2;
import com.huawei.iotplatform.client.dto.BatchTaskCreateOutDTO;
import com.huawei.iotplatform.client.dto.CommandDTOV4;
import com.huawei.iotplatform.client.dto.DeviceCmd;
import com.huawei.iotplatform.client.dto.QueryOneTaskOutDTO;
import com.huawei.iotplatform.client.dto.QueryTaskDetailsInDTO;
import com.huawei.iotplatform.client.dto.QueryTaskDetailsOutDTO;
import com.huawei.iotplatform.client.invokeapi.Authentication;
import com.huawei.iotplatform.client.invokeapi.BatchProcess;
import com.huawei.iotplatform.utils.AuthUtil;
import com.huawei.iotplatform.utils.PropertyUtil;

public class BatchProcessTest
{
    public static void main(String args[]) throws Exception
    {
    	/**---------------------initialize northApiClient------------------------*/
    	NorthApiClient northApiClient = AuthUtil.initApiClient();
    	BatchProcess batchProcess = new BatchProcess(northApiClient);
        
        /**---------------------get accessToken at first------------------------*/
        Authentication authentication = new Authentication(northApiClient);        
        AuthOutDTO authOutDTO = authentication.getAuthToken();        
        String accessToken = authOutDTO.getAccessToken();
        
        /**---------------------create a task------------------------*/
        System.out.println("======create a task begin======");
        List<String> deviceList = new ArrayList<String>();
        
        deviceList.add("a5c7f6c9-5e81-4693-9c56-070123fc186a");
        deviceList.add("ae81fe57-9ac7-4d9c-9a42-206bc9545a19");
        
        BatchTaskCreateOutDTO task = createTask(batchProcess, deviceList, accessToken);
        
        
        if (task != null) {
        	System.out.println(task.toString());
        	
        	/**---------------------query a specified task------------------------*/
        	System.out.println("\n======query a specified task======");
            String taskId = task.getTaskID();
            QueryOneTaskOutDTO qotOutDTO = batchProcess.queryOneTask(taskId, null, null, accessToken);
            System.out.println(qotOutDTO.toString());
            
            /**---------------------query a specified task detail------------------------*/
            System.out.println("\n======query a specified task detail======");
            QueryTaskDetailsInDTO qtdInDTO = new QueryTaskDetailsInDTO();
            qtdInDTO.setTaskId(taskId);
            QueryTaskDetailsOutDTO qtdOutDTO = batchProcess.queryTaskDetails(qtdInDTO, accessToken);
            System.out.println(qtdOutDTO.toString());
		}
  
    }
    
    private static BatchTaskCreateOutDTO createTask(BatchProcess batchProcess, List<String> deviceList, String accessToken) throws NorthApiException {
    	//fill input parameters BatchTaskCreateInDTO
    	BatchTaskCreateInDTO2 btcInDTO = new BatchTaskCreateInDTO2();
    	
        Random random = new Random();	
		String taskName = "testTask" + (random.nextInt(9000000) + 1000000); //this is a test task name
        btcInDTO.setTaskName(taskName);
        
        btcInDTO.setTimeout(300);
        btcInDTO.setAppId(PropertyUtil.getProperty("appId"));
        btcInDTO.setTaskType("DeviceCmd");
        
        //set DeviceCmd
        DeviceCmd deviceCmd = new DeviceCmd();
        deviceCmd.setType("DeviceList");
        
        deviceCmd.setDeviceList(deviceList);
        
        //fill command according to profile
        CommandDTOV4 command = new CommandDTOV4();
        command.setMethod("PUT"); //PUT is the command name
        command.setServiceId("Brightness");
        
        Map<String, Object> cmdPara = new HashMap<String, Object>();
        cmdPara.put("brightness", 80); //brightness is a command parameter
     
		command.setParas(cmdPara);        
		deviceCmd.setCommand(command);
		
		try {
			btcInDTO.setParam(deviceCmd);
			return batchProcess.createBatchTask(btcInDTO, accessToken);
			
		} catch (Exception e) {
			System.out.println(e.toString());
		}

        return null;
    }
}
