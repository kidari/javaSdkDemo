package com.huawei.iotplatform.DeviceGroupManagement.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.huawei.iotplatform.client.NorthApiClient;
import com.huawei.iotplatform.client.dto.AuthOutDTO;
import com.huawei.iotplatform.client.dto.CreateDeviceGroupInDTO;
import com.huawei.iotplatform.client.dto.CreateDeviceGroupOutDTO;
import com.huawei.iotplatform.client.dto.DeviceGroupWithDeviceListDTO;
import com.huawei.iotplatform.client.dto.ModifyDeviceGroupInDTO;
import com.huawei.iotplatform.client.dto.ModifyDeviceGroupOutDTO;
import com.huawei.iotplatform.client.dto.QueryDeviceGroupMembersInDTO;
import com.huawei.iotplatform.client.dto.QueryDeviceGroupMembersOutDTO;
import com.huawei.iotplatform.client.dto.QueryDeviceGroupsInDTO;
import com.huawei.iotplatform.client.dto.QueryDeviceGroupsOutDTO;
import com.huawei.iotplatform.client.dto.QuerySingleDeviceGroupOutDTO;
import com.huawei.iotplatform.client.invokeapi.Authentication;
import com.huawei.iotplatform.client.invokeapi.DeviceGroupManagement;
import com.huawei.iotplatform.utils.AuthUtil;

public class DeviceGroupManagementTest {
	public static void main(String args[]) throws Exception {
		/**---------------------initialize northApiClient------------------------*/
    	NorthApiClient northApiClient = AuthUtil.initApiClient();
    	DeviceGroupManagement groupManagement = new DeviceGroupManagement(northApiClient);
        
        /**---------------------get accessToken at first------------------------*/
        Authentication authentication = new Authentication(northApiClient);        
        AuthOutDTO authOutDTO = authentication.getAuthToken();        
        String accessToken = authOutDTO.getAccessToken();
        
        
        String deviceId1 = "cb15cbb6-04ce-4e2b-bf35-d6a264c74646";
        String deviceId2 = "ab5dfcec-b156-40c7-86a9-0dc3cbb7bad6";
        String deviceId3 = "9d1d6a70-33c1-47e5-89e7-50a4369fbe95";
        
        /**---------------------create a device group------------------------*/
        System.out.println("======create a device group======");
        CreateDeviceGroupInDTO cdgInDTO = new CreateDeviceGroupInDTO();
        Random random = new Random();
        String groupName = "group" + (random.nextInt(9000000) + 1000000);//this is a test group name
        cdgInDTO.setName(groupName);
        //add two devices into the list
        List<String> deviceIdList = new ArrayList<String>();
        deviceIdList.add(deviceId1);
        deviceIdList.add(deviceId2);
        cdgInDTO.setDeviceIds(deviceIdList);
        
        CreateDeviceGroupOutDTO cdgOutDTO = groupManagement.createDeviceGroup(cdgInDTO, accessToken);
        if (cdgOutDTO != null) {
        	System.out.println(cdgOutDTO.toString());
        	String groupId = cdgOutDTO.getId();
        	
        	/**---------------------modify a device group------------------------*/
        	System.out.println("\n======modify a device group======");
        	ModifyDeviceGroupInDTO mdgInDTO = new ModifyDeviceGroupInDTO();        	
        	Random r = new Random();
            String name = "group" + (r.nextInt(9000000) + 1000000);//this is a test group name
        	mdgInDTO.setName(name);
        	ModifyDeviceGroupOutDTO mdgOutDTO = groupManagement.modifyDeviceGroup(mdgInDTO, groupId, null, accessToken);
        	System.out.println(mdgOutDTO.toString());
        	
        	/**---------------------query a specified device group------------------------*/
        	System.out.println("\n======query a specified device group======");
        	QuerySingleDeviceGroupOutDTO qsdgOutDTO = groupManagement.querySingleDeviceGroup(groupId, null, accessToken);
        	System.out.println(qsdgOutDTO.toString());
        	
        	/**---------------------query device group members------------------------*/
        	System.out.println("\n======query device group members======");
        	QueryDeviceGroupMembersInDTO qdgmInDTO = new QueryDeviceGroupMembersInDTO();
        	qdgmInDTO.setDevGroupId(groupId);
        	QueryDeviceGroupMembersOutDTO qdgmOutDTO = groupManagement.queryDeviceGroupMembers(qdgmInDTO, accessToken);
        	System.out.println(qdgmOutDTO.toString());
        	
        	/**---------------------add device group members------------------------*/
        	System.out.println("\n======add device group members======");
        	DeviceGroupWithDeviceListDTO dgwdlDTO = new DeviceGroupWithDeviceListDTO();
        	dgwdlDTO.setDevGroupId(groupId);
        	//add new devices to the list
        	List<String> list = new ArrayList<String>();
        	list.add(deviceId3);
        	dgwdlDTO.setDeviceIds(list);
        	DeviceGroupWithDeviceListDTO dgwdlDTO_rsp = groupManagement.addDevicesToGroup(dgwdlDTO, null, accessToken);
        	System.out.println(dgwdlDTO_rsp.toString());
        	
        	/**---------------------delete device group members------------------------*/
        	//delete the device list from group member again
        	System.out.println("\n======delete device group members======");
        	groupManagement.deleteDevicesFromGroup(dgwdlDTO, null, accessToken);
        	System.out.println("delete device group members succeeded");
		}
        
        /**---------------------query device groups------------------------*/
        System.out.println("\n======query device groups======");
        QueryDeviceGroupsInDTO qdgInDTO = new QueryDeviceGroupsInDTO();
        qdgInDTO.setPageNo(0);
        qdgInDTO.setPageSize(10);
        QueryDeviceGroupsOutDTO qdgOutDTO = groupManagement.queryDeviceGroups(qdgInDTO, accessToken);
        System.out.println(qdgOutDTO.toString());
        
        //delete all the device groups of page 0
        List<QuerySingleDeviceGroupOutDTO> groupList = qdgOutDTO.getList();
        for (QuerySingleDeviceGroupOutDTO querySingleDeviceGroupOutDTO : groupList) {
        	/**---------------------delete a device group------------------------*/
        	System.out.println("\n======delete a device group======");
        	groupManagement.deleteDeviceGroup(querySingleDeviceGroupOutDTO.getId(), null, accessToken);
        	System.out.println("delete a device group succeeded");
		}
	}
}
