package org.netbeans.test.gui.web.util;

import org.netbeans.jellytools.actions.OptionsViewAction;


import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.OptionsOperator;
import org.netbeans.jellytools.Bundle;

import org.netbeans.jellytools.properties.ComboBoxProperty;
import org.netbeans.jellytools.properties.TextFieldProperty;
import org.netbeans.jellytools.properties.PropertySheetTabOperator;
import org.netbeans.jellytools.properties.PropertySheetOperator;


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

}

