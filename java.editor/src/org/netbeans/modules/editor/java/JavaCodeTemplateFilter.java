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

package org.netbeans.modules.editor.java;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.awt.Toolkit;
import java.io.IOException;
import java.util.EnumSet;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplate;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateFilter;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Dusan Balek
 */
public class JavaCodeTemplateFilter implements CodeTemplateFilter, CancellableTask<CompilationController> {
    
    private int startOffset;
    private int endOffset;
    private Tree.Kind ctx = null;
    
    private JavaCodeTemplateFilter(JTextComponent component, int offset) {
        this.startOffset = offset;
        this.endOffset = component.getSelectionStart() == offset ? component.getSelectionEnd() : -1;            
        JavaSource js = JavaSource.forDocument(component.getDocument());
        if (js != null) {
            try {
                if (SourceUtils.isScanInProgress()) {
                    StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(JavaCodeTemplateFilter.class, "JCT-scanning-in-progress")); //NOI18N
                    Toolkit.getDefaultToolkit().beep();
                } else {
                    js.runUserActionTask(this, true);
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    public synchronized boolean accept(CodeTemplate template) {
        return ctx != null && getTemplateContexts(template).contains(ctx);
    }
    
    public void cancel() {
    }

    public synchronized void run(CompilationController controller) throws IOException {
        controller.toPhase(Phase.PARSED);
        Tree tree = controller.getTreeUtilities().pathFor(startOffset).getLeaf();
        if (endOffset >= 0 && startOffset != endOffset) {
            if (controller.getTreeUtilities().pathFor(endOffset).getLeaf() != tree)
                return;
        }
        ctx = tree.getKind();
    }

    private EnumSet<Tree.Kind> getTemplateContexts(CodeTemplate template) {
        //TODO: rewrite this method when contexts are provided by templates
        String abbrev = template.getAbbreviation().toLowerCase();
        if (abbrev.equals("runn") || abbrev.startsWith("for") || abbrev.startsWith("while") || abbrev.equals("inst") || abbrev.startsWith("if") || abbrev.startsWith("do") || abbrev.startsWith("try"))
            return EnumSet.of(Tree.Kind.BLOCK, Tree.Kind.CASE);
        return EnumSet.noneOf(Tree.Kind.class);
    }

    public static final class Factory implements CodeTemplateFilter.Factory {
        
        public CodeTemplateFilter createFilter(JTextComponent component, int offset) {
            return new JavaCodeTemplateFilter(component, offset);
        }
    }
}
