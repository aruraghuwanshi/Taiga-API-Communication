package Controller;


import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import ch.qos.logback.core.net.server.Client;

@RestController
public class TaigaController {
	
	
	public static void getProjectData()
	{
		List<String> resultList = new ArrayList<>();
		
		Scanner scanner = new Scanner(System.in);
	    System.out.println("Enter username");
	    String userName = scanner.nextLine();
	    
	    System.out.println("Enter password");
	    String password = scanner.nextLine();
	    
	    resultList.add("User with username " +userName+ " has successfully logged in.");
	    
		
		RestTemplate restTemplate = new RestTemplate();
		String url = "https://api.taiga.io/api/v1/projects/by_slug?slug=abhishekmohabe-ser-515-team-33-premier-soccer-league-cup";

		HttpHeaders headers = new HttpHeaders();
		HttpEntity entity = new HttpEntity(headers);
		
		ResponseEntity<String> response = restTemplate.exchange(
			    url, HttpMethod.GET, entity, String.class, new Object());
		
		JSONObject projectResponse = new JSONObject(response.getBody());
		
		int projectId = (int)projectResponse.get("id");
		
		//Members and Roles
		JSONArray memberList = (JSONArray)projectResponse.get("members");
		
		for(Object obj: memberList)
		{
			JSONObject ob = (JSONObject)obj;
			String temp = "Full Name: " + (String)ob.get("full_name");
			resultList.add(temp);
			
			temp = "Role: " + (String)ob.get("role_name");
			resultList.add(temp);
			
			resultList.add("");
		}
		
		
		//Sprints
		url = "https://api.taiga.io/api/v1/milestones?project=" + projectId;
		
		response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class, new Object());
		
		JSONArray sprintResponse = new JSONArray(response.getBody());
		Map<String, JSONArray> sprintMap = new HashMap<>();
		
		for(Object obj: sprintResponse)
		{
			JSONObject ob = (JSONObject)obj;
			String temp = "Sprint Name: " + (String)ob.get("name");
			resultList.add(temp);
			
			temp = "Sprint start date: " + (String)ob.get("estimated_start");
			resultList.add(temp);
			
			temp = "Sprint end date: " + (String)ob.get("estimated_finish");
			resultList.add(temp);
			
			JSONArray userStoryList = (JSONArray)ob.get("user_stories");
			
			sprintMap.put((String)ob.get("name"),userStoryList);
			
			double totalPoints = 0;
			double finishedPoints = 0;
			
			for(Object userStoryObject: userStoryList)
			{
				JSONObject userStory = (JSONObject)userStoryObject;
				
				if(userStory.get("total_points") != null || userStory.get("total_points") != org.json.JSONObject.NULL)
				{
					try
					{
						totalPoints += (double)userStory.get("total_points");
					}
					catch(Exception e)
					{}
				}
				
				
				if(userStory.get("is_closed") != null && (boolean)userStory.get("is_closed"))
				{
					finishedPoints += (double)userStory.get("total_points");
				}
			}
			
			temp = "Total Points: " + totalPoints;
			resultList.add(temp);
			temp = "Finished Points: " + finishedPoints;
			resultList.add(temp);
			
			resultList.add("");
			
		}
			
		for(String result: resultList)
		{
			System.out.println(result);
		}
		
		resultList = new ArrayList<>();
		
		//Sprint Selection and User Stories' details	
		System.out.println("Please select a Sprint name from the above mentioned options");
	    String sprintSelection = scanner.nextLine();
		
	    while(!sprintMap.containsKey(sprintSelection))
	    {
	    	System.out.println("Incorrect Selection. Please try again");
	    	
	    	System.out.println("Please select a Sprint name from the above mentioned options");
		    sprintSelection = scanner.nextLine();
	    }
	    
	    resultList.add("");
	    
	    JSONArray userStories = sprintMap.get(sprintSelection);
	    
	    List<Integer> userStoryIds = new ArrayList<>();
	    
	    for(Object userStoryObject: userStories)
		{
			JSONObject userStory = (JSONObject)userStoryObject;
			
			resultList.add("Name of User Story: " + (String)userStory.get("subject"));
			
			if((boolean)userStory.get("is_closed"))
				resultList.add("Story status: Finished");
			else
				resultList.add("Story status: Not Finished");
			
			resultList.add("Created date: " + (String)userStory.get("created_date"));
			
			resultList.add("Story moved to sprint date: " + (String)userStory.get("modified_date"));
			
			resultList.add("");
			
			userStoryIds.add((int)userStory.get("id"));
		}
	    
	    resultList.add("");
	    
	    Map<String,Integer> taskCountMap = new HashMap<>();
	    
	    //Tasks
	    for(Integer userStoryId: userStoryIds)
	    {
	    	url = "https://api.taiga.io/api/v1/tasks?user_story=" + userStoryId;
			
			response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class, new Object());
			
			JSONArray taskResponse = new JSONArray(response.getBody());
			
			for(Object taskObject: taskResponse)
			{
				JSONObject task = (JSONObject)taskObject;
				String assignedTo;
				resultList.add("Task Name: " + (String)task.get("subject"));
				if(task.get("assigned_to") == null || (int)task.get("assigned_to") == 0)
				{
					assignedTo = "None";
				}
				else
				{
					assignedTo = (String)((JSONObject)task.get("assigned_to_extra_info")).get("full_name_display");
					
					taskCountMap.put(assignedTo, taskCountMap.getOrDefault(assignedTo,1) + 1);
				}
				resultList.add("Assigned To: " + assignedTo);
			}
			
			resultList.add("");
	    }
	    
	    resultList.add("");
	    
	    //Users and their assigned Task count
	    for(Map.Entry<String,Integer> entry: taskCountMap.entrySet())
	    {
	    	resultList.add("User: "+entry.getKey()+"; Task Count: "+entry.getValue());
	    }
	    
	    for(String result: resultList)
		{
			System.out.println(result);
		}
	
	}


	

	
}
