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


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public interface IReference extends IDirectedRelationship{

//           The element that is referring to another.
//   HRESULT ReferencingElement([out, retval] IElement* *pVal);
  public IElement getReferencingElement();

//           The element that is referring to another.
//   HRESULT ReferencingElement([in] IElement* newVal);
  public void setReferencingElement(IElement elem);

//           The element that is being referred to.
//   HRESULT ReferredElement([out, retval] IElement* *pVal);
  public IElement getReferredElement();

//           The element that is being referred to.
//   HRESULT ReferredElement([in] IElement* newVal);
  public void setReferredElement(IElement elem);
}
