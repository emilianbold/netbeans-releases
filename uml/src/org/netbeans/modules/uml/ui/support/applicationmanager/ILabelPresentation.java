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


package org.netbeans.modules.uml.ui.support.applicationmanager;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETLabel;
import com.tomsawyer.drawing.TSLabel;
import com.tomsawyer.editor.ui.TSELabelUI;

public interface ILabelPresentation extends IGraphPresentation
{
	/**
	 * Get the product label attached to this presentation element
	*/
	public IETLabel getETLabel();

	/**
	 * Get the TS label view this presentation element represents.
	*/
	public TSELabelUI getLabelView();

	/**
	 * Get the TS label this presentation element represents.
	*/
	public TSLabel getTSLabel();

	/**
	 * Get the TS label this presentation element represents.
	*/
	public void setTSLabel( TSLabel value );

	/**
	 * Begins editing of this presentation element.
	*/
	public long beginEdit();

	/**
	 * Indicates that if the subsequent editing operation was cancelled to delete this label.
	*/
	public boolean getDeleteIfNotEdited();

	/**
	 * Indicates that if the subsequent editing operation was cancelled to delete this label.
	*/
	public long setDeleteIfNotEdited( boolean bDelete );

	/**
	 * Returns the owning presentation element (a node or an edge).
	*/
	public IPresentationElement getPresentationOwner();

	/**
	 * Returns location information for this node
	*/
	public long getLocation( int pWidth, int pHeight, int pXCenter, int pYCenter );

	/**
	 * Moves this node to the logical x and y points.  Flags is an OR of MoveToFlags (ie MTF_MOVEX | MTF_MOVEY | MTF_LOGICALCOORD)
	*/
	public long moveTo( int x, int y, int flags );
        
        /**
         * Returns node height
         */
	public long getHeight();
        
        /**
         * Returns node width
         */
	public long getWidth();
        
        /**
         * Returns node center
         */
	public IETPoint getCenter();
}
