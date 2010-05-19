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

import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xslt.model.enums.TBoolean;
import org.netbeans.modules.xslt.model.enums.Validation;
import org.w3c.dom.Element;

/**
 * @author ads
 *
 */
abstract class ValidationCopyNsSpecImpl extends SequenceElementImpl {

    ValidationCopyNsSpecImpl( XslModelImpl model, Element e ) {
        super(model, e);
    }

    ValidationCopyNsSpecImpl( XslModelImpl model , XslElements type ){
        super( model , type );
    }
            
    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.CopyNamespacesSpec#getCopyNamespeces()
     */
    public TBoolean getCopyNamespeces() {
        return TBoolean.forString( getAttribute( XslAttributes.COPY_NAMESPACES) );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.CopyNamespacesSpec#setCopyNamespeces(org.netbeans.modules.xslt.model.enums.TBoolean)
     */
    public void setCopyNamespeces( TBoolean value ) {
        setAttribute( XslAttributes.COPY_NAMESPACES, value);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.ValidationSpec#getValidation()
     */
    public Validation getValidation() {
        return Validation.forString( getAttribute( XslAttributes.VALIDATION ));
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.ValidationSpec#setValidation(org.netbeans.modules.xslt.model.enums.Validation)
     */
    public void setValidation( Validation validation ) {
        setAttribute( XslAttributes.VALIDATION, validation );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.TypeSpec#getType()
     */
    public Reference<GlobalType> getType() {
        // TODO getType
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.TypeSpec#setType(org.netbeans.modules.xml.xam.Reference)
     */
    public void setType( Reference<GlobalType> type ) {
        // TODO getType
        
    }

}