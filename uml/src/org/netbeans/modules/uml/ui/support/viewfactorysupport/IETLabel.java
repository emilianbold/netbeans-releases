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


package org.netbeans.modules.uml.ui.support.viewfactorysupport;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem;
import com.tomsawyer.drawing.TSLabel;
import com.tomsawyer.editor.ui.TSELabelUI;

public interface IETLabel extends IETGraphObject
{
	/**
	 * The label attached to this object.
	*/
	public TSLabel getLabel();

	/**
	 * The label view attached to this object.
	*/
	public void setLabelView( TSELabelUI value );

	/**
	 * The label view attached to this object.
	*/
	public TSELabelUI getLabelView();

	/**
	 * Notifies the node that a context menu is about to be displayed
	*/
	public void onContextMenu( IProductContextMenu pContextMenu, int logicalX, int logicalY );

	/**
	 * Notifies the node that a context menu has been selected
	*/
	public void onContextMenuHandleSelection( IProductContextMenu pContextMenu, IProductContextMenuItem pMenuItem );

	/**
	 * Get/Set the kind of this label
	*/
	public int getLabelKind();

	/**
	 * Get/Set the kind of this label
	*/
	public void setLabelKind( /* TSLabelKind */ int value );

	/**
	 * Get/Set the kind of this label
	*/
	public int getLabelPlacement();

	/**
	 * Set the placement of this label
	*/
	public void setLabelPlacement( /* TSLabelPlacementKind */ int newPlacement );

	/**
	 * Get/Set the draw engine offset for the placement of the label
	*/
	public IETPoint getSpecifiedXY();

	/**
	 * Get/Set the draw engine offset for the placement of the label
	*/
	public void setSpecifiedXY( IETPoint value );

	/**
	 * Move the label to the correct position.
	*/
	public void reposition();

	/**
	 * Gets/Sets the text of the label
	*/
	public String getText();

	/**
	 * Gets/Sets the text of the label
	*/
	public void setText( String value );

	/**
	 * Returns the node or edge's ET element
	*/
	public IETGraphObject getParentETElement();

	/**
	 * Returns the node or edge's presentation element
	*/
	public IPresentationElement getParentPresentationElement();

	/**
	 * Used during paste, this creates a copy of this label.
	*/
	public IETLabel createLabelCopy( IDiagram pTargetDiagram, IETPoint pCenter, IPresentationElement pOwner);

}
