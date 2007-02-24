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
 * File       : AttributeTypeChangeRequest.java
 * Created on : Nov 24, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.roundtripframework;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;

/**
 * @author Aztec
 */
public class AttributeTypeChangeRequest
    extends TypeChangeRequest
    implements IAttributeTypeChangeRequest
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.roundtripframework.IAttributeTypeChangeRequest#getImpactedAttribute()
     */
    public IAttribute getImpactedAttribute()
    {
        IElement elem = getImpactedElement();
        IAttribute pAttr = (elem instanceof IAttribute)
                            ? (IAttribute)elem : null;
        return pAttr;
    }

}
