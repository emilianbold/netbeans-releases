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
