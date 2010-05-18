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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.netbeans.modules.xslt.model.Import;
import org.netbeans.modules.xslt.model.InvalidAttributeValueException;
import org.netbeans.modules.xslt.model.Stylesheet;
import org.netbeans.modules.xslt.model.StylesheetChild;
import org.netbeans.modules.xslt.model.XslComponent;
import org.netbeans.modules.xslt.model.XslVisitor;
import org.netbeans.modules.xslt.model.enums.Annotaions;
import org.netbeans.modules.xslt.model.enums.DefaultValidation;
import org.w3c.dom.Element;

/**
 * @author ads
 */
class StylesheetImpl extends XslComponentImpl implements Stylesheet {

    StylesheetImpl( XslModelImpl model ) {
        this( model , null );
    }

    StylesheetImpl( XslModelImpl model , Element element ) {
        super( model , element );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.impl.XslComponentImpl#accept(org.netbeans.modules.xslt.model.XslVisitor)
     */
    @Override
    public void accept(XslVisitor visitor)
    {
        visitor.visit(this);
        acceptChildren(visitor, this, ""); // NOI18N
    }

    private void acceptChildren(XslVisitor visitor, XslComponent component, String indent) {
        List<XslComponent> children = component.getChildren();

        for (XslComponent child : children) {
//System.out.println("see: " + indent + child.getClass().getName());
            child.accept(visitor);
            acceptChildren(visitor, child, "    "); // NOI18N
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.impl.XslComponentImpl#getComponentType()
     */
    @Override
    public Class<? extends XslComponent> getComponentType()
    {
        return Stylesheet.class;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.Stylesheet#addImport(org.netbeans.modules.xslt.model.Import, int)
     */
    public void addImport( Import impt, int position ) {
        insertAtIndex( IMPORT_PROPERTY , impt, position );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.Stylesheet#appendImport(org.netbeans.modules.xslt.model.Import)
     */
    public void appendImport( Import impt ) {
        addAfter( IMPORT_PROPERTY, impt , STYLESHEET_CHILDREN );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.Stylesheet#removeImport(org.netbeans.modules.xslt.model.Import)
     */
    public void removeImport( Import impt ) {
        removeChild( IMPORT_PROPERTY , impt );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.Stylesheet#setID(java.lang.String)
     */
    public void setID( String id ) {
        setAttribute( XslAttributes.ID,  id );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.Stylesheet#getID()
     */
    public String getID() {
        return getAttribute( XslAttributes.ID );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.Stylesheet#addStylesheetChild(org.netbeans.modules.xslt.model.StylesheetChild, int)
     */
    public void addStylesheetChild( StylesheetChild child, int position ) {
        insertAtIndex( STYLESHEET_TOP_LEVEL_ELEMENTS, child , position );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.Stylesheet#appendStylesheetChild(org.netbeans.modules.xslt.model.StylesheetChild)
     */
    public void appendStylesheetChild( StylesheetChild child ) {
        appendChild( STYLESHEET_TOP_LEVEL_ELEMENTS, child);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.Stylesheet#getImports()
     */
    public List<Import> getImports() {
        return getChildren( Import.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.Stylesheet#getStylesheetChild()
     */
    public List<StylesheetChild> getStylesheetChildren() {
        return getChildren( StylesheetChild.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.Stylesheet#removeStylesheetChild(org.netbeans.modules.xslt.model.StylesheetChild)
     */
    public void removeStylesheetChild( StylesheetChild child ) {
        removeChild( STYLESHEET_TOP_LEVEL_ELEMENTS,  child );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.Stylesheet#getDefaultCollation()
     */
    @SuppressWarnings("unchecked")
    public List<String> getDefaultCollation() {
        return (List<String>) getAttributeValueOf( 
                XslAttributes.DEFAULT_COLLATION, 
                getAttribute( XslAttributes.DEFAULT_COLLATION ));
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.Stylesheet#getDefaultValidation()
     */
    public DefaultValidation getDefaultValidation() {
        return DefaultValidation.forString( 
                getAttribute( XslAttributes.DEFAULT_VALIDATION ) );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.Stylesheet#getExcludeResultPrefixes()
     */
    @SuppressWarnings("unchecked")
    public List<String> getExcludeResultPrefixes() {
        return (List<String>) getAttributeValueOf( 
                XslAttributes.EXCLUDE_RESULT_PREFIXES, 
                getAttribute( XslAttributes.EXCLUDE_RESULT_PREFIXES ));
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.Stylesheet#getExtensionElementPrefixes()
     */
    @SuppressWarnings("unchecked")
    public List<String> getExtensionElementPrefixes() {
        return (List<String>)getAttributeValueOf( 
                XslAttributes.EXTENSION_ELEMENT_PREFIXES, 
                getAttribute( XslAttributes.EXTENSION_ELEMENT_PREFIXES));
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.Stylesheet#getInputTypeAnnotations()
     */
    public Annotaions getInputTypeAnnotations() {
        return Annotaions.forString( 
                getAttribute( XslAttributes.INPUT_TYPE_ANNOTAIONS ));
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.Stylesheet#getVersion()
     */
    public BigDecimal getVersion() throws InvalidAttributeValueException {
        String value = getAttribute( XslAttributes.VERSION );
        BigDecimal ret = null;
        try {
            ret = new BigDecimal( value );
        }
        catch ( NumberFormatException exc ) {
            throw new InvalidAttributeValueException( value , exc );
        }
        return ret;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.Stylesheet#getXpathDefaultNamespace()
     */
    public String getXpathDefaultNamespace() {
        return getAttribute( XslAttributes.XPATH_DEFAULT_NAMESPACE );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.Stylesheet#setDefaultCollation(java.util.List)
     */
    public void setDefaultCollation( List<String> list ) {
        setAttributeTokenList( XslAttributes.DEFAULT_COLLATION, list);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.Stylesheet#setDefaultValidation(org.netbeans.modules.xslt.model.enums.DefaultValidation)
     */
    public void setDefaultValidation( DefaultValidation value ) {
        setAttribute( XslAttributes.DEFAULT_VALIDATION, value );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.Stylesheet#setExcludeResultPrefixes(java.util.List)
     */
    public void setExcludeResultPrefixes( List<String> list ) {
        setAttributeTokenList( XslAttributes.EXCLUDE_RESULT_PREFIXES, list);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.Stylesheet#setExtensionElementPrefixes(java.util.List)
     */
    public void setExtensionElementPrefixes( List<String> list ) {
        setAttributeTokenList( XslAttributes.EXTENSION_ELEMENT_PREFIXES, list);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.Stylesheet#setInputTypeAnnotations(org.netbeans.modules.xslt.model.enums.Annotaions)
     */
    public void setInputTypeAnnotations( Annotaions value ) {
        setAttribute( XslAttributes.INPUT_TYPE_ANNOTAIONS, value );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.Stylesheet#setVersion(java.math.BigDecimal)
     */
    public void setVersion( BigDecimal value ) {
        setAttribute( XslAttributes.VERSION , value );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.Stylesheet#setXpathDefaultNamespace(java.lang.String)
     */
    public void setXpathDefaultNamespace( String value ) {
        setAttribute( XslAttributes.XPATH_DEFAULT_NAMESPACE, value );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.Stylesheet#findAllDefinedChildren()
     */
    public Collection<StylesheetChild> findAllDefinedChildren() {
        // TODO Auto-generated method stub
        return null;
    }
    
    private static final Collection<Class<? extends XslComponent>> STYLESHEET_CHILDREN = new ArrayList<Class<? extends XslComponent>>(
            1);

    static {
        STYLESHEET_CHILDREN.add(StylesheetChild.class);
    }

}
