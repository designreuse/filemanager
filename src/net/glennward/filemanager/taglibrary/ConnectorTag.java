/**
 * 
 */
package net.glennward.filemanager.taglibrary;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import net.glennward.filemanager.transferobjects.FileDetails;
import net.glennward.filemanager.transferobjects.FileProperties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * @author gjw
 * 
 */
public class ConnectorTag extends TagSupport {

	private static final String DEFAULT_ERROR_RESPONSE = "{\"Code\":-1,\"Error\":\"An error occurred\"}";

	/**
   * 
   */
	private static final long serialVersionUID = 5869449320498227135L;

	private String getsize = null;

	private String getsizes = null;
	/**
   * 
   */
	protected final Log logger = LogFactory.getLog(getClass().getName());

	private ObjectMapper mapper = new ObjectMapper();

	private String mode = null;

	private String name = null;

	private String newPath = null;

	private String oldPath = null;

	private String path = null;

	/**
	 * add a new folder
	 * 
	 * @return JSON result
	 * @throws JspException
	 */
	private Map<String, Object> addFolder() {

		Map<String, Object> result = new HashMap<String, Object>();

		String error = "No error";
		int code = 0;

		if (StringUtils.isNotBlank(path)) {
			if (StringUtils.isNotBlank(name)) {
				File folder = new File(getRealPath(path) + "/" + name);
				if (!folder.exists()) {
					boolean success = folder.mkdir();
					if (!success) {
						error = "An error occurred. Please re-try.";
						code = -1;
					}
				} else {
					error = "File/Folder exists";
					code = -1;
				}
			} else {
				error = "name parameter is missing";
				code = -1;
			}
		} else {
			error = "path parameeter is missing";
			code = -1;
		}

		result.put("Parent", path);
		result.put("Name", name);
		result.put("Error", error);
		result.put("Code", code);

		return result;
	}

