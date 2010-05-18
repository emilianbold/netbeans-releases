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
 
 /**
 * The default behavior to this method is to return true if the names of the
 * two elements being compared are same. Subclasses should override to 
 * implement class specific <em>isSimilar</em> behavior.
 *
 * @param other The other named element to compare this named element to.
 * @return true, if the names are the same, otherwise, false.
 */
 public boolean isSimilar(INamedElement other);
}
