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
 * File       : JavaChangeHandlerManager.java
 * Created on : Nov 18, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent;

import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;
import org.netbeans.modules.uml.core.roundtripframework.IAssociationEndTransformChangeRequest;
import org.netbeans.modules.uml.core.roundtripframework.IParameterTypeChangeRequest;
import org.netbeans.modules.uml.core.roundtripframework.IRequestProcessor;
import org.netbeans.modules.uml.core.roundtripframework.ITypeChangeRequest;
import org.netbeans.modules.uml.core.roundtripframework.RTElementKind;

/**
 * @author Aztec
 */
public class JavaChangeHandlerManager implements IJavaChangeHandlerManager
{
    private JavaChangeHandlerUtilities m_Utilities 
    						= new JavaChangeHandlerUtilities();;

    private IJavaClassChangeHandler          m_ClassChange =
                            new JavaClassChangeHandler();
    private IJavaInterfaceChangeHandler      m_InterfaceChange = 
                            new JavaInterfaceChangeHandler();
    private IJavaGeneralizationChangeHandler m_GeneralizationChange =
                            new JavaGeneralizationChangeHandler();
    private IJavaImplementationChangeHandler m_ImplementationChange =
                            new JavaImplementationChangeHandler();
    private IJavaMethodChangeHandler         m_MethodChange =
                            new JavaMethodChangeHandler();
    private IJavaAttributeChangeHandler      m_AttributeChange =
                            new JavaAttributeChangeHandler();
    private JavaEnumerationChangeHandler     m_EnumerationChange = new JavaEnumerationChangeHandler();
    
    private JavaEnumLiteralChangeHandler m_EnumLiteralChange = new JavaEnumLiteralChangeHandler();

    private IRequestProcessor              m_Processor;

    private int                            m_BatchCount;
    private IPlugManager                     m_PlugManager;
    
