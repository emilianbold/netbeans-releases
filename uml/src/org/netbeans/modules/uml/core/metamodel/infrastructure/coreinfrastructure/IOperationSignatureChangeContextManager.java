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


package org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure;

public interface IOperationSignatureChangeContextManager
{
	/**
	 * The operation whose signature change is encapsulated by a context managed by this manager.
	*/
	public IOperation getOperation();

	/**
	 * Constructs a signature change context, setting the operation of the context, and pushes that context onto the event dispatch controller. If this manager is already responsible for a context, the current one is popped and a new one is pushed. To change the sign?Ó?
	*/
	public void startSignatureChange( IOperation newVal );

	/**
	 * Pops the context from the event dispatch controller. This function can be called to force the context to be popped before the manager destructs.
	*/
	public void endSignatureChange();

}
