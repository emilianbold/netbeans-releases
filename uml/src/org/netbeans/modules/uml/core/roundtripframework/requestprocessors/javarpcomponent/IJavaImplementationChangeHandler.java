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
 * Created on Nov 13, 2003
 *
 */
package org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent;

import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;

/**
 * @author aztec
 *
 */
public interface IJavaImplementationChangeHandler extends IJavaDependencyChangeHandler					
{
	public void handleRequest( IRequestValidator requestValidator);
	
	public void  added ( IRequestValidator request, IClassifier pBaseClass, IClassifier pDerivedClass );
	
	public void  added ( IClassifier pBaseClass, IClassifier pDerivedClass );

	public void  deleted ( IRequestValidator request, IClassifier pBaseClass, IClassifier pDerivedClass );
	
	public void  deleted ( IClassifier pBaseClass, IClassifier pDerivedClass );
}



