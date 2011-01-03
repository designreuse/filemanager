/**
 * 
 */
package net.glennward.filemanager.transferobjects;


/**
 * @author gjw
 *
 */
public class FileDetails {

  private Integer code = null;
  private String error = null;
  private String filename = null;
  private String fileType = null;
  private String path = null;
  private String preview = null;
  private FileProperties properties = null;
  
  
  /**
   * @return the code
   */
  public Integer getCode() {
    return code;
  }
  /**
   * @return the error
   */
  public String getError() {
    return error;
  }
  /**
   * @return the filename
   */
  public String getFilename() {
    return filename;
  }
  /**
   * @return the fileType
   */
  public String getFileType() {
    return fileType;
  }
  /**
   * @return the path
   */
  public String getPath() {
    return path;
  }
  /**
   * @return the preview
   */
  public String getPreview() {
    return preview;
  }
  /**
   * @return the properties
   */
  public FileProperties getProperties() {
    return properties;
  }
  /**
   * @param code the code to set
   */
  public void setCode(Integer code) {
    this.code = code;
  }
  /**
   * @param error the error to set
   */
  public void setError(String error) {
    this.error = error;
  }
  /**
   * @param filename the filename to set
   */
  public void setFilename(String filename) {
    this.filename = filename;
  }
  /**
   * @param fileType the fileType to set
   */
  public void setFileType(String fileType) {
    this.fileType = fileType;
  }
  /**
   * @param path the path to set
   */
  public void setPath(String path) {
    this.path = path;
  }
  /**
   * @param preview the preview to set
   */
  public void setPreview(String preview) {
    this.preview = preview;
  }
  /**
   * @param properties the properties to set
   */
  public void setProperties(FileProperties properties) {
    this.properties = properties;
  }
  
}
