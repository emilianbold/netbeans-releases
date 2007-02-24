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



package org.netbeans.modules.uml.ui.products.ad.diagramengines.sequencediagram;

import org.netbeans.modules.uml.ui.products.ad.ADDrawEngines.IADEdgeDrawEngine;
import org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation;

/**
 * The IMessageEdgeDrawEngine is used specify the draw engine that is used
 * to render a message.
 */
public interface IMessageEdgeDrawEngine extends IADEdgeDrawEngine
{
	/** Indicates that the message starts and ends on the same node */
	public boolean isMessageToSelf( );
	
	/** Indicates that the message starts and ends on the same node */ 
	public void setIsMessageToSelf(boolean bIsMessageToSelf);

	/** Move this message vertically */ 
	public void move( double nY, boolean bDoItNow );

	/** Set/Get wether the message is shown (drawn) */ 
	public void setShow( boolean bShow );
	
	/** Set/Get wether the message is shown (drawn) */ 
	public boolean getShow();

	/** Set/Get the data to be displayed in the label above the message */ 
	public void setShowMessageType( int /*ShowMessageType*/ type );
	
	/** Set/Get the data to be displayed in the label above the message */ 
	public int getShowMessageType();

	/** Retrieves the edge presentation for the associated result message */ 
	public IEdgePresentation getAssociatedResultMessage();
}
