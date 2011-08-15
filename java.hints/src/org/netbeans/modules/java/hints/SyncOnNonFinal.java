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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2007-2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.SynchronizedTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.spi.AbstractHint;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public class SyncOnNonFinal extends AbstractHint {

    public SyncOnNonFinal() {
        super(true, true, HintSeverity.WARNING);
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(SyncOnNonFinal.class, "DSC_SynchronizationOnNonFinalField");
    }

    public Set<Kind> getTreeKinds() {
        return EnumSet.of(Kind.SYNCHRONIZED);
    }

    public List<ErrorDescription> run(CompilationInfo compilationInfo, TreePath treePath) {
        if (!getTreeKinds().contains(treePath.getLeaf().getKind()))
            return null;
        
        ExpressionTree expression = ((SynchronizedTree) treePath.getLeaf()).getExpression();
        
        Element e = compilationInfo.getTrees().getElement(new TreePath(treePath,expression));
        
        if (e == null || e.getKind() != ElementKind.FIELD || e.getModifiers().contains(Modifier.FINAL)) {
            return null;
        }
        
        int start = (int) compilationInfo.getTrees().getSourcePositions().getStartPosition(compilationInfo.getCompilationUnit(), expression);
        int end   = (int) compilationInfo.getTrees().getSourcePositions().getEndPosition(compilationInfo.getCompilationUnit(), expression);
        String displayName = NbBundle.getMessage(SyncOnNonFinal.class, "ERR_SynchronizationOnNonFinalField");
        
        return Collections.singletonList(ErrorDescriptionFactory.createErrorDescription(getSeverity().toEditorSeverity(), displayName, compilationInfo.getFileObject(), start, end));
    }

    public String getId() {
        return SyncOnNonFinal.class.getName();
    }

    public String getDisplayName() {
        return NbBundle.getMessage(SyncOnNonFinal.class, "DN_SynchronizationOnNonFinalField");
    }

    public void cancel() {
    }

}
