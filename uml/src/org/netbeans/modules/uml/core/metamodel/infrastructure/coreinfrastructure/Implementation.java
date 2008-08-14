/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;


public class Implementation extends Realization implements IImplementation
{
    public Implementation()
    {
        super();
    }
	/**
	 * property ImplementingClassifier
	*/
	public IClassifier getImplementingClassifier()
	{
		IClassifier retClassifier = null;
		INamedElement namedElem = super.getClient();
		if (namedElem != null)
			retClassifier = (IClassifier)namedElem;
		
		return retClassifier;		
	}

	/**
	 * property ImplementingClassifier
	*/
	public void setImplementingClassifier( IClassifier classifier )
	{
		super.setClient(classifier);	
	}

	/**
	 * property Contract
	*/
	public IClassifier getContract()
	{
		IClassifier retInterface = null;
		INamedElement namedElem = super.getSupplier();
		if (namedElem instanceof IClassifier)
			retInterface = (IClassifier)namedElem;
		
		return retInterface;	
	}

	/**
	 * property Contract
	*/
	public void setContract( IClassifier value )
	{
		super.setSupplier(value);
	}

	/**
	 * Establishes the appropriate XML elements for this UML type.
	 *
	 * [in] The document where this element will reside
	 * [in] The element's parent node.
	 */	
        @Override
	public void establishNodePresence(Document doc, Node parent)
	{
		super.buildNodePresence("UML:Implementation",doc,parent);
	}	
}


