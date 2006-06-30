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

package org.netbeans.test.gui.web.extbrowser.using;

import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.ExplorerOperator;
import org.netbeans.jellytools.OptionsOperator;
import org.netbeans.jellytools.NbDialogOperator;

import org.netbeans.jellytools.properties.ComboBoxProperty;
import org.netbeans.jellytools.properties.PropertySheetTabOperator;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jellytools.properties.editors.FileCustomEditorOperator;
import org.netbeans.jellytools.actions.Action;



import org.netbeans.test.gui.web.util.BrowserUtils;

import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTextComponentOperator;
import org.netbeans.junit.NbTestSuite;
import java.io.File;

public class Using extends JellyTestCase {
    private static String fSep = System.getProperty("file.separator");
    private static String iSep = "|";
   

    public Using(java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
         
    //method required by JUnit
    public static junit.framework.Test suite() {
	if(System.getProperty("os.name").indexOf("Windows")!=-1) { //Add WINDOWS tests
	    
        }else { //Add UNIX tests
	    
	}
	return new NbTestSuite(Using.class);
    }

    /**
       Using : External (Command Line) : Internet Explorer 5.x in Path 
     **/
    public void testEBCLIeInPath() {
	String ie = BrowserUtils.getIEInPath();
	if(ie == null) {
	    fail("Internet Explorer not found in path");
	}
	BrowserUtils.setCLBrowser();
	BrowserUtils.setCLBrowserCommand(ie);
	view();
    }
    /**
       Using : External (Command Line) : Internet Explorer 5.x FullPath 
     **/
    public void testEBCLIeFullPath() {
	String iefp = BrowserUtils.getIEFullPath();
	if(iefp == null) {
	    fail("Internet Explorer not found. See output for details.");
	}
	BrowserUtils.setCLBrowser();
	BrowserUtils.setCLBrowserCommand(iefp);
	view();
    }

    /**
       Using: External (Command Line) : Netscape Navigator 4.7x in Path
     **/

    public void testEBCLNetscapeInPath() {
	String ns = BrowserUtils.getNetscapeInPath();
	if(ns == null) {
	    fail("Netscape not found in path");
	}
	BrowserUtils.setCLBrowser();
	BrowserUtils.setCLBrowserCommand(ns);
	view();
    }

    /**
       Using: External (Command Line) : Netscape Navigator 4.7x FullPath
     **/

    public void testEBCLNetscapeFullPath() {
	String nsfp = BrowserUtils.getNetscapeFullPath();
	if(nsfp == null) {
	    fail("Netscape not found. See output for details.");
	}
	BrowserUtils.setCLBrowser();
	BrowserUtils.setCLBrowserCommand(nsfp);
	view();
    }

    /**
       Using: External (Command Line) : Netscape 6.x in Path
     **/

    public void testEBCLNetscape6InPath() {
	String ns = BrowserUtils.getNetscape6InPath();
	if(ns == null) {
	    fail("Netscape6 not found in path.");
	}
	BrowserUtils.setCLBrowser();
	BrowserUtils.setCLBrowserCommand(ns);
	System.out.println("before view");
	view();
	System.out.println("after view");
	if(BrowserUtils.handleErrorInCLBrowser()) {
	    fail("Problems in starting Netscape6 in path.");
	}
	System.out.println("ended test");
    }

    /**
       Using: External (Command Line) : Netscape 6.x FullPath
     **/

    public void testEBCLNetscape6FullPath() {
	String nsfp = BrowserUtils.getNetscape6FullPath();
	if(nsfp == null) {
	    fail("Netscape6 not found. See output for details.");
	}
	BrowserUtils.setCLBrowser();
	BrowserUtils.setCLBrowserCommand(nsfp);
	view();
    }

    /****
	 End of EBCL section 
     ****/
    /****
	 External Browser(Unix) section
     ****/


    /**
       Using: External (Unix) : Netscape in Path
     **/

    public void testEBUNetscapeInPath() {
	String ns = BrowserUtils.getNetscapeInPath();
	if(ns == null) {
	    fail("Netscape not found.");
	}
	BrowserUtils.setExternalUnixBrowser();
	BrowserUtils.setEBUBrowserCommand(ns);
	view();
    }

    /**
       Using: External (Unix) : Netscape FullPath
     **/

    public void testEBUNetscapeFullPath() {
	String nsfp = BrowserUtils.getNetscapeFullPath();
	if(nsfp == null) {
	    fail("Netscape not found. See output for details.");
	}
	BrowserUtils.setExternalUnixBrowser();
	BrowserUtils.setEBUBrowserCommand(nsfp);
	view();
    }

   
    /****
	 End of EBU section 
    ****/


    /****
	 External Browser(Windows) section
     ****/


    /**
       Using: External (Windows) : Netscape
     **/

    public void testEBWNetscape() {
	BrowserUtils.setExternalWinBrowser();
	BrowserUtils.setDDEServerNetscape();
	view();
    }

    /**
       Using: External (Windows) : Netscape6
     **/

    public void testEBWNetscape6() {
	BrowserUtils.setExternalWinBrowser();
	BrowserUtils.setDDEServerNetscape6();
	view();
    }


    /**
       Using: External (Windows) : Internet Explorer
     **/

    public void testEBWExplorer() {
	BrowserUtils.setExternalWinBrowser();
	BrowserUtils.setDDEServerExplorer();
	view();
    }


   
    /****
	 End of EBU section 
    ****/




    /**must be last test in this suite. See bug #
       Using: Swing HTML browser
     **/

    public void testSwingBrowser() {
	BrowserUtils.setSwingBrowser();
	view();
    }
    


    /**
       Private methods
     **/
    public void view() {
	String menuPath = "View|Web Browser"; //NOI18N
	new Action(menuPath,null).performMenu();
    }
}








