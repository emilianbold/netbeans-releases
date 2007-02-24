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

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.infrastructure.ICollaboration;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface IDesignPatternDetails
{
	public IProject getProject();
	public void setProject(IProject newVal);
	public boolean getShowOnDiagram();
	public void setShowOnDiagram(boolean newVal);
	public boolean getCreateDiagram();
	public void setCreateDiagram(boolean newVal);
	public INamespace getNamespace();
	public void setNamespace(INamespace newVal);
	public ICollaboration getCollaboration();
	public void setCollaboration(ICollaboration newVal);
	public ETList < IDesignPatternRole > getRoles();
	public void setRoles(ETList < IDesignPatternRole > newVal);
	public String getDiagramName();
	public void setDiagramName(String newVal);
	public boolean getRemoveOnPromote();
	public void setRemoveOnPromote(boolean newVal);

	public void addRole(IDesignPatternRole pRole);
	public void addParticipantName(String roleID, String partName);
	public ETList < String > getParticipantNames(String roleID);
	public void clearParticipantNames();
	public void addParticipantRole(String participantID, String roleID);
	public ETList < String > getParticipantRoles(String participantID);
	public void addParticipantInstance(String roleID, IElement pElement);
	public ETList < IElement > getParticipantInstances(String roleID);

}
