/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.completion.cplusplus;
import java.util.Set;
import org.netbeans.modules.cnd.completion.csm.CsmProjectContentResolver;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.util.CsmSortUtilities;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import java.util.TreeSet;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsChangeEvent;
import org.netbeans.editor.SettingsChangeListener;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.ext.ExtSettingsDefaults;
import org.netbeans.editor.ext.ExtSettingsNames;
import org.openide.filesystems.FileObject;

import org.netbeans.modules.cnd.completion.cplusplus.ext.CsmFinder;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmModel;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CsmCompletion;

/**
 *
 * @author Vladimir Voskresensky
 * based on MDRFinder
 */
public class CsmFinderImpl implements CsmFinder, SettingsChangeListener {

    public CsmModel model = null;
    
    private boolean caseSensitive = false;
    
    private boolean naturalSort = false;    
    
    private FileObject fo;
    private CsmFile csmFile;
    
    private Class kitClass;

    // ..........................................................................
    
    public CsmFinderImpl(FileObject fo, Class kitClass){
        this();
        this.fo = fo;
        this.kitClass = kitClass;
        caseSensitive = getCaseSensitive();
        naturalSort = getNaturalSort();
        Settings.addSettingsChangeListener(this);        
    }
        
    public CsmFinderImpl(CsmFile csmFile, Class kitClass){
        this();
        this.csmFile = csmFile;
        this.kitClass = kitClass;
        caseSensitive = getCaseSensitive();
        naturalSort = getNaturalSort();
        Settings.addSettingsChangeListener(this);        
    }
    
    public CsmFinderImpl(){
        super();
    }

    public CsmFile getCsmFile() {
        return this.csmFile;
    }
    
    public void settingsChange(SettingsChangeEvent evt) {
        if (evt == null || kitClass != evt.getKitClass()) return;
        
        if (ExtSettingsNames.COMPLETION_CASE_SENSITIVE.equals((evt.getSettingName()))){
            caseSensitive = getCaseSensitive();
        }else if (ExtSettingsNames.COMPLETION_NATURAL_SORT.equals((evt.getSettingName()))){
            naturalSort = getNaturalSort();
        }
    }
    
    
    private boolean getCaseSensitive() {
        return SettingsUtil.getBoolean(kitClass,
            ExtSettingsNames.COMPLETION_CASE_SENSITIVE,
            ExtSettingsDefaults.defaultCompletionCaseSensitive);
    }
    
    private boolean getNaturalSort() {
        return SettingsUtil.getBoolean(kitClass,
            ExtSettingsNames.COMPLETION_NATURAL_SORT,
            ExtSettingsDefaults.defaultCompletionNaturalSort);
    }
    
    private CsmNamespace resolveNamespace(String namespaceName, boolean caseSensitive) {
	CsmModel model = CsmModelAccessor.getModel();
        Set libraries = new HashSet();
        for (Iterator it = model.projects().iterator(); it.hasNext();) {
            CsmProject prj = (CsmProject) it.next();
            CsmNamespace ns = prj.findNamespace(namespaceName);
            if (ns != null) {
                return ns;
            }
            // remember libs
            libraries.addAll(prj.getLibraries());
        }
        for (Iterator it = libraries.iterator(); it.hasNext();) {
            CsmProject lib = (CsmProject) it.next();
            CsmNamespace ns = lib.findNamespace(namespaceName);
            if (ns != null) {
                return ns;
            }            
        }
        return null;
    }
    
    public CsmNamespace getExactNamespace(String namespaceName) {
        
        // System.out.println ("getExactNamespace: " + packageName); //NOI18N
        
//        repository.beginTrans (false);
        try {
//            ((JMManager) JMManager.getManager()).setSafeTrans(true);            
            CsmNamespace nmsp = resolveNamespace(namespaceName, true);
            return nmsp;
        } finally {
//            repository.endTrans (false);
        }
        
//        return null;
    }

    public CsmClassifier getExactClassifier(String classFullName) {
        // System.out.println ("getExactClassifier: " + classFullName); //NOI18N
        CsmClassifier cls = csmFile.getProject().findClassifier(classFullName);
//        Type cls = JavaModel.getDefaultExtent().getType().resolve(classFullName);
//        if (cls instanceof UnresolvedClass)
        return cls;
    }
    
