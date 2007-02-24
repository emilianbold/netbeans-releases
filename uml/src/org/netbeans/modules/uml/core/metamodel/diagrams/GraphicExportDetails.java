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


package org.netbeans.modules.uml.core.metamodel.diagrams;

import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;


public class GraphicExportDetails implements IGraphicExportDetails {

	public GraphicExportDetails() {
		m_BoundingRect = null;
		m_Map = null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.diagrams.IGraphicExportDetails#getGraphicBoundingRect()
	 */
	public IETRect getGraphicBoundingRect() {
		return m_BoundingRect;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.diagrams.IGraphicExportDetails#setGraphicBoundingRect(org.netbeans.modules.uml.core.support.umlsupport.IETRect)
	 */
	public void setGraphicBoundingRect(IETRect value) {
		m_BoundingRect = value;
	}

	public IETRect getFrameBoundingRect() {
		return m_FrameBoundingRect;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.diagrams.IGraphicExportDetails#setGraphicBoundingRect(org.netbeans.modules.uml.core.support.umlsupport.IETRect)
	 */
	public void setFrameBoundingRect(IETRect value) {
		m_FrameBoundingRect = value;
	}
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.diagrams.IGraphicExportDetails#getMapLocations()
	 */
	public ETList < IGraphicMapLocation > getMapLocations() {
		if (m_Map == null){
			m_Map = new ETArrayList<IGraphicMapLocation>();
		}
		return m_Map;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.diagrams.IGraphicExportDetails#setMapLocations()
	 */
	public void setMapLocations(ETList < IGraphicMapLocation > locations) {
		m_Map = locations;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.diagrams.IGraphicExportDetails#clear()
	 */
	public void clear() {
		m_BoundingRect = null;
		m_Map = null;
	}

	private IETRect m_BoundingRect;
	private IETRect m_FrameBoundingRect;
	private ETList < IGraphicMapLocation > m_Map;
}
