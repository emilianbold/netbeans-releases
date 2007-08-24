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

package org.netbeans.modules.cnd.completion.cplusplus.ext;

import org.netbeans.modules.cnd.api.model.CsmClassifier;
import java.util.List;

import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;

/**
* Java completion finder
*
* @author Vladimir Voskresensky
* @version 1.00
* based on JCFinder @version 1.00 - move to CSM model
*/

public interface CsmFinder {

    public CsmFile getCsmFile();
    
    /** Get the namespace from the namespace name */
    public CsmNamespace getExactNamespace(String namespaceName);

    /** Get the class from full name of the class */
    public CsmClassifier getExactClassifier(String classFullName);

    /** Get the list of nested namespaces that start with the given name
    * @param name the start of the requested namespace(s) name
    * @return list of the matching namespaces
    */
    public List findNestedNamespaces(CsmNamespace nmsp, String name, boolean exactMatch, boolean searchNested);

    /** Find classes by name and possibly in some namespace
    * @param nmsp namespace where the classes should be searched for. It can be null
    * @param begining of the name of the class. The namespace name must be omitted.
    * @param exactMatch whether the given name is the exact requested name
    *   of the class or not.
    * @return list of the matching classes
    */
    public List findClasses(CsmNamespace nmsp, String name, boolean exactMatch, boolean searchNested);

    /** Find elements (classes, variables, enumerators) by name and possibly in some namespace
    * @param nmsp namespace where the elements should be searched for. It can be null
    * @param begining of the name of the element. The namespace name must be omitted.
    * @param exactMatch whether the given name is the exact requested name
    *   of the element or not.
    * @return list of the matching elements
    */
    public List findNamespaceElements(CsmNamespace nmsp, String name, boolean exactMatch, boolean searchNested);

//    /** Find fields by name in a given class.
//    * @param c class which is searched for the fields.
//    * @param name start of the name of the field
//    * @param exactMatch whether the given name of the field is exact
//    * @param staticOnly whether search for the static fields only
//    * @param inspectOuterClasses if the given class is inner class of some
//    *   outer class, whether the fields of the outer class should be possibly
//    *   added or not. This should be false when searching for 'this.'
//    * @return list of the matching fields
//    */
//    public List findFields(CsmClass c, String name, boolean exactMatch,
//                           boolean staticOnly, boolean inspectOuterClasses);
//
//    /** Find methods by name in a given class.
//    * @param c class which is searched for the methods.
//    * @param name start of the name of the method
//    * @param exactMatch whether the given name of the method is exact
//    * @param staticOnly whether search for the static methods only
//    * @param inspectOuterClasses if the given class is inner class of some
//    *   outer class, whether the methods of the outer class should be possibly
//    *   added or not. This should be false when searching for 'this.'
//    * @return list of the matching methods
//    */
//    public List findMethods(CsmClass c, String name, boolean exactMatch,
//                            boolean staticOnly, boolean inspectOuterClasses);

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
    public List findFields(CsmOffsetableDeclaration contextDeclaration, CsmClass c, String name, boolean exactMatch,
                           boolean staticOnly, boolean inspectOuterClasses, boolean inspectParentClasses,boolean scopeAccessedClassifier, boolean sort);

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
    public List findEnumerators(CsmOffsetableDeclaration contextDeclaration, CsmClass c, String name, boolean exactMatch, 
            boolean inspectOuterClasses, boolean inspectParentClasses,boolean scopeAccessedClassifier, boolean sort);
    
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
    public List findMethods(CsmOffsetableDeclaration contextDeclaration, CsmClass c, String name, boolean exactMatch,
                            boolean staticOnly, boolean inspectOuterClasses, boolean inspectParentClasses,boolean scopeAccessedClassifier, boolean sort);    
    
    /** Find nested classifiers by name in a given class.
    * @param contextDeclaration declaration which defines context (class or function)
    * @param c class which is searched for the nested classifiers.
    * @param name start of the name of the nested classifiers
    * @param exactMatch whether the given name of the nested classifiers is exact
    * @param staticOnly whether search for the static nested classifiers only
    * @param inspectParentClasses if the given class is inner class of some
    *   outer class, whether the classifiers of the outer class should be possibly
    *   added or not
    * @return list of the matching nested classifiers
    */
    public List findNestedClassifiers(CsmOffsetableDeclaration contextDeclaration, CsmClass c, String name, boolean exactMatch,
                            boolean inspectParentClasses, boolean sort);      
}
