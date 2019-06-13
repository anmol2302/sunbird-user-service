package org.sunbird.util.response;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.sunbird.util.responsecode.ResponseCode;

/**
 * This is a common response class for all the layer. All layer will send same response object.
 *
 * @author anmolgupta
 */
public class Response implements Serializable, Cloneable {

  private static final long serialVersionUID = -3773253896160786443L;
  private String id;
  private String ver;
  private String ts;
  private ResponseCode responseCode = ResponseCode.OK;
  private Map<String, Object> result = new HashMap<>();

  /**
   * This will provide request unique id.
   *
   * @return String
   */
  public String getId() {
    return id;
  }

  /**
   * set the unique id
   *
   * @param id String
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * this will provide api version
   *
   * @return String
   */
  public String getVer() {
    return ver;
  }

  /**
   * set the api version
   *
   * @param ver String
   */
  public void setVer(String ver) {
    this.ver = ver;
  }

  /**
   * this will provide complete time value
   *
   * @return String
   */
  public String getTs() {
    return ts;
  }

  /**
   * set the time value
   *
   * @param ts String
   */
  public void setTs(String ts) {
    this.ts = ts;
  }

  /** @return Map<String, Object> */
  public Map<String, Object> getResult() {
    return result;
  }

  /**
   * @param key String
   * @return Object
   */
  public Object get(String key) {
    return result.get(key);
  }

  /**
   * @param key String
   * @param vo Object
   */
  public void put(String key, Object vo) {
    result.put(key, vo);
  }

  /** @param map Map<String, Object> */
  public void putAll(Map<String, Object> map) {
    result.putAll(map);
  }

  public boolean containsKey(String key) {
    return result.containsKey(key);
  }

  /**
   * Set the response code for header.
   *
   * @param code ResponseCode
   */
  public void setResponseCode(ResponseCode code) {
    this.responseCode = code;
  }

  /**
   * get the response code
   *
   * @return ResponseCode
   */
  public ResponseCode getResponseCode() {
    return this.responseCode;
  }

  public Response clone(Response response) {
    try {
      return (Response) response.clone();
    } catch (CloneNotSupportedException e) {
      return null;
    }
  }
}
