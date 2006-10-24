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
package org.netbeans.modules.java.editor.codegen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.SourceUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jan Lahoda
 */
public class GeneratorUtils {
    
    private static final ErrorManager ERR = ErrorManager.getDefault().getInstance(GeneratorUtils.class.getName());
    
    private GeneratorUtils() {
    }
    
    public static List<? extends ExecutableElement> findUndefs(CompilationInfo info, TypeElement impl) {
        List<ExecutableElement> undef = new ArrayList<ExecutableElement>();
        List<TypeElement> classes = getAllClasses(impl);
        
        if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
            ERR.log(ErrorManager.INFORMATIONAL, "findUndefs(" + info + ", " + impl + ")");
            ERR.log(ErrorManager.INFORMATIONAL, "classes=" + classes);
        }
        
        for (TypeElement te : getAllParents(impl)) {
            if (te.getModifiers().contains(Modifier.ABSTRACT)) {
                for (Element e : te.getEnclosedElements()) {
                    if (e.getKind() == ElementKind.METHOD && e.getModifiers().contains(Modifier.ABSTRACT)) {
                        ExecutableElement ee = (ExecutableElement)e;
                        
                        if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                            ERR.log(ErrorManager.INFORMATIONAL, "ee=" + ee);
                            ERR.log(ErrorManager.INFORMATIONAL, "isOverriden(info, ee, classes)=" + isOverriden(info, ee, classes));
                        }
                        
                        Element over = SourceUtils.getImplementationOf(info, ee, impl);
                        
                        if (over == null || over == ee) {
                            boolean quit = false;
                            
                            for (ExecutableElement o : undef) {
                                if (o.getSimpleName().toString().contentEquals(ee.getSimpleName())) {
                                    boolean equals = o.getReturnType().equals(ee.getReturnType());
                                    List<? extends VariableElement> op = o.getParameters();
                                    List<? extends VariableElement> eep = ee.getParameters();
                                    
                                    equals &= op.size() == eep.size();
                                    
                                    Iterator<? extends VariableElement> oi = op.iterator();
                                    Iterator<? extends VariableElement> eei = eep.iterator();
                                    
                                    while (equals && oi.hasNext()) {
                                        VariableElement ov = oi.next();
                                        VariableElement eev = eei.next();
                                        
                                        equals &= ov.asType().equals(eev.asType());
                                    }
                                    
                                    quit |= equals;
                                }
                            }
                            
                            if (!quit)
                                undef.add(ee);
                        }
                    }
                }
            }
        }
        
        if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
            ERR.log(ErrorManager.INFORMATIONAL, "undef=" + undef);
        }
        
        return undef;
    }
    
    public static List<? extends ExecutableElement> findOverridable(CompilationInfo info, TypeElement impl) {
        List<ExecutableElement> overridable = new ArrayList<ExecutableElement>();
        List<TypeElement> classes = getAllClasses(impl);
        
        if (ERR.isLoggable(ErrorManager.INFORMATIONAL))
            ERR.log(ErrorManager.INFORMATIONAL, "classes=" + classes);
        
        for (TypeElement te : classes.subList(1, classes.size())) {
            for (ExecutableElement ee : ElementFilter.methodsIn(te.getEnclosedElements())) {
                Set<Modifier> set = EnumSet.copyOf(NOT_OVERRIDABLE);
                
                set.removeAll(ee.getModifiers());
                
                if (set.size() != NOT_OVERRIDABLE.size())
                    continue;
                
                //TODO: cannot override a package private method outside the package
                
                int thisElement = classes.indexOf(te);
                
                if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                    ERR.log(ErrorManager.INFORMATIONAL, "ee=" + ee);
                    ERR.log(ErrorManager.INFORMATIONAL, "thisElement = " + thisElement);
                    ERR.log(ErrorManager.INFORMATIONAL, "classes.subList(0, thisElement + 1)=" + classes.subList(0, thisElement + 1));
                    ERR.log(ErrorManager.INFORMATIONAL, "isOverriden(info, ee, classes.subList(0, thisElement + 1))=" + isOverriden(info, ee, classes.subList(0, thisElement + 1)));
                }
                
                if (!isOverriden(info, ee, classes.subList(0, thisElement + 1))) {
                    overridable.add(ee);
                }
            }
        }
        
        return overridable;
    }

    public static Map<? extends TypeElement, ? extends List<? extends VariableElement>> findAllAccessibleFields(CompilationInfo info, TypeElement clazz) {
        Map<TypeElement, List<? extends VariableElement>> result = new HashMap<TypeElement, List<? extends VariableElement>>();

        result.put(clazz, findAllAccessibleFields(info, clazz, clazz));

        for (TypeElement te : getAllParents(clazz)) {
            result.put(te, findAllAccessibleFields(info, clazz, te));
        }

        return result;
    }

    private static List<? extends VariableElement> findAllAccessibleFields(CompilationInfo info, TypeElement accessibleFrom, TypeElement toScan) {
        List<VariableElement> result = new ArrayList<VariableElement>();

        for (VariableElement ve : ElementFilter.fieldsIn(toScan.getEnclosedElements())) {
            //check if ve is accessible from accessibleFrom:
            if (ve.getModifiers().contains(Modifier.PUBLIC)) {
                result.add(ve);
                continue;
            }
            if (ve.getModifiers().contains(Modifier.PRIVATE)) {
                if (accessibleFrom == toScan)
                    result.add(ve);
                continue;
            }
            if (ve.getModifiers().contains(Modifier.PROTECTED)) {
                if (getAllParents(accessibleFrom).contains(toScan))
                    result.add(ve);
                continue;
            }
            //TODO:package private:
        }

        return result;
    }
    
    public static Collection<TypeElement> getAllParents(TypeElement of) {
        Set<TypeElement> result = new HashSet<TypeElement>();
        
        for (TypeMirror t : of.getInterfaces()) {
            TypeElement te = (TypeElement) ((DeclaredType)t).asElement();
            
            if (te != null) {
                result.add(te);
                result.addAll(getAllParents(te));
            } else {
                if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                    ERR.log(ErrorManager.INFORMATIONAL, "te=null, t=" + t);
                }
            }
        }
        
        TypeMirror sup = of.getSuperclass();
        TypeElement te = sup.getKind() == TypeKind.DECLARED ? (TypeElement) ((DeclaredType)sup).asElement() : null;
        
        if (te != null) {
            result.add(te);
            result.addAll(getAllParents(te));
        } else {
            if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                ERR.log(ErrorManager.INFORMATIONAL, "te=null, t=" + of);
            }
        }
        
        return result;
    }

    public static boolean supportsOverride(FileObject file) {
        return SUPPORTS_OVERRIDE_SOURCE_LEVELS.contains(SourceLevelQuery.getSourceLevel(file));
    }
    
    private static final Set<String> SUPPORTS_OVERRIDE_SOURCE_LEVELS;
    
    static {
        SUPPORTS_OVERRIDE_SOURCE_LEVELS = new HashSet();
        
        SUPPORTS_OVERRIDE_SOURCE_LEVELS.add("1.5");
        SUPPORTS_OVERRIDE_SOURCE_LEVELS.add("1.6");
    }
    
    private static List<TypeElement> getAllClasses(TypeElement of) {
        List<TypeElement> result = new ArrayList<TypeElement>();
        TypeMirror sup = of.getSuperclass();
        TypeElement te = sup.getKind() == TypeKind.DECLARED ? (TypeElement) ((DeclaredType)sup).asElement() : null;
        
        result.add(of);
        
        if (te != null) {
            result.addAll(getAllClasses(te));
        } else {
            if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                ERR.log(ErrorManager.INFORMATIONAL, "te=null, t=" + of);
            }
        }
        
        return result;
    }
    
    private static boolean isOverriden(CompilationInfo info, ExecutableElement methodBase, List<TypeElement> classes) {
        if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
            ERR.log(ErrorManager.INFORMATIONAL, "isOverriden(" + info + ", " + methodBase + ", " + classes + ")");
        }
        
        for (TypeElement impl : classes) {
            for (ExecutableElement methodImpl : ElementFilter.methodsIn(impl.getEnclosedElements())) {
                if (   ERR.isLoggable(ErrorManager.INFORMATIONAL)
                && info.getElements().overrides(methodImpl, methodBase, impl)) {
                    ERR.log(ErrorManager.INFORMATIONAL, "overrides:");
                    ERR.log(ErrorManager.INFORMATIONAL, "impl=" + impl);
                    ERR.log(ErrorManager.INFORMATIONAL, "methodImpl=" + methodImpl);
                }
                
                if (info.getElements().overrides(methodImpl, methodBase, impl))
                    return true;
            }
        }
        
        if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
            ERR.log(ErrorManager.INFORMATIONAL, "no overriding methods overrides:");
        }
        
        return false;
    }

    private static final Set<Modifier> NOT_OVERRIDABLE = /*EnumSet.noneOf(Modifier.class);/*/EnumSet.of(Modifier.ABSTRACT, Modifier.STATIC, Modifier.FINAL);

    public static boolean isAccessible(TypeElement from, Element what) {
        if (what.getModifiers().contains(Modifier.PUBLIC))
            return true;

        TypeElement fromTopLevel = SourceUtils.getOutermostEnclosingTypeElement(from);
        TypeElement whatTopLevel = SourceUtils.getOutermostEnclosingTypeElement(what);

        if (fromTopLevel.equals(whatTopLevel))
            return true;

        if (what.getModifiers().contains(Modifier.PRIVATE))
            return false;

        if (what.getModifiers().contains(Modifier.PROTECTED)) {
            if (getAllClasses(fromTopLevel).contains(SourceUtils.getEnclosingTypeElement(what)))
                return true;
        }

        //package private:
        return ((PackageElement) fromTopLevel.getEnclosingElement()).getQualifiedName().toString().contentEquals(((PackageElement) whatTopLevel.getEnclosingElement()).getQualifiedName());
    }
    
}
