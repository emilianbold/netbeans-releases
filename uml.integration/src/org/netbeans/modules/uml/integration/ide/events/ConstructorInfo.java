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
 * File         : ConstructorInfo.java
 * Version      : 1.6
 * Description  : Describes a constructor
 * Author       : Trey Spiva
 */
package org.netbeans.modules.uml.integration.ide.events;

import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IDerivationClassifier;
import org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent.JavaMethodChangeFacility;
import org.netbeans.modules.uml.core.support.umlutils.ElementLocator;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.ListIterator;

import org.netbeans.modules.uml.integration.ide.ChangeUtils;
import org.netbeans.modules.uml.integration.ide.JavaClassUtils;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter;
import org.netbeans.modules.uml.core.support.umlsupport.Log;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.integration.ide.UMLSupport;
//import org.netbeans.modules.uml.integration.netbeans.NBEventProcessor;

/**
 * The ConstructorInfo communicates with the EventManager to update Describe.
 * ConstructorInfo is still a high level class.  It knows how to communicate
 * with the EventMangaer to update Describe, but it does not know any of the
 * details of how to update Describe.
 * <br>
 * Because ConstructorInfo contains both before and after data, ConstructorInfo
 * is able to search for the constructor and update it to how the source file
 * current represents the constructor.
 * <br>
 * For Wolverine, the ConstructorInfo may also carry data for model-source
 * roundtrip events. In such cases, only the 'before' data fields are used.
 *
 * Revision History
 * No.  Date        Who         What
 * ---  ----        ---         ----
 *   1  2002-05-03  Darshan     Added methods to manipulate parameters using
 *                              IParameters. These are needed because of the
 *                              changes to Wolverine's method-parameter-change
 *                              event mechanism in build 64.
 *                              Also reformatted to use 4-space tabs and Java
 *                              style brace positioning.
 *   2  2002-05-06  Darshan     Added constructor to take a ClassInfo and an
 *                              IOperation for model-source work.
 *
 * @see EventManager
 * @see MethodInfo
 * @author Trey Spiva
 */
public class ConstructorInfo extends ElementInfo {
    private ClassInfo  mContainer      = null;
    private LinkedList mOrigParameters = null;
    private LinkedList mNewParameters  = null;
    private LinkedList mExceptions     = null;
    private String mSource     = null;
     private JavaMethodChangeFacility facility = new JavaMethodChangeFacility();

    /**
     *  Intiailizes a new ConstructorInfo.
     *  @param container The class that contains the data member.
     *  @param type The transaction type.
     *  @see ElementInfo
     */
    public ConstructorInfo(ClassInfo container, int type) {
        super(type);
        setContainingClass(container);
    }

    public ConstructorInfo(ClassInfo container, IOperation op) {
        super(op);
        setContainingClass(container);
    }

    /**
     * Retrieves the containing clas of the data member.
     * @return The containing class.
     */
    public ClassInfo getContainingClass() {
        return mContainer;
    }

    /**
     * Sets the containing clas of the data member.
     * @param container The containing class.
     */
    public void setContainingClass(ClassInfo container) {
        mContainer = container;
    }

    public void syncFields() {
        super.syncFields();
        if (getNewParameters() != null) {
            setParameters(getNewParameters());
            setNewParameters(null);
        }
    }

    /**
     * Retrieves all of the constructor's parameters.
     * @return An array of MethodParameterInfo objects.
     */
    public MethodParameterInfo[] getParameters() {
        // D: The caller had better set the parameters correctly!
        if(mOrigParameters == null)
            return null;

        // Convert to an array
        MethodParameterInfo[] retVal = new MethodParameterInfo[
                                                mOrigParameters.size()];
        for(int i = 0; i < retVal.length; i++)
            retVal[i] = (MethodParameterInfo)mOrigParameters.get(i);

        return retVal;
    }

    /**
     * Retrieves a specific parameter information.
     * @return A MethodParameterInfo object.
     * @throws IndexOutOfBoundsException
     */
    public MethodParameterInfo getParameters(int index)
            throws IndexOutOfBoundsException {
        if(mOrigParameters == null)
            throw new IndexOutOfBoundsException();

        return (MethodParameterInfo)mOrigParameters.get(index);
    }

    /**
     * Sets the constructors parameters. This is setting the original parameters.
     * @param params All the parameters.
     */
    public void setParameters(MethodParameterInfo[] params) {
        if (params != null)
            mOrigParameters = new LinkedList(Arrays.asList(params));
        else
            mOrigParameters = null;
    }

    /**
     * Adds a parameters to a specific location in the parameters list.  This is
     * setting the original parameters.
     * @param index The position that the parameter should fill.
     * @param param The parameter.
     */
    public void addParameter(int index,MethodParameterInfo param) throws IndexOutOfBoundsException {
        if(mOrigParameters == null)
            mOrigParameters = new LinkedList();

        mOrigParameters.add(index, param);
    }

