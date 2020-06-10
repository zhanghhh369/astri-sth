package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import connector.StreamSetsConnector;
//import model.BaseJPARepository;
//import model.SysEvent;
import play.libs.Json;

public class EventLogger {
	private @Inject StreamSetsConnector streamSetsConnector;
	private static Map<String,String> preLastRecordTime = new HashMap<String,String>();
	private static Map<String,String> preInputCount = new HashMap<String,String>();
	private static Map<String,String> firstRecordTime = new HashMap<String,String>();
	private static Map<String,Integer> zeroCount = new HashMap<String,Integer>();
	public static Map<String,String> pipelines = new HashMap<String,String>();
	public List<Entry<String, String>> pipelineList = new ArrayList<Map.Entry<String, String>>();
	String UNKNOWN_USER = "UNKNOWN_USER";
	public List<String> pipelineIds = new ArrayList<String>();
	@Inject
	public EventLogger() {
	}
	
	/**Function to log events (Username is extracted from request header and recorded)
	 * @param eventType
	 * @param eventMsg
	 * @param request
	 * @return 
	 * @return 
	 */
	public void setPipelines() {
		streamSetsConnector.getAllPipeLines().thenAccept(resultNode -> {
			if (resultNode.path("success").asBoolean()) {
				ArrayNode pipelineEntries = (ArrayNode) resultNode.path("results");
				for (JsonNode pipeline : pipelineEntries) {
					String pipelineId = pipeline.path("pipelineId").asText();
					String pipelineTitle = pipeline.path("title").asText();
					pipelines.put(pipelineTitle, pipelineId);
				}
				pipelineList = new ArrayList<Map.Entry<String, String>>(pipelines.entrySet());
				Collections.sort(pipelineList, new Comparator<Map.Entry<String, String>>() {  
					@Override
				    public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {      
				        return (o1.getKey()).toString().compareTo(o2.getKey());
				    }
				});
	//			System.out.println(pipelineList + "#########");
			}
//			try {
//				Thread.sleep(1000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			System.out.println(pipelineList + "#########");
		});
	}
	
	public void pullEventLogFromStreamSets() {
		if (!pipelineIds.isEmpty()) {
			for (String pipelineId : pipelineIds) {
				CalculateEfficiencyAndLog(pipelineId);
			}
		}
	}
	
	public void CalculateEfficiencyAndLog(String pipelineId) {
		streamSetsConnector.getPipeLineMetrics(pipelineId).thenApply(resultNode -> {
			JsonNode pipelineMetricseResults = resultNode.path("success").asBoolean() ? resultNode.path("results") : Json.newObject();
			Map<String,List<String>> metricEntries = new HashMap<String,List<String>>();
			List<String> metrics = new ArrayList<String>();
//			System.out.println(pipelineId + "#########");
			if (pipelineMetricseResults != null && pipelineMetricseResults.size() > 0) {
				String inputCount = pipelineMetricseResults.get("counters").get("pipeline.batchInputRecords.counter").get("count").toString();
			    String lastRecordTime = pipelineMetricseResults.get("gauges").get("RuntimeStatsGauge.gauge").get("value").get("timeOfLastReceivedRecord").toString();
//			    System.out.println(lastRecordTime + "#########");
			    if (preLastRecordTime.containsKey(pipelineId) && preInputCount.containsKey(pipelineId)) {
			    	long timeDifference = Long.parseLong(lastRecordTime) - Long.parseLong(preLastRecordTime.get(pipelineId));
			    	long batchDifference = Long.parseLong(inputCount) - Long.parseLong(preInputCount.get(pipelineId));
//			    	System.out.println(preInputCount.get(pipelineId)+"@@@@@@");
			    	if (timeDifference > 0 && batchDifference > 0) {
			    		String efficiency = String.valueOf((timeDifference*1.0000/batchDifference*1.0000));
			    		metrics.add(lastRecordTime);
			    		metrics.add(inputCount);
			    		metrics.add(efficiency);
			    		preLastRecordTime.put(pipelineId, lastRecordTime);
			    		preInputCount.put(pipelineId, inputCount);
			    	} else{
			    			metrics.add(preLastRecordTime.get(pipelineId));
			    			metrics.add(inputCount);
			    			metrics.add("0");
			    	}
			    } else {
//			    	System.out.println("fist data record");
			    	preLastRecordTime.put(pipelineId, lastRecordTime);
			    	preInputCount.put(pipelineId, inputCount);
			    	firstRecordTime.put(pipelineId, lastRecordTime);
			    }
			    System.out.println(metrics);
			    
				if (lastRecordTime != null && !lastRecordTime.trim().isEmpty()) metricEntries.put(pipelineId, metrics);
			} else {
				preLastRecordTime.remove(pipelineId);
				preInputCount.remove(pipelineId);
		    	firstRecordTime.remove(pipelineId);
				zeroCount.remove(pipelineId);
			}
			return metricEntries;
		}).thenAccept(metricEntries -> {
			for (String key : metricEntries.keySet()) {
				System.out.println(pipelines.get(pipelineId));
				Logger.write(pipelineId + " " + metricEntries.get(key).get(0) + " " + metricEntries.get(key).get(1) + " " + metricEntries.get(key).get(2)+ " " + pipelines.get(pipelineId));
			}
		});
	}
	
}
