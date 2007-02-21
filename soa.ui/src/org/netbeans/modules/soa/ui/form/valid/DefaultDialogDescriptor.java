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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.soa.ui.form.valid;

import java.awt.Container;
import org.netbeans.modules.soa.ui.UserNotification;
import org.netbeans.modules.soa.ui.form.FormLifeCycle;

/**
 * The dialog descriptor which supports form life cycle and validation.
 *
 * @author nk160297
 */
public class DefaultDialogDescriptor extends AbstractDialogDescriptor {
    
    public DefaultDialogDescriptor(Object innerPane, String title) {
        super(innerPane, title);
    }
    
    public void processOkButton() {
        Object innerPane = super.getMessage();
        //
        ValidStateManager vsManager = getValidStateManager(false);
        //
        if (innerPane instanceof Container) {
            vsManager.clearReasons();
            Container parent = ((Container)innerPane).getParent();
            DefaultValidStateManager.validateChildrenControls(
                    vsManager, parent, false);
        }
        //
        if (vsManager.isValid()) {
            setOptionClosable(btnOk, true);
        } else {
            String reason = vsManager.getReason();
            if (reason != null && reason.length() != 0) {
                UserNotification.showMessage(reason);
            }
        }
    }
    
    public void processWindowClose() {
        Object innerPane = super.getMessage();
        //
        if (innerPane instanceof FormLifeCycle) {
            FormLifeCycle flc = (FormLifeCycle)innerPane;
            flc.unsubscribeListeners();
            flc.afterClose();
        }
    }
    
    public void setMessage(Object innerPane) {
        super.setMessage(innerPane);
    }
    
}
