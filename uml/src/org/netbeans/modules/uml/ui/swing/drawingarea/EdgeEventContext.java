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

import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;

/**
 * @author KevinM
 *
 */
public class EdgeEventContext implements IEdgeEventContext {

	IETPoint m_logicalPoint = null;
	boolean m_canceled = false;
	String m_viewDesc = null;
	
	/**
	 *
	 */
	public EdgeEventContext() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeEventContext#getLogicalPoint()
	 */
	public IETPoint getLogicalPoint() {
		return m_logicalPoint;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeEventContext#setLogicalPoint(org.netbeans.modules.uml.core.support.umlsupport.IETPoint)
	 */
	public void setLogicalPoint(IETPoint value) {
		m_logicalPoint = value;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeEventContext#getCancel()
	 */
	public boolean getCancel() {
		return m_canceled;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeEventContext#setCancel(boolean)
	 */
	public void setCancel(boolean value) {
		m_canceled = value;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeEventContext#getViewDescription()
	 */
	public String getViewDescription() {
		return m_viewDesc;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IEdgeEventContext#setViewDescription(java.lang.String)
	 */
	public void setViewDescription(String value) {
		m_viewDesc = value;
	}

}
