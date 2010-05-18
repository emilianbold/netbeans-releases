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

import java.util.Vector;

import org.netbeans.modules.uml.core.eventframework.EventDispatcher;
import org.netbeans.modules.uml.core.eventframework.EventFunctor;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.eventframework.EventManager;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author sumitabhk
 *
 */
public class ElementChangeEventDispatcher extends EventDispatcher implements IElementChangeEventDispatcher{

	private EventManager< IElementModifiedEventsSink > m_ElementSink = null;
	private EventManager< IElementModifiedEventsSink > m_GuarenteedElementSink = null;
	private EventManager< IMetaAttributeModifiedEventsSink > m_MetaAttrSink = null;
	private EventManager< IDocumentationModifiedEventsSink > m_DocSink = null;
	private EventManager< INamespaceModifiedEventsSink > m_NamespaceSink = null;
	private EventManager< INamedElementEventsSink > m_NamedElementSink = null;
	private EventManager< IImportEventsSink > m_ImportSink = null;
	private EventManager< IExternalElementEventsSink > m_ExtSink = null;
	private EventManager< IStereotypeEventsSink > m_StereotypeSink = null;
	private EventManager< IRedefinableElementModifiedEventsSink > m_RedefSink = null;
	private EventManager< IPackageEventsSink > m_PackSink = null;
	
	/**
	 * 
	 */
	public ElementChangeEventDispatcher() {
		super();

		//initialize the event manager member variables
		m_ElementSink = new EventManager<IElementModifiedEventsSink>();
		m_GuarenteedElementSink = new EventManager<IElementModifiedEventsSink>();
		m_MetaAttrSink = new EventManager<IMetaAttributeModifiedEventsSink>();
		m_DocSink = new EventManager<IDocumentationModifiedEventsSink>();
		m_NamespaceSink = new EventManager<INamespaceModifiedEventsSink>();
		m_NamedElementSink = new EventManager<INamedElementEventsSink>();
		m_ImportSink = new EventManager<IImportEventsSink>();
		m_ExtSink = new EventManager<IExternalElementEventsSink>();
		m_StereotypeSink = new EventManager<IStereotypeEventsSink>();
		m_RedefSink = new EventManager<IRedefinableElementModifiedEventsSink>();
		m_PackSink = new EventManager<IPackageEventsSink>();
	}

	/**
	 *
	 * Registers the passed-in event sink with this dispatcher.
	 *
	 * @param sink[in] The actual sink that will recieve notifications
	 * @param cookie[in] The unique identifier to be used when removing the listener
	 *
	 * @return HRESULT
	 * 
	 */
	public void registerForElementModifiedEvents(IElementModifiedEventsSink handler) 
	{
		m_ElementSink.addListener(handler, null);
	}

	/**
	 *
	 * Removes a listener from the current list.
	 *
	 * @param cookie[in] The unique cookie used to identify the sink to remove
	 *
	 * @return S_OK
	 * 
	 */
	public void revokeElementModifiedSink(IElementModifiedEventsSink handler) 
	{
		m_ElementSink.removeListener(handler);
	}

	/**
	 *
	 * Registers the passed-in handler on the dispatch list that will be notified of any element modified event,
	 * regardless of whether or not the events are plugged.
	 *
	 * @param handler[in] The sink
	 * @param cookie[out] The cookie returned for revoke purposes
	 *
	 * @return HRESULT
	 *
	 */
	public void registerForGuarenteedElementModifiedEvents(IElementModifiedEventsSink handler) 
	{
		m_GuarenteedElementSink.addListener( handler, null );
	}

	/**
	 *
	 * Removes a listener from the current guarenteed list.
	 *
	 * @param cookie[in] The unique cookie used to identify the sink to remove
	 *
	 * @return S_OK
	 * 
	 */
	public void revokeGuarenteedElementModifiedSink(IElementModifiedEventsSink handler) 
	{
		m_GuarenteedElementSink.removeListener( handler );
	}

	/**
	 *
	 * Registers the passed-in event sink with this dispatcher.
	 *
	 * @param sink[in] The actual sink that will receive notifications
	 * @param cookie[in] The unique identifier to be used when removing the listener
	 *
	 * @return HRESULT
	 * 
	 */
	public void registerForMetaAttributeModifiedEvents(IMetaAttributeModifiedEventsSink handler) 
	{
		m_MetaAttrSink.addListener( handler, null );
	}

	/**
	 *
	 * Removes a listener from the current list.
	 *
	 * @param cookie[in] The unique cookie used to identify the sink to remove
	 *
	 * @return S_OK
	 * 
	 */
	public void revokeMetaAttributeModifiedSink(IMetaAttributeModifiedEventsSink handler) 
	{
		m_MetaAttrSink.removeListener( handler );
	}

	/**
	 *
	 * Registers the passed-in event sink with this dispatcher.
	 *
	 * @param sink[in] The actual sink that will recieve notifications
	 * @param cookie[in] The unique identifier to be used when removing the listener
	 *
	 * @return HRESULT
	 * 
	 */
	public void registerForDocumentationModifiedEvents(IDocumentationModifiedEventsSink handler) 
	{
		m_DocSink.addListener( handler, null );
	}

	/**
	 *
	 * Removes a listener from the current list.
	 *
	 * @param cookie[in] The unique cookie used to identify the sink to remove
	 *
	 * @return S_OK
	 * 
	 */
	public void revokeDocumentationModifiedSink(IDocumentationModifiedEventsSink handler)
	{
		m_DocSink.removeListener( handler );
	}

	/**
	 *
	 * Registers the passed-in event sink with this dispatcher.
	 *
	 * @param sink[in] The actual sink that will receive notifications
	 * @param cookie[in] The unique identifier to be used when removing the listener
	 *
	 * @return HRESULT
	 * 
	 */
	public void registerForNamespaceModifiedEvents(INamespaceModifiedEventsSink handler) 
	{
		m_NamespaceSink.addListener( handler, null );
	}

	/**
	 *
	 * Removes a listener from the current list.
	 *
	 * @param cookie[in] The unique cookie used to identify the sink to remove
	 *
	 * @return S_OK
	 * 
	 */
	public void revokeNamespaceModifiedSink(INamespaceModifiedEventsSink handler) 
	{
		m_NamespaceSink.removeListener( handler );
	}