    /**
     * Removes all the parameter from the parameter list.  This does not mean
     * that the parameters will be removed from Describe, only from the
     * ConstructorInfo.
     *
     * This is setting the original parameters.
     */
    public void clearParameters() {
        mOrigParameters = null;
    }

    /**
     * Remove a specific parameter from the collection of parameters.  This does
     * not mean that the parameter will be removed from Describe, only from
     * the ConstructorInfo.  This is setting the original parameters.
     * @param index The parameter to be removed.
     */
    public void removeParameter(int index) {
        if (mOrigParameters != null)
            mOrigParameters.remove(index);
    }

    public MethodParameterInfo getParameter(IParameter param) {
        // EARLY EXIT
        if (mOrigParameters == null) return null;

        ListIterator i = mOrigParameters.listIterator();

        if (i == null)
            return null;

        while (i.hasNext()) {
            MethodParameterInfo mpi = (MethodParameterInfo) i.next();
            if (mpi.getName().equals(param.getName()) &&
                            JavaClassUtils.getInnerClassName(
                                mpi.getType()).equals(
                                param.getTypeName()))
                return mpi;
        }
        return null;
    }

    public void removeParameter(IParameter param) {
        MethodParameterInfo mpi = getParameter(param);
        if (mpi != null)
            mOrigParameters.remove(mpi);
    }

    public void changeParameter(IParameter orig, IParameter newp) {
        MethodParameterInfo mpi = getParameter(orig);
        if (mpi != null) {
            mpi.setName(newp.getName());
            mpi.setType(newp.getTypeName());
        }
    }

    // New Parameters
    public MethodParameterInfo[] getNewParameters() {
        MethodParameterInfo[] retVal = null;
        if(mNewParameters != null) {
            // Convert to an array
            retVal = new MethodParameterInfo[mNewParameters.size()];
            for(int i = 0; i < retVal.length; i++)
                retVal[i] = (MethodParameterInfo)mNewParameters.get(i);
        }

        return retVal;
    }

    /**
     * Sets the constructors parameters. This is setting the new parameters.
     * @param params All the parameters.
     */
    public MethodParameterInfo getNewParameters(int index) throws IndexOutOfBoundsException {
        if(mNewParameters == null)
            throw new IndexOutOfBoundsException();

        return (MethodParameterInfo)mNewParameters.get(index);
    }

    /**
     * Sets the constructors parameters. This is setting the new parameters.
     * @param params All the parameters.
     */
    public void setNewParameters(MethodParameterInfo[] params) {
        if (params != null)
            mNewParameters = new LinkedList(Arrays.asList(params));
        else
            mNewParameters = null;
    }

    /**
     * Adds a parameters to a specific location in the parameters list.  This is
     * setting the new parameters.
     * @param index The position that the parameter should fill.
     * @param param The parameter.
     */
    public void addNewParameter(int index,MethodParameterInfo param)
            throws IndexOutOfBoundsException {
        if(mNewParameters == null)
            mNewParameters = new LinkedList();

        mNewParameters.add(index, param);
    }

    /**
     * Removes all the parameter from the parameter list.  This does not mean that
     * the parameters will be removed from Describe, only from the ConstructorInfo.
     * This is setting the new parameters.
     */
    public void clearNewParameters() {
        mNewParameters = null;
    }

    /**
     * Remove a specific parameter from the collection of parameters.  This does
     * not mean that the parameter will be removed from Describe, only from
     * the ConstructorInfo.  This is setting the new parameters.
     * @param index The parameter to be removed.
     */
    public void removeNewParameter(int index) {
        mNewParameters.remove(index);
    }

    /**
     * Retrieves the exceptions that can be thrown by the constructor.
     * @return The list of thrown exceptions.
     */
    public String[] getExceptions() {
        String[] retVal = null;
        if((mExceptions != null) && (mExceptions.size() > 0)) {
            // Convert to an array
            retVal = new String[mExceptions.size()];
            for(int i = 0; i < retVal.length; i++)
                retVal[i] = (String)mExceptions.get(i);
        }

        return retVal;
    }

    /**
     * Sets the exceptions that can be thrown by the constructor.
     * @param value The list of thrown exceptions.
     */
    public void setExceptions(String[] value) {
        mExceptions = null;
        mExceptions = new LinkedList(Arrays.asList(value));
    }

    /**
     * Adds a exceptions that can be thrown by the constructor.
     * @param An exception that can be thrown.
     */
    public void addException(String value) {
        if(mExceptions == null)
            mExceptions = new LinkedList();

        mExceptions.add(value);
    }

