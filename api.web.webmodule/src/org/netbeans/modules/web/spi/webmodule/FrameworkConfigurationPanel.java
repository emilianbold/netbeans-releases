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

package org.netbeans.modules.web.spi.webmodule;

import org.openide.WizardDescriptor;

/** Extension to WizardDescriptor.Panel. It allows to enable/disable
 *  framework configuration panel components as requested by different
 *  usages of the dialog.
 */
public interface FrameworkConfigurationPanel extends  WizardDescriptor.Panel {
    
    /** Enable/disable panel components.
    * @param enable if the components should be enabled or disabled
    */
    public void enableComponents(boolean enable);
}
