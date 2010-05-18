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

import org.netbeans.modules.xslt.model.AttributeValueTemplate;
import org.netbeans.modules.xslt.model.Sort;
import org.netbeans.modules.xslt.model.XslComponent;
import org.netbeans.modules.xslt.model.XslVisitor;
import org.netbeans.modules.xslt.model.enums.TBoolean;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
class SortImpl extends SequenceElementConstructorImpl implements Sort {

    SortImpl( XslModelImpl model, Element element ) {
        super( model , element );
    }
    
    SortImpl( XslModelImpl model ) {
        super( model , XslElements.SORT );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.impl.XslComponentImpl#accept(org.netbeans.modules.xslt.model.XslVisitor)
     */
    @Override
    public void accept( XslVisitor visitor )
    {
        visitor.visit( this );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.impl.XslComponentImpl#getComponentType()
     */
    @Override
    public Class<? extends XslComponent> getComponentType()
    {
        return Sort.class;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.CollationSpec#getCollation()
     */
    public String getCollation() {
        return getAttribute( XslAttributes.COLLATION );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.CollationSpec#setCollation(java.lang.String)
     */
    public void setCollation( String value ) {
        setAttribute( XslAttributes.COLLATION, value );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.LangSpec#getLang()
     */
    public AttributeValueTemplate getLang() {
        return AttributeValueTemplateImpl.creatAttributeValueTemplate( this, 
                XslAttributes.LANG );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.LangSpec#setLang(org.netbeans.modules.xslt.model.AttributeValueTemplate)
     */
    public void setLang( AttributeValueTemplate avt ) {
        setAttribute( XslAttributes.LANG, avt);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.Sort#getCaseOrder()
     */
    public AttributeValueTemplate getCaseOrder() {
        return AttributeValueTemplateImpl.creatAttributeValueTemplate( this , 
                XslAttributes.CASE_ORDER );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.Sort#getDataType()
     */
    public AttributeValueTemplate getDataType() {
        return AttributeValueTemplateImpl.creatAttributeValueTemplate( this , 
                XslAttributes.DATA_TYPE );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.Sort#getOrder()
     */
    public AttributeValueTemplate getOrder() {
        return AttributeValueTemplateImpl.creatAttributeValueTemplate( this , 
                XslAttributes.ORDER );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.Sort#getStable()
     */
    public TBoolean getStable() {
        return TBoolean.forString( getAttribute(XslAttributes.STABLE ));
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.Sort#setCaseOrder(org.netbeans.modules.xslt.model.AttributeValueTemplate)
     */
    public void setCaseOrder( AttributeValueTemplate value ) {
        setAttribute( XslAttributes.CASE_ORDER, value);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.Sort#setDataType(org.netbeans.modules.xslt.model.AttributeValueTemplate)
     */
    public void setDataType( AttributeValueTemplate value ) {
        setAttribute( XslAttributes.DATA_TYPE, value);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.Sort#setOrder(org.netbeans.modules.xslt.model.AttributeValueTemplate)
     */
    public void setOrder( AttributeValueTemplate value ) {
        setAttribute( XslAttributes.ORDER, value);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.Sort#setStable(org.netbeans.modules.xslt.model.enums.TBoolean)
     */
    public void setStable( TBoolean value ) {
        setAttribute( XslAttributes.STABLE, value);
    }
    
}
