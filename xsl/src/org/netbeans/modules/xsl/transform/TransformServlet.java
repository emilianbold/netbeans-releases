/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xsl.transform;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.xml.sax.SAXParseException;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;

import org.openide.util.SharedClassObject;
import org.openide.util.HttpServer;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;

import org.netbeans.api.xml.cookies.*;
import org.netbeans.spi.xml.cookies.*;

import org.netbeans.modules.xsl.utils.TransformUtil;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public class TransformServlet extends HttpServlet {
    private static final long serialVersionUID = 1632869007241230624L;    

    private static TransformableCookie transformable;
    /** Last cached XML Source. */
    private static Source xmlSource;
    /** Last cached XSL Script. */
    private static Source xslSource;
    
    
    public static void prepare (TransformableCookie trans, Source xml, Source xsl) {
        transformable = trans;
        xmlSource = xml;
        xslSource = xsl;
    }

    /** Initializes the servlet.
     */
    public void init (ServletConfig config) throws ServletException {
        super.init (config);
    }
    
    /** Destroys the servlet.
     */
    public void destroy () {
        xmlSource = null;
        xslSource = null;
    }
    
    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        Result outputResult = new StreamResult (out);

        Observer notifier = new Observer();
        try {
            String guessOutputExt = TransformUtil.guessOutputExt (xslSource);
            String mimeType = FileUtil.getMIMEType (guessOutputExt);

            if ( mimeType != null ) {
                response.setContentType (mimeType);
            }

            if ( Util.THIS.isLoggable() ) /* then */ {
                Util.THIS.debug ("[TransformServlet] Response MIME Type: '" + mimeType + "'");
                Util.THIS.debug ("    xmlSource.getSystemId() = " + xmlSource.getSystemId());
                Util.THIS.debug ("    transformable = " + transformable);
                Util.THIS.debug ("    xslSource.getSystemId() = " + xslSource.getSystemId());
            }

            TransformUtil.transform (xmlSource, transformable, xslSource, outputResult, notifier);
        } catch (Exception exc) {
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("    EXCEPTION!!!: " + exc.getClass().getName(), exc);

            // thrown if error in style sheet
            CookieMessage message = null;
            
            if ( exc instanceof TransformerException ) {
                // do not log again TransformerException, it is already done by ErrorListener
            } else if ( exc instanceof SAXParseException ) {
                message = new CookieMessage
                    (TransformUtil.unwrapException (exc).getLocalizedMessage(), 
                     CookieMessage.FATAL_ERROR_LEVEL,
                     new DefaultXMLProcessorDetail ((SAXParseException) exc)
                     );            
            } else {
                message = new CookieMessage
                    (exc.getLocalizedMessage(), 
                     CookieMessage.FATAL_ERROR_LEVEL
                     );
            }

            if ( Util.THIS.isLoggable() ) /* then */ {
                Util.THIS.debug ("    message  = " + message);
                Util.THIS.debug ("    notifier = " + notifier);
            }

            if ( message != null ) {
                notifier.receive (message);
            }

            // create warning page
            response.setContentType ("text/html");

            out.println ("<html><head>");
            out.println ("    <title>" + Util.THIS.getString ("MSG_error_html_title") + "</title>");
            out.println ("    <style>" + Util.THIS.getString ("MSG_error_html_style") + "</style>");
            out.println ("</head><body>");
            out.println ("    <h2>" + Util.THIS.getString ("MSG_error_page_title") + "</h2>");
            out.println ("    <p>" + Util.THIS.getString ("MSG_error_page_message") + "</p>");
            out.println ("    <hr size=\"1\" noshade=\"\" />\n" + generateReport (notifier.getList()) + "<hr size=\"1\" noshade=\"\" />");
            out.println ("    <p>" + Util.THIS.getString ("MSG_error_bottom_message") + "</p>");
            out.println ("</body></html>");
        } finally {
            out.close();
        }
    }
    
    /** Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doGet (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest (request, response);
    }
    
    /** Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doPost (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest (request, response);
    }
    
    /** Returns a short description of the servlet.
     */
    public String getServletInfo () {
        return "XSL Transformation Preview Servlet";
    }

    public static URL getServletURL () throws MalformedURLException, UnknownHostException {
        
        URL base = getSampleHTTPServerURL();
        // XXX hack: assume that the path /servlet/CLASSNAME works on this server.
        URL root = new URL (base.getProtocol(), base.getHost(), base.getPort(), "/servlet/" + TransformServlet.class.getName() + "/");
        
        return root;
    }

    private static URL getSampleHTTPServerURL() {
        FileSystem fs = Repository.getDefault().getDefaultFileSystem();
	    FileObject fo = fs.findResource("HTTPServer_DUMMY");
	    if (fo == null) {
	        return null;
	    }
	    URL u = URLMapper.findURL(fo, URLMapper.NETWORK);
	    return u;
    }

    private String generateReport (List msgList) {
        StringBuffer sb = new StringBuffer();

        try {

        Iterator it = msgList.iterator();
        while ( it.hasNext() ) {
            CookieMessage msg = (CookieMessage) it.next();
            XMLProcessorDetail detail = (XMLProcessorDetail) msg.getDetail (XMLProcessorDetail.class);

            // Message
            sb.append ("    &nbsp;&nbsp;&nbsp;&nbsp;<font class=\"").append (levelName (msg.getLevel())).append ("\">").append (msg.getMessage()).append ("</font>"); // NOI18N

            if ( detail != null ) {
                // SystemId
                String systemId = preferFileName (detail.getSystemId());
                if ( systemId != null ) {
                    sb.append ("&nbsp;(<font class=\"system-id\">");
                    boolean isFile = systemId.startsWith ("file:");
                    if ( isFile ) {
                        sb.append ("<a href=\"").append (systemId).append ("\">");
                    }
                    sb.append (systemId);
                    if ( isFile ) {
                        sb.append ("</a>");
                    }
                    sb.append ("</font>\n"); // NOI18N
                    // LineNumber
                    sb.append ("&nbsp;[<font class=\"line-number\">").append (detail.getLineNumber()).append ("</font>])<br>"); // NOI18N
                }
            }
        }

        } catch (Exception exc) {
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug (exc);
        }

        return sb.toString();
    }


    private String preferFileName (String systemId) {
        String name = systemId;

        try {
            URL url = new URL (systemId);
            FileObject[] fos = URLMapper.findFileObjects (url);
            if ( fos.length > 0 ) {
                name = TransformUtil.getURLName (fos[0]);
            }
        } catch (Exception exc) {
            // ignore it -> use systemId

            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug (exc);
        }

        return name;
    }

    private String levelName (int level) {
        if ( level == CookieMessage.FATAL_ERROR_LEVEL ) {
            return "fatal-error"; // NOI18N
        } else if ( level == CookieMessage.ERROR_LEVEL ) {
            return "error"; // NOI18N
        } else if ( level == CookieMessage.WARNING_LEVEL ) {
            return "warning"; // NOI18N
        } else { // CookieMessage.INFORMATIONAL_LEVEL 
            return "informational"; // NOI18N
        }
    }


    //
    // class Observer
    //

    private static class Observer implements CookieObserver {

        private final List msgList;
        
        public Observer () {
            msgList = new Vector();
        }

        public void receive (CookieMessage msg) {
            msgList.add (msg);
        }

        public List getList () {
            return msgList;
        }
    }
}
