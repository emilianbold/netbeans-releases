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

package org.netbeans.modules.php.editor.model.nodes;

import java.util.Collections;
import java.util.List;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.editor.model.Parameter;
import org.netbeans.modules.php.editor.model.PhpModifiers;
import org.netbeans.modules.php.editor.model.QualifiedName;
import org.netbeans.modules.php.editor.model.nodes.ASTNodeInfo.Kind;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocTag;

/**
 * @author Radek Matous
 */
public class MagicMethodDeclarationInfo extends ASTNodeInfo<PHPDocTag> {
    private String returnType;
    private String methodName;
    private int offset;
    MagicMethodDeclarationInfo(PHPDocTag node) {
        super(node);
        String parts[] = node.getValue().split("\\s+", 3); //NOI18N
        if (parts.length >= 2) {
            String[] typeNames = parts[0].split("\\|", 2);
            String[] methodNames = parts[1].split("[(, ]", 2);
            if (typeNames.length > 0 && methodNames.length > 0) {
                returnType = typeNames[0];
                methodName = methodNames[0];
                offset = getOriginalNode().getStartOffset()+PHPDocTag.Type.METHOD.toString().length() + 2 +node.getValue().indexOf(methodName);
            }
        }
    }

    @CheckForNull
    public static MagicMethodDeclarationInfo create(PHPDocTag node) {
        MagicMethodDeclarationInfo retval = new MagicMethodDeclarationInfo(node);
        return (retval.getName() != null) ? retval : null;
    }

    @Override
    public Kind getKind() {
        return Kind.METHOD;
    }

    @Override
    public String getName() {
        return methodName;
    }

    public String getReturnType() {
        return returnType;
    }

    @Override
    public QualifiedName getQualifiedName() {
        return QualifiedName.create(getName());
    }

    @Override
    public OffsetRange getRange() {
        return new OffsetRange(offset,
                offset+getName().length());
    }

    public List<? extends Parameter> getParameters() {
        return Collections.emptyList();
    }
    
    public PhpModifiers getAccessModifiers() {
        return new PhpModifiers(PhpModifiers.PUBLIC);
    }
}