	/**
	 *
	 * Registers the passed-in event sink with this dispatcher.
	 *
	 * @param sink[in] The actual sink that will receive notifications
	 * @param cookie[in] The unique identifier to be used when removing the listener
	 *
	 * @return HRESULT
	 * 
	 */
	public void registerForNamedElementEvents(INamedElementEventsSink handler) 
	{
		m_NamedElementSink.addListener( handler, null );
	}

	/**
	 *
	 * Removes a listener from the current list.
	 *
	 * @param cookie[in] The unique cookie used to identify the sink to remove
	 *
	 * @return S_OK
	 * 
	 */
	public void revokeNamedElementSink(INamedElementEventsSink handler) 
	{
		m_NamedElementSink.removeListener( handler );
	}

	/**
	 *
	 * Registers the passed-in event sink with this dispatcher.
	 *
	 * @param sink[in] The actual sink that will recieve notifications
	 * @param cookie[in] The unique identifier to be used when removing the listener
	 *
	 * @return HRESULT
	 * 
	 */
	public void registerForImportEventsSink(IImportEventsSink handler) 
	{
		m_ImportSink.addListener( handler, null );
	}

	/**
	 *
	 * Removes a listener from the current list.
	 *
	 * @param cookie[in] The unique cookie used to identify the sink to remove
	 *
	 * @return S_OK
	 * 
	 */
	public void revokeImportEventsSink(IImportEventsSink handler)
	{
		m_ImportSink.removeListener( handler );
	}

	public void registerForExternalElementEventsSink(IExternalElementEventsSink handler) 
	{
		m_ExtSink.addListener( handler, null );
	}

	public void revokeExternalElementEventsSink(IExternalElementEventsSink handler) 
	{
		m_ExtSink.removeListener( handler );
	}

	public void registerForStereotypeEventsSink(IStereotypeEventsSink handler) 
	{
		m_StereotypeSink.addListener( handler, null );
	}

	public void revokeStereotypeEventsSink(IStereotypeEventsSink handler) 
	{
		m_StereotypeSink.removeListener( handler );

	}

	public void registerForRedefinableElementModifiedEvents(IRedefinableElementModifiedEventsSink handler)
	{
		m_RedefSink.addListener( handler, null );
	}

	public void revokeRedefinableElementModifiedEvents(IRedefinableElementModifiedEventsSink handler) 
	{
		m_RedefSink.removeListener( handler );
	}

	public void registerForPackageEventsSink(IPackageEventsSink handler) 
	{
		m_PackSink.addListener( handler, null );
	}

	public void revokePackageEventsSink(IPackageEventsSink handler)
	{
		m_PackSink.removeListener( handler );
	}

	/**
	 *
	 * Dispatches the OnElementPreModified event.
	 *
	 * @param element[in] The IVersionableElement that is about to be modified
	 * @param payload[in] The EventPayload to include with the event
	 * @param proceed[out] true if the event was fully dispatched, else
	 *                     false if a listener cancelled full dispatch
	 *
	 * @return HRESULT
	 * 
	 */
	public boolean fireElementPreModified(IVersionableElement element, IEventPayload payload) {
		boolean proceed = true;
		boolean isBlocked = getPreventAllEvents();
		IResultCell cell = null;
		boolean fireEvents = true;
		if (isBlocked)
		{
			cell = prepareResultCell(null);
		}
		else
		{
			fireEvents = validateEvent( "ElementPreModified", element);
			if(fireEvents == true)
			{
				cell = prepareResultCell( payload );
			}
		}
		if (fireEvents)
		{
			EventFunctor elePreModFunc = new EventFunctor("org.netbeans.modules.uml.core.metamodel.core.foundation.IElementModifiedEventsSink", "onElementPreModified");

			Object[] parms = new Object[2];
			parms[0] = element;
			parms[1] = cell;
			elePreModFunc.setParameters(parms);

			// Notify the guarenteed list first
			m_GuarenteedElementSink.notifyListenersWithQualifiedProceed( elePreModFunc);
			if (cell != null)
			{
				proceed = cell.canContinue();
			}
			
			if (proceed && !isBlocked)
			{
				m_ElementSink.notifyListenersWithQualifiedProceed( elePreModFunc);
				if (cell != null)
				{
					proceed = cell.canContinue();
				}
			}
		}
		return proceed;
	}

	/**
	 *
	 * Dispatches the OnElementModified event.
	 *
	 * @param element[in] The IVersionableElement that modified
	 * @param payload[in] The EventPayload to include with the event
	 *
	 * @return HRESULT
	 * 
	 */
	public void fireElementModified(IVersionableElement element, IEventPayload payload) 
	{
		boolean isBlocked = getPreventAllEvents();
		IResultCell cell = null;
		boolean proceed = true;
		if (isBlocked)
		{
			cell = prepareResultCell(null);
		}
		else
		{
			proceed = validateEvent("ElementModified", element);
			if(proceed == true)
			{
				cell = prepareResultCell(payload);
			}
		}
		
		if (proceed)
		{
			EventFunctor eleModFunc = new EventFunctor("org.netbeans.modules.uml.core.metamodel.core.foundation.IElementModifiedEventsSink", "onElementModified");
			
			Object[] parms = new Object[2];
			parms[0] = element;
			parms[1] = cell;
			eleModFunc.setParameters(parms);

			// Notify the guarenteed list first
			m_GuarenteedElementSink.notifyListeners( eleModFunc);
			
			if (!isBlocked)
			{
				m_ElementSink.notifyListeners( eleModFunc);
			}
		}
	}

	/**
	 *
	 * Dispatches the OnMetaAttributePreModified event.
	 *
	 * @param payLoad[in] The IMetaAttributeModifiedEventPayload to include with the event
	 * @param proceed[out] true if the event was fully dispatched, else
	 *                     false if a listener cancelled full dispatch
	 *
	 * @return HRESULT
	 * 
	 */
	public boolean fireMetaAttributePreModified(IMetaAttributeModifiedEventPayload payload) {
		boolean proceed = true;
		
		if( validateEvent( "MetaAttributePreModified", payload))
		{
			IResultCell cell = prepareResultCell(payload);
			EventFunctor metaAttrPreModFunc = new EventFunctor("org.netbeans.modules.uml.core.metamodel.core.foundation.IMetaAttributeModifiedEventsSink", "onMetaAttributePreModified");
			
			Object[] parms = new Object[2];
			parms[0] = payload;
			parms[1] = cell;
			metaAttrPreModFunc.setParameters(parms);

		    m_MetaAttrSink.notifyListenersWithQualifiedProceed( metaAttrPreModFunc);
		    if (cell != null)
		    	proceed = cell.canContinue();
		}
		return proceed;
	}

	/**
	 *
	 * Dispatches the OnMetaAttributeModified event.
	 *
	 * @param payload[in] The IMetaAttributeModifiedEventPayload to include with the event
	 *
	 * @return HRESULT
	 * 
	 */
	public void fireMetaAttributeModified(IMetaAttributeModifiedEventPayload payload) {
		
		if( validateEvent( "MetaAttributeModified", payload))
		{
			IResultCell cell = prepareResultCell(payload);
			EventFunctor metaAttrModFunc = new EventFunctor("org.netbeans.modules.uml.core.metamodel.core.foundation.IMetaAttributeModifiedEventsSink", "onMetaAttributeModified");
			
			Object[] parms = new Object[2];
			parms[0] = payload;
			parms[1] = cell;
			metaAttrModFunc.setParameters(parms);

			m_MetaAttrSink.notifyListeners( metaAttrModFunc);
		}
		
	}

	/**
	 *
	 * Fired whenever the documentation field of an element is about to be modified.
	 *
	 * @param element[in] 
	 * @param doc
	 * @param payload[in]
	 * @param proceed [in]
	 *
	 * @result S_OK
	 */
	public boolean fireDocumentationPreModified(IElement element, String doc, IEventPayload payload) {
		boolean proceed = true;

		// Collect the additional parameters for the EventContext to use
		// during the validation pass of the trigger
		Vector <Object > col = new Vector < Object > ();
		col.add(element);
		col.add(doc);
		
		Object var = prepareVariant(col);
		
		if(validateEvent("DocumentationPreModified", var))
		{
			IResultCell cell = prepareResultCell(payload);
			EventFunctor docPreModFunc = new EventFunctor("org.netbeans.modules.uml.core.metamodel.core.foundation.IDocumentationModifiedEventsSink", "onDocumentationPreModified");
			
			Object[] parms = new Object[3];
			parms[0] = element;
			parms[1] = doc;
			parms[2] = cell;
			docPreModFunc.setParameters(parms);
			
			m_DocSink.notifyListenersWithQualifiedProceed(docPreModFunc);
			if (cell != null)
			{
				proceed = cell.canContinue();
			}
		}
		return proceed;
	}

	/**
	 *
	 * Fired whenever an element's documentation field has been modified.
	 *
	 * @param element[in] 
	 * @param payload[in]
	  *
	 * @result S_OK
	 */
	public void fireDocumentationModified(IElement element, IEventPayload payload) {
		
		if(validateEvent("DocumentationModified", element))
		{
			IResultCell cell = prepareResultCell(payload);
			
			EventFunctor docModFunc = new EventFunctor("org.netbeans.modules.uml.core.metamodel.core.foundation.IDocumentationModifiedEventsSink", "onDocumentationModified");
			
			Object[] parms = new Object[2];
			parms[0] = element;
			parms[1] = cell;
			docModFunc.setParameters(parms);
			
			m_DocSink.notifyListeners(docModFunc);
		}
		
	}

	/**
	 *
	 * Fired whenever the documentation field of an element is about to
	 * be modified.
	 *
	 * @param space[in] 
	 * @param elementToAdd[in]
	 * @param payload[in]
	 * @param proceed[in]
	 *
	 * @result S_OK
	 */
	public boolean firePreElementAddedToNamespace(INamespace space, INamedElement elementToAdd, IEventPayload payload) {
		boolean proceed = true;

		// Collect the additional parameters for the EventContext to use
		// during the validation pass of the trigger
		Vector <Object > col = new Vector < Object > ();
		col.add(space);
		col.add(elementToAdd);
		
		Object var = prepareVariant(col);
		
		if(validateEvent("PreElementAddedToNamespace", var))
		{
			IResultCell cell = prepareResultCell(payload);
			EventFunctor preElemAddFunc = new EventFunctor("org.netbeans.modules.uml.core.metamodel.core.foundation.INamespaceModifiedEventsSink", "onPreElementAddedToNamespace");
			
			Object[] parms = new Object[3];
			parms[0] = space;
			parms[1] = elementToAdd;
			parms[2] = cell;
			preElemAddFunc.setParameters(parms);
			
			m_NamespaceSink.notifyListenersWithQualifiedProceed(preElemAddFunc);
			if (cell != null)
			{
				proceed = cell.canContinue();
			}
		}
		return proceed;
	}

	/**
	 *
	 * Fired whenever an element's documentation field has been modified.
	 *
	 * @param space[in] 
	 * @param elementToAdd[in]
	 * @param payload[in]
	 *
	 * @result S_OK
	 */
	public void fireElementAddedToNamespace(INamespace space, INamedElement elementToAdd, IEventPayload payload) {
		// Collect the additional parameters for the EventContext to use
		// during the validation pass of the trigger
		Vector <Object > col = new Vector < Object > ();
		col.add(space);
		col.add(elementToAdd);
		
		Object var = prepareVariant(col);
		
		if(validateEvent("ElementAddedToNamespace", var))
		{
			IResultCell cell = prepareResultCell(payload);
			EventFunctor elemAddFunc = new EventFunctor("org.netbeans.modules.uml.core.metamodel.core.foundation.INamespaceModifiedEventsSink", "onElementAddedToNamespace");
			
			Object[] parms = new Object[3];
			parms[0] = space;
			parms[1] = elementToAdd;
			parms[2] = cell;
			elemAddFunc.setParameters(parms);
			
			m_NamespaceSink.notifyListeners(elemAddFunc);
		}
		
	}

