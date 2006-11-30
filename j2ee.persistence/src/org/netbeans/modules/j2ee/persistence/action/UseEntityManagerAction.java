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

package org.netbeans.modules.j2ee.persistence.action;

import java.io.IOException;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * @author Martin Adamek
 */
public final class UseEntityManagerAction extends NodeAction {
    
    protected void performAction(Node[] activatedNodes) {
        if (activatedNodes.length != 1) {
            return;
        }
        
        FileObject target = activatedNodes[0].getCookie(DataObject.class).getPrimaryFile();
        
        EntityManagerGenerator emGenerator = new EntityManagerGenerator(target, target.getName());
        GenerationOptions options = new GenerationOptions();
        options.setParameterName("object");
        options.setParameterType("Object");
        options.setMethodName("persist");
        options.setOperation(GenerationOptions.Operation.PERSIST);
        options.setReturnType("void");
        try {
            emGenerator.generate(options);
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }
    
    public String getName() {
        return NbBundle.getMessage(UseEntityManagerAction.class, "CTL_UseEntityManagerAction");
    }
    
    @Override
    protected void initialize() {
        super.initialize();
        putValue("noIconInMenu", Boolean.TRUE);
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    @Override
    protected boolean asynchronous() {
        return false;
    }
    
    protected boolean enable(Node[] activatedNodes) {
        return true;
    }
    
}

