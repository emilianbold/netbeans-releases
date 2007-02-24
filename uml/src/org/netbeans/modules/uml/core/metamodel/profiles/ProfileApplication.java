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

/*
 * Created on Sep 16, 2003
 *
 */
package org.netbeans.modules.uml.core.metamodel.profiles;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.core.foundation.PackageImport;

/**
 * @author aztec
 *
 */
public class ProfileApplication
	extends PackageImport
	implements IProfileApplication
{

	/**
	 * Gets the Profile this application is importing.
	 */
	public IProfile getImportedProfile()
	{
		IProfile retProfile = null;
		IPackage pack = getImportedPackage();
		if (pack instanceof IProfile)
		{
			retProfile = (IProfile)pack;
		}
		return retProfile;
	}

	/**
	 * Sets the Profile this application is importing.
	 */
	public void setImportedProfile(IProfile profile)
	{
		setImportedPackage(profile);
	}
	
	/**
	 * Establishes the appropriate XML elements for this UML type.
	 *
	 * [in] The document where this element will reside
	 * [in] The element's parent node.
	 */	
	public void establishNodePresence(Document doc, Node parent)
	{
		buildNodePresence("UML:ProfileApplication",doc,parent);
	}
}



