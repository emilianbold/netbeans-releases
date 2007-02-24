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


package org.netbeans.modules.uml.core.metamodel.core.foundation;

import java.util.Vector;

import org.netbeans.modules.uml.core.eventframework.EventDispatchNameKeeper;
import org.netbeans.modules.uml.core.eventframework.EventDispatchRetriever;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author sumitabhk
 *
 */
public class ElementDisposal implements IElementDisposal{

	//collection of IVersionableElement to be disposed.
	private ETList< IVersionableElement > m_Elements = 
									new ETArrayList< IVersionableElement >();

	/**
	 * 
	 */
	public ElementDisposal() {
		super();
	}

	/**
	 *
	 * Puts the passed-in element on our internal collection of elements
	 * that need to be disposed of.
	 *
	 * @param element[in] The element to dispose of
	 *
	 * @return HRESULT
	 *
	 */
	public long queueForDisposal(IVersionableElement element) 
	{
		m_Elements.add(element);
		return 0;
	}

	/**
	 *
	 * Disposes all elements in the disposal queue. 
	 *
	 * @return HRESULT
	 *
	 */
	public long disposeElements() {
		if (m_Elements != null )
		{
			int count = m_Elements.size();
			if (count > 0)
			{
				EventDispatchRetriever ret = EventDispatchRetriever.instance();
				IElementLifeTimeEventDispatcher disp = (IElementLifeTimeEventDispatcher)ret.getDispatcher(EventDispatchNameKeeper.lifeTime());
				ETList<IVersionableElement> elems = getElements();
				boolean proceed = true;
				if (disp != null)
				{
					IEventPayload payload = disp.createPayload("PreDisposeElements");
					proceed = disp.firePreDisposeElements(elems, payload);
				}
				if (proceed && disp != null)
				{
					IEventPayload payload = disp.createPayload("DisposeElements");
					disp.fireDisposedElements(elems, payload);
				}
				m_Elements.clear();
			}
		}
		return 0;
	}

	/**
	 * @return
	 */
	private ETList<IVersionableElement> getElements() 
	{
		return m_Elements;
	}

	/**
	 *
	 * Retrieves the collection of elements that still need to be disposed of
	 *
	 * @param pVal[out] The elements currently on the queue
	 *
	 * @return HRESULT
	 *
	 */
	public ETList<IVersionableElement> getToBeDisposed() 
	{
		return getElements();
	}

}



