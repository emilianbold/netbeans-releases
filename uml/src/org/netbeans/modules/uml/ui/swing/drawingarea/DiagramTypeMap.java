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

import java.util.HashMap;

import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramKind;

/**
 * @author KevinM
 */
public class DiagramTypeMap {

	/**
	 *
	 */
	protected DiagramTypeMap() {
		super();
		m_cache = null;
		populateMap();
	}

	public static Integer getDiagramType(Object diagramName)
	{
		return (Integer)m_instance.m_cache.get(diagramName);
	}

	protected void populateMap()
	{
		m_cache = new HashMap();
		m_cache.put("Diagram", new Integer(IDiagramKind.DK_DIAGRAM));
		m_cache.put("Activity Diagram", new Integer(IDiagramKind.DK_ACTIVITY_DIAGRAM));
		m_cache.put("Class Diagram", new Integer(IDiagramKind.DK_CLASS_DIAGRAM));
		m_cache.put("Collaboration Diagram", new Integer(IDiagramKind.DK_COLLABORATION_DIAGRAM));
		m_cache.put("Component Diagram", new Integer(IDiagramKind.DK_COMPONENT_DIAGRAM));
		m_cache.put("Deployment Diagram", new Integer(IDiagramKind.DK_DEPLOYMENT_DIAGRAM));
		m_cache.put("Sequence Diagram", new Integer(IDiagramKind.DK_SEQUENCE_DIAGRAM));
		m_cache.put("State Diagram", new Integer(IDiagramKind.DK_STATE_DIAGRAM));
		m_cache.put("Use Case Diagram", new Integer(IDiagramKind.DK_USECASE_DIAGRAM));
	}

	protected HashMap m_cache;
	protected static DiagramTypeMap m_instance = new DiagramTypeMap();
}