	/**
	 *
	 * Fired whenever the name of the passed-in element is about to change.
	 *
	 * @param element[in] 
	 * @param proposedName
	 * @param payload[in]
	 * @param proceed[in]
	 *
	 * @result S_OK
	 */
	public boolean firePreNameModified(INamedElement element, String proposedName, IEventPayload payload) {
		boolean proceed = true;

		// Collect the additional parameters for the EventContext to use
		// during the validation pass of the trigger
		Vector <Object > col = new Vector < Object > ();
		col.add(element);
		col.add(proposedName);
		
		Object var = prepareVariant(col);
		
		if(validateEvent("PreNameModified", var))
		{
			IResultCell cell = prepareResultCell(payload);
			
			EventFunctor preNameModFunc = new EventFunctor("org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink", "onPreNameModified");
			
			Object[] parms = new Object[3];
			parms[0] = element;
			parms[1] = proposedName;
			parms[2] = cell;
			preNameModFunc.setParameters(parms);
			
			m_NamedElementSink.notifyListenersWithQualifiedProceed(preNameModFunc);
			if (cell != null)
			{
				proceed = cell.canContinue();
			}
		}
		return proceed;
	}

	/**
	 *
	 * Fired whenever the element's name has changed.
	 *
	 * @param element[in] 
	 * @param payload[in]
	 *
	 * @result S_OK
	 */
	public void fireNameModified(INamedElement element, IEventPayload payload) {
		
		if(validateEvent("NameModified", element))
		{
			IResultCell cell = prepareResultCell(payload);
			EventFunctor nameModFunc = new EventFunctor("org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink", "onNameModified");

			Object[] parms = new Object[2];
			parms[0] = element;
			parms[1] = cell;
			nameModFunc.setParameters(parms);
			
			m_NamedElementSink.notifyListeners(nameModFunc);
		}
		
	}

	/**
	 *
	 * Fired whenever the visibility value of the passed-in element
	 * is about to change.
	 *
	 * @param element[in] 
	 * @param proposedValue
	 * @param payload[in]
	 * @param proceed[in]
	 *
	 * @result S_OK
	 */
	public boolean firePreVisibilityModified(INamedElement element, int proposedValue, IEventPayload payload) {
		boolean proceed = true;

		// Collect the additional parameters for the EventContext to use
		// during the validation pass of the trigger
		Vector < Object >  col = new Vector < Object > ();
		col.add(element);
		col.add(Integer.toString(proposedValue));
		
		Object var = prepareVariant(col);
		
		if(validateEvent("PreVisibilityModified", var))
		{
			IResultCell cell = prepareResultCell(payload);
			EventFunctor preVisModFunc = new EventFunctor("org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink", "onPreVisibilityModified");
			
			Object[] parms = new Object[3];
			parms[0] = element;
			parms[1] = new Integer(proposedValue);
			parms[2] = cell;
			preVisModFunc.setParameters(parms);
			
			m_NamedElementSink.notifyListenersWithQualifiedProceed(preVisModFunc);
			if (cell != null)
			{
				proceed = cell.canContinue();
			}
		}
		return proceed;
	}

	/**
	 *
	 * Fired whenever the visibility value of the passed-in element 
	 * has changed.
	 *
	 * @param element[in] 
	 * @param payload[in]
	 *
	 * @result S_OK
	 */
	public void fireVisibilityModified(INamedElement element, IEventPayload payload) {
		if(validateEvent("VisibilityModified", element))
		{
			IResultCell cell = prepareResultCell(payload);
			EventFunctor visModFunc = new EventFunctor("org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink", "onVisibilityModified");
			
			Object[] parms = new Object[2];
			parms[0] = element;
			parms[1] = cell;
			visModFunc.setParameters(parms);
			
			m_NamedElementSink.notifyListeners(visModFunc);
		}
		
	}

	/**
	 *
	 * Fired whenever the name of the passed-in element is about to change.
	 *
	 * @param importingPackage[in] 
	 * @param importedPackage
	 * @param payload[in]
	 * @param proceed[in]
	 *
	 * @result S_OK
	 */
	public boolean firePrePackageImport(IPackage importingPackage, IPackage importedPackage, INamespace owner, IEventPayload payload) {
		boolean proceed = true;

		// Collect the additional parameters for the EventContext to use
		// during the validation pass of the trigger
		Vector < Object >  col = new Vector < Object > ();
		col.add(importingPackage);
		col.add(importedPackage);
		
		Object var = prepareVariant(col);
		
		if(validateEvent("PrePackageImport", var))
		{
			IResultCell cell = prepareResultCell(payload);
			EventFunctor prePackImpFunc = new EventFunctor("org.netbeans.modules.uml.core.metamodel.core.foundation.IImportEventsSink", "onPrePackageImport");
			
			Object[] parms = new Object[4];
			parms[0] = importingPackage;
			parms[1] = importedPackage;
                        parms[2] = owner;
			parms[3] = cell;
			prePackImpFunc.setParameters(parms);
			
			m_ImportSink.notifyListenersWithQualifiedProceed(prePackImpFunc);
			if (cell != null)
			{
				proceed = cell.canContinue();
			}
		}
		return proceed;
	}

	/**
	 *
	 * Fired whenever the element's name has changed.
	 *
	 * @param packImport[in] 
	 * @param payload[in]
	 *
	 * @result S_OK
	 */
	public void firePackageImported(IPackageImport packImport, IEventPayload payload) {
		
		if(validateEvent("PackageImported", packImport))
		{
			IResultCell cell = prepareResultCell(payload);
			
			EventFunctor packImpFunc = new EventFunctor("org.netbeans.modules.uml.core.metamodel.core.foundation.IImportEventsSink", "onPackageImported");
			
			Object[] parms = new Object[2];
			parms[0] = packImport;
			parms[1] = cell;
			packImpFunc.setParameters(parms);
			
			m_ImportSink.notifyListeners(packImpFunc);
		}
		
	}

