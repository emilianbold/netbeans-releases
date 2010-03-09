/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.verification;

import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.QualifiedNameKind;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ConstantDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.GotoLabel;
import org.netbeans.modules.php.editor.parser.astnodes.GotoStatement;
import org.netbeans.modules.php.editor.parser.astnodes.LambdaFunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceName;
import org.netbeans.modules.php.editor.parser.astnodes.TypeDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.UseStatement;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultTreePathVisitor;
import org.netbeans.modules.php.project.api.PhpLanguageOptions;
import org.netbeans.modules.php.project.api.PhpLanguageOptions.Properties;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class CheckPHPVersionVisitor extends DefaultTreePathVisitor {
    private FileObject fobj;
    private ArrayList<PHPVersionError> errors = new ArrayList<PHPVersionError>();

    public CheckPHPVersionVisitor(FileObject fobj) {
        this.fobj = fobj;
    }
    
    public static  boolean appliesTo(FileObject fobj) {
        if (fobj != null){
            Properties props = PhpLanguageOptions.getDefault().getProperties(fobj);

            if (props.getPhpVersion() == PhpLanguageOptions.PhpVersion.PHP_5) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void visit(NamespaceDeclaration declaration) {
        createError(declaration.getStartOffset(), declaration.getName().getEndOffset());
    }

    @Override
    public void visit(LambdaFunctionDeclaration declaration) {
        createError(declaration);
    }
    
    @Override
    public void visit(GotoLabel label) {
        createError(label);
    }

    @Override
    public void visit(ConstantDeclaration statement) {
        for (ASTNode node : getPath()) {
            if (node instanceof TypeDeclaration) {
                return;
            } 
        }
        createError(statement);
    }

    @Override
    public void visit(GotoStatement statement) {
        createError(statement);
    }

    @Override
    public void visit(UseStatement statement) {
        createError(statement);
    }

    @Override
    public void visit(NamespaceName namespaceName) {

        QualifiedName qname = QualifiedName.create(namespaceName);
        if (qname.getKind() != QualifiedNameKind.UNQUALIFIED){
            createError(namespaceName);
        }
    }

    public Collection<? extends org.netbeans.modules.csl.api.Error> getErrors(){
        return errors;
    }

    private  void createError(int startOffset, int endOffset){

        PHPVersionError error = new PHPVersionError(startOffset, endOffset);
        errors.add(error);
    }
    
    private void createError(ASTNode node){
        createError(node.getStartOffset(), node.getEndOffset());
        super.visit(node);
    }

    class PHPVersionError implements org.netbeans.modules.csl.api.Error{

        private int startPosition;
        private int endPosition;

        public PHPVersionError(int startPosition, int endPosition) {
            this.startPosition = startPosition;
            this.endPosition = endPosition;
        }

        public String getDescription() {
            return NbBundle.getMessage(CheckPHPVersionVisitor.class, "CheckPHPVerDesc");
        }

        public String getDisplayName() {
            return NbBundle.getMessage(CheckPHPVersionVisitor.class, "CheckPHPVerDispName");
        }

        public String getKey() {
            return "php.ver"; //NOI18N
        }

        public FileObject getFile() {
            return fobj;
        }

        public int getStartPosition() {
            return startPosition;
        }

        public int getEndPosition() {
            return endPosition;
        }

        public boolean isLineError() {
            return true;
        }

        public Severity getSeverity() {
            return Severity.ERROR;
        }

        public Object[] getParameters() {
            return new Object[]{};
        }

    }

}
