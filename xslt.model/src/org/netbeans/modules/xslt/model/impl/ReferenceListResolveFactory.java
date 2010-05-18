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
package org.netbeans.modules.xslt.model.impl;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.netbeans.modules.xslt.model.ReferenceableXslComponent;
import org.netbeans.modules.xslt.model.XslReference;


/**
 * This interface is intended for custom 
 * reference list resolving.
 * There is a comon way for resolving reference list :
 * when each token in list correspond ONLY one referenced
 * component. It is used in AttributeAccess#resolveGlobalReferenceList
 * method. But sometimes there can be more referenced 
 * elements than original token size in list.
 * This interface will be used for applying implemented
 * in custom way such logic.
 *    
 * @author ads
 *
 */
interface ReferenceListResolveFactory {

    boolean isApplicable( Class referenceType );
    
    <T extends ReferenceableXslComponent> List<XslReference<T>> resolve(
            AttributeAccess access , Class<T> clazz, String value );
    
    class Factories {
        static final Collection<ReferenceListResolveFactory> FACTORIES = 
            new LinkedList<ReferenceListResolveFactory>();
        
        static {
            FACTORIES.add( new AttributeSetReferenceListResolver() );
        }
    }
}
