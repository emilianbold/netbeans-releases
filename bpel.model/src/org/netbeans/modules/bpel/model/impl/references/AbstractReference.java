/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/**
 *
 */
package org.netbeans.modules.bpel.model.impl.references;

import org.netbeans.modules.xml.xam.AbstractComponent;
import org.netbeans.modules.xml.xam.Referenceable;


/**
 * @author ads
 *
 */
abstract class AbstractReference<T extends Referenceable> extends
        org.netbeans.modules.xml.xam.AbstractReference<T>
{

    public AbstractReference( T referenced, Class<T> referencedType,
            AbstractComponent parent )
    {
        super(referenced, referencedType, parent );
    }


    public AbstractReference( Class<T> referencedType, AbstractComponent parent, 
            String ref )
    {
        super(referencedType, parent , ref );
    }

    protected AbstractComponent getParent() {
        return super.getParent();
    }
}
