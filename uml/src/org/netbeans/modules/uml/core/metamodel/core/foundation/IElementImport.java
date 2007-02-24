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

public interface IElementImport extends IDirectedRelationship {

//   Sets / Gets the Package that relies on a PackageableElement from another Package.
//HRESULT ImportingPackage([out, retval] IPackage* *pVal);
  public IPackage getImportingPackage();

//   Sets / Gets the Package that relies on a PackageableElement from another Package.
//HRESULT ImportingPackage([in] IPackage* newVal);
  public void setImportingPackage(IPackage pack);

//   Sets / Gets the AutonomousElement that an importingPackage imports.
//HRESULT ImportedElement([out, retval] IAutonomousElement* *pVal);
  public IAutonomousElement getImportedElement();

//   Sets / Gets the AutonomousElement that an importingPackage imports.
//HRESULT ImportedElement([in] IAutonomousElement* newVal);
  public void setImportedElement(IAutonomousElement val);

// Sets / Gets the visibility of the imported PackageableElement within the importing Package.
//HRESULT Visibility([out, retval] VisibilityKind *pVal);
  public int getVisibility();

//   Sets / Gets the visibility of the imported PackageableElement within the importing Package.
//HRESULT Visibility([in] VisibilityKind newVal);
  public void setVisibility(int val);

// Sets / Gets the name of an imported PackageableElement that is to be used instead of its name within the importing Package. By default, no alias is used.
//HRESULT Alias([out, retval] BSTR* pVal);
  public String getAlias();

//   Sets / Gets the name of an imported PackageableElement that is to be used instead of its name within the importing Package. By default, no alias is used.
//HRESULT Alias([in] BSTR newVal);
  public void setAlias(String val);

}
