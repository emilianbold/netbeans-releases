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


package org.netbeans.modules.uml.core.metamodel.core.foundation;

import org.netbeans.modules.uml.core.support.umlutils.ETList;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public interface IConstraint extends IAutonomousElement{

//           Sets / Gets the condition that must be true when evaluated in order for an instance in a system to be well-formed.
//   HRESULT Specification([out, retval] IValueSpecification* *pVal);
  public IValueSpecification getSpecification();

//           Sets / Gets the condition that must be true when evaluated in order for an instance in a system to be well-formed.
//   HRESULT Specification([in] IValueSpecification* newVal);
  public void setSpecification(IValueSpecification spec);

//           Adds an element to this Constraint's list of constrained elements.
//   HRESULT AddConstrainedElement([in] IElement* element);
  public void addConstrainedElement(IElement elem);

//           Removes an element from this constraint.
//   HRESULT RemoveConstrainedElement([in] IElement* element);
  public void removeConstrainedElement(IElement elem);

//           Retrieves the collection of elements this constraint is applied to.
//   HRESULT ConstrainedElements([out, retval] IElements* *pVal);
  public ETList<IElement> constrainedElements();

// Determines whether or not the NamedElement is currently being constrained.
//   HRESULT IsConstrained( [in] IElement* element, [out,retval]VARIANT_BOOL* flag );
  public boolean isConstrained(IElement elem);

// Sets / Gets the condition that must be true when evaluated in order for an instance in a system to be well-formed.
//   HRESULT Expression2([out, retval] BSTR* pVal);
  public String getExpression();

//           Sets / Gets the condition that must be true when evaluated in order for an instance in a system to be well-formed.
//   HRESULT Expression2([in] BSTR newVal);
  public void setExpression(String str);
}
