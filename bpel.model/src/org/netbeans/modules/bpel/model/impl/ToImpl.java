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
package org.netbeans.modules.bpel.model.impl;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Query;
import org.netbeans.modules.bpel.model.api.To;
import org.netbeans.modules.bpel.model.api.support.BpelModelVisitor;
import org.netbeans.modules.bpel.model.xam.BpelElements;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
public class ToImpl extends ToFromIntersectImpl implements To {

    ToImpl( BpelModelImpl model, Element e ) {
        super(model, e);
    }

    ToImpl( BpelBuilderImpl builder ) {
        super(builder, BpelElements.TO.getName() );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.BpelEntity#getElementType()
     */
    public Class<? extends BpelEntity> getElementType() {
        return To.class;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.To#getQuery()
     */
    public Query getQuery() {
        return getChild( Query.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.To#removeQuery()
     */
    public void removeQuery() {
        removeChild( Query.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.To#setQuery(org.netbeans.modules.bpel.model.api.Query)
     */
    public void setQuery( Query query ) {
        setChild( query , Query.class );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.BpelEntityImpl#acceptThis(org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor)
     */
    public void accept( BpelModelVisitor visitor ) {
        visitor.visit( this );
    }
    
}