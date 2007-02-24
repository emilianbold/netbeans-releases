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

package org.netbeans.modules.uml.core.metamodel.profiles;

import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.support.umlutils.ETList;


public interface IStereotype extends IClass
{

	// Retrieves a collection of meta type names of elements this Stereotype can apply to.
	//   HRESULT AppliesTo([out, retval] IStrings* *pVal);
	public ETList<String> appliesTo();

	// Adds a meta type to this stereotype, allowing this stereotype to be applied to all instances of that meta type.
	//   HRESULT AddApplicableMetaType([in] BSTR sType);
	public void addApplicableMetaType(String sType);

	// Removes a meta type from this stereotype, preventing this stereotype to be applied to all instances of that meta type. 
	//   HRESULT RemoveApplicableMetaType([in] BSTR sType);
	public void removeApplicableMetaType(String sType);

}


