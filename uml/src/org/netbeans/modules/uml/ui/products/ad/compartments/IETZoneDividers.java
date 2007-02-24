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



package org.netbeans.modules.uml.ui.products.ad.compartments;

import org.netbeans.modules.uml.common.ETException;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETSize;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ISetCursorEvent;


import java.awt.Color;
import java.awt.event.MouseEvent;

/**
 * @author KevinM
 *
 */
public interface IETZoneDividers {

	// Orientation.
	public static final int DMO_UNKNOWN = -1;
	public static final int DMO_HORIZONTAL = 0;
	public static final int DMO_VERTICAL = 1;

	/// Sets the pen style for the dividers
	public void setLineStyle(int DrawEngineLineKindEnum);

	/// Sets the orientation for this divider
	public void setOrientation(int orientation);

	/// Gets the orientation for this divider
	public int getOrientation();

	/// Insert a divider into the list of dividers
	public void insertDivider(int nIndex);

	/// Add a number of evenly spaced dividers
	public void addDividers(int lCnt);

	/// Shift all the divider from the start on "up"
	public void shiftDividers(int lOffset, int ulStartIndex);

	/// Delete a divider from the list of dividers
	public void deleteDivider(int nIndex);

   /// Removes all the dividers, and recreates new dividers
   public void resetDividers();

	/// Draw all the dividers
	public void draw(IDrawInfo pInfo, final IETRect rectBounding, Color crDivider, int nDividerWidth);

	/// Returns true when the mouse event occurs on any of the dividers
	public boolean isMouseOnDivider(MouseEvent pMouseEvent);

	/// Handle the set cursor event, if the event occured on a divider
	public boolean handleSetCursor( IETPoint pPoint, ISetCursorEvent event );

	/// Handle the left mouse begin drag event, if the event occured on a divider
	public boolean handleLeftMouseBeginDrag(IETPoint pptETStartPos);

	/// The number of dividers
	public int getDividerCnt();

	/// Returns the zone index associated with the input logical offset
	public int getZoneIndex(int lOffset);

	/// Returns the logical offset for the associated divider
	public int getDividerOffset(int nIndex);

	/// Sets the specific divider offset
	public void setDividerOffset(int nIndex, int lOffset);

	/// Returns the minimum logical size 
	public IETSize calculateOptimumSize(IDrawInfo pInfo);

	/// Returns the minimum logical size 
	public int getMinimumSize();

	/// Move the current divider to the specified logical location
	public void updateCurrentDivider(final IETPoint ptMoveTo);

	/// Process the archive information
	public void writeToArchive(IProductArchiveElement pCompartmentElement) throws ETException;
	public void readFromArchive(IProductArchiveElement pCompartmentElement) throws ETException;

	/// Informs this class that a resize of the node is about to take place
	public void startNodeResize();

	/// Informs this class that a resize of the node has ended
	public void finishNodeResize();

	/// The offset used to draw the dividers, and compartments
	public IETPoint getDrawOffset(final IETRect rectBounding);

	public int getIndexFromTSLogical( final IETPoint ptTSLogical );
	
}
