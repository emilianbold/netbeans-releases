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


package org.netbeans.modules.uml.designpattern;

import java.util.Hashtable;
import java.util.Vector;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.infrastructure.ICollaboration;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public class DesignPatternDetails implements IDesignPatternDetails
{
	private ICollaboration				m_Pattern = null;
	private IProject 					m_Project = null;
	private INamespace 					m_Namespace = null;
	boolean								m_CreateDiagram = false;
	boolean								m_ShowOnDiagram = false;
	String								m_DiagramName = "";
	boolean								m_RemoveOnPromote = false;

	private Hashtable < String, Vector<String> >	m_ParticipantNameMap = new Hashtable < String, Vector<String> >();
	private Hashtable < String, Vector<String> >	m_ParticipantRoleMap = new Hashtable < String, Vector<String> >();
	private Hashtable < String, Vector<IElement> >	m_ParticipantInstanceMap = new Hashtable < String, Vector<IElement> >();

	private ETList < IDesignPatternRole >	m_Roles = new ETArrayList < IDesignPatternRole >();

	/**
	 *
	 */
	public DesignPatternDetails()
	{
		super();
	}

	/**
	 * Gets the stored ICollaboration
	 *
	 * @param[out] pVal		The collaboration on this pattern details
	 *
	 * @return HRESULT
	 */
	public ICollaboration getCollaboration()
	{
		return m_Pattern;
	}
	/**
	 * Sets the stored ICollaboration
	 *
	 * @param[in] newVal		The collaboration on this pattern details
	 *
	 * @return HRESULT
	 */
	public void setCollaboration(ICollaboration newVal)
	{
	   m_Pattern = newVal;
	}
	/**
	 * Gets the stored INamespace
	 *
	 * @param[out] pVal		The namespace on this pattern details
	 *
	 * @return HRESULT
	 */
	public INamespace getNamespace()
	{
		INamespace space = null;
	    if (m_Namespace != null)
	    {
	   		space = m_Namespace;
	    }
		else
		{
			// if there isn't a namespace and we ask for one
			// return the project
			if (m_Project != null)
			{
				space = m_Project;
			}
		}
		return space;
	}
	/**
	 * Sets the stored INamespace
	 *
	 * @param[out] pVal		The namespace on this pattern details
	 *
	 * @return HRESULT
	 */
	public void setNamespace(INamespace newVal)
	{
		m_Namespace = newVal;
	}
	/**
	 * Whether or not a diagram should be created when a pattern is applied.
	 * The diagram would contain the information that was created/changed during
	 * the apply process.
	 *
	 * @param[out] pVal		The flag for whether or not a diagram should be created
	 *
	 * @return HRESULT
	 */
	public boolean getCreateDiagram()
	{
		return m_CreateDiagram;
	}
	/**
	 * See getCreateDiagram
	 *
	 *
	 * @return HRESULT
	 */
	public void setCreateDiagram(boolean newVal)
	{
	   m_CreateDiagram = newVal;
	}
	/**
	 * Whether or not to show the actual collaboration on the diagram that is created
	 * when a pattern is applied
	 *
	 * @param[out] pVal		The flag for whether or not to create the collaboration draw object
	 *
	 * @return HRESULT
	 */
	public boolean getShowOnDiagram()
	{
		return m_ShowOnDiagram;
	}
	/**
	 * See getShowOnDiagram
	 *
	 * @return HRESULT
	 */
	public void setShowOnDiagram(boolean newVal)
	{
		m_ShowOnDiagram = newVal;
	}
	/**
	 * Gets the roles associated with these details
	 *
	 * @param[out] pVal		The roles
	 *
	 * @return HRESULT
	 */
	public ETList < IDesignPatternRole > getRoles()
	{
		return m_Roles;
	}
	/**
	 * Sets the stored roles for these details
	 *
	 * @param[in]	newVal	The roles
	 *
	 * @return HRESULT
	 */
	public void setRoles(ETList <IDesignPatternRole > newVal)
	{
		m_Roles = newVal;
	}
	/**
	 * Adds the passed in role to these details
	 *
	 * @param[in]	pRole		The role to add
	 *
	 * @return HRESULT
	 */
	public void addRole(IDesignPatternRole pRole)
	{
		if (m_Roles == null)
		{
			m_Roles = new ETArrayList < IDesignPatternRole >();
		}
		if (pRole != null)
		{
			m_Roles.add(pRole);
		}
	}
	/**
	 * A map is kept for every participant in a pattern and what role that participant
	 * is supposed to be playing.
	 *
	 * @param[in]	roleID		The role ID that the participant is playing
	 * @param[in]	partName		The name of the participant
	 *
	 * @return HRESULT
	 */
	public void addParticipantName(String roleID, String partName)
	{
		if (roleID != null && partName != null)
		{
			// only want to add the name for a particular role, if it isn't already there
			Vector <String> pVals = m_ParticipantNameMap.get(roleID);
			if (pVals == null)
			{
				pVals = new Vector <String>();
				if (pVals != null)
				{
					pVals.add(partName);
					m_ParticipantNameMap.put(roleID, pVals);
				}
			}
			else
			{
				boolean bFound = false;
				int cnt = pVals.size();
				for (int x = 0; x < cnt; x++)
				{
					String str = pVals.get(x);
					if (str != null && str.length() > 0)
					{
						if (str.equals(roleID)){
							bFound = true;
							break;
						}
					}
				}
				if (!bFound){
					pVals.add(partName);
					m_ParticipantNameMap.put(roleID, pVals);
				}
			}
		}
	}
	/**
	 * Get the entries from the map that match the passed in roleID.  This will be the
	 * names of the participants that will playing that role.
	 *
	 * @param[in]	roleID		The roleID to find
	 * @param[out]	partNames	The names of the participants playing this role
	 *
	 * @return HRESULT
	 */
	public ETList < String > getParticipantNames(String roleID)
	{
		ETList <String> pTemp = new ETArrayList<String>();
		if (roleID != null && roleID.length() > 0)
		{
			Vector <String> pVals = m_ParticipantNameMap.get(roleID);
			if (pVals != null)
			{
				int count = pVals.size();
				for (int x = 0; x < count; x++)
				{
					String partName = pVals.get(x);
					if (partName != null && partName.length() > 0)
					{
						pTemp.add(partName);
					}
				}
			}
		}
	   return pTemp;
	}
	/**
	 * Clears the entries from the participant name map.
	 *
	 *
	 * @return HRESULT
	 */
	public void clearParticipantNames()
	{
		m_ParticipantNameMap.clear();
	}
	/**
	 * A map is kept of the items playing in the pattern and the role that they are playing
	 *
	 * @param[in]	partID		The id of the element playing in the pattern
	 * @param[in]	roleID		The role ID that the element is playing
	 *
	 * @return HRESULT
	 */
	public void addParticipantRole(String participantID, String roleID)
	{
		if (participantID != null && roleID != null)
		{
			Vector <String> pVals = m_ParticipantRoleMap.get(participantID);
			if (pVals == null)
			{
				pVals = new Vector <String>();
				if (pVals != null)
				{
					pVals.add(roleID);
					m_ParticipantRoleMap.put(participantID, pVals);
				}
			}
			else
			{
				boolean bFound = false;
				int cnt = pVals.size();
				for (int x = 0; x < cnt; x++)
				{
					String str = pVals.get(x);
					if (str != null && str.length() > 0)
					{
						if (str.equals(roleID)){
							bFound = true;
							break;
						}
					}
				}
				if (!bFound){
					pVals.add(roleID);
					m_ParticipantRoleMap.put(participantID, pVals);
				}
			}
		}
	}
	/**
	 * Get the entries from the map that match the passed in id.  This will be the
	 * names of the participants that will playing that role.
	 *
	 * @param[in]	ID				The ID to find
	 * @param[out]	roleIDs		The role ids that the element is in
	 *
	 * @return HRESULT
	 */
	public ETList < String > getParticipantRoles(String participantID)
	{
		ETList <String> pTemp = new ETArrayList<String>();
		if (participantID != null && participantID.length() > 0)
		{
			Vector <String> pVals = m_ParticipantRoleMap.get(participantID);
			if (pVals != null)
			{
				int count = pVals.size();
				for (int x = 0; x < count; x++)
				{
					String roleID = pVals.get(x);
					if (roleID != null && roleID.length() > 0)
					{
						pTemp.add(roleID);
					}
				}
			}
		}
	   return pTemp;
	}
	/**
	 * A map is kept of the roles in the pattern and the element that is playing it
	 *
	 * @param[in]	roleID		The role ID
	 * @param[in]	pElement		The element playing the role
	 *
	 * @return HRESULT
	 */
	public void addParticipantInstance(String roleID, IElement pElement)
	{
		if (roleID != null && pElement != null)
		{
			Vector <IElement> pVals = m_ParticipantInstanceMap.get(roleID);
			if (pVals == null)
			{
				pVals = new Vector <IElement>();
				if (pVals != null)
				{
					pVals.add(pElement);
					m_ParticipantInstanceMap.put(roleID, pVals);
				}
			}
			else
			{
				boolean bFound = false;
				int cnt = pVals.size();
				for (int x = 0; x < cnt; x++)
				{
					IElement pEle = pVals.get(x);
					if (pEle != null)
					{
						if (pEle.isSame(pElement)){
							bFound = true;
							break;
						}
					}
				}
				if (!bFound){
					pVals.add(pElement);
					m_ParticipantInstanceMap.put(roleID, pVals);
				}
			}
		}
	}
	/**
	 * Get the entries from the map that match the passed in role id.  This will be the
	 * elements that will playing that role.
	 *
	 * @param[in]	ID				The ID to find
	 * @param[out]	pElements	The elements playing the role
	 *
	 * @return HRESULT
	 */
	public ETList < IElement > getParticipantInstances(String roleID)
	{
		ETList <IElement> pTemp = new ETArrayList<IElement>();
		if (roleID != null && roleID.length() > 0)
		{
			Vector <IElement> pVals = m_ParticipantInstanceMap.get(roleID);
			if (pVals != null)
			{
				int count = pVals.size();
				for (int x = 0; x < count; x++)
				{
					IElement pElement = pVals.get(x);
					if (pElement != null)
					{
						pTemp.add(pElement);
					}
				}
			}
		}
	   return pTemp;
	}
	/**
	 * Gets the stored IProject from these details
	 *
	 * @param[out]	pVal		The stored project
	 *
	 * @return HRESULT
	 */
	public IProject getProject()
	{
		return m_Project;
	}
	/**
	 * Sets the project on these details
	 *
	 * @param[in] newVal		The project to store
	 *
	 * @return HRESULT
	 */
	public void setProject(IProject newVal)
	{
		m_Project = newVal;
	}
	/**
	 * The name of the diagram to create when a pattern is applied.
	 * The diagram would contain the information that was created/changed during
	 * the apply process.
	 *
	 * @param[out] pVal		The name of the diagram to create
	 *
	 * @return HRESULT
	 */
	public String getDiagramName()
	{
		return m_DiagramName;
	}
	/**
	 * See getDiagramName
	 *
	 *
	 * @return HRESULT
	 */
	public void setDiagramName(String newVal)
	{
	   m_DiagramName = newVal;
	}
	/**
	 * Whether or not a to remove a pattern from its current project when promoting
	 * it to the design center area.
	 *
	 * @param[out] pVal		The flag for whether or not to remove the pattern
	 *
	 * @return HRESULT
	 */
	public boolean getRemoveOnPromote()
	{
	   return m_RemoveOnPromote;
	}
	/**
	 * See getRemoveOnPromote
	 *
	 *
	 * @return HRESULT
	 */
	public void setRemoveOnPromote(boolean newVal)
	{
	   m_RemoveOnPromote = newVal;
	}

}
