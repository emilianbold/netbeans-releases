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
package org.netbeans.modules.languages.features.refactoring;

import java.text.MessageFormat;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ASTPath;
import org.netbeans.api.languages.ASTToken;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Daniel Prusa
 */
public class WhereUsedQueryUI implements RefactoringUI {
    
    private WhereUsedQuery query = null;
    private ASTPath path;
    private String name;

    public WhereUsedQueryUI(ASTPath path, FileObject fileObject, Document doc) {
        this.query = new WhereUsedQuery(Lookups.fixed(path, fileObject, doc));
        this.path = path;
        ASTItem item = path.getLeaf();
        this.name = item instanceof ASTToken ? ((ASTToken)item).getIdentifier() : ((ASTNode) item).getNT();
    }
    
    public boolean isQuery() {
        return true;
    }

    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        return null;
    }

    public org.netbeans.modules.refactoring.api.Problem setParameters() {
        return query.checkParameters();
    }

    public org.netbeans.modules.refactoring.api.Problem checkParameters() {
        return query.fastCheckParameters();
    }

    public org.netbeans.modules.refactoring.api.AbstractRefactoring getRefactoring() {
        return query;
    }

    public String getDescription() {
        return new MessageFormat(NbBundle.getMessage(WhereUsedQueryUI.class, "DSC_WhereUsed")).format (
                    new Object[] {name}
                );
    }

    public String getName() {
        return NbBundle.getMessage(WhereUsedQueryUI.class, "LBL_WhereUsed");
    }
    
    public boolean hasParameters() {
        return false;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(WhereUsedQueryUI.class);
    }
    
}
