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


package org.netbeans.modules.uml.designpattern;

import javax.swing.JDialog;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.infrastructure.ICollaboration;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaDropContext;

public interface IDesignPatternManager
{
	// Validates the information stored in the pattern details
	public int validatePattern(IDesignPatternDetails pDetails);

	// Create elements for the instance participants which do not exist in the scope of the details
	public boolean createParticipants(IDesignPatternDetails pDetails);

	// Make the instance participants like the pattern participants
	public boolean cloneParticipants(IDesignPatternDetails pDetails);

	// Gets/Sets the pattern details of the manager
	public IDesignPatternDetails getDetails();

	// Gets/Sets the pattern details of the manager
	public void setDetails(IDesignPatternDetails newVal);

	// Build a details object representing information from the collaboration
	public void buildPatternDetails(Object pCollab, IDesignPatternDetails pDetails );

	// The project tree the addin deals with
	public IProjectTreeControl getProjectTree();

	// The project tree the addin deals with
	public void setProjectTree(IProjectTreeControl newVal);

	// Gets/Sets the how the participant instances should be created
	public int getParticipantScope();

	// Gets/Sets the how the participant instances should be created
	public void setParticipantScope(int newVal);

	// The current pattern
	public ICollaboration getCollaboration();

	// The current pattern
	public void setCollaboration(ICollaboration newVal);

	// Apply the pattern using these details
	public void applyPattern(IDesignPatternDetails pDetails);

	// Determines whether or not drag/drop should be allowed
	public boolean allowDragAndDrop(IDrawingAreaDropContext pContext);
	public ICollaboration getDragAndDropCollab(IDrawingAreaDropContext pContext);

	// Determines whether or not this diagram is owned by the addins project
	public boolean diagramOwnedByAddInProject(IProject pProject, IDiagram pParentDiagram, IDrawingAreaDropContext pContext);

	// Determines whether or not this element takes care of all of the roles of the pattern
	public boolean doesElementFulfillPattern(IElement pElement, ICollaboration pCollab);

	// Gets/Sets whether or not to display the apply dialog
	public boolean getDisplayGUI();

	// Gets/Sets whether or not to display the apply dialog
	public void setDisplayGUI(boolean newVal);

	// Determines whether or not all of the roles of the pattern have a participant instance representing them
	public boolean isPatternFulfilled(IDesignPatternDetails pDetails);

	// Promote the pattern in the details
	public void promotePattern(IDesignPatternDetails pDetails);

	// Figures out the appropriate elements from the details
	public ETList<IElement> getElementsFromDetails(IDesignPatternDetails pDetails);

	// Gets the patterns from the passed in project
	public ETList<IElement> getPatternsInProject(IProject pProject);

	public void setDialog(JDialog pDialog);

}
