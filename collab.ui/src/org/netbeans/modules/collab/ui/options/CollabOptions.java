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
package org.netbeans.modules.collab.ui.options;

import org.netbeans.spi.options.OptionsPanelController;
import org.netbeans.spi.options.AdvancedOption;
import org.openide.util.NbBundle;

/**
 *
 * @author catlan
 */
public class CollabOptions extends AdvancedOption {
    
    public String getDisplayName () {
        return NbBundle.getMessage (CollabOptions.class, "CTL_CollabOptions_Name");
    }
    
    public String getTooltip () {
        return NbBundle.getMessage (CollabOptions.class, "CTL_CollabOptions_Tooltip");      
    }
    
    public OptionsPanelController create () {
        return new CollabOptionPanelController();
    }    
}
