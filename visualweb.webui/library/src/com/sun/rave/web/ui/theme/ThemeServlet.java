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
package com.sun.rave.web.ui.theme;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>Use this servlet to retrieve resource file
 * that control the client-side behaviour and
 * appearance of a web application that uses the
 * Sun Web.Components. The resource files must
 * be in a Jar file that is in the application's
 * classpath. Configure one instance of this servlet
 * per web application using themed components:
 * <pre>
     &lt;servlet&gt;
         &lt;servlet-name&gt;ThemeServlet&lt;/servlet-name&gt;
         &lt;servlet-class&gt;com.sun.rave.web.ui.theme.ThemeServlet&lt;/servlet-class&gt;
      &lt;/servlet&gt;

     &lt;servlet-mapping&gt;
         &lt;servlet-name&gt;ThemeServlet&lt;/servlet-name&gt;
         &lt;url-pattern&gt;/theme/*&lt;/url-pattern&gt;
     &lt;/servlet-mapping&gt;
   </pre>
 * <p>
 *
 * <p>For each theme used in an application, you must ensure that this
 * servlet is mapped to the identifier specified in the "prefix" property
 * of the <code>META-INF/swc_theme.properties</code> resource for that
 * theme.  For the standard themes, this is set to <code>/theme</code> so
 * the mapping illustrated above is sufficient.</p>
 *
 * <p>Note that, since the JAR files for all installed themes are loaded
 * into the same class loader, the actual resource paths for the resources
 * used by each theme <strong>MUST</strong> be unique.  This is true
 * regardless of whether the themes share a prefix or not.</p>
 *
 * @author avk
 */
public class ThemeServlet extends HttpServlet {

    public static final String localeParameter = "com.sun.rave.web.ui.locales"; 
    private final static boolean DEBUG = false;
    private final static Map respType = new HashMap();

    // Some mime-types... by extension
    static {
	// There is no IANA registered type for JS files. See 
	// http://annevankesteren.nl/archives/2005/02/javascript-mime-type 
	// for a discussion. I picked text/javascript because that's
	// what we use in the script tag. Apache defaults to 
	// application/x-javascript
	respType.put("js", "text/javascript");
	respType.put("css", "text/css"); 
	respType.put("htm", "text/html"); 
	respType.put("html", "text/html");
	respType.put("wml", "text/wml");
	respType.put("txt", "text/plain");
	respType.put("xml", "text/xml");
	respType.put("jpeg", "image/jpeg");
	respType.put("jpe", "image/jpeg");
	respType.put("jpg", "image/jpeg");
	respType.put("png", "image/png");
	respType.put("tif", "image/tiff");
	respType.put("tiff", "image/tiff");
	respType.put("bmp", "image/bmp"); 
	respType.put("xbm", "image/xbm"); 
	respType.put("ico", "image/x-icon"); 
	respType.put("gif", "image/gif");
	respType.put("pdf", "application/pdf");
	respType.put("ps", "application/postscript");
	respType.put("mim", "application/mime"); 
	respType.put("mime", "application/mime"); 
	respType.put("mid", "application/midi"); 
	respType.put("midi", "application/midi"); 
	respType.put("wav", "audio/wav"); 
	respType.put("bwf", "audio/wav"); 
	respType.put("cpr", "image/cpr"); 
	respType.put("avi", "video/x-msvideo"); 
	respType.put("mpeg", "video/mpeg"); 
	respType.put("mpg", "video/mpeg"); 
	respType.put("mpm", "video/mpeg"); 
	respType.put("mpv", "video/mpeg"); 
	respType.put("mpa", "video/mpeg"); 
	respType.put("au", "audio/basic"); 
	respType.put("snd", "audio/basic"); 
	respType.put("ulw", "audio/basic"); 
	respType.put("aiff", "audio/x-aiff"); 
	respType.put("aif", "audio/x-aiff"); 
	respType.put("aifc", "audio/x-aiff"); 
	respType.put("cdda", "audio/x-aiff"); 
	respType.put("pict", "image/x-pict"); 
	respType.put("pic", "image/x-pict"); 
	respType.put("pct", "image/x-pict"); 
	respType.put("mov", "video/quicktime"); 
	respType.put("qt", "video/quicktime"); 
	respType.put("pdf", "application/pdf"); 
	respType.put("pdf", "application/pdf"); 
	respType.put("ssm", "application/smil"); 
	respType.put("rsml", "application/vnd.rn-rsml"); 
	respType.put("ra", "application/vnd.rn-realaudio"); 
	respType.put("rm", "application/vnd.rn-realmedia"); 
	respType.put("rv", "application/vnd.rn-realvideo"); 
	respType.put("rf", "application/vnd.rn-realflash"); 
	respType.put("rf", "application/vnd.rn-realflash"); 
	respType.put("asf", "application/x-ms-asf"); 
	respType.put("asx", "application/x-ms-asf"); 
	respType.put("wm", "application/x-ms-wm"); 
	respType.put("wma", "application/x-ms-wma"); 
	respType.put("wax", "application/x-ms-wax"); 
	respType.put("wmw", "application/x-ms-wmw"); 
	respType.put("wvx", "application/x-ms-wvx"); 
	respType.put("swf", "application/x-shockwave-flash"); 
	respType.put("spl", "application/futuresplash"); 
	respType.put("avi", "video/msvideo"); 
	respType.put("flc", "video/flc"); 
	respType.put("mp4", "video/mpeg4"); 
    }

