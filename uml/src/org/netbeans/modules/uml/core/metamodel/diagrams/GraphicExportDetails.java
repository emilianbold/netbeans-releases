/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
