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
	BrowserUtils.setCLBrowser();
	BrowserUtils.setCLBrowserCommand(ie);
	view();
    }
    /**
       Using : External (Command Line) : Internet Explorer 5.x FullPath 
     **/
    public void testEBCLIeFullPath() {
	String iefp = BrowserUtils.getIEFullPath();
	BrowserUtils.setCLBrowser();
	BrowserUtils.setCLBrowserCommand(iefp);
	view();
    }

    /**
       Using: External (Command Line) : Netscape Navigator 4.7x in Path
     **/

    public void testEBCLNetscapeInPath() {
	String ns = BrowserUtils.getNetscapeInPath();
	BrowserUtils.setCLBrowser();
	BrowserUtils.setCLBrowserCommand(ns);
	view();
    }

    /**
       Using: External (Command Line) : Netscape Navigator 4.7x FullPath
     **/

    public void testEBCLNetscapeFullPath() {
	String nsfp = BrowserUtils.getNetscapeFullPath();
	BrowserUtils.setCLBrowser();
	BrowserUtils.setCLBrowserCommand(nsfp);
	view();
    }

    /**
       Using: External (Command Line) : Netscape 6.x in Path
     **/

    public void testEBCLNetscape6InPath() {
	String ns = BrowserUtils.getNetscape6InPath();
	BrowserUtils.setCLBrowser();
	BrowserUtils.setCLBrowserCommand(ns);
	view();
    }

    /**
       Using: External (Command Line) : Netscape 6.x FullPath
     **/

    public void testEBCLNetscape6FullPath() {
	String nsfp = BrowserUtils.getNetscape6FullPath();
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
	BrowserUtils.setExternalUnixBrowser();
	BrowserUtils.setEBUBrowserCommand(ns);
	view();
    }

    /**
       Using: External (Unix) : Netscape FullPath
     **/

    public void testEBUNetscapeFullPath() {
	String nsfp = BrowserUtils.getNetscapeFullPath();
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
	new Action(menuPath,null);
    }
}








