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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.j2ee.persistence.action;

import java.io.IOException;
import org.netbeans.api.java.classpath.ClassPath;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
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
        
        // It is possible that the activated node has no DataObject
        // See issue 137541
        if(activatedNodes[0].getCookie(DataObject.class) == null) {
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
            Exceptions.printStackTrace(ex);
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
        if (activatedNodes.length != 1) {
            return false;
        }
        
        if(activatedNodes[0].getCookie(DataObject.class) == null) {
            return false;
        }
        
        // Enable it only if the EntityManager is in the project classpath
        // This check was motivated by issue 139333 - The Use Entity Manager action 
        // breaks from left to right if the javax.persistence.EntityManager class is missing
        FileObject target = activatedNodes[0].getCookie(DataObject.class).getPrimaryFile();
        ClassPath cp = ClassPath.getClassPath(target, ClassPath.COMPILE);
        if(cp == null) {
            return false;
        }
        FileObject entityMgrRes = cp.findResource("javax/persistence/EntityManager.class"); // NOI18N
       
        if (entityMgrRes != null) { 
            return true;
        } else {
            return false;
        }
    }
}

