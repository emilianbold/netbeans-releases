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
package org.netbeans.modules.xml.wsdl.refactoring;

import java.io.IOException;
import org.netbeans.modules.xml.refactoring.DeleteRequest;
import org.netbeans.modules.xml.refactoring.RefactorRequest;
import org.netbeans.modules.xml.refactoring.RenameRequest;
import org.netbeans.modules.xml.refactoring.spi.ChangeExecutor;
import org.netbeans.modules.xml.refactoring.spi.SharedUtils;
import org.netbeans.modules.xml.refactoring.spi.UIHelper;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.Referenceable;

/**
 *
 * @author Nam Nguyen
 */
public class WSDLChangeExecutor extends ChangeExecutor {
    
    private WSDLUIHelper uiHelper;
    
    /** Creates a new instance of WSDLChangeExecutor */
    public WSDLChangeExecutor() {
    }

    public <T extends RefactorRequest> boolean canChange(Class<T> changeType, Referenceable target) {
        if ((target instanceof WSDLComponent || target instanceof WSDLModel) && 
            (changeType == RenameRequest.class || changeType == DeleteRequest.class)) 
        {
            return true;
        }
        return false;
    }

    public void doChange(RefactorRequest request) throws IOException {
        if (request instanceof RenameRequest) {
            SharedUtils.renameTarget((RenameRequest) request);
        } else if (request instanceof DeleteRequest) {
            SharedUtils.deleteTarget((DeleteRequest) request);
        } else {
            //just do nothing
        }
    }
    
    public UIHelper getUIHelper() {
        if (uiHelper == null) {
            uiHelper = new WSDLUIHelper();
        }
        return uiHelper;
    }
    
}
