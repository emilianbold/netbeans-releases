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

/*
 * EnsureEventState.java
 *
 * Created on July 19, 2004, 12:06 PM
 */

package org.netbeans.modules.uml.core.eventframework;

import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.support.Debug;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;

/**
 * EnsureEventState's whole purpose in life is to ensure that events
 * either fire, or do not fire, dependent on the flag used to instantiate
 * the object.
 *
 * @author  Trey Spiva
 * @see IEventDispatcher
 * @see IEventDispatchController
 */
public class EnsureEventState
{
   IEventDispatcher         m_Dispatch = null;
   IEventDispatchController m_Controller = null;
   boolean                  m_OrigFlag = false;
   boolean                  m_PlugEvents = false;
   
   /**
    *EnsureEventState constructor. Calls the setPreventAllEvents( true )
    * on the passed in IEventDispatcher.
    */
   public EnsureEventState(boolean plugEvents, IEventDispatcher dispatcher)
   {
      m_Dispatch = dispatcher;
      m_PlugEvents = plugEvents;
      
      if(m_Dispatch != null)
      {
         m_OrigFlag = m_Dispatch.getPreventAllEvents();
         if(m_OrigFlag != m_PlugEvents)
         {
            m_Dispatch.setPreventAllEvents(m_PlugEvents);
         }
      }
   }
   
   /**
    *
    * Prevents all event from going out on any and every dispatcher that the
    * passed in controller manages, IF plugEvents is set to true. Otherwise,
    * this makes sure that the events are NOT plugged.
    * If the passed in controller is 0, then
    * the CoreProduct is retrieved, and the EventDispatchController on that
    * product is used.
    *
    */
   public EnsureEventState( boolean plugEvents)
   {
      initializeWithController(plugEvents, null);
   }
   
   /**
    *
    * Prevents all event from going out on any and every dispatcher that the
    * passed in controller manages, IF plugEvents is set to true. Otherwise,
    * this makes sure that the events are NOT plugged.
    * If the passed in controller is 0, then
    * the CoreProduct is retrieved, and the EventDispatchController on that
    * product is used.
    *
    * @param controller[in] The controller to prevent / enable events on. If null, the
    *                       controller on the CoreProduct is used.
    *
    */
   public EnsureEventState( boolean plugEvents, 
                            IEventDispatchController controller )
   {
      initializeWithController(plugEvents, controller);       
   }
   
   /**
    * Initializes the Event state when a controler is involved.
    */
   protected void initializeWithController(boolean plugEvents, 
                                           IEventDispatchController controller )
   {
      m_PlugEvents = plugEvents;
      
      ICoreProduct coreProd = null;      
      if( controller != null)
      {
         m_Controller = controller;
      }
      else
      {
         coreProd = ProductRetriever.retrieveProduct();
         Debug.assertNotNull(coreProd);
         if( coreProd != null)
         {
            m_Controller = coreProd.getEventDispatchController();
         }
      }
      
      if( m_Controller != null)
      {
         m_OrigFlag = m_Controller.getPreventAllEvents();
         
         if( m_OrigFlag != m_PlugEvents )
         {
            m_Controller.setPreventAllEvents(m_PlugEvents);
         }
      }
   }
   
   /**
    * EnsureEventState desctructor. Calls the put_PreventAllEvents( false)
    * on the encapuslated IEventDispatcher.
    */
   
   public void unPlug()
   {
      if( m_Dispatch != null)
      {
         m_Dispatch.setPreventAllEvents( m_OrigFlag );
      }
      
      if( m_Controller != null)
      {
         m_Controller.setPreventAllEvents( m_OrigFlag );
      }
   }
   
}