    public List findNamespaces(String name, boolean exactMatch, boolean subNamespaces) {
        // System.out.println("findNamespaces: " + name); //NOI18N
        
        ArrayList ret = new ArrayList ();
        if (true) {
            // this methods should not be called
            return ret;
        }
        
//        repository.beginTrans (false);
        try {
//            ((JMManager) JMManager.getManager()).setSafeTrans(true);
            if (exactMatch) {
                CsmNamespace nmsp = getExactNamespace (name);
                if (nmsp != null) {
                    ret.add (nmsp);
                }
            } else {
                int index = name.lastIndexOf(CsmCompletion.SCOPE);
                String prefix = index > 0 ? name.substring(0, index) : ""; //NOI18N
                CsmNamespace nmsp = resolveNamespace(prefix, caseSensitive);
                if (nmsp != null) {
                    Collection subpackages = nmsp.getNestedNamespaces();
                    ArrayList list = new ArrayList();
                    for (Iterator it = subpackages.iterator(); it.hasNext();) {
                        CsmNamespace subPackage = (CsmNamespace) it.next();
                        String spName = caseSensitive ? subPackage.getName() : subPackage.getName().toUpperCase();
                        String csName = caseSensitive ? name : name.toUpperCase();
                        if (spName.startsWith(csName)) {
                            list.add(subPackage);
                        }
                    }
                    for (Iterator iter = list.iterator (); iter.hasNext ();) {
                        CsmNamespace nestedNmsp = (CsmNamespace) iter.next ();
                        ret.add (nestedNmsp);
                    }
                }
            } // else
            
            if (subNamespaces) {
                int size = ret.size ();                
                for (int x = 0; x < size; x++) {
                    CsmNamespace nestedNmsp = (CsmNamespace) ret.get(x);
                    addNestedNamespaces(ret, nestedNmsp);
                }
            }
            
        } finally {
//            repository.endTrans (false);
        }                
        return ret;
    }    

    
    public List findNestedNamespaces(CsmNamespace nmsp, String name, boolean exactMatch, boolean searchNested) {
        
        // System.out.println("findNamespaces: " + name); //NOI18N
        
        List ret = new ArrayList ();
        CsmProjectContentResolver contResolver = new CsmProjectContentResolver(getCaseSensitive());
        ret = contResolver.getNestedNamespaces(nmsp, name, exactMatch);
        return ret;
    }

    /** Find elements (classes, variables, enumerators) by name and possibly in some namespace
    * @param nmsp namespace where the elements should be searched for. It can be null
    * @param begining of the name of the element. The namespace name must be omitted.
    * @param exactMatch whether the given name is the exact requested name
    *   of the element or not.
    * @return list of the matching elements
    */
    public List findNamespaceElements(CsmNamespace nmsp, String name, boolean exactMatch, boolean searchNested) {
        List ret = new ArrayList();

        CsmProjectContentResolver contResolver = new CsmProjectContentResolver(getCaseSensitive());

        if (csmFile != null && csmFile.getProject() != null) {
            CsmProject prj = csmFile.getProject();
            CsmNamespace ns = nmsp == null ? prj.getGlobalNamespace() : nmsp;
            Collection classes = contResolver.getNamespaceClassesEnums(ns, name, exactMatch, searchNested);
            if (classes != null) {
                ret.addAll(classes);
            }
            classes = contResolver.getNamespaceEnumerators(ns, name, exactMatch, searchNested);
            if (classes != null) {
                ret.addAll(classes);
            }
            classes = contResolver.getNamespaceVariables(ns, name, exactMatch, searchNested);
            if (classes != null) {
                ret.addAll(classes);
            }
            classes = contResolver.getNamespaceFunctions(ns, name, exactMatch, searchNested);
            if (classes != null) {
                ret.addAll(classes);
            }
            if (prj.getGlobalNamespace() != ns) {
                classes =  contResolver.getLibClassesEnums(name, exactMatch);
                if (classes != null) {
                    ret.addAll(classes);
                }
            }
        }
        return ret;
    }

