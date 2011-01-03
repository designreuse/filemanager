/**
 * 
 */
package net.glennward.filemanager.taglibrary;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * @author Glenn
 * 
 */
public class DownloadTag extends TagSupport {

	/**
	   * 
	   */
	protected final Log logger = LogFactory.getLog(getClass().getName());
	private ObjectMapper mapper = new ObjectMapper();
	private String mode = null;
	private String name = null;
	private String path = null;

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
			Map<String, Object> result = null;

			if ("download".equalsIgnoreCase(mode)) {
				result = download();
			} else {
				result = new HashMap<String, Object>();
				result.put("Error", "Missing or invalid mode parameter");
				result.put("Code", -1);
			}

			if (logger.isInfoEnabled()) {
				logger.info(mapper.writeValueAsString(result));
			}

			mapper.writeValue(pageContext.getOut(), result);
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
	 * download a file
	 * 
	 * @return JSON result
	 * @throws JspException
	 */
	private Map<String, Object> download() {

		Map<String, Object> result = new HashMap<String, Object>();

		String error = "No error";
		int code = 0;

		if (StringUtils.isNotBlank(path)) {
			File file = new File(getRealPath(path));
			if (file.exists()) {
				try {
					HttpServletResponse response = (HttpServletResponse) pageContext.getResponse();

					ServletOutputStream out = response.getOutputStream();
					ServletContext context = pageContext.getServletContext();
					String mimeType = context.getMimeType(file.getName());
					
					response.setContentType((mimeType != null) ? mimeType : "application/octet-stream");
					response.setContentLength((int) file.length());
					response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
					response.setHeader("Cache-Control", "no-cache"); // HTTP 1.1
					response.setHeader("Pragma", "no-cache"); // HTTP 1.0
					response.setDateHeader("Expires", -1);

//					byte[] bbuf = new byte[1024];
					DataInputStream in = new DataInputStream(new FileInputStream(file));
//
//					int length = 0;
//					while ((in != null) && ((length = in.read(bbuf)) != -1)) {
//						out.write(bbuf, 0, length);
//					}

					int status = IOUtils.copy(in, out);

					in.close();
					out.flush();
					out.close();

					
					if (status != 1 && status != (int) file.length()) {
						error = "File download unsuccessful. Please retry.";
						code = -1;
					}
				} catch (IOException ioe) {
					error = "An error occurred. Please retry.";
					code = -1;
				}
			} else {
				error = "File/Folder does not exist";
				code = -1;
			}
		} else {
			error = "path parameter missing";
			code = -1;
		}

		// result.put("Path", path);
		result.put("Error", error);
		result.put("Code", code);

		return result;
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
	 * @param path
	 *            the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

}
