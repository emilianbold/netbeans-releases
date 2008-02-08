/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.visualweb.insync.java;

import com.sun.rave.designtime.ContextMethod;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
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
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.visualweb.insync.beans.Bean;
import org.netbeans.modules.visualweb.insync.beans.BeansUnit;
import org.netbeans.modules.visualweb.insync.beans.Naming;
import org.netbeans.modules.visualweb.insync.faces.HtmlBean;
import org.openide.filesystems.FileObject;

/**
 *
 * @author jdeva
 */
public class JavaClass {
    private ElementHandle<TypeElement> typeElementHandle;
    //JavaUnit javaUnit;    //To obtain FileObject/JavaSource
    private FileObject fObj;        //Temporary till we plug this into insync
    private String name;

    private enum MethodKind {
        NORMAL,
        DELEGATOR,
        EVENT
    }
    
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
    
    public String getPackageName() {
        return name == null ? null : name.substring(0, name.lastIndexOf('.'));
    }
    
    /*
     * Return the file which contains this java class
     */     
    public FileObject getFileObject() {
        return fObj;
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
    public List<Method> getMethods() {
         return (List<Method>)ReadTaskWrapper.execute( new ReadTaskWrapper.Read() {
            public Object run(CompilationInfo cinfo) {
                List<Method> methods = new ArrayList<Method>();
                for(ExecutableElement method : getMethods(cinfo, null, null, null)) {
                    methods.add(new Method(method, JavaClass.this));
                }
                return methods;
            }
        }, fObj);        
    }
    
    /*
     * Return all methods that has same return type and parameter types as 
     * specified by arguments
     */ 
    public List<String> getMethodNames(final Class[] params, final Class retType) {
        return (List<String>)ReadTaskWrapper.execute( new ReadTaskWrapper.Read() {
            public Object run(CompilationInfo cinfo) {
                List<String> names = new ArrayList<String>();
                for(ExecutableElement method : getMethods(cinfo, null, params, retType)) {
                    names.add(method.getSimpleName().toString());
                }
                return names;
            }
        }, fObj);        
    }    
    
    /**
     * Return map of properties name and type information for all the properties 
     * The value entry of map is a list of strings, the first item is the property's type and 
     * the subsequent ones are the type parameters only in case of parameterized type.
     */    
    public HashMap<String, List<String>> getPropertiesNameAndTypes() {
        return (HashMap<String, List<String>>)ReadTaskWrapper.execute( new ReadTaskWrapper.Read() {
            public Object run(CompilationInfo cinfo) {
                TypeElement typeElement = typeElementHandle.resolve(cinfo);
                HashMap<String, List<String>> nameAndtypes = new HashMap<String, List<String>>();
                for(ExecutableElement method : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
                    if(isBeanGetter(method)) {
                        TypeMirror type = method.getReturnType();
                        String typeName = type.toString();
                        List<String> typeNames = new ArrayList<String>();

                        // In case of parameterized type, use the raw type
                        if(isParameterizedType(type)) {
                            typeName = cinfo.getTypes().erasure(type).toString();
                            addTypeParameters(type, typeNames);
                        }
                        typeNames.add(0, typeName);
                        String name = Naming.propertyName(method.getSimpleName().toString(), typeName.equals("boolean"));
                        nameAndtypes.put(name, typeNames);
                    }
                }
                return nameAndtypes;
            }
        }, fObj);
    }
   
    /**
     * Extracts type parameters for a paramterized type recursively
     */
    private void addTypeParameters(TypeMirror type, List<String> argTypeNames) {
        if(type.getKind() == TypeKind.DECLARED) {
            for(TypeMirror argType : ((DeclaredType)type).getTypeArguments()) {
                if(isParameterizedType(argType)) {
                    addTypeParameters(argType, argTypeNames);
                }else {
                    argTypeNames.add(argType.toString());
                }
            }
        }
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
    private ClassTree addProperty(final String name, final Class type, final boolean getter, 
            final boolean setter, ClassTree ctree, Tree tree, WorkingCopy wc) {
        TreeMaker make = wc.getTreeMaker();
        ClassTree newctree = ctree;
        VariableTree vtree = TreeMakerUtils.createPropertyField(wc, name, type);

        newctree = make.insertClassMember(newctree, ctree.getMembers().indexOf(tree), vtree);
        MethodTree mtree = null;
        if (getter) {
            mtree = TreeMakerUtils.createPropertyGetterMethod(wc, name, type);
            newctree = make.insertClassMember(newctree, newctree.getMembers().indexOf(tree), mtree);
        }
        if (setter) {
            mtree = TreeMakerUtils.createPropertySetterMethod(wc, name, type);
            newctree = make.insertClassMember(newctree, newctree.getMembers().indexOf(tree), mtree);
        }
        return newctree;
    }
    
    /*
     * Deletes a field, getter and setter given the property name and type, boolean 
     * flags to control the addition of getter/setter
     */    
    private ClassTree removeProperty(final String name, ClassTree ctree, WorkingCopy wc) {
        TreeMaker make = wc.getTreeMaker();
        ClassTree newctree = ctree;
        VariableElement varElem = getField(wc, name);
        if (varElem != null) {
            newctree = make.removeClassMember(ctree, wc.getTrees().getTree(varElem));
        }
        ExecutableElement getElem = getMethod(wc, Naming.getterName(name), new Class[0]);
        TypeMirror type = null;
        if (getElem != null) {
            type = getElem.getReturnType();
            newctree = make.removeClassMember(newctree, wc.getTrees().getTree(getElem));
            ExecutableElement setElem = getMethod(wc, Naming.setterName(name), Collections.<TypeMirror>singletonList(type));
            if (setElem != null) {
                newctree = make.removeClassMember(newctree, wc.getTrees().getTree(setElem));
            }
        }
        return newctree;
    }
    
    public void addBeans(final List<Bean> beans) {
        WriteTaskWrapper.execute(new WriteTaskWrapper.Write() {
            public Object run(WorkingCopy wc) {
                TypeElement typeElement = typeElementHandle.resolve(wc);
                ClassTree ctree = wc.getTrees().getTree(typeElement);
                ClassTree oldTree = ctree;
                // Find the constructor
                // TBD: index selection logic, i.e where to add the field, getter and setter
                // For now insert before the ctor
                Tree ctor = getPublicConstructor(wc, ctree);
                BeansUnit beansUnit = null;
                BlockTree blockTree = null;
                for (Bean bean : beans) {
                    ctree = addProperty(bean.getName(), bean.getType(), 
                            bean.isGetterRequired(), bean.isSetterRequired(), ctree, ctor, wc);
                    beansUnit = bean.getUnit();
                    if(beansUnit != null) {
                        blockTree = beansUnit.getPropertiesInitMethod().addPropertySetStatements(wc, bean, blockTree);
                    }
                    bean.setInserted(true);
                }
                if(beansUnit != null) {
                    beansUnit.getCleanupMethod().addCleanupStatements(wc, beans);
                }
                if(oldTree != ctree) {
                    wc.rewrite(oldTree, ctree);
                }
                return null;
            }
        }, fObj);
    }

    public void removeBeans(final List<Bean> beans) {
        WriteTaskWrapper.execute(new WriteTaskWrapper.Write() {
            public Object run(WorkingCopy wc) {
                TypeElement typeElement = typeElementHandle.resolve(wc);
                ClassTree ctree = wc.getTrees().getTree(typeElement);
                ClassTree oldTree = ctree;
                BeansUnit beansUnit = null;
                BlockTree blockTree = null;
                for (Bean bean : beans) {
                    if(!(bean instanceof HtmlBean)) {
                        ctree = removeProperty(bean.getName(), ctree, wc);
                        beansUnit = bean.getUnit();
                        if (beansUnit != null) {
                            blockTree = beansUnit.getPropertiesInitMethod().removeSetStatements(wc, bean, blockTree);
                        }
                        bean.setInserted(false);
                    }
                }
                if(beansUnit != null) {
                   beansUnit.getCleanupMethod().removeCleanupStatements(wc, beans);
                }
                if(oldTree != ctree) {
                    wc.rewrite(oldTree, ctree);
                }
                return null;
            }
        }, fObj);
    }
    
    /*
     * Renames field, getter and setter and its usage given the property new & old names, and
     * the list of files where the property could be used
     */    
    public void renameProperty(final String name, final String newName, final List<FileObject> fObjs) {
        final HashMap<ElementHandle, String> elementAndNames = getElementHandlesToReplace(name, newName);
        WriteTaskWrapper.execute( new WriteTaskWrapper.Write() {
            public Object run(WorkingCopy wc) {
                new Refactor.ElementsRenamer(wc, elementAndNames).scan(wc.getCompilationUnit(), null);
                //To take care of VB expressions, Ex:- getValue(#{SessionBean1.personRowSet})
                renamePropertyBindingExpression(wc, name, newName);
                return null;
            }
        }, fObjs);
    }
    
    private HashMap<ElementHandle, String> getElementHandlesToReplace(final String name, final String newName) {
        return (HashMap<ElementHandle, String>)ReadTaskWrapper.execute( new ReadTaskWrapper.Read() {
            public Object run(CompilationInfo cinfo) {
                final HashMap<ElementHandle, String> elementAndNames = new HashMap<ElementHandle, String>();
                VariableElement varElem = getField(cinfo, name);
                if(varElem != null) {
                    elementAndNames.put(ElementHandle.create(varElem), newName);
                }
                ExecutableElement getElem = getMethod(cinfo, Naming.getterName(name), new Class[0]);
                if(getElem != null) {
                    elementAndNames.put(ElementHandle.create(getElem), Naming.getterName(newName));
                    TypeMirror type = getElem.getReturnType();
                    ExecutableElement setElem = getMethod(cinfo, Naming.setterName(name), Collections.<TypeMirror>singletonList(type));
                    if(setElem != null) {
                        elementAndNames.put(ElementHandle.create(setElem), Naming.setterName(newName));
                    }
                }
                return elementAndNames;
            }
        }, fObj);
    }
    
    private void renamePropertyBindingExpression(WorkingCopy wc, String name, String newName) {
        String oldLiteral = "#{" + getShortName() + "." + name + "}"; //NOI18N
        String newLiteral = "#{" + getShortName() + "." + newName + "}"; //NOI18N
        new Refactor.LiteralRenamer(wc, oldLiteral, newLiteral).scan(wc.getCompilationUnit(), null);
    }
    
    /*
     *  Internal method to add a method, returns element handle which can be cached by the caller
     */ 
    private Method addMethod(final ContextMethod cm, final String retType, final MethodKind kind) {
        Method method = null;
        method = (Method)WriteTaskWrapper.execute( new WriteTaskWrapper.Write() {
            public Object run(WorkingCopy wc) {
                Method m = getMethod(wc, cm.getName(), cm.getParameterTypes(), kind);
                if(m == null) {
                    TreeMaker make = wc.getTreeMaker();
                    TypeElement typeElement = typeElementHandle.resolve(wc);
                    ClassTree ctree = wc.getTrees().getTree(typeElement);
                    ClassTree newctree = ctree;
                    MethodTree mtree = TreeMakerUtils.createMethod(wc, cm, retType);
                    newctree = make.addClassMember(ctree, mtree);
                    wc.rewrite(ctree, newctree);
                }
                return m;
            }
        }, fObj);
        //If the method is newly added, write task should be completed first before
        //we access the method
        if(method == null) {
            method = getMethod(cm.getName(), cm.getParameterTypes(), kind);
        }
        return method;
    }
    
    public Method addMethod(ContextMethod cm) {
        String retTypeName = null;
        if(cm.getReturnType() != null) {
            retTypeName = cm.getReturnType().getCanonicalName();
        }
        return addMethod(cm, retTypeName, MethodKind.NORMAL);
    }
    
    
    public Method addMethod(MethodInfo mInfo) {
        return addMethod(mInfo, mInfo.getReturnTypeName(), MethodKind.NORMAL);
    }
    
    public DelegatorMethod addDelegatorMethod(MethodInfo mInfo) {
        return (DelegatorMethod)addMethod(mInfo, mInfo.getReturnTypeName(), MethodKind.DELEGATOR);
    }
    
    public EventMethod addEventMethod(MethodInfo mInfo) {
        return (EventMethod)addMethod(mInfo, mInfo.getReturnTypeName(), MethodKind.EVENT);
    }

    /*
     * Removes a method corresponding to passed in element handle
     */     
    void removeMethod(final ElementHandle<ExecutableElement> methodElementHandle) {
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
    private MethodTree getPublicConstructor(CompilationInfo cinfo, ClassTree ctree) {
        for(Tree tree : ctree.getMembers()) {
            if(Tree.Kind.METHOD == tree.getKind()) {
                MethodTree mtree = (MethodTree)tree;
                if(mtree.getName().toString().equals(Method.CTOR) &&
                        !cinfo.getTreeUtilities().isSynthetic(TreeUtils.getTreePath(cinfo, ctree))) {
                    return mtree;
                }
            }
        }
        return null;
    }
    
    /*
     * Returns a method corresponding to a method by given name and parameter types
     */     
    public Method getMethod(final String name, final Class[] params) {
        return (Method)ReadTaskWrapper.execute( new ReadTaskWrapper.Read() {
            public Object run(CompilationInfo cinfo) {
                return getMethod(cinfo, name, params, MethodKind.NORMAL);
            }
        }, fObj);
    }
    
    /*
     * Returns a method corresponding to a method by given name and parameter types
     */     
    private Method getMethod(final String name, final Class[] params, final MethodKind kind) {
        return (Method)ReadTaskWrapper.execute( new ReadTaskWrapper.Read() {
            public Object run(CompilationInfo cinfo) {
                return getMethod(cinfo, name, params, kind);
            }
        }, fObj);
    }
    
    /*
     * Returns a event method
     */     
    public EventMethod getEventMethod(final String name, final Class[] params) {
        return (EventMethod)ReadTaskWrapper.execute( new ReadTaskWrapper.Read() {
            public Object run(CompilationInfo cinfo) {
                return getMethod(cinfo, name, params, MethodKind.EVENT);
            }
        }, fObj);
    }
    
    /*
     * Returns a delegator method
     */     
    public DelegatorMethod getDelegatorMethod(final String name, final Class[] params) {
        return (DelegatorMethod)ReadTaskWrapper.execute( new ReadTaskWrapper.Read() {
            public Object run(CompilationInfo cinfo) {
                return getMethod(cinfo, name, params, MethodKind.DELEGATOR);
            }
        }, fObj);
    }        
    
    /*
     * Returns a public method corresponding to a method by given name and parameter types
     */         
    public Method getPublicMethod(final String name, final Class[] params) {
        return (Method)ReadTaskWrapper.execute( new ReadTaskWrapper.Read() {
            public Object run(CompilationInfo cinfo) {
                ExecutableElement elem = getMethod(cinfo, name, params);
                if(elem != null && elem.getModifiers().contains(Modifier.PUBLIC)) {
                    return new Method(elem, JavaClass.this);
                }
                
                return null;
            }
        }, fObj);        
    }
    
    /*
     * Returns a method corresponding to a method by given name and parameter types
     */     
    private Method getMethod(CompilationInfo cinfo, String name, Class[] params, 
            MethodKind kind) {
        ExecutableElement elem = getMethod(cinfo, name, params);
        if(elem != null) {
            switch(kind) {
                case NORMAL :
                    return new Method(elem, this);
                case DELEGATOR:
                    return new DelegatorMethod(elem, this);
                case EVENT:
                    return new EventMethod(elem, this);
            }
        }
        return null;
    }    
    
    /*
     * Returns a element corresponding to a method by given name and parameter types
     */     
    private ExecutableElement getMethod(CompilationInfo cinfo, String name, Class[] params) {
        if(params == null) {
            params = new Class[0];
        }
        if(name.equals(Method.CTOR)) {
            return getConstructor(cinfo, params);
        }else {
            List<ExecutableElement> methods = getMethods(cinfo, name, params, null);
            return (methods.size() == 1) ? methods.get(0) : null;
        }
    }
    
    /*
     * Returns element corresponding to a method by given parameter types
     */
    private ExecutableElement getConstructor(CompilationInfo cinfo, Class[] params) {
        TypeElement typeElement = typeElementHandle.resolve(cinfo);
        for(ExecutableElement method : ElementFilter.constructorsIn(typeElement.getEnclosedElements())) {
             if((params == null || matchTypes(cinfo, method.getParameters(), params))) {
                return method;
            }
        }
        return null;        
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
    
    /**
     * Returns the JavaClass(TypeElement wrapper) for public class in the given file
     */
    public static JavaClass getJavaClass(final FileObject fObj) {
        return (JavaClass)ReadTaskWrapper.execute(new ReadTaskWrapper.Read() {
            public Object run(CompilationInfo cinfo) {
                CompilationUnitTree cunit = cinfo.getCompilationUnit();
                for(Tree tree : cunit.getTypeDecls()) {
                    if(tree.getKind() == Tree.Kind.CLASS) {
                        ClassTree clazz = (ClassTree)tree;
                        if(clazz.getSimpleName().toString().equals(fObj.getName()) &&
                                clazz.getModifiers().getFlags().contains(Modifier.PUBLIC)) {
                            TypeElement element = TreeUtils.getTypeElement(cinfo, clazz);
                            if(element != null) {
                                return new JavaClass(element, fObj);
                            }                   
                        }
                    }
                }
                return null;
            }
        }, fObj);        
    }
    
    /*
     * @return The usage status, it returns 
     *         UseStatus.init_use_only if the bean is used in _init() method
     *         UseStatus.used if it is used elsewhere in .java in addition to _init() method
     *         UseStatus.not_used if it is not used in  
     */    
    public UsageStatus isPropertyUsed(final String name, final List<FileObject> fObjs) {
        final HashMap<ElementHandle, String> elementAndNames = getElementHandlesToReplace(name, name);
        return (UsageStatus)WriteTaskWrapper.execute( new WriteTaskWrapper.Write() {
            public Object run(WorkingCopy wc) {
                ElementsUsageFinder usageFinder = new ElementsUsageFinder(wc, elementAndNames);
                usageFinder.scan(wc.getCompilationUnit(), null);
                UsageStatus result = usageFinder.getUseStatus();
                return result;
            }
        }, fObjs);
    }
    
    public enum UsageStatus {
        NOT_USED, INIT_USE_ONLY, USED;
    }
    
    /* Look for the occurence of list of elements and a EL expression string literal
     * in the given java source. It sets an instance field by name useStatus as following
     *      UseStatus.init_use_only if the elements/literal is used in _init() method
     *      UseStatus.used if elements/literal is used elsewhere in addition to _init() method
     *      UseStatus.not_used if not used
     */
    class ElementsUsageFinder extends TreePathScanner<Tree, Void> {
        private final CompilationInfo cinfo;
        private HashMap<Element, String> elementAndNames = new HashMap<Element, String>();
        private UsageStatus useStatus = UsageStatus.NOT_USED;
        private String vbExpression;

        /** Creates a new instance of Refactor */
        public ElementsUsageFinder(CompilationInfo cinfo, HashMap<? extends ElementHandle, String> handleAndNames) {
            this.cinfo = cinfo;
            for (ElementHandle elemHandle : handleAndNames.keySet()) {
                Element elem = elemHandle.resolve(cinfo);
                elementAndNames.put(elem, handleAndNames.get(elemHandle));
            }
            //To take care of VB expressions, Ex:- getValue(#{SessionBean1.personRowSet})
            //renamePropertyBindingExpression(wc, name, newName);
            vbExpression = "#{" + getShortName() + "." + name + "}";                
        }

        @Override
        public Tree visitIdentifier(IdentifierTree tree, Void v) {
            if (useStatus != UsageStatus.USED) {
                UsageStatus status = getUseStatus(getCurrentPath());
                if(status != useStatus && status != UsageStatus.NOT_USED) {
                    useStatus = status;
                }
                return super.visitIdentifier(tree, v);
            }
            return null;
        }

        @Override
        public Tree visitMemberSelect(MemberSelectTree tree, Void v) {
            if (useStatus != UsageStatus.USED) {
                UsageStatus status = getUseStatus(getCurrentPath());
                if(status != useStatus && status != UsageStatus.NOT_USED) {
                    useStatus = status;
                }
                return super.visitMemberSelect(tree, v);
            }
            return null;
        }
        
        @Override
        public Tree visitMethod(MethodTree tree, Void v) {
            if (useStatus != UsageStatus.USED && !canSkip(getCurrentPath())) {
                return super.visitMethod(tree, v);
            }
            return null;
        }

        @Override
        public Tree visitVariable(VariableTree tree, Void v) {
            if (useStatus != UsageStatus.USED && !canSkip(getCurrentPath())) {
                return super.visitVariable(tree, v);
            }
            return null;
        }

        public UsageStatus getUseStatus() {
            return useStatus;
        }
        
        @Override
        public Tree visitLiteral(LiteralTree tree, Void v) {
            if (useStatus != UsageStatus.USED) {
                UsageStatus status = getUseStatus(tree);
                if (status != useStatus && status != UsageStatus.NOT_USED) {
                    useStatus = status;
                }
                return super.visitLiteral(tree, v);
            }
            return null;
        }
        
        private UsageStatus getUseStatus(LiteralTree tree) {
            if(tree.getKind() == Tree.Kind.STRING_LITERAL && vbExpression.equals(tree.getValue())) {
                if(getEnclosingMethodName(getCurrentPath()).equals("_init")) {
                    return UsageStatus.INIT_USE_ONLY;
                }else {
                    return UsageStatus.USED;
                }
            }else {
                return UsageStatus.NOT_USED;
            }
        }        

        private UsageStatus getUseStatus(TreePath path) {
            if (cinfo.getTreeUtilities().isSynthetic(path)) {
                return UsageStatus.NOT_USED;
            }
            Element el = cinfo.getTrees().getElement(path);
            if (el != null && elementAndNames.containsKey(el)) {
                if(getEnclosingMethodName(path).equals("_init")) {
                    return UsageStatus.INIT_USE_ONLY;
                }else {
                    return UsageStatus.USED;
                }
            }
            return UsageStatus.NOT_USED;
        }
        
        private boolean canSkip(TreePath path) {
            if (cinfo.getTreeUtilities().isSynthetic(path)) {
                return false;
            }
            Element el = cinfo.getTrees().getElement(path);
            if (el != null && elementAndNames.containsKey(el)) {
                TreePath declPath = cinfo.getTrees().getPath(el);
                if(declPath.getLeaf().equals(path.getLeaf())) {
                    return true;
                }
            }
            return false;
        }
        
        private String getEnclosingMethodName(TreePath path) {
            while(path != null) {
                Tree leaf = path.getLeaf();
                if(leaf.getKind() == Kind.METHOD) {
                    MethodTree mtree = (MethodTree)leaf;
                    return mtree.getName().toString();
                }
                path = path.getParentPath();
            }
            return null;
        }
    }    
}
