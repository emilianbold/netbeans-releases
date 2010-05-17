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

/*
 * File         : JavaClassUtils.java
 * Version      : 1.3
 * Description  : Utility methods for UML-Java conversion
 * Author       : Trey Spiva
 */
package org.netbeans.modules.uml.integration.ide;

import java.util.Locale;
import org.netbeans.modules.uml.core.metamodel.structure.IModel;
import java.io.File;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.Vector;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;

import org.netbeans.modules.uml.integration.ide.events.ClassInfo;
import org.netbeans.modules.uml.integration.ide.events.EventManager;
import org.netbeans.modules.uml.integration.ide.events.SymbolTransaction;
import org.netbeans.modules.uml.integration.ide.events.MemberInfo;
import org.netbeans.modules.uml.integration.ide.events.MethodInfo;
import org.netbeans.modules.uml.integration.ide.events.MethodParameterInfo;
import org.netbeans.modules.uml.core.IUMLCreationFactory;
import org.netbeans.modules.uml.core.coreapplication.IPreferenceManager2;
import org.netbeans.modules.uml.core.eventframework.IEventDispatchController;
import org.netbeans.modules.uml.core.eventframework.IEventDispatcher;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IDataType;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumeration;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IDependency;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ITaggedValue;
import org.netbeans.modules.uml.core.metamodel.infrastructure.RelationFactory;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IGeneralization;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IImplementation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.INavigableEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeature;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlsupport.Log;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.ElementLocator;
import org.netbeans.modules.uml.core.support.umlutils.IElementLocator;
import org.netbeans.modules.uml.core.typemanagement.ITypeManager;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspace;
import org.netbeans.modules.uml.core.scm.ISCMIntegrator;
import org.netbeans.modules.uml.core.scm.ISCMTool;


/**
 * A collection of utility methods to use when dealing with the Java naming
 * convention and the UML naming convention.
 *
 * Revision History
 * No.  Date        Who         What
 * ---  ----        ---         ----
 *   1  2002-04-23  Darshan     Added getFullyQualifiedName() to return the
 *                              fully qualified name of an INamedElement.
 *   2  2002-04-24  Darshan     Reindented methods, added method to get base
 *                              directory given an IProject.
 *   3  2002-04-30  Darshan     Added methods to map Java modifiers to Describe
 *                              codes and back.
 *   4  2002-05-10  Darshan     Changed getBaseDirectory() to call
 *                              GDProSupport's getBaseDirectory().
 *   5  2002-05-24  Darshan     Added startsWith() method to do a
 *                              case-insensitive prefix match.
 *   6  2002-05-24  Darshan     Added diagnostic method to display immediate
 *                              children of a project.
 *   7  2002-05-30  Darshan     Added convenience method to extract filename
 *                              (minus extension) from the path.
 *   8  2002-06-03  Darshan     Added methods to get and set tagged values for
 *                              an IElement.
 *   9  2002-06-14  Darshan     Optimized findAttribute() method.
 *  10  2002-06-19  Darshan     Modified behaviour of convertJavaToUML() to
 *                              suit Wolverine better.
 *  11  2002-06-20  Darshan     Fixed determineCommonRelations() to return any
 *                              association (including compositions and
 *                              aggregations), not just the vanilla association.
 *  12  2002-06-21  Darshan     Added check for primitive types before searching
 *                              for the type in the model, shifted isPrimitive
 *                              function here (from IDEProcessor), fixed
 *                              findAttribute to search for INavigableEnds as
 *                              well as IAttributes and added findAssociation.
 * 13. 2002-07-04  Mukta        Added method for deleting folder for a package.
 *
 * @author Trey Spiva
 * @version 1.0
 */
public class JavaClassUtils {
    public static final int DESC_PUBLIC     = 0;
    public static final int DESC_PROTECTED  = 1;
    public static final int DESC_PRIVATE    = 2;
    public static final int DESC_PACKAGE    = 3;

    public static final String REF_CLASS    = "ReferenceClass";

    /**
     * The default contructor for JavaClassUtils.
     */
    public JavaClassUtils() {
    }

    /**
     *  Returns the Describe 6.0 modifier for a Java access modifier int.
     * @param  mod A Java modifier integer
     * @return A modifier as Describe expects it to be.
     */
    public static int getDescribeModifier(int mod) {
        if ((mod & Modifier.PUBLIC) != 0)
            return DESC_PUBLIC;
        else if ((mod & Modifier.PROTECTED) != 0)
            return DESC_PROTECTED;
        else if ((mod & Modifier.PRIVATE) != 0)
            return DESC_PRIVATE;
        return DESC_PACKAGE;
    }

    /**
     *  Returns the Java modifier for a Describe 6.0 access modifier int.
     * @param  mod A Describe 6.0 modifier integer
     * @return A modifier as Java expects it to be.
     */
    public static int getJavaModifier(int mod) {
        switch (mod) {
        case DESC_PUBLIC:
                return Modifier.PUBLIC;
        case DESC_PROTECTED:
                return Modifier.PROTECTED;
        case DESC_PRIVATE:
                return Modifier.PRIVATE;
        default:
                return 0;
        }
    }

