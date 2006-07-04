/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
