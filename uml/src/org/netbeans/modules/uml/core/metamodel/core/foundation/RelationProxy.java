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


package org.netbeans.modules.uml.core.metamodel.core.foundation;

import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAggregation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IGeneralization;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author sumitabhk
 *
 */
public class RelationProxy implements IRelationProxy{

	private IElement m_From = null;
	private IElement m_To = null;
	private IElement m_Connection = null;
	private String m_ConnectionElementType = null;
	private boolean m_RelationValidated = false;
        private boolean reconnected = false;

	/**
	 * 
	 */
	public RelationProxy() {
		super();
	}

	/**
	 * Gets the from element in this proxy.
	 *
	 * @param pVal
	 * 
	 * @result HRESULT
	 */
	public IElement getFrom() {
		return m_From;
	}

	/**
	 * Sets the from element in this proxy.
	 *
	 * @param newVal
	 * 
	 * @result HRESULT
	 */
	public void setFrom(IElement value) {
		m_From = value;
	}

	/**
	 * Gets the to element in this proxy.
	 *
	 * @param pVal
	 *
	 * @results HRESULT
	 */
	public IElement getTo() {
		return m_To;
	}

	/**
	 * Sets the to element in this proxy.
	 *
	 * @param newVal
	 *
	 * @results HRESULT
	 */
	public void setTo(IElement value) {
		m_To = value;
	}

	/**
	 *
	 * Gets the element that performs the connection between the two
	 * elements.
	 *
	 * @param pVal[out] The element
	 *
	 * @return HRESULT
	 *
	 */
	public IElement getConnection() {
		return m_Connection;
	}

	/**
	 * If the connection is 0 then this is the type of connection that should be verified.
	 *
	 * @param pVal
	 * 
	 * @result HRESULT
	 */
	public String getConnectionElementType() {
		return m_ConnectionElementType;
	}

	/**
	 * If the connection is 0 then this is the type of connection that should be verified.
	 *
	 * @param newVal
	 * 
	 * @result HRESULT
	 */
	public void setConnectionElementType(String value) {
		m_ConnectionElementType = value;
	}

	/**
	 * If used for validation this returns true if the relation has been validated.
	 *
	 * @param pVal
	 *
	 * @results HRESULT
	 */
	public boolean getRelationValidated() {
		return m_RelationValidated;
	}

	/**
	 * If used for validation this returns true if the relation has been validated.
	 *
	 * @param newVal
	 *
	 * @results HRESULT
	 */
	public void setRelationValidated(boolean value) {
		m_RelationValidated = value;
	}

	/**
	 *
	 * Sets the element that performs the connection between the two
	 * elements.
	 *
	 * @param newVal[in] 
	 *
	 * @return HRESULT
	 *
	 */
	public void setConnection(IElement value) {
		m_Connection = value;
	}

	/**
	 *
	 * Determines whether or not the passed-in elements
	 * match the corresponding elements in this proxy.
	 *
	 * @param from[in] The from element
	 * @param to[in] The to element
	 * @param connection[in] The connection element
	 * @param matches[out] true if a match is found
	 *
	 * @return HRESULT
	 *
	 */
	public boolean matches(IElement from, IElement to, IElement connection) {
		boolean isMatching = false;
		if (from != null && to != null && connection != null)
		{
			isMatching = m_Connection.isSame(connection);
		}
		return isMatching;
	}

	/**
	 *
	 * Retrieves the element on the from side of the internal connection object.
	 *
	 * @param pVal[out]  The element in that role
	 *
	 * @return HRESULT
	 *
	 */
	public IElement getRelationFrom() {
		return getRelationEnd(true);
	}

	/**
	 *
	 * Retrieves the element on the to side of the internal connection object.
	 *
	 * @param pVal[out]  The element in that role
	 *
	 * @return HRESULT
	 *
	 */
	public IElement getRelationTo() {
		return getRelationEnd(false);
	}

	/**
	 *
	 * Retrieves an end from the internal Connection type. Which end is retrieved is
	 * dictated by isFrom.
	 *
	 * @param isFrom[in] - true to retrieve the from end of the connection, else
	 *                   - false to retrieve the to end of the connection
	 * @param pVal[out]  The found end
	 *
	 * @return HRESULT
	 * @note The type of element actually returned depends completely on the type
	 *       of connection currently found on this Proxy.
	 *
	 * Connection Type         Returned End Type
	 * ---------------         -----------------
	 * Generalization          IClassifier
	 * Aggregation             The participant IClassifier of the AssociationEnd
	 * Association             The participant IClassifier of the AssociationEnd
	 * Dependency              INamedElement
	 *
	 */
	private IElement getRelationEnd(boolean isFrom) {
		IElement retEle = null;
		IElement connection = getConnection();
		if (connection != null)
		{
			if (connection instanceof IGeneralization)
			{
				IGeneralization gen = (IGeneralization)connection;
				retEle = getGeneralizationEnd(gen, isFrom);
			}
			else if (connection instanceof IAggregation)
			{
				IAggregation agg = (IAggregation)connection;
				retEle = getAggregationEnd(agg, isFrom);
			}
			else if (connection instanceof IAssociation)
			{
				IAssociation assoc = (IAssociation)connection;
				retEle = getAssociationEnd(assoc, isFrom);
			}
			else if (connection instanceof IDependency)
			{
				IDependency dep = (IDependency)connection;
				retEle = getDependencyEnd(dep, isFrom);
			}
		}
		return retEle;
	}