    /** Find classes by name and possibly in some namespace
    * @param nmsp namespace where the classes should be searched for. It can be null
    * @param begining of the name of the class. The namespace name must be omitted.
    * @param exactMatch whether the given name is the exact requested name
    *   of the class or not.
    * @return list of the matching classes
    */
    public List findClasses(CsmNamespace nmsp, String name, boolean exactMatch, boolean searchNested) {
        // System.out.println("findClasses: " + (nmsp == null ? "null" : nmsp.getName ()) + " " + name); //NOI18N
        
        List ret = new ArrayList();

        CsmProjectContentResolver contResolver = new CsmProjectContentResolver(getCaseSensitive());

        if (csmFile != null && csmFile.getProject() != null) {
            CsmProject prj = csmFile.getProject();
            CsmNamespace ns = nmsp == null ? prj.getGlobalNamespace() : nmsp;
            Collection  classes = contResolver.getNamespaceClassesEnums(ns, name, exactMatch, searchNested);
            if (classes != null) {
                ret.addAll(classes);
            }
            classes = prj.getGlobalNamespace() == ns ? null : contResolver.getLibClassesEnums(name, exactMatch);
            if (classes != null) {
                ret.addAll(classes);
            }
        }
        return ret;
        
////        repository.beginTrans (false);
//        try {
////            ((JMManager) JMManager.getManager()).setSafeTrans(true);
//            ArrayList classes = new ArrayList();
//            String clsName = (nmsp == null ? "" : nmsp.getName() + '.') + name;
//            if (fo == null){
//                // [TODO] rewrite to use a meaningful classpath
//                ClassPath cp = JavaMetamodel.getManager().getClassPath();
//                FileObject[] cpRoots = cp.getRoots();
//                for (int i = 0; i < cpRoots.length; i++) {
//                    ClassIndex ci = ClassIndex.getIndex(JavaModel.getJavaExtent(cpRoots[i]));
//                    if (ci == null) continue;
//                    if (nmsp == null) {
//                        if (exactMatch) {
//                            classes.addAll(ci.getClassesBySimpleName(name, caseSensitive));
//                        } else {
//                            classes.addAll(ci.getClassesBySNPrefix(name, caseSensitive));
//                        }
//                    } else {
//                        if (exactMatch) {
//                            CsmClass cls = ci.getClassByFqn(name);
//                            if (cls != null) {
//                                classes.add(cls);
//                            }
//                        } else {
//                            classes.addAll(ci.getClassesByFQNPrefix(clsName));
//                        }
//                    }
//               }
//            }else{
//                ClassIndex ci = ClassIndex.getIndex(JavaModel.getJavaExtent(fo));
//                if (ci != null){
//                    if (nmsp == null) {
//                        if (exactMatch) {
//                            classes.addAll(ci.getClassesBySimpleName(name, caseSensitive));
//                        } else {
//                            classes.addAll(ci.getClassesBySNPrefix(name, caseSensitive));
//                        }
//                    } else {
//                        if (exactMatch) {
//                            CsmClass cls = ci.getClassByFqn(name);
//                            if (cls != null) {
//                                classes.add(cls);
//                            }
//                        } else {
//                            classes.addAll(ci.getClassesByFQNPrefix(clsName));
//                        }
//                    }
//                }
//            }
//            for (Iterator it = classes.iterator(); it.hasNext();) {
//                CsmClass jcls = (CsmClass) it.next();
//                if (jcls==null || (CsmUtilities.getModifiers(jcls) & Modifier.PUBLIC) == 0){
//                    // filter out non public classes
//                    continue;
//                }
//                ret.add(new ClassImpl(jcls));
//            }
//            Collections.sort(ret, naturalSort ? JCBaseFinder.INSENSITIVE_CLASS_NAME_COMPARATOR : JCBaseFinder.CLASS_NAME_COMPARATOR);
//        } finally {
//            repository.endTrans (false);
//        }
//        return ret;
    }

//    /** Find fields by name in a given class.
//    * @param cls class which is searched for the fields.
//    * @param name start of the name of the field
//    * @param exactMatch whether the given name of the field is exact
//    * @param staticOnly whether search for the static fields only
//    * @return list of the matching fields
//    */
//    public List findFields(CsmClass c, String name, boolean exactMatch,
//                           boolean staticOnly, boolean inspectOuterClasses) {
//                
//        // System.out.println("findFields: " + cls.getFullName ()); //NOI18N
//        
//        TreeSet ts = naturalSort ? new TreeSet(CsmSortUtilities.NATURAL_MEMBER_NAME_COMPARATOR) : new TreeSet();
//        
//        // get class variables visible in this method
//        CsmClass clazz = c;
//        CsmProjectContentResolver contResolver = new CsmProjectContentResolver(getCaseSensitive(), getNaturalSort());
//        List classFields = contResolver.getFields(clazz, CsmInheritanceUtilities.MAX_VISIBILITY, name, staticOnly, exactMatch);
//        return classFields;
////        repository.beginTrans (false);
////        try {
////            ((JMManager) JMManager.getManager()).setSafeTrans(true);
////            CsmClass jc = null;
////            if (cls instanceof ClassImpl) {
////                jc = ((ClassImpl) cls).javaSource;
////            } else {
////                TypeClass typeProxy = JavaModel.getDefaultExtent().getType();
////                Object o = typeProxy.resolve(cls.getFullName());
////                if (o instanceof CsmClass) {
////                    jc = (CsmClass) o;
////                } else {
////                    jc = (CsmClass) typeProxy.resolve("java.lang.Object"); //NOI18N 
////                }
////            }
////            if (jc == null) {
////                return new ArrayList ();
////            }
////            List clsList = getClassList(jc);
////            String pkgName = cls.getPackageName();
////            HashSet ifaces = new HashSet(); // The set for temporal storage of all implemented interfaces
////
////            CsmClass tempClass;
////
////            for (int i = clsList.size() - 1; i >= 0; i--) {
////                tempClass = (CsmClass) clsList.get (i);
////
////                // remember all the interfaces along the way through hierarchy
////                if (tempClass.isInterface()) {
////                    ifaces.add(tempClass); //bugfix of #19615
////                }            
////                ifaces.addAll(tempClass.getInterfaces());
////
////                String pName = getPackageName(jc);
////                boolean difPkg = !pName.equals(pkgName);
////                List outerList = (i == 0 && inspectOuterClasses && cls.getName().indexOf('.') >= 0)
////                                 ? getOuterClasses(tempClass) : null;
////                int outerInd = (outerList != null) ? (outerList.size() - 1) : -1;
////                do {
////                    if (outerInd >= 0) {
////                        tempClass = (CsmClass)outerList.get(outerInd--);
////                    }
////                    Iterator iter = tempClass.getFeatures().iterator ();
////                    while (iter.hasNext ()) {
////                        Feature feature = (Feature) iter.next();
////                        if (!(feature instanceof CsmField)) continue;
////                        CsmField fld = (CsmField) feature;
////                        int mods = CsmUtilities.getModifiers(fld);
////                        if ((staticOnly && (mods & Modifier.STATIC) == 0)
////                                || ((mods & Modifier.PRIVATE) != 0)//(i > 0 && (mods & Modifier.PRIVATE) != 0) - #46851
////                                || (difPkg && (mods & (Modifier.PUBLIC | Modifier.PROTECTED)) == 0)
////                                || ((outerInd >-1) && ((CsmUtilities.getModifiers(jc) & Modifier.STATIC) != 0 ) && ((mods & Modifier.STATIC) == 0))
////                           ) {
////                            continue;
////                        }
////                        if (exactMatch) {
////                            if (!fld.getName().equals(name)) {
////                                continue;
////                            }
////                        } else {
////                            if (!startsWith(fld.getName(), name)) {
////                                continue;
////                            }
////                        }
////
////                        boolean isLocal = jc.equals(tempClass) && outerInd == -1;
////                        // [PENDING]
////                        // if (showDeprecated || !JCUtilities.isDeprecated(fld)){
////                            ts.add(new FieldImpl(fld, isLocal));
////                        // }
////
////                    } // while
////                } while (outerInd >= 0);
////            } // while
////            // add ALL known fields from interfaces, ALL as they are public static
////            for( Iterator it = ifaces.iterator(); it.hasNext(); ) {
////                tempClass = (CsmClass) it.next ();            
////                Iterator fieldsIter = tempClass.getFeatures().iterator ();
////                while (fieldsIter.hasNext ()) {
////                    Object tmp = fieldsIter.next();
////                    if (!(tmp instanceof CsmField)) continue;
////                    CsmField fld = (CsmField) tmp;
////                    if( exactMatch ? !fld.getName().equals(name)
////                                   : !startsWith(fld.getName(), name) ) continue;
////
////                    // [PENDING]
////                    // if (showDeprecated || !JCUtilities.isDeprecated(fld)){
////                        ts.add(new FieldImpl (fld));
////                    // }
////                }            
////            }
////
////            if (staticOnly){
////                if((exactMatch && "class".equals(name)) || (!exactMatch && startsWith("class", name))){ //NOI18N
////                    JCField field = new BaseField(JavaCompletion.CLASS_CLASS, "class", //NOI18N
////                    JavaCompletion.CLASS_TYPE, Modifier.PUBLIC);
////                    ts.add(field);
////                }
////            }
////            
////            if (cls == JavaCompletion.OBJECT_CLASS_ARRAY) {
////                ts.add(new BaseField(JavaCompletion.INT_CLASS, "length", JavaCompletion.INT_TYPE, Modifier.PUBLIC)); // NOI18N
////            }
////
////        } finally {
////            repository.endTrans (false);
////        }
//        
////        return new ArrayList(ts);        
//    }
//
//    
//    /** Find methods by name in a given class.
//    * @param cls class which is searched for the methods.
//    * @param name start of the name of the method
//    * @param exactMatch whether the given name of the method is exact
//    * @param staticOnly whether search for the static methods only
//    * @return list of the matching methods
//    */
//    public List findMethods(CsmClass c, String name, boolean exactMatch,
//                            boolean staticOnly, boolean inspectOuterClasses) {
//                                
//        // System.out.println("findMethods: " + cls.getName ()); //NOI18N
//
//        TreeSet ts = naturalSort ? new TreeSet(CsmSortUtilities.NATURAL_MEMBER_NAME_COMPARATOR) : new TreeSet();
// 
//        CsmClass clazz = c;
//        CsmProjectContentResolver contResolver = new CsmProjectContentResolver(getCaseSensitive(), getNaturalSort());
//        List classMethods = contResolver.getMethods(clazz, CsmInheritanceUtilities.MAX_VISIBILITY, name, staticOnly, exactMatch);
//        return classMethods;
//        
////       repository.beginTrans (false);
////       try {
////            ((JMManager) JMManager.getManager()).setSafeTrans(true);
////            CsmClass jc = null;
////            if (cls instanceof ClassImpl) {
////                jc = ((ClassImpl) cls).javaSource;
////            } else {
////                TypeClass typeProxy = JavaModel.getDefaultExtent().getType();
////                Object o = typeProxy.resolve(cls.getFullName());
////                if (o instanceof CsmClass) {
////                    jc = (CsmClass) o;
////                } else {
////                    jc = (CsmClass) typeProxy.resolve("java.lang.Object"); //NOI18N
////                }
////            }
////            if (jc == null) {
////                return new LinkedList ();
////            }else{
////                jc = (CsmClass)JMIUtils.getSourceElementIfExists(jc);
////            }
////            
////            List clsList = getClassList(jc);
////            String pkgName = cls.getPackageName();
////
////            for (int i = clsList.size() - 1; i >= 0; i--) {
////                CsmClass tempCls = (CsmClass) clsList.get(i);
////                if (tempCls != null) {
////                    String tempName = getPackageName(tempCls);
////                    boolean difPkg = !tempName.equals(pkgName);
////                    List outerList = (i == 0 && inspectOuterClasses && (tempCls.getDeclaringClass () != null))
////                                     ? getOuterClasses(tempCls) : null;
////                    int outerInd = (outerList != null) ? (outerList.size() - 1) : -1;
////                    do {
////                        if (outerInd >= 0) {
////                            tempCls = (CsmClass)outerList.get(outerInd--);
////                        }
////
////                        tempCls = (CsmClass)JMIUtils.getSourceElementIfExists(tempCls);
////                        Iterator methodsIter = tempCls.getFeatures().iterator ();
////                        while (methodsIter.hasNext ()) {          
////                            Object tmp = methodsIter.next();
////                            if (!(tmp instanceof CsmMethod)) continue;
////                            CsmMethod mtd = (CsmMethod) tmp;
////                            int mods = CsmUtilities.getModifiers(mtd);
////                            if ((staticOnly && (mods & Modifier.STATIC) == 0)
////                                    || ((mods & Modifier.PRIVATE) != 0)//(i > 0 && (mods & Modifier.PRIVATE) != 0) - #46851
////                                    || (difPkg && (mods & (Modifier.PUBLIC | Modifier.PROTECTED)) == 0)
////                                    || ((outerInd >-1) && ((CsmUtilities.getModifiers(jc) & Modifier.STATIC) != 0 ) && ((mods & Modifier.STATIC) == 0))
////                               ) {
////                                continue;
////                            }
////                            if (exactMatch) {
////                                if (!mtd.getName().equals(name)) {
////                                    continue;
////                                }
////                            } else { // match begining
////                                if (!startsWith(mtd.getName(), name)) {
////                                    continue;
////                                }
////                            }
////
////                            boolean isLocal = jc.equals(tempCls) && outerInd == -1;
////                            
////                            // override the method from superclass (throwing exceptions could differ)
////                            MethodImpl mtdImpl = new MethodImpl (mtd, isLocal);
////                            if (ts.contains(mtdImpl)) 
////                                ts.remove(mtdImpl);
////
////                            // [PENDING]
////                            // if (showDeprecated || !JCUtilities.isDeprecated(mtd)){
////                                ts.add(mtdImpl);
////                            // }
////                        }
////                    } while (outerInd >= 0);
////                }
////            }
////        } finally {
////            repository.endTrans (false);
////        }
////            
////        return new ArrayList(ts);
//    }

