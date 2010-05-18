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


package org.netbeans.modules.uml.core.eventframework;

import org.netbeans.modules.uml.common.ETSystem;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Vector;
import java.util.WeakHashMap;
import org.openide.ErrorManager;

/**
 * EventManager is a simple base class used to manage
 * the collection of lister interfaces. The type used to instanciate
 * this class should be the event listener interface, such as IWorkspaceEventsSink.
 */
public class EventManager<Element>
{
        private Vector<WeakReference<Object>> m_listeners = new Vector<WeakReference<Object>>();
        private WeakHashMap<Object, Object> hashedListeners = new WeakHashMap<Object, Object>();
	private IEventDispatcher m_dispatcher = null;
	private IValidationSink m_Validator = null;


	public void addListener(Element obj, IValidationSink<Element> sink)
	{
            // conover - duplicate listeners were being registered, and this
            // will prevent that from happening
	    //if (!m_listeners.contains(obj))
            if (! contains(obj))
            {
		m_listeners.addElement(new WeakReference(obj));
		hashedListeners.put(obj, new Object());
		
                if (sink != null)
                    setValidator(sink);
            }
            
            // log instances when there is an attempt to register
            // a duplicate listener
            else
                ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, 
                    "Duplicate listener registration was attempted with " 
                    + obj.toString() ); // NOI18N
        }
	
	public void removeListener(Element sink)
	{
	    if (sink != null) {
		Iterator<WeakReference<Object>> iter = m_listeners.iterator();
		if (iter != null) 
		{
		    while(iter.hasNext()) 
		    {
			WeakReference<Object> ref = iter.next(); 
			if (ref != null) 
			{
			    Object elem = ref.get();
			    if (elem != null && elem.equals(sink)) 
			    {
				iter.remove();
			    } 
			}
		    }
		}
		hashedListeners.remove(sink);	    
	    }
	}
	
        private boolean contains(Element obj) 
        {
	    if (obj != null) 
	    {
		if (hashedListeners.get(obj) != null) {
		    return true;
		}
	    }
	    return false;
	}

        private void cleanUp() 
        {
	    Iterator<WeakReference<Object>> iter = m_listeners.iterator();
	    if (iter != null) 
	    {
		while(iter.hasNext()) 
		{
		    WeakReference<Object> ref = iter.next(); 
		    if (ref == null) 
		    {
			iter.remove();
		    } 
		    else 
		    {
			Object elem = ref.get();
			if (elem == null) 
			{
			    iter.remove();
			}
		    }
		}
	    }
	}
    

	public void setDispatcher(IEventDispatcher disp)
	{
		m_dispatcher = disp;
	}
	
	public boolean anyListeners()
	{
		return m_listeners.size() > 0;
	}
	
	public int getNumListeners()
	{
		return m_listeners.size();
	}
	
	public void notifyListeners(EventFunctor func)
	{
		if (func == null)
			return;

		cleanUp();
		//
		// Switched the loop to go backwards because in onCoreProductPreQuit
		// the listeners could be removed in the execute and that was causing
		// the array to be messed up.
		// In c++, this function takes a boolean as a parameter, but it is only
		// set to true in WorkspaceEventDispatcherImpl::FireWSProjectPreSave
		// so I am not sure how this isn't causing problems in c++ as well
		for (int i = m_listeners.size() - 1; i >= 0 ; i--)
		{
			try
			{
				func.execute(m_listeners.elementAt(i).get());
			}
			catch (Exception e)
			{
				ETSystem.out.println("Error in notifyListeners");
			}
		}
	}
	
	public void notifyListeners(EventFunctor func, Object[] params)
	{
		if (func == null)
			return;
		
		cleanUp();
		// See above for comment
		for (int i = m_listeners.size() - 1; i >= 0 ; i--)
		{
			try
			{
				func.execute(params, m_listeners.elementAt(i).get());
			}
			
			catch (Exception e)
			{
				ETSystem.out.println("Error in notifyListeners");
			}
		}
	}
	
	public void notifyListenersWithQualifiedProceed(EventFunctor func)
	{
		cleanUp();
		// cvc - 6269224
		// loop in reverse because an element might get removed outside of
		// this instance and cause some listeners to be skipped
		// see other notifier methods in this class for exact same comments
		// as this was a problem in the past but this loop wasn't touched
		for (int i = m_listeners.size()-1; i > -1 ; i--)
		{
			try
			{
				func.execute(m_listeners.elementAt(i).get());
                                //check the result and if required call dispatchCancelEvent
				if (!func.isResultOK())
				{
                                    // We should notify every the listeners
                                    // that have already accepted the change.
                                    // However, since I do not have a use case
                                    // to use this feature, and I do not have
                                    // a test case to make sure I am not about
                                    // to mess anything up, I am going to 
                                    // comment this code out.  
                                    // 
                                    // If we ever need this feature simply 
                                    // uncomment the following line.
//                                    dispatchCancelledEvent( i, func );
					break;
				}
			}
			
			catch (Exception e)
			{
				ETSystem.out.println(
					"Exception in notifyListenersWithQualifiedProceed"); // NOI18N
			}
		}
	}
	/**
	 * @return
	 */
	public IValidationSink getValidator()
	{
		return m_Validator;
	}
	
	/**
	 * @param sink
	 */
	public void setValidator(IValidationSink sink)
	{
		m_Validator = sink;
	}
}
