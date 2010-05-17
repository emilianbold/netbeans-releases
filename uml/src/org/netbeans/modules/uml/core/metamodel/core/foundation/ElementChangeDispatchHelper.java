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

import org.dom4j.Node;

import org.netbeans.modules.uml.core.eventframework.EventDispatchHelper;
import org.netbeans.modules.uml.core.eventframework.EventDispatchNameKeeper;
import org.netbeans.modules.uml.core.eventframework.IEventDispatcher;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author sumitabhk
 *
 */
public class ElementChangeDispatchHelper extends EventDispatchHelper implements IElementChangeDispatchHelper{

	private ETList<INamedElement> m_colliding = null;

	/**
	 * 
	 */
	public ElementChangeDispatchHelper() {
		super();
	}

	public boolean dispatchElementPreModified(IVersionableElement element) {
		boolean proceed = true;
		IElementChangeEventDispatcher disp = modDispatcher();
		if (disp != null)
		{
			IEventPayload payload = disp.createPayload("ElementPreModified");
			proceed = disp.fireElementPreModified(element, payload);
		}
		return proceed;
	}

	public long dispatchElementModified(IVersionableElement element) {
		IElementChangeEventDispatcher disp = modDispatcher();
		if (disp != null)
		{
			IEventPayload payload = disp.createPayload("ElementModified");
			disp.fireElementModified(element, payload);
		}
		return 0;
	}

	/**
	 *
	 * Retrieves the element modify dispatcher
	 *
	 * @param disp[out] The dispatcher
	 *
	 * @return HRESULT
	 */
	private IElementChangeEventDispatcher modDispatcher() {
		IElementChangeEventDispatcher disp = null;
		if (m_dispatcher == null)
		{
			retrieveModifyDispatcher();
		}
		if(m_dispatcher != null)
		{
			disp = (IElementChangeEventDispatcher)m_dispatcher;
		}
		return disp;
	}

	/**
	 *
	 * Fires the OnMetaAttributePreModified event.
	 *
	 * @param element[in] The VersionableElement that is about to be affected
	 * @param attrName[in] The name of the XMI attribute to be affected
	 * @param newValue[in] The new value to be assigned to the XMI attribute
	 * @param payload[out] The filled in payload that was delivered with this event. This payload
	 *                     can be used in the DispatchMetaAttrModified() method.
	 * @param proceed[out] true if the event was fully dispatched, else
	 *                     false if a listener cancelled full dispatch.
	 *
	 * @return HRESULT
	 * @see DispatchMetaAttrModified()
	 *
	 */
	public boolean dispatchMetaAttrPreMod(IVersionableElement element, 
										  String attrName, 
										  String newValue, 
										  IMetaAttributeModifiedEventPayload payload) {
		boolean proceed = true;
		IElementChangeEventDispatcher disp = modDispatcher();
		if (disp != null)
		{
			payload = (IMetaAttributeModifiedEventPayload)disp.createPayload("ElementPreModified");
			if (payload != null)
			{
				populateMetaAttrLoad(element, attrName, newValue, payload);
				proceed = disp.fireMetaAttributePreModified(payload);
			}
		}
		return proceed;
	}

	/**
	 *
	 * Dispatches the OnMetaAttributeModified event
	 *
	 * @param payload[in] The payload that describes the data associated with the event.
	 *
	 * @return HRESULT
	 *
	 */
	public long dispatchMetaAttrModified(IMetaAttributeModifiedEventPayload payload) {
		IElementChangeEventDispatcher disp = modDispatcher();
		if (disp != null)
		{
			disp.fireMetaAttributeModified(payload);
		}
		return 0;
	}

	/**
	 *
	 * Fired when the Documentation tagged value on an element is about to be modified
	 *
	 * @param element[in] The element whose docs are being modified
	 * @param doc[in] The new text of the docs
	 * @param proceed[out] true to continue, else false
	 *
	 * @return HRESULT
	 *
	 */
	public boolean dispatchDocPreModified(IElement element, String doc) {
		boolean proceed = true;
		IElementChangeEventDispatcher disp = modDispatcher();
		if (disp != null)
		{
			IEventPayload payload = disp.createPayload("DocumentationPreModified");
			proceed = disp.fireDocumentationPreModified(element, doc, payload);
		}
		return proceed;
	}

	/**
	 *
	 * Fired after the documentation tagged value of the passed in element has been set.
	 *
	 * @param element[in] The element who was affected.
	 *
	 * @return HRESULT
	 *
	 */
	public long dispatchDocModified(IElement element) {
		IElementChangeEventDispatcher disp = modDispatcher();
		if (disp != null)
		{
			IEventPayload payload = disp.createPayload("DocumentationModified");
			disp.fireDocumentationModified(element, payload);
		}
		return 0;
	}

	/**
	 *
	 * Called whenever an element is about to be added to a namespace.
	 *
	 * @param space[in] The namespace about to be modified
	 * @param elementToAdd[in] The element being added to space
	 * @param proceed[out] true to proceed, else false
	 *
	 * @return HRESULT
	 *
	 */
	public boolean dispatchPreElementAddedToNamespace(INamespace space, INamedElement elementToAdd) {
		boolean proceed = true;
		IElementChangeEventDispatcher disp = modDispatcher();
		if (disp != null)
		{
			IEventPayload payload = disp.createPayload("PreElementAddedToNamespace");
			proceed = disp.firePreElementAddedToNamespace(space, elementToAdd, payload);
		}
		return proceed;
	}

	/**
	 *
	 * Called right after an element has been added to the given namespace
	 *
	 * @param space[in] The namespace just modified
	 * @param elementToAdd[in] The element that was just added to space.
	 *
	 * @return HRESULT
	 *
	 */
	public long dispatchElementAddedToNamespace(INamespace space, INamedElement elementToAdd) {
		IElementChangeEventDispatcher disp = modDispatcher();
		if (disp != null)
		{
			IEventPayload payload = disp.createPayload("ElementAddedToNamespace");
			disp.fireElementAddedToNamespace(space, elementToAdd, payload);
		}
		return 0;
	}

	/**
	 *
	 * Fired right before the name of a named element is about to change.
	 *
	 * @param element[in] The element about to be modified
	 * @param proposedName[in] The new name
	 * @param proceed[out] true to proceed, else false
	 *
	 * @return HRESULT
	 *
	 */
	public boolean dispatchPreNameModified(INamedElement element, String proposedName) {
		boolean proceed = true;
		IElementChangeEventDispatcher disp = modDispatcher();
		if (disp != null)
		{
			IEventPayload payload = disp.createPayload("PreNameModified");
			proceed = disp.firePreNameModified(element, proposedName, payload);
		}
		return proceed;
	}

	/**
	 *
	 * Called right after the name of the passed in element has changed.
	 *
	 * @param element[in] The element that was modified.
	 *
	 * @return HRESULT
	 *
	 */
	public long dispatchNameModified(INamedElement element) {
		IElementChangeEventDispatcher disp = modDispatcher();
		if (disp != null)
		{
			IEventPayload payload = disp.createPayload("NameModified");
			disp.fireNameModified(element, payload);
		}
		return 0;
	}

	/**
	 *
	 * Fired right before the visibility flag of an element is about to change.
	 *
	 * @param element[in] The element changing
	 * @param proposedValue[in] The new value of the visibility flag
	 * @param proceed[out] true to proceed, else false
	 *
	 * @return HRESULT
	 *
	 */
	public boolean dispatchPreVisibilityModified(INamedElement element, int proposedValue) {
		boolean proceed = true;
		IElementChangeEventDispatcher disp = modDispatcher();
		if (disp != null)
		{
			IEventPayload payload = disp.createPayload("PreVisibilityModified");
			proceed = disp.firePreVisibilityModified(element, proposedValue, payload);
		}
		return proceed;
	}

