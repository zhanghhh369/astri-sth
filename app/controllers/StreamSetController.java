package controllers;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import play.data.*;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.twirl.api.Content;
import services.StreamSetsHistoryExtractor;

public class StreamSetController extends Controller {

	public @Inject StreamSetsHistoryExtractor streamSetsHistoryExtractor;
	public @Inject FormFactory formFactory;

	
	public Result start(Http.Request request) {
		String[] pipelineIds = request.body().asFormUrlEncoded().get("pipelineId");
		for (String pipelineId : pipelineIds)
			streamSetsHistoryExtractor.eventLogger.pipelineIds.add(pipelineId);
		return ok(views.html.start.render());
	}
	
	public Result listPipelines() throws InterruptedException {
		streamSetsHistoryExtractor.eventLogger.setPipelines();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<Entry<String, String>> pipelineList = new ArrayList<Map.Entry<String, String>>(streamSetsHistoryExtractor.eventLogger.pipelineList);
		Content html = views.html.list.render(pipelineList);
		return ok(html);
	}
	
	public Result stop() {
		streamSetsHistoryExtractor.eventLogger.pipelineIds.clear();
		return ok(views.html.index.render());
	}
}
