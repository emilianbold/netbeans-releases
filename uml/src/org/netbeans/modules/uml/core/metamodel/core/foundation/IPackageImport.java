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

public interface IPackageImport extends IDirectedRelationship {

//   Sets / Gets the Package that imports another Package.
//HRESULT ImportingPackage([out, retval] IPackage* *pVal);
  public IPackage getImportingPackage();

//   Sets / Gets the Package that imports another Package.
//HRESULT ImportingPackage([in] IPackage* newVal);
  public void setImportingPackage(IPackage pack);

//   Sets / Gets the Package that is imported by an importing Package.
//HRESULT ImportedPackage([out, retval] IPackage* *pVal);
  public IPackage getImportedPackage();

//   Sets / Gets the Package that is imported by an importing Package.
//HRESULT ImportedPackage([in] IPackage* newVal);
  public void setImportedPackage(IPackage pack);

// Retrieves an element from the imported package by name.
//HRESULT FindByName([ in ] BSTR name, [out, retval ]INamedElements** foundElements );
  public ETList<INamedElement> findByName(String name);

}