	/**
	 *
	 * Fired whenever the visibility value of the passed-in element is
	 * about to change..
	 *
	 * @param importingPackage[in] 
	 * @param importedElement[in]
	 * @param payload[in]
	 * @param proceed[in]
	 *
	 * @result S_OK
	 */
	public boolean firePreElementImport(IPackage importingPackage, IElement importedElement, INamespace owner, IEventPayload payload) {
		boolean proceed = true;

		// Collect the additional parameters for the EventContext to use
		// during the validation pass of the trigger
		Vector < Object >  col = new Vector < Object > ();
		col.add(importingPackage);
		col.add(importedElement);
		
		Object var = prepareVariant(col);
		
		if(validateEvent("PreElementImport", var))
		{
			IResultCell cell = prepareResultCell(payload);
			EventFunctor preElemImpFunc = new EventFunctor("org.netbeans.modules.uml.core.metamodel.core.foundation.IImportEventsSink", "onPreElementImport");
			
			Object[] parms = new Object[4];
			parms[0] = importingPackage;
			parms[1] = importedElement;
                        parms[2] = owner;
			parms[3] = cell;
			preElemImpFunc.setParameters(parms);
			
			m_ImportSink.notifyListenersWithQualifiedProceed(preElemImpFunc);
			if (cell != null)
			{
				proceed = cell.canContinue();
			}
		}
		return proceed;
	}

	/**
	 *
	 * Fired whenever the visibility value of the passed-in element
	 * has changed.
	 *
	 * @param elImport[in] 
	 * @param payload[in]
	 *
	 * @result S_OK
	 */
	public void fireElementImported(IElementImport elImport, IEventPayload payload) {
		
		if(validateEvent("ElementImported", elImport))		
		{
			IResultCell cell = prepareResultCell(payload);
			
			EventFunctor elemImpFunc = new EventFunctor("org.netbeans.modules.uml.core.metamodel.core.foundation.IImportEventsSink", "onElementImported");
			
			Object[] parms = new Object[2];
			parms[0] = elImport;
			parms[1] = cell;
			elemImpFunc.setParameters(parms);
			
			m_ImportSink.notifyListeners(elemImpFunc);
		}
		
	}

	public boolean fireExternalElementPreLoaded(String uri, IEventPayload payload) {
		boolean proceed = true;
		
//		if(validateEvent("ExternalElementPreLoaded", uri))
//		{
//			IResultCell cell = prepareResultCell(payload);
//			
//       EventFunctor extElemPreFunc = new EventFunctor("org.netbeans.modules.uml.core.metamodel.core.foundation.IExternalElementEventsSink", "onExternalElementPreLoaded");
//			
//			Object[] parms = new Object[2];
//			parms[0] = uri;
//			parms[1] = cell;
//			extElemPreFunc.setParameters(parms);
//			
//			m_ExtSink.notifyListenersWithQualifiedProceed(extElemPreFunc);
//			if (cell != null)
//			{
//				proceed = cell.canContinue();
//			}
//		}
		return proceed;
	}

	public void fireExternalElementLoaded(IVersionableElement element, IEventPayload payload) {
		
		if(validateEvent("ExternalElementLoaded", element))
		{
			IResultCell cell = prepareResultCell(payload);
			
         EventFunctor extElemFunc = new EventFunctor("org.netbeans.modules.uml.core.metamodel.core.foundation.IExternalElementEventsSink", "onExternalElementLoaded");
			
			Object[] parms = new Object[2];
			parms[0] = element;
			parms[1] = cell;
			extElemFunc.setParameters(parms);
			
			m_ExtSink.notifyListeners(extElemFunc);
		}
		
	}

	public boolean firePreInitialExtraction(String fileName, IVersionableElement element, IEventPayload payload) {
		boolean proceed = true;

		// Collect the additional parameters for the EventContext to use
		// during the validation pass of the trigger
		Vector < Object >  col = new Vector < Object > ();
		col.add(fileName);
		col.add(element);
		
		Object var = prepareVariant(col);
		
		if(validateEvent("PreInitialExtraction", var))
		{
			IResultCell cell = prepareResultCell(payload);
			EventFunctor preInitExtFunc = new EventFunctor("org.netbeans.modules.uml.core.metamodel.core.foundation.IExternalElementEventsSink", "onPreInitialExtraction");
			
			Object[] parms = new Object[3];
			parms[0] = fileName;
			parms[1] = element;
			parms[2] = cell;
			preInitExtFunc.setParameters(parms);
			
			m_ExtSink.notifyListenersWithQualifiedProceed(preInitExtFunc);
			if (cell != null)
			{
				proceed = cell.canContinue();
			}
		}
		return proceed;
	}

	public void fireInitialExtraction(IVersionableElement element, IEventPayload payload) {
		
		if(validateEvent("InitialExtraction", element))
		{
			IResultCell cell = prepareResultCell(payload);
			EventFunctor initExtFunc = new EventFunctor("org.netbeans.modules.uml.core.metamodel.core.foundation.IExternalElementEventsSink", "onInitialExtraction");
			
			Object[] parms = new Object[2];
			parms[0] = element;
			parms[1] = cell;
			initExtFunc.setParameters(parms);
			
			m_ExtSink.notifyListeners(initExtFunc);
		}
		
	}

	public boolean firePreAliasNameModified(INamedElement element, String proposedName, IEventPayload payload) {
		boolean proceed = true;

		// Collect the additional parameters for the EventContext to use
		// during the validation pass of the trigger
		Vector < Object >  col = new Vector < Object > ();
		col.add(element);
		col.add(proposedName);
		
		Object var = prepareVariant(col);
		
		if(validateEvent("PreAliasNameModified", var))
		{
			IResultCell cell = prepareResultCell(payload);
			EventFunctor preAliasModFunc = new EventFunctor("org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink", "onPreAliasNameModified");
			
			Object[] parms = new Object[3];
			parms[0] = element;
			parms[1] = proposedName;
			parms[2] = cell;
			preAliasModFunc.setParameters(parms);
			
			m_NamedElementSink.notifyListenersWithQualifiedProceed(preAliasModFunc);
			if (cell != null)
			{
				proceed = cell.canContinue();
			}
		}
		return proceed;
	}

