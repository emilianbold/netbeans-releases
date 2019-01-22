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
package org.netbeans.modules.languages.refactoring;

import java.text.MessageFormat;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ASTPath;
import org.netbeans.api.languages.ASTToken;
import org.netbeans.api.languages.ASTToken;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * 
 */
public class RenameRefactoringUI implements RefactoringUI {
    
    private String newName = null;
    private String oldName = null;
    private RenameRefactoring refactoring = null;
    private RenamePanel panel;
    private ASTPath path;

    public RenameRefactoringUI(ASTPath path, FileObject fileObject, Document doc) {
        this.refactoring = new RenameRefactoring(Lookups.fixed(path, doc));
        this.path = path;
        ASTItem item = path.getLeaf();
        this.oldName = item instanceof ASTToken ? ((ASTToken)item).getIdentifier() : ((ASTNode) item).getNT();
    }
    
    public boolean isQuery() {
        return false;
    }

    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        if (panel == null) {
            String itemName = path.getLeaf() instanceof ASTToken ? ((ASTToken) path.getLeaf()).getIdentifier() : "";
            String panelName = NbBundle.getMessage(RenameRefactoringUI.class, "LBL_Rename") + ' ' + itemName;
            panel = new RenamePanel(itemName, parent, panelName);
        }
        return panel;
    }

    public org.netbeans.modules.refactoring.api.Problem setParameters() {
        newName = panel.getNameValue();
        refactoring.setNewName(newName);
        return refactoring.checkParameters();
    }

    public org.netbeans.modules.refactoring.api.Problem checkParameters() {
        return refactoring.fastCheckParameters();
    }

    public org.netbeans.modules.refactoring.api.AbstractRefactoring getRefactoring() {
        return refactoring;
    }

    public String getDescription() {
        return new MessageFormat(NbBundle.getMessage(RenameRefactoringUI.class, "DSC_Rename")).format (
                    new Object[] {oldName, newName}
                );
    }

    public String getName() {
        return NbBundle.getMessage(RenameRefactoringUI.class, "LBL_Rename");
    }
    
    public boolean hasParameters() {
        return true;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(WhereUsedQueryUI.class);
    }
    
}
