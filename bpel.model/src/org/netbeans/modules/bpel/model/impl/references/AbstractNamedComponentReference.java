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

import org.netbeans.modules.bpel.model.api.references.Reference;
import org.netbeans.modules.xml.xam.NamedReferenceable;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.netbeans.modules.bpel.model.api.references.MappedReference;

/**
 * @author ads
 */
public abstract class AbstractNamedComponentReference<T extends NamedReferenceable> extends
        org.netbeans.modules.xml.xam.dom.AbstractNamedComponentReference<T>
        implements Reference<T>, MappedReference
{

    public AbstractNamedComponentReference( T referenced, Class<T> referencedType, 
            AbstractDocumentComponent parent )
    {
        super(referenced ,referencedType, parent);
        if ( referenced!= null ){
            setResolved();
        }
    }

    public AbstractNamedComponentReference( Class<T> referencedType, 
            AbstractDocumentComponent parent, String ref )
    {
        super(referencedType, parent , ref);
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.xam.dom.AbstractNamedComponentReference#getRefString()
     */
    @Override
    public String getRefString()
    {
        if ( (prefix == null || prefix.length()==0 ) 
                && ( isResolved()) )
        {
            refString=null;
        }
        return super.getRefString();
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.references.Reference#isResolved()
     */
    public boolean isResolved(){
        return isResolved;
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
    public void setAttribute( Attribute attr ){
        myAttribute = attr;
    }
    
    protected void setResolved(){
        isResolved = true;
    }
    
    public AbstractDocumentComponent getParent(){
        return super.getParent();
    }
    
    protected void setReferenced(T referenced) {
        super.setReferenced( referenced );
        if ( referenced != null ){
            setResolved();
        }
    }
    
    private boolean isResolved;
    private Attribute myAttribute;
}