	public void fireAliasNameModified(INamedElement element, IEventPayload payload) {
		
		if(validateEvent("AliasNameMethod" , element) == true)
		{
			IResultCell cell = prepareResultCell(payload);
			
			EventFunctor aliasModFunc = new EventFunctor("org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink", "onAliasNameModified");
			
			Object[] parms = new Object[2];
			parms[0] = element;
			parms[1] = cell;
			aliasModFunc.setParameters(parms);
			
			m_NamedElementSink.notifyListeners(aliasModFunc);
		}
		
	}

	public boolean firePreStereotypeApplied(Object pStereotype, IElement element, IEventPayload payload) {
		boolean proceed = true;

		// Collect the additional parameters for the EventContext to use
		// during the validation pass of the trigger
		Vector < Object >  col = new Vector < Object > ();
		col.add(pStereotype);
		col.add(element);
		
		Object var = prepareVariant(col);
		
		if(validateEvent("PreStereotypeApplied", var))
		{
			IResultCell cell = prepareResultCell(payload);
			EventFunctor preStereoAppFunc = new EventFunctor("org.netbeans.modules.uml.core.metamodel.core.foundation.IStereotypeEventsSink", "onPreStereotypeApplied");
			
			Object[] parms = new Object[3];
			parms[0] = pStereotype;
			parms[1] = element;
			parms[2] = cell;
			preStereoAppFunc.setParameters(parms);
			
			m_StereotypeSink.notifyListenersWithQualifiedProceed(preStereoAppFunc);
			if (cell != null)
			{
				proceed = cell.canContinue();
			}
		}
		return proceed;
	}

	public void fireStereotypeApplied(Object pStereotype, IElement element, IEventPayload payload) {
		// Collect the additional parameters for the EventContext to use
		// during the validation pass of the trigger
		Vector < Object >  col = new Vector < Object > ();
		col.add(pStereotype);
		col.add(element);
		
		Object var = prepareVariant(col);
		
		if(validateEvent("StereotypeApplied", var))
		{
			IResultCell cell = prepareResultCell(payload);
			EventFunctor stereoAppFunc = new EventFunctor("org.netbeans.modules.uml.core.metamodel.core.foundation.IStereotypeEventsSink", "onStereotypeApplied");
			
			Object[] parms = new Object[3];
			parms[0] = pStereotype;
			parms[1] = element;
			parms[2] = cell;
			stereoAppFunc.setParameters(parms);
			
			m_StereotypeSink.notifyListeners(stereoAppFunc);
		}
		
	}

	public boolean firePreStereotypeDeleted(Object pStereotype, IElement element, IEventPayload payload) {
		boolean proceed = true;

		// Collect the additional parameters for the EventContext to use
		// during the validation pass of the trigger
		Vector < Object >  col = new Vector < Object > ();
		col.add(pStereotype);
		col.add(element);
		
		Object var = prepareVariant(col);
		
		if(validateEvent("PreStereotypeDeleted", var))
		{
			IResultCell cell = prepareResultCell(payload);
			EventFunctor preStereoDelFunc = new EventFunctor("org.netbeans.modules.uml.core.metamodel.core.foundation.IStereotypeEventsSink", "onPreStereotypeDeleted");
			
			Object[] parms = new Object[3];
			parms[0] = pStereotype;
			parms[1] = element;
			parms[2] = cell;
			preStereoDelFunc.setParameters(parms);
			
			m_StereotypeSink.notifyListenersWithQualifiedProceed(preStereoDelFunc);
			if (cell != null)
			{
				proceed = cell.canContinue();
			}
		}
		return proceed;
	}

	public void fireStereotypeDeleted(Object pStereotype, IElement element, IEventPayload payload) {
		// Collect the additional parameters for the EventContext to use
		// during the validation pass of the trigger
		Vector < Object >  col = new Vector < Object > ();
		col.add(pStereotype);
		col.add(element);
		
		Object var = prepareVariant(col);
		
		if(validateEvent("StereotypeDeleted", var))
		{
			IResultCell cell = prepareResultCell(payload);
			EventFunctor stereoDelFunc = new EventFunctor("org.netbeans.modules.uml.core.metamodel.core.foundation.IStereotypeEventsSink", "onStereotypeDeleted");
			
			Object[] parms = new Object[3];
			parms[0] = pStereotype;
			parms[1] = element;
			parms[2] = cell;
			stereoDelFunc.setParameters(parms);
			
			m_StereotypeSink.notifyListeners(stereoDelFunc);
		}
		
	}

	public boolean firePreFinalModified(IRedefinableElement element, boolean proposedValue, IEventPayload payload) {
		boolean proceed = true;

		// Collect the additional parameters for the EventContext to use
		// during the validation pass of the trigger
		Vector < Object >  col = new Vector < Object > ();
		col.add(element);
		col.add(Boolean.valueOf(proposedValue));
		
		Object var = prepareVariant(col);
		
		if(validateEvent("PreFinalModified", var))
		{
			IResultCell cell = prepareResultCell(payload);
			EventFunctor preFinalModFunc = new EventFunctor("org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElementModifiedEventsSink", "onPreFinalModified");
			
			Object[] parms = new Object[3];
			parms[0] = element;
			parms[1] = Boolean.valueOf(proposedValue);
			parms[2] = cell;
			preFinalModFunc.setParameters(parms);
			
			m_RedefSink.notifyListenersWithQualifiedProceed(preFinalModFunc);
			if (cell != null)
			{
				proceed = cell.canContinue();
			}
		}
		return proceed;
	}

	public void fireFinalModified(IRedefinableElement element, IEventPayload payload) {
		
		if(validateEvent("FinalModified", element))
		{
			IResultCell cell = prepareResultCell(payload);
			
			EventFunctor finalModFunc = new EventFunctor("org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElementModifiedEventsSink", "onFinalModified");
			
			Object[] parms = new Object[2];
			parms[0] = element;
			parms[1] = cell;
			finalModFunc.setParameters(parms);
			
			m_RedefSink.notifyListeners(finalModFunc);
		}
		
	}

