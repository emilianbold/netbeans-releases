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

import javax.xml.namespace.QName;

import org.netbeans.modules.xslt.model.AttributeValueTemplate;


/**
 * @author ads
 *
 */
class AttributeValueTemplateImpl implements AttributeValueTemplate {
    
    AttributeValueTemplateImpl( XslComponentImpl parent, String value ) {
        assert value!=null;
        assert parent!=null;
        
        myValue = value;
        myParent = parent;
        int begin = myValue.indexOf( "{" );
        if ( begin > -1 ) {
            isTemplate = myValue.indexOf( "}" ) > begin;
        }
        else {
            isTemplate = false;
        }
    }
    
    AttributeValueTemplateImpl( QName qName ) {
        myQName = qName;
        isTemplate = false;
        String localPart = qName.getLocalPart();
        //String ns = qName.getNamespaceURI();
        String prefix = qName.getPrefix();
        if ( prefix == null || prefix.length() == 0) {
            myValue = localPart; 
        }
        else {
            myValue = prefix + ":" +localPart;
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.AttributeValueTemplate#getQName()
     */
    public QName getQName() {
        if ( myQName != null ) {
            return myQName;
        }
        else {
            return QNameBuilder.createQName( getParent(), toString() );
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.AttributeValueTemplate#isTemplate()
     */
    public boolean isTemplate() {
        return isTemplate;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return myValue;
    }
    
    public static AttributeValueTemplateImpl creatAttributeValueTemplate(
            XslComponentImpl parent, XslAttributes attr ) 
    {
        String str = parent.getAttribute( attr );
        return creatAttributeValueTemplate( parent , str );
    }
    
    public static AttributeValueTemplateImpl creatAttributeValueTemplate(
            XslComponentImpl parent, String value ) 
    {
        if ( value == null ) {
            return null;
        }
        return new AttributeValueTemplateImpl( parent , value );
    }
    
    public static AttributeValueTemplateImpl creatAttributeValueTemplate( 
            QName qName ) 
    {
        return new AttributeValueTemplateImpl( qName );
    }
    
    private XslComponentImpl getParent() {
        return myParent;
    }

    private XslComponentImpl myParent;
    
    private String myValue;
    
    private boolean isTemplate;
    
    private QName myQName;
    
}
