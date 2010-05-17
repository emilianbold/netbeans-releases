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


package org.netbeans.modules.uml.core.metamodel.core.foundation;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.DispatchHelper;

/**
 * @author sumitabhk
 *
 */
public class Constraint extends AutonomousElement implements IConstraint, IExpressionListener{

	/**
	 *
	 */
	public Constraint() {
		super();
	}

	/**
	 * Gets the condition that must be true when evaluated in order for an
	 * instance in a system to be well-formed.
	 *
	 * @Param exp [out] 
	 *
	 * @return
	 */
	public IValueSpecification getSpecification() 
   {
      ElementCollector< IValueSpecification > col = new ElementCollector< IValueSpecification >();
		return col.retrieveSingleElement(m_Node, "UML:Constraint.specification/*", IValueSpecification.class);
	}

	/**
	 * Sets the condition that must be true when evaluated in order for an
	 * instance in a system to be well-formed.
	 *
	 * @Param exp [in] 
	 *
	 * @return
	 */
	public void setSpecification(IValueSpecification spec) {
		addChild("UML:Constraint.specification", "UML:Constraint.specification", spec);
	}

	/**
	 * Adds an element to this Constraint's list of constrained elements.
	 *
	 * @Param element [in] 
	 *
	 * @return
	 */
	public void addConstrainedElement(IElement elem) {
		addElementByID(elem, "constrainedElement");
	}

	/**
	 * Removes an element from this constraint.
	 *
	 * @Param element [in] 
	 *
	 * @return
	 */
	public void removeConstrainedElement(IElement elem) {
		removeElementByID(elem, "constrainedElement");
	}

	/**
	 * Retrieves the collection of elements this constraint is applied to.
	 *
	 * @Param elements [out] 
	 *
	 * @return
	 */
	public ETList<IElement> constrainedElements() 
    {
      ElementCollector< IElement > col = new ElementCollector< IElement >();
	  return col.retrieveElementCollectionWithAttrIDs(this, "constrainedElement", IElement.class);      
	}

	/**
	 *
	 * Determines whether or not the passed in NamedElement is already being
	 * constrained by this Constraint
	 *
	 * @param element[in] The Element to look for
	 * @param flag[out] True if the element is already in the collection
	 *					  of constrained elements, else Falso if the element is not constrained.
	 *
	 * @return HRESULTs
	 */
	public boolean isConstrained(IElement elem) {
		return isElementPresent(elem, "constrainedElement", true);
	}

	/**
	 *
	 * Retrieves the string representation of the constraint
	 *
	 * @param pVal[out] The expression in a string
	 *
	 * @return HRESULT
	 *
	 */
	public String getExpression() {
		String val = null;
		IValueSpecification spec = getSpecification();
		if (spec instanceof IExpression)
		{
			val = ((IExpression)spec).getBody();
		}
		return val;
	}

	/**
	 *
	 * Sets the expression of this Constraint with the passed-in string
	 *
	 * @param newVal[in] The expression body
	 *
	 * @return HRESULT
	 *
	 */
	public void setExpression(String str) {
		IValueSpecification spec = getSpecification();
		boolean created = false;
		IExpression exp = null;
		if (spec instanceof IExpression)
		{
			exp = (IExpression)spec;
		}
		else
		{
			TypedFactoryRetriever < IExpression > ret = new TypedFactoryRetriever < IExpression >();
			exp = ret.createType("Expression");
			created = true;
		}
		
		if (exp != null)
		{
            if (created)
                setSpecification(exp);
			exp.setBody(str);
		}
	}

	/**
	 * Establishes the appropriate XML elements for this UML type.
	 *
	 * [in] The document where this element will reside
	 * [in] The element's parent node.
	 *
	 * @return HRESULT
	 */

	public void establishNodePresence( Document doc, Node parent )
	{
	   buildNodePresence( "UML:Constraint", doc, parent );
	}

	/**
	 *
	 * @see AutonomousElementImpl::PerformDuplication()
	 *
	 */
	public IVersionableElement performDuplication()
	{
		IVersionableElement dup = super.performDuplication();
		IValueSpecification spec = getSpecification();
		if (spec != null)
		{
			IVersionableElement ver = spec.duplicate();
			if (ver instanceof IValueSpecification)
			{
				((IConstraint)dup).setSpecification((IValueSpecification)ver);
			}
		}
		return dup;
	}

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IExpressionListener#onBodyModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression)
    */
   public void onBodyModified(IExpression exp)
   {
		IElementChangeEventDispatcher disp = (new DispatchHelper()).getElementChangeDispatcher();
		if (disp != null){
			disp.fireElementModified(this, disp.createPayload("ElementModified"));
		}

   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IExpressionListener#onLanguageModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression)
    */
   public void onLanguageModified(IExpression exp)
   {
 
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IExpressionListener#onPreBodyModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression, java.lang.String)
    */
   public boolean onPreBodyModified(IExpression exp, String proposedValue)
   {
		return true;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IExpressionListener#onPreLanguageModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression, java.lang.String)
    */
   public boolean onPreLanguageModified(IExpression exp, String proposedValue)
   {
      return false;
   }

}



