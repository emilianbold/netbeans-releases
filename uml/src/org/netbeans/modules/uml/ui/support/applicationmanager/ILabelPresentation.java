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
