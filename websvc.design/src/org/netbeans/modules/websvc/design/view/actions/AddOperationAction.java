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

package org.netbeans.modules.websvc.design.view.actions;

import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import org.netbeans.modules.websvc.core.AddWsOperationHelper;
import org.netbeans.modules.websvc.core._RetoucheUtil;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Ajit Bhate
 */
public class AddOperationAction extends AbstractAction {
    
    private FileObject implementationClass;
    /** 
     * Creates a new instance of AddOperationAction 
     * @param implementationClass fileobject of service implementation class
     */
    public AddOperationAction(FileObject implementationClass) {
        super(getName());
        this.implementationClass = implementationClass;
    }
    
    private static String getName() {
        return NbBundle.getMessage(AddWsOperationHelper.class, "LBL_OperationAction");
    }

    public void actionPerformed(ActionEvent arg0) {
        AddWsOperationHelper strategy = new AddWsOperationHelper(getName());
        try {
            String className = _RetoucheUtil.getMainClassName(implementationClass);
            if (className != null) {
                strategy.addMethod(implementationClass, className);
            }
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }

}
