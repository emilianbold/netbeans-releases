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
 * SymbolTransaction.java
 *
 * Created on February 3, 2001, 8:08 AM
 */

package org.netbeans.modules.uml.integration.ide.events;

import org.netbeans.modules.uml.core.support.umlutils.ElementLocator;
import java.io.File;
import java.util.StringTokenizer;

import org.netbeans.modules.uml.integration.ide.UMLSupport;
import org.netbeans.modules.uml.integration.ide.JavaClassUtils;
//import org.netbeans.modules.uml.integration.ide.Log;
import org.netbeans.modules.uml.core.IUMLCreationFactory;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlsupport.Log;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.ProductHelper;


/**
 * SymbolTransaction is used to handle the state of storing GDPro information.
 *
 * Revision History
 * No.  Date        Who         What
 * ---  ----        ---         ----
 *   1  2002-05-21  Darshan     Changed .getUMLCreationFactory() call from
 *                              application to product.
 *   2  2002-06-19  Darshan     Removed unnecessary conversions between Java
 *                              and UML fully scoped class/package names.
 * @todo Cleanup all commented out code.
 * @author  tspiva
 * @version
 */
public class SymbolTransaction {
    private IClassifier    mGDSymbol            = null;

    // Changed for wolverine, currently active IProject
    //protected static IProject aProj = null;


    /**
     * Creates and initializes a new symbol transaction.  Describe is
     * required to specify where to search for the symbol data.
     * @param system The system to use.
     */
    public SymbolTransaction() {
        this(null);
    }

    /**
     * Creates and initializes a new symbol transaction.  The Describe is
     * required to specify where to search for the symbol data.
     * @param info The symbol to find or create it it does not exist.
     * @param system The system to use.
     */
    public SymbolTransaction(ClassInfo info) {
        //setProject();
        if(info != null) {
            setSymbol(info);
        }
    }

    protected void finalize() {
        mGDSymbol = null;
    }


    public  IClassifier getSymbol() {
        return mGDSymbol;
    }

    /**
     * Sets the Describe system to use when retrieving the symbols information.
     * Therefore, the symbol will also reside in the Describe system.
     * @param value The Describe system.
     */
//    protected void setProject() {
//        aProj = GDProSupport.getCurrentProject();
//    }


    /**
     * Sets the symbol to use for the transaction.  A transaction only start
     * when a symbol has been set.  I commit transaction <B>MUST</B> be called
     * after the transaction is completed.
     *
     * @param sym The symbol to be used during the transaction.
     * @see #commitChanges
     */
    public void setSymbol(IClassifier sym) {
        mGDSymbol = sym;
    }

    /**
     * Sets the symbol to use for the transaction.  Describe will be used to
     * locate the symbol specified in the ClassInfo.  If a Describe symbol is
     * not found then a new symbol is created. A transaction only start when a
     * symbol has been set.  I commit transaction <B>MUST</B> be called after
     * the transaction is completed.
     *
     * @param sym The symbol to be used during the transaction.
     * @see #commitChanges
     */
    public void setSymbol(final ClassInfo info) {
        Log.out("Inside setSymbol ..");
        // First set the symbol to null to allow the current symbol to be GC
        mGDSymbol = null;
        IProject system = null; /* NB60TBD UMLSupport.getProjectForPath(info.getFilename()); */
//        IProject system = UMLSupport.getCurrentProject();
//        IProject system = info.getProject();
        if(system != null) {
            // Get the UML qualified name in UML
            setSymbol(
                createClass(
                    info.getName(),
                    info.getPackage(),
                    info.isInterface(),
                    info.isEnumeration(),
                    null,
                    info.getChangeType(),
                    info.getFilename()));
        }
    }

    /**
     * Retrieves an inner class from is transactions Describe symbol.  If the
     * inner class does not exist then a new class symbol is created and the
     * transaction is returned.
     * @param info The inner class to retieve.
     * @return The inner class transaction.
     */
    public SymbolTransaction getInnerClass(ClassInfo info) {
        SymbolTransaction innerTrans = new SymbolTransaction(info);

        // Test if the found symbol already has the inner class association.
        // If it does not create the association.
        IClassifier outerSym = getSymbol();
        IClassifier innerSym = innerTrans.getSymbol();

        try {
            if((outerSym != null) && (innerSym != null)) {
                // Now it is possible for one class to play in multiple inner
                // class relationships.  So, if the inner class does not already
                // in an inner class relationship with the outer class then put
                // it in the inner class relationship.
                Log.out("Trying to add the inner class ..... ");
                boolean    foundIt   = findInnerClass(outerSym, innerSym);
                Log.out("fountIt = " + foundIt);
                if(!foundIt) {
                    innerSym.setOwner(outerSym);
                }

            }
        } catch(Exception ioE) {
            Log.stackTrace(ioE);
        }

        return innerTrans;
    }

    // *************************************************************************
    // Helper Methods
    // *************************************************************************

