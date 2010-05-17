/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package com.sun.rave.web.ui.util;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;

import com.sun.rave.web.ui.component.Upload;
import com.sun.rave.web.ui.util.MessageUtil;

/**
 * <p>Use the UploadFilter if your application contains an Upload component 
 * (&lt;ui:upload&gt; tag).</p> 
 * <p>Configure the filter by declaring the filter element in the web application's
 * deployment descriptor.</p> 
 * <pre> 
  &lt;filter&gt;
    &lt;filter-name&gt;UploadFilter&lt;/filter-name&gt;
    &lt;filter-class&gt;com.sun.rave.web.ui.util.UploadFilter&lt;/filter-class&gt;
  &lt;/filter&gt;
 </pre>
 *<p>Map the filter to the FacesServlet, for example</p>
 *<pre>
  &lt;filter-mapping&gt;
    &lt;filter-name&gt;UploadFilter&lt;/filter-name&gt;
    &lt;servlet-name&gt;FacesServlet&lt;/servlet-name&gt;
  &lt;/filter-mapping&gt;
 </pre> 
 *<p>The UploadFilter uses the Apache commons fileupload package. You can 
 *configure the parameters of the DiskFileUpload class by specifying init 
 *parameters to the Filter. The following parameters are available: 
 *<ul> 
 *<li><code>maxSize</code> The maximum allowed upload size in bytes. 
 *If negative, there is no maximum. The default value is 1,000,000.</li> 
 *
 *<li><code>sizeThreshold</code>The implementation of the uploading 
 * functionality uses temporary storage of the file contents before the 
 * Upload component stores them per its configuration. In the temporary 
 * storage, smaller files are stored in memory while larger files are 
 * written directly to disk . Use this parameter 
 * to specify an integer value of the cut-off where files should be 
 * written to disk. The default value is 4096 bytes.</li> 
 *<li><code>tmpDir</code> Use this directory to specify the directory to 
 *be used for temporary storage of files. The default behaviour is to use
 *the directory specified in the system property "java.io.tmpdir". </li> 
 *</ul> 

 */
public class UploadFilter implements Filter {
    
    /**
     * The name of the filter init parameter used to specify the maximum 
     * allowable file upload size.
     */
    public static final String MAX_SIZE = "maxSize";
    /**
     * The name of the filter init parameter used to specify the byte 
     * size above which temporary storage of files is on disk.
     */
    public static final String SIZE_THRESHOLD = "sizeThreshold";
    /**
     * The name of the filter init parameter used to specify
     * the directory to be used for temporary storage of 
     * uploaded files.
     */
    public static final String TMP_DIR = "tmpDir";
    
    private long maxSize = 1000000;
    private int sizeThreshold = 4096; 
    private String tmpDir = System.getProperty("java.io.tmpdir");
    private String messages = "com.sun.rave.web.ui.resources.LogMessages";
    // <RAVE>
    private String filterName;
    // </RAVE>
    