	/**
	 *
	 * Called right after the visibility flag of the passed in element was changed.
	 *
	 * @param element[in] The element modified.
	 *
	 * @return HRESULT
	 *
	 */
	public long dispatchVisibilityModified(INamedElement element) {
		IElementChangeEventDispatcher disp = modDispatcher();
		if (disp != null)
		{
			IEventPayload payload = disp.createPayload("VisibilityModified");
			disp.fireVisibilityModified(element, payload);
		}
		return 0;
	}

	/**
	 *
	 * Fired right before the alias name of a named element is about to change.
	 *
	 * @param element[in] The element about to be modified
	 * @param proposedName[in] The new name
	 * @param proceed[out] true to proceed, else false
	 *
	 * @return HRESULT
	 *
	 */
	public boolean dispatchPreAliasNameModified(INamedElement element, String proposedName) {
		boolean proceed = true;
		IElementChangeEventDispatcher disp = modDispatcher();
		if (disp != null)
		{
			IEventPayload payload = disp.createPayload("PreAliasNameModified");
			proceed = disp.firePreAliasNameModified(element, proposedName, payload);
		}
		return proceed;
	}

	/**
	 *
	 * Called right after the alias name of the passed in element has changed.
	 *
	 * @param element[in] The element that was modified.
	 *
	 * @return HRESULT
	 *
	 */
	public long dispatchAliasNameModified(INamedElement element) {
		IElementChangeEventDispatcher disp = modDispatcher();
		if (disp != null)
		{
			IEventPayload payload = disp.createPayload("AliasNameModified");
			disp.fireAliasNameModified(element, payload);
		}
		return 0;
	}

	public ETList<INamedElement> getCollidingElements() 
	{
		return m_colliding;
	}

	public void setCollidingElements(ETList<INamedElement> value) 
	{
		m_colliding = value;
	}

	public boolean dispatchPreNameCollision(INamedElement element, String proposedValue) {
		boolean proceed = true;
		IElementChangeEventDispatcher disp = modDispatcher();
		if (disp != null)
		{
			IEventPayload payload = disp.createPayload("PreNameCollision");
			proceed = disp.firePreNameCollision(element, proposedValue, m_colliding, payload);
		}
		return proceed;
	}

	public long dispatchNameCollision(INamedElement element) {
		IElementChangeEventDispatcher disp = modDispatcher();
		if (disp != null)
		{
			IEventPayload payload = disp.createPayload("NameCollision");
			disp.fireNameCollision(element, m_colliding, payload);
		}
		return 0;
	}

	/**
	 *
	 * Retrieves the event dispatcher used to signal element modification events
	 *
	 * @param disp[out] The dispatcher
	 *
	 * @return HRESULT
	 *
	 */
	protected void retrieveModifyDispatcher()
	{
		IElementChangeEventDispatcher disp = null;
		IEventDispatcher dispatch = retrieveDispatcher(EventDispatchNameKeeper.modifiedName());
		if (dispatch != null && dispatch instanceof IElementChangeEventDispatcher)
		{
			disp = (IElementChangeEventDispatcher)dispatch;
			m_dispatcher = disp;
		}
	}

	/**
	 *
	 * Populates the 
	 *
	 * @param element[in] The VersionableElement that is about to be affected
	 * @param attrName[in] The name of the XMI attribute to be affected
	 * @param newValue[in] The new value to be assigned to the XMI attribute
	 * @param payload[in] The payload to be filled out.
	 *
	 * @return 
	 *
	 */
	protected void populateMetaAttrLoad( IVersionableElement element, 
										 String attrName, 
										 String newValue, 
										 IMetaAttributeModifiedEventPayload payload )
	{
		// Retrieve the current value of the XMI attribute.
		Node node = element.getNode();
		if (node != null)
		{
			String curVal = XMLManip.getAttributeValue(node, attrName);
			payload.setElement(element);
			payload.setPropertyName(attrName);
			payload.setOriginalValue(curVal);
			payload.setNewValue(newValue);
		}
	}
	
}



