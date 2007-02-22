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
package org.netbeans.modules.xml.schema.refactoring;

import java.io.IOException;
import org.netbeans.modules.xml.refactoring.DeleteRequest;
import org.netbeans.modules.xml.refactoring.FileRenameRequest;
import org.netbeans.modules.xml.refactoring.RefactorRequest;
import org.netbeans.modules.xml.refactoring.RenameRequest;
import org.netbeans.modules.xml.refactoring.spi.ChangeExecutor;
import org.netbeans.modules.xml.refactoring.spi.SharedUtils;
import org.netbeans.modules.xml.refactoring.spi.UIHelper;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.xam.Referenceable;

/**
 *
 * @author Nam Nguyen
 */
public class SchemaChangeExecutor extends ChangeExecutor {
    
    /** Creates a new instance of SchemaChangePerformer */
    public SchemaChangeExecutor() {
    }

    public <T extends RefactorRequest> boolean canChange(Class<T> changeType, Referenceable target) {
        return (target instanceof SchemaComponent || target instanceof SchemaModel) &&
               (changeType == RenameRequest.class || 
                changeType == DeleteRequest.class);
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
        return new SchemaUIHelper();
    }

}

