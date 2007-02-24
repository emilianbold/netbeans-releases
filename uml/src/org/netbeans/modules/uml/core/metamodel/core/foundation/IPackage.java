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

public interface IPackage extends INamespace{

//   Adds an ElementImport to this package.
//HRESULT AddElementImport([in] IElementImport* element);
  public void addElementImport(IElementImport elem, INamespace owner);
    //public void addElementImport(IElementImport elem);
    
//   Removes an imported element from this package.
//HRESULT RemoveElementImport([in] IElementImport* element);
  public void removeElementImport(IElement elem);

//   Retrieves the collection of ElementImports on this package.
//HRESULT ElementImports([out, retval] IElementImports* *pVal);
  public ETList<IElementImport> getElementImports();

//   Adds a PackageImport to this Package.
//HRESULT AddPackageImport([in] IPackageImport* packImport);
  //public void addPackageImport(IPackageImport pack);
  public void addPackageImport(IPackageImport pack, INamespace owner);

//   Removes a PackageImport from this Package.
//HRESULT RemovePackageImport([in] IPackageImport* packImport);
  public void removePackageImport(IPackageImport elem);

//   Retrieves the collection of PackageImports on this package.
//HRESULT PackageImports([out, retval] IPackageImports* *pVal);
  public ETList<IPackageImport> getPackageImports();

// Retrieves an element from this package's imports by name.
//HRESULT FindTypeByNameInImports([in] BSTR name, [out, retval] INamedElements** pVal);
  public ETList<INamedElement> findTypeByNameInImports(String name);

// A convenience routine to handle the import of an IPackage.
//HRESULT ImportPackage([in] IPackage* pack, [in, defaultvalue(0)] BSTR href, [in, defaultvalue(0)] VARIANT_BOOL versionedElement, [out, retval] IPackageImport** pVal);
  public IPackageImport importPackage(IPackage pack, String href, boolean versioned);

// A convenience routine to handle the import of an IPackageableElement.
//HRESULT ImportElement([in] IAutonomousElement* pack, [in, defaultvalue(0)] BSTR href, [in, defaultvalue(0)] VARIANT_BOOL versionedElement, [out, retval] IElementImport** pVal);
  public IElementImport importElement(IAutonomousElement elem, String href, boolean versioned);

// .
//HRESULT ElementImportCount([out, retval] long* pVal);
  public long getElementImportCount();

// .
//HRESULT PackageImportCount([out, retval] long* pVal);
  public long getPackageImportCount();

// Retrieves the collection of Imported packages on this package. This is a shortcut through the PackageImports property.
//HRESULT ImportedPackages([out, retval] INamespaces* *pVal);
  public ETList<INamespace> getImportedPackages();

// Retrieves the collection of Imported element on this package. This is a shortcut through the ElementImports property.
//HRESULT ImportedElements([out, retval] IElements* *pVal);
  public ETList<IElement> getImportedElements();

  /**
   * The location where source file artifacts will be generated into.
  */
  public String getSourceDir();

  /**
   * The location where source file artifacts will be generated into.
  */
  public void setSourceDir( String value );

  /**
   * Determines whether or not this is a top level package or not. A top level package is any package whose owning namespace is the Project, or the Project itself.
  */
  public boolean getIsTopLevelPackage(boolean checkLength);

}
