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
