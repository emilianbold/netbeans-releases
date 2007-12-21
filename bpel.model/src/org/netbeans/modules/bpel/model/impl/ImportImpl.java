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

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.bpel.model.api.support.BpelModelVisitor;
import org.netbeans.modules.bpel.model.xam.AfterExtensions;
import org.netbeans.modules.bpel.model.xam.BpelAttributes;
import org.netbeans.modules.bpel.model.xam.BpelElements;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
public class ImportImpl extends NamespaceSpecImpl 
    implements Import, AfterExtensions 
{


    ImportImpl( BpelModelImpl model, Element e ) {
        super(model, e);
    }

    ImportImpl( BpelBuilderImpl builder ) {
        super(builder, BpelElements.IMPORT.getName() );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Import#getLocation()
     */
    public String getLocation() {
        return getAttribute( BpelAttributes.LOCATION );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Import#setLocation(java.lang.String)
     */
    public void setLocation( String value ) throws VetoException {
        setBpelAttribute( BpelAttributes.LOCATION , value );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Import#getImportType()
     */
    public String getImportType() {
        return getAttribute( BpelAttributes.IMPORT_TYPE );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Import#setImportType(java.lang.String)
     */
    public void setImportType( String value ) throws VetoException {
        setBpelAttribute( BpelAttributes.IMPORT_TYPE , value );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.BpelEntity#getElementType()
     */
    public Class<? extends BpelEntity> getElementType() {
        return Import.class;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.BpelEntityImpl#acceptThis(org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor)
     */
    public void accept( BpelModelVisitor visitor ) {
        visitor.visit( this );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.Import#removeLocation()
     */
    public void removeLocation() {
        removeAttribute( BpelAttributes.LOCATION );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.Import#removeNamespace()
     */
    public void removeNamespace() {
        removeAttribute( BpelAttributes.NAMESPACE );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.BpelEntityImpl#getDomainAttributes()
     */
    protected Attribute[] getDomainAttributes() {
        if ( myAttributes.get() == null ){
            Attribute[] attr = super.getDomainAttributes();
            Attribute[] ret = new Attribute[ attr.length + 2];
            System.arraycopy( attr , 0 , ret , 2 , attr.length );
            ret[ 0 ] = BpelAttributes.LOCATION;
            ret[ 1 ] = BpelAttributes.IMPORT_TYPE;
            myAttributes.compareAndSet( null, ret);
        }
        return myAttributes.get();
    }
    
    private static AtomicReference<Attribute[]> myAttributes = 
        new AtomicReference<Attribute[]>();

}
