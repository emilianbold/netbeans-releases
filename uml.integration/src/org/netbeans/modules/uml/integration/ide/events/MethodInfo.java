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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.uml.integration.ide.events;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;

import org.netbeans.modules.uml.common.Util;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IDerivationClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.Classifier;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;

import org.netbeans.modules.uml.integration.ide.ChangeUtils;
import org.netbeans.modules.uml.integration.ide.JavaClassUtils;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IDependency;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlsupport.Log;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.integration.ide.UMLSupport;

/**
 * The MethodInfo communicates with the EventManager to update Describe.
 * MethodInfo is still a high level class.  It knows how to communicate
 * with the EventMangaer to update Describe, but it does not know any of the
 * details of how to update Describe.
 * <br>
 * Because MethodInfo contains both before and after data, MethodInfo
 * is able to search for the metohd and update it to how the source file current
 * represents the method.
 *
 * Revision History
 * No.  Date        Who         What
 * ---  ----        ---         ----
 *   1  2002-04-25  Darshan     Reformatted to 4-space indent and added
 *                              constructor to create MethodInfo from an
 *                              IOperation
 *   2  2002-04-26  Darshan     Fixed dropped modifiers, corrected handling
 *                              of return types and parameters when
 *                              constructing a MethodInfo off an IOperation.
 *   3  2002-04-26  Darshan     Fixed NullPointerException when retrieving
 *                              return type for constructors.
 *   4  2002-04-30  Darshan     Used JavaClassUtils to map Describe's modifiers
 *                              to Java modifiers.
 *   5  2002-05-02  Darshan     Refixed using JavaClassUtils to map modifiers
 *                              - the wrong modifier was still being used.
 *   6  2002-05-03  Darshan     Kludged method parameter type retrieval to
 *                              default to int if no parameter type is found.
 *                              This prevents faulty events from producing
 *                              an unparseable Java file.
 *   7  2002-05-06  Darshan     Changed the MethodInfo(ClassInfo, IOperation)
 *                              constructor to call ElementInfo(INamedElement)
 *                              for model-source work.
 *   8  2002-05-15  Darshan     Added toString() method.
 *   9  2002-05-22  Darshan     Added code to handle method concurrency.
 *  10  2002-06-04  Darshan     Fixed bug with not picking up method parameters
 *                              correctly (issue #19).
 *  11  2002-06-14  Darshan     Fixed bug with code assuming that the parent
 *                              for an IOperation must be an IClass, instead of
 *                              an IClassifier.
 *  12  2002-06-19  Darshan     Added code to prevent interface methods being
 *                              explicitly flagged abstract.
 *
 * @see EventManager
 * @see ConstructorInfo
 */
public class MethodInfo extends ConstructorInfo 
{
    // Describe synchronization (concurrency) constants
    public static final int CCK_SEQUENTIAL  = 0;
    public static final int CCK_GUARDED     = 1;
    public static final int CCK_CONCURRENT  = 2;

    String mReturn = null;

    private int lineNo = 0;
    
    /**
     *  The operation which this MethodInfo wraps. Will be null for source-model
     * operations, non-null for model-source operations.
     */
    private IOperation operation;

    /**
     * If this operation is an accessor or mutator, the attribute in question.
     */
    private MemberInfo attribute;

    /**
     * This is used to identify the attribute for which this is a getter or setter.
     * If this method is not getter or setter, this varibale will be null.
     */
    private String attributeName = null;

    private MethodParameterInfo returnParameter = null;
    
    /**
     * This is used to identify if this method is a getter or setter.
     * True indicates a getter
     * False indicates a setter.
     */
    private boolean accessor = false;
    private boolean mutator  = false;

    /**
	 * @return Returns the lineNo.
	 */
	public  int getLineNo()
	{
		return lineNo;
	}
	
	/**
	 * @param lineNo The lineNo to set.
	 */
	public  void setLineNo(int lineNo)
	{
		this.lineNo = lineNo;
	}
	
	/**
     *  Initializes a new MethodInfo.
     *  @param container The class that contains the data member.
     *  @param type The transaction type.
     *  @see ElementInfo
     */
    public MethodInfo(ClassInfo container, int type) {
        super(container, type);
    }

