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

package org.netbeans.modules.form;

import org.netbeans.spi.options.AdvancedOption;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.NbBundle;

/**
 * Contains information about Abbreviations Panel, and creates a new 
 * instance of it.
 *
 * @author Jan Jancura
 */
public final class FormEditorOptions extends AdvancedOption {

    private static String loc (String key) {
        return NbBundle.getMessage (FormEditorOptions.class, key);
    }

    
    public String getDisplayName () {
        return loc ("Form_Editor"); // NOI18N
    }

    public String getTooltip () {
        return loc ("Form_Editor_Tooltip"); // NOI18N
    }

    public OptionsPanelController create () {
        return new FormEditorPanelController ();
    }
}