    /**
     * Removes a exceptions that can be thrown by the constructor.
     * @param An exception that can be thrown.
     */
    public void removeException(String exception) {
        mExceptions.remove(exception);
    }

    /**
     * Updates the constructor.  A Class Transaction is began and {@link #update(GDSymbolTransaction trans) update}
     * is called.
     */
    public void update() {
        if(getContainingClass() != null) {
            SymbolTransaction trans =
                new SymbolTransaction(getContainingClass());
            update(trans);
        }
    }

    protected void checkSanity() {
        if (getChangeType() == MODIFY && mNewParameters != null &&
                mNewParameters.size() > 0) {
            // Check whether parameter lists are the same
            MethodParameterInfo[] oldP = getParameters(),
                                  newP = getNewParameters();
            if (Arrays.equals(oldP, newP))
                setNewParameters(null);
        }
    }

    /**
     *  Returns whether the parameter list of this constructor needs to be
     * updated to the Describe model.
     *
     * @return <code>true</code> if the model needs to be updated.
     */
    protected boolean needParameterUpdate() {
        return getNewParameters() != null;
    }

    /**
     * Updates the constructor using the specified Symbol transaction.
     * @param trans The transaction that is to be used to update the correct symbol.
     * @return The method transaction that was created to update the data member.
     */
    public MethodTransaction update(SymbolTransaction trans) {
        checkSanity();

        Log.out("GDMethodTransaction.update(): Updating " + this);
        MethodTransaction retVal = null;
        EventManager manager = EventManager.getEventManager();
        Log.out("GDMethodTransaction.update(): Creating GDMethodTransaction");
        retVal = new MethodTransaction(trans, this);
        Log.out("GDMethodTransaction.update(): Done Creating GDMethodTransaction");
        IOperation op =	retVal.getOperation();
        
        Log.out("GDMethodTransaction.update(): IOperation is " + op);
        
        if(retVal != null && op != null) {
            if(getChangeType() == ElementInfo.DELETE) {
                Log.out("About to delete the method ..");
                facility.deleted(op, op.getFeaturingClassifier(), true ); //JM added this
                manager.deleteMethod(retVal);
                return null;
            }

            if(op.getRaisedExceptions() != null) {
                ETList<IClassifier> existingExceptions = op.getRaisedExceptions();
                for(int i = 0; i < existingExceptions.getCount(); i++)
                    op.removeRaisedException(existingExceptions.item(i));
            }
            if(getExceptions() != null) {
                Log.out("Setting raised exceptions for the method....");
                String[] exceptions = getExceptions();
                for(int i = 0; i < exceptions.length; i++){
                    IClassifier clazz = JavaClassUtils.findClassSymbol(exceptions[i]);
                    if(clazz == null)
                        clazz = JavaClassUtils.createDataType(exceptions[i]);
                     op.addRaisedException(clazz);
                }
            }

            if(isCommentSet()) {
                Log.out("Setting the comment for method to =========" + getComment());
                op.setDocumentation(getComment());
            }

            if(getModifiers() != null) {
                Log.out(" Updating modifiers........");
                manager.updateMethodModifers(retVal, getModifiers().intValue());
            }

            if(getNewName() != null && !getNewName().equals(getName())) {
//                op.setName(getNewName());
                facility.changeName(op, getNewName(), false, true);
            }

            if (needParameterUpdate())
                updateParameters(retVal);
            
            if (getChangeType() == ElementInfo.MODIFY) {
                if (needParameterUpdate()) {
                    facility.typeChanged(op, true);
                }
            }
            
            IClassifier sym = trans.getSymbol();
            if(sym != null && !(sym.findMatchingOperation(op) == op)) {
                // add newly created operation to class, when all stuff related to it (like parameters, modifiers, etc.) is set
                facility.added(op, true);
                sym.addOperation(op);
            }
        }
        return retVal;
    }

    protected ETList<IParameter> getParameterCollection(IOperation op) {
        return new ETArrayList<IParameter>();
    }

