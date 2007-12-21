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


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.xml.namespace.QName;

import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.netbeans.modules.xslt.model.AttributeValueTemplate;
import org.netbeans.modules.xslt.model.ContentElement;
import org.netbeans.modules.xslt.model.QualifiedNameable;
import org.netbeans.modules.xslt.model.ReferenceableXslComponent;
import org.netbeans.modules.xslt.model.SequenceConstructor;
import org.netbeans.modules.xslt.model.SequenceElement;
import org.netbeans.modules.xslt.model.XslComponent;
import org.netbeans.modules.xslt.model.XslReference;
import org.netbeans.modules.xslt.model.XslVisitor;
import org.netbeans.modules.xslt.model.enums.EnumValue;
import org.w3c.dom.Comment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;


/**
 * @author ads
 *
 */
abstract class XslComponentImpl extends AbstractDocumentComponent<XslComponent>
    implements XslComponent 
{

    XslComponentImpl( XslModelImpl model, Element e ) {
        super(model, e);
        myAttributeAccess = new AttributeAccess( this );
    }
    
    XslComponentImpl( XslModelImpl model , XslElements type ) {
        this( model , createNewElement( type, model) );
    }
    
    public abstract Class<? extends XslComponent> getComponentType();


    public abstract void accept( XslVisitor visitor );

    
    /***************************************************************************
     * 
     * The methods below are frequently used in specific impls. So I place them here. 
     * 
     *************************************************************************** 
     */
    
    public AttributeValueTemplate createTemplate( QName qName ) {
        return AttributeValueTemplateImpl.creatAttributeValueTemplate( qName );
    }
    
    public AttributeValueTemplate createTemplate( String value  ) {
        return AttributeValueTemplateImpl.creatAttributeValueTemplate( this , 
                value );
    }
    
    
    public String getSelect() {
        return getAttribute( XslAttributes.SELECT );
    }

    public void setSelect( String select ) {
        setAttribute( XslAttributes.SELECT, select );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.ContentElement#getContent()
     */
    public String getContent() {
        StringBuilder text = new StringBuilder();
        NodeList nodeList = getPeer().getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if ( node instanceof Element ) {
                break;
            }
            if (node instanceof Text && ! ( node  instanceof Comment ) ) {
                text.append(node.getNodeValue());
            }
        }
        return text.toString();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.ContentElement#setContent(java.lang.String)
     */
    public void setContent( String text ) {
        verifyWrite();
        StringBuilder oldValue = new StringBuilder();
        ArrayList<Node> toRemove = new ArrayList<Node>();
        NodeList nodeList = getPeer().getChildNodes();

        Element ref = null;
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node != null && node.getNodeType() == Node.ELEMENT_NODE) {
                ref = (Element) node;
                break;
            }
            if (node instanceof Text &&  node.getNodeType() != Node.COMMENT_NODE) {
                toRemove.add(node);
                oldValue.append(node.getNodeValue());
            }
        }
        
        getModel().getAccess().removeChildren(getPeer(), toRemove, this);
        if ( text != null) {
             Text newNode = getModel().getDocument().createTextNode(text);
             if (ref != null) {
                getModel().getAccess().insertBefore(getPeer(), newNode, ref, this);
             } else {
                getModel().getAccess().appendChild(getPeer(), newNode, this); 
             }
        }
        
        firePropertyChange(ContentElement.TEXT_CONTENT_PROPERTY, 
                oldValue == null ? null : oldValue.toString(), text );
        fireValueChanged();
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.SequenceElement#getTrailingText()
     */
    public String getTrailingText() {
        XslComponentImpl parent = getParent();
        if( parent == null ) {
            return null;
        }
        return parent.getTrailingText( this );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.SequenceElement#setTrailingText(java.lang.String)
     */
    public void setTrailingText( String text ) {
        XslComponentImpl parent = getParent();
        if( parent == null ) {
            throw new IllegalStateException("Trailing text cannot be set for " +  // NOI18N
                    "component that doesn't have parent element");                // NOI18N
        }
        parent.setTrailingText( SequenceElement.TEXT_CONTENT_PROPERTY, text, 
                this );
    }

    /*
     *************************************************************************** 
     */
    
    /*
     * (non-Javadoc)
     * @see org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent#getModel()
     */
    @Override
    public XslModelImpl getModel() {
        return (XslModelImpl)super.getModel();
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.xam.AbstractComponent#getParent()
     */
    @Override
    public XslComponentImpl getParent() {
        return (XslComponentImpl)super.getParent();
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent#lookupNamespaceURI(java.lang.String)
     */
    @Override
    public String lookupNamespaceURI(String prefix) {
        return lookupNamespaceURI(prefix, true);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslComponent#createReferenceTo(org.netbeans.modules.xslt.model.ReferenceableXslComponent, java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public <T extends ReferenceableXslComponent> XslReference<T> createReferenceTo( 
            T referenced, Class<T> type ) 
    {
        // currently we only know how to resolve  QualifiedNameable elements.
        // later this impl could be changed respectively.
        assert type.isAssignableFrom( QualifiedNameable.class );
        return new GlobalReferenceImpl( (QualifiedNameable) referenced , type , 
                this );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslComponent#fromSameModel(org.netbeans.modules.xslt.model.XslComponent)
     */
    public boolean fromSameModel( XslComponent other ) {
        return getModel().equals(other.getModel());
    }
    
    protected void setAttribute( XslAttributes attribute, EnumValue value ) {
        assert value==null || !value.isInvalid() : 
            "Attempt to set up invalid enumeration value";          // NOI18N
        setAttribute( attribute, (Object)value);
    }
    
    protected void setAttribute( XslAttributes attribute, 
            AttributeValueTemplate avt) 
    {
        verifyWrite();
        if( avt == null ) {
            setAttribute( attribute, (Object)null);
        }
        Object resultValue = avt; 
        if ( !avt.isTemplate() ) {
            QName qName = avt.getQName();
            if ( qName!= null ) {
                resultValue = 
                    getPrefixedName( qName.getNamespaceURI(), 
                            qName.getLocalPart(), null, true);
            }
        }
        if ( resultValue instanceof String ) {
            Object old = null;
            String s = getAttribute(attribute);
            if (s != null) {
                old = getAttributeValueOf(attribute, s);
            }
            setAttributeAndFireChange(attribute, (String)resultValue, old, avt );
        }
        else {
            setAttribute(attribute, (Object)avt);
        }
    }
    
    protected void setAttribute( XslAttributes attribute, Object value ) {
        setAttribute( attribute.getName() , attribute, value);
    }
    
    protected void setAttributeTokenList( XslAttributes attribute, 
            List<String> value ) 
    {
        setAttribute(attribute, value, Lazy.SIMPLE_STRATEGY );
    }
    
    protected void setAttribute( XslAttributes attribute, 
            XslReference<? extends ReferenceableXslComponent> value ) 
    {
        verifyWrite();
        if( value == null ) {
            setAttribute( attribute, (Object)null);
        }
        QName qName = value.getQName();
        assert qName!= null;
        String resultValue = getPrefixedName( qName.getNamespaceURI(), 
                            qName.getLocalPart(), null, true);
        Object old = null;
        String s = getAttribute(attribute);
        if (s != null) {
            old = getAttributeValueOf(attribute, s);
        }
        setAttributeAndFireChange(attribute, resultValue, old, value);
    }
    
    protected <T extends QualifiedNameable> GlobalReferenceImpl<T> 
        resolveGlobalReference( Class<T> clazz, XslAttributes attrName )
    {
        String value = getAttribute(attrName);
        return getAtttributeAccess().resolveGlobalReference(clazz, value);
    }
    
    protected <T extends QualifiedNameable> List<XslReference<T>> 
        resolveGlobalReferenceList( Class<T> clazz, XslAttributes attrName )
    {
        String value = getAttribute(attrName);
        return getAtttributeAccess().resolveGlobalReferenceList(clazz, value);
    }
    
    @SuppressWarnings("unchecked")
    protected <T extends ReferenceableXslComponent> void setAttributeList( 
            XslAttributes attr, List<XslReference<T>> collection ) 
    {
        setAttribute( attr, collection, Lazy.REFERENCE_STRATEGY );
    }
    
    protected void setAttribute( XslAttributes attr , List<QName> list ) {
        setAttribute(attr, list, Lazy.QNAME_STRATEGY );
    }
    
    protected List<QName> getQNameList( String value ){
        return getAtttributeAccess().getQNameList(value);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent#getAttributeValueOf(org.netbeans.modules.xml.xam.dom.Attribute, java.lang.String)
     */
    @Override
    protected Object getAttributeValueOf( Attribute attr, String stringValue )
    {
        return getAtttributeAccess().getAttributeValueOf(attr, stringValue);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent#populateChildren(java.util.List)
     */
    @Override
    protected void populateChildren( List<XslComponent> children )
    {
        NodeList nl = getPeer().getChildNodes();
        if (nl != null) {
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                if (n instanceof Element) {
                    XslComponent comp = (XslComponent) getModel().getFactory()
                            .create((Element) n, this);
                    if (comp != null) {
                        children.add(comp);
                    }
                }
            }
        }
    }

    @Override
    protected int findDomainIndex(Element e) {
        int result = super.findDomainIndex( e );
        if ( result != -1 ) {
            return result;
        }
        
        // only sequence constructor could have non-xsl components.
        if ( !( this instanceof SequenceConstructor )) {
            return -1;
        }
        
        int domainInsertIndex = 0;
        NodeList list = getPeer().getChildNodes();
        for (int i=0; i<list.getLength(); i++) {
            Node node = list.item( i );
            if (list.item(i) == e) {
                return domainInsertIndex;
            }
            if ( node instanceof Element ) {
                domainInsertIndex++;
            }
        }
        return -1;
    }
    
    protected String getTrailingText( XslComponent child) {
        return getText(child, false, false);
    }
    
    protected void setTrailingText(String propName, String text, 
            XslComponent child ) 
    {
        setText(propName, text, child, false, false );
    }
    
    protected static Element createNewElement(XslElements type, XslModelImpl model){
        return model.getDocument().createElementNS( XSL_NAMESPACE, type.getName());
    }
    
    private AttributeAccess getAtttributeAccess() {
        return myAttributeAccess;
    }
    
    private <T> void setAttribute( XslAttributes attribute, List<T> list,
            AttributeListValueStartegy<T> strategy ) 
    {
        if ( list == null ) {
            setAttribute( attribute, list );
        }
        verifyWrite();
        StringBuilder builder = new StringBuilder();
        for ( T t: list ) {
            assert t!=null;
            String resultValue = strategy.toString( t , this );
            builder.append( resultValue );
            builder.append( " " );
        }
        String result = null;
        if ( builder.length() > 0 ) {
            result = builder.substring( 0, builder.length() -1 );
        }
        else {
            result = builder.toString();
        }
        Object old = null;
        String s = getAttribute(attribute);
        if (s != null) {
            old = getAttributeValueOf(attribute, s);
        }
        setAttributeAndFireChange(attribute, result, old, list );
    }
    
    private void setAttributeAndFireChange( XslAttributes attr , String
            newStringValue, Object oldValue , Object newValue ) 
    {
        setAttributeQuietly(attr, newStringValue );
        firePropertyChange( attr.getName(), oldValue, newValue );
        fireValueChanged();
    }

    @SuppressWarnings("unchecked")
    protected static final Collection<Class<? extends XslComponent>> EMPTY = 
        Collections.EMPTY_LIST;  
    
    protected static final Collection<Class<? extends XslComponent>> 
        SEQUENCE_ELEMENTS = new ArrayList<Class<? extends XslComponent>>(1);
    
    private AttributeAccess myAttributeAccess;

    static {
        SEQUENCE_ELEMENTS.add( SequenceElement.class );
    }
    
    interface AttributeListValueStartegy<T> {
        
        String toString( T token , XslComponentImpl comp );
    }
    
    static class SimpleStrategy implements AttributeListValueStartegy<String> {

        public String toString( String token , XslComponentImpl comp ) {
            return token;
        }
    }
    
    static class ReferenceStrategy<T> implements 
        AttributeListValueStartegy<T> 
    {

        public String toString( T token , XslComponentImpl comp ) {
            assert token instanceof XslReference;
            QName qName = ((XslReference)token).getQName();
            return comp.getPrefixedName( qName.getNamespaceURI(), 
                    qName.getLocalPart(), null, true);
        }
    }
    
    static class QNameStrategy implements AttributeListValueStartegy<QName> {

        public String toString( QName token, XslComponentImpl comp ) {
            return comp.getPrefixedName( token.getNamespaceURI(), 
                    token.getLocalPart(), null, true);
        }
        
    }
    
    static class Lazy {
        static final SimpleStrategy SIMPLE_STRATEGY = new SimpleStrategy();
        
        static final ReferenceStrategy REFERENCE_STRATEGY  = 
            new ReferenceStrategy();
        
        static final QNameStrategy QNAME_STRATEGY = new QNameStrategy();
    }
}
