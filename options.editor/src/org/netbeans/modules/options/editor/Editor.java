/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.options.editor;

import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.spi.options.OptionsCategory;
import org.netbeans.spi.options.OptionsCategory.PanelController;
import org.openide.util.NbBundle;


/**
 * Contains information about Abbreviations Panel, and creates a new 
 * instance of it.
 *
 * @author Jan Jancura
 */
public final class Editor extends OptionsCategory {

    private static String loc (String key) {
        return NbBundle.getMessage (Editor.class, key);
    }
 
    public String getIconBase () {
        return "org/netbeans/modules/options/resources/editor";
    }
    
    public String getCategoryName () {
        return loc ("CTL_Editor");
    }

    public String getTitle () {
        return loc ("CTL_Editor_Title");
    }
    
    public String getDescription () {
        return loc ("CTL_Editor_Description");
    }

    public PanelController create () {
        return new EditorPanelController ();
    }
}