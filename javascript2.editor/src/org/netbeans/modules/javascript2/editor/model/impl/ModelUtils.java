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
package org.netbeans.modules.javascript2.editor.model.impl;

import java.util.List;
import org.netbeans.modules.javascript2.editor.model.FileScope;
import org.netbeans.modules.javascript2.editor.model.FunctionScope;
import org.netbeans.modules.javascript2.editor.model.Identifier;
import org.netbeans.modules.javascript2.editor.model.ModelElement;

/**
 *
 * @author Petr Pisl
 */
public class ModelUtils {

    public static FileScopeImpl getFileScope(ModelElement element) {
        FileScopeImpl result = null;

        while (element != null && !(element instanceof FileScope)) {
            element = element.getInElement();
        }

        if (element != null && element instanceof FileScope) {
            result = (FileScopeImpl) element;
        }
        return result;
    }
    
    public static String getNameWithoutPrototype(List<Identifier> fqName) {
        StringBuilder name = new StringBuilder();
        int size = fqName.size();
        String part;
        for(int i = 0; i < size; i++) {
            part = fqName.get(i).getName();
            if ("prototype".equals(part)) {   //NOI18N
                break;
            }
            name.append(part);
            if (i < (size - 1) && !("prototype".equals(fqName.get(i+1).getName()))) {
               name.append(".");                //NOI18N
            }
        }
        return name.toString();
    }
    
    public static String getPartName(List<Identifier> fqName, int parts) {
        StringBuilder name = new StringBuilder();
        int size = fqName.size();
        String part;
        for(int i = 0; i < size && i < parts; i++) {
            part = fqName.get(i).getName();
            name.append(part);
            if (i < (size - 1) && i < (parts - 1)) {
               name.append(".");                //NOI18N
            }
        }
        return name.toString();
    }
    
    public static String getObjectName(FunctionScope function) {
        String name = null;
        int size = function.getFQDeclarationName().size();
        if(size > 1) {
            if ("prototype".equals(function.getFQDeclarationName().get(size - 2).getName())) {
                name = getNameWithoutPrototype(function.getFQDeclarationName());
            } else {
                name = getPartName(function.getFQDeclarationName(), size - 1);
            }
        }
        return name;
    }
}
