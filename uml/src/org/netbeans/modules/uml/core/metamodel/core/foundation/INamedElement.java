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

import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature;
import org.netbeans.modules.uml.core.support.umlutils.ETList;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public interface INamedElement extends IElement
{

//   Sets / Gets the name of this element.
//     HRESULT Name([out, retval] BSTR* curVal );
  public String getName();

//   Sets / Gets the name of this element.
//     HRESULT Name([in] BSTR newName );
  public void setName(String str);

//   Sets / Gets the visibility of this element.
//     HRESULT Visibility([out, retval] VisibilityKind* vis );
  public int getVisibility();

//   Sets / Gets the visibility of this element.
//     HRESULT Visibility([in] VisibilityKind vis );
  public void setVisibility(int vis);

//   Sets / Gets the Namespace of this element.
//     HRESULT Namespace([out, retval] INamespace** curSpace );
  public INamespace getNamespace();

//   Sets / Gets the Namespace of this element.
//     HRESULT Namespace([in] INamespace* space );
  public void setNamespace(INamespace space);

//   Adds a supplier dependency relationship to this element.
//     HRESULT AddSupplierDependency([in] IDependency* dep );
  public void addSupplierDependency(IDependency dep);

//             Removes a supplier dependency relationship from this element.
//     HRESULT RemoveSupplierDependency([in] IDependency* dep );
  public void removeSupplierDependency(IDependency dep);

//             Retrieves the collection of Dependencies where this element plays the supplier role.
//     HRESULT SupplierDependencies([out, retval] IDependencies** deps);
  public ETList<IDependency> getSupplierDependencies();

//             Retrieves the collection of Dependencies where this element plays the supplier role.  The Dependencies are of type sElementType.
//     HRESULT SupplierDependenciesByType([in]BSTR sElementType, [out, retval] IDependencies** deps);
  public ETList<IDependency> getSupplierDependenciesByType(String type);

//             Adds a client dependency relationship to this element.
//     HRESULT AddClientDependency([in] IDependency* dep );
  public void addClientDependency(IDependency dep);

//             Removes a client dependency relationship from this element.
//     HRESULT RemoveClientDependency([in] IDependency* dep );
  public void removeClientDependency(IDependency dep);

//             Retrieves the collection of Dependencies where this element plays the client role.
//     HRESULT ClientDependencies([out, retval] IDependencies** deps );
 public ETList<IDependency> getClientDependencies();

//             Retrieves the collection of Dependencies where this element plays the client role.  The Dependencies are of type sElementType.
//     HRESULT ClientDependenciesByType([in]BSTR sElementType, [out, retval] IDependencies** deps);
 public ETList<IDependency> getClientDependenciesByType(String type);

//   Retrieves the fully qualified name of the element. Project name is included based on the user preference.  This will be in the form '[ProjectName::]A::B::C'.
//     HRESULT QualifiedName([out, retval] BSTR* name );
 public String getQualifiedName();

//   Retrieves the fully qualified name of the element. This will be in the form '[ProjectName::]A::B::C'.
//     HRESULT FullyQualifiedName([in] VARIANT_BOOL useProjectName, [out, retval] BSTR* name );
 public String getFullyQualifiedName(boolean useProjName);

//   Used to establish a different name for this element.
//     HRESULT Alias([out, retval] BSTR* curVal );
 public String getAlias();

//   Used to establish a different name for this element.
//     HRESULT Alias([in] BSTR newName );
 public void setAlias(String str);

//   Does this element have an aliased name?
//     HRESULT IsAliased([out, retval] VARIANT_BOOL* bIsAliased );
 public boolean isAliased();

//   .
//     HRESULT SupplierDependencyCount([out, retval] long* pVal);
 public long getSupplierDependencyCount();

//   .
//     HRESULT ClientDependencyCount([out, retval] long* pVal);
 public long getClientDependencyCount();

//   Retrieves the fully qualified name of the element. Project name is never included.  This will be in the form 'A::B::C'.
//     HRESULT QualifiedName2([out, retval] BSTR* name );
 public String getQualifiedName2();

 public boolean isNameSame(IBehavioralFeature feature);
 
 public String getNameWithAlias();
 public void setNameWithAlias(String newVal);
 
}
