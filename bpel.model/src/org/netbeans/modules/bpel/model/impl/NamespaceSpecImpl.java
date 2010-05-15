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

import java.util.concurrent.atomic.AtomicReference;

import org.netbeans.modules.bpel.model.api.NamespaceSpec;
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.bpel.model.xam.BpelAttributes;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
public abstract class NamespaceSpecImpl extends ExtensibleElementsImpl
    implements NamespaceSpec
{

    NamespaceSpecImpl( BpelModelImpl model, Element e ) {
        super(model, e);
    }

    NamespaceSpecImpl( BpelBuilderImpl builder, String tagName ) {
        super(builder, tagName);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.NamespaceReference#getNamespace()
     */
    public String getNamespace() {
        return getAttribute( BpelAttributes.NAMESPACE );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.NamespaceReference#setNamespace(java.lang.String)
     */
    public void setNamespace( String uri ) throws VetoException {
        setBpelAttribute( BpelAttributes.NAMESPACE , uri );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.BpelEntityImpl#getDomainAttributes()
     */
    protected Attribute[] getDomainAttributes() {
        if ( myAttributes.get() == null ){
            Attribute[] attr = super.getDomainAttributes();
            Attribute[] ret = new Attribute[ attr.length + 1];
            System.arraycopy( attr , 0 , ret , 1 , attr.length );
            ret[ 0 ] = BpelAttributes.NAMESPACE;
            myAttributes.compareAndSet( null , ret);
        }
        return myAttributes.get();
    }
    
    private static AtomicReference<Attribute[]> myAttributes = 
        new AtomicReference<Attribute[]>();

}