    public MethodInfo(ClassInfo container, IOperation op) {
        super(container, op);
        setOtherMethodInfo(container, op);
    }
    
    /* (non-Javadoc)
     * @see com.embarcadero.integration.events.ElementInfo#getOwningProject()
     */
    public IProject getOwningProject() {
        return  operation != null? 
                    (IProject) operation.getProject() : 
                getContainingClass() != null?
                    getContainingClass().getOwningProject() :
                    null;
    }

    protected void setOtherMethodInfo(ClassInfo container, IOperation op) 
    {
        operation = op;

        if (container == null) 
        {
            IElement owner = op.getOwner();
            if (owner != null && owner instanceof IClassifier)
            {
                IClassifier cpowner = (IClassifier) owner;
                //container = new ClassInfo(cpowner);
                container = ClassInfo.getRefClassInfo(cpowner, true);

                setContainingClass(container);
            }
        }

        setName(op.getName());

        boolean parentIsInterface = getContainingClass() != null &&
            getContainingClass().isInterface();

        int mods = JavaClassUtils.getJavaModifier(op.getVisibility());
        if (op.getIsFinal())
            mods |= Modifier.FINAL;
        if (op.getIsStatic())
            mods |= Modifier.STATIC;
        if (op.getIsAbstract() && !parentIsInterface)
            mods |= Modifier.ABSTRACT;
        if (op.getIsNative())
            mods |= Modifier.NATIVE;
        if (op.getConcurrency() == CCK_GUARDED)
            mods |= Modifier.SYNCHRONIZED;
        if (op.getIsStrictFP())
            mods |= Modifier.STRICT;

        // we don't want to call setModifiers(new Integer(mods)) directly;
        mModifiers = new Integer(mods);
        accessor = mutator = false;
        
        String getStr = checkIfGetter(op);
        if (getStr != null) 
        {
            attributeName = getStr;
            accessor = true;
        }
        
        String setStr = checkIfSetter(op);
        if (setStr != null) {
            attributeName = setStr;
            mutator = true;
            accessor = false;
        }
        
        IParameter ret = op.getReturnType();

// conover - replacing String type with MethodParameterInfo type
//        if (op.getIsConstructor() || ret == null )
//        {
//            setReturnType(null);
//        }
//        
//        else
//        {
//            setReturnType(MethodParameterInfo.getType(ret, accessor));
//        }
// conover - using MethodParameterInfo instead of String allows for more robust
// code generation, like for Collection Override

        if (!op.getIsConstructor() && ret != null)
            setReturnParameter(new MethodParameterInfo(this, ret, false));
        
        setParameters(
            mutator 
                ? getMutatorParameter(attribute, op) 
                : getParameterInfo(op));

        // add exceptions info
        ETList<IClassifier> exceptions = op.getRaisedExceptions();
        
        if (exceptions != null) 
        {
            String[] raisedExceptions = new String[exceptions.getCount()];
        
            for (int i = 0; i < exceptions.getCount(); i++) 
            {
                raisedExceptions[i] = JavaClassUtils.replaceDollarSign(
                    JavaClassUtils.getFullyQualifiedName(exceptions.item(i)));
            }
            
            setExceptions(raisedExceptions);
        }
    }

    /**
     *  Set the access modifiers for this method. Interface methods will be
     * forced abstract, regardless of the actual modifiers set (unless null).
     *
     * @param mods An <code>Integer</code> of the modifiers as defined in
     *             <code>java.lang.ref.Modifier</code>. If <code>null</code>,
     *             the modifiers will not be updated to the model.
     */
    public void setModifiers(Integer mods) {
        ClassInfo owner = getContainingClass();
        if (owner != null && owner.isInterface() && mods != null &&
                (mods.intValue() & Modifier.ABSTRACT) == 0)
            mods = new Integer(mods.intValue() | Modifier.ABSTRACT);
        super.setModifiers(mods);
    }

    public MemberInfo getAttribute() {
        return attribute;
    }

    public boolean isCollectionType() {
        return attribute != null && attribute.isUseCollectionOverride();
    }

