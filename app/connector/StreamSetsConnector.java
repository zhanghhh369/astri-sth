package connector;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;


import play.libs.ws.WSRequest;

public class StreamSetsConnector extends BaseConnector {
	private final String STREAMSETS_USER;
	private final String STREAMSETS_PASS;
	private final String STREAMSETS_GET_PIPELINE_TITLES;
	private final String STREAMSETS_GET_PIPELINE_METRICS;

	@Inject
	public StreamSetsConnector(Config config) {
		super(config, "streamsets");
		
		this.STREAMSETS_USER = config.hasPath("streamsets.username") ? config.getString("streamsets.username") : null;
		this.STREAMSETS_PASS = config.hasPath("streamsets.password") ? config.getString("streamsets.password") : null;
		this.STREAMSETS_GET_PIPELINE_TITLES = config.hasPath("streamsets.get_pipeline_titles") ? config.getString("streamsets.get_pipeline_titles") : null;
		this.STREAMSETS_GET_PIPELINE_METRICS = config.hasPath("streamsets.get_pipeline_metrics") ? config.getString("streamsets.get_pipeline_metrics") : null;
	}
	
//	public List<String> getPipeLineIds(){
//		return PIPELINE_IDS;
//	}
	
	public CompletableFuture<ObjectNode> getPipeLineMetrics(String pipelineId){
		String queryUrl = sURL + sPATH + STREAMSETS_GET_PIPELINE_METRICS;
		queryUrl = queryUrl.replace("{pipelineId}", pipelineId);
		WSRequest req = getHTTPRequest(queryUrl)
				.setAuth(STREAMSETS_USER, STREAMSETS_PASS)
				.addHeader("X-Requested-By","sdc");
		return handleJsonResponse(req.get()).toCompletableFuture();
	}
	
	public CompletableFuture<ObjectNode> getAllPipeLines(){
		String queryUrl = sURL + sPATH + STREAMSETS_GET_PIPELINE_TITLES;
		WSRequest req = getHTTPRequest(queryUrl)
				.setAuth(STREAMSETS_USER, STREAMSETS_PASS)
				.addHeader("X-Requested-By","sdc");
		return handleJsonResponse(req.get()).toCompletableFuture();
	}

}