    protected void updateParameters(MethodTransaction trans)
    {
        // Create the IParameter for each new parameter added and add it to the
        // IOperation
        EventManager.getEventManager().getEventFilter().blockEventType(
                ChangeUtils.RDT_DEPENDENCY_ADDED);
        try
        {
            IOperation op = trans.getOperation();
            if (getNewParameters() != null)
            {
                ETList<IParameter> pars = getParameterCollection(op);
                MethodParameterInfo[] params = getNewParameters();
                for(int i = 0; i < params.length; i++)
                {
                    MethodParameterInfo param = params[i];
                    
                    String type = param.getType();
                    int mul = MemberInfo.getMultiplicity(type);
                    type = MemberInfo.getTypeName(type);
                    
                    IParameter ip = null;
                    IDerivationClassifier derivation = getDerivationClassifer(op, type);
                    if(derivation != null)
                    {
                        ip = op.createParameter2(derivation, param.getName());
                    }
                    else
                    {
                        String typename = JavaClassUtils.convertJavaToUML(type);
                        ip = op.createParameter(typename, param.getName());
                    }
                    MemberInfo.setMultiplicity(ip, mul, 0);
                    Log.out("Adding new param: "
                            + JavaClassUtils.convertJavaToUML(param.getType()) + " "
                            + param.getName());
                    
                    pars.add(ip);
                }
                Log.out("Calling setParameters() for operation " + getName());
                try
                {
                    UMLSupport.setRoundTripEnabled(true);
                    /* NB60TBD NBEventProcessor.setUpdatingModel(true); */
                    op.setParameters(pars);
                }
                finally
                {
                    UMLSupport.setRoundTripEnabled(false);
                    /* NB60TBD NBEventProcessor.setUpdatingModel(false); */
                }
                
                Log.out("Done calling setParameters() for operation " +
                        getName());
            }
        }
        finally
        {
            EventManager.getEventManager().getEventFilter().unblockEventType(
                    ChangeUtils.RDT_DEPENDENCY_ADDED);
        }
    }
    
    protected IDerivationClassifier getDerivationClassifer(IElement owner, String name)
    {
        IDerivationClassifier retVal = null;
        
        if(name.indexOf('<') > 0)
        {
            // We have a template instance.  We should create a derived 
            // classifier instead of a DataType.
            ElementLocator pLocator = new ElementLocator();
            ETList < IElement > elements = pLocator.findScopedElements(owner, name);
            if((elements != null) && (elements.size() > 0))
            {
                retVal = (IDerivationClassifier)elements.get(0);
            }
            else
            {
                FactoryRetriever fact = FactoryRetriever.instance();
                Object obj = fact.createType("DerivationClassifier", null);
                if (obj != null && obj instanceof IDerivationClassifier)
                {
                   IDerivationClassifier nEle = (IDerivationClassifier)obj;
                   nEle.setName(name);
                   owner.getProject().addOwnedElement(nEle);
                   retVal = nEle;
                }
            }
        }
        
        return retVal;
    }
    
    public String getCode() {
        return "I";
    }

    public Object clone() {
        ConstructorInfo ci = (ConstructorInfo) super.clone();
        if (mOrigParameters != null) {
            ci.mOrigParameters = (LinkedList) mOrigParameters.clone();
            deepClone(ci.mOrigParameters);
        }
        if (mNewParameters != null) {
            ci.mNewParameters = (LinkedList) mNewParameters.clone();
            deepClone(ci.mNewParameters);
        }
        if (mExceptions != null)
            ci.mExceptions = (LinkedList) mExceptions.clone();
        return ci;
    }

    protected void deepClone(LinkedList l) {
        ListIterator li = l.listIterator();
        while (li.hasNext()) {
            Object obj = li.next();
            if (obj instanceof MethodParameterInfo) {
                MethodParameterInfo mpi = (MethodParameterInfo) obj,
                                    oth = (MethodParameterInfo) mpi.clone();

                li.remove();
                li.add(oth);
            }
        }
    }

    protected String getDisplayString(String fnName,
                                      MethodParameterInfo[] mpi) {
        StringBuffer text = new StringBuffer(fnName);
        text.append('(');

        for (int i = 0; mpi != null && i < mpi.length; ++i) {
            if (i > 0)
                text.append(", ");
            text.append(mpi[i].getName())
                .append(": ")
                .append(mpi[i].getType());
        }
        text.append(')');
        return text.toString();
    }

    public String toString() {
        int mods = getModifiers() != null? getModifiers().intValue() : 0;
        String original = getDisplayString(getName(), getParameters());
        if (getNewName() != null || getNewParameters() != null) {
            String newName = getNewName() != null? getNewName() : getName();
            MethodParameterInfo[] mpi = getNewParameters() != null?
                                            getNewParameters() :
                                            getParameters();
            original += " -> " + getDisplayString(newName, mpi);
        }
        return "(" + getChangeName() + ") " + Modifier.toString(mods) + " "
                 + original + getExceptionString();
    }

    public void setSource(String src) {
        mSource = src;
    }

    public String getSource() {
        return mSource;
    }

    private String getExceptionString() {
        StringBuffer excStr = new StringBuffer();
        String[] ex = getExceptions();
        if (ex != null && ex.length > 0) {
            excStr.append(" throws ");
            for (int i = 0; i < ex.length; ++i) {
                if (i > 0) excStr.append(", ");
                excStr.append(ex[i]);
            }
        }
        return excStr.toString();
    }
}
