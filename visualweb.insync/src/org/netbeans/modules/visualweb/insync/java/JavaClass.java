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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.visualweb.insync.java;

import com.sun.rave.designtime.ContextMethod;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.modules.visualweb.insync.beans.Naming;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectConstants;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;
import org.openide.filesystems.FileObject;
import org.netbeans.api.java.classpath.ClassPath;

/**
 *
 * @author jdeva
 */
public class JavaClass {
    ElementHandle<TypeElement> typeElementHandle;
    //JavaUnit javaUnit;    //To obtain FileObject/JavaSource
    FileObject fObj;        //Temporary till we plug this into insync
    String name;
    
    /** Creates a new instance of TypeElementAdapter */
    public JavaClass(TypeElement element, FileObject fObj) {
        this.fObj = fObj;
        typeElementHandle = ElementHandle.create(element);
        name = element.getQualifiedName().toString();
    }
    
    /*
     * Returns short name for the class
     */ 
    public String getShortName() {
        return name == null ? null : name.substring(name.lastIndexOf('.')+1);
    }
    
    /*
     * Returns FQN for the class
     */     
    public String getName() {
        return name;
    }
    
    /*
     * Checks if the passed in type as string is a super type of this class
     */ 
    public boolean isSubTypeOf(final String typeName) {
         Boolean result = (Boolean)ReadTaskWrapper.execute( new ReadTaskWrapper.Read() {
            public Object run(CompilationInfo cinfo) {
                TypeElement typeElement = typeElementHandle.resolve(cinfo);
                TypeMirror superType = cinfo.getElements().getTypeElement(typeName).asType();
                if(superType.getKind() == TypeKind.DECLARED &&
                   cinfo.getTypes().isSubtype(typeElement.asType(), superType)) {
                        return Boolean.TRUE;
                }
                return Boolean.FALSE;
            }
        }, fObj); 
        return result.booleanValue();
    }    
    
    /*
     * Return all the methods
     */
    public List<ExecutableElement> getMethods() {
        return getMethods(null, null, null);
    }
    
    /*
     * Return all methods that has same return type and parameter types as 
     * specified by arguments
     */ 
    public List<String> getMethodNames(Class[] params, Class retType) {
        List<String> names = new ArrayList<String>();
        for(ExecutableElement method : getMethods(null, params, retType)) {
            names.add(method.getSimpleName().toString());
        }
        return names;
    }    
    
    /*
     * For all the properties found in a class, its names and types are returned in a hashmap
     */    
    public HashMap<String, String> getPropertiesNamesAndTypes() {
        HashMap<String, String> types = (HashMap<String, String>)ReadTaskWrapper.execute( new ReadTaskWrapper.Read() {
            public Object run(CompilationInfo cinfo) {
                TypeElement typeElement = typeElementHandle.resolve(cinfo);
                HashMap<String, String> types = new HashMap<String, String>();
                for(ExecutableElement method : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
                    if(isBeanGetter(method)) {
                        TypeMirror type = method.getReturnType();
                        // In case of array get the component type;
                        if(type.getKind() == TypeKind.ARRAY) {
                            type = ((ArrayType)type).getComponentType();
                        }
                        String typeName = type.toString();
                        // In case of parameterized type, use the raw type
                        if(isParameterizedType(type)) {
                            typeName = cinfo.getTypes().erasure(type).toString();
                        }
                        String name = Naming.propertyName(method.getSimpleName().toString(), typeName.equals("boolean"));
                        types.put(name, typeName);
                    }
                }
                return types;
            }
        }, fObj);
        return types;
    }
    
    /* 
     * Returns a handle corresponding to a field
     */
    public ElementHandle getField(final String name) {
        return (ElementHandle)ReadTaskWrapper.execute( new ReadTaskWrapper.Read() {
            public Object run(CompilationInfo cinfo) {
                VariableElement field = getField(cinfo, name);
                if(field != null) {
                    return ElementHandle.create(field);
                }
                return null;
            }
        }, fObj);
    }
    
