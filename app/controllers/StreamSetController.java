package controllers;


import java.util.HashMap;
import java.util.Map;

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

//	pipelineIds = eventLogger.pullEventLogFromStreamSets();
	public Result start(Http.Request request) {
//	    DynamicForm pipelineForm = formFactory.form().bindFromRequest(request);
//		String pipelineIds = pipelineForm.;
		streamSetsHistoryExtractor.eventLogger.setPipelines();
		String[] pipelineIds = request.body().asFormUrlEncoded().get("pipelineId");
		for (String pipelineId : pipelineIds)
			streamSetsHistoryExtractor.eventLogger.pipelineIds.add(pipelineId);
		return ok(views.html.start.render());
	}
	
	public Result listPipelines() throws InterruptedException {
		streamSetsHistoryExtractor.eventLogger.setPipelines();
		Thread.sleep(1000);
		Map<String,String> pipelines = new HashMap<String,String>(streamSetsHistoryExtractor.eventLogger.pipelines);
		Content html = views.html.list.render(pipelines);
		return ok(html);
	}
	
	public Result stop() {
		streamSetsHistoryExtractor.eventLogger.pipelineIds.clear();
		return ok(views.html.index.render());
	}
}