    /**
     *  Returns the fully-qualified name for the given named element.
     * @param element The element for which the qualified name needs to be
     *                determined.
     * @return Fully qualified name, with packages separated by periods and
     *         inner classes separated by '$'.
     */
    public static String getFullyQualifiedName(INamedElement element) {
        StringBuffer name = new StringBuffer();        

        if(element != null)
        {            
            name.append(element.getName());
            for (IElement parent = element.getOwner(); parent != null;
                            parent = parent.getOwner()) {
                if(!(parent instanceof IModel))
                {
                    if (parent instanceof IClassifier) {
                        IClassifier p = (IClassifier)  parent;
                        name.insert(0, '$');
                        name.insert(0, p.getName());
                    } else if (parent instanceof IPackage
                                    && !(parent instanceof IProject)) {
                        IPackage p = (IPackage)  parent;
                        name.insert(0, '.');
                        name.insert(0, p.getName());
                    } else
                    {
                        break;
                    }
                }
            }
        }
        return name.toString();
    }

    public static String getTaggedValue(IElement element, String tagName) {
        ITaggedValue tag = null;
        if(element != null)
        {
        	tag = element.getTaggedValueByName(tagName);
        if (tag == null || tag.getName().length() == 0)
            return null;

        return tag.getDataValue();
    }
        return null;
    }

    public static void setTaggedValue(IElement element, String tagName,
                    String data) {
        ITaggedValue tag = element.getTaggedValueByName(tagName);
        if (tag == null || tag.getName().length() == 0) {
            if (data != null)
                element.addTaggedValue(tagName, data);
        } else {
            if (data != null)
                tag.setDataValue(data);
            else
                element.removeTaggedValue(tag);
        }
    }

    public static IDataType createDataType(String fullname) {
        Log.out("]]] Creating data type " + fullname);
        String name = getInnerClassName(fullname);
        INamedElement parent = SymbolTransaction.getClassOwner(fullname);
        if (parent == null)
            return null;
        IUMLCreationFactory fact = SymbolTransaction.getUMLCreationFactory();
        IDataType type = fact.createDataType(null);
        parent.addElement(type);
        type.setName(name);
        return type;
    }

    /**
     * Returns the IPackage reference if it already exists in project
     * @param name
     * @param proj
     * @return
     */
    public static IPackage findPackage(String name, IProject proj) {
        INamedElement pack = findElement(proj, name, IPackage.class);
        return pack != null? (IPackage)  pack : null;
    }

    public static IPackage findScopedPackage(String name, INamespace ns) {
        if (ns == null && (ns = UMLSupport.getCurrentProject()) == null)
            return null;
        ETList<INamedElement> els = ns.getOwnedElementsByName(name);
        if (els != null) {
            for (int i = els.getCount() - 1; i >= 0; --i) {
                INamedElement element = els.item(i);
                if (element != null && element instanceof IPackage)
                    return (IPackage)  element;
            }
        }
        return null;
    }

    /**
     * Converts a fully qualified Java classname to Describe's internal
     * naming format. Note that inner class names should be delimited by '$'
     * instead of '.' for this to work correctly.
     *
     * Do *not* call this method for primitives!
     *
     * @param javaName A fully qualifid java class name.
     */
    public static String convertJavaToUML( String javaName ) {
        String retVal = javaName.replace('$', '.');

        // replace "." with "::"
        int pos = retVal.indexOf(".");
        while(pos > 0) {
            StringBuffer buf = new StringBuffer(retVal);
            retVal = buf.replace(pos, pos + 1, "::").toString();
            pos = retVal.indexOf(".");
        }

        return retVal;
    }

    /**
     * Builds a UML notated name from a Java package and class name. Note that
     * inner class names should be delimited by '$' instead of '.' for this
     * to work correctly.
     * @param pName The fully quallified package of the class.
     * @param name The name of the class.
     */
    public static String convertJavaToUML(String pName, String name) {
        String curPName = (pName.length() > 0 ? pName + "." : "");
        String fullScopeName  = curPName + name;
        return convertJavaToUML(fullScopeName);
    }

    /**
     * Converts a UML fully qualified name into a Java fully qualified name.
     * @param umlNam A UML formated string.
     */
    public static String convertUMLtoJava( String umlName ) {
        String retVal = umlName;

        // replace "::" with "."
        int pos = retVal.indexOf("::");
        while(pos > 0) {
            StringBuffer buf = new StringBuffer(retVal);
            retVal = buf.replace(pos, pos + 2, ".").toString();
            pos = retVal.indexOf("::");
        }

        return retVal;
    }

    /**
     *  Given a package name and a class name, joins them to form a full class
     * name. Caveat: inner class names will be separated by '$' instead of '.'
     *  Ex: formFullClassName("com.si", "Outer") returns "com.si.Outer"
     *      formFullClassName("com.si", "Outer.Inner") returns
     *                                                   "com.si.Outer$Inner"
     */
    public static String formFullClassName(String pack, String className) {
        if (className == null) return null;
        String rep = className.replace('.', '$');
        return (pack == null || pack.equals(""))? rep : (pack + "." + rep);
    }

