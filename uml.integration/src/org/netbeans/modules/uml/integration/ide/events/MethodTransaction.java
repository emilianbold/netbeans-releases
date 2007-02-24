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

import org.netbeans.modules.uml.integration.ide.ChangeUtils;
import org.netbeans.modules.uml.integration.ide.JavaClassUtils;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter;
import org.netbeans.modules.uml.core.support.umlsupport.Log;

/**
 * The MethodTransaction is use to maintain a context when updating methods.
 * The MethodTransaction will locate the method in Describes database.
 * When locating the method the class symbol specified by the SymbolTransaction
 * will be searched for any attributes that match the requested attribute.
 */
public class MethodTransaction extends Object
{
  /** The attribute that this context is managing. */
  IOperation mOpe = null;

  /** Creates new MethodTransaction */
  public MethodTransaction()
  {
    setOperation(null);
  }

  /**
   * Create a new MemberTransaction and specify the symbol to search and
   * method to find.  If the method will be created if one is needed.
   * @param trans The symbol transaction used when searching for the memeber.
   * @param member The information required to locate the method.
   */
  public MethodTransaction(SymbolTransaction trans, final ConstructorInfo method)
  {
    if(method != null)
    {
      setAttribute(trans, method);
    }
  }
  /**
   * Retrieve the Describe representation of the method.
   * <i>In the future this may be abstracted. </i>
   */
  public IOperation getOperation()
  {
    return mOpe;
  }

  /**
   * Set the Describe representation of the method
   * <i>In the future this may be abstracted. </i>
   */
  public void setOperation(IOperation attr)
  {
    mOpe = attr;
  }
  public void setAttribute(SymbolTransaction trans, final ConstructorInfo method) {
      try {
          doSetAttribute(trans, method);
      }
      catch (Exception ex) {
          Log.stackTrace(ex);
      }

  }
  public void doSetAttribute(SymbolTransaction trans, final ConstructorInfo method)
  {
    Log.out("Inside setAttribute() of MethodTransaction ........");
    // First set the symbol to null to allow the current symbol to be GC
    mOpe = null;
    IClassifier sym = trans.getSymbol();
    if(sym != null)
    {
      mOpe = JavaClassUtils.findOperation(sym, method.getName(), method.getParameters());
      if(mOpe == null) {
        Log.out("Unable to find the method ........" + method.getName());
      }

      if((mOpe == null) && (method.getChangeType() == ElementInfo.CREATE))
      {
        // **## Log.out("Creating a new Attribute...");
        mOpe = createOperation(sym, method.getName(), method.getParameters());

        // null the parameters to prevent them getting recreated.
        //method.setParameters(null);
        Log.out("Successfully created the new operation ........");
      }
    }
    else
        Log.out("setAttribute(): Classifier is null for - " + method);
  }

  /**
   * Sets the Describe attribute that defines method.  A Describe symbol
   * is search for a method that matches the requested data member name and
   * parameters.  If a Describe attribute is not found then one is created for
   * the method.
   * @param trans The symbol transaction used to locate the method.
   * @param member The information needed to locate the method.
   * @param params The parameter information.
   */
  protected IOperation createOperation(IClassifier sym, String name, MethodParameterInfo[] params)
  {
    IOperation op   = null;
    EventManager.getEventManager().getEventFilter().blockEventType(
            ChangeUtils.RDT_DEPENDENCY_ADDED);
    try
    {
        if(name.equals(sym.getName())
            && (params == null || params.length == 0)){ // method is a empty constructor
            op = sym.createConstructor();
        }
        else {
            if (name.equals(sym.getName()))
                op = sym.createConstructor();
            else
                op = sym.createOperation("void", name);
            for (int i = 0; i < params.length; ++i) {
				String partype = params[i].getType();
				int mul = MemberInfo.getMultiplicity(partype);
				partype = MemberInfo.getTypeName(partype);
				
                Log.out("MethodTransaction.createOperation: " +                    "Creating parameter of type: " 
                    + JavaClassUtils.convertJavaToUML(partype));
                IParameter param =
                    op.createParameter(
                        JavaClassUtils.convertJavaToUML(partype),
                        params[i].getName());
				if (mul > 0 && param != null)
					MemberInfo.setMultiplicity(param, mul, 0);
                op.addParameter(param);
            }
        }
    }
    catch(Exception E)
    {
        Log.stackTrace(E);
    }finally {
        EventManager.getEventManager().getEventFilter().unblockEventType(
                ChangeUtils.RDT_DEPENDENCY_ADDED);   
    }
    return op;
  }

}
