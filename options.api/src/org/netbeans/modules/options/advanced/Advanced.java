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

package org.netbeans.modules.options.advanced;

import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.spi.options.OptionsCategory;
import org.openide.util.NbBundle;


/**
 * Contains information about Advanced Panel, and creates a new 
 * instance of it.
 *
 * @author Jan Jancura
 */
public final class Advanced extends OptionsCategory {

    private static String loc (String key) {
        return NbBundle.getMessage (Advanced.class, key);
    }

    public String getIcon () {
        return "org/netbeans/modules/options/resources/advanced";
    }
    
    public String getCategoryName () {
        return loc ("CTL_Advanced_Options");
    }

    public String getTitle () {
        return loc ("CTL_Advanced_Options_Title");
    }
    
    public String getDescription () {
        return loc ("CTL_Advanced_Options_Description");
    }

    public JComponent getPane () {
        return new AdvancedPanel ();
    }
}