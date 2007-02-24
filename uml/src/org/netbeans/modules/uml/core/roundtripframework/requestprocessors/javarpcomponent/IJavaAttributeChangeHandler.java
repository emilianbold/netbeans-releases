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

/*
 * Created on Nov 5, 2003
 *
 */
package org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent;

import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.INavigableEnd;

/**
 * @author aztec
 *
 */
public interface IJavaAttributeChangeHandler extends IJavaChangeHandler
{
	public void handleRequest(IRequestValidator request);
	
	public void transformToClass (IAttribute pAttribute, IClassifier pClass);
   
   public void transformToEnumeration(IAttribute pAttribute, IClassifier pClass);
	
	public void transformToInterface( IAttribute pAttribute, IClassifier pClass);
	
	public boolean addNavigableEndAttribute (IRequestValidator request, IAttribute pAttribute,
   										     IClassifier pClass, boolean valid );
   										     
	public void setMultipleInitialValue (IAttribute pAttribute, boolean force);
	
	public void setDefaultInitialValue(IAttribute pAttribute);
	
	public void deleted(IAttribute pAttribute, IClassifier pClass);
	
	public boolean nameNavigableEnd(INavigableEnd pEnd);
	
	public void added( IAttribute pAttribute, boolean valid, IClassifier pClass);
	  			
	public void nameChange(IAttribute pAttribute, IClassifier pClass);
	
	public void multiplicityChange(IAttribute pAttribute, IClassifier pClass);
	
	public void moved(IRequestValidator request, IAttribute pAttribute, 
					  IClassifier pFromClass, IClassifier pToClass);
					  
	public void duplicated(IAttribute pFromAttribute, IClassifier pFromClass, 
						   IAttribute pToAttribute, IClassifier pToClass );					     
}



