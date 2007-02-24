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
