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

package org.netbeans.test.gui.web.util;

import org.netbeans.jellytools.actions.OptionsViewAction;


import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.OptionsOperator;
import org.netbeans.jellytools.Bundle;

import org.netbeans.jellytools.properties.ComboBoxProperty;
import org.netbeans.jellytools.properties.TextFieldProperty;
import org.netbeans.jellytools.properties.PropertySheetTabOperator;
import org.netbeans.jellytools.properties.PropertySheetOperator;


import java.io.File;

public class BrowserUtils {
    private static String iSep = "|";
    public BrowserUtils() {
    }

    public static void setExternalUnixBrowser() {
	String browser = Bundle.getString("org.netbeans.modules.extbrowser.Bundle","CTL_UnixBrowserName");
	setBrowser(browser);
    }
    public static void setExternalWinBrowser() {
	String browser = Bundle.getString("org.netbeans.modules.extbrowser.Bundle","CTL_WinBrowserName");
	setBrowser(browser);
    }
    public static void setSwingBrowser() {
	String browser = Bundle.getString("org.netbeans.beaninfo.Bundle","CTL_SwingBrowser");
	setBrowser(browser);
    }
    public static void setCLBrowser() {
	String browser = Bundle.getString("org.netbeans.modules.extbrowser.Bundle","CTL_SimpleExtBrowser");
	setBrowser(browser);
    }
    public static void setCLBrowserCommand(String command) {
	OptionsOperator oo = OptionsOperator.invoke();
	String ideConfiguration = Bundle.getString("org.netbeans.core.Bundle", "UI/Services/IDEConfiguration");
	String sets = Bundle.getString("org.netbeans.core.Bundle", "UI/Services/IDEConfiguration/ServerAndExternalToolSettings");
	String browsers = Bundle.getString("org.netbeans.core.Bundle", "Services/Browsers");
	String cl = Bundle.getString("org.netbeans.modules.extbrowser.Bundle","CTL_SimpleExtBrowser");
	oo.selectOption(ideConfiguration + iSep + sets + iSep + browsers + iSep + cl);
	PropertySheetOperator pso = PropertySheetOperator.invoke();
        PropertySheetTabOperator psto = new PropertySheetTabOperator(pso);
	String pnameBrowserExecutable = Bundle.getString("org.netbeans.modules.extbrowser.Bundle" ,"PROP_browserExecutable");
	TextFieldProperty pr = new TextFieldProperty(psto, pnameBrowserExecutable);
	if (!pr.getValue().equals(command)) {
	    pr.setValue(command);
	}
    }
    

    public static void setEBUBrowserCommand(String command) {
	OptionsOperator oo = OptionsOperator.invoke();
	String ideConfiguration = Bundle.getString("org.netbeans.core.Bundle", "UI/Services/IDEConfiguration");
	String sets = Bundle.getString("org.netbeans.core.Bundle", "UI/Services/IDEConfiguration/ServerAndExternalToolSettings");
	String browsers = Bundle.getString("org.netbeans.core.Bundle", "Services/Browsers");
	String cl = Bundle.getString("org.netbeans.modules.extbrowser.Bundle","CTL_UnixBrowserName");
	oo.selectOption(ideConfiguration + iSep + sets + iSep + browsers + iSep + cl);
	PropertySheetOperator pso = PropertySheetOperator.invoke();
        PropertySheetTabOperator psto = new PropertySheetTabOperator(pso);
	String pnameBrowserExecutable = Bundle.getString("org.netbeans.modules.extbrowser.Bundle" ,"PROP_browserExecutable");
	TextFieldProperty pr = new TextFieldProperty(psto, pnameBrowserExecutable);
	if (!pr.getValue().equals(command)) {
	    pr.setValue(command);
	}
    }