    /*
     * Inserts a field, getter and setter given the property name and type, boolean 
     * flags to control the addition of getter/setter
     */
    public void addProperty(final String name, final Class type, final boolean getter, final boolean setter) {
        WriteTaskWrapper.execute( new WriteTaskWrapper.Write() {
            public Object run(WorkingCopy wc) {
                TreeMaker make = wc.getTreeMaker();
                TypeElement typeElement = typeElementHandle.resolve(wc);
                ClassTree ctree = wc.getTrees().getTree(typeElement);
                ClassTree newctree = ctree;
                TreeMakerUtils utils = new TreeMakerUtils(wc);
                VariableTree vtree = utils.createPropertyField(name, type);
                
                // Find the constructor
                // TBD: index selection logic, i.e where to add the field, getter and setter
                // For now insert before the ctor
                Tree ctor = getPublicConstructor(wc, ctree);
                newctree = make.insertClassMember(newctree, ctree.getMembers().indexOf(ctor), vtree);
                MethodTree mtree = null;
                if(getter) {
                    mtree = utils.createPropertyGetterMethod(name, type);
                    newctree = make.insertClassMember(newctree, newctree.getMembers().indexOf(ctor), mtree);
                }
                if(setter) {
                    mtree = utils.createPropertySetterMethod(name, type);
                    newctree = make.insertClassMember(newctree, newctree.getMembers().indexOf(ctor), mtree);
                }
                wc.rewrite(ctree, newctree);
                return null;
            }
        }, fObj);    
    }
    
    /*
     * Deletes a field, getter and setter given the property name and type, boolean 
     * flags to control the addition of getter/setter
     */    
    public void removeProperty(final String name) {
        WriteTaskWrapper.execute( new WriteTaskWrapper.Write() {
            public Object run(WorkingCopy wc) {
                TreeMaker make = wc.getTreeMaker();
                TypeElement typeElement = typeElementHandle.resolve(wc);
                ClassTree ctree = wc.getTrees().getTree(typeElement);
                ClassTree newctree = ctree;
                VariableElement varElem = getField(wc, name);
                if(varElem != null) {
                    newctree = make.removeClassMember(ctree, wc.getTrees().getTree(varElem));
                }
                ExecutableElement getElem = getMethod(wc, Naming.getterName(name), new Class[0]);
                TypeMirror type = null;
                if(getElem != null) {
                    type = getElem.getReturnType();
                    newctree = make.removeClassMember(newctree, wc.getTrees().getTree(getElem));
                    ExecutableElement setElem = getMethod(wc, Naming.setterName(name), Collections.<TypeMirror>singletonList(type));
                    if(setElem != null) {
                        newctree = make.removeClassMember(newctree, wc.getTrees().getTree(setElem));
                    }
                }
                wc.rewrite(ctree, newctree);
                return null;
            }
        }, fObj);    
    }
    
    /*
     * Renames field, getter and setter and its usage given the property new and old names
     */    
    public void renameProperty(final String name, final String newName) {
        WriteTaskWrapper.execute( new WriteTaskWrapper.Write() {
            public Object run(WorkingCopy wc) {
                TypeElement typeElement = typeElementHandle.resolve(wc);
                ClassTree ctree = wc.getTrees().getTree(typeElement);
                List<Element> elemsToRename = new ArrayList<Element>();
                Refactor.ElementRenamer elementRenamer = null;
                VariableElement varElem = getField(wc, name);
                if(varElem != null) {
                    elementRenamer = new Refactor.ElementRenamer(wc, newName);
                    elementRenamer.scan(wc.getCompilationUnit(), varElem);
                }
                ExecutableElement getElem = getMethod(wc, Naming.getterName(name), new Class[0]);
                if(getElem != null) {
                    elementRenamer = new Refactor.ElementRenamer(wc, Naming.getterName(newName));
                    elementRenamer.scan(wc.getCompilationUnit(), getElem);
                    TypeMirror type = getElem.getReturnType();
                    ExecutableElement setElem = getMethod(wc, Naming.setterName(name), Collections.<TypeMirror>singletonList(type));
                    if(setElem != null) {
                        elementRenamer = new Refactor.ElementRenamer(wc, Naming.setterName(newName));
                        elementRenamer.scan(wc.getCompilationUnit(), setElem);
                    }
                }
                //To take care of VB expressions, Ex:- getValue(#{SessionBean1.personRowSet})                
                //renamePropertyBindingExpression(wc, name, newName);
                return null;
            }
        }, fObj);    
    }
    
    public void renamePropertyBindingExpression(WorkingCopy wc, String name, String newName) {
        String oldLiteral = "#{" + getShortName() + "." + name + "}"; //NOI18N
        String newLiteral = "#{" + getShortName() + "." + newName + "}"; //NOI18N
        new Refactor.LiteralRenamer(wc, oldLiteral, newLiteral).scan(wc.getCompilationUnit(), null);
    }
    