    private void addNestedNamespaces (List list, CsmNamespace nmsp) {
        Iterator iter = nmsp.getNestedNamespaces().iterator();
        while (iter.hasNext ()) {
            CsmNamespace n = (CsmNamespace) iter.next ();
            list.add (n);
            addNestedNamespaces (list, n);
        }
    }
    
    private List getClassListForList(List classes) {
        Iterator interfacesIt=classes.iterator();
        List ret = new ArrayList ();
        
        while(interfacesIt.hasNext()) {
            ret.addAll(getClassList((CsmClass)interfacesIt.next()));
        }
        return ret;
    }
    
    private List getClassList(CsmClass jc) {
        List ret = new ArrayList ();
//        if (jc != null) {
//            ret.add(jc);
//            if (jc.isInterface()) {                
//                ret.addAll(getClassListForList(jc.getInterfaces()));
//                // #16252 it is legal to call methods for java.lang.Object from an interface
//                CsmClass objectClass = (CsmClass)JavaModel.getDefaultExtent().getType().resolve("java.lang.Object"); // NOI18N
//                if (objectClass != null)
//                    ret.add(objectClass); // [PENDING] ???
//            } else {
//                CsmClass superClass = jc.getSuperClass();
//                if (superClass != null)
//                    ret.addAll(getClassList(superClass));
//                if (Modifier.isAbstract(CsmUtilities.getModifiers(jc))) {
//                    // in the case of abstract implementor of interface
//                    ret.addAll(getClassListForList(jc.getInterfaces()));
//                } // if
//            } // else
//        }
        return ret;
    }    
    
