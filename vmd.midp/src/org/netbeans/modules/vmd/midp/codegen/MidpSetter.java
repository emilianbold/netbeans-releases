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
