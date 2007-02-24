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
 * File       : DependencyChangeFacility.java
 * Created on : Nov 20, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.roundtripframework;

import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;

/**
 * @author Aztec
 */
public class DependencyChangeFacility
    extends RequestFacility
    implements IDependencyChangeFacility
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IDependencyChangeFacility#added(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
     */
    public void added(IClassifier pDependent, IClassifier pIndependent)
    {
        // Stubbed in C++ code
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IDependencyChangeFacility#deleted(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
     */
    public void deleted(IClassifier pDependent, IClassifier pIndependent)
    {
        // Stubbed in C++ code
    }

}