    public String getFilename() {
        return (getContainingClass() != null?
                       getContainingClass().getFilename() : null);
    }

    public IProject getProject() {
        return operation != null? (IProject) operation.getProject()
                                   : null;
    }

    private MethodParameterInfo[] getMutatorParameter(
        MemberInfo minfo, IOperation op)
    {
        if (attribute == null)
        {
            Log.impossible("getMutatorParameter(): null attribute?"); // NOI18N
            return null;
        }
        
        MethodParameterInfo[] params = getParameterInfo(op);
        if (params != null && params.length == 1)
        {
            // Note that we have to be careful here - if this MethodInfo
            // represents the *old* state of a method, we shouldn't set the
            // parameter type to the *current* type of the associated attribute.
            if (minfo.getCollectionOverrideDataType() != null &&
                MethodParameterInfo.isArray(params[0].getType()) &&
                (params[0].getType().equals(minfo.getType()) ||
                params[0].getType().equals(minfo.getQualifiedType())))
            {
                Log.out("Changing parameter type from " // NOI18N
                    + params[0].getType() + " to " // NOI18N
                    + minfo.getCollectionOverrideDataType());
                
                params[0].setType(minfo.getCollectionOverrideDataType());
            }
        }
        return params;
    }

    /**
     *  Returns an array of MethodParameterInfos describing the parameters of
     * the given operation.
     *  If the operation has no parameters, an empty array will be returned.
     * @param op A valid (non-null) operation
     * @return
     */
    private MethodParameterInfo[] getParameterInfo(IOperation op) 
    {
        ETList<IParameter> pars = op.getFormalParameters();
        
        if (pars == null || pars.getCount() < 1)
            return new MethodParameterInfo[0];

        MethodParameterInfo[] params = new MethodParameterInfo[pars.getCount()];
        
        for (int i = 0; i < pars.getCount(); ++i) 
        {
            IParameter param = pars.item(i);
            
            MethodParameterInfo mpi = 
                new MethodParameterInfo(this, param, false);
            
            params[i] = mpi;
        }
        
        return params;
    }

    private String checkIfGetter(IOperation op) {
        ETList<IDependency> clDeps = op.getClientDependencies();
        if (clDeps != null) {
            for (int i = 0; i < clDeps.getCount(); i++) {
                IDependency dep = clDeps.item(i);
                INamedElement ne = dep.getSupplier();
                if (ne instanceof IAttribute) {
                    attribute = new MemberInfo(
                                   (IAttribute)  ne);
                    return ne.getName();
                }
            }
        }
        return null;
    }

    private String checkIfSetter(IOperation op) {
        ETList<IDependency> suDeps = op.getSupplierDependencies();
        if (suDeps != null) {
            for (int j = 0; j < suDeps.getCount(); j++) {
                IDependency dep = suDeps.item(j);
                INamedElement ne = dep.getClient();
                if (ne instanceof IAttribute) {
                    attribute = new MemberInfo(
                                   (IAttribute)  ne);
                    return ne.getName();
                }
            }
        }
        return null;
    }

    /**
     * Set the return type of the method.
     * @param value The return type.
     */
    public void setReturnType( String value ) {
        mReturn = value;
    }

    /**
     * Gets the return type of the method.
     * @return The return type.
     */
    public String getCodeGenReturnType() 
    {
        // if no return param, probably a Constructor; return empty string
        if (getReturnParameter() == null)
            return ""; // NOI18N
        
        MethodParameterInfo param = getReturnParameter();
        IParameter element = param.getParameterElement();
        String[] shortNames = GenCodeUtil
	    .getCollectionOverrideDataTypes(element.getMultiplicity(), false);
        return GenCodeUtil.getCodeGenType(element.getType(), 
                                          shortNames,
                                          param.isUseGenerics(),
                                          element.getMultiplicity(),
					  false,
					  getContainingClass());
    }

    /**
     * Returns <code>true</code> if this is an accessor.
     */
    public boolean isAccessor() {
        return accessor;
    }

    /**
     * Returns <code>true</code> if this is a mutator.
     */
    public boolean isMutator() {
        return mutator;
    }

