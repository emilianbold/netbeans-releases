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


package org.netbeans.modules.uml.ui.controls.projecttree;

public interface IProjectTreeDragVerify
{
	/**
	 * The target node for the drop operation
	*/
	public IProjectTreeItem getTargetNode();

	/**
	 * The target node for the drop operation
	*/
	public void setTargetNode( IProjectTreeItem value );

	/**
	 * Get/Set the cancel flag for this event
	*/
	public boolean isCancel();

	/**
	 * Get/Set the cancel flag for this event
	*/
	public void setCancel( boolean value );

	/**
	 * Get/Set whether something has handled this event
	*/
	public boolean getHandled();

	/**
	 * Get/Set whether something has handled this event
	*/
	public void setHandled( boolean value );

	/**
	 * Get the drop effect that should be displayed
	*/
	public long getDropEffect();

	/**
	 * Get the drop effect that should be displayed.  The value of the drop
    * effect is one of the DnDConstants attributes.
    * 
    * @see java.awt.dnd.DnDConstants
	 */
	public void setDropEffect( long value );

}
