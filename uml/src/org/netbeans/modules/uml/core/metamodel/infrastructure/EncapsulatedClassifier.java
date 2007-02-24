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


package org.netbeans.modules.uml.core.metamodel.infrastructure;

import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author sumitabhk
 *
 */
public class EncapsulatedClassifier extends StructuredClassifier
									implements IEncapsulatedClassifier
{
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.IEncapsulatedClassifier#addPort(org.netbeans.modules.uml.core.metamodel.infrastructure.IPort)
	 */
	public void addPort(IPort pPort)
	{
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.IEncapsulatedClassifier#removePort(org.netbeans.modules.uml.core.metamodel.infrastructure.IPort)
	 */
	public void removePort(IPort pPort) 
	{
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.infrastructure.IEncapsulatedClassifier#getPorts()
	 */
	public ETList<IPort> getPorts() 
	{		
		return null;
	}
}