    /**
     *  gets the name of an inner class as Outer.Inner given a fully qualified
     * class name (with the inner class specified as Outer$Inner).
     *   For a class com.foo.Outer$Inner, returns "Outer.Inner"
     *   For a class com.foo.Unique, returns "Unique"
     *   For a class NoPackage, returns "NoPackage"
     */
    public static String getFullInnerClassName(String fullClassName) {
        return getShortClassName(fullClassName).replace('$', '.');
    }

    /**
     *  removes the package prefix from a fully qualified class name. Inner
     * classes should be written as Outer$Inner.
     *  For a class com.foo.Outer$Inner, returns "Outer$Inner"
     *  For a class NoPackage, returns "NoPackage"
     */
    public static String getShortClassName(String fullClassName) 
    {
        if (fullClassName == null || fullClassName.length() < 1)
            return "";
        
        int dotPos = fullClassName.lastIndexOf('.');
    
        String clsName = (dotPos == -1)
            ? fullClassName 
            : fullClassName.substring(dotPos + 1);
        return clsName;
    }

    /**
     *  gets the name of the inner class from a fully qualified class name.
     *   For a class com.foo.Outer$Inner, returns "Inner"
     *   For a class com.foo.Unique, returns "Unique"
     *   For a class NoPackage, returns "NoPackage"
     */
    public static String getInnerClassName(String fullClassName) {
        if (fullClassName == null) return null;
        int dolPos = fullClassName.lastIndexOf('$');
        String clsName = (dolPos == -1)? getShortClassName(fullClassName) :
                fullClassName.substring(dolPos + 1);
        //Log.out("Inner class is '" + clsName + "'");
        return clsName;
    }

    /**
     *  Gets the name of the outermost class from a fully qualified name.
     *  For a class com.foo.Outer$Inner1$Inner2, returns "com.foo.Outer"
     *  For a class com.foo.Unique, returns "com.foo.Unique"
     *
     * @param fullClassName
     * @return
     */
    public static String getOuterClassName(String fullClassName) {
        int dolPos = fullClassName.indexOf('$');
        return (dolPos == -1)? fullClassName :
                               fullClassName.substring(0, dolPos);
    }

    /**
     *  returns the package name, given a fully qualified class name. It should
     * be noted that inner classes should not be represented as Outer.Inner, but
     * as Outer$Inner for this to work.
     *  For a class com.foo.Outer$Inner, returns "com.foo"
     *  For a class NoPackage, returns ""
     *  null is never returned.
     */
    public static String getPackageName(String fullClassName) {
        int dotPos = fullClassName.lastIndexOf('.');
        String pkg = (dotPos == -1)? "" : fullClassName.substring(0, dotPos);
        //Log.out("Package is '" + pkg + "'");
        return pkg;
    }

    /**
     * Replaces the "." with "::" in package name string
     * @param name
     * @return
     */
    public static String getClassName(String name) {
        if(name.indexOf("::") == -1)
            return name;
        StringBuffer sf = new StringBuffer();
        StringTokenizer st = new StringTokenizer(name, "::");
        while(st.hasMoreTokens()) {
            sf.append(st.nextToken() + ".");
        }
        String retVal = sf.toString();
        retVal = retVal.substring(0, retVal.lastIndexOf("."));
        return retVal;
    }

    /**
     * Replaces the "." with "::" in package name string
     * @param name
     * @return
     */
    public static String getQualifiedPackageName(String name) {
        if(name.indexOf(".") == -1)
            return name;
        StringBuffer sf = new StringBuffer();
        StringTokenizer st = new StringTokenizer(name, ".");
        while(st.hasMoreTokens()) {
            sf.append(st.nextToken() + "::");
        }
        String retVal = sf.toString();
        retVal = retVal.substring(0, retVal.lastIndexOf("::"));
        return retVal;
    }

    protected static void showProjectSymbols(IProject p) {
        ETList<IElement> els = p.getElements();
        if (els == null) {
            return ;
        }
        Log.out("Project " + p.getName() + " has " + els.getCount()
                            + " elements");
        for (int i = 0; i < els.getCount(); ++i) {
            IElement el = els.item(i);
            INamedElement nel = null;
            if (el instanceof INamedElement)
                nel = (INamedElement)  el;
            Log.out("" + i + ") " +
                            (nel == null? el.toString() : nel.getName()));
        }
    }

    public static void findAndDeleteSymbolsByName(String fileName) {
        Log.out("The file to delete is " + fileName);
        IProject proj = null; /* NB60TBD UMLSupport.getProjectForPath(fileName);*/

        // Default to the current project if there's nothing better
        if (proj == null)
            proj = UMLSupport.getCurrentProject();
        if(proj != null) {
            Log.out("IProject to search for symbols is == "
                            + proj.getName());
            EventManager.setRoundTripActive(true);
            findAndDeleteSymbol(proj, fileName);
            EventManager.setRoundTripActive(false);
        } else {
            Log.out("Unable to find the project for the path = "
                            + fileName);
        }
    }


