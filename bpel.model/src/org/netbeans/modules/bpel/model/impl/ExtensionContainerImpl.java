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

import java.util.List;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Extension;
import org.netbeans.modules.bpel.model.api.ExtensionContainer;
import org.netbeans.modules.bpel.model.api.support.BpelModelVisitor;
import org.netbeans.modules.bpel.model.xam.BpelElements;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
public class ExtensionContainerImpl extends ExtensibleElementsImpl implements
        ExtensionContainer
{

    ExtensionContainerImpl( BpelModelImpl model, Element e ) {
        super(model, e);
    }

    public ExtensionContainerImpl( BpelBuilderImpl builder) {
        super(builder, BpelElements.EXTENSIONS.getName() );
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.ExtensionContainer#getExtensions()
     */
    public Extension[] getExtensions() {
        readLock();
        try {
            List<Extension> list = getChildren( Extension.class );
            return list.toArray( new Extension[ list.size() ] );
        }
        finally {
            readUnlock();
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.ExtensionContainer#getExtension(int)
     */
    public Extension getExtension( int i ) {
        return getChild( Extension.class , i );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.ExtensionContainer#removeExtension(int)
     */
    public void removeExtension( int i ) {
        removeChild( Extension.class , i );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.ExtensionContainer#addExtension(org.netbeans.modules.soa.model.bpel20.api.Extension)
     */
    public void addExtension( Extension extension ) {
        addChild( extension , Extension.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.ExtensionContainer#setExtension(org.netbeans.modules.soa.model.bpel20.api.Extension, int)
     */
    public void setExtension( Extension extension, int i ) {
        setChildAtIndex( extension , Extension.class , i );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.ExtensionContainer#insertExtension(org.netbeans.modules.soa.model.bpel20.api.Extension, int)
     */
    public void insertExtension( Extension extension, int i ) {
        insertAtIndex( extension , Extension.class , i );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.ExtensionContainer#setExtensions(org.netbeans.modules.soa.model.bpel20.api.Extension[])
     */
    public void setExtensions( Extension[] extensions ) {
        setArrayBefore( extensions , Extension.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.ExtensionContainer#sizeOfExtensions()
     */
    public int sizeOfExtensions() {
        readLock();
        try {
            return getChildren( Extension.class ).size();
        }
        finally {
            readUnlock();
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.BpelEntity#getElementType()
     */
    public Class<? extends BpelEntity> getElementType() {
        return ExtensionContainer.class;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.BpelEntityImpl#acceptThis(org.netbeans.modules.soa.model.bpel20.api.support.BpelModelVisitor)
     */
    public void accept( BpelModelVisitor visitor ){
        visitor.visit( this );
    }
    

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.ExtensibleElementsImpl#create(org.w3c.dom.Element)
     */
    @Override
    protected BpelEntity create( Element element ){
        if ( BpelElements.EXTENSION.getName().equals( element.getLocalName())){
            return new ExtensionImpl( getModel() , element );
        }
        return super.create(element);
    }

}
