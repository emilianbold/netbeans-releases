/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.cnd.mixeddev.java.model;

import java.util.List;

/**
 *
 * @author Petr Kudryavtsev <petrk@netbeans.org>
 */
public final class JavaMethodInfo implements JavaEntityInfo {
    
    private final CharSequence name;
    
    private final List<QualifiedNamePart> fullQualifiedName;
    
    private final List<JavaTypeInfo> parameters;
    
    private final JavaTypeInfo returnType;
    
    private final boolean overloaded;

    public JavaMethodInfo(CharSequence name, List<QualifiedNamePart> fullQualifiedName, List<JavaTypeInfo> parameters, JavaTypeInfo returnType, boolean overloaded) {
        this.name = name;
        this.fullQualifiedName = fullQualifiedName;
        this.parameters = parameters;
        this.returnType = returnType;
        this.overloaded = overloaded;
    }
    
    public CharSequence getName() {
        return name;
    }    

    public List<QualifiedNamePart> getFullQualifiedName() {
        return fullQualifiedName;
    }    

    public List<JavaTypeInfo> getParameters() {
        return parameters;
    }

    public JavaTypeInfo getReturnType() {
        return returnType;
    }

    public boolean isOverloaded() {
        return overloaded;
    }
}
