package com.huawei.iotplatform.Subscription.test;

import com.huawei.iotplatform.client.NorthApiClient;
import com.huawei.iotplatform.client.NorthApiException;
import com.huawei.iotplatform.client.dto.AuthOutDTO;
import com.huawei.iotplatform.client.dto.DeleteBatchSubInDTO;
import com.huawei.iotplatform.client.dto.QueryBatchSubInDTO;
import com.huawei.iotplatform.client.dto.QueryBatchSubOutDTO;
import com.huawei.iotplatform.client.dto.SubDeviceDataInDTO;
import com.huawei.iotplatform.client.dto.SubDeviceManagementDataInDTO;
import com.huawei.iotplatform.client.dto.SubscriptionDTO;
import com.huawei.iotplatform.client.invokeapi.Authentication;
import com.huawei.iotplatform.client.invokeapi.SubscriptionManagement;
import com.huawei.iotplatform.utils.AuthUtil;
import com.huawei.iotplatform.utils.PropertyUtil;

public class SubscriptionTest
{
    public static void main(String args[]) throws Exception
    {
    	/**---------------------initialize northApiClient------------------------*/
    	NorthApiClient northApiClient = AuthUtil.initApiClient();
    	SubscriptionManagement subscriptionManagement = new SubscriptionManagement(northApiClient);
        
        /**---------------------get accessToken at first------------------------*/
        Authentication authentication = new Authentication(northApiClient);        
        AuthOutDTO authOutDTO = authentication.getAuthToken();        
        String accessToken = authOutDTO.getAccessToken();
        
        /**---------------------sub deviceAdded notification------------------------*/
        //note: 10.X.X.X is a LAN IP, not a public IP, so subscription callbackUrl's IP cannot be 10.X.X.X
        System.out.println("======subscribe to device business data notification======");
        String callbackUrl = "https://10.63.163.181:8099/v1.0.0/messageReceiver";//this is a test callbackUrl        
        SubscriptionDTO subDTO = subDeviceData(subscriptionManagement, "deviceAdded", callbackUrl, accessToken);
        subDeviceData(subscriptionManagement, "deviceDataChanged", callbackUrl, accessToken);
        
        /**---------------------sub device upgrade result notification------------------------*/
        System.out.println("\n======subscribe to device management data notification======");
        subDeviceManagementData(subscriptionManagement, "swUpgradeResultNotify", callbackUrl, accessToken);
        
        if (subDTO != null) {
        	/**---------------------query single subscription------------------------*/
        	System.out.println("\n======query single subscription======");
        	SubscriptionDTO subDTO2 = subscriptionManagement.querySingleSubscription(subDTO.getSubscriptionId(), null, accessToken);
        	System.out.println(subDTO2.toString());
        	
        	/**---------------------delete single subscription------------------------*/
        	System.out.println("\n======delete single subscription======");
        	subscriptionManagement.deleteSingleSubscription(subDTO.getSubscriptionId(), null, accessToken);
        	System.out.println("delete single subscription succeeds");
		}
        
        /**---------------------query batch subscriptions------------------------*/
        System.out.println("\n======query batch subscriptions======");
        QueryBatchSubInDTO qbsInDTO = new QueryBatchSubInDTO();
        qbsInDTO.setAppId(PropertyUtil.getProperty("appId"));
        QueryBatchSubOutDTO qbsOutDTO = subscriptionManagement.queryBatchSubscriptions(qbsInDTO, accessToken);
        System.out.println(qbsOutDTO.toString());
        
        /**---------------------delete batch subscriptions------------------------*/
        System.out.println("\n======delete batch subscriptions======");
        DeleteBatchSubInDTO dbsInDTO = new DeleteBatchSubInDTO();
        dbsInDTO.setAppId(PropertyUtil.getProperty("appId"));
        try {
        	subscriptionManagement.deleteBatchSubscriptions(dbsInDTO, accessToken);
        	System.out.println("delete batch subscriptions succeeds");
		} catch (NorthApiException e) {
			if ("200001".equals(e.getError_code())) {
				System.out.println("there's no subscription any more");
			}
		}
        
    }
    
    private static SubscriptionDTO subDeviceData(SubscriptionManagement subscriptionManagement, 
    		String notifyType, String callbackUrl, String accessToken) {
    	SubDeviceDataInDTO sddInDTO = new SubDeviceDataInDTO();
    	sddInDTO.setNotifyType(notifyType);
    	sddInDTO.setCallbackUrl(callbackUrl);
    	try {
    		SubscriptionDTO subDTO = subscriptionManagement.subDeviceData(sddInDTO, null, accessToken);
    		System.out.println(subDTO.toString());
			return subDTO;
		} catch (NorthApiException e) {
			System.out.println(e.toString());
		}
    	return null;
    }
    
    
    private static void subDeviceManagementData(SubscriptionManagement subscriptionManagement, 
    		String notifyType, String callbackUrl, String accessToken) {
    	SubDeviceManagementDataInDTO sddInDTO = new SubDeviceManagementDataInDTO();
    	sddInDTO.setNotifyType(notifyType);
    	sddInDTO.setCallbackurl(callbackUrl);
    	try {
			subscriptionManagement.subDeviceData(sddInDTO, accessToken);
			System.out.println("subscribe to device management data succeeds");
		} catch (NorthApiException e) {
			System.out.println(e.toString());
		}
    	return;
    }
}
