/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xsl.transform;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import org.netbeans.api.xml.cookies.CookieMessage;
import org.netbeans.api.xml.cookies.CookieObserver;
import org.netbeans.api.xml.cookies.TransformableCookie;
import org.netbeans.api.xml.cookies.XMLProcessorDetail;
import org.netbeans.modules.xsl.utils.TransformUtil;
import org.netbeans.spi.xml.cookies.DefaultXMLProcessorDetail;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.filesystems.URLMapper;
import org.xml.sax.SAXParseException;

/**
 *
 * @author  Libor Kramolis
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
            String mimeType;
            if (guessOutputExt.equals("txt")) { // NOI18N
                mimeType = "text/plain"; // NOI18N
            } else if (guessOutputExt.equals("xml")) { // NOI18N
                mimeType = "text/xml"; // NOI18N
            } else if (guessOutputExt.equals("html")) { // NOI18N
                mimeType = "text/html"; // NOI18N
            } else {
                mimeType = null;
            }

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
            FileObject fo = URLMapper.findFileObject(url);
            if (fo != null) {
                name = TransformUtil.getURLName (fo);
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