    /**
     * Gets the member name for which this is an accessor.
     */
    public String getMemberName() {
        return attributeName;
    }

    /**
     * Updates the method.  A Class Transaction is began and
     * {@link #update(GDSymbolTransaction trans) update}
     * is called.
     */
    public void update()
    {
        super.update();
    }

    /**
     * Updates the method using the specified Symbol transaction.
     * @param trans The transaction that is to be used to update the correct symbol.
     * @return The method transaction that was created to update the data member.
     */
    public MethodTransaction update(SymbolTransaction symTrans)
    {
        MethodTransaction retVal = super.update(symTrans);
        if((retVal != null) && (getChangeType() != ElementInfo.DELETE))
        {
            IOperation oper    = retVal.getOperation();
            boolean cons;
            if (oper != null
                    && (cons = oper.getName().equals(symTrans.getSymbol().getName())) != oper.getIsConstructor())
                oper.setIsConstructor(cons);

            /*
            if(getReturnType() != null)
            {
                EventManager manager = EventManager.getEventManager();
                try
                {
                    IOperation oper    = retVal.getOperation();
                    if((oper != null) && !oper.getName().equals(symTrans.getSymbol().getName())) {
                        oper.setReturnType2(getReturnType());
                    } else {
                        if(oper != null) {
                           oper.setIsConstructor(true);
                        }
                    }
                }
                catch(Exception E)
                {
                    String msg = "Error occured while updating a Describe "
                                + "method attribute.<br>";
                    msg += " to the value: " +  getReturnType();
                    ExceptionDialog.showExceptionError(msg, E);
                }
            }

            */
        }
        return retVal;
    }

    /**
     *  Returns whether the parameter list of this method needs to be
     * updated to the Describe model.
     *
     * @return <code>true</code> if the model needs to be updated.
     */
    protected boolean needParameterUpdate() {
        return true;
    }

    protected ETList<IParameter> getParameterCollection(IOperation op) {
        ETList<IParameter> pars = super.getParameterCollection(op);

        if (getCodeGenReturnType() != null) {
            IParameter ret = op.createReturnType();
            
            String type = getCodeGenReturnType();
            int mul = MemberInfo.getMultiplicity(type);
            type = MemberInfo.getTypeName(type);

            ret.setType2(JavaClassUtils.convertJavaToUML(type));
            MemberInfo.setMultiplicity(ret, mul, 0);
            pars.add(ret);
        }

        return pars;
    }

    protected void updateParameters(MethodTransaction trans)
    {
        if (getNewParameters() == null)
        {
            if (getCodeGenReturnType() != null)
            {
                EventManager.getEventManager().getEventFilter().blockEventType(
                        ChangeUtils.RDT_DEPENDENCY_ADDED);
                try
                {
                    IOperation op = trans.getOperation();
                    Log.out("updateParameters: Operation is " + op);
                    Log.out("updateParameters: Operation return type is "
                            + op.getReturnType2() + "(" + op.getReturnType() + ")");
//                if (op.getCodeGenReturnType() == null
//                    || !JavaClassUtils.convertJavaToUML(getCodeGenReturnType()).equals(
//                        op.getCodeGenReturnType().getTypeName())) {
                    String type = getCodeGenReturnType();
                    int mul = MemberInfo.getMultiplicity(type);
                    type = MemberInfo.getTypeName(type);
                    
                    IDerivationClassifier derivation = getDerivationClassifer(op, type);
                    if(derivation != null)
                    {
                        IParameter param = op.getReturnType();
                        if(param == null)
                        {
                            param = op.createParameter2(derivation, "");
                        }
                        op.setReturnType(param);
                    }
                    else
                    {
                        op.setReturnType2(JavaClassUtils.convertJavaToUML(type));
                    }
                    
                    IParameter retp = op.getReturnType();
                    MemberInfo.setMultiplicity(retp, mul, MemberInfo.getMultiplicity(retp));
                }
                finally
                {
                    EventManager.getEventManager().getEventFilter().unblockEventType(
                            ChangeUtils.RDT_DEPENDENCY_ADDED);
                }
//                }
            }
        }
        else
            super.updateParameters(trans);
    }

