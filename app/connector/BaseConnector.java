package connector;

import static utils.ResultGenerator.createErrorNode;
import static utils.ResultGenerator.createSuccess;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.CompletionStage;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;

import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;

/**
 * Parent class for all REST connectors
 * @author EK
 */
public abstract class BaseConnector {

	private @Inject WSClient ws;  // Injected by Guice with auto close
	private @Inject Config config;
	//protected final LogHelper LOG;

	public final String sURL, sPATH, sPREFIX, sCOOKIE_NAME;

	public String getURL() { return sURL+sPATH; }

	/**
	 * Constructor to get host, port, and path from configuration file
	 * @param config Configuration file
	 * @param sConfigPrefix Configuration prefix
	 */
	protected BaseConnector(Config config, String sConfigPrefix) {
		sPREFIX = sConfigPrefix+".";
		sURL = (config.hasPath(sPREFIX+"protocol")?config.getString(sPREFIX+"protocol"):"http")+
				"://"+config.getString(sPREFIX+"host")+":"+config.getString(sPREFIX+"port");
		sPATH = config.getString(sPREFIX+"path");
		sCOOKIE_NAME = config.hasPath(sPREFIX+"cookieName")?config.getString(sPREFIX+"cookieName"):null;
		//this.LOG = new LogHelper("connector", getClass().getSimpleName());
	}

	/**
	 * Asynchronous POST request
	 * @param url Host and path to POST
	 * @param sContentType Content type of request
	 * @param sContent Content to POST
	 * @return Promise of web response
	 */
	protected CompletionStage<WSResponse> post(String url, String sContentType, String sContent) {
		return getHTTPRequest(url).setContentType(sContentType).post(sContent);
	}

	/**
	 * Get HTTP request for setting query parameters
	 * @param url Host URL
	 * @return HTTP request
	 */
	protected WSRequest getHTTPRequest(String url) {
		WSRequest req = ws.url(url).setRequestTimeout(Duration.ofSeconds(config.getLong(sPREFIX+"timeout.sec")));
//		req = addCookies(req);
		return req;
	}
	
//	/**
//	 * Add headers and cookies to HTTP request
//	 * @param req HTTP request
//	 * @return HTTP request with added headers and cookies
//	 */
//	private WSRequest addCookies(WSRequest currReq) {
//		if (Http.Context.current.get() == null) return currReq;
//		Request prevReq = Http.Context.current().request();
//		prevReq.cookies().forEach(prevCookie -> currReq.addCookie(prevCookie));
//		return currReq;
//	}

	/**
	 * Handle HTTP response with JSON
	 * @param response Promise of HTTP response
	 * @return CompletionStage of JSON returned in response
	 * {
	 * 		"message":	"<error message (if any)>",
	 * 		"token":	"<HTTP response cookie (if any)>",
	 * 		...
	 * }
	 * @throws IOException 
	 */
	protected CompletionStage<ObjectNode> handleJsonResponse(CompletionStage<WSResponse> stage) {
		return stage.thenApply(response -> {
			ObjectNode feedback = null;
			try {
				int status = response.getStatus();
				switch (status) {
				case 200:
				case 201:
				case 202:
					feedback = (ObjectNode) createSuccess(true).set("results", response.asJson());
					break;
				case 400:
				case 401:
				case 500:
					feedback = createErrorNode(response.getBody()).put("code", status);
					break;
				default:
					feedback = createErrorNode(getDefaultError(status));
					//LOG.error(feedback.get("message").textValue());
				}
			} catch (RuntimeException ex) {
				feedback = createErrorNode("Cannot parse the feedback from " + sURL + " => " + ex.getMessage());
				//LOG.error("handleJsonResponse:" + response.getBody().substring(0, 100));
			}
			return feedback;
		}).exceptionally(ex -> {
			ex.printStackTrace();
			return createErrorNode("Cannot connect to " + sURL + " (" + ex + ")");
		});
	}

	/**
	 * Centralize error message for unknown HTTP code
	 * @param nStatus HTTP code
	 * @return Error message for the code
	 */
	String getDefaultError(int nStatus) {
		return "Unexpected HTTP code from "+sURL+": "+nStatus;
	}

	/**
	 * Handle HTTP response as an InputStream.
	 * @param response CompletionStage of HTTP response
	 * @return CompletionStage of InputStream for success; otherwise throws RuntimeException 
	 */
	CompletionStage<byte[]> handleByteArrayResponse(CompletionStage<WSResponse> stage) {
		return stage.thenApply(response -> { 
			int status = response.getStatus();
			switch (status) {
			case 200:
				try {
					//					logger.debug("handleByteArrayResponse:"+response.getBodyAsStream());
					return response.getBody().getBytes("UTF-8");
				} catch (Exception e) {
					throw new RuntimeException(e);
				}

				// Server error -> Extract error message
			case 400: case 500:
				throw new RuntimeException(response.getBody());

				// Other HTTP responses
			default:
				throw new RuntimeException(getDefaultError(status));
			}
		});
	}

}