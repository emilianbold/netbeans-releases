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

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.references.SchemaReference;
import org.netbeans.modules.bpel.model.api.references.SchemaReferenceBuilder;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;

/**
 * @author ads
 */
public class SchemaReferenceImpl<T extends ReferenceableSchemaComponent>
    extends AbstractNamedComponentReference<T> implements SchemaReference<T>,
    BpelAttributesType
{

    public SchemaReferenceImpl( T target , Class<T> type , 
            AbstractDocumentComponent parent, 
            SchemaReferenceBuilder.SchemaResolver resolver ) 
    {
        super(target, type, parent);
        myResolver = resolver;
    }

    public SchemaReferenceImpl( Class<T> type , AbstractDocumentComponent parent , 
            String value ,  SchemaReferenceBuilder.SchemaResolver resolver )
    {
        super( type , parent , value );
        myResolver = resolver;
    }
    
    

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.xam.NamedComponentReference#getEffectiveNamespace()
     */
    public String getEffectiveNamespace() {
        /*
         * Note : refString is not MAIN data in reference.
         * Reference could be created by existed element.
         * In this case namespace should be asked at this element.
         * Parent element could not have any prefix for this namepace yet.
         * 
         * Otherwise - element was DEFENITLEY created via
         * reference. And in this case we can try to ask
         * namespace via prefix at parent element.
         */
        T referenced = getReferenced();
        if (referenced != null ){
            SchemaModel sModel = referenced.getModel();
            if (sModel != null) {
                Schema schema = sModel.getSchema();
                if (schema != null) {
                    return schema.getTargetNamespace();
                }
            }
        }
        //
        assert refString != null;
        return ((BpelEntity)getParent()).getNamespaceContext().getNamespaceURI(
                getPrefix());
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.xam.Reference#get()
     */
    public T get() {
        if ( getReferenced() == null ){
            T ret = myResolver.resolve( this );
            setReferenced( ret );
            return ret;
        }
        return getReferenced();
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.references.BpelAttributesType#getAttributeType()
     */
    public AttrType getAttributeType() {
        return AttrType.QNAME;
    }
    
    private  SchemaReferenceBuilder.SchemaResolver myResolver;
}