    /*
     *  Internal method to add a method, returns element handle which can be cached by the caller
     */ 
    private ElementHandle<ExecutableElement> addMethod(final ContextMethod cm, final String retType) {
        return (ElementHandle<ExecutableElement>)WriteTaskWrapper.execute( new WriteTaskWrapper.Write() {
            public Object run(WorkingCopy wc) {
                ExecutableElement elem = getMethod(wc, cm.getName(), cm.getParameterTypes());
                if(elem == null) {
                    TreeMaker make = wc.getTreeMaker();
                    TypeElement typeElement = typeElementHandle.resolve(wc);
                    TreeMakerUtils utils = new TreeMakerUtils(wc);
                    ClassTree ctree = wc.getTrees().getTree(typeElement);
                    ClassTree newctree = ctree;
                    MethodTree mtree = utils.createMethod(cm, retType);
                    newctree = make.addClassMember(ctree, mtree);
                    wc.rewrite(ctree, newctree);
                    elem = (ExecutableElement)wc.getTrees().getElement(TreeUtils.getTreePath(wc, mtree));
                }
                return ElementHandle.create(elem);
            }
        }, fObj);            
    }
    
   public ElementHandle<ExecutableElement> addMethod(ContextMethod cm) {
        String retTypeName = null;
        if(cm.getReturnType() != null) {
            retTypeName = cm.getReturnType().getCanonicalName();
        }
        return addMethod(cm, retTypeName);
    }
    

    public ElementHandle<ExecutableElement> addMethod(MethodInfo mInfo) {
        return addMethod(mInfo, mInfo.getReturnTypeName());
    }
    
    /*
     * Removes a method corresponding to passed in element handle
     */     
    public void removeMethod(final ElementHandle<ExecutableElement> methodElementHandle) {
         WriteTaskWrapper.execute( new WriteTaskWrapper.Write() {
            public Object run(WorkingCopy wc) {
                ExecutableElement execElement = methodElementHandle.resolve(wc);
                TypeElement typeElement = typeElementHandle.resolve(wc);
                ClassTree ctree = wc.getTrees().getTree(typeElement);
                ClassTree newctree = ctree;
                newctree = wc.getTreeMaker().removeClassMember(ctree, wc.getTrees().getTree(execElement));
                wc.rewrite(ctree, newctree);
                return null;
            }
        }, fObj);        
    }    
    
    /*
     * Returns the first public constructor
     */ 
    public MethodTree getPublicConstructor(CompilationInfo cinfo, ClassTree ctree) {
        for(Tree tree : ctree.getMembers()) {
            if(Tree.Kind.METHOD == tree.getKind()) {
                MethodTree mtree = (MethodTree)tree;
                if(mtree.getName().toString().equals("<init>") &&
                        !cinfo.getTreeUtilities().isSynthetic(TreeUtils.getTreePath(cinfo, ctree))) {
                    return mtree;
                }
            }
        }
        return null;
    }
    
    /*
     * Returns a element handle corresponding to a method by given name and parameter types
     */     
    public ElementHandle<ExecutableElement> getMethod(final String name, final Class[] params) {
        return (ElementHandle<ExecutableElement>)ReadTaskWrapper.execute( new ReadTaskWrapper.Read() {
            public Object run(CompilationInfo cinfo) {
                return ElementHandle.<ExecutableElement>create(getMethod(cinfo, name, params));
            }
        }, fObj);
    }
    
    /*
     * Returns a element corresponding to a method by given name and parameter types
     */     
    private ExecutableElement getMethod(CompilationInfo cinfo, String name, Class[] params) {
        List<ExecutableElement> methods = getMethods(cinfo, name, params, null);
        return (methods.size() == 1) ? methods.get(0) : null;
    }
    
    /*
     * Returns a element corresponding to a method by given name and parameter types(TypeMirror)
     */
    private ExecutableElement getMethod(CompilationInfo cinfo, String name, List<? extends TypeMirror> paramTypes) {
        TypeElement typeElement = typeElementHandle.resolve(cinfo);
        for(ExecutableElement method : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
            if( (name == null || method.getSimpleName().toString().equals(name)) &&
                    (paramTypes == null || matchTypes(cinfo, method.getParameters(), paramTypes))) {
                return method;
            }
        }
        return null;
    }    
    
    /*
     *  Returns all methods that match the passed in name, parameter types and return type
     */ 
    private List<ExecutableElement> getMethods(final String name, final Class[] params, final Class retType) {
         return (List<ExecutableElement>)ReadTaskWrapper.execute( new ReadTaskWrapper.Read() {
            public Object run(CompilationInfo cinfo) {
                return getMethods(cinfo, name, params, retType);
            }
        }, fObj);
     }
    
