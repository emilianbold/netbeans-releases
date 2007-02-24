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

public interface IDependency extends IDirectedRelationship {

//   The supplier of the Dependency.
//     HRESULT Supplier([in] INamedElement* newVal );
  public void setSupplier(INamedElement elem);

//   The supplier of the Dependency.
//     HRESULT Supplier([out, retval] INamedElement* *pVal);
  public INamedElement getSupplier();

//   The client of the Dependency.
//     HRESULT Client([in] INamedElement* newVal );
  public void setClient(INamedElement elem);

//   The client of the Dependency.
//   HRESULT Client([out, retval] INamedElement* *pVal);
  public INamedElement getClient();

}
