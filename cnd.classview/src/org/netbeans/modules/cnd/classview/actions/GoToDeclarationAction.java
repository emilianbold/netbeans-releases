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

package org.netbeans.modules.cnd.classview.actions;

import javax.swing.*;
import java.awt.event.*;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;

import  org.netbeans.modules.cnd.api.model.*;

import org.netbeans.modules.cnd.classview.resources.I18n;

/**
 * @author Vladimir Kvashin
 */
public class GoToDeclarationAction extends AbstractAction {
    
    private CsmOffsetable csmObject;
    
    public GoToDeclarationAction(CsmOffsetable csmObject) {
        this.csmObject = csmObject;
        putValue(Action.NAME, I18n.getMessage("LBL_GoToDeclaration")); //NOI18N
    }
    
    public void actionPerformed(ActionEvent e) {
        //JOptionPane.showMessageDialog(null, "Not implemented");
        CsmUtilities.openSource(csmObject);
    }
}
