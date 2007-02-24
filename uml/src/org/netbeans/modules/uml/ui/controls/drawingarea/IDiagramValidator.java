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



package org.netbeans.modules.uml.ui.controls.drawingarea;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramValidation;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramValidationResult;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;

/**
 * @author josephg
 *
 */
public interface IDiagramValidator {
	//  Validates the diagram.
	IDiagramValidationResult validateDiagram(IDiagram diagram, boolean onlySelectedElements, IDiagramValidation diagramValidation);
	
	//  Does the post select validation on the element 
	void doPostSelectValidation(IDiagram diagram, IETGraphObject etGraphObject);

	//  Force the presentation elements attached to this element to be deep synched 
	void forceElementDeepSync(IDiagram diagram, IElement elementToDeepSync);
}


