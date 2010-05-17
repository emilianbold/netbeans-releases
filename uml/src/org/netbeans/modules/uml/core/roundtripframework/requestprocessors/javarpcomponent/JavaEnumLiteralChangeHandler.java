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
 * JavaEnumLiteralChangeHandler.java
 *
 * Created on April 8, 2005, 8:52 AM
 */

package org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent;


import org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumeration;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumerationLiteral;
import java.util.Iterator;

import org.dom4j.Attribute;
import org.dom4j.Node;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IDependency;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationProxy;
import org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IVisibilityKind;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.INavigableEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterableElement;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguage;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguageDataType;
import org.netbeans.modules.uml.core.roundtripframework.ChangeKind;
import org.netbeans.modules.uml.core.roundtripframework.ChangeRequest;
import org.netbeans.modules.uml.core.roundtripframework.IAssociationEndTransformChangeRequest;
import org.netbeans.modules.uml.core.roundtripframework.IChangeRequest;
import org.netbeans.modules.uml.core.roundtripframework.IRequestProcessor;
import org.netbeans.modules.uml.core.roundtripframework.RTElementKind;
import org.netbeans.modules.uml.core.roundtripframework.RequestDetailKind;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.NameManager;
import org.netbeans.modules.uml.ui.support.ErrorDialogIconKind;

/**
 *
 * @author Administrator
 */
public class JavaEnumLiteralChangeHandler extends JavaChangeHandler
{
   
   /** Creates a new instance of JavaEnumLiteralChangeHandler */
   public JavaEnumLiteralChangeHandler()
   {
      super();
   }
   
	public JavaEnumLiteralChangeHandler(JavaChangeHandler copy)
	{
		super(copy);
	}
	
	public void handleRequest(IRequestValidator requestValidator)
	{
		if (requestValidator != null && requestValidator.getValid())		
		{			
			if (m_Utilities != null)
			{
				RequestDetails details = m_Utilities.getRequestDetails(
													requestValidator.getRequest());																
				if(details != null)
				{
				   int cType = details.changeKind;	
				   int cDetail = details.requestDetailKind;
				   int eType = details.rtElementKind;
				   
				   if (eType == RTElementKind.RCT_ENUMERATION_LITERAL )
               {
                  
                  added(requestValidator, cType, cDetail);
                  deleted(requestValidator, cType, cDetail);
                  visibilityChange(requestValidator, cType, cDetail);
                  nameChange(requestValidator, cType, cDetail);
                  moved(requestValidator, cType, cDetail);
               }
               else if(cDetail == RequestDetailKind.RDT_FEATURE_DUPLICATED )
               {
                  duplicated(requestValidator, cType, cDetail);
               }
            }
			}
		}
	}
   
   public void added( IEnumerationLiteral literal, 
                      boolean valid, 
                      IClassifier pClass)
	{
		IClassifier pLocalClass = pClass;
		if (pLocalClass == null)
      {
			pLocalClass = literal.getEnumeration();
      }
      
		if ( pLocalClass != null )
		{
         // Nothing to do at this time.
         // TODO: In the future we ned to add the abstract methods to the literal
		}				
	}
   
   public void deleted(IEnumerationLiteral pAttribute, IEnumeration pClass)
	{
		// There is nothing to do at this time.
      
	}	
   
   public void nameChange(IEnumerationLiteral pAttribute, IEnumeration pClass)
	{
		// There is nothing to do.
	}
   
   /////////////////////////////////////////////////////////////////////////////
   // Helper Methods
   
   protected void added (IRequestValidator requestValidator, int cType, int cDetail )
	{
		try
		{
			if ( requestValidator != null && requestValidator.getValid())
			{
				// First, determine if this is an attribute create
				if ( cType == ChangeKind.CT_CREATE )
				{
					boolean valid = true;
               
               // Current there is nothing to do.  In the future we will want to
               // add all abstract operation owned by the enumeration as children
               // of the litereal.
               
               // TODO: Make abstract operations children of the literal.
               
					requestValidator.setValid(valid);
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
   
   protected void deleted (IRequestValidator requestValidator, int cType, int cDetail )
	{
		// There is nothing to do here
      // TODO: In the future we will want to delete the owned operations and attributes
	}
   
   protected void visibilityChange (IRequestValidator requestValidator, int cType, int cDetail )
	{
		// There is nothing to do here
	}
   
   protected void nameChange (IRequestValidator requestValidator, int cType, int cDetail )
	{
		// There is nothing to do here
	}
   
   protected void moved (IRequestValidator requestValidator, int cType, int cDetail )
	{
		// There is nothing to do here.
      // TODO: In the future we may want to move the owned operations and attributes
	}
   
   protected void duplicated (IRequestValidator requestValidator, int cType, int cDetail )
	{
		// There is nothing to do here.
      // TODO: In the future we will want to duplicate the owned operations and attributes
	}
}
