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
package org.netbeans.modules.bpel.model.impl.references;

import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.model.api.references.BpelReferenceable;
import org.netbeans.modules.bpel.model.api.references.MappedReference;
import org.netbeans.modules.xml.xam.AbstractComponent;
import org.netbeans.modules.xml.xam.dom.Attribute;

/**
 * @author ads
 */
class BpelReferenceImpl<T extends BpelReferenceable>
    extends AbstractReference<T>  implements BpelReference<T>, MappedReference
{

    BpelReferenceImpl( T target , Class<T> type , AbstractComponent parent, 
            String value , BpelReferenceBuilder.BpelResolver resolver )
    {
        super( type , parent , value );
        setReferenced( target );
        myResolver = resolver;
        if ( target!= null ){
            setResolved();
        }
    }
    
    BpelReferenceImpl( Class<T> type , AbstractComponent parent, 
            String value , BpelReferenceBuilder.BpelResolver resolver )
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
    public boolean references( T entity )
    {
        if ( !myResolver.haveRefString( this , entity ) ) {
            return false;
        }
        return super.references(entity);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.references.MappedReference#getAttribute()
     */
    public Attribute getAttribute() {
        return myAttribute;
    }
    
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.references.MappedReference#setAttribute(org.netbeans.modules.xml.xam.dom.Attribute)
     */
    public void setAttribute( Attribute attr ) {
        myAttribute = attr;
    }

    protected void setResolved(){
        isResolved = true;
    }
    
    protected void setReferenced(T referenced) {
        super.setReferenced( referenced );
        if ( referenced != null ){
            setResolved();
        }
    }
    
    private Attribute myAttribute;
    private BpelReferenceBuilder.BpelResolver myResolver;
    private boolean isResolved;
}
