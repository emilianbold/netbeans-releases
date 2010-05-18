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
 * Created on Jul 13, 2004
 */
package org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent;

import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.roundtripframework.ChangeKind;
import org.netbeans.modules.uml.core.roundtripframework.IChangeRequest;
import org.netbeans.modules.uml.core.roundtripframework.RTElementKind;
import org.netbeans.modules.uml.core.roundtripframework.RequestDetailKind;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author avaneeshj
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class JavaInterfaceChangeHandler extends JavaClassChangeHandler 
                                        implements IJavaInterfaceChangeHandler
{
    public JavaInterfaceChangeHandler()
    {       
    }   
   
    public JavaInterfaceChangeHandler(JavaChangeHandler copy )
    {
        super(copy);
    }
    
    public void handleRequest(IRequestValidator requestValidator)
	{
    	if ( requestValidator != null && requestValidator.getValid() && 
             !requestValidator.isRelation())    
        {
    		RequestDetails details = m_Utilities.getRequestDetails(
                    requestValidator.getRequest());
              
            if ( details != null && 
                     details.rtElementKind == RTElementKind.RCT_INTERFACE)
            {
            	int cType = details.changeKind;  
            	int cDetail = details.requestDetailKind;                
                
            	transform( requestValidator, cType, cDetail );
            	created( requestValidator, cType, cDetail );
            	nameChange( requestValidator, cType, cDetail );
            	strictfpChange( requestValidator, cType, cDetail );
            	finalChange( requestValidator, cType, cDetail );
            	documentationChange( requestValidator, cType, cDetail );
                namespaceChange( requestValidator, cType, cDetail );
                abstractChange( requestValidator, cType, cDetail );
                visibilityChange( requestValidator, cType, cDetail );
                featureDuplicated( requestValidator, cType, cDetail );
                deleted ( requestValidator, cType, cDetail );
            }
        }
     }
        
     public void transform(IRequestValidator requestValidator, IClassifier pInterface)
     {
     	try
		{
     		if ( pInterface != null )
     		{
                // If the interface is currently "unnamed" invalidate the request, 
                // but make the interface still look like an interface
                if ( m_Utilities.isElementUnnamed ( pInterface ) )
                {
                	IRequestPlug plug = new RequestPlug();
                	plug(plug);
                	requestValidator.setValid(false);
                    plug.unPlug();
                }
                // A classifier is being transformed into an interface.
                // We must delete constructors and destructors, transform attributes, 
                // and make all operations abstract. Also must make the 
                // interface itself abstract.
                // Unlike classes, we don't create constructor or finalize function.

                // Make the class abstract. We don't want to send a change request for 
                // this because interfaces are by default abstract in Java. However, 
                // the model must be made abstract
                {
                    IRequestPlug plug = new RequestPlug();
                    plug(plug);
                    pInterface.setIsAbstract (true);
                    plug.unPlug();
                }

                IJavaMethodChangeHandler opHandler = new JavaMethodChangeHandler(this);
                opHandler.setSilent (true );

                IJavaAttributeChangeHandler attrHandler 
                    = new JavaAttributeChangeHandler(this);
                attrHandler.setSilent (true);
                // delete all constructors
                 
                ETList<IOperation> constructors = 
                 	            m_Utilities.getConstructors(pInterface);
                 
                opHandler.deleteList(constructors,true);
                
                 // transform attributes
                ETList<IAttribute> attributes = pInterface.getAttributes();
                 
                if ( attributes != null && (attributes.size() > 0) )
                {
                	int count = attributes.size();
                    int idx = 0;
                    while ( idx < count)
                    {
                    	IAttribute  pItem = attributes.get(idx++);
                    	if ( pItem != null )
                    	{
                    		attrHandler.transformToInterface( pItem, pInterface);
                    	}
                    }
                 }
                
                 // Transform operations
                 ETList<IOperation> operations = pInterface.getOperations();
                 if ( operations != null && operations.size() > 0)
                 {
                    int count = operations.size();
                    int idx = 0;
                    while ( idx < count )
                    {
                    	IOperation  pItem = operations.get( idx++);
                    	if ( pItem != null )
                    	{
                    		boolean noAbstractProcessing = false; // this means "no abstract processing". Readability
                            opHandler.transformToInterface
							(requestValidator, pItem, pInterface, noAbstractProcessing);
                       }
                    }
                 }

                 // Transform all attributes from associations
                 ETList<IAssociationEnd> ends = 
                 	    m_Utilities.getReferencingNavEnds (pInterface);
                 if ( ends != null && ends.size() >  0)
                 {
                    int count = ends.size();
                    int idx = 0;
                    while ( idx < count )
                    {
                    	IAssociationEnd  pItem  = ends.get(idx++);
                    	IAttribute  pEnd  =  ( pItem instanceof IAttribute) 
                                            ? (IAttribute)pItem : null;
                        if ( pEnd != null )
                        	attrHandler.transformToClass ( pEnd, pInterface);
                    }
                 }
     		}
        }
     	catch( Exception e )
        {
             e.printStackTrace();
        }
     }
        
     public void transform(IClassifier pInterface)
     {
        IRequestValidator request = new RequestValidator();
        request.setValid(true);
        transform(request, pInterface);
     }
     
     protected void transform( IRequestValidator requestValidator, int cType, int cDetail )
	 {
        if (requestValidator != null && requestValidator.getValid())
     	{
        	try
			{
     			if ( cDetail == RequestDetailKind.RDT_TRANSFORM && 
                        cType != ChangeKind.CT_DELETE )
     			{
     				// A classifier is being transformed into an interface.
                    // We must delete constructors and destructors, delete all attributes, 
                    // and make all operations abstract. Also must make the 
                    // interface itself abstract.
                    IClassifier  pInterface = 
                        m_Utilities.getClass(requestValidator.getRequest());
                    if ( pInterface != null )
                    	transform ( requestValidator, pInterface ) ;
     			}
			}
     		catch( Exception e )
			{
     			e.printStackTrace();
			}
     	}
	 }
     protected void created( IRequestValidator requestValidator, int cType, int cDetail )
	 {   
     	if ( requestValidator != null && requestValidator.getValid())
        {
        	try
			{
        		if ( cType == ChangeKind.CT_CREATE && cDetail != RequestDetailKind.RDT_TRANSFORM )
        		{
        			// Unlike classes, we don't create constructor or finalize function.
        		}
			}
        	catch(Exception e )
			{
                e.printStackTrace();
			}
        }
	 }
     
     protected void abstractChange( IRequestValidator requestValidator, int cType, int cDetail )
	 {
        if ( requestValidator != null && requestValidator.getValid())
     	{
     		// TODO : Interfaces can never be made non-abstract, right?
     		if ( cType == ChangeKind.CT_MODIFY && 
                    cDetail == RequestDetailKind.RDT_ABSTRACT_MODIFIED )
     		{
                requestValidator.setValid(false);
     		}
     	}
	 }
    
}
