/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.verification;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.editor.api.ElementQuery.Index;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.elements.ClassElement;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.nodes.ASTNodeInfo;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.netbeans.modules.php.editor.verification.PHPHintsProvider.Kind;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class AbstractClassInstantiationHint extends AbstractRule {

    private static final String HINT_ID = "Abstract.Class.Instantiation.Hint"; //NOI18N

    @Override
    void computeHintsImpl(PHPRuleContext context, List<Hint> hints, Kind kind) throws BadLocationException {
        PHPParseResult phpParseResult = (PHPParseResult) context.parserResult;
        if (phpParseResult.getProgram() == null) {
            return;
        }
        FileObject fileObject = phpParseResult.getSnapshot().getSource().getFileObject();
        CheckVisitor checkVisitor = new CheckVisitor(fileObject, context.getIndex());
        phpParseResult.getProgram().accept(checkVisitor);
        hints.addAll(checkVisitor.getHints());
    }

    private class CheckVisitor extends DefaultVisitor {
        private final FileObject fileObject;
        private final Index index;
        private final List<Hint> hints = new LinkedList<Hint>();

        private CheckVisitor(FileObject fileObject, Index index) {
            this.fileObject = fileObject;
            this.index = index;
        }

        public List<Hint> getHints() {
            return hints;
        }

        @Override
        @Messages({
            "# {0} - Class name",
            "AbstractClassInstantiationDesc=Abstract class {0} can not be instantiated"
        })
        public void visit(ClassInstanceCreation node) {
            ASTNodeInfo<ClassInstanceCreation> info = ASTNodeInfo.create(node);
            Set<ClassElement> classes = index.getClasses(NameKind.exact(info.getQualifiedName()));
            if (!classes.isEmpty()) {
                ClassElement classElement = ModelUtils.getFirst(classes);
                if (classElement != null && classElement.isAbstract()) {
                    OffsetRange offsetRange = new OffsetRange(node.getStartOffset(), node.getEndOffset());
                    hints.add(new Hint(AbstractClassInstantiationHint.this, Bundle.AbstractClassInstantiationDesc(classElement.getFullyQualifiedName().toString()), fileObject, offsetRange, null, 500));
                }
            }
        }

    }

    @Override
    public String getId() {
        return HINT_ID;
    }

    @Override
    @Messages("AbstractClassInstantiationHintDesc=Abstract classes can not be instantiated.")
    public String getDescription() {
        return Bundle.AbstractClassInstantiationHintDesc();
    }

    @Override
    @Messages("AbstractClassInstantiationHintDispName=Abstract Class Instantiation")
    public String getDisplayName() {
        return Bundle.AbstractClassInstantiationHintDispName();
    }

    @Override
    public HintSeverity getDefaultSeverity() {
        return HintSeverity.ERROR;
    }

}
