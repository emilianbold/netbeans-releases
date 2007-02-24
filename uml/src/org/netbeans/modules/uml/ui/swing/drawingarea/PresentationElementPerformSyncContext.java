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

/**
 * @author josephg
 *
 */
public class PresentationElementPerformSyncContext
	implements IPresentationElementPerformSyncContext {
	protected IDiagram m_Diagram = null;
	protected IPresentationElement m_PresentationElement = null;

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IPresentationElementPerformSyncContext#getDiagram()
	 */
	public IDiagram getDiagram() {
		return m_Diagram;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IPresentationElementPerformSyncContext#getPresentationElement()
	 */
	public IPresentationElement getPresentationElement() {
		return m_PresentationElement;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IPresentationElementPerformSyncContext#setDiagram(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram)
	 */
	public void setDiagram(IDiagram value) {
		m_Diagram = value;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.IPresentationElementPerformSyncContext#setPresentationElement(org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement)
	 */
	public void setPresentationElement(IPresentationElement value) {
		m_PresentationElement = value;
	}

}


