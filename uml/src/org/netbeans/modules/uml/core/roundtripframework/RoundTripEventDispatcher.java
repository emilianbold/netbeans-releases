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
 * File       : RoundTripEventDispatcher.java
 * Created on : Nov 5, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.roundtripframework;

import org.netbeans.modules.uml.core.eventframework.EventDispatcher;
import org.netbeans.modules.uml.core.eventframework.EventFunctor;
import org.netbeans.modules.uml.core.eventframework.EventManager;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.eventframework.IValidationSink;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;

/**
 * @author Aztec
 */
public class RoundTripEventDispatcher
    extends EventDispatcher
    implements IRoundTripEventDispatcher
{

    private EventManager< IRoundTripAttributeEventsSink >     m_AttrSink =
				new EventManager< IRoundTripAttributeEventsSink >();
     private EventManager< IRoundTripEnumLiteralEventsSink >     m_EnumLiteralSink =
				new EventManager< IRoundTripEnumLiteralEventsSink >();
    private EventManager< IRoundTripOperationEventsSink >     m_OperSink =
				new EventManager< IRoundTripOperationEventsSink >();
    private EventManager< IRoundTripClassEventsSink >         m_ClassSink =
				new EventManager< IRoundTripClassEventsSink >();
    private EventManager< IRoundTripEnumEventsSink >         m_EnumSink =
				new EventManager< IRoundTripEnumEventsSink >();
    private EventManager< IRoundTripPackageEventsSink >       m_PackSink =
				new EventManager< IRoundTripPackageEventsSink >();
    private EventManager< IRoundTripRelationEventsSink >      m_RelSink =
				new EventManager< IRoundTripRelationEventsSink >();
    private EventManager< IRequestProcessorInitEventsSink >   m_InitSink =
				new EventManager< IRequestProcessorInitEventsSink >();
    private ValidationMediator                                m_Mediator =
                new ValidationMediator();

    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripEventDispatcher#fireAttributeChangeRequest(org.netbeans.modules.uml.core.roundtripframework.IChangeRequest, org.netbeans.modules.uml.core.eventframework.IEventPayload)
     */
    public void fireAttributeChangeRequest(
        IChangeRequest req,
        IEventPayload payload)
    {
        if( req == null) return;
        
        m_Mediator.changeRequest( req );

        if( validateEvent("AttributeChangeRequest", payload))
        {
            IResultCell cell = prepareResultCell(payload);
            
            if(cell != null)
            {
                EventFunctor func 
                    = new EventFunctor(
                        "org.netbeans.modules.uml.core.roundtripframework.IRoundTripAttributeEventsSink",
                        "onAttributeChangeRequest");
                func.setParameters(new Object[]{req,cell});
                m_AttrSink.notifyListeners( func );
            }
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripEventDispatcher#fireEnumLiteralChangeRequest(org.netbeans.modules.uml.core.roundtripframework.IChangeRequest, org.netbeans.modules.uml.core.eventframework.IEventPayload)
     */
    public void fireEnumLiteralChangeRequest(
        IChangeRequest req,
        IEventPayload payload)
    {
        if( req == null) return;
        
        m_Mediator.changeRequest( req );

        if( validateEvent("EnumLiteralChangeRequest", payload))
        {
            IResultCell cell = prepareResultCell(payload);
            
            if(cell != null)
            {
                EventFunctor func 
                    = new EventFunctor(
                        "org.netbeans.modules.uml.core.roundtripframework.IRoundTripEnumLiteralEventsSink",
                        "onEnumLiteralChangeRequest");
                func.setParameters(new Object[]{req,cell});
                m_EnumLiteralSink.notifyListeners( func );
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripEventDispatcher#fireClassChangeRequest(org.netbeans.modules.uml.core.roundtripframework.IChangeRequest, org.netbeans.modules.uml.core.eventframework.IEventPayload)
     */
    public void fireClassChangeRequest(
        IChangeRequest req,
        IEventPayload payload)
    {
        if( req == null) return;
        
        m_Mediator.changeRequest( req );

        
        if( validateEvent("ClassChangeRequest", payload))
        {
            IResultCell cell = prepareResultCell(payload);
            
            if(cell != null)
            {
                EventFunctor func 
                    = new EventFunctor(
                        "org.netbeans.modules.uml.core.roundtripframework.IRoundTripClassEventsSink",
                        "onClassChangeRequest");
                func.setParameters(new Object[]{req,cell});
                m_ClassSink.notifyListeners( func );
            }
        }

    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripEventDispatcher#fireEnumerationChangeRequest(org.netbeans.modules.uml.core.roundtripframework.IChangeRequest, org.netbeans.modules.uml.core.eventframework.IEventPayload)
     */
    public void fireEnumerationChangeRequest(
        IChangeRequest req,
        IEventPayload payload)
    {
        if( req == null) return;
        
        m_Mediator.changeRequest( req );

        
        if( validateEvent("EnumerationChangeRequest", payload))
        {
            IResultCell cell = prepareResultCell(payload);
            
            if(cell != null)
            {
                EventFunctor func 
                    = new EventFunctor(
                        "org.netbeans.modules.uml.core.roundtripframework.IRoundTripEnumEventsSink",
                        "onEnumChangeRequest");
                func.setParameters(new Object[]{req,cell});
                m_EnumSink.notifyListeners( func );
            }
        }

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripEventDispatcher#fireInitialized(org.netbeans.modules.uml.core.roundtripframework.IRequestProcessor, org.netbeans.modules.uml.core.eventframework.IEventPayload)
     */
    public void fireInitialized(IRequestProcessor proc, IEventPayload payload)
    {
        if( proc == null) return;
        
        if( validateEvent("Initialized", payload))
        {
            IResultCell cell = prepareResultCell(payload);
            
            if(cell != null)
            {
                EventFunctor func 
                    = new EventFunctor(
                        "org.netbeans.modules.uml.core.roundtripframework.IRequestProcessorInitEventsSink",
                        "onInitialized");
                func.setParameters(new Object[]{proc,cell});
                m_InitSink.notifyListeners( func );
            }
        }

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripEventDispatcher#fireOperationChangeRequest(org.netbeans.modules.uml.core.roundtripframework.IChangeRequest, org.netbeans.modules.uml.core.eventframework.IEventPayload)
     */
    public void fireOperationChangeRequest(
        IChangeRequest req,
        IEventPayload payload)
    {
        if( req == null) return;
        
        m_Mediator.changeRequest( req );

        if( validateEvent("OperationChangeRequest", payload))
        {
            IResultCell cell = prepareResultCell(payload);
            
            if(cell != null)
            {
                EventFunctor func 
                    = new EventFunctor(
                        "org.netbeans.modules.uml.core.roundtripframework.IRoundTripOperationEventsSink",
                        "onOperationChangeRequest");
                func.setParameters(new Object[]{req,cell});
                m_OperSink.notifyListeners( func );
            }
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripEventDispatcher#firePackageChangeRequest(org.netbeans.modules.uml.core.roundtripframework.IChangeRequest, org.netbeans.modules.uml.core.eventframework.IEventPayload)
     */
    public void firePackageChangeRequest(
        IChangeRequest req,
        IEventPayload payload)
    {
        if( req == null) return;
        
        m_Mediator.changeRequest( req );

        if( validateEvent("PackageChangeRequest", payload))
        {
            IResultCell cell = prepareResultCell(payload);
            
            if(cell != null)
            {
                EventFunctor func 
                    = new EventFunctor(
                        "org.netbeans.modules.uml.core.roundtripframework.IRoundTripPackageEventsSink",
                        "onPackageChangeRequest");
                func.setParameters(new Object[]{req,cell});
                m_PackSink.notifyListeners( func );
            }
        }

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripEventDispatcher#firePreAttributeChangeRequest(org.netbeans.modules.uml.core.roundtripframework.IChangeRequest, org.netbeans.modules.uml.core.eventframework.IEventPayload)
     */
    public boolean firePreAttributeChangeRequest(
        IChangeRequest req,
        IEventPayload payload)
    {
        if( req == null) return false;
        
        m_Mediator.changeRequest( req );

        if( validateEvent("PreAttributeChangeRequest", payload))
        {
            IResultCell cell = prepareResultCell(payload);
            
            if(cell != null)
            {
                EventFunctor func 
                    = new EventFunctor(
                        "org.netbeans.modules.uml.core.roundtripframework.IRoundTripAttributeEventsSink",
                        "onPreAttributeChangeRequest");
                func.setParameters(new Object[]{req,cell});
                m_AttrSink.notifyListenersWithQualifiedProceed( func );
            }
            
            return cell.canContinue();
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripEventDispatcher#firePreEnumLiteralChangeRequest(org.netbeans.modules.uml.core.roundtripframework.IChangeRequest, org.netbeans.modules.uml.core.eventframework.IEventPayload)
     */
    public boolean firePreEnumLiteralChangeRequest(
        IChangeRequest req,
        IEventPayload payload)
    {
        if( req == null) return false;
        
        m_Mediator.changeRequest( req );

        if( validateEvent("PreEnumLiteralChangeRequest", payload))
        {
            IResultCell cell = prepareResultCell(payload);
            
            if(cell != null)
            {
                EventFunctor func 
                    = new EventFunctor(
                        "org.netbeans.modules.uml.core.roundtripframework.IRoundTripEnumLiteralEventsSink",
                        "onPreEnumLiteralChangeRequest");
                func.setParameters(new Object[]{req,cell});
                m_EnumLiteralSink.notifyListenersWithQualifiedProceed( func );
            }
            
            return cell.canContinue();
        }
        return false;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripEventDispatcher#firePreClassChangeRequest(org.netbeans.modules.uml.core.roundtripframework.IChangeRequest, org.netbeans.modules.uml.core.eventframework.IEventPayload)
     */
    public boolean firePreClassChangeRequest(
        IChangeRequest req,
        IEventPayload payload)
    {
        if( req == null) return false;
        
        m_Mediator.changeRequest( req );

        if( validateEvent("PreClassChangeRequest", payload))
        {
            IResultCell cell = prepareResultCell(payload);
            
            if(cell != null)
            {
                EventFunctor func 
                    = new EventFunctor(
                        "org.netbeans.modules.uml.core.roundtripframework.IRoundTripClassEventsSink",
                        "onPreClassChangeRequest");
                func.setParameters(new Object[]{req,cell});
                m_ClassSink.notifyListenersWithQualifiedProceed( func );
            }
            
            return cell.canContinue();
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripEventDispatcher#firePreEnumerationChangeRequest(org.netbeans.modules.uml.core.roundtripframework.IChangeRequest, org.netbeans.modules.uml.core.eventframework.IEventPayload)
     */
    public boolean firePreEnumerationChangeRequest(
        IChangeRequest req,
        IEventPayload payload)
    {
        if( req == null) return false;
        
        m_Mediator.changeRequest( req );

        if( validateEvent("PreEnumerationChangeRequest", payload))
        {
            IResultCell cell = prepareResultCell(payload);
            
            if(cell != null)
            {
                EventFunctor func 
                    = new EventFunctor(
                        "org.netbeans.modules.uml.core.roundtripframework.IRoundTripEnumEventsSink",
                        "onPreEnumChangeRequest");
                func.setParameters(new Object[]{req,cell});
                m_EnumSink.notifyListenersWithQualifiedProceed( func );
            }
            
            return cell.canContinue();
        }
        return false;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripEventDispatcher#firePreInitialized(java.lang.String, org.netbeans.modules.uml.core.eventframework.IEventPayload)
     */
    public boolean firePreInitialized(String proc, IEventPayload payload)
    {
      
        if( validateEvent("PreInitialized", payload))
        {
            IResultCell cell = prepareResultCell(payload);
            
            if(cell != null)
            {
                EventFunctor func 
                    = new EventFunctor(
                        "org.netbeans.modules.uml.core.roundtripframework.IRequestProcessorInitEventsSink",
                        "onPreInitialized");
                func.setParameters(new Object[]{proc,cell});
                m_InitSink.notifyListenersWithQualifiedProceed( func );
                
                return cell.canContinue();
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripEventDispatcher#firePreOperationChangeRequest(org.netbeans.modules.uml.core.roundtripframework.IChangeRequest, org.netbeans.modules.uml.core.eventframework.IEventPayload)
     */
    public boolean firePreOperationChangeRequest(
        IChangeRequest req,
        IEventPayload payload)
    {
        if( req == null) return false;
        
        m_Mediator.changeRequest( req );
        
        if( validateEvent("PreOperationChangeRequest", payload))
        {
            IResultCell cell = prepareResultCell(payload);
            
            if(cell != null)
            {
                EventFunctor func 
                    = new EventFunctor(
                        "org.netbeans.modules.uml.core.roundtripframework.IRoundTripOperationEventsSink",
                        "onPreOperationChangeRequest");
                func.setParameters(new Object[]{req,cell});
                m_OperSink.notifyListenersWithQualifiedProceed( func );
                
                return cell.canContinue();
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripEventDispatcher#firePrePackageChangeRequest(org.netbeans.modules.uml.core.roundtripframework.IChangeRequest, org.netbeans.modules.uml.core.eventframework.IEventPayload)
     */
    public boolean firePrePackageChangeRequest(
        IChangeRequest req,
        IEventPayload payload)
    {
        if( req == null) return false;
        
        m_Mediator.changeRequest( req );

        if( validateEvent("PrePackageChangeRequest", payload))
        {
            IResultCell cell = prepareResultCell(payload);
            
            if(cell != null)
            {
                EventFunctor func 
                    = new EventFunctor(
                        "org.netbeans.modules.uml.core.roundtripframework.IRoundTripPackageEventsSink",
                        "onPrePackageChangeRequest");
                func.setParameters(new Object[]{req,cell});
                m_PackSink.notifyListenersWithQualifiedProceed( func );
                
                return cell.canContinue();
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripEventDispatcher#firePreRelationChangeRequest(org.netbeans.modules.uml.core.roundtripframework.IChangeRequest, org.netbeans.modules.uml.core.eventframework.IEventPayload)
     */
    public boolean firePreRelationChangeRequest(
        IChangeRequest req,
        IEventPayload payload)
    {
        m_Mediator.changeRequest( req );
       
        if( validateEvent("PreRelationChangeRequest", payload))
        {
            IResultCell cell = prepareResultCell(payload);
            
            if(cell != null)
            {
                EventFunctor func 
                    = new EventFunctor(
                        "org.netbeans.modules.uml.core.roundtripframework.IRoundTripRelationEventsSink",
                        "onPreRelationChangeRequest");
                func.setParameters(new Object[]{req,cell});
                m_RelSink.notifyListenersWithQualifiedProceed( func );
                
                return cell.canContinue();
            }
        }
        return false;
    }

    /**
     *
     * Fired when a relation change request needs to be processed.
     *
     * @param req[in] The request that needs to be handled
     * @param payload[in] An optional payload that is delivered with the event.
     *
     * @return HRESULT
     *
     */
    public void fireRelationChangeRequest(
        IChangeRequest req,
        IEventPayload payload)
    {
        m_Mediator.changeRequest( req );

        if (validateEvent("RelationChangeRequest", payload))
        {    
            IResultCell cell = prepareResultCell(payload);
            if(cell != null)
            {
                EventFunctor relationChangeRequest = 
                            new EventFunctor(IRoundTripRelationEventsSink.class,
                                             "onRelationChangeRequest");
                m_RelSink.notifyListeners(relationChangeRequest, 
                                          new Object[] { req, cell } );
            }
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripEventDispatcher#registerForRequestProcessorInitEvents(org.netbeans.modules.uml.core.roundtripframework.IRequestProcessorInitEventsSink)
     */
    public void registerForRequestProcessorInitEvents(IRequestProcessorInitEventsSink handler)
    {
        m_InitSink.addListener( handler, null );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripEventDispatcher#registerForRoundTripAttributeEvents(org.netbeans.modules.uml.core.roundtripframework.IRoundTripAttributeEventsSink, java.lang.String)
     */
    public void registerForRoundTripAttributeEvents(
        IRoundTripAttributeEventsSink handler,
        String language)
    {
        m_AttrSink.addListener( handler,
                        new RTValidator< IRoundTripAttributeEventsSink >(m_Mediator, language ));

    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripEventDispatcher#registerForRoundTripEnumLiteralEvents(org.netbeans.modules.uml.core.roundtripframework.IRoundTripAttributeEventsSink, java.lang.String)
     */
    public void registerForRoundTripEnumLiteralEvents(
        IRoundTripEnumLiteralEventsSink handler,
        String language)
    {
        m_EnumLiteralSink.addListener( handler,
                        new RTValidator< IRoundTripEnumLiteralEventsSink >(m_Mediator, language ));

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripEventDispatcher#registerForRoundTripClassEvents(org.netbeans.modules.uml.core.roundtripframework.IRoundTripClassEventsSink, java.lang.String)
     */
    public void registerForRoundTripClassEvents(
        IRoundTripClassEventsSink handler,
        String language)
    {
        m_ClassSink.addListener( handler,
                        new RTValidator< IRoundTripClassEventsSink >( m_Mediator, language ));

    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripEventDispatcher#registerForRoundTripEnumEvents(org.netbeans.modules.uml.core.roundtripframework.IRoundTripClassEventsSink, java.lang.String)
     */
    public void registerForRoundTripEnumEvents(
        IRoundTripEnumEventsSink handler,
        String language)
    {
        m_EnumSink.addListener( handler,
                        new RTValidator< IRoundTripEnumEventsSink >( m_Mediator, language ));

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripEventDispatcher#registerForRoundTripOperationEvents(org.netbeans.modules.uml.core.roundtripframework.IRoundTripOperationEventsSink, java.lang.String)
     */
    public void registerForRoundTripOperationEvents(
        IRoundTripOperationEventsSink handler,
        String language)
    {
        m_OperSink.addListener( handler,
                        new RTValidator< IRoundTripOperationEventsSink >( m_Mediator, language ));
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripEventDispatcher#registerForRoundTripPackageEvents(org.netbeans.modules.uml.core.roundtripframework.IRoundTripPackageEventsSink, java.lang.String)
     */
    public void registerForRoundTripPackageEvents(
        IRoundTripPackageEventsSink handler,
        String language)
    {
        m_PackSink.addListener( handler,
               new RTValidator< IRoundTripPackageEventsSink >( m_Mediator, language ));

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripEventDispatcher#registerForRoundTripRelationEvents(org.netbeans.modules.uml.core.roundtripframework.IRoundTripRelationEventsSink, java.lang.String)
     */
    public void registerForRoundTripRelationEvents(
        IRoundTripRelationEventsSink handler,
        String language)
    {
        m_RelSink.addListener( handler, 
                new RTValidator< IRoundTripRelationEventsSink >( m_Mediator, language ) );

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripEventDispatcher#revokeRequestProcessorInitEvents(org.netbeans.modules.uml.core.roundtripframework.IRequestProcessorInitEventsSink)
     */
    public void revokeRequestProcessorInitEvents(IRequestProcessorInitEventsSink handler)
    {
        m_InitSink.removeListener( handler );

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripEventDispatcher#revokeRoundTripAttributeSink(org.netbeans.modules.uml.core.roundtripframework.IRoundTripAttributeEventsSink)
     */
    public void revokeRoundTripAttributeSink(IRoundTripAttributeEventsSink handler)
    {
        m_AttrSink.removeListener( handler );

    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripEventDispatcher#revokeRoundTripEnumLiteralSink(org.netbeans.modules.uml.core.roundtripframework.IRoundTripAttributeEventsSink)
     */
    public void revokeRoundTripEnumLiteralSink( IRoundTripEnumLiteralEventsSink handler )
    {
        m_EnumLiteralSink.removeListener( handler );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripEventDispatcher#revokeRoundTripClassSink(org.netbeans.modules.uml.core.roundtripframework.IRoundTripClassEventsSink)
     */
    public void revokeRoundTripClassSink(IRoundTripClassEventsSink handler)
    {
        m_ClassSink.removeListener( handler );

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripEventDispatcher#revokeRoundTripEnumSink(org.netbeans.modules.uml.core.roundtripframework.IRoundTripClassEventsSink)
     */
    public void revokeRoundTripEnumSink(IRoundTripEnumEventsSink handler)
    {
        m_EnumSink.removeListener( handler );

    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripEventDispatcher#revokeRoundTripOperationSink(org.netbeans.modules.uml.core.roundtripframework.IRoundTripOperationEventsSink)
     */
    public void revokeRoundTripOperationSink(IRoundTripOperationEventsSink handler)
    {
        m_OperSink.removeListener( handler );

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripEventDispatcher#revokeRoundTripPackageSink(org.netbeans.modules.uml.core.roundtripframework.IRoundTripPackageEventsSink)
     */
    public void revokeRoundTripPackageSink(IRoundTripPackageEventsSink handler)
    {
        m_PackSink.removeListener( handler );

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IRoundTripEventDispatcher#revokeRoundTripRelationSink(org.netbeans.modules.uml.core.roundtripframework.IRoundTripRelationEventsSink)
     */
    public void revokeRoundTripRelationSink(IRoundTripRelationEventsSink handler)
    {
        m_RelSink.removeListener( handler );

    }

    /**
     * ValidationMediator is used to help the roundtrip event dispatcher
     * determine whether or not a roundtrip event sink should be notified
     * of a particular ChangeRequest.
     */

    private static class ValidationMediator
    {
       /**
        *
        * Checks to see if the passed in language name matches the
        * current language dictated by the ChangeRequest currently
        * being processed.
        *
        * @param lang[in] Name of the language
        *
        * @return true if there's a match, else false
        * @see RoundTripEventDispatcherImpl
        *
        */

       public boolean matchesLanguage( String lang )
       {
          return lang != null && lang.equals(m_CurLanguage);
       }

       public String getCurLanguage() 
       {
           return m_CurLanguage;
       }
       
       public void setCurLanguage(String lang)
       {
           m_CurLanguage = lang;
       }
       
       public void changeRequest( IChangeRequest req )
       {
           m_CurLanguage = req != null? req.getLanguage() : null;
       }

       private String m_CurLanguage;
    }

    /**
     * RTValidator is placed on all listeners to the roundtrip events,
     * allowing language based filtering when firing round trip events.
     */

    private static class RTValidator<T> implements IValidationSink<T>
    {
       public RTValidator( ValidationMediator med, String lang ) 
       {
           m_Mediator = med;
           m_Language = lang;
       }

       /**
        *
        * This is never called directly except by the appropriate
        * EventManager object. Called before an event is dispatched
        * to the passed in sink.
        *
        * @param sink[in] The sink about to be fired.
        *
        * @return false to not allow the sink to be fired, else
        *         true to allow processing
        *
        */
       public boolean onValidateSink( T sink )
       {
           boolean isValid = true;

           if (sink != null && m_Mediator != null)
               isValid = m_Mediator.matchesLanguage( m_Language );

           return isValid;
       }

       public IValidationSink<T> clone()
       {
           return new RTValidator<T>( m_Mediator, m_Language );
       }

       private ValidationMediator m_Mediator;
       private String             m_Language;
    }  
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.eventframework.IEventDispatcher#getNumRegisteredSinks()
     */
    public int getNumRegisteredSinks()
    {
        // TODO Auto-generated method stub
        return m_AttrSink.getNumListeners() +
                m_OperSink.getNumListeners() +
                m_ClassSink.getNumListeners() +
                m_PackSink.getNumListeners() +
                m_RelSink.getNumListeners() +
                m_InitSink.getNumListeners();
    }

}