    public String getCode() {
        return "M";
    }

    protected String getDisplayString(String fnName, MethodParameterInfo[] mpi) 
    {
        String s = super.getDisplayString(fnName, mpi);
        String type = getCodeGenReturnType();
        
        if (type != null) 
            s += ": " + type;
        
        return s;
    }

    public MethodParameterInfo getReturnParameter()
    {
        return returnParameter;
    }

    public void setReturnParameter(MethodParameterInfo val)
    {
        returnParameter = val;
    }

    public IOperation getOperation()
    {
        return operation;
    }

    public void setOperation(IOperation operation)
    {
        this.operation = operation;
    }

    
    //
    // added for template codegen
    //


    public Integer getModifiers() {
	Integer mods = super.getModifiers();
	if (getOperation() != null && getOperation().getIsConstructor()
	    && getContainingClass().isEnumeration())
	{
	    int m = mods.intValue();
	    m &= ( ~ ( Modifier.PUBLIC | Modifier.PROTECTED ) );
	    mods = new Integer(m);
	}
        return mods;
    }


    public boolean isAbstract() {
	return ( Modifier.isAbstract(getModifiers()) 
		 || (getContainingClass() != null 
		     && getContainingClass().isInterface())); 
    }

    public boolean isNative() {
	return Modifier.isNative(getModifiers()); 
    }


    public ArrayList<MethodParameterInfo> getParameterInfos() 
    {
        ArrayList<MethodParameterInfo> res = new ArrayList<MethodParameterInfo>();
	MethodParameterInfo[] parms = getParameterInfo(getOperation());
	if (parms == null && parms.length == 0) {
	    return null;
	}
	for(int i = 0; i < parms.length; i++) {
	    if (parms[i] != null) {
		res.add(parms[i]);
	    }
	}
	return res;
    }


    // see getCodeGenType() for how the type string is formed 
    public ArrayList<String[]> getReferredCodeGenTypes()
    {
    
	ArrayList<String[]> res = new ArrayList<String[]>();
	HashSet<String> fqNames = new HashSet<String>();
 
	MethodParameterInfo retType = getReturnParameter();
	if (retType != null) {
	    ArrayList<String[]> refs = retType.getReferredCodeGenTypes();
	    GenCodeUtil.mergeReferredCodeGenTypes(res, fqNames, refs);
	}

	MethodParameterInfo[] params = getParameters();
	if (params != null) {
	    for(int i = 0; i < params.length; i++)  {
		if (params[i] != null) {
		    ArrayList<String[]> refs = params[i].getReferredCodeGenTypes();
		    GenCodeUtil.mergeReferredCodeGenTypes(res, fqNames, refs);
		}
	    }
	}

	/* isn't needed as the exceptions contains fully-qualified names
	String[] exceptions = getExceptions();
	if (exceptions != null) {
	    for(int i = 0; i < exceptions.length; i++)  {
		if (exceptions[i] != null) {
		    // TBD - exception string may be with generic
		    String[] pn = new String[]{JavaClassUtils.getPackageName(exceptions[i]), 
					       JavaClassUtils.getShortClassName(exceptions[i])};
		    ArrayList<String[]> refs = new ArrayList<String[]>();
		    refs.add(pn);
		    GenCodeUtil.mergeReferredCodeGenTypes(res, fqNames, refs);
		}
	    }
	}
	*/

	return res;	
    }


    private static final HashMap<String, String> defaultReturnValues = new HashMap<String, String>();

    static {
        defaultReturnValues.put("int", "0");
        defaultReturnValues.put("short", "0");
        defaultReturnValues.put("long", "0");
        defaultReturnValues.put("float", "0.0f");
        defaultReturnValues.put("double", "0.0");
        defaultReturnValues.put("byte", "0");
        defaultReturnValues.put("char", "'a'");
        defaultReturnValues.put("boolean", "true");
    }

    public String getDefaultReturnValue() 
    {
	String retType = getCodeGenReturnType();
	if (retType == null || retType.equals("void")) {
	    return null;
	}
	String res = defaultReturnValues.get(retType);
	if (res == null) {
	    return "null";
	}
	return res;
    }
    

}
