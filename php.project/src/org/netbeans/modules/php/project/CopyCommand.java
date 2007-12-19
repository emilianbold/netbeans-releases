/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.project;

import org.netbeans.modules.php.project.ui.actions.*;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.support.ProjectOperations;
import org.openide.awt.Actions;
import org.openide.util.NbBundle;

/**
 *
 * @author avk
 */
public class CopyCommand extends AbstractCommand{

    public CopyCommand(Project project){
        super(project);
    }
    
    public String getId() {
        return ActionProvider.COMMAND_COPY;
    }

    public String getLabel() {
        return NbBundle.getMessage(Actions.class, "LBL_CopyProjectAction_Name");
    }

    public void run() {
//        DefaultProjectOperations.performDefaultCopyOperation(myProject);
        // checks are copied from DefaultProjectOperations.performDefaultCopyOperation
        // DefaultProjectOperationsImplementation.copyProject 
        // is replaced by our implementation.
        if (getProject() == null) {
            throw new IllegalArgumentException("Project is null");
        }
        
        if (!ProjectOperations.isCopyOperationSupported(getProject())) {
            throw new IllegalStateException(
                    "Attempt to copy project that does not support copy.");
        }
        
        PhpProjectOperationsImplementation.copyProject(getProject());
        
    }

}
