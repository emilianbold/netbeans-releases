/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.text;

import javax.swing.JEditorPane;

import org.openide.modules.ModuleInstall;
import org.openide.text.PrintSettings;
import org.openide.options.SystemOption;
import org.openide.util.*;

import org.netbeans.editor.Settings;
import org.netbeans.modules.editor.options.AllOptions;

import org.netbeans.modules.xml.core.XMLDataObject;
import org.netbeans.modules.xml.core.DTDDataObject;

import org.netbeans.modules.xml.text.syntax.XMLKit;
import org.netbeans.modules.xml.text.syntax.DTDKit;
import org.netbeans.modules.xml.text.syntax.XMLOptions;
import org.netbeans.modules.xml.text.syntax.DTDOptions;
import org.netbeans.modules.xml.text.syntax.XMLPrintOptions;
import org.netbeans.modules.xml.text.syntax.DTDPrintOptions;
import org.netbeans.modules.xml.text.syntax.XMLSettingsInitializer;

/**
 * Module installation class for text-edit module.
 *
 * @author Libor Kramolis
 */
public class TextEditModuleInstall extends ModuleInstall {

    private static final long serialVersionUID = -7645158417177075459L;

    // name of kit class that was replaced
    private static transient String originalXMLKit;
    
    // name of kit class that was replaced    
    private static transient String originalDTDKit;

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


    //
    // Text editor
    //

    /**
     */
    public void restoredTextEditor () {
        Settings.addInitializer (new XMLSettingsInitializer());

        ClassLoader loader = this.getClass().getClassLoader();
        
        
        // Registration of the editor kits to JEditorPane
        
        originalXMLKit =
            JEditorPane.getEditorKitClassNameForContentType(XMLDataObject.MIME_TYPE);
        originalDTDKit =
            JEditorPane.getEditorKitClassNameForContentType(DTDDataObject.MIME_TYPE);
        
        JEditorPane.registerEditorKitForContentType
            (XMLDataObject.MIME_TYPE, XMLKit.class.getName(), loader);
        JEditorPane.registerEditorKitForContentType
            (DTDDataObject.MIME_TYPE, DTDKit.class.getName(), loader);

        // editor options
        
        AllOptions ao = (AllOptions)AllOptions.findObject (AllOptions.class, true);
        ao.addOption (new XMLOptions());
        ao.addOption (new DTDOptions());
        
        PrintSettings ps = (PrintSettings)PrintSettings.findObject (PrintSettings.class, true);
        ps.addOption (new XMLPrintOptions());
        ps.addOption (new DTDPrintOptions());
    }
    
    /**
     */
    public void uninstalledTextEditor () {

        // remove options
        AllOptions ao = (AllOptions)AllOptions.findObject (AllOptions.class, true);
        
        XMLOptions xo = (XMLOptions) ao.findObject (XMLOptions.class, false);
        if (xo != null)
	    ao.removeOption(xo);

        DTDOptions xdo = (DTDOptions) ao.findObject (DTDOptions.class, false);
        if (xdo != null)
	    ao.removeOption(xdo);

        PrintSettings ps = (PrintSettings) PrintSettings.findObject (PrintSettings.class, true);
        
        SystemOption opt = (SystemOption) SystemOption.findObject (XMLPrintOptions.class, false);
        if (opt != null)
	    ps.removeOption (opt);

        opt = (SystemOption) SystemOption.findObject (DTDPrintOptions.class, false);
        if (opt != null)
	    ps.removeOption (opt);
        
        // uninstall kits

        ClassLoader loader = (ClassLoader) Lookup.getDefault().lookup(ClassLoader.class);
        
        if (XMLKit.class.getName().equals(JEditorPane.getEditorKitClassNameForContentType(XMLDataObject.MIME_TYPE))) {
            JEditorPane.registerEditorKitForContentType(XMLDataObject.MIME_TYPE, originalXMLKit, loader);
        }
        
        if (XMLKit.class.getName().equals(JEditorPane.getEditorKitClassNameForContentType(DTDDataObject.MIME_TYPE))) {
            JEditorPane.registerEditorKitForContentType(DTDDataObject.MIME_TYPE, originalDTDKit, loader);
        }
        
    }
    
}

