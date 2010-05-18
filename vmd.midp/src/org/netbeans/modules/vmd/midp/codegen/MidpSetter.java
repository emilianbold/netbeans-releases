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
package org.netbeans.modules.vmd.midp.codegen;

import org.netbeans.modules.vmd.api.codegen.*;
import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.Versionable;
import org.netbeans.modules.vmd.midp.components.MidpTypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author David Kaspar
 */
public class MidpSetter implements Setter {

    private TypeID constructorRelatedTypeID;
    private String instanceNameSuffix;
    private Versionable versionable;

    private String arrayParameterName;
    private ArrayList<String> parameterNames = new ArrayList<String> ();
    private ArrayList<String> exceptions = new ArrayList<String> ();

    private MidpSetter (TypeID constructorRelatedTypeID, String instanceNameSuffix, Versionable versionable) {
        this.constructorRelatedTypeID = constructorRelatedTypeID;
        this.instanceNameSuffix = instanceNameSuffix;
        this.versionable = versionable;
    }

    public MidpSetter setArrayParameter (String arrayParameterName) {
        this.arrayParameterName = arrayParameterName;
        return this;
    }

    public MidpSetter addParameters (String... parameterNames) {
        this.parameterNames.addAll (Arrays.asList (parameterNames));
        return this;
    }

    public MidpSetter addExceptions (String... exceptions) {
        this.exceptions.addAll (Arrays.asList (exceptions));
        return this;
    }

    public static MidpSetter createConstructor (TypeID constructorRelatedTypeID, Versionable versionable) {
        return new MidpSetter (constructorRelatedTypeID, " = new " + MidpTypes.getSimpleClassName (constructorRelatedTypeID), versionable); // NOI18N
    }

    public static MidpSetter createFactoryMethod (TypeID constructorRelatedTypeID, String classCast, String className, String methodName, Versionable versionable) {
        return new MidpSetter (constructorRelatedTypeID, " = " + (classCast != null ? ("(" + classCast + ") ") : "") + className + "." + methodName, versionable); // NOI18N
    }

    public static MidpSetter createSetter (String setterName, Versionable versionable) {
        return new MidpSetter (null, "." + setterName, versionable); // NOI18N
    }

    public boolean isConstructor () {
        return constructorRelatedTypeID != null;
    }

    public TypeID getConstructorRelatedTypeID () {
        return constructorRelatedTypeID;
    }

    public int getPriority () {
        return 0;
    }

    public String getSetterName () {
        return instanceNameSuffix;
    }

    public Versionable getVersionable () {
        return versionable;
    }

    public void generateSetterCode (MultiGuardedSection section, DesignComponent component, Map<String, Parameter> name2parameter) {
        if (! exceptions.isEmpty ())
            section.getWriter ().write ("try {\n"); // NOI18N

        if (arrayParameterName != null) {
            Parameter arrayParameter = name2parameter.get (arrayParameterName);
            final int count = arrayParameter.getCount (component);
            for (int index = 0; index < count; index ++) {
                if (! arrayParameter.isRequiredToBeSet (component, index))
                    continue;
                section.getWriter ().write (CodeReferencePresenter.generateDirectAccessCode (component));
                section.getWriter ().write (instanceNameSuffix);
                section.getWriter ().write (" ("); // NOI18N
                for (int paramIndex = 0; paramIndex < parameterNames.size (); paramIndex ++) {
                    if (paramIndex > 0)
                        section.getWriter ().write (", "); // NOI18N
                    String parameterName = parameterNames.get (paramIndex);
                    Parameter parameter = name2parameter.get (parameterName);
                    parameter.generateParameterCode (component, section, index);
                }
                section.getWriter ().write (");\n"); // NOI18N
            }
        } else {
            section.getWriter ().write (CodeReferencePresenter.generateDirectAccessCode (component));
            section.getWriter ().write (instanceNameSuffix);
            section.getWriter ().write (" ("); // NOI18N
            for (int paramIndex = 0 ; paramIndex < parameterNames.size (); paramIndex ++) {
                if (paramIndex > 0)
                    section.getWriter ().write (", "); // NOI18N
                String parameterName = parameterNames.get (paramIndex);
                Parameter parameter = name2parameter.get (parameterName);
                parameter.generateParameterCode (component, section, -1);
            }
            section.getWriter ().write (");\n"); // NOI18N
        }

        if (! exceptions.isEmpty ()) {
            for (String exception : exceptions) {
                section.getWriter ().write ("} catch (" + exception + " e) {\n").commit (); // NOI18N
                section.switchToEditable (component.getComponentID () + "-@" + exception); // NOI18N
                section.getWriter ().write ("e.printStackTrace ();\n").commit (); // NOI18N
                section.switchToGuarded ();
            }
            section.getWriter ().write ("}\n"); // NOI18N
        }
    }

    public List<String> getParameters () {
        return parameterNames;
    }

    public List<String> getExceptions () {
        return exceptions;
    }

}
