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

package org.netbeans.installer.event.dialog.swing;


import com.installshield.event.*;
import com.installshield.event.ui.*;
import com.installshield.event.wizard.*;
import com.installshield.event.product.*;
import com.installshield.wizard.*;
import com.installshield.wizard.service.*;
import com.installshield.wizard.service.system.*;
import com.installshield.wizard.awt.*;
import com.installshield.wizard.swing.*;
import com.installshield.wizard.console.*;
import com.installshield.product.*;
import com.installshield.util.*;
import com.installshield.product.service.product.ProductService;

public class PanelLogout {
    boolean active;
    
    public void queryEnterLogout(
        com.installshield.event.ISQueryContext arg0) {
        	
        SystemUtilService service = null;
        try {
            service =
                (SystemUtilService)arg0.getService(SystemUtilService.NAME);
            active = service.isLogoutRequired();
            if (!active) {
                arg0.setReturnValue(false);
                return;
            }

        }
        catch (Throwable e) {
            e.printStackTrace();
        }
    }

}