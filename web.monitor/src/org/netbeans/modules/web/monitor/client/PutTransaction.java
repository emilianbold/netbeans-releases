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

package org.netbeans.modules.web.monitor.client; 

import java.io.*;
import java.text.*;
import java.util.Enumeration;
import java.util.StringTokenizer;
import javax.servlet.*;
import javax.servlet.http.*;
import org.netbeans.modules.web.monitor.server.Constants;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileLock;

/*
 * Put a transaction
 */
  
public class PutTransaction extends HttpServlet {


    private static FileObject currDir = null;
    private static boolean debug = false;
     
    private ServletConfig servletConfig = null;
    

    public void doPost(HttpServletRequest req, HttpServletResponse res) 
	throws ServletException, IOException {

	if(debug) log("PutTransaction::doPost"); //NOI18N
	if(currDir == null) {
	    try { 
		currDir = Controller.getCurrDir();
	    }
	    catch(FileNotFoundException ex) {
		// PENDING report this error properly
		if(debug) log("Couldn't write the transaction data");  //NOI18N
		return;
	    }
	}

	// As soon as you get the parameters, you've gotten an input
	// string for this. Don't do that. 

	String id = req.getQueryString(); 

	try {
	    if(debug) log("Trying to add the transaction"); //NOI18N

	    // PENDING: the id is parsed in TransactionNode. Should
	    // *not* do that twice - may be it should be parsed here. 
	    String name = 
		id.substring(0, id.indexOf(Constants.Punctuation.itemSep));
		
	    FileObject fo = currDir.createData(name, "xml"); //NOI18N
	    FileLock lock = fo.lock();
	    PrintWriter fout = new PrintWriter(fo.getOutputStream(lock));
	    
	    if(debug) log("Reading buffer"); //NOI18N

	    InputStreamReader isr = 
		new InputStreamReader(req.getInputStream());

	    char[] charBuf = new char[4096];
	    int numChars;
	     
	    while((numChars = isr.read(charBuf, 0, 4096)) != -1) {
		fout.write(charBuf, 0, numChars);
		if(debug) log(new String(charBuf));
	    }
	    
	    isr.close();
	    fout.close();
	    lock.releaseLock();
	     
	    if(debug) log("Done reading"); //NOI18N

	    try {
		MonitorAction.getController().addTransaction(id); 
	    }
	    catch(Exception ex) {
		log("Couldn't add the transaction");  //NOI18N
		if (debug) 
		   log("MonitorAction.getController(): " +  //NOI18N
		       MonitorAction.getController());
		if (debug) log(ex); 
	    }
	    
	    if(debug) log("...success"); //NOI18N

	    res.setContentType("text/plain");  //NOI18N
	    
	    PrintWriter out = res.getWriter();
	    out.println(Constants.Comm.ACK); 
	}
	catch (Exception e) { 
	    if(debug) log(e); 
	}
    }

    // PENDING - deal better with this
    public void doGet(HttpServletRequest req, HttpServletResponse res) 
	throws ServletException, IOException {

	if(debug) log("PutTransaction::doGet");  //NOI18N

	PrintWriter out = res.getWriter();
	try { 
	    //out.println(id); 
	    out.println("Shouldn't use GET for this!");  //NOI18N
	}
	catch (Exception e) { 
	    System.out.println(e.getMessage());
	}
	try { out.close(); } catch(Exception ex) {}
    }


    /**
     * Init method for this filter 
     *
     */
    public void init(ServletConfig servletConfig) { 

	this.servletConfig = servletConfig;
	if(debug) log("PutTransaction::init");  //NOI18N
    }
    
    public void log(String msg) {
	//servletConfig.getServletContext().log(msg); 
	System.err.println(msg);
	
    }

    public void log(Throwable t) {
	//servletConfig.getServletContext().log(getStackTrace(t)); 
	System.err.println(getStackTrace(t));
    }


    public static String getStackTrace(Throwable t) {

	String stackTrace = null;
	    
	try {
	    StringWriter sw = new StringWriter();
	    PrintWriter pw = new PrintWriter(sw);
	    t.printStackTrace(pw);
	    pw.close();
	    sw.close();
	    stackTrace = sw.getBuffer().toString();
	}
	catch(Exception ex) {}
	return stackTrace;
    }

} //PutTransaction.java



