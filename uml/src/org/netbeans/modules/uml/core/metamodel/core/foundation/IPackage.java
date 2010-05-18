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