    /**
     * This method handles the requests for the Theme files.
     * @param request The Servlet Request for the theme file
     * @param response The Servlet Response
     * @throws ServletException If the Servlet fails to serve the resource file
     * @throws IOException If the Servlet cannot locate and read a requested ThemeFile
     */
    protected void doGet(HttpServletRequest request,
			 HttpServletResponse response)
	throws ServletException, IOException {

        if(DEBUG) log("doGet()");
	String resourceName = request.getPathInfo();
	InputStream inStream = null;
	OutputStream outStream = null;
	try {
	    // Get InputStream
	    inStream = this.getClass().getResourceAsStream(resourceName);
	    if (inStream == null) {
		response.sendError(404, request.getRequestURI());
		return;
	    }
	    inStream = new BufferedInputStream(inStream, 4096);

            // Ask the container to resolve the MIME type if possible
            String type = getServletContext().getMimeType(resourceName);
            if (type == null) {
                // Otherwise, use our own hard coded list
                int lastDot = resourceName.lastIndexOf('.');
                if (lastDot != -1) {
                    String suffix = resourceName.substring(lastDot+1);
                    type = (String) respType.get(suffix.toLowerCase());
                }
            }
            // Set the content type of this response
            if (type != null) {
                response.setContentType(type);
            }

            // Set the timestamp of the response to enable caching
            response.setDateHeader("Last-Modified", getLastModified(request));

            // Get the OutputStream
	    outStream = response.getOutputStream();
	    outStream = new BufferedOutputStream(outStream, 4096);

	    int character;
	    while ((character = inStream.read()) != -1) {
		outStream.write(character);
	    }
	} catch(IOException ioex) {
	    //Log an error
	} finally {
	    try { inStream.close(); } catch(Throwable t) {}
	    try { outStream.close(); } catch(Throwable t) {}
	}
	return;
    }

    /**
     * Returns a short description of the servlet.
     * @return A String that names the Servlet
     */
    public String getServletInfo() {
	return "Theme Servlet for Sun Web Components";
    }

    public void init(ServletConfig config) throws ServletException {
        super.init(config);     
        ThemeFactory.initializeThemeManager(config.getServletContext(), 
                                            getLocales(config));
    }
    
    /** 
     * Determines the set of supported locales.
     * @return set containing the support locales.
     * @throws JspException Thrown if there is any exception encountered.
     */
    private Set getLocales(ServletConfig config) {
    
        if(DEBUG) log("getLocales()"); 
        
        String localesString = config.getInitParameter(localeParameter);
        if(localesString == null) { 
            return null;
        } 
        String[] localeArray = localesString.split(","); 
        Set locales = new HashSet(); 
        String language = null; 
        String country = null; 
        String variant = null; 
        String[] strings = null; 
        Locale locale = null; 
        String localeString = null; 
        
        for(int counter = 0; counter < localeArray.length; ++counter) { 
            localeString = localeArray[counter].trim();
            if(DEBUG) log(localeString); 
       
            if(localeString.length() ==0 ) { 
                continue; 
            } 
            strings = localeString.split("_"); 
            if(strings.length > 2) { 
                locale = new Locale(strings[0], strings[1], strings[2]); 
            } 
            else if (strings.length > 1) { 
                locale = new Locale(strings[0], strings[1]); 
            } 
            else if(strings.length > 0) { 
                if(DEBUG) log("language only " + strings[0]);
                locale = new Locale(strings[0]); 
            } 
            if(DEBUG) log("\tNew locale is " + locale.toString());
            locales.add(locale);
        }        
        return locales;
    }  


    /**
     * <p>The "last modified" timestamp we should broadcast for all resources
     * provided by this servlet.  This will enable browsers that cache static
     * resources to send an "If-Modified-Since" header, which will allow us to
     * return a "Not Modified" response.</p>
     */
    private long lastModified = (new Date()).getTime();


    /**
     * <p>Return the timestamp for when resources provided by this servlet
     * were last modified.  By default, this will be the timestamp when this
     * servlet was first loaded at the deployment of the containing webapp,
     * so that any changes in the resources will be automatically sent to
     * the clients who might have cached earlier versions.</p>
     */
    public long getLastModified(HttpServletRequest request) {
        return this.lastModified;
    }


}