    /**
     * Finds the symbols with the 'file' tagged value set to the specified file
     * name and deletes them
     * @param fileName
     */
    private static void findAndDeleteSymbol(INamespace owner, String fileName) {
        ETList<INamedElement> elems = owner.getOwnedElements();
        if(elems != null) {
            for(int i=0; i<elems.getCount(); i++) {
                INamedElement elem = elems.item(i);
                if(elem instanceof IClassifier) {
                    // also delete all the relationships for this class.
                    IClassifier clazz = (IClassifier)  elem;
                    if(ClassInfo.getSymbolFilename(clazz) != null &&
                            ClassInfo.getSymbolFilename(clazz)
                                     .equals(fileName)) {
                        ETList<IAssociation> assos = clazz.getAssociations();
                        if(assos != null) {
                            for(int m=0; m<assos.getCount(); m++) {
                                if(assos.item(m) != null)
                                    assos.item(m).delete();
                            }
                        }
                        ETList<IGeneralization> gens = clazz.getGeneralizations();
                        if(gens != null) {
                            for(int j=0; j<gens.getCount(); j++) {
                                if(gens.item(j) != null)
                                    gens.item(j).delete();
                            }
                        }
                        ETList<IImplementation> impls = clazz.getImplementations();
                        if(impls != null) {
                            for(int k=0; k<impls.getCount(); k++) {
                                if(impls.item(k) != null)
                                    impls.item(k).delete();
                            }
                        }
                        elem.delete();
                    }
                } else if(elem instanceof IPackage) {
                    IPackage p = (IPackage)  elem;
                    findAndDeleteSymbol(p, fileName);
                }
            }
        }
    }

    /**
     * Finds the package and deletes it.
     * @param fileName
     */
    /*public static void findAndDeletePackage(String packageName) {
        IProject owner = GDProSupport.getCurrentProject();
        INamedElements elems = owner.getOwnedElements();
        if(elems != null) {
            for(int i=0; i<elems.getCount(); i++) {
                INamedElement elem = elems.item(i);
                Log.out("ELEMENT TO DELETE : " + elem.getName());
                if(elem instanceof IPackage) {
                    if(elem.getName().equals(packageName)) {
                        IPackage p = (IPackage)  elem;
                        ETSystem.out.println("THE PACKAGE TO DELETE IS : " + p.getName());
                        p.delete();
                    }
                }
            }
        }
    }*/
    /**
     * Deletes a package given its fully qualified name.
     * @param packageName - fully qualified name : "a.b.c"
     */
    public static void findAndDeletePackage(String packageName) {
        Log.out("In findAndDeletePackage()");
        INamedElement elem = findElement(packageName);
        if(elem != null) {
            Log.out("ELEMENT TO DELETE : " + elem.getName());
            if(elem instanceof IPackage) {
                IPackage p = (IPackage)  elem;
                Log.out("THE PACKAGE TO DELETE IS : " + p.getName());
                p.delete();
            }
        }
    }

    public static INamedElement findElement(IProject proj,
                                            String qualifiedName,
                                            Class type) {
        Log.out("Trying to find element " + qualifiedName);

        if (proj == null && (proj = UMLSupport.getCurrentProject()) == null) {
            Log.out("Project is null ...");
            return null;
        }

        Log.out("findClassSymbol: Looking for element " + qualifiedName
                    + " inside project " + proj.getName());

        ITypeManager typeManager = proj.getTypeManager();
        if (typeManager == null) {
            Log.impossible("findClassSymbol: Project "
                           + proj.getName() + " has no ITypeManager");
            return null;
        }

        String umlName = convertJavaToUML(qualifiedName);
        Log.out("findClassSymbol: Asking ITypeManager for " + umlName);
        ETList<INamedElement> elems = typeManager.getLocalCachedTypesByName(umlName);

        int count = elems != null? elems.getCount() : 0;
        if(count == 0){
//            String classToFind = JavaClassUtils.getInnerClassName(qualifiedName);
            IElementLocator loc = new ElementLocator();
            IElementLocator lp = (IElementLocator) loc;
//            elems = lp.findByName(proj, classToFind);
//            count = elems != null? elems.getCount() : 0;
            ETList < IElement > foundItems = lp.findScopedElements(proj, proj.getName() + "::" + umlName);
            elems = new ETArrayList < INamedElement >();
            for(IElement curE : foundItems)
            {
                if(curE instanceof INamedElement)
                {
                    elems.add((INamedElement)curE);
                }
            }
            
            count = elems != null? elems.getCount() : 0;
        }
        for(int i = 0; i < count; i++) {
            INamedElement elem = elems.item(i);
            if (elem == null) continue;
            if (type != null && !type.isAssignableFrom(elem.getClass())) continue;
            String nameToCompare = getFullyQualifiedName(elem);
            if(nameToCompare.equals(qualifiedName)
                   || nameToCompare.replace('$', '.').equals(qualifiedName))
                return elem;
        }
        Log.out(
            "JavaClassUtils.findElement: Unable to find element "
                + qualifiedName);
        return null;
    }

