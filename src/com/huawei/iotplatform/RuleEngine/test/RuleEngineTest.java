package com.huawei.iotplatform.RuleEngine.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.huawei.iotplatform.client.NorthApiClient;
import com.huawei.iotplatform.client.NorthApiException;
import com.huawei.iotplatform.client.dto.ActionDeviceCMD;
import com.huawei.iotplatform.client.dto.AuthOutDTO;
import com.huawei.iotplatform.client.dto.CMD;
import com.huawei.iotplatform.client.dto.CondiotionDeviceInfo;
import com.huawei.iotplatform.client.dto.ConditionDeviceData;
import com.huawei.iotplatform.client.dto.QueryRulesInDTO2;
import com.huawei.iotplatform.client.dto.RuleCreateOrUpdateOutDTO;
import com.huawei.iotplatform.client.dto.RuleDTO2;
import com.huawei.iotplatform.client.dto.Strategy;
import com.huawei.iotplatform.client.dto.UpdateBatchRuleStatusInDTO;
import com.huawei.iotplatform.client.dto.UpdateBatchRuleStatusOutDTO;
import com.huawei.iotplatform.client.dto.UpdateRuleStatusInDTO;
import com.huawei.iotplatform.client.invokeapi.Authentication;
import com.huawei.iotplatform.client.invokeapi.RuleEngine;
import com.huawei.iotplatform.utils.AuthUtil;
import com.huawei.iotplatform.utils.PropertyUtil;

public class RuleEngineTest
{
    public static void main(String args[]) throws Exception
    {
    	/**---------------------initialize northApiClient------------------------*/
        NorthApiClient northApiClient = AuthUtil.initApiClient();
        RuleEngine ruleEngine = new RuleEngine(northApiClient);
        
        /**---------------------get accessToken at first------------------------*/
        Authentication authentication = new Authentication(northApiClient);        
        AuthOutDTO authOutDTO = authentication.getAuthToken();        
        String accessToken = authOutDTO.getAccessToken();
        
        String deviceIdA = "2f21bc54-b10c-43d0-91f6-ba4c418a830d";
        String deviceIdB = "09691354-872d-49a1-9c1c-a18512f10ae2";
        
        /**---------------------create a rule------------------------*/
        System.out.println("======create a rule======");
        RuleCreateOrUpdateOutDTO rcorOutDTO = createRule(ruleEngine, deviceIdA, deviceIdB, accessToken);
        
        if (rcorOutDTO != null) { 
        	String ruleId = rcorOutDTO.getRuleId();
        	System.out.println("create rule succeeds, ruleId=" + ruleId);
        	
        	/**---------------------update the rule------------------------*/
        	System.out.println("\n======update the rule======");
        	RuleCreateOrUpdateOutDTO rcorOutDTO2 = updateRule(ruleEngine, ruleId, deviceIdA, deviceIdB, accessToken);
        	if (rcorOutDTO2 != null) {
				//update rule succeeds
        		ruleId = rcorOutDTO2.getRuleId();
        		System.out.println("update rule succeeds, ruleId=" + ruleId);
			}
        	
        	/**---------------------update rule status------------------------*/
        	System.out.println("\n======update rule status======");
        	UpdateRuleStatusInDTO ursInDTO = new UpdateRuleStatusInDTO();
        	ursInDTO.setRuleId(ruleId);
        	//change the rule status to inactive
        	ursInDTO.setStatus("inactive");
        	
        	ruleEngine.updateRuleStatus(ursInDTO, null, accessToken);
        	System.out.println("update rule status succeeds");
		}
        
        /**---------------------query rules------------------------*/
        System.out.println("\n======query rules======");
    	QueryRulesInDTO2 queryRulesInDTO = new QueryRulesInDTO2();
    	queryRulesInDTO.setAuthor(PropertyUtil.getProperty("appId"));
    	List<RuleDTO2> rules = ruleEngine.queryRules(queryRulesInDTO, accessToken);
    	System.out.println(rules.toString());
    	
    	if (rules.size() > 0) {
    		/**---------------------batch update rule status------------------------*/
    		System.out.println("\n======batch update rule status======");
        	UpdateBatchRuleStatusInDTO ubrsInDTO = new UpdateBatchRuleStatusInDTO();
        	List<UpdateRuleStatusInDTO> list = new ArrayList<UpdateRuleStatusInDTO>();
        	
        	for (RuleDTO2 ruleDTO : rules) {
        		UpdateRuleStatusInDTO ursInDTO = new UpdateRuleStatusInDTO();
            	ursInDTO.setRuleId(ruleDTO.getRuleId());
            	//change all rules' status to inactive
            	ursInDTO.setStatus("inactive");
            	list.add(ursInDTO);        	
    		}
        	ubrsInDTO.setRequests(list);
        	UpdateBatchRuleStatusOutDTO ubrsOutDTO = ruleEngine.updateBatchRuleStatus(ubrsInDTO, null, accessToken);
        	System.out.println(ubrsOutDTO.toString());
        	
        	//delete all rules
        	for (RuleDTO2 ruleDTO : rules) {
        		/**---------------------delete rule------------------------*/
        		System.out.println("\n======delete rule======");
        		ruleEngine.deleteRule(ruleDTO.getRuleId(), null, accessToken);
        	}
		}
    	
    }
    