    /** Get outer classes to search
    * the fields and methods there. The original class is added
    * as the first member of the resulting list.
    */
    private List getOuterClasses(CsmClass jc) {        
        ArrayList outers = new ArrayList();
        outers.add(jc);
        while (jc != null) {
            List/*<CsmClass>*/ baseCls = jc.getBaseClasses();
            if (baseCls != null) {
                // [PENDING] check for deprecated classes ...
                // if (showDeprecated || !JCUtilities.isDeprecated(cls)) 
                outers.add (baseCls);
            }
        }
        return outers;
    }
    
//    //......................................
//    public Iterator getClasses () {
//        return null;
//    }
//    
//    public boolean append(JCClassProvider cp) {
//        return true;
//    }
//
//    public void reset() {
//    }
//
//    public boolean notifyAppend(CsmClass c, boolean appendFinished) {
//        return false;
//    }
//    //......................................
    
    public void setCaseSensitive(boolean sensitive){
        caseSensitive = sensitive;
    }

    public void setNaturalSort(boolean sort){
        naturalSort = sort;        
    }
    
    private boolean startsWith(String theString, String prefix){
        return caseSensitive ? theString.startsWith(prefix) :
            theString.toLowerCase().startsWith(prefix.toLowerCase());
    }

    /** Find fields by name in a given class.
    * @param contextDeclaration declaration which defines context (class or function)
    * @param c class which is searched for the fields.
    * @param name start of the name of the field
    * @param exactMatch whether the given name of the field is exact
    * @param staticOnly whether search for the static fields only
    * @param inspectOuterClasses if the given class is inner class of some
    *   outer class, whether the fields of the outer class should be possibly
    *   added or not. This should be false when searching for 'this.'
    * @return list of the matching fields
    */    
    public List findFields(CsmOffsetableDeclaration contextDeclaration, CsmClass classifier, String name, boolean exactMatch, boolean staticOnly, boolean inspectOuterClasses, boolean inspectParentClasses,boolean scopeAccessedClassifier, boolean sort) {
        // get class variables visible in this method
        CsmProjectContentResolver contResolver = new CsmProjectContentResolver(getCaseSensitive());
        List classFields = contResolver.getFields(classifier, contextDeclaration, name, staticOnly, exactMatch,inspectParentClasses, scopeAccessedClassifier);
        return classFields;        
    }