    public static final INamedElement findElement(String qualifiedName,
                                                  Class type) {
        return findElement(null, qualifiedName, type);
    }

    /**
     * Finds the INamedElement given its qualified name
     * @param qualifiedName
     * @return
     */
    public static final INamedElement findElement(String qualifiedName) {
        return findElement(qualifiedName, null);
    }

    /**
     * Finds the IClass element for the qualified name
     * @param qualifiedName
     * @return
     */
    public static IClassifier findClassSymbol(String qualifiedName) {
        if (isPrimitive(qualifiedName))
            return null;

        INamedElement elem = findElement(qualifiedName, IClassifier.class);
        if (elem != null) {
            if (elem instanceof IClass)
                return (IClass)  elem;
            else if (elem instanceof IInterface)
                return (IInterface)  elem;
            else if (elem instanceof IDataType)
                return (IDataType)  elem;
            else
                return (IClassifier)  elem;
        }
        return null;
    }

    /**
     *  Given a class name as com.foo.Outer$Inner$Innermost, returns a String[]
     *  = { "Outer", "Inner", "Innermost" }.
     */
    public static String[] getOuterClassArray(String qualifiedName) {
        String innerCls = getFullInnerClassName(qualifiedName);
        String[] outers = tokenize2Array(innerCls, ".");
        return outers;
    }

    public static String[] tokenize2Array(String str, String tok) {
        StringTokenizer strTok = new StringTokenizer(str, tok);
        Vector tokV = new Vector();
        while (strTok.hasMoreTokens()) {
            tokV.add(strTok.nextToken());
        }
        String[] rets = new String[tokV.size()];
        tokV.copyInto(rets);

        return rets;
    }

    /**
     *  Returns true if the given string has the supplied prefix, ignoring
     * case.
     *
     * @param full   The String to check
     * @param prefix The expected prefix
     * @return <code>true</code> If 'prefix' prefixes 'full'
     */
    public static boolean startsWith(String full, String prefix) {
        if (prefix == null || full == null)
            return false;

        return full.toLowerCase().startsWith(prefix.toLowerCase());
    }

    /**
     *  Returns the name of the specificed file from its absolute path,
     * stripping off extension and directory structure.
     *
     * @param path
     * @return
     */
    public static String getFileName(String path) {
        if (path == null || path.trim().length() == 0)
            return null;
        int lastDelim = path.lastIndexOf(File.separator);
        if (lastDelim != -1)
            path = path.substring(lastDelim + 1);
        int dotPos = path.lastIndexOf('.');
        if (dotPos != -1)
            path = path.substring(0, dotPos);
        return path;
    }

    public static IOperation findOperation(MethodInfo minf) {
        ClassInfo ci = minf.getContainingClass();
        if (ci == null) return null;

        IClassifier c = ci.getClassElement();
        if (c == null)
            c = findClassSymbol(ci.getFullClassName());
        if (c == null) return null;

        return findOperation(c, minf.getName(), minf.getParameters());
    }

    /**
     * Searchs a CLD_Class for a operation that matches the specified parameters
     *
     * @param sym The symbol to search
     * @param params The methods parameters
     * @return IGDAttribute The operation if found, otherwise NULL.
     */
    public static IOperation findOperation(IClassifier sym, String name,
                                           MethodParameterInfo[] params) {
        IOperation retVal = null;

        Log.out("findOperation: Looking for " + name + " in " + sym.getName());
        try {
            if(sym != null) {
                ETList<IOperation> methods = sym.getOperations();
                if (methods != null) {
                    // traverse through all the present methods
                    int count = methods.getCount();
                    for (int i = 0; i < count; i++) {
                        IOperation curOperation = methods.item(i);
                        if (isDesiredOperation(curOperation, name, params)) {
                            retVal = curOperation;
                            break;
                        }
                    }
                }
            }
        }
        catch(Exception ioE) {
            Log.stackTrace(ioE);
        }
        if (retVal != null)
            Log.out("findOperation: Found operation " + retVal.getName());
        else
            Log.out("findOperation: Couldn't find operation " + name);
        return retVal;
    }

    /**
     * Checks the operation is the desired operation.  The name of the operation
     * and it's parameters are check to verify the operation.
     * @param op A Describe "Operations" attribute.
     * @param name The name of the desired operation.
     * @param params The parameters on the desired operation.
     */
    private static boolean isDesiredOperation(IOperation op, String name,
                                              MethodParameterInfo[] params) {
        boolean retVal = false;

        try {
            String       opNameStr        = op.getName();
            ETList<IParameter> opParams   = op.getFormalParameters();
            if(opParams != null && opParams.getCount()>0) {
                if(opParams.item(0).getName() == null)
                    opParams.remove(0);
            }

            if(name.equals(opNameStr)
                            && compareParameters(opParams, params)) {
                retVal = true;
            }

            return retVal;
        }
        catch(Exception ioE) {
            Log.stackTrace(ioE);

        }

        return retVal;
    }