    public static  void setBrowser(String name) {
	//new OptionsViewAction().perform();
	OptionsOperator oo = OptionsOperator.invoke();
	String ideConfiguration = Bundle.getString("org.netbeans.core.Bundle", "UI/Services/IDEConfiguration");
	String system = Bundle.getString("org.netbeans.core.Bundle", "UI/Services/IDEConfiguration/System");
	String systemSettings = Bundle.getString("org.netbeans.core.Bundle", "Services/org-netbeans-core-IDESettings.settings");
	oo.selectOption(ideConfiguration + iSep + system + iSep + systemSettings);
	PropertySheetOperator pso = PropertySheetOperator.invoke();
        PropertySheetTabOperator psto = new PropertySheetTabOperator(pso);
	String pnameWebBrowser = Bundle.getString("org.netbeans.core.Bundle" ,"PROP_WWW_BROWSER");
	ComboBoxProperty pr = new ComboBoxProperty(psto, pnameWebBrowser);
	if (!pr.getValue().equals(name)) {
	    pr.setValue(name);
	}
    }
    public static boolean handleSwingBrowserDialog() {
	String title = Bundle.getString("org.openide.Bundle","NTF_InformationTitle");
	try {
	    NbDialogOperator dialog = new NbDialogOperator(title);
	    dialog.ok();
	    return true;
	}catch(Exception e) {
	    return false;
	}
    }


    /**
       DDE servers section
    */
    public static void setDDEServerExplorer() {
	System.out.println("#####\nNot implemented yet\n#####");
    }
    public static void setDDEServerNetscape() {
	System.out.println("#####\nNot implemented yet\n#####");
    }
    
    public static void setDDEServerNetscape6() {
	System.out.println("#####\nNot implemented yet\n#####");
    }
    /* End of DDE servers section */

    /**
       Get Browsers section
     */

    public static String getIEInPath() {
	System.out.println("#####\nNot implemented yet\n#####");
	return null;
    }
    public static String getIEFullPath() {
	System.out.println("#####\nNot implemented yet\n#####");
	return null;
    }
    public static String getNetscapeFullPath() {
	String[] paths = null;
	String command = null;
	if(System.getProperty("os.name").indexOf("Windows")!=-1) {
            System.out.println("##########\nThis test must be extended for Windows platform\n#######");
	    return null;
        }else {
	    paths = new String[] {"/usr/bin/netscape","/usr/local/bin/netscape","/bin/netscape"};
	}
	for(int i=0;i<paths.length;i++) {
	    if((new File(paths[i])).exists()) {
		command = paths[i] + " {URL}";
		i = paths.length;
	    }
	}
	if(command == null) {
	    StringBuffer reason = new StringBuffer("Nothing of following commands found on your system : ");
	    for(int i=0;i<paths.length;i++) {
		reason.append(paths[i] + ";");
	    }
	    System.out.println("##########\n" + reason.toString() + "\n##########");
	    return null;
	}
	return command;
    }
    public static String getNetscapeInPath() {
	String netscapeWin = "netscape.exe";
	String netscapeUx = "netscape";
	if(System.getProperty("os.name").indexOf("Windows")!=-1) {
	    return netscapeWin;
        }else {
	    return netscapeUx;
	}
    }
    public static String getNetscape6FullPath() {
	String[] paths = null;
	String command = null;
	if(System.getProperty("os.name").indexOf("Windows")!=-1) {
            System.out.println("##########\nThis test must be extended for Windows platform\n#######");
	    return null;
        }else {
	    paths = new String[] {"/usr/bin/netscape6","/usr/local/bin/netscape6","/bin/netscape6"};
	}
	for(int i=0;i<paths.length;i++) {
	    if((new File(paths[i])).exists()) {
		command = paths[i] + " {URL}";
		i = paths.length;
	    }
	}
	if(command == null) {
	    StringBuffer reason = new StringBuffer("Nothing of following commands found on your system : ");
	    for(int i=0;i<paths.length;i++) {
		reason.append(paths[i] + ";");
	    }
	    System.out.println("##########\n" + reason.toString() + "\n##########");
	    return null;
	}
	return command;
    }
    public static String getNetscape6InPath() {
	String netscapeWin = "netscp6.exe";
	String netscapeUx = "netscape6";
	if(System.getProperty("os.name").indexOf("Windows")!=-1) {
	    return netscapeWin;
        }else {
	    return netscapeUx;
	}
    }
}

