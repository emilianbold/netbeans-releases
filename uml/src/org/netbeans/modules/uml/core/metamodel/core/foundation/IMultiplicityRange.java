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

import org.netbeans.modules.uml.common.generics.ETPairT;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public interface IMultiplicityRange extends IElement {

//   Sets / Gets the lower bound for this range.
//HRESULT Lower([out, retval] BSTR *pVal);
  public String getLower();

//   Sets / Gets the lower bound for this range.
//HRESULT Lower([in] BSTR newVal);
  public void setLower(String val);

//   Sets / Gets the upper bound for this range. If upper is not specified( i.e., -1 ), then the range includes the lower bound and all integers greater than the lower bound.
//HRESULT Upper([out, retval] BSTR *pVal);
  public String getUpper();

//   Sets / Gets the upper bound for this range. If upper is not specified( i.e., -1 ), then the range includes the lower bound and all integers greater than the lower bound.
//HRESULT Upper([in] BSTR newVal);
  public void setUpper(String val);

// A convenience function used to get the upper and lower bounds in one call.
//HRESULT GetRange([out] BSTR *lower, [out] BSTR* upper );
  public ETPairT<String, String> getRange();

//   A convenience function used to set the upper and lower bounds in one call.
//HRESULT SetRange([in] BSTR lower, [in] BSTR upper );
  public void setRange(String lower, String upper);

//   Returns the parent IMultiplicity object
//HRESULT ParentMultiplicity([out, retval] IMultiplicity* *pParentMult );
  public IMultiplicity getParentMultiplicity();

  public String getRangeAsString();
}
