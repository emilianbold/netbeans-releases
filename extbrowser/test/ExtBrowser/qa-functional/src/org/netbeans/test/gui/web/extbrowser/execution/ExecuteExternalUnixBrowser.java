/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.test.gui.web.extbrowser.execution;

import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.ExplorerOperator;




import org.netbeans.jellytools.nodes.FolderNode;
import org.netbeans.jellytools.nodes.JavaNode;
import org.netbeans.jellytools.nodes.HTMLNode;


import org.netbeans.jellytools.actions.ExecuteAction;

import org.netbeans.junit.NbTestSuite;


import org.netbeans.jemmy.Timeouts;
import org.netbeans.jemmy.Waiter;

import org.netbeans.web.test.nodes.JSPNode;
import org.netbeans.web.test.nodes.ServletNode;
import org.netbeans.web.test.util.Utils;
import org.netbeans.test.gui.web.util.JSPServletResponseWaitable;
import org.netbeans.test.gui.web.util.HttpRequestWaitable;
import org.netbeans.test.gui.web.util.BrowserUtils;

public class ExecuteExternalUnixBrowser extends JellyTestCase {
    private static String workDir = null;     
    private static String webModule = null;
    private static String wmName = "wm1";
    private static String fSep = System.getProperty("file.separator");
    private static String iSep = "|";
    private static Timeouts tm = null;
    private static boolean first = true;
    private static String classes = "Classes";
    private static String servletForExecution = "ServletForExecution";
    private static String htmlForExecution = null;;
    private static String wmForExecution = "wmForExecution"; 
    private static String urlToRedirectFromHTML = "RedirectFromHtmlForExecution.html";
    private static String jspForExecution = null;
    private static String pkg = "execution";
    private static ExplorerOperator explorer = null;
    private String servletId = "cebde3e2-e8f1-4421-8a1c-df11dcc6e79a";
    private String jspId     = "c78eae2b-39f2-4b41-b2be-032e5373d7f4";
    private String wmId      = "9bc4ac0b-0a21-452a-9e51-ca9df3c2fa04";
    private int defaultPort = 1357;
    private int port = 2468;
    private String defaultAnswer = "HTTP/1.0 200 OK\nServer: FFJ Automated Tests SimpleServ\nLast-Modified: Fri, 12 Jul 2002 09:53:56 GMT\nContent-Length: 281\nConnection: close\nContent-Type: text/html\n\n<html>\n<head>\n   <title>Tests passed</title>\n</head>\n<body>\n<center><H1>Request Accepted</H1></center>\n</body>\n</html>";
    


    public ExecuteExternalUnixBrowser(java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
         
    //method required by JUnit
    public static junit.framework.Test suite() {
	workDir = System.getProperty("extbrowser.workdir").replace('/', fSep.charAt(0));
	webModule = workDir + fSep + wmName;
	htmlForExecution = webModule + iSep + "html" + iSep + "HtmlFileForExecution";
	jspForExecution = webModule + iSep + "jsp" + iSep + "JSPForExecution";
	pkg = webModule + iSep + "WEB-INF" + iSep + classes + iSep + pkg;
	String wmc = System.getProperty("extbrowser.mountcount");
	int count = 0;
	if(wmc != null) {
	    count = new Integer(wmc).intValue();
	}
	if(first) {
	    while(count >0) {
		Utils.handleDialogAfterNewWebModule();
		count--;
	    }
	    first = false;
	}
	tm = new Timeouts();
	tm.initTimeout("Waiter.WaitingTime", 300000); //5 minutes

	BrowserUtils.setExternalUnixBrowser();
	return new NbTestSuite(ExecuteExternalUnixBrowser.class);
    }

    public void testExecuteHtml() {
	HTMLNode node1 = null;
	
	try {
	    node1 = new HTMLNode(htmlForExecution);
	}catch(Exception e) {
	    fail("Not found: " + htmlForExecution);
	}
	new ExecuteAction().perform(node1);
	HttpRequestWaitable hrw = new HttpRequestWaitable(urlToRedirectFromHTML, defaultAnswer, defaultPort);
	Waiter w = new Waiter(hrw);
	w.setTimeouts(tm);
	try {
	    w.waitAction(hrw);
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Looks like browser not started or URL not loaded");
	} 
    }

    public void testExecuteSevlet() {
	ServletNode node1 = null;
	try {
	    node1 = new ServletNode(pkg + iSep + servletForExecution);
	}catch(Exception e) {
	    fail("Not found: " + servletForExecution);
	}
	node1.execute();
	JSPServletResponseWaitable jsrw = new JSPServletResponseWaitable(servletId, defaultAnswer, port);
	Waiter w = new Waiter(jsrw);
	w.setTimeouts(tm);
	try {
	    w.waitAction(jsrw);
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Looks like browser not started or URL not loaded");
	}
    }

    public void testExecuteJSP() {
	JSPNode node1 = null;
	try {
	    node1 = new JSPNode(jspForExecution);
	}catch(Exception e) {
	    fail("Not found: " + jspForExecution);
	}
	node1.execute();
	JSPServletResponseWaitable jsrw = new JSPServletResponseWaitable(jspId, defaultAnswer, port);
	Waiter w = new Waiter(jsrw);
	w.setTimeouts(tm);
	try {
	    w.waitAction(jsrw);
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Looks like browser not started or URL not loaded");
	}
    }

    public void testExecuteWebModule() {
	FolderNode node1 = null;
	try {
	    node1 = new FolderNode(workDir + fSep + wmForExecution + iSep + "WEB-INF");
	}catch(Exception e) {
	    fail("Web Module for execution not found");
	}
	new ExecuteAction().perform(node1);
	JSPServletResponseWaitable jsrw = new JSPServletResponseWaitable(wmId, defaultAnswer, port);
	Waiter w = new Waiter(jsrw);
	w.setTimeouts(tm);
	try {
	    w.waitAction(jsrw);
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Looks like browser not started or URL not loaded");
	}
    }
    
   
}
