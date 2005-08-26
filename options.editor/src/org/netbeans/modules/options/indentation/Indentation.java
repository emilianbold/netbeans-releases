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

package org.netbeans.modules.options.indentation;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.netbeans.spi.options.OptionsCategory;
import org.openide.util.NbBundle;
import org.netbeans.modules.options.*;


/**
 * Contains information about Indentation Panel, and creates a new 
 * instance of it.
 *
 * @author Jan Jancura
 */
public final class Indentation extends OptionsCategory {

    private static String loc (String key) {
        return NbBundle.getMessage (Indentation.class, key);
    }
 
    public String getIconBase () {
        return "org/netbeans/modules/options/resources/indentation";
    }
    
    public String getCategoryName () {
        return loc ("CTL_Indentation");
    }

    public String getTitle () {
        return loc ("CTL_Indentation_Title");
    }
    
    public String getDescription () {
        return loc ("CTL_Indentation_Description");
    }

    public PanelController create () {
        return new IndentationPanelController ();
    }
}