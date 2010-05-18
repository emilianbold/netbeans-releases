/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.vmd.midp.codegen.ui;

import java.text.MessageFormat;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.midp.codegen.InstaceRenameRefactoring;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;


/**
 *
 * @author ads
 */
public class RenameRefactoringUI implements RefactoringUI{
    private String myOldName ;
    private DesignComponent myComponent;
    private String newName;
    private RenamePanel panel;
    private InstaceRenameRefactoring myRefactoring;
    private boolean isAccessor;
    
    public RenameRefactoringUI(InstaceRenameRefactoring refactoring, 
            CompilationInfo info, String oldName , DesignComponent component, 
            boolean useAccessor )
    {
        myRefactoring = refactoring;
        //oldName = element.getSimpleName().toString();
        myOldName = oldName;
        myComponent = component;
        isAccessor = useAccessor;
        
        //refactoring.getContext().add(RetoucheUtils.getClasspathInfoFor(true, true, RetoucheUtils.getFileObject(handle)));
    }
    
    public boolean isQuery() {
        return false;
    }

    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        if (panel == null) {
            String name = myOldName;
            panel = new RenamePanel(name, parent, NbBundle.getMessage(
                    RenamePanel.class, "LBL_Rename") + " " + name ,
                    myComponent, isAccessor);
        }
        return panel;
    }
    
    public org.netbeans.modules.refactoring.api.Problem setParameters() {
        newName = panel.getNameValue();
        myRefactoring.setNewFieldName(newName);
        if ( isAccessor ){
            myRefactoring.setNewGetterName( panel.getGetter() );
        }
        return myRefactoring.checkParameters();
    }
    
    public org.netbeans.modules.refactoring.api.Problem checkParameters() {
        return setParameters();
    }

    public org.netbeans.modules.refactoring.api.AbstractRefactoring getRefactoring() {
        return myRefactoring;
    }

    public String getDescription() {
        return new MessageFormat(NbBundle.getMessage(RenamePanel.class, "DSC_Rename")).format (
                    new Object[] {myOldName, newName}
                );
    }

    public String getName() {
        return NbBundle.getMessage(RenamePanel.class, "LBL_Rename");
    }

    public boolean hasParameters() {
        return true;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(RenameRefactoringUI.class.getName() + ".InstanceName");      // NOI18N
    }
 
}
