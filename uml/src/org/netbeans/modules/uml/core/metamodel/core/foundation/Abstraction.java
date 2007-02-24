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

import org.dom4j.Document;
import org.dom4j.Node;

/**
 * @author sumitabhk
 *
 */
public class Abstraction extends Dependency implements IAbstraction{

	/**
	 *
	 */
	public Abstraction() {
		super();
	}

	/**
	 *
	 * Retrieves the expression that states the abstraction relationship
	 * between the supplier and the client. In some cases, such as Derivation, it is usually
	 * formal and unidirectional; in other cases, such as Trace, it is usually informal
	 * and bidirectional. The mapping expression is optional and may be omitted if the
	 * precise relationship between the elements is not specified.
	 *
	 * @param exp[out] The expression
	 *
	 * @return HRESULT
	 *
	 */
	public IExpression getMapping()
	{
      IExpression dummy = null;
		return getSpecificElement( "UML:Abstraction.mapping", dummy, IExpression.class);
	}

	/**
	 *
	 * Sets the mapping expression.
	 *
	 * @param exp[in] The expression
	 *
	 * @return HRESULT
	 * @see get_Mapping
	 *
	 */
	public void setMapping( IExpression value )
	{
		addChild( "UML:Abstraction.mapping", "UML:Abstraction.mapping", value );
		
	}

	/**
	 * Establishes the appropriate XML elements for this UML type.
	 *
	 * [in] The document where this element will reside
	 * [in] The element's parent node.
	 *
	 * @return HRESULT
	 */
	public void establishNodePresence( Document doc, Node parent )
	{
	   buildNodePresence( "UML:Abstraction", doc, parent );
	}
}