    /**
     * Test if a GDPro operation has the required parameters.
     *
     * @param opParams  The parameters in GDPro to test.
     * @param params The Source code parameters.
     * @return bool true if the parameters match, false otherwise.
     */
    private static boolean compareParameters(ETList<IParameter> params,
                                          MethodParameterInfo[] methodParams) {

        if ((params == null || params.getCount() == 0)
                        && (methodParams == null || methodParams.length == 0))
            return true;

        if ((params == null || params.getCount() == 0)
                        != (methodParams == null || methodParams.length == 0))
            return false;

        // return if the no of params are diff
        if(params.getCount() != methodParams.length) {
            return false;
        }

        for(int i=0; i<methodParams.length; i++) {
            String param = MemberInfo.getArrayTypeName(params.item(i));
            String mParam =
                JavaClassUtils.getInnerClassName(methodParams[i].getType());

            if(param == null) {
                continue;
            }
            if(!param.equals(mParam)) {
                if(mParam.length() == 0)
                {
                    // Check the names because we are in a parameter type change
                    // event.
                    String paramName = params.item(i).getName();
                    String methodParam = methodParams[i].getName();
                    if( paramName.equals(methodParam) == false)
                    {
                        return false;
                    }
                }
                else
                {
                    return false;
                }
            }
        }
        return true;
    }

    public static IStructuralFeature findAttribute(MemberInfo minf) {
        if (minf == null) return null;

        ClassInfo ci = minf.getContainingClass();
        if (ci == null) return null;

        IClassifier c = ci.getClassElement();
        if (c == null) c = findClassSymbol(ci.getFullClassName());
        if (c == null) return null;

        return findAttribute(c, minf.getName());
    }

    /**
     *  Search the attributes of the given classifier for one named 'name'. If
     * no attributes are found, this method will look for an association with a
     * navigable end of the same name.
     *
     * @param sym The IClas to search.
     * @param name The name of the data member.
     * @return If a attribute is found the the attribute, otherwise null.
     */
    public static IStructuralFeature findAttribute(IClassifier sym,
                                                   String name) {
        IAttribute retVal = null;

        try {
            if(sym != null) {
                ETList<INamedElement> els = sym.getOwnedElementsByName(name);

                if (els != null) {
                    int maxC = els.getCount();
                    for (int i = 0; i < maxC; ++i) {
                        INamedElement el = els.item(i);
                        if (el instanceof IAttribute) {
                            return (IAttribute)  el;
                        }
                    }
                }

                // Okay, no luck so far - hunt through the associations
                ETList<IAssociation> assocs = sym.getAssociations();
                if (assocs != null) {
                    int maxA = assocs.getCount();
                    for (int i = 0; i < maxA; ++i) {
                        ETList<IAssociationEnd> ends = assocs.item(i).getEnds();
                        if (ends != null) {
                            int maxE = ends.getCount();
                            for (int j = 0; j < maxE; ++j) {
                                IAssociationEnd end = ends.item(j);
                                if (end.getIsNavigable()) {
                                    INavigableEnd navEnd = (INavigableEnd) end;
                                    if (navEnd.getName().equals(name))
                                        return navEnd;
                                }
                            }
                        }
                    }
                }
            }
        }
        catch(Exception ioE) {
        }

        return retVal;
    }

    public static IAssociation findAssociation(IClassifier cl1,
                                               IClassifier cl2) {
        ETList<IElement> els = new ETArrayList<IElement>();
        els.add(cl1);
        els.add(cl2);

        return determineCommonRelations(els);
    }

    /**
     * Returns the IAssociation element if the association exists between
     * the two classes
     * @param elems
     * @return
     */
    public static IAssociation determineCommonRelations(ETList<IElement> elems) {
        RelationFactory factory = new RelationFactory();
        ETList<IRelationProxy> rels = factory.determineCommonRelations(elems);

        if(rels == null)
            return null;

        int count = rels.getCount();
        for(int i=0; i < count; i++) {
            IRelationProxy rel = rels.item(i);
            if(rel != null) {
                IElement conn = rel.getConnection();
                if(conn instanceof IAssociation)
                    return (IAssociation) conn;
            }
        }

        return null;
    }

    /**
     *  Tags a classifier (even an interface) as a reference class. Reference
     * classes receive special treatment in that the model-source roundtrip
     * ignores events on reference classes and that reference classes may be
     * deleted at any time they are discovered to be orphaned.
     *
     * @param cl
     * @param isRef
     */
    public static void setReferenceClass(IClassifier cl, boolean isRef) {
        setTaggedValue(cl, REF_CLASS, (isRef? "true" : null));
    }

    /**
     *  Returns true if the given classifier should be considered a reference
     * class.
     *
     * @param cl
     * @return
     */
    public static boolean isReferenceClass(IClassifier cl) {
        return (cl != null && cl instanceof IDataType && !(cl instanceof IEnumeration)) ||
                      "true".equals(getTaggedValue(cl, REF_CLASS));
    }

