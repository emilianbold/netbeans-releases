/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.source.save;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.Trees;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.util.Context;
import java.io.IOException;
import javax.swing.text.Document;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CodeStyle;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.PositionConverter;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.java.source.JavaSourceAccessor;
import org.openide.filesystems.FileObject;

/**
 *
 * @author lahvac
 */
public class DiffContext {
    public final TokenSequence<JavaTokenId> tokenSequence;
    public final String origText;
    public final CodeStyle style;
    public final Context context;
    public final JCCompilationUnit origUnit;
    public final Trees trees;
    public final Document doc;
    public final PositionConverter positionConverter;
    public final FileObject file;

    public DiffContext(CompilationInfo copy) {
        this.tokenSequence = copy.getTokenHierarchy().tokenSequence(JavaTokenId.language());
        this.origText = copy.getText();
        this.style = getCodeStyle(copy);
        this.context = JavaSourceAccessor.getINSTANCE().getJavacTask(copy).getContext();
        this.origUnit = (JCCompilationUnit) copy.getCompilationUnit();
        this.trees = copy.getTrees();
        this.doc = copy.getSnapshot().getSource().getDocument(false); //TODO: true or false?
        this.positionConverter = copy.getPositionConverter();
        this.file = copy.getFileObject();
    }

    public DiffContext(CompilationInfo copy, CompilationUnitTree cut, String code, PositionConverter positionConverter, FileObject file) {
        this.tokenSequence = TokenHierarchy.create(code, JavaTokenId.language()).tokenSequence(JavaTokenId.language());
        this.origText = code;
        this.style = getCodeStyle(copy);
        this.context = JavaSourceAccessor.getINSTANCE().getJavacTask(copy).getContext();
        this.origUnit = (JCCompilationUnit) cut;
        this.trees = copy.getTrees();
        this.doc = null;
        this.positionConverter = positionConverter;
        this.file = file;
    }

    public static CodeStyle getCodeStyle(CompilationInfo info) {
        if (info != null) {
            try {
                Document doc = info.getDocument();
                if (doc != null) {
                    CodeStyle cs = (CodeStyle)doc.getProperty(CodeStyle.class);
                    return cs != null ? cs : CodeStyle.getDefault(doc);
                }
            } catch (IOException ioe) {
                // ignore
            }

            FileObject file = info.getFileObject();
            if (file != null) {
                return CodeStyle.getDefault(file);
            }
        }

        return CodeStyle.getDefault((Document)null);
    }

}