	/**
	 * delete a file or folder
	 * 
	 * @return JSON result
	 * @throws JspException
	 */
	private Map<String, Object> delete() {

		Map<String, Object> result = new HashMap<String, Object>();

		String error = "No error";
		int code = 0;

		if (StringUtils.isNotBlank(path)) {
			File file = new File(getRealPath(path));
			if (file.exists()) {
				boolean success = file.delete();
				if (!success) {
					error = "Unable to delete: May contain folders and/or files";
					code = -1;
				}
			} else {
				error = "Unable to delete: File/folder does not exist";
				code = -1;
			}
		} else {
			error = "path parameter is missing";
			code = -1;
		}

		result.put("Path", path);
		result.put("Error", error);
		result.put("Code", code);

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
	 */
	@Override
	public int doStartTag() throws JspException {

		HttpServletRequest request = (HttpServletRequest) pageContext
				.getRequest();

		try {
			if (mode.equalsIgnoreCase("getfolder")) {
				List folders = getFolder();
				mapper.writeValue(pageContext.getOut(), folders);
				if (logger.isInfoEnabled()) {
					logger.info(mapper.writeValueAsString(folders));
				}
			} else {
				Map<String, Object> result = null;

				if ("getinfo".equalsIgnoreCase(mode)) {
					result = getInfo();
				} else if ("rename".equalsIgnoreCase(mode)) {
					result = rename();
				} else if ("delete".equalsIgnoreCase(mode)) {
					result = delete();
				} else if ("addfolder".equalsIgnoreCase(mode)) {
					result = addFolder();
				} else {
					result = new HashMap<String, Object>();
					result.put("Error", "Missing or invalid mode parameter");
					result.put("Code", -1);
				}

				mapper.writeValue(pageContext.getOut(), result);

				if (logger.isInfoEnabled()) {
					logger.info(mapper.writeValueAsString(result));
				}
			}
		} catch (JsonMappingException jme) {
			if (logger.isErrorEnabled()) {
				logger.error(jme.getMessage());
				logger.error(jme.getStackTrace());
			}
			throw new JspException(jme);
		} catch (JsonGenerationException jge) {
			if (logger.isErrorEnabled()) {
				logger.error(jge.getMessage());
				logger.error(jge.getStackTrace());
			}
			throw new JspException(jge);
		} catch (IOException ioe) {
			if (logger.isErrorEnabled()) {
				logger.error(ioe.getMessage());
				logger.error(ioe.getStackTrace());
			}
			throw new JspException(ioe);
		}

		return TagSupport.SKIP_BODY;
	}

	/**
	 * @param file
	 * @return
	 */
	private Map<String, Object> getFileDetails(File file, boolean appendName) {

		FileDetails details = new FileDetails();
		FileProperties properties = new FileProperties();

		// set the name
		String name = file.getName();
		details.setFilename(name);

		// set the path
		details.setPath(this.path);
		if (appendName) {
			details.setPath(details.getPath() + name);
		}

		// set the file type and preview
		setPreview(details, file.isDirectory(), appendName);

		// create calendar and date format objects
		Calendar cal = Calendar.getInstance();
		DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

		// set the date created
		cal.setTimeInMillis(file.lastModified());
		properties.setDateCreated(df.format(cal.getTime()));

		// set the date modified
		cal.setTimeInMillis(file.lastModified());
		properties.setDateModified(df.format(cal.getTime()));

		// set the height
		properties.setHeight("");

		// set the width
		properties.setWidth("");

		// set the size
		properties.setSize(String.valueOf(file.length()));

		// set the properties into the details
		details.setProperties(properties);

		// set the error
		details.setError("No error");

		// set the code
		details.setCode(0);

		Map<String, Object> f = new HashMap<String, Object>();
		f.put("Path", details.getPath());
		f.put("Filename", details.getFilename());
		f.put("File Type", details.getFileType());
		f.put("Preview", details.getPreview());
		f.put("Error", details.getError());
		f.put("Code", details.getCode());

		Map<String, String> fProps = new HashMap<String, String>();
		fProps.put("Date Created", details.getProperties().getDateCreated());
		fProps.put("Date Modified", details.getProperties().getDateModified());
		fProps.put("Height", details.getProperties().getHeight());
		fProps.put("Width", details.getProperties().getWidth());
		fProps.put("Size", details.getProperties().getSize());
		f.put("Properties", fProps);

		return f;
	}

	/**
	 * get the folder information
	 * 
	 * @return JSON result
	 * @throws JspException
	 */
	private List<Map> getFolder() {

		List<Map> result = null;

		String error = "No error";
		int code = 0;

		if (StringUtils.isNotBlank(path)) {
			if (StringUtils.isBlank(getsizes)) {
				getsizes = "true";
			}
			File folder = new File(getRealPath(path));
			if (folder.exists()) {
				String[] files = folder.list();
				result = new ArrayList<Map>(files.length);
				for (String fileName : files) {
					File f = new File(folder.getAbsolutePath() + "/" + fileName);
					result.add(getFileDetails(f, true));
				}
			} else {
				error = "Folder does not exist";
				code = -1;
			}
		} else {
			error = "path parameter is missing";
			code = -1;
		}

		if (result == null || code == -1) {
			Map<String, Object> errorResponse = new HashMap<String, Object>();
			errorResponse.put("Error", error);
			errorResponse.put("Code", code);
			result = new ArrayList<Map>(1);
			result.add(errorResponse);
		}

		return result;
	}

	/**
	 * @return the getsize
	 */
	public String getGetsize() {
		return getsize;
	}

	/**
	 * @return the getsizes
	 */
	public String getGetsizes() {
		return getsizes;
	}

	/**
	 * get the file information
	 * 
	 * @return JSON result
	 */
	private Map<String, Object> getInfo() {

		Map<String, Object> result = null;

		String error = "No error";
		int code = 0;

		if (StringUtils.isNotBlank(path)) {
			if (StringUtils.isBlank(getsize)) {
				getsize = "true";
			}
			File file = new File(getRealPath(path));
			if (file.exists()) {
				result = getFileDetails(file, false);
			} else {
				error = "File/Folder does not exist";
				code = -1;
			}
		} else {
			error = "path parameter missing";
			code = -1;
		}

		if (result == null || code == -1) {
			result = new HashMap<String, Object>();
			result.put("Error", error);
			result.put("Code", code);
		}

		return result;
	}

	/**
	 * @return the logger
	 */
	public Log getLogger() {
		return logger;
	}

	/**
	 * @return the mode
	 */
	public String getMode() {
		return mode;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the newPath
	 */
	public String getNewPath() {
		return newPath;
	}

	/**
	 * @return the oldPath
	 */
	public String getOldPath() {
		return oldPath;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * get the absolute file system path
	 * 
	 * @return a String representing the absolute path
	 */
	private String getRealPath(String path) {

		HttpServletRequest request = (HttpServletRequest) pageContext
				.getRequest();
		String realPath = request.getSession().getServletContext().getRealPath(
				path);
		return realPath;
	}

	/**
	 * rename a file or folder
	 * 
	 * @return JSON result
	 * @throws JspException
	 */
	private Map<String, Object> rename() {

		Map<String, Object> result = new HashMap<String, Object>();

		String error = "No error";
		int code = 0;

		String oldFileName = null;
		String newWebPath = null;

		if (StringUtils.isNotBlank(oldPath)) {
			if (StringUtils.isNotBlank(newPath)) {
				File oldFile = new File(getRealPath(oldPath));
				if (oldFile.exists()) {
					oldFileName = oldFile.getName();
					File newFile = new File(oldFile.getParent()
							+ File.separator + newPath);
					if (!newFile.exists()) {
						// rename the file or folder
						oldFile.renameTo(newFile);

						/*
						 * determine the relative web path so it can be passed
						 * back with the new file name (required by file
						 * manager)
						 */
						int count = StringUtils.countMatches(oldPath, oldFile
								.getName());
						if (count == 1) {
							newWebPath = StringUtils.replace(oldPath, oldFile
									.getName(), newPath);
						} else {
							// TODO: determine another way to get relative web
							// path?
						}
					} else {
						error = "A file/folder with the new name already exists";
						code = -1;
					}
				} else {
					error = "Old file/folder does not exist";
					code = -1;
				}
			} else {
				error = "new parameter is missing";
				code = -1;
			}
		} else {
			error = "old parameter is missing";
			code = -1;
		}

		result.put("Old Path", oldPath);
		result.put("Old Name", oldFileName);
		result.put("New Path", newWebPath);
		result.put("New Name", newPath); // yes, setting this to newPath
		result.put("Error", error);
		result.put("Code", code);

		return result;
	}

	/**
	 * @param getsize
	 *            the getsize to set
	 */
	public void setGetsize(String getsize) {
		this.getsize = getsize;
	}

	/**
	 * @param getsizes
	 *            the getsizes to set
	 */
	public void setGetsizes(String getsizes) {
		this.getsizes = getsizes;
	}

	/**
	 * @param mode
	 *            the mode to set
	 */
	public void setMode(String mode) {
		this.mode = mode;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param newPath
	 *            the newPath to set
	 */
	public void setNewPath(String newPath) {
		this.newPath = newPath;
	}

	/**
	 * @param oldPath
	 *            the oldPath to set
	 */
	public void setOldPath(String oldPath) {
		this.oldPath = oldPath;
	}

	/**
	 * @param path
	 *            the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * Set the preview icon
	 * 
	 * @param details
	 * @param appendName
	 */
	private void setPreview(FileDetails details, boolean isDirectory,
			boolean appendName) {

		if (isDirectory) {

			details.setFileType("dir");

			details.setPreview("images/fileicons/_Close.png");
		} else {

			String name = details.getFilename();
			int dotIndex = name.lastIndexOf('.');
			String fileType = dotIndex > 0 ? name.substring(dotIndex + 1) : "";
			details.setFileType(fileType);

			String applicationPath = pageContext.getServletContext()
					.getServletContextName();

			String preview = "images/fileicons/default.png";
			if (StringUtils.isNotEmpty(fileType)) {
				for (Image image : Image.values()) {
					if (image.extension().equalsIgnoreCase(fileType)) {
						// set preview to be same as path
						preview = appendName ? "/" + applicationPath + "/"
								+ this.path + name : "/" + applicationPath
								+ "/" + this.path;
						break;
					} else {
						// set preview to be custom icon
						preview = "images/fileicons/" + fileType + ".png";
					}
				}
			}
			details.setPreview(preview);
		}
	}

}
