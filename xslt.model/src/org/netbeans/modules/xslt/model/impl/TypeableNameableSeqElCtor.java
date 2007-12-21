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
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
abstract class TypeableNameableSeqElCtor extends
        NameableSequenceElementConstructor
{


    TypeableNameableSeqElCtor( XslModelImpl model, Element e ) {
        super(model, e);
    }

    TypeableNameableSeqElCtor( XslModelImpl model, XslElements element ){
        super( model , element );
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
