/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.vmd.midp.components.items;

import org.netbeans.modules.vmd.api.codegen.*;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.Versionable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.vmd.api.model.PropertyValue;

final class MidpSetterItemCommandListener implements Setter {

    private TypeID constructorRelatedTypeID;
    private String instanceNameSuffix;
    private Versionable versionable;
    private String arrayParameterName;
    private ArrayList<String> parameterNames = new ArrayList<String>();
    private ArrayList<String> exceptions = new ArrayList<String>();

    private MidpSetterItemCommandListener(TypeID constructorRelatedTypeID, String instanceNameSuffix, Versionable versionable) {
        this.constructorRelatedTypeID = constructorRelatedTypeID;
        this.instanceNameSuffix = instanceNameSuffix;
        this.versionable = versionable;
    }

    public static MidpSetterItemCommandListener createSetter(String setterName, Versionable versionable) {
        return new MidpSetterItemCommandListener(null, "." + setterName, versionable); // NOI18N
    }

    public MidpSetterItemCommandListener addParameters(String... parameterNames) {
        this.parameterNames.addAll(Arrays.asList(parameterNames));
        return this;
    }

    @Override
    public boolean isConstructor() {
        return constructorRelatedTypeID != null;
    }

    @Override
    public TypeID getConstructorRelatedTypeID() {
        return constructorRelatedTypeID;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public String getSetterName() {
        return instanceNameSuffix;
    }

    @Override
    public Versionable getVersionable() {
        return versionable;
    }

    @Override
    public void generateSetterCode(MultiGuardedSection section, final DesignComponent component, Map<String, Parameter> name2parameter) {
        if (!exceptions.isEmpty()) {
            section.getWriter().write("try {\n"); // NOI18N
        }
        final List[] value = new List[1];
        component.getDocument().getTransactionManager().readAccess(new Runnable() {

            @Override
            public void run() {
                value[0] = component.readProperty(ItemCD.PROP_COMMANDS).getArray();
            }
        });
        List<PropertyValue> array = value[0];
        if (arrayParameterName != null) {
            Parameter arrayParameter = name2parameter.get(arrayParameterName);
            final int count = arrayParameter.getCount(component);
            for (int index = 0; index < count; index++) {
                if (!arrayParameter.isRequiredToBeSet(component, index)) {
                    continue;
                }
                section.getWriter().write(CodeReferencePresenter.generateDirectAccessCode(component));
                section.getWriter().write(instanceNameSuffix);
                section.getWriter().write(" ("); // NOI18N
                for (int paramIndex = 0; paramIndex < parameterNames.size(); paramIndex++) {
                    if (paramIndex > 0) {
                        section.getWriter().write(", "); // NOI18N
                    }
                    String parameterName = parameterNames.get(paramIndex);
                    Parameter parameter = name2parameter.get(parameterName);
                    parameter.generateParameterCode(component, section, index);
                }
                section.getWriter().write(");\n"); // NOI18N
            }
        } else if (array != null && array.size() > 0) {
            section.getWriter().write(CodeReferencePresenter.generateDirectAccessCode(component));
            section.getWriter().write(instanceNameSuffix);
            section.getWriter().write(" ("); // NOI18N
            for (int paramIndex = 0; paramIndex < parameterNames.size(); paramIndex++) {
                if (paramIndex > 0) {
                    section.getWriter().write(", "); // NOI18N
                }
                String parameterName = parameterNames.get(paramIndex);
                Parameter parameter = name2parameter.get(parameterName);
                parameter.generateParameterCode(component, section, -1);
            }
            section.getWriter().write(");\n"); // NOI18N
        }

        if (!exceptions.isEmpty()) {
            for (String exception : exceptions) {
                section.getWriter().write("} catch (" + exception + " e) {\n").commit(); // NOI18N
                section.switchToEditable(component.getComponentID() + "-@" + exception); // NOI18N
                section.getWriter().write("e.printStackTrace ();\n").commit(); // NOI18N
                section.switchToGuarded();
            }
            section.getWriter().write("}\n"); // NOI18N
        }
    }

    @Override
    public List<String> getParameters() {
        return parameterNames;
    }

    public List<String> getExceptions() {
        return exceptions;
    }
}