	public boolean firePreRedefinedElementAdded(IRedefinableElement redefiningElement, IRedefinableElement redefinedElement, IEventPayload payload) {
		boolean proceed = true;

		// Collect the additional parameters for the EventContext to use
		// during the validation pass of the trigger
		Vector < Object >  col = new Vector < Object > ();
		col.add(redefiningElement);
		col.add(redefinedElement);
		
		Object var = prepareVariant(col);
		
		if(validateEvent("PreRedefinedElementAdded", var))
		{
			IResultCell cell = prepareResultCell(payload);
			EventFunctor preRedefAddFunc = new EventFunctor("org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElementModifiedEventsSink", "onPreRedefinedElementAdded");
			
			Object[] parms = new Object[3];
			parms[0] = redefiningElement;
			parms[1] = redefinedElement;
			parms[2] = cell;
			preRedefAddFunc.setParameters(parms);
			
			m_RedefSink.notifyListenersWithQualifiedProceed(preRedefAddFunc);
			if (cell != null)
			{
				proceed = cell.canContinue();
			}
		}
		return proceed;
	}

	public void fireRedefinedElementAdded(IRedefinableElement redefiningElement, IRedefinableElement redefinedElement, IEventPayload payload) {
		// Collect the additional parameters for the EventContext to use
		// during the validation pass of the trigger
		Vector < Object >  col = new Vector < Object > ();
		col.add(redefiningElement);
		col.add(redefinedElement);
		
		Object var = prepareVariant(col);
		
		if(validateEvent("RedefinedElementAdded", var))
		{
			IResultCell cell = prepareResultCell(payload);
			EventFunctor redefAddFunc = new EventFunctor("org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElementModifiedEventsSink", "onRedefinedElementAdded");
			
			Object[] parms = new Object[3];
			parms[0] = redefiningElement;
			parms[1] = redefinedElement;
			parms[2] = cell;
			redefAddFunc.setParameters(parms);
			
			m_RedefSink.notifyListeners(redefAddFunc);
		}
		
	}

	public boolean firePreRedefinedElementRemoved(IRedefinableElement redefiningElement, IRedefinableElement redefinedElement, IEventPayload payload) {
		boolean proceed = true;

		// Collect the additional parameters for the EventContext to use
		// during the validation pass of the trigger
		Vector < Object >  col = new Vector < Object > ();
		col.add(redefiningElement);
		col.add(redefinedElement);
		
		Object var = prepareVariant(col);
		
		if(validateEvent("PreRedefinedElementRemoved", var))
		{
			IResultCell cell = prepareResultCell(payload);
			EventFunctor preRedefRemFunc = new EventFunctor("org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElementModifiedEventsSink", "onPreRedefinedElementRemoved");
			
			Object[] parms = new Object[3];
			parms[0] = redefiningElement;
			parms[1] = redefinedElement;
			parms[2] = cell;
			preRedefRemFunc.setParameters(parms);
			
			m_RedefSink.notifyListenersWithQualifiedProceed(preRedefRemFunc);
			if (cell != null)
			{
				proceed = cell.canContinue();
			}
		}
		return proceed;
	}

	public void fireRedefinedElementRemoved(IRedefinableElement redefiningElement, IRedefinableElement redefinedElement, IEventPayload payload) {
		// Collect the additional parameters for the EventContext to use
		// during the validation pass of the trigger
		Vector < Object >  col = new Vector < Object > ();
		col.add(redefiningElement);
		col.add(redefinedElement);
		
		Object var = prepareVariant(col);
		
		if(validateEvent("RedefinedElementRemoved", var))
		{
			IResultCell cell = prepareResultCell(payload);
			
			EventFunctor redefRemFunc = new EventFunctor("org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElementModifiedEventsSink", "onRedefinedElementRemoved");
			
			Object[] parms = new Object[3];
			parms[0] = redefiningElement;
			parms[1] = redefinedElement;
			parms[2] = cell;
			redefRemFunc.setParameters(parms);
			
			m_RedefSink.notifyListeners(redefRemFunc);
		}
		
	}

	public boolean firePreRedefiningElementAdded(IRedefinableElement redefinedElement, IRedefinableElement redefiningElement, IEventPayload payload) {
		boolean proceed = true;

		// Collect the additional parameters for the EventContext to use
		// during the validation pass of the trigger
		Vector < Object >  col = new Vector < Object > ();
		col.add(redefinedElement);
		col.add(redefiningElement);
		
		Object var = prepareVariant(col);
		
		if(validateEvent("PreRedefiningElementAdded", var))
		{
			IResultCell cell = prepareResultCell(payload);
			EventFunctor preRedefiningAddFunc = new EventFunctor("org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElementModifiedEventsSink", "onPreRedefiningElementAdded");
			
			Object[] parms = new Object[3];
			parms[0] = redefinedElement;
			parms[1] = redefiningElement;
			parms[2] = cell;
			preRedefiningAddFunc.setParameters(parms);
			
			m_RedefSink.notifyListenersWithQualifiedProceed(preRedefiningAddFunc);
			if (cell != null)
			{
				proceed = cell.canContinue();
			}
		}
		return proceed;
	}

	public void fireRedefiningElementAdded(IRedefinableElement redefinedElement, IRedefinableElement redefiningElement, IEventPayload payload) {
		// Collect the additional parameters for the EventContext to use
		// during the validation pass of the trigger
		Vector < Object >  col = new Vector < Object > ();
		col.add(redefinedElement);
		col.add(redefiningElement);
		
		Object var = prepareVariant(col);
		
		if(validateEvent("RedefiningElementAdded", var))
		{
			IResultCell cell = prepareResultCell(payload);
			EventFunctor redefiningAddFunc = new EventFunctor("org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElementModifiedEventsSink", "onRedefiningElementAdded");
			
			Object[] parms = new Object[3];
			parms[0] = redefinedElement;
			parms[1] = redefiningElement;
			parms[2] = cell;
			redefiningAddFunc.setParameters(parms);
			
			m_RedefSink.notifyListeners(redefiningAddFunc);
		}
		
	}

