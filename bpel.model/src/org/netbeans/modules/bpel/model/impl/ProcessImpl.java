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

package org.netbeans.modules.bpel.model.impl;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.ExtensionContainer;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.bpel.model.api.support.BpelModelVisitor;
import org.netbeans.modules.bpel.model.api.support.TBoolean;
import org.netbeans.modules.bpel.model.ext.ExtBpelAttribute.IsAtomicAttribute;
import org.netbeans.modules.bpel.model.xam.BpelAttributes;
import org.netbeans.modules.bpel.model.xam.BpelElements;
import org.netbeans.modules.bpel.model.xam.BpelTypesEnum;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.Element;

/**
 * @author ads
 */
public class ProcessImpl extends BaseScopeImpl implements Process {

    ProcessImpl( BpelModelImpl model, Element element ) {
        super(model, element);
    }

    ProcessImpl( BpelBuilderImpl builder ) {
        super(builder, BpelElements.PROCESS.getName() );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.Process#getExpressionLanguage()
     */
    public String getExpressionLanguage() {
        return getAttribute(BpelAttributes.EXPRESSION_LANGUAGE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.Process#setExpressionLanguage(java.lang.String)
     */
    public void setExpressionLanguage( String value ) throws VetoException {
        setBpelAttribute(BpelAttributes.EXPRESSION_LANGUAGE, value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.Process#getQueryLanguage()
     */
    public String getQueryLanguage() {
        return getAttribute(BpelAttributes.QUERY_LANGUAGE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.Process#setQueryLanguage(java.lang.String)
     */
    public void setQueryLanguage( String value ) throws VetoException {
        setBpelAttribute(BpelAttributes.QUERY_LANGUAGE, value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.Process#getTargetNamespace()
     */
    public String getTargetNamespace() {
        return getAttribute(BpelAttributes.TARGET_NAMESPACE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.Process#setTargetNamespace(java.lang.String)
     */
    public void setTargetNamespace( String value ) throws VetoException {
        assert value != null;
        setBpelAttribute(BpelAttributes.TARGET_NAMESPACE, value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.Process#removeQueryLanguage()
     */
    public void removeQueryLanguage() {
        removeAttribute(BpelAttributes.QUERY_LANGUAGE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.soa.model.bpel20.api.Process#removeExpressionLanguage()
     */
    public void removeExpressionLanguage() {
        removeAttribute(BpelAttributes.EXPRESSION_LANGUAGE);
    }
    

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Process#getExtensionContainer()
     */
    public ExtensionContainer getExtensionContainer() {
        return getChild( ExtensionContainer.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Process#setExtensionContainer(org.netbeans.modules.soa.model.bpel20.api.ExtensionContainer)
     */
    public void setExtensionContainer( ExtensionContainer value ) {
        setChild( value , ExtensionContainer.class , 
                BpelTypesEnum.AFTER_EXTENSIONS );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Process#removeExtensionContainer()
     */
    public void removeExtensionContainer() {
        removeChild( ExtensionContainer.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Process#getImports()
     */
    public Import[] getImports() {
        readLock();
        try {
            List<Import> list = getChildren( Import.class);
            return list.toArray( new Import[list.size() ] );
        }
        finally {
            readUnlock();
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Process#addImport(org.netbeans.modules.soa.model.bpel20.api.Import)
     */
    public void addImport( Import imp ) {
        addChildBefore( imp , Import.class , BpelTypesEnum.AFTER_IMPORTS );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Process#setImport(org.netbeans.modules.soa.model.bpel20.api.Import, int)
     */
    public void setImport( Import imp, int i ) {
        setChildAtIndex( imp , Import.class , i );
        
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Process#insertImport(org.netbeans.modules.soa.model.bpel20.api.Import, int)
     */
    public void insertImport( Import imp, int i ) {
        insertAtIndex( imp , Import.class , i , BpelTypesEnum.AFTER_IMPORTS );
        
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Process#removeImport(int)
     */
    public void removeImport( int i ) {
        removeChild( Import.class , i );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Process#setImports(org.netbeans.modules.soa.model.bpel20.api.Import[])
     */
    public void setImports( Import[] imports ) {
        setArrayBefore( imports , Import.class , BpelTypesEnum.AFTER_IMPORTS );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Process#getImport(int)
     */
    public Import getImport( int i ) {
        return getChild( Import.class , i );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.Process#sizeOfImports()
     */
    public int sizeOfImports() {
        readLock();
        try {
            return getChildren( Import.class ).size();
        }
        finally {
            readUnlock();
        }
    }

    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.api.BpelEntity#getElementType()
     */
    public Class<? extends BpelEntity> getElementType() {
        return Process.class;
    }

    public void accept( BpelModelVisitor visitor ) {
        visitor.visit(this);
    }

    @Override
    protected BpelEntity create( Element element ) {
        if ( BpelElements.EXTENSIONS.getName().equals(element.getLocalName()))
        {
            return new ExtensionContainerImpl(getModel(), element);
        }
        else if ( BpelElements.IMPORT.getName().equals(element.getLocalName()))
        {
            return new ImportImpl(getModel(), element);
        }
        return super.create(element);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.BpelContainerImpl#getMultiplicity(org.netbeans.modules.bpel.model.api.BpelEntity)
     */
    @Override
    protected Multiplicity getMultiplicity( BpelEntity entity ) {
        if ( getChildType( entity).equals(ExtensionContainer.class ) ){
            return Multiplicity.SINGLE;
        }
        return super.getMultiplicity(entity);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.soa.model.bpel20.impl.BpelEntityImpl#getDomainAttributes()
     */
    protected Attribute[] getDomainAttributes() {
        if ( myAttributes.get() == null ){
            Attribute[] attr = super.getDomainAttributes();
            Attribute[] ret = new Attribute[ attr.length + 3];
            System.arraycopy( attr , 0 , ret , 3 , attr.length );
            ret[ 0 ] = BpelAttributes.EXPRESSION_LANGUAGE;
            ret[ 1 ] = BpelAttributes.QUERY_LANGUAGE;
            ret[ 2 ] = BpelAttributes.TARGET_NAMESPACE;
            myAttributes.compareAndSet( null, ret);
        }
        return myAttributes.get();
    }
    
    private static AtomicReference<Attribute[]> myAttributes = 
        new AtomicReference<Attribute[]>();

    public TBoolean isAtomic() {
        return getBooleanAttribute(new IsAtomicAttribute());
    }

    public void setAtomic(TBoolean value) {
        writeLock();
        try {
            IsAtomicAttribute newAttr = new IsAtomicAttribute();
            newAttr.setOwner(this);
            newAttr.registerNsPrefix();
            setBpelAttribute(newAttr, value);
        } finally {
            writeUnlock();
        }
    }
    
}
