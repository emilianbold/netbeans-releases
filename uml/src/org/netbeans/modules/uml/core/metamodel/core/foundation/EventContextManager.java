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

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.eventframework.EventContext;
import org.netbeans.modules.uml.core.eventframework.EventDispatchNameKeeper;
import org.netbeans.modules.uml.core.eventframework.EventDispatchRetriever;
import org.netbeans.modules.uml.core.eventframework.IEventContext;
import org.netbeans.modules.uml.core.eventframework.IEventDispatchController;
import org.netbeans.modules.uml.core.eventframework.IEventDispatcher;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;

/**
 * Used to push various EventContexts onto dispatchers or controllers with various
 * properties.
 */
public class EventContextManager {

	/**
	 * 
	 */
	public EventContextManager() {
		super();
	}

	/**
	 *
	 * Creates a new EventContext that will be propogated to all 
	 * EventDispatchers on the Product's EventDispatchController.
	 * This Context will prevent events from firing when initiated 
	 * from the passed in element.
	 *
	 * @param owner[in]   If not null, and element supports the ITransitionElement interface,
	 *                    this will be put on the FutureOwner property of the ITransitionElement
	 *                    interface
	 * @param element[in] The feature to create the context and 
	 *                    EventFilter with.
	 * @param controller[in] Default is 0. If 0, the EventDispatchController off the current product
	 *                       is automatically retrieved.
	 *
	 * @return HRESULT
	 *
	 */

	public void establishVersionableElementContext( IElement owner, 
												 IVersionableElement element, 
												 IEventDispatchController controller )
	{
		if (controller == null)
		{
			controller = getController();
		}
		
		if (controller != null)
		{
			IVersionableElementEventFilter filter = new VersionableElementEventFilter();
			IEventContext context = new EventContext();
			
			filter.setVersionableElement(element);
			context.setName("CreateTransitionContext");
			context.setFilter(filter);
			
			controller.pushEventContext3(context);
			
			// Let's see if this type supports the ITransitionElement interface.
			// If it does, we can put ourselves on that interface to help resolve
			// types
			if (owner != null && element instanceof ITransitionElement)
			{
				ITransitionElement trans = (ITransitionElement)element;
				trans.setFutureOwner(owner);
			}
		}
	}

	/**
	 *
	 * Retrieves the controller on the current product.
	 *
	 * @param cont[out] The dispatch controller
	 *
	 * @return HRESULT
	 *
	 */
	private IEventDispatchController getController() {
		IEventDispatchController cont = null;
		ICoreProduct prod = ProductRetriever.retrieveProduct();
		if (prod != null)
		{
			cont = prod.getEventDispatchController();
		}
		return cont;
	}

	/**
	 *
	 * This method will create a new event context, giving it a name that is passed in.
	 * It will then check the current dirty state of the passed in element, placing the
	 * flag on the EventContext's Data() field. 
	 *
	 * The idea here is that there are cases where certain element modifications physically
	 * change the structure of the core XML element, but logically, we want the action
	 * to appear as if nothing happened. No elements, become dirty, version control is
	 * not affected, etc.
	 *
	 * @param curElement[in]   The element being manipulated. Can be 0.
	 * @param dispatchName[in] The name of the dispatch interface to retrieve off the controller.
	 * @param contextName[in]  The name to use for the EventContext. This will not be used
	 *                         to create the context from the event framework. A base IEventContext
	 *                         will be created and this name will be set.
	 *
	 * @return HRESULT
	 * @note    Typically, this call is followed by an instansiation of the EventState
	 *          object. See ElementImpl::AddPresentationElement() for an example of
	 *          usage.
	 *
	 */
	public ETPairT < IEventContext, IEventDispatcher > getNoEffectContext(IElement curEle, 
							                                                    String dispatchName, 
                                                                         String contextName) 
   {
      IEventContext retCon = new EventContext();
		retCon.setName(contextName);
		if (curEle != null)
		{
			boolean isDirty = false;
			isDirty = curEle.isDirty();

			// Put the current dirty state on this element. In this way,
			// we can clear the dirty state to keep the "logical" state
			// of this element consistent. For version control purposes,
			// we are not going to check out the element when adding a presentation
			// element to it, for example. In that same regard, the act of adding that
			// presentation element should not case this element to be dirty
			retCon.setData(Boolean.valueOf(isDirty));
		}
		EventDispatchRetriever ret = EventDispatchRetriever.instance();
		IEventDispatcher disp = (IEventDispatcher)ret.getDispatcher(dispatchName);
		return new ETPairT < IEventContext, IEventDispatcher >(retCon, disp);
	}
	
	/**
	 *
	 * Pops any event context who has a filter on it with an
	 * id that matches the XMI ID of the element passed into this
	 * method
	 *
	 * @param element[in] The element to match against.
	 *
	 * @return HRESULT
	 *
	 */
	public void revokeEventContext( IVersionableElement element, IEventDispatchController controller )
	{
		IEventDispatchController cont = controller;
		if (cont == null)
		{
			cont = getController(); 
		}
		
		if (cont != null && element != null)
		{
			String xmiid = element.getXMIID();
			cont.removeEventContextByFilterID(xmiid);
		}
	}

	/**
	 *
	 * Determines whether or not the current EventContext is a
	 * "PresentationAdded" or "DefaultImports" context.
	 *
	 * @return  -  true if the current context is a "PresentationAdded" or
	 *             "DefaultImports"
	 *             context else
	 *          -  false if not
	 */
	public boolean isNoEffectModification()
	{
		boolean retVal = false;
		EventDispatchRetriever ret = EventDispatchRetriever.instance();
		Object obj = ret.getDispatcher(EventDispatchNameKeeper.modifiedName());
		IElementChangeEventDispatcher disp = null;
		if (obj != null && obj instanceof IElementChangeEventDispatcher)
		{
			disp = (IElementChangeEventDispatcher)obj;
		}
		if (disp != null)
		{
			IEventContext curContext = disp.getCurrentContext();
			if (curContext != null)
			{
            retVal = isContextNoEffectModification(curContext);
			}
		}
		return retVal;
	}
	
   /**
	 * Retreives the context that is a no effect context.
    *
	 * @return  If <code>null</code> is returned then there are not "no effect
    *          modification" contexts.
	 */
	public IEventContext getNoEffectContext()
	{
		IEventContext retVal = null;
		EventDispatchRetriever ret = EventDispatchRetriever.instance();
		Object obj = ret.getDispatcher(EventDispatchNameKeeper.modifiedName());
      
		IElementChangeEventDispatcher disp = null;      
		if (obj != null && obj instanceof IElementChangeEventDispatcher)
		{
			disp = (IElementChangeEventDispatcher)obj;
		}
      
		if (disp != null)
		{
			IEventContext curContext = disp.getCurrentContext();
			if (curContext != null)
			{
            if(isContextNoEffectModification(curContext) == true)
            {
               retVal = curContext;
            }
				
			}
		}
		return retVal;
	}
   
   protected boolean isContextNoEffectModification(IEventContext context)
   {
      boolean retVal = false;
      
      if(context != null)
      {
         String name = context.getName();
         if (name != null && (name.equals("PresentationAdded") ||
                              name.equals("DefaultImports") ||
                              name.equals("PresentationRemoved") ||
                              name.equals("DefaultMultiplicityAdded") ||
                              name.equals("DefaultAdded") ||
                              name.equals("PresentationReferenceAdded") ||
                              name.equals("PresentationReferenceRemoved")
                        ))
         {
            retVal = true;
         }
      }
      return retVal;
   }
}
