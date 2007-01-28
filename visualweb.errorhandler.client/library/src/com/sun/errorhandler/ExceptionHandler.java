/*
 * {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */
/*
 * ExceptionHandler.java
 * Created on October 10, 2003, 2:06 PM
 */

package com.sun.errorhandler;

import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.lang.StackTraceElement;
import java.net.URL;

import com.sun.errorhandler.DebugClientThread;
import java.util.ResourceBundle ;
import java.util.Locale ;


/**
 * @author  Winston Prakash
 *   jfbrown for the debugging control and lovely l10n-ization.
 */
public class ExceptionHandler extends HttpServlet {
    
    private static ResourceBundle rb = ResourceBundle.getBundle("com.sun.errorhandler.Bundle", // NOI18N
        Locale.getDefault());    
    
    private static int debugLevel = 0 ;
    public static int getDebugLevel() { 
        return debugLevel ; 
    }
    
    public void init(javax.servlet.ServletConfig sc) {
        try {
            super.init(sc) ;
        }
        catch (Exception ex) {
            ex.printStackTrace() ;
        }
    }
    
    private boolean readPropertiesAlready = false ;
    public void initProps() {
        if (! readPropertiesAlready) {
            readPropertiesAlready = true ;
            
            String errH = this.getInitParameter("errorHost" ) ; // NOI18N
            if ( errH != null ) {
                DebugClientThread.errorHost = errH ; //NOI18N
                String errP = this.getInitParameter("errorPort") ; // NOI18N
                if (errP != null ) {
                    DebugClientThread.setErrorPort( errP ) ;
                }
                else {
                    DebugClientThread.setErrorPort( "0" ) ; // NOI18N
                }
            }
        }
    } 
    
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        initProps() ;
        generateResponse(request, response);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        initProps() ;
        // only test for params in the doGet().
        if ( request.getParameter("debug") != null  //NOI18N
                || request.getParameter("errorHost") != null   //NOI18N
                || request.getParameter("errorPort") != null ) {  //NOI18N
            setDebugVars(request) ; 
        }
        generateResponse(request, response);
    }

    
    public void generateResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Throwable exception = (Throwable) request.getAttribute("javax.servlet.error.exception");
        String request_uri = (String) request.getAttribute("javax.servlet.error.request_uri");

        // Specify the content type is HTML
        response.setContentType("text/html;charset=UTF-8");  // NOI18N
        PrintWriter out = response.getWriter();

        // Generate the HTML response
        out.println("<HTML>");  // NOI18N
        out.println("<HEAD>");  // NOI18N
        out.println("<TITLE>") ;  // NOI18N
        out.println( rb.getString("EH_pagetitle") ) ; // NOI18N
        out.println("</TITLE>");  // NOI18N
        out.print("<style>I{font-family:") ; // NOI18N
        out.print( "Verdana,UTF-8" ) ; //TODO - needs to be for the project!  // NOI18N
        out.println(";font-weight:italic;font-size:8pt;color:maroon}</style>"); // NOI18N
        out.println("</HEAD>");  // NOI18N
        out.println("<BODY BGCOLOR='white'>");  // NOI18N
        out.println("<CENTER><B>") ;  // NOI18N
        out.println( rb.getString("EH_pagetitle") ) ; // NOI18N
        out.println("</B></CENTER>");  // NOI18N
        out.println("<P>");  // NOI18N
        out.println("<FONT COLOR='blue'>");  // NOI18N

        out.println("<BR><B>") ;  // NOI18N
        out.println( rb.getString("EH_Description") ) ;  // NOI18N
        out.println("</B> ") ;  // NOI18N
        out.println( rb.getString("EH_Reason") ) ;  // NOI18N
        out.println("<BR>" );  // NOI18N

        // output the causes in reverse order
        ArrayList eList = new ArrayList() ;
        while (exception != null ) {
            eList.add(exception) ;
            exception = exception.getCause() ;
        }
        for ( int ecnt = eList.size()-1 ; ecnt >= 0 ; ecnt-- ) {
            displayMessage( (Throwable)eList.get(ecnt),out);
        }

        out.println("</FONT>");  // NOI18N
        out.println("</P>");  // NOI18N
        if (getDebugLevel() > 0 ) { 
            out.println("<HR><P>Debugging on.</P>") ;
            out.println("<P>Errors will be sent to "+DebugClientThread.errorHost+" port "
                    + DebugClientThread.getErrorPort() + "</P>") ;
        }
        out.println("</BODY>");  // NOI18N
        out.println("</HTML>");  // NOI18N
        out.close();
    }

    private void displayMessage(Throwable exception, PrintWriter out) {
        String expTypeFullName  = exception.getClass().getName();
        String expTypeName = expTypeFullName.substring(expTypeFullName.lastIndexOf(".")+1);
        StackTraceElement[] ste = exception.getStackTrace();

        // Browse through the stack trace and find the first trace
        // that has the file name (non-null)

        int stackCount = -1;
        String fileName = null;
        for (int i = 0; i < ste.length; i++) {
            fileName = ste[i].getFileName();
            if (fileName != null) {
                // Check if the java file actually exists
                String className = ste[i].getClassName();
                try{
                    Class clazz = Class.forName(className);
                    URL url = clazz.getResource(fileName);
                    InputStream is1 = url.openStream();
                    if (is1 != null) {
                        is1.close();
                        stackCount = i;
                        break;
                    }
                }
                catch (Exception ex) {
                }
                finally{
                }
            }
        }

        out.println("<BR><B>" ) ; // NOI18N
        out.println( rb.getString("EH_ExceptionDetails") ) ; // NOI18N
        out.println("</B> " ) ; // NOI18N
        out.println( expTypeFullName ) ;
        out.println( "<BR>" ) ; // NOI18N
        String xmsg = exception.getLocalizedMessage() ;
        if ( xmsg == null ) xmsg = exception.getMessage() ;
        out.println("&nbsp;&nbsp;" + xmsg + "<BR>"); // NOI18N

        out.println("<BR><B>" ) ; // NOI18N
        out.println( rb.getString("EH_PossibleSource")) ; // NOI18N
        out.println("</B><BR>"); // NOI18N

        out.println("&nbsp;&nbsp;" ) ; // NOI18N
        out.println(rb.getString("EH_ClassName")) ; // NOI18N
        out.println( " <I>" + ( ste.length < 1 ? rb.getString("EH_unknownValue") : ste[0].getClassName() ) + "</I> <BR>"); // NOI18N
        
        out.println("&nbsp;&nbsp;" ) ; // NOI18N
        out.println(rb.getString("EH_FileName")) ; // NOI18N
        out.println( " <I>" + ( ste.length < 1 ? rb.getString("EH_unknownValue") : ste[0].getFileName() ) + "</I> <BR>"); // NOI18N
        
        out.println("&nbsp;&nbsp;" ) ; // NOI18N
        out.println(rb.getString("EH_MethodName")) ; // NOI18N
        out.println( " <I>" + ( ste.length < 1 ? rb.getString("EH_unknownValue") : ste[0].getMethodName() ) + "</I> <BR>"); // NOI18N
        
        out.println("&nbsp;&nbsp;" ) ; // NOI18N
        out.println(rb.getString("EH_LineNumber")) ; // NOI18N
        out.println( " <I>" + ( ste.length < 1 ? rb.getString("EH_unknownValue") : Integer.toString((ste[0].getLineNumber()) ) )+ "</I> <BR>"); // NOI18N
        out.println("<BR>"); // NOI18N

        if (stackCount != -1) {
            int lineNumber = ste[stackCount].getLineNumber();

            String className = ste[stackCount].getClassName();
            fileName = ste[stackCount].getFileName();
            try {
                Class clazz = Class.forName(className);
                URL url = clazz.getResource(fileName);
                if (url != null) {
                    InputStream is = url.openStream();
                    LineNumberReader reader = new LineNumberReader(new InputStreamReader(is));
                    out.println("<table width=100% bgcolor=\"#ffffcc\"><tr><td><code>");  // NOI18N
                    for (int i = 0; i < lineNumber+3; i++) {
                        String line = reader.readLine();
                        int lineNo = reader.getLineNumber();
                        if (lineNo >= lineNumber-3) {
                            if (lineNo == lineNumber) {
                                out.println("<FONT COLOR='red' BGCOLOR='white'><B>");  // NOI18N
                                out.println(lineNo + ": " + appendHTMLChar(line) + "<BR>");  // NOI18N
                                out.println("</B></FONT>");  // NOI18N
                            } else {
                                out.println(lineNo + ": " + appendHTMLChar(line) + "<BR>");  // NOI18N
                            }
                        }
                    }
                    out.println("</code></td></tr></table>");  // NOI18N
                    is.close();
                    reader.close();
                }
            }
            catch (Exception ex) {
            }
        } else {
            out.println("<table width=100% bgcolor=\"#ffffcc\"><tr><td><code>");  // NOI18N
            out.println( rb.getString("EH_NoSourceLong"));  // NOI18N
            out.println("<BR></code></td></tr></table>"); // NOI18N
        }

        out.println("<BR><B>" ) ; // NOI18N
        out.println(rb.getString("EH_StackTrace")) ; // NOI18N
        out.println(" </B><BR><BR>"); // NOI18N
        out.println("<table width=100% bgcolor=\"#eeddff\"><tr><td><code>");  // NOI18N
        for (int i = 0; i < ste.length; i++ ) {
            int lineNumber = ste[i].getLineNumber();
            String cName = ste[i].getClassName();
            String fName = ste[i].getFileName();
            String mName = ste[i].getMethodName();
            if (i == stackCount)
                out.println("<FONT COLOR='brown' BGCOLOR='white'><B>");  // NOI18N
            if (fName != null) {
                out.println(cName + "." + mName + "(" + fName + ":" + lineNumber +")<BR>");  // NOI18N
            } else {
                out.println(cName + "." + mName + "(" ) ;
                out.println( rb.getString("EH_NoSourceShort")) ;  // NOI18N
                out.println(")<BR>");  // NOI18N
            }
            if (i == stackCount)
                out.println("</B></FONT>");  // NOI18N
        }
        out.println("</code></td></tr></table>");  // NOI18N

        if (stackCount == -1)
            stackCount = 0;
        if ( DebugClientThread.errorHost != null && ste.length > 0) {
            DebugClientThread clientThread = new DebugClientThread();
            DebugProtocol debugprotocol = new DebugProtocol();
            clientThread.start();
            if (clientThread.testConnected()) {
                clientThread.sendMessage(debugprotocol.DEBUG_CLIENT_ID + debugprotocol.DEBUG_DELIMITER + debugprotocol.DEBUG_CLIENT_NAME);
                clientThread.sendMessage(debugprotocol.DEBUG_REQUEST_START);
                clientThread.sendMessage(debugprotocol.DEBUG_CLASS_NAME + debugprotocol.DEBUG_DELIMITER + ste[stackCount].getClassName());
                clientThread.sendMessage(debugprotocol.DEBUG_FILE_NAME + debugprotocol.DEBUG_DELIMITER + ste[stackCount].getFileName());
                clientThread.sendMessage(debugprotocol.DEBUG_METHOD_NAME + debugprotocol.DEBUG_DELIMITER + ste[stackCount].getMethodName());
                clientThread.sendMessage(debugprotocol.DEBUG_LINE_NUMBER + debugprotocol.DEBUG_DELIMITER + ste[stackCount].getLineNumber());
                clientThread.sendMessage(debugprotocol.DEBUG_REQUEST_END);
                clientThread.disconnect();
            }
        }
    }

    /** Append a character to a StringBuffer intended for HTML
     * display - it will escape <, >, etc. such that the char is
     * shown properly in HTML.
     */
    public String appendHTMLChar(String str) {
        StringBuffer sb = new StringBuffer();
        int len = str.length();
        for (int i = 0; i < len; i++) {
            char chr =  str.charAt(i);
            switch (chr) {
                case '<': sb.append("&lt;"); break; // NOI18N
                case '>': sb.append("&gt;"); break; // NOI18N
                case '&': sb.append("&amp;"); break; // NOI18N
                case '"': sb.append("&quot;"); break; // NOI18N
                case ' ': sb.append("&nbsp;"); break; // NOI18N
                case '\n': sb.append("<br>"); break; // NOI18N
                default: sb.append(chr);
            }
        }
        return sb.toString();
    }

    /****
     * HACK - sets debug variables.
     * possible HttpServletRequest paramaters:
     * debug = turn on/off misc logging statements to app server log
     * errorHost = for sending exceptions back to Creator, this is the hostname.
     * errorPort = for sending exceptions back to Creator, this is the port.
     */
    private void setDebugVars(HttpServletRequest request) {
        String val = request.getParameter("debug") ;  //NOI18N
        if ( val != null ) {
            val = val.trim() ;
            if ("1".equals(val)  //NOI18N
                    || "true".equalsIgnoreCase(val) //NOI18N
                    || "yes".equalsIgnoreCase(val))  //NOI18N
                debugLevel = 1 ; 
            else debugLevel = 0 ;
        }
        
        // HACK:  allow setting of errorHost and errorPort too.
        val = request.getParameter("errorHost") ;  //NOI18N
        if ( val != null ) {
            if ( !"".equals(val)) DebugClientThread.errorHost = null ;  //NOI18N
            else DebugClientThread.errorHost = val ;
        }
        val = request.getParameter("errorPort") ;  //NOI18N
        if ( val != null ) {
            DebugClientThread.setErrorPort(val) ;
        }
    }
}