    public JavaChangeHandlerManager()
    {
        m_Processor = null;
        
        m_BatchCount = 0;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent.IJavaChangeHandlerManager#dandleRequest(org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent.IRequestValidator)
     */
    public void handleRequest(IRequestValidator request)
    {
        if ( request.getValid() )
        {
            // If we are not in batch mode now, we have to make sure that
            // all handlers are not in batch mode, and that all queries are 
            // reset.

            if ( m_BatchCount == 0 )
            {
                endBatch ();
            }

            // Impact requests are not currently handled by this request processor.
            // They are changes to the model collected by core and are sent to the
            // listeners.

            ITypeChangeRequest pTypeChange = null;
            
            try
            {
                pTypeChange = (ITypeChangeRequest)request.getRequest();
            }
            catch(ClassCastException ex){}

            if ( pTypeChange == null )
            {

                // First, determine if this is a class create
                int eType = request.getRequest().getElementType();

                // If the change request has a relation on it, then the type
                // of element does not matter. What matters is the type of
                // relationship. IRelationProxy is not an IElement, so we 
                // cannot "fix" ChangeRequest.

                IRelationProxy relation = request.getRequest().getRelation();
                if (relation != null)
                {
                    eType = RTElementKind.RCT_RELATION;
                }

                IAssociationEndTransformChangeRequest pEndTransform  = null;
                try
                {
                    pEndTransform = (IAssociationEndTransformChangeRequest)request.getRequest();
                }
                catch(ClassCastException ex){}
                
                if ( pEndTransform != null )
                {
                    eType = RTElementKind.RCT_NAVIGABLE_END_ATTRIBUTE;
                }

                switch ( eType )
                {
                    case RTElementKind.RCT_CLASS :                    

                        m_ClassChange.handleRequest(request);
                        m_GeneralizationChange.handleRequest(request);
                        break;
                    case RTElementKind.RCT_INTERFACE :
                        m_InterfaceChange.handleRequest(request);
                        m_GeneralizationChange.handleRequest(request);
                        m_ImplementationChange.handleRequest(request);
                        break;
                    case RTElementKind.RCT_TEMPLATE_PARAMETER :
                       IClassifier classifier = m_Utilities.getClass(request.getRequest());
                       if(classifier instanceof IInterface)
                       {
                          m_InterfaceChange.handleRequest(request);
                       }
                       else if(classifier instanceof IClass)
                       {
                          m_ClassChange.handleRequest(request);
                       }
                       break;
                    case RTElementKind.RCT_ATTRIBUTE :
                    case RTElementKind.RCT_NAVIGABLE_END_ATTRIBUTE :
                        m_AttributeChange.handleRequest(request);
                        break;
                    case RTElementKind.RCT_OPERATION :
                    case RTElementKind.RCT_PARAMETER :
                        m_MethodChange.handleRequest(request);
                        break;
                    case RTElementKind.RCT_PACKAGE :
                        // TODO : 
                        break;
                    case RTElementKind.RCT_RELATION : 

                       if ( relation != null )
                       {
                           String relType = m_Utilities.getRelationType(relation);
                           if ( relType != null && relType.equals("Generalization") )
                           {
                               m_GeneralizationChange.handleRequest(request);
                           }
                           else if ( relType != null && relType.equals("Implementation") )
                           {
                               m_ImplementationChange.handleRequest(request);
                           }
                           else if (relType != null && ( 
                                  relType.equals("AssociationEnd") ||
                                    relType.equals("Association") ||
                                    relType.equals("Aggregation") ||
                                    relType.equals("Composition") ))
                           {
                               m_AttributeChange.handleRequest(request);
                           }
                       }
                       else
                       {
                           IElement pReqElement = m_Utilities.getElement(request.getRequest(), false);
                           if (pReqElement != null)
                           {
                               String elemType = pReqElement.getElementType();

                               if (elemType != null && 
                                  (elemType.equals("Association") ||
                                   elemType.equals("Aggregation") ||
                                   elemType.equals("Composition")) )
                               {
                                   m_AttributeChange.handleRequest(request);
                               }
                           }
                       }
                       break;
                    case RTElementKind.RCT_ENUMERATION :
                       m_EnumerationChange.handleRequest(request);
                       break;
                    case RTElementKind.RCT_ENUMERATION_LITERAL :
                       m_EnumLiteralChange.handleRequest(request);
                       break;
                    case RTElementKind.RCT_NONE :
                       break;
                }
            }
            else
            {
                IParameterTypeChangeRequest pParmTypeChange = null;
                try
                {
                    pParmTypeChange = (IParameterTypeChangeRequest)pTypeChange;
                }
                catch(ClassCastException ex){}
                
                if (pParmTypeChange != null)
                {
                    m_MethodChange.handleRequest(request);
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent.IJavaChangeHandlerManager#endBatch()
     */
    public void endBatch()
    {
        if ( m_BatchCount > 0 )
        {
           m_BatchCount--;
        }
        
        if ( m_BatchCount == 0 )
        {
           // tell all of the handlers we are now exitting batch mode
        
           m_ClassChange.exitBatch();
           m_InterfaceChange.exitBatch();
           m_GeneralizationChange.exitBatch();
           m_ImplementationChange.exitBatch();
           m_MethodChange.exitBatch();
           m_AttributeChange.exitBatch();
        }


    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent.IJavaChangeHandlerManager#getPlugManager()
     */
    public IPlugManager getPlugManager()
    {
        return m_PlugManager;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent.IJavaChangeHandlerManager#getProcessor()
     */
    public IRequestProcessor getProcessor()
    {
        return m_Processor;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent.IJavaChangeHandlerManager#setChangeHandlerUtilities(org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent.IJavaChangeHandlerUtilities)
     */
    public void setChangeHandlerUtilities(IJavaChangeHandlerUtilities utils)
    {
        m_ClassChange.setChangeHandlerUtilities ( utils );
        m_InterfaceChange.setChangeHandlerUtilities ( utils );
        m_GeneralizationChange.setChangeHandlerUtilities ( utils );
        m_ImplementationChange.setChangeHandlerUtilities ( utils );
        m_MethodChange.setChangeHandlerUtilities ( utils );
        m_AttributeChange.setChangeHandlerUtilities ( utils );
        m_EnumLiteralChange.setChangeHandlerUtilities ( utils );
        m_EnumerationChange.setChangeHandlerUtilities ( utils );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent.IJavaChangeHandlerManager#setPlugManager(org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent.IPlugManager)
     */
    public void setPlugManager(IPlugManager manager)
    {
        m_PlugManager = manager;
        m_ClassChange.setPlugManager ( manager );
        m_InterfaceChange.setPlugManager ( manager );
        m_GeneralizationChange.setPlugManager ( manager );
        m_ImplementationChange.setPlugManager ( manager );
        m_MethodChange.setPlugManager ( manager );
        m_AttributeChange.setPlugManager ( manager );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent.IJavaChangeHandlerManager#setProcessor(org.netbeans.modules.uml.core.roundtripframework.IRequestProcessor)
     */
    public void setProcessor(IRequestProcessor pProcessor)
    {
        m_Processor = pProcessor;
        m_ClassChange.setProcessor ( pProcessor );
        m_InterfaceChange.setProcessor ( pProcessor );
        m_GeneralizationChange.setProcessor ( pProcessor );
        m_ImplementationChange.setProcessor ( pProcessor );
        m_MethodChange.setProcessor ( pProcessor );
        m_AttributeChange.setProcessor ( pProcessor );

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent.IJavaChangeHandlerManager#startBatch()
     */
    public void startBatch()
    {
        m_BatchCount++;
        if ( m_BatchCount == 1 )
        {
            // tell all of the handlers we are now in batch mode

            m_ClassChange.enterBatch();
            m_InterfaceChange.enterBatch();
            m_GeneralizationChange.enterBatch();
            m_ImplementationChange.enterBatch();
            m_MethodChange.enterBatch();
            m_AttributeChange.enterBatch();
        }

    }

}
