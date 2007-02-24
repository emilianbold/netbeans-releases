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


package org.netbeans.modules.uml.ui.swing.drawingarea;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;

public interface IPresentationElementSyncState
{
	/**
	 * The diagram the presentation element is on.
	*/
	public IDiagram getDiagram();

	/**
	 * The diagram the presentation element is on.
	*/
	public void setDiagram( IDiagram value );

	/**
	 * The PresentationElement that we want the synch state on
	*/
	public IPresentationElement getPresentationElement();

	/**
	 * The PresentationElement that we want the synch state on
	*/
	public void setPresentationElement( IPresentationElement value );

	/**
	 * The original synch state
	*/
	public int getOriginalSynchState();

	/**
	 * The original synch state
	*/
	public void setOriginalSynchState( /* SynchStateKind */ int value );

	/**
	 * The new synch state on the presentation element.
	*/
	public int getNewSynchState();

	/**
	 * The new synch state on the presentation element.
	*/
	public void setNewSynchState( /* SynchStateKind */ int value );

}
