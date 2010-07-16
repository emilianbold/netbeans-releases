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
 * License. When distributing the software, include this License Header
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.bpel.model.api.support;

import java.util.ArrayList;
import java.util.List;

import org.netbeans.modules.xml.misc.Xml;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.Variable;
import org.netbeans.modules.bpel.model.api.VariableContainer;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2009.04.17
 */
public final class VariableUtil {

    private VariableUtil() {}

    public static String getGoodVariableName(Object object) {
        if ( !(object instanceof Variable)) {
            return object.toString();
        }
        Variable variable = (Variable) object;
        StringBuilder builder = new StringBuilder(Xml.getGoodName(variable));
        BpelEntity parent = variable.getParent();
        String name;

        while (parent != null) {
            if (parent instanceof Process) {
                break;
            }
            if ( !(parent instanceof VariableContainer)) {
                name = Xml.getGoodName(parent);

                if (name != null) {
                    builder.insert(0, name + "."); // NOI18N
                }
            }
            parent = parent.getParent();
        }
        return builder.toString();
    }
    
    public static List<Object> getAllScopeVariables(BpelEntity entity) {
//out();
//out("ALL VAR: " + myValidate);
        List<Object> list = new ArrayList<Object>();
        findVariables(entity, list);
        return list;
    }

    private static void findVariables(BpelEntity entity, List<Object> list) {
//out("  find: " + entity);
        if (entity == null) {
            return;
        }
        if (entity instanceof VariableDeclaration) {
            list.add(((VariableDeclaration) entity).getVariableName());
        }
        List<VariableContainer> containers = entity.getChildren(VariableContainer.class);

        for (VariableContainer container : containers) {
            collectVariables(container, list);
        }
        findVariables(entity.getParent(), list);
    }

    private static void collectVariables(VariableContainer container, List<Object> list) {
        Variable[] variables = container.getVariables();
//out("  container: " + variables.length);

        for (Variable variable : variables) {
            if ( !contains(list, variable)) {
                list.add(variable);
            }
        }
    }

    private static boolean contains(List<Object> list, Variable variable) {
        for (Object element : list) {
            String name;
            
            if (element instanceof Variable) {
                name = ((Variable) element).getName();
            }
            else {
                name = element.toString();
            }
            if (name != null && name.equals(variable.getName())) {
                return true;
            }
        }
        return false;
    }
}