    /** 
     * When deviceA's report data match the conditions, IoT platform will trigger the actions and send the command to deviceB
     * note: the conditions and actions can belong to different services and properties, but here, the example just shows a simple scenario
     **/
    private static RuleCreateOrUpdateOutDTO createRule(RuleEngine ruleEngine, String deviceIdA_condition, String deviceIdB_action,
    		String accessToken) {
    	//set conditions
    	List<Object> conditions = new ArrayList<Object>();
    	//the condition is "when the deviceA's brightness > 80" 
    	ConditionDeviceData conditionDeviceData = setCondition(deviceIdA_condition, ">", "80");
    	//add conditionDeviceData to the condition list
    	conditions.add(conditionDeviceData);
    	
    	//set actions
    	List<Object> actions = new ArrayList<Object>();
    	//the action is "put deviceB's brightness to 50"
    	ActionDeviceCMD actionDeviceCMD = setAction(deviceIdB_action, 50);
    	//add actionDeviceCMD to the action list
    	actions.add(actionDeviceCMD);
    	
    	//create the rule
    	RuleDTO2 ruleDTO = new RuleDTO2();
    	Random random = new Random();
    	String ruleName = "rule" + (random.nextInt(900000) + 100000); //this is a test rule name
    	ruleDTO.setName(ruleName);
    	ruleDTO.setAuthor(PropertyUtil.getProperty("appId"));
    	ruleDTO.setConditions(conditions);
    	ruleDTO.setActions(actions);
    	
    	RuleCreateOrUpdateOutDTO rcouOutDTO = new RuleCreateOrUpdateOutDTO();
    	try {
			rcouOutDTO = ruleEngine.createRule(ruleDTO, null, accessToken);
			return rcouOutDTO;
		} catch (NorthApiException e) {
			System.out.println(e.toString());
		}
    	return null;
    }
    
    private static ConditionDeviceData setCondition(String deviceId, String operator, String value) {
    	//set condition
    	ConditionDeviceData conditionDeviceData = new ConditionDeviceData();
    	conditionDeviceData.setType("DEVICE_DATA");
    	
    	//the condition is, for example, "when the deviceA's brightness > (operator) 80 (value)" 
    	CondiotionDeviceInfo condiotionDeviceInfo = new CondiotionDeviceInfo();
    	condiotionDeviceInfo.setDeviceId(deviceId);
    	condiotionDeviceInfo.setPath("Brightness/brightness"); //serviceId/propertyName that defined in the profile
    	
    	conditionDeviceData.setDeviceInfo(condiotionDeviceInfo);
    	conditionDeviceData.setOperator(operator);
    	conditionDeviceData.setValue(value);
    	
    	Strategy strategy = new Strategy();
    	strategy.setTrigger("pulse");
    	
    	conditionDeviceData.setStrategy(strategy);
    	return conditionDeviceData;
    }
    
    private static ActionDeviceCMD setAction(String deviceId, int value) {
    	ActionDeviceCMD actionDeviceCMD = new ActionDeviceCMD();
    	actionDeviceCMD.setType("DEVICE_CMD");
    	
    	//the action is "put deviceB's brightness to the specified value"
    	Map<String, Object> cmdBody = new HashMap<String, Object>();
    	cmdBody.put("brightness", value); //command parameter that defined in the profile
    	
    	CMD cmd = new CMD();
    	cmd.setServiceId("Brightness"); //seviceId that defined in the profile
    	cmd.setMessageType("PUT"); //"PUT" is the command that defined in the profile
    	cmd.setMessageBody(cmdBody);
    	
    	actionDeviceCMD.setCmd(cmd);
    	actionDeviceCMD.setDeviceId(deviceId);
    	return actionDeviceCMD;
    }
    
    private static RuleCreateOrUpdateOutDTO updateRule(RuleEngine ruleEngine, String ruleId, String deviceIdA_condition, String deviceIdB_action,
    		String accessToken) {
    	//set conditions
    	List<Object> conditions = new ArrayList<Object>();
    	//the condition is "when the deviceA's brightness > 90" 
    	ConditionDeviceData conditionDeviceData = setCondition(deviceIdA_condition, ">", "90");    	
    	//add conditionDeviceData to the condition list
    	conditions.add(conditionDeviceData);
    	
    	//set actions
    	List<Object> actions = new ArrayList<Object>();
    	//the action is "put deviceB's brightness to 20"
    	ActionDeviceCMD actionDeviceCMD = setAction(deviceIdB_action, 20);
    	//add actionDeviceCMD to the action list
    	actions.add(actionDeviceCMD);
    	
    	//create the rule
    	RuleDTO2 ruleDTO = new RuleDTO2();
    	Random random = new Random();
    	String ruleName = "rule" + (random.nextInt(900000) + 100000); //this is a test rule name
    	ruleDTO.setName(ruleName);
    	
    	//ruleId cannot be null when update the rule
    	ruleDTO.setRuleId(ruleId); 
    	ruleDTO.setAuthor(PropertyUtil.getProperty("appId"));
    	ruleDTO.setConditions(conditions);
    	ruleDTO.setActions(actions);
    	
    	RuleCreateOrUpdateOutDTO rcouOutDTO = new RuleCreateOrUpdateOutDTO();
    	try {
			rcouOutDTO = ruleEngine.updateRule(ruleDTO, null, accessToken);
			return rcouOutDTO;
		} catch (NorthApiException e) {
			System.out.println(e.toString());
		}
    	return null;
    }
    
}
