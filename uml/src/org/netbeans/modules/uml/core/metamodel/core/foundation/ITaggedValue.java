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

public interface ITaggedValue extends INamedElement{
//   Retrieves / Sets the string value that are part of the tagged value.
//     HRESULT DataValue([out, retval] BSTR* pVal );
  public String getDataValue();

//   Retrieves / Sets the string value that are part of the tagged value.
//     HRESULT DataValue([in] BSTR pVal );
  public void setDataValue(String val);

//   The NamedElement this TaggedValue is associated with.
//     HRESULT NamedElement([out, retval] INamedElement** pVal);
  public INamedElement getNamedElement();

//             The NamedElement this TaggedValue is associated with.
//     HRESULT NamedElement([in] INamedElement* newVal);
  public void setNamedElement(INamedElement elem);

//   The ID of the NamedElement this TaggedValue is to be associated with.
//     HRESULT NamedElementID([in] BSTR ownerID );
  public void setNamedElementID(String str);

//   Specifies the NamedElement(s) that make up the value of this tag.
//     HRESULT ReferenceValue([out, retval] INamedElements** pVal );
  public ETList<INamedElement> getReferenceValue();

//   Makes this tag value behave like the UML 1.3 version did, with a simple name / value pair.
//     HRESULT Populate([in] BSTR tagName, BSTR dataValue );
  public void populate(String tagName, String val);

//   Determines whether or not the tagged value should be displayed in the GUI.
//     HRESULT Hidden([in] VARIANT_BOOL newVal );
  public void setHidden(boolean b);

//   Determines whether or not the tagged value should be displayed in the GUI.
//     HRESULT Hidden([out, retval] VARIANT_BOOL* pVal );
  public boolean isHidden();
}