    /**
     * Returns true if this classifier is not a participant in any
     * relationship. Orphan reference classes should be deleted when detected.
     *
     * @param cl
     * @return
     */
    public static boolean isOrphan(IClassifier cl) {
        ETList<IImplementation> imps  = cl.getImplementations();
        ETList<IGeneralization> gens  = cl.getGeneralizations();
        ETList<IGeneralization> specs = cl.getSpecializations();
        ETList<IAssociation>    ascs  = cl.getAssociations();
        ETList<IDependency>     sdeps = cl.getSupplierDependencies();
        ETList<IDependency>     cdeps = cl.getClientDependencies();

        return ( (imps  == null || imps.getCount()  == 0) &&
                 (gens  == null || gens.getCount()  == 0) &&
                 (ascs  == null || ascs.getCount()  == 0) &&
                 (sdeps == null || sdeps.getCount() == 0) &&
                 (cdeps == null || cdeps.getCount() == 0) &&
                 (specs == null || specs.getCount() == 0));
    }



    public static String getPrimitiveWrapperType(String primType)
    {
        if (primType.equals("int"))
            return "Integer";
        
        else if (primType.equals("short"))
            return "Short";
        
        else if (primType.equals("long"))
            return "Long";
        
        else if (primType.equals("float"))
            return "Float";
        
        else if (primType.equals("double"))
            return "Double";
        
        else if (primType.equals("byte"))
            return "Byte";
        
        else if (primType.equals("char"))
            return "Character";
        
        else if (primType.equals("boolean"))
            return "Boolean";
        
        return null;
    }
    
    
    public static final boolean isPrimitive(String type) {
        return primitives.contains(type);
    }
    
    private static final HashSet primitives = new HashSet();

    static {
        primitives.add("int");
        primitives.add("short");
        primitives.add("long");
        primitives.add("float");
        primitives.add("double");
        primitives.add("byte");
        primitives.add("char");
        primitives.add("boolean");
    }

    public static String getPkgName(String name) {
        if(name.indexOf("::") == -1)
            return name;
        StringBuffer sf = new StringBuffer();
        StringTokenizer st = new StringTokenizer(name, "::");
        while(st.hasMoreTokens()) {
            sf.append(st.nextToken() + File.separator);
        }
        String retVal = sf.toString();
        retVal = retVal.substring(0, retVal.lastIndexOf(File.separator));
        return retVal;
    }

  /**
   * When a java file is read-only we just tell the user that the file is read-only so
   * no modification can be done. But if the file is read-only because it is versioned
   * it needs to be checked out to make the modification. When there are changes in the
   * diagram like adding generalization or implementation, the corresponding model
   * elements are checked out. But the java elements are not, so when we get a call to
   * change the source file - we should check if we need to checkout the java file.
   * Currently the way we are using is - if the corresponding model element is versioned
   * and the preference is set to sync model and source for SCM, we need to checkout the
   * java file.
   */
   public static boolean needToCheckOut(ClassInfo cls)
    {
        IClassifier clazz = cls.getClassElement();
        if (clazz.isVersioned())
        {
            UMLSupport gdpro = UMLSupport.getUMLSupport();
            IPreferenceManager2 prefMan = gdpro.getProduct().getPreferenceManager();
            String str = prefMan.getPreferenceValue("ConfigManagement", "SourceSync");
            if (str != null && str.equals("PSK_YES")){
                String filename = cls.getFilename();
                Log.out("Class is versioned, so need to checkout the corresponding java file " + filename);
                ISCMIntegrator integrator = gdpro.getProduct().getSCMIntegrator();
                if (integrator != null)
                {
                    ISCMTool tool = integrator.getSCMToolByWorkspace(UMLSupport.getCurrentWorkspace());
                    if (tool != null)
                    {
                      IEventDispatchController controller = gdpro.getProduct().getEventDispatchController();

//                      if( controller != null )
//                      {
//                          // TODO
//                        IEventDispatcher evDisp = controller.retrieveDispatcher("SCM");
//                        ISCMEventDispatcher scmDisp = (ISCMEventDispatcher) evDisp;
//                        boolean origVal = scmDisp.getPreventAllEvents();
//                        try {
//                      scmDisp.setPreventAllEvents( true );
//                          SCMItemFactory fact = new SCMItemFactory();
//                          ISCMItemGroup grp = new SCMItemGroup();
//                          ISCMOptions opt = new SCMOptions();
//                          ISCMItem scmItem = (ISCMItem) fact.createItem(filename);
//                          Log.out("Added this file to be checked out " + filename);
//                          grp.add(scmItem);
//                          Log.out("Adding corresponding class element too ");
//                          ISCMItem modelItem = (ISCMItem) fact.createItem(clazz.getVersionedFileName());
//                          grp.add(modelItem);
//                          tool.executeFeature(DefaultSCMEventsSink.FK_CHECK_OUT, grp, opt, false);
//                        } catch(Exception exp4) {
//                          Log.out("Exception while executing Feature for SCM");
//                          Log.stackTrace(exp4);
//                        }
//                        finally {
//                          scmDisp.setPreventAllEvents( origVal );
//                        }
//                      }
                      return true;
                    }
                }
            }
            Log.out("Returning after checkout");
        }
        Log.out("No need to checkout");
        return false;
    }

