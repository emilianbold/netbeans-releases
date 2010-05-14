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
package org.netbeans.modules.xslt.tmap.model.impl;

import org.netbeans.modules.xml.xam.AbstractComponent;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.netbeans.modules.xslt.tmap.model.api.MappedReference;
import org.netbeans.modules.xslt.tmap.model.api.TMapReference;
import org.netbeans.modules.xslt.tmap.model.api.TMapReferenceable;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class TMapReferenceImpl<T extends TMapReferenceable>
    extends AbstractReference<T>  implements TMapReference<T>, MappedReference
{

    private Attribute myAttribute;
    private TMapReferenceBuilder.TMapResolver myResolver;
    private boolean isResolved;

    TMapReferenceImpl( T target , Class<T> type , AbstractComponent parent, 
            String value , TMapReferenceBuilder.TMapResolver resolver )
    {
        super( type , parent , value );
        setReferenced( target );
        myResolver = resolver;
        if ( target!= null ){
            setResolved();
        }
    }
    
    TMapReferenceImpl( Class<T> type , AbstractComponent parent, 
            String value , TMapReferenceBuilder.TMapResolver resolver )
    {
        super( type , parent , value );
        myResolver = resolver;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.xam.Reference#get()
     */
    public T get() {
        if ( getReferenced() == null ){
            T ret = myResolver.resolve( this );
            setReferenced( ret );
            return ret;
        }
        return getReferenced();
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.references.Reference#isResolved()
     */
    public boolean isResolved() {
        return isResolved;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.xam.AbstractReference#references(T)
     */
    @Override
    public boolean references( T component )
    {
        if ( !myResolver.haveRefString( this , component ) ) {
            return false;
        }
        return super.references(component);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.tmap.model.api.MappedReference#getAttribute()
     */
    public Attribute getAttribute() {
        return myAttribute;
    }
    
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.tmap.model.api.MappedReference#setAttribute(org.netbeans.modules.xml.xam.dom.Attribute)
     */
    public void setAttribute( Attribute attr ) {
        myAttribute = attr;
    }

    protected void setResolved(){
        isResolved = true;
    }
    
    @Override
    protected void setReferenced(T referenced) {
        super.setReferenced( referenced );
        if ( referenced != null ){
            setResolved();
        }
    }
}