    /**
     * checks if the inner class with the given name already exists for this
     * symbol
     *
     * @param clazz
     */
    public boolean findInnerClass(IClassifier clazz, IClassifier inner) {
        ETList<INamedElement> els = clazz.getOwnedElements();

        int c = els.getCount();
        for (int i = 0; i < c; ++i) {
            IElement el = els.item(i);
            if (el instanceof IClass) {
                //IClass innerCl = (IClass) el;
                IClass innerCl = (IClass) el;
                if(innerCl.getName().equals(inner.getName())) {
                    Log.out("The inner class with the name " + inner.getName()
                                + " already exists in " + clazz.getName());
                    return true;
                }
            } else {
                Log.out("The owned element type is " + el.getElementType());
            }

        }
        return false;
    }

    
    public static IClassifier createClass(String name, String pk,
            boolean isInterface, boolean isEnumeration, IProject proj, int changeType, String file) {

        // First, try to find if the class already present
        String qualifiedName = JavaClassUtils.formFullClassName(pk, name);
        IClassifier orig = JavaClassUtils.findClassSymbol(qualifiedName);

        Log.out("createClass: name = " + name);
        if (orig != null && JavaClassUtils.isReferenceClass(orig)
                && changeType == ElementInfo.CREATE) {
            Log.out("createClass: Found data type : " + name);
            Log.out("createClass: Transforming data type");
            orig = orig.transform(isInterface? ClassInfo.DS_INTERFACE :
                                        (isEnumeration ? ClassInfo.DS_ENUMERATION : ClassInfo.DS_CLASS));
        }

//        String fullyQName = qualifiedName.replaceAll("$", "::");
//        orig = JavaClassUtils.findClassSymbol(fullyQName);
        
        if (orig != null || changeType != ElementInfo.CREATE) {
            return orig;
        }

        IUMLCreationFactory fact = getUMLCreationFactory();
        IClassifier clazz = null;
        if(isInterface) {
            clazz = fact.createInterface(null);
        } else if (isEnumeration) {
            Log.out("createClass: Creating enumeration : " + name);
            clazz = fact.createEnumeration(null);
        } else {
            Log.out("createClass: Creating class : " + name);
            clazz = fact.createClass(null);
        }

        if (proj == null)
            proj = UMLSupport.getCurrentProject();

        // for inner classes set the parents as required
        if(qualifiedName.indexOf("$") >0) {
            // find the last outer class
            String parent = qualifiedName.substring(0,
                                        qualifiedName.lastIndexOf("$"));
            IClassifier cl = JavaClassUtils.findClassSymbol(parent);
            if(cl != null) {
                cl.addOwnedElement(clazz);
            }
        } else {
            if(pk != null && !pk.trim().equals("")) {
                IPackage pack = getClassPackage(pk, file);
                pack.addOwnedElement(clazz);
            } else {
                proj.addOwnedElement(clazz);
            }
        }
        Log.out("createClass: Setting class name to " +
                JavaClassUtils.getInnerClassName(name));
        clazz.setName(JavaClassUtils.getInnerClassName(name));

        return clazz;
    }

    public static INamedElement getClassOwner(String qualifiedName) {
        // for inner classes set the parents as required
        if(qualifiedName.indexOf("$") >0) {
            // find the last outer class
            String parent = qualifiedName.substring(0,
                                            qualifiedName.lastIndexOf("$"));
            IClassifier cl = JavaClassUtils.findClassSymbol(parent);
            return cl;
        } else {
            String pk = JavaClassUtils.getPackageName(qualifiedName);
            if(pk != null && !pk.trim().equals("")) {
                IPackage pack = getClassPackage(pk, null);
                return pack;
            } else
                return UMLSupport.getCurrentProject();
        }
    }

//    public IProject getProject() {
//        return aProj;
//    }

    public static IPackage getClassPackage(String pk, String filename) {
        Log.out("getClassPackage(" + pk + ", " + filename + ")");
        IUMLCreationFactory fact = getUMLCreationFactory();
        // pk.replaceAll(".", "::"); not working
        pk = JavaClassUtils.getQualifiedPackageName(pk);
        StringTokenizer st = new StringTokenizer(pk, "::");
        int i=0;
        IPackage prev = null;
        IPackage pack = null;
        String packName = null;

        IPackage base = null;
        while(st.hasMoreTokens()) {
            ++i;
            prev = pack;
            packName = st.nextToken();
            pack = JavaClassUtils.findScopedPackage(packName, prev);
            if(pack != null) // package already exists
                continue;
            // create new package
            Log.out("Creating package " + packName);
            pack = fact.createPackage(null);
            pack.setName(packName);
            if(i == 1 && prev == null) { // if first set the owner as proj
                UMLSupport.getCurrentProject().addOwnedElement(pack);
            } else {
                prev.addOwnedElement(pack);
            }

            // TODO: 
//            if (pack.getIsTopLevelPackage() || i == 1)
//                base = pack;
//            else
//                Log.out(pack.getName() + " is not a top-level package");
        }

        if (base != null && filename != null) {
            File sourceDir = new File(filename);
            while (i-- >= 0)
                sourceDir = sourceDir.getParentFile();
            Log.out("getClassPackage: Setting source directory for "
                    + base.getName() + " to " + sourceDir);
            base.setSourceDir(sourceDir.toString());
            Log.out("getClassPackage: New source directory for "
                    + base.getName() + " is " + base.getSourceDir());
        }

        return pack;
    }

    public static IUMLCreationFactory getUMLCreationFactory() {
        IUMLCreationFactory fact = 
            (IUMLCreationFactory) ProductHelper.getProduct().getCreationFactory();
        return fact;
    }
}
