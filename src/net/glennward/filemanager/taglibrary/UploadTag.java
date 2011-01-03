/**
 * 
 */
package net.glennward.filemanager.taglibrary;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * @author Glenn
 * 
 */
public class UploadTag extends TagSupport {

	private static final String ERROR_REQUEST_IS_NOT_MULTIPART = "Request is not multipart";
	private static final String TAG_TEXTAREA_END = "</textarea>";
	private static final String TAG_TEXTAREA_START = "<textarea>";
	/**
	   * 
	   */
	protected final Log logger = LogFactory.getLog(getClass().getName());
	private ObjectMapper mapper = new ObjectMapper();
	private String mode = null;

	/**
	 * @param request
	 * @throws Exception
	 */
	private Map<String, Object> add() {

		Map<String, Object> response = new HashMap<String, Object>();

		String error = "No error";
		int code = 0;

		// form field variables
		// String mode = null;
		String currentPath = null;

		// file upload variables
		String uploadedFileName = null;
		InputStream uploadedStream = null;

		// Create a factory for disk-based file items
		FileItemFactory factory = new DiskFileItemFactory();

		// Create a new file upload handler
		ServletFileUpload upload = new ServletFileUpload(factory);

		// Parse the request
		try {
			List<FileItem> items = upload
					.parseRequest((HttpServletRequest) pageContext.getRequest());

			// Process the uploaded items
			Iterator<FileItem> iter = items.iterator();
			while (iter.hasNext()) {
				FileItem item = iter.next();

				if (item.isFormField()) {
					// Process a regular form field
					if (item.isFormField()) {
						String fieldName = item.getFieldName();
						String fieldValue = item.getString();
						if ("currentpath".equals(fieldName)) {
							currentPath = fieldValue;
						}
						// else if ("mode".equals(fieldName)) {
						// mode = fieldValue;
						// }
					}
				} else {
					// Process a file upload
					if (!item.isFormField()) {
						uploadedFileName = item.getName();
						uploadedStream = item.getInputStream();
					}
				}
			}

			// copy the file to the file system
			if (uploadedStream != null) {
				File uploadedFile = new File(getRealPath(currentPath) + "/"
						+ uploadedFileName);
				OutputStream out = new FileOutputStream(uploadedFile);
				int status = IOUtils.copy(uploadedStream, out);
				if (status == -1) {
					error = "File upload unsuccessful. Please retry.";
					code = -1;
				}
				out.close();
				uploadedStream.close();
			}
		} catch (Exception e) {
			error = e.getMessage();
			code = -1;
		}

		response.put("Path", currentPath);
		response.put("Name", uploadedFileName);
		response.put("Error", error);
		response.put("Code", code);

		return response;
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
		// Check that we have a file upload request
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);

		try {
			if (isMultipart) {
				// "add" is only multipart request so safe to assume it is a
				// file upload
				Map<String, Object> uploadResponse = add();
				if (logger.isInfoEnabled()) {
					logger.info(mapper.writeValueAsString(uploadResponse));
				}
				JspWriter out = pageContext.getOut();
				out.write(TAG_TEXTAREA_START);
				out.write(mapper.writeValueAsString(uploadResponse));
				out.write(TAG_TEXTAREA_END);
			} else {
				if (logger.isErrorEnabled()) {
					logger.error(ERROR_REQUEST_IS_NOT_MULTIPART);
				}
				throw new JspException(ERROR_REQUEST_IS_NOT_MULTIPART);

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
	 * @return the mode
	 */
	public String getMode() {
		return mode;
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

}
