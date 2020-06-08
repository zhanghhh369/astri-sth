/**
 * 
 */
package utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import play.libs.Json;

/**
 * Help class for generating JSON ObjectNode<br>
 * Latest update: 2018-02-23
 * @author srwu
 *
 */
public class ResultGenerator {

  public static ObjectNode createResult(String filed, String content) {
    ObjectNode resultNode = Json.newObject();
    resultNode.put(filed, content);
    return resultNode;
  }

  /**
   * Create success node according to {@code isSuccess}.
   * @param isSuccess
   * @return {
   * 			  "success": {@code isSuccess}
   * 		   }
   */
  public static ObjectNode createSuccess(boolean isSuccess) {
    ObjectNode resultNode = Json.newObject();
    resultNode.put("success", isSuccess);
    return resultNode;
  }

  /**
   * Set success to true and put {@code successInfo} in "info" field.
   * @param successInfo
   * @return {
   * 			  "success": true,
   *            "info": {@code successInfo}
   * 		   }
   */
  public static ObjectNode createSuccessInfo(String successInfo) {
    ObjectNode resultNode = Json.newObject();
    resultNode.put("success", true);
    resultNode.put("info", successInfo);
    return resultNode;
  }

  /**
   * Set success to false and put {@code errorMsg} in "error" field.
   * @param errorMsg
   * @return {
   * 			  "success": false,
   *            "error": {@code errorMsg}
   * 		   }
   */
  public static ObjectNode createErrorNode(String errorMsg) {
    ObjectNode resultNode = Json.newObject();
    resultNode.put("success", false);
    resultNode.put("error", errorMsg);
    return resultNode;
  }

  /**
   * Set success to false and put {@code errorMsg} in "error" field (ArrayNode).
   * @param errorMsg
   * @return {
   * 			  "success": false,
   *            "error": [
   *               {@code errorMsg}
   *             ]
   * 		   }
   */
  public static ObjectNode createErrorArray(String errorMsg) {
    ObjectNode resultNode = Json.newObject();
    resultNode.put("success", false);
    ArrayNode errorArray = resultNode.putArray("error");
    errorArray.add(errorMsg);
    return resultNode;
  }

  /**
   * Set success to false and add {@code errorMsg} in "error" field (ArrayNode) of {@code resultNode}.
   * Return {@code resultNode} for method chaining.
   * @param resultNode
   * @param errorMsg
   * @return {
   * 			  "success": false,
   *            "error": [
   *               {@code errorMsg}
   *             ]
   * 		   }
   */
  public static ObjectNode addErrorMsg(ObjectNode resultNode, String errorMsg) {
    resultNode.put("success", false);
    JsonNode errorNode = resultNode.path("error");

    // no exist error, create an array node
    if(errorNode.isMissingNode()) {
      errorNode = resultNode.putArray("error");
    }

    // field "error" is a text node, then get the content and create an array node
    if(!errorNode.isArray()) {
      String prvError = errorNode.asText();
      errorNode = resultNode.putArray("error");
      ((ArrayNode)errorNode).add(prvError);
    }
    ((ArrayNode)errorNode).add(errorMsg);

    return resultNode;
  }
}
