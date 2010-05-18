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



