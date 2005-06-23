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
package org.netbeans.modules.xml.text;

import org.openide.modules.ModuleInstall;
import org.openide.text.PrintSettings;
import org.openide.options.SystemOption;
import org.openide.util.*;

import org.netbeans.editor.Settings;

import org.netbeans.modules.xml.text.syntax.XMLPrintOptions;
import org.netbeans.modules.xml.text.syntax.DTDPrintOptions;
import org.netbeans.modules.xml.text.syntax.XMLSettingsInitializer;

/**
 * Module installation class for text-edit module.
 *
 * @author Libor Kramolis
 */
public class TextEditModuleInstall extends ModuleInstall {

    public void installed() {
        restored();
    }
    
    /**
     */
    public void restored () {
        restoredTextEditor();
    }

    /**
     */
    public void uninstalled () {
        uninstalledTextEditor();
    }

    /**
     */
    public void restoredTextEditor () {
        //layer based defaults still need it
        Settings.addInitializer (new XMLSettingsInitializer());

        // editor options
        PrintSettings ps = (PrintSettings)PrintSettings.findObject (PrintSettings.class, true);
        ps.addOption ((XMLPrintOptions)XMLPrintOptions.findObject(XMLPrintOptions.class, true));
        ps.addOption ((DTDPrintOptions)DTDPrintOptions.findObject(DTDPrintOptions.class, true));
    }
    
    /**
     */
    public void uninstalledTextEditor () {
        // remove options
        PrintSettings ps = (PrintSettings) PrintSettings.findObject (PrintSettings.class, true);
        
        SystemOption opt = (SystemOption) SystemOption.findObject (XMLPrintOptions.class, false);
        if (opt != null)
	    ps.removeOption (opt);

        opt = (SystemOption) SystemOption.findObject (DTDPrintOptions.class, false);
        if (opt != null)
	    ps.removeOption (opt);
        
        //layer based defaults still need it
        Settings.removeInitializer (XMLSettingsInitializer.NAME);                
    }
    
}