    /**
     *  When a file is renamed, the model element does not change its name, the
     * versioned element name remains same. But this is not the case with the
     * java source file.  So we need to checkin the new source file. Later on
     * this code needs to be modified so that the old file can be removed from
     * the source control - needs some more support in ISCMOptions - like
     * removeLocalCopy.
     */
    public static void checkInNewFile(ClassInfo oldC, ClassInfo newC,
                                      String oldFile) {
        if (oldC == null || newC == null) {
            Log.err("JCU: Can't check in file without ClassInfos");
            return;
        }

        String newName = newC.getFilename();

        IClassifier clazz = oldC.getClassElement();
        if (clazz == null) clazz = newC.getClassElement();

        // We need the IClassifier for the oldC ClassInfo; check if that's
        // available
        if (clazz == null) {
            Log.err("JCU: Can't find model element for " + oldC);
            return;
        }

        // If the caller didn't supply the old filename, retrieve it from the
        // model.
        if (oldFile == null) {
            oldFile = oldC.updateFilename(null);
            Log.out("JCU: oldFile retrieved was " + oldFile);
        }

        Log.out("JCU: Change event for " + oldFile + newName);
        if (newName.equalsIgnoreCase(oldFile.toLowerCase()))
        {
            Log.out("JCU: Old and new files are same, so returning");
            return;
        }
        else
            Log.out("JCU: Files are different");
        UMLSupport gdpro = UMLSupport.getUMLSupport();
        IPreferenceManager2 prefMan = gdpro.getProduct().getPreferenceManager();
        String isSCMEnabled = prefMan.getPreferenceValue("ConfigManagement", "Enabled");
        if(isSCMEnabled == null || isSCMEnabled.equals("PSK_NO"))
            return;
        String str = prefMan.getPreferenceValue("ConfigManagement", "SourceSync");
        if (str != null && str.equals("PSK_YES")){
            if (!clazz.isVersioned()){
                Log.out("JCU: " + clazz.getName() +
                        " is not versioned, so not adding the renamed " +
                        "file to version control" );
                return;
            }

            ISCMIntegrator integrator = gdpro.getProduct().getSCMIntegrator();
            IWorkspace work = UMLSupport.getCurrentWorkspace();
            ISCMTool tool = integrator.getSCMToolByWorkspace(work);
            if (tool != null)
            {
               IEventDispatchController controller = gdpro.getProduct().getEventDispatchController();

               if( controller != null )
               {
                 IEventDispatcher evDisp = controller.retrieveDispatcher("SCM");
                 // TODO:
//                 ISCMEventDispatcher scmDisp = (ISCMEventDispatcher) evDisp;
//                 boolean origVal = scmDisp.getPreventAllEvents();
//                 try {
//                   scmDisp.setPreventAllEvents( true );
//                   ISCMItemGroup group = new SCMItemGroup();
//                   SCMItemFactory fact = new SCMItemFactory();
//                   ISCMItem scmItem = fact.createItem(newName);
//                   group.add(scmItem);
//                   ISCMOptions options = new SCMOptions();
//                   Log.out("Adding the new file to the source control");
//                   tool.executeFeature(DefaultSCMEventsSink.FK_ADD_TO_SOURCE_CONTROL, group, options, false);
//                 } catch(Exception exp4) {
//                   Log.out("Exception while executing Feature for SCM");
//                   Log.stackTrace(exp4);
//                 }
//                 finally {
//                   scmDisp.setPreventAllEvents( origVal );
//                 }
               }
            }
        }
    }

    /**
     * Given a fully-qualified Java element name, returns the parent of that
     * element. The element name may be delimited by '.' (package names) and '$'
     * (inner class names).
     *
     * @param element The fully-qualified Java element name.
     * @return The fully-qualified name of the parent element, "" if the element
     *         has no parent, or null if element is null.
     */
    public static String getParentName(String elementName) {
        if (elementName == null) return null;

        int dot = elementName.lastIndexOf('.'),
            dol = elementName.lastIndexOf('$');
        if (dol > dot) dot = dol;
        if (dot == -1) return "";
        return elementName.substring(0, dot);
    }

    public static String replaceDollarSign(String string)
    {
        return StringUtilities.replaceAllSubstrings(string, "$", ".");
    }
    
    public static IClassifier getOuterMostOwner(IClassifier clazz)
    {
        IElement owner = clazz.getOwner();
        
        if (owner == null || owner instanceof IProject ||
            owner instanceof IPackage)
        {
            return clazz;
        }
        
        else if (owner instanceof IClassifier)
            return getOuterMostOwner((IClassifier)owner);
        
        else
            return null;
    }

    public static boolean isAnOwner(IClassifier elem, IClassifier clazz)
    {
	if (elem == null || clazz == null) 
	{
	    return false;
	}

	if (elem.equals(clazz)) 
	{
	    return true;
	}

        IElement owner = clazz.getOwner();
        
        if (owner == null || owner instanceof IProject ||
            owner instanceof IPackage)
        {
            return false;
        }
        
        else if (owner instanceof IClassifier) 
	{
	    return isAnOwner(elem, (IClassifier)owner);	    
	}
	else 
            return false;
    }

}
