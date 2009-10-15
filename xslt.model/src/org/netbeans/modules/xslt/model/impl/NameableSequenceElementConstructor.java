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
import org.w3c.dom.Element;

/**
 * @author ads
 *
 */
abstract class NameableSequenceElementConstructor extends
        SequenceElementConstructorImpl
{

    NameableSequenceElementConstructor( XslModelImpl model, Element e ) {
        super(model, e);
    }
    
    NameableSequenceElementConstructor( XslModelImpl model, XslElements type ) {
        super(model, type );
    }
    
    public AttributeValueTemplate getName() {
        return AttributeValueTemplateImpl.creatAttributeValueTemplate(
                this, XslAttributes.AVT_NAME );
    }

    public void setName( AttributeValueTemplate name ) {
        setAttribute( XslAttributes.AVT_NAME , name );
    }
    
    /*
     *
     *  The methods below are not common for any sublclass of this class,
     *  but I put them here ( without declaring implementation corresponding
     *  interface ) because only one subclass doesn't have those methods.   
     * 
     */
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.NamespaceSpec#getNamespace()
     */
    public AttributeValueTemplate getNamespace() {
        return AttributeValueTemplateImpl.creatAttributeValueTemplate(
                this, XslAttributes.NAMESPACE );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.NamespaceSpec#setNamespace(org.netbeans.modules.xslt.model.AttributeValueTemplate)
     */
    public void setNamespace( AttributeValueTemplate namespace ) {
        setAttribute( XslAttributes.NAMESPACE , namespace );
    }

}