    private static final boolean DEBUG = false;
    
    
    /**
     * <p>The upload filter checks if the incoming request has multipart content. 
     * If it doesn't, the request is passed on as is to the next filter in the chain. 
     * If it does, the filter processes the request for form components. If it finds
     * input from an Upload component, the file contents are stored for access by the 
     * Upload component's decode method. </p>
     * <p>For other form components, the input is processed and used to create a 
     * request parameter map. The original incoming request is wrapped, and the 
     * wrapped request is configured to use the created map. This means that 
     * subsequent filters in the chain (and Servlets, and JSPs) see the 
     * input from the other components as request parameters.</p> 
     *<p>For advanced users: the UploadFilter uses the Apache commons FileUpload
     *package to process the file upload. When it detects input from an Upload
     *component, a <code>org.apache.commons.fileupload.FileItem</code> is placed
     * in a request attribute whose name is the ID of the HTML input element 
     * written by the Upload component.</p> 
     *
     *
     * @param response The servlet response
     * @param request The servlet request we are processing
     * @param chain The filter chain we are processing
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet error occurs
     */
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest)request;
        
        if(FileUpload.isMultipartContent(req)) {
            
            DiskFileUpload fu = new DiskFileUpload();
            // maximum size before a FileUploadException will be thrown
            // Store this in a context parameter perhaps?
            fu.setSizeMax(maxSize);
            // maximum size that will be stored in memory
            fu.setSizeThreshold(sizeThreshold);
            // the location for saving data that is larger than getSizeThreshold()           
            fu.setRepositoryPath(tmpDir);

            // Fix copied from Woodstock for 6420807-2139833:
            // files with names in other languages (like Japanese) are not
            // being uploaded with the proper names. Proper encoding has to 
            // be set for this to happen.
            if (request.getCharacterEncoding() != null) {
                fu.setHeaderEncoding(request.getCharacterEncoding());
            } else {
                /*
                 * Warning: this should be set to the charset that the document
                 * containing the form was encoded in, but that requires keeping
                 * server state.
                 */ 
                fu.setHeaderEncoding("UTF-8");
            }
            // End of woodstock fix
            
            List fileItems = null;
            try {
                fileItems = fu.parseRequest(req);
            } catch(FileUploadException fue) {
                request.setAttribute(Upload.UPLOAD_ERROR_KEY, fue); 
                request.setAttribute(Upload.FILE_SIZE_KEY, String.valueOf(maxSize));
            }
            
            Hashtable parameters = null; 
            if(fileItems != null) {
		parameters = parseRequest(fileItems, req); 
	    } 
            else { 
                parameters = new Hashtable(); 
            } 
            
            UploadRequest wrappedRequest  = new UploadRequest(req, parameters);
            chain.doFilter(wrappedRequest, response);
            Enumeration e = request.getAttributeNames(); 
            while (e.hasMoreElements()) { 
                Object o = request.getAttribute(e.nextElement().toString()); 
                if(o instanceof FileItem) { 
                    ((FileItem)o).delete();
                }
            }
        }
        else { 
             chain.doFilter(request, response);
        }        
    }
    

    private Hashtable parseRequest(List fileItems, 
			           HttpServletRequest request) { 
        
        if(DEBUG) log("parseRequest()");
        
        // Iterate over the FileItems to see if any of them correspond
        // to the ID of an input type="file" that is rendered inside a 
        // a span. This happens if the component has a label attribute or 
        // a label facet, and in this case, the DOM id of the input 
        // element is created by the component in such a way that we 
        // can match it directly. If there is a match, a request attribute 
        // is added for the corresponding FileItem . If there is no match, 
        // add the fileItem to a map, with the fieldID as the key. 
    
	Iterator iterator = fileItems.iterator();
	String fieldID = null; 
	FileItem fileItem = null; 
        Map fileItemsMap = new TreeMap(); 
        
        if(DEBUG) log("\tFirst pass through the parameters which are"); 
	while(iterator.hasNext()) {
	    fileItem = (FileItem)(iterator.next());
	    fieldID = fileItem.getFieldName(); 
            if(DEBUG) log("\t\t" + fieldID); 
	    if(fieldID.endsWith(Upload.INPUT_ID)) {                       
		request.setAttribute(fieldID, fileItem);
                if(DEBUG) log(fieldID + " added to the request parameters"); 
            }
            else { 
                fileItemsMap.put(fieldID, fileItem);
            }
	} 
        
        // Iterate over the FileItems to see if any of them correspond
        // to a hidden field used to identify a FileUpload which does
        // not have an associated label. (In that case, the ID of the 
        // input element is the same of the component ID, so we can't 
        // tell by looking at the ID directly.) If so, we take the value 
        // of the hidden ID, which is the ID of the input component and 
        // get the corresponding file item. Both the ID of the hidden 
        // component and that of the input itself are added to the 
        // parameters to be ignored. 
      
        if(DEBUG) log("\tSecond pass through the parameters"); 
        ArrayList removes = new ArrayList();
        ArrayList unlabeledUploads = new ArrayList(); 
	iterator = fileItemsMap.keySet().iterator(); 
        String param = null; 
	while(iterator.hasNext()) {
	    fieldID = (String)(iterator.next()); 
	    if(fieldID.endsWith(Upload.INPUT_PARAM_ID)) {
		fileItem = (FileItem)(fileItemsMap.get(fieldID)); 
                param = fileItem.getString(); 
                unlabeledUploads.add(param);
                removes.add(fieldID);
                if(DEBUG) log("\tFound other fileUpload for parameter " + param); 
            } 
        } 
        
        // If we found IDs of any unlabeled Uploads, we create request 
        // attributes for them too, and add their IDs to the list of IDs
        // to remove. 
        if(!unlabeledUploads.isEmpty()) {
            if(DEBUG) log("\tFound unlabeledUploads ");
            iterator = unlabeledUploads.iterator();
            while(iterator.hasNext()) {
                fieldID = iterator.next().toString();
                fileItem = (FileItem)(fileItemsMap.get(fieldID));   
                request.setAttribute(fieldID.concat(Upload.INPUT_ID), fileItem);
                removes.add(fieldID);
                if(DEBUG) log("\tAdd FileItem for " + fieldID.concat(Upload.INPUT_ID));
            }
        }
        
        // If we have any fields to be removed from the parameter map,
        // we do so
        if(!removes.isEmpty()) {
            iterator = removes.iterator();
            while(iterator.hasNext()) {
                fileItemsMap.remove(iterator.next());
            }
        }
          
        // Create a hashtable to use for the parameters, and add the remaining
        // fileItems
        Hashtable parameters = new Hashtable();  
        iterator = fileItemsMap.keySet().iterator();
        Object id = null;
        if(DEBUG) log("\tFinally, create regular parameters for ");
        while(iterator.hasNext()) {
            id = iterator.next();
            // <RAVE> Convert to the right encoding for parameter values
            FileItem aFileItem = (FileItem) fileItemsMap.get(id);
            String encoding = calculateEncoding(aFileItem, request);
            String aParam;
            try {
                aParam = aFileItem.getString(encoding);
            } catch (UnsupportedEncodingException e) {
                aParam = aFileItem.getString();
            }
            parameters.put(id, aParam);
            if(DEBUG) log("\t\t " + id + ":" + aParam);
            // </RAVE>
        }
        
        return parameters; 
    } 

    // <RAVE> I18N fixes for parameter values
    private String calculateEncoding(FileItem fileItem, HttpServletRequest req) {
        // TODO parse the FileItem character encoding, ie. charset.
        // fileItem.getContentType();
        // Next, try the request character encoding
        String encoding = req.getCharacterEncoding();
        if (encoding == null) {
            // Fallback to "UTF-8"
            encoding = "UTF-8";
        }
        return encoding;
    }
    // </RAVE>
    
    /**
     * Initializes the Upload filter by reading any init parameters.
     * 
     * @param filterConfig
     *            the filter configuration
     */
    public void init(FilterConfig filterConfig) {
        // <RAVE>
        filterName = filterConfig.getFilterName();
        // </RAVE>

        StringBuffer errorMessageBuffer = new StringBuffer(300); 
        String param = filterConfig.getInitParameter(MAX_SIZE); 
        if(param != null) { 
            try { 
                maxSize = Long.parseLong(param); 
            }
            catch(NumberFormatException nfe) { 
                Object[] params = { MAX_SIZE, param }; 
                String msg = MessageUtil.getMessage
                    (messages, "Upload.invalidLong", params);
                errorMessageBuffer.append(msg);
            }
        } 
        param = filterConfig.getInitParameter(SIZE_THRESHOLD);
        if(param != null) {       
            try { 
                 sizeThreshold = Integer.parseInt(param); 
            }
            catch(NumberFormatException nfe) { 
                Object[] params = { SIZE_THRESHOLD, param }; 
                errorMessageBuffer.append(" ");
                String msg = MessageUtil.getMessage
                    (messages, "Upload.invalidInt", params);
                errorMessageBuffer.append(msg); 
            }
        }
        param = filterConfig.getInitParameter(TMP_DIR);
        if(param != null) { 
            tmpDir = param; 
            File dir = new File(tmpDir); 
            if(!dir.canWrite()) { 
                Object[] params = { TMP_DIR, param }; 
                errorMessageBuffer.append(" ");
                String msg = MessageUtil.getMessage(messages, 
                                                    "Upload.invalidDir", 
                                                    params);
                errorMessageBuffer.append(msg);
            }
        } 
        String error = errorMessageBuffer.toString(); 
        if(error.length() > 0) { 
            throw new RuntimeException(error);
        }
    }
    
    /**
     * Return a String representation of the UploadFilter
     * @return A String representation of the UploadFilter
     */
    public String toString() {
        
        return (this.getClass().getName());      
    }

    /** Invoked when the Filter is destroyed */ 
    public void destroy() {
        // do nothing
    }
    
    private void log(String s) { 
        System.out.println(getClass().getName() + "::" + s); 
    }
      
    /**
     *  This request wrapper class extends the support class HttpServletRequestWrapper,
     *  which implements all the methods in the HttpServletRequest interface, as
     *  delegations to the wrapped request.
     *  You only need to override the methods that you need to change.
     *  You can get access to the wrapped request using the method getRequest()
     */
    class UploadRequest extends HttpServletRequestWrapper {
        
        private Hashtable parameters;
        private static final boolean DEBUG = false;
        
        public UploadRequest(HttpServletRequest request, Hashtable parameters) {
            super(request);
            this.parameters = parameters;
        }
        
        
        public String getParameter(String name) {
            //Thread.currentThread().dumpStack();
            Object param = parameters.get(name);
            
            if(param instanceof String) {
                return (String)param;
            }
            if(param instanceof String[]) {
                String[] params = (String[])param;
                return params[0];
            }
            return (param == null ? null : param.toString());
        }
        
        /**
         * Returns an array of String objects containing all of the values the given request parameter has, 
         * or null if the parameter does not exist. If the parameter has a single value, the array has a length of 1.
         */
        public String[] getParameterValues(String name) {  
            
            // <RAVE>
            // return (String[])parameters.get(name);
            Object value = parameters.get(name);
            if( value == null )
                return new String[0];
            else if( value instanceof String[] )
                return (String[])parameters.get(name);
            else // Must be one big string
                return new String[] { value.toString() };
            // <RAVE>
        }
        
        public Enumeration getParameterNames() {
            return parameters.keys();
        }
        
        public Map getParameterMap() {
            return parameters;
        }
    }
}


