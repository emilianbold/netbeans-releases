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

package org.netbeans.test.editor.suites.abbrevs;

import org.netbeans.test.oo.gui.jelly.MainFrame;
import org.netbeans.test.oo.gui.jelly.Options;
import org.netbeans.test.oo.gui.jelly.PropertiesWindow;
import org.netbeans.modules.editor.options.BaseOptions;
import java.util.ResourceBundle;
import org.openide.util.NbBundle;
import org.netbeans.modules.editor.options.BaseOptionsBeanInfo;
import java.awt.event.KeyEvent;

/**
 *
 * @author  Jan Lahoda
 */
public class Utilities {
    
    /** Creates a new instance of Utilities */
    public Utilities() {
    }
    
    public static void addAbbreviation(String editorName, String[] abbrevName, String[] abbrevContent) throws Exception {
        if (abbrevName.length != abbrevContent.length)
            throw new IllegalArgumentException("The arrays do not have same number of items.");
        
        MainFrame mainWindow = MainFrame.getMainFrame();
        mainWindow.pushToolsMenu("Options...");
        
        Options options = Options.find();
        PropertiesWindow properties = options.getPropertiesWindow("Editing" + options.delim + "Editor Settings" + options.delim + editorName + " Editor");
        
        properties.openEditDialog(getEditorBundle().getString("PROP_" + BaseOptions.ABBREV_MAP_PROP));
        
        AbbreviationsDialog abbreviations = new AbbreviationsDialog();
        
        for (int cntr = 0; cntr < abbrevContent.length; cntr++) {
            abbreviations.pushAddNoBlock();
            
            AddAbreviationDialog addAbbrev = new AddAbreviationDialog();
            
            addAbbrev.putAbbrevShortCut(abbrevName[cntr]);
            addAbbrev.putAbbrevContent(abbrevContent[cntr]);
            addAbbrev.pushOKButton();
        }

        Thread.sleep(1000);
        
        abbreviations.pushOKButtonNoBlock();
        
        properties.close();
        options.close();
    }

    public static void removeAbbreviation(String editorName, String[] abbrevName) throws Exception {
        MainFrame mainWindow = MainFrame.getMainFrame();
        mainWindow.pushToolsMenu("Options...");
        
        Options options = Options.find();
        PropertiesWindow properties = options.getPropertiesWindow("Editing" + options.delim + "Editor Settings" + options.delim + editorName + " Editor");
        
        properties.openEditDialog(getEditorBundle().getString("PROP_" + BaseOptions.ABBREV_MAP_PROP));
        
        AbbreviationsDialog abbreviations = new AbbreviationsDialog();

        for (int cntr = 0; cntr < abbrevName.length; cntr++ ){
            if (abbreviations.selectAbbreviation(abbrevName[cntr]))
                abbreviations.pushRemoveNoBlock();
        }
        
        Thread.sleep(1000);
        
        abbreviations.pushOKButtonNoBlock();
        
        properties.close();
        options.close();
    }
    
    public static ResourceBundle getEditorBundle() {
        return NbBundle.getBundle(BaseOptionsBeanInfo.class);
    }
        
}