    /*
     * Returns all methods that match the passed in name, parameter types and return type
     */ 
    private List<ExecutableElement> getMethods(CompilationInfo cinfo, String name, Class[] params, Class retType) {
        List<ExecutableElement> methods = new ArrayList<ExecutableElement>();
        TypeElement typeElement = typeElementHandle.resolve(cinfo);
        for(ExecutableElement method : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
            if( (name == null || method.getSimpleName().toString().equals(name)) &&
                    (params == null || matchTypes(cinfo, method.getParameters(), params)) &&
                    (retType == null || matchType(cinfo, method.getReturnType(), retType))) {
                methods.add(method);
            }
        }
        return methods;
    }
    
    /*
     * Returns a element corresponding to a field by given name
     */ 
    private VariableElement getField(CompilationInfo cinfo, String name) {
        TypeElement typeElement = typeElementHandle.resolve(cinfo);
        for(VariableElement var : ElementFilter.fieldsIn(typeElement.getEnclosedElements())) {
            if(var.getSimpleName().toString().equals(name)) {
                return var;
            }
        }
        return null;
    }    
    
    /*
     * Returns true if the type matches cls type
     */    
    private boolean matchType(CompilationInfo cinfo, TypeMirror type, Class cls) {
        String typeName = type.toString();
        //Use the raw type if the type represents paramaterized type
        TypeMirror compType = type;
        if(type.getKind() == TypeKind.ARRAY) {
            compType = ((ArrayType)type).getComponentType();
        }
        if(isParameterizedType(compType)) {
            typeName = cinfo.getTypes().erasure(type).toString();
        }
        if(cls.getCanonicalName().equals(typeName)) {
            return true;
        }
        return false;
    }
    
    /*
     * Returns true if the variable elements types matches params types
     */
    private boolean matchTypes(CompilationInfo cinfo, List<? extends VariableElement> varElems, Class[] params) {
        if(varElems.size() == params.length) {
            int i = 0;
            for(VariableElement varElem : varElems) {
                if(!matchType(cinfo, varElem.asType(), params[i++])) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    
    /*
     * Returns true if the variable elements types matches params types
     */
    private boolean matchTypes(CompilationInfo cinfo, List<? extends VariableElement> varElems, List<? extends TypeMirror> paramTypes) {
        if(varElems.size() == paramTypes.size()) {
            ListIterator<? extends VariableElement> elemsIter = varElems.listIterator();
            ListIterator<? extends TypeMirror> typesIter = paramTypes.listIterator();
            while(elemsIter.hasNext()) {
                if(!cinfo.getTypes().isSameType(elemsIter.next().asType(), typesIter.next())) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }    
    
    /*
     * Returns true if the supplied method qualifies as a bean getter method
     */
    private boolean isBeanGetter(ExecutableElement method) {
        Set<Modifier> mods = method.getModifiers();
        // Must be public and can't be abstract or static
        if(!mods.contains(Modifier.PUBLIC) || 
            mods.contains(Modifier.STATIC) || 
            mods.contains(Modifier.ABSTRACT)) {
            return false;
        }
        
        // Must have zero parameters
        if(method.getParameters().size() > 0) {
            return false;
        }
        
        // Return type should be non void
        TypeMirror type = method.getReturnType();
        if(type.getKind() == TypeKind.VOID) {
            return false;
        }
        
        // Check if it is a valid getter name
        String name = Naming.propertyName(method.getSimpleName().toString(), 
                type.toString().equals("boolean"));
        if (name == null)
            return false;
        
        return true;
    }
    
    /*
     * Returns true if the type represents a parameterized type
     */ 
    private boolean isParameterizedType(TypeMirror type) {
        if(type.getKind() == TypeKind.DECLARED) {
            DeclaredType declType = (DeclaredType)type;
            if(declType.getTypeArguments().size() > 0) {
                return true;
            }
        }
        return false;
    }
    
    /*
     * Returns the TypeElement wrapper for public class in the given file
     */
    public static JavaClass getJavaClass(final FileObject fObj) {
        return (JavaClass)ReadTaskWrapper.execute(new ReadTaskWrapper.Read() {
            public Object run(CompilationInfo cinfo) {
                String pkgName = JsfProjectUtils.getProjectProperty(FileOwnerQuery.getOwner(fObj), 
                        JsfProjectConstants.PROP_JSF_PAGEBEAN_PACKAGE);
                TypeElement element = cinfo.getElements().getTypeElement(pkgName + "." + fObj.getName());
                return new JavaClass(element, fObj);
            }
        }, fObj);        
    }
}
