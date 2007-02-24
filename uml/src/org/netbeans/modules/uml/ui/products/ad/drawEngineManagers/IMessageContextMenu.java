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



package org.netbeans.modules.uml.ui.products.ad.drawEngineManagers;

import org.netbeans.modules.uml.core.metamodel.dynamics.IMessage;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;

public interface IMessageContextMenu {

	// Update the context menu with the operations associated with the message's representing classifier
	public void addOperationsPullRight(IMessage pMessage, IMenuManager pContextMenu) throws Exception;

	// Update the context menu with the operations associated with the message's representing classifier
	public void addOperationsPullRight(IClassifier pClassifier, IMenuManager pContextMenu) throws Exception;

	// Select the operation from the recieving operations to be associated with the message.
	public IOperation selectOperation(long lOperationIndx) throws Exception;

	// Returns the operation text
	public String getMessagesOperationText(IMessage pMessage);

	// Called after the context menu is handled
	public void cleanUp();

}