	public boolean firePreRedefiningElementRemoved(IRedefinableElement redefinedElement, IRedefinableElement redefiningElement, IEventPayload payload) {
		boolean proceed = true;

		// Collect the additional parameters for the EventContext to use
		// during the validation pass of the trigger
		Vector < Object >  col = new Vector < Object > ();
		col.add(redefinedElement);
		col.add(redefiningElement);
		
		Object var = prepareVariant(col);
		
		if(validateEvent("PreRedefiningElementRemoved", var))
		{
			IResultCell cell = prepareResultCell(payload);
			EventFunctor preRedefiningRemFunc = new EventFunctor("org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElementModifiedEventsSink", "onPreRedefiningElementRemoved");
			
			Object[] parms = new Object[3];
			parms[0] = redefinedElement;
			parms[1] = redefiningElement;
			parms[2] = cell;
			preRedefiningRemFunc.setParameters(parms);
			
			m_RedefSink.notifyListenersWithQualifiedProceed(preRedefiningRemFunc);
			if (cell != null)
			{
				proceed = cell.canContinue();
			}
		}
		return proceed;
	}

	public void fireRedefiningElementRemoved(IRedefinableElement redefinedElement, IRedefinableElement redefiningElement, IEventPayload payload) {
		// Collect the additional parameters for the EventContext to use
		// during the validation pass of the trigger
		Vector < Object >  col = new Vector < Object > ();
		col.add(redefinedElement);
		col.add(redefiningElement);
		
		Object var = prepareVariant(col);
		
		if(validateEvent("RedefiningElementRemoved", var))
		{
			IResultCell cell = prepareResultCell(payload);
			EventFunctor redefiningRemFunc = new EventFunctor("org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElementModifiedEventsSink", "onRedefiningElementRemoved");
			
			Object[] parms = new Object[3];
			parms[0] = redefinedElement;
			parms[1] = redefiningElement;
			parms[2] = cell;
			redefiningRemFunc.setParameters(parms);
			
			m_RedefSink.notifyListeners(redefiningRemFunc);
		}
		
	}

	public boolean firePreNameCollision(INamedElement element, String proposedName, 
								ETList<INamedElement> collidingElements, IEventPayload payload) 
	{
		boolean proceed = true;

		// Collect the additional parameters for the EventContext to use
		// during the validation pass of the trigger
		Vector < Object >  col = new Vector < Object > ();
		col.add(element);
		col.add(proposedName);
		col.add(collidingElements);
		
		Object var = prepareVariant(col);
		
		if(validateEvent("PreNameCollision", var))
		{
			IResultCell cell = prepareResultCell(payload);
			EventFunctor preNameColFunc = new EventFunctor("org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink", "onPreNameCollision");
			
			Object[] parms = new Object[4];
			parms[0] = element;
			parms[1] = proposedName;
			parms[2] = collidingElements;
			parms[3] = cell;
			preNameColFunc.setParameters(parms);
			
			m_NamedElementSink.notifyListenersWithQualifiedProceed(preNameColFunc);
			if (cell != null)
			{
				proceed = cell.canContinue();
			}
		}
		return proceed;
	}

	public void fireNameCollision(INamedElement element, ETList<INamedElement> collidingElements, IEventPayload payload) {
		// Collect the additional parameters for the EventContext to use
		// during the validation pass of the trigger
		Vector <Object > col = new Vector < Object > ();
		col.add(element);
		col.add(collidingElements);
		
		Object var = prepareVariant(col);
		
		if(validateEvent("NameCollision", var))
		{
			IResultCell cell = prepareResultCell(payload);
			EventFunctor nameColFunc = new EventFunctor("org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink", "onNameCollision");
			
			Object[] parms = new Object[3];
			parms[0] = element;
			parms[1] = collidingElements;
			parms[2] = cell;
			nameColFunc.setParameters(parms);
			
			m_NamedElementSink.notifyListeners(nameColFunc);
		}
		
	}

	public boolean firePreSourceDirModified(IPackage element, String proposedSourceDir, 
								 IEventPayload payload) 
	{
		boolean proceed = true;

		// Collect the additional parameters for the EventContext to use
		// during the validation pass of the trigger
		Vector < Object >  col = new Vector < Object > ();
		col.add(element);
		col.add(proposedSourceDir);
		
		Object var = prepareVariant(col);
		
		if(validateEvent("PreSourceDirModified", var))
		{
			IResultCell cell = prepareResultCell(payload);
			EventFunctor preSrcModFunc = new EventFunctor("org.netbeans.modules.uml.core.metamodel.core.foundation.IPackageEventsSink", "onPreSourceDirModified");
			
			Object[] parms = new Object[3];
			parms[0] = element;
			parms[1] = proposedSourceDir;
			parms[2] = cell;
			preSrcModFunc.setParameters(parms);
			
			m_PackSink.notifyListenersWithQualifiedProceed(preSrcModFunc);
			if (cell != null)
			{
				proceed = cell.canContinue();
			}
		}
		return proceed;
	}

	public void fireSourceDirModified(IPackage element, IEventPayload payload) 
	{
		if(validateEvent("SourceDirModified", element))
		{
			IResultCell cell = prepareResultCell(payload);
			EventFunctor srcModFunc = new EventFunctor("org.netbeans.modules.uml.core.metamodel.core.foundation.IPackageEventsSink", "onSourceDirModified");
			
			Object[] parms = new Object[2];
			parms[0] = element;
			parms[1] = cell;
			srcModFunc.setParameters(parms);
			
			m_PackSink.notifyListeners(srcModFunc);
		}
		
	}

	/**
	 * IEventDispatcher override.  Returns the number of registered sinks
	 */
	public int getNumRegisteredSinks()
	{
		int retVal = 0;
	   try
	   {
		  retVal = m_ElementSink.getNumListeners() +
				  m_MetaAttrSink.getNumListeners() +
				  m_DocSink.getNumListeners() +
				  m_NamespaceSink.getNumListeners() +
				  m_NamedElementSink.getNumListeners() +
				  m_ImportSink.getNumListeners() +
				  m_ExtSink.getNumListeners() +
				  m_StereotypeSink.getNumListeners() +
				  m_RedefSink.getNumListeners();
	   }
	   catch ( Exception err )
	   {
	   }
	   return retVal;
	}

}


