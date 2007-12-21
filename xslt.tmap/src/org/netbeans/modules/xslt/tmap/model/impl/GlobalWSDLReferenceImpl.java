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
package org.netbeans.modules.xslt.tmap.model.impl;

import org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xslt.tmap.model.api.WSDLReference;
import org.netbeans.modules.xslt.tmap.model.impl.AttributesType.AttrType;

/**
 * @author ads
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class GlobalWSDLReferenceImpl<T extends ReferenceableWSDLComponent> 
        extends AbstractNamedComponentReference<T> 
        implements NamedComponentReference<T>, WSDLReference<T>
{

////    public GlobalWSDLReferenceImpl(
////            T referenced, 
////            Class<T> type, 
////            AbstractDocumentComponent parent) {
////        super(referenced, type, parent);
////    }
////    
////    public GlobalWSDLReferenceImpl(
////            Class<T> type, 
////            AbstractDocumentComponent parent, 
////            String refString){
////        super(type, parent, refString);
////    }
////
////    // impl getted from org.netbeans.modules.xml.wsdl.model.impl#GlobalReferenceImpl
////    public T get() {
////        WSDLComponentBase wparent = WSDLComponentBase.class.cast(getParent());
////        if (super.getReferenced() == null) {
////            String localName = getLocalName();
////            String namespace = getEffectiveNamespace();
////            WSDLModel model = wparent.getWSDLModel();
////            T target = null;
////            if (namespace != null && namespace.equals(model.getDefinitions().getTargetNamespace())) {
////                target = new FindReferencedVisitor<T>(model.getDefinitions()).find(localName, getType());
////            }
////            if (target == null) {
////                for (Import i : wparent.getWSDLModel().getDefinitions().getImports()) {
////                    if (! i.getNamespace().equals(namespace)) {
////                        continue;
////                    }
////                    try {
////                        model = i.getImportedWSDLModel();
////                    } catch(CatalogModelException ex) {
////                        continue;
////                    }
////                    target = new FindReferencedVisitor<T>(model.getDefinitions()).find(localName, getType());
////                    if (target != null) {
////                        break;
////                    }
////                }
////            }
////            setReferenced(target);
////        }
////        return getReferenced();
////    }
////    
////    public WSDLComponentBase getParent() {
////        return (WSDLComponentBase) super.getParent();
////    }
////    
////    public String getEffectiveNamespace() {
////        if (refString == null) {
////            assert getReferenced() != null;
////            return getReferenced().getModel().getDefinitions().getTargetNamespace();
////        } else {
////            return getParent().lookupNamespaceURI(getPrefix());
////        }
////    }
//////
////    
////    /* (non-Javadoc)
////     * @see org.netbeans.modules.bpel.model.impl.references.BpelAttributesType#getAttributeType()
////     */
////    public AttrType getAttributeType() {
////        return AttrType.QNAME;
////    }
    GlobalWSDLReferenceImpl( Class<T> type, AbstractDocumentComponent parent,
            String refString , WSDLReferenceBuilder.WSDLResolver resolver )
    {
        super( type, parent , refString );
        myResolver = resolver;
    }
    
    GlobalWSDLReferenceImpl( T target, Class<T> type, 
            AbstractDocumentComponent parent , 
            WSDLReferenceBuilder.WSDLResolver resolver )
    {
        super(target, type, parent );
        myResolver = resolver;
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
        if ( isBroken() ) {
            assert refString!=null;
            return ((TMapComponentAbstract)getParent()).getNamespaceContext().getNamespaceURI(
                getPrefix());
        }
        else {
            return getReferenced().getModel().getDefinitions().
                getTargetNamespace();
        }
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.references.BpelAttributesType#getAttributeType()
     */
    public AttrType getAttributeType() {
        return AttrType.QNAME;
    }

    private WSDLReferenceBuilder.WSDLResolver myResolver;
}