	/**
	 *
	 * Retrieves the appropriate end of the passed in Generalization
	 *
	 * @param gen[in]    The generalization
	 * @param isFrom[in] - true to retrieve the from end of the connection, else
	 *                   - false to retrieve the to end of the connection
	 * @param pVal[out]  The end
	 *
	 * @return HRESULT
	 *
	 */
	private IElement getGeneralizationEnd(IGeneralization gen, boolean isFrom) {
		IClassifier end = null;
		if (isFrom)
		{
			end = gen.getSpecific();
		}
		else
		{
			end = gen.getGeneral();
		}
		return end;
	}

	/**
	 *
	 * Retrieves the appropriate end of the passed in Aggregation
	 *
	 * @param agg[in]    The IAggregation
	 * @param isFrom[in] - true to retrieve the Aggregate end of the connection, else
	 *                   - false to retrieve the Part end of the connection
	 * @param pVal[out]  The end
	 *
	 * @return HRESULT
	 *
	 */
	private IElement getAggregationEnd(IAggregation agg, boolean isFrom) {
		IClassifier retEle = null;
		IAssociationEnd end = null;
		if (isFrom)
		{
			end = agg.getAggregateEnd();
		}
		else
		{
			end = agg.getPartEnd();
		}
		
		if(end != null)
		{
			retEle = end.getParticipant();
		}
		return retEle;
	}

	/**
	 *
	 * Retrieves the appropriate end of the passed in Association
	 *
	 * @param assoc[in]  The IAssociation
	 * @param isFrom[in] - true to retrieve the first end of the association, else
	 *                   - false to retrieve second end of the association
	 * @param pVal[out]  The end
	 *
	 * @return HRESULT
	 * @warning If the Association contains more than two ends, only the first two are
	 *          used in this routine.
	 *
	 */
	private IElement getAssociationEnd(IAssociation assoc, boolean isFrom) {
		IClassifier retEle = null;
		ETList<IAssociationEnd> ends = assoc.getEnds();
		if (ends != null)
		{
			int count = ends.size();
			if (count >= 2)
			{
				IAssociationEnd end = null;
				if (isFrom)
				{
					end = ends.get(0);
				}
				else
				{
					end = ends.get(1);
				}
				
				if(end != null)
				{
					retEle = end.getParticipant();
				}
			}
		}
		return retEle;
	}

	/**
	 *
	 * Retrieves the appropriate end of the passed in Dependency
	 *
	 * @param dep[in]  The Dependency
	 * @param isFrom[in] - true to retrieve the client end of the dependency, else
	 *                   - false to retrieve supplier end of the dependency
	 * @param pVal[out]  The end
	 *
	 * @return HRESULT
	 *
	 */
	private IElement getDependencyEnd(IDependency dep, boolean isFrom) {
		INamedElement end = null;
		if (isFrom)
		{
			end = dep.getClient();
		}
		else
		{
			end = dep.getSupplier();
		}
		
		return end;
	}
	

	/**
	 *
	 * Retrieves the element that physically owns the connection element. In every
	 * case EXCEPT for a Generalization, this will always by the Namespace of the
	 * connection. If the connection is an IGeneralization, the owner is the sub class
	 * or Specific role of the Generalization.
	 *
	 * @param pVal[out] The connection's owner
	 *
	 * @return HRESULT
	 * @warning If the Connection property returns 0, so will this method.
	 *
	 */
	public IElement getRelationOwner() {
		IElement retEle = null;
		IElement connection = getConnection();
		if (connection != null)
		{
			if (connection instanceof IGeneralization)
			{
				IGeneralization gen = (IGeneralization)connection;
				IClassifier sub = gen.getSpecific();
				if (sub != null)
				{
					retEle = sub;
				}
			}
			else
			{
				retEle = connection.getOwner();
			}
		}
		return retEle;
	}

    public boolean isReconnected()
    {
        return this.reconnected;
    }

    public void setReconnectionFlag(boolean val)
    {
        this.reconnected = val;
    }
}