    /** Find enumerators by name in a given class.
    * @param contextDeclaration declaration which defines context (class or function)
    * @param c class which is searched for the enumerators.
    * @param name start of the name of the field
    * @param exactMatch whether the given name of the enumerators is exact
    * @param inspectOuterClasses if the given class is inner class of some
    *   outer class, whether the enumerators of the outer class should be possibly
    *   added or not. This should be false when searching for 'this.'
    * @return list of the matching fields
    */    
    public List findEnumerators(CsmOffsetableDeclaration contextDeclaration, CsmClass classifier, String name, boolean exactMatch, boolean inspectOuterClasses, boolean inspectParentClasses,boolean scopeAccessedClassifier, boolean sort) {
        CsmProjectContentResolver contResolver = new CsmProjectContentResolver(getCaseSensitive());
        List classFields = contResolver.getEnumerators(classifier, contextDeclaration, name, exactMatch, inspectParentClasses,scopeAccessedClassifier);
        return classFields;        
    }
    
    /** Find methods by name in a given class.
    * @param contextDeclaration declaration which defines context (class or function)
    * @param c class which is searched for the methods.
    * @param name start of the name of the method
    * @param exactMatch whether the given name of the method is exact
    * @param staticOnly whether search for the static methods only
    * @param inspectOuterClasses if the given class is inner class of some
    *   outer class, whether the methods of the outer class should be possibly
    *   added or not. This should be false when searching for 'this.'
    * @return list of the matching methods
    */
    public List findMethods(CsmOffsetableDeclaration contextDeclaration, CsmClass classifier, String name, boolean exactMatch, boolean staticOnly, boolean inspectOuterClasses, boolean inspectParentClasses,boolean scopeAccessedClassifier, boolean sort) {
        CsmClass clazz = classifier;
        CsmProjectContentResolver contResolver = new CsmProjectContentResolver(getCaseSensitive());
        if (contextDeclaration == null) {
            // in global context get all
            contextDeclaration = clazz;
        }
        List classFields = contResolver.getMethods(clazz, contextDeclaration, name, staticOnly, exactMatch, inspectParentClasses,scopeAccessedClassifier);
        return classFields;          
    }

    public List findNestedClassifiers(CsmOffsetableDeclaration contextDeclaration, CsmClass c, String name, boolean exactMatch, boolean inspectParentClasses, boolean sort) {
        CsmClass clazz = c;
        CsmProjectContentResolver contResolver = new CsmProjectContentResolver(getCaseSensitive());
        List classClassifiers = contResolver.getNestedClassifiers(clazz, contextDeclaration, name, exactMatch, inspectParentClasses);
        return classClassifiers; 
    }
}
