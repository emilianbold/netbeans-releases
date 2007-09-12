/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.xslt.tmap.model.impl;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Nameable;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xslt.tmap.model.api.ExNamespaceContext;
import org.netbeans.modules.xslt.tmap.model.spi.ExternalModelRetriever;
import org.netbeans.modules.xslt.tmap.model.api.Param;
import org.netbeans.modules.xslt.tmap.model.api.PartnerLinkTypeReference;
import org.netbeans.modules.xslt.tmap.model.api.TMapComponent;
import org.netbeans.modules.xslt.tmap.model.api.TMapModel;
import org.netbeans.modules.xslt.tmap.model.api.Transform;
import org.netbeans.modules.xslt.tmap.model.api.Variable;
import org.netbeans.modules.xslt.tmap.model.api.VariableReference;
import org.netbeans.modules.xslt.tmap.model.api.WSDLReference;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class WSDLReferenceBuilder {
    
    private static final WSDLReferenceBuilder INSTANCE = new WSDLReferenceBuilder();
    private static Collection<? extends ExternalModelRetriever> myRetrievers;
    private Collection<WSDLReferenceFactory> myCollection;

    private WSDLReferenceBuilder() {
        Result<ExternalModelRetriever> result = Lookup.getDefault().lookup(
            new Lookup.Template<ExternalModelRetriever>(ExternalModelRetriever.class));
        myRetrievers = result.allInstances();
        
        myCollection = new LinkedList<WSDLReferenceFactory>();
        myCollection.add( new PartnerLinkTypeResolver() );
        myCollection.add( new RoleResolver() );
        myCollection.add( new OperationResolver() );
        myCollection.add( new PartResolver() );
    }
    
    public static WSDLReferenceBuilder getInstance(){
        return INSTANCE;
    }
    
    public <T extends ReferenceableWSDLComponent> WSDLReference<T> build( 
            Class<T> clazz , TMapComponentAbstract entity , Attribute attr )
    {
        WSDLReference<T> ref = build( clazz , entity , entity.getAttribute( attr ) );
// TODO m | r        
//        if ( ref instanceof MappedReference ){
//            ((MappedReference)ref).setAttribute( attr );
//        }
        return ref;
    }
    
    public <T extends ReferenceableWSDLComponent> WSDLReference<T> build( 
            Class<T> clazz ,AbstractDocumentComponent entity , String refString )
    {
        if ( refString == null ){
            return null;
        }
        for (WSDLReferenceFactory resolver : myCollection) {
            if ( resolver.isApplicable( clazz )){
                return resolver.createUnresolvedReference( 
                        clazz , entity , refString );
            }
        }
        return null;
    }
    
    public <T extends ReferenceableWSDLComponent> WSDLReference<T> build( 
                T target ,Class<T> clazz , AbstractDocumentComponent entity  )
    {
        for (WSDLReferenceFactory resolver : myCollection) {
            if ( resolver.isApplicable( clazz )){
                return resolver.create( target , clazz , entity );
            }
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    public AttributesType.AttrType getAttributeType( Attribute attr ) {
        Class clazz = null;
        if ( List.class.isAssignableFrom( attr.getType() )){
            clazz = attr.getMemberType();
        }
        else {
            clazz = attr.getType();
        }
        for (WSDLReferenceFactory resolver : myCollection) {
            if ( resolver.isApplicable( clazz )){
                return resolver.getAttributeType();
            }
        }
        assert false;
        return null;
    }
    
//    public void setAttribute( WSDLReference ref , Attribute attr ) {
//            ref.setAttribute( attr );
//    }
    
    static Collection<WSDLModel> getWSDLModels( AbstractDocumentComponent entity, 
            String prefix ) 
    {
        assert entity instanceof TMapComponent;
        ExNamespaceContext context = ((TMapComponent)entity).getNamespaceContext();
        Collection<WSDLModel> models = getWSDLModels( 
                ((TMapComponent)entity).getModel(), 
                context.getNamespaceURI( prefix ) );
        return models;
    }
    
    
    
    static Collection<WSDLModel> getWSDLModels( TMapModel model , 
            String namespace )
    {
        return getInstance().getModels(model, namespace);
    }

    private Collection<WSDLModel> getModels( TMapModel model, String namespace )
    {
        Collection<WSDLModel> ret = new LinkedList<WSDLModel>();
        
        if ( myRetrievers.size() == 1) {
            return myRetrievers.iterator().next().getWSDLModels(model, namespace);
        }
        for ( ExternalModelRetriever retriever : myRetrievers ) {
            Collection<WSDLModel> collection = 
                retriever.getWSDLModels(model, namespace);
            ret.addAll( collection );
        }

        return ret;
    }
    
    interface WSDLResolver {
        <T extends ReferenceableWSDLComponent> T resolve(
                AbstractNamedComponentReference<T> reference );
    }

//////
///////*
////// * Could be consider to do this as service spi and move impls declarations 
////// * into service file.  
////// */
//////
interface WSDLReferenceFactory extends WSDLReferenceBuilder.WSDLResolver {

    <T extends ReferenceableWSDLComponent> boolean isApplicable( Class<T> clazz );

    <T extends ReferenceableWSDLComponent> WSDLReference<T> create(
            T target, Class<T> clazz, AbstractDocumentComponent entity );

    <T extends ReferenceableWSDLComponent> WSDLReference<T> 
        createUnresolvedReference( Class<T> clazz, 
                AbstractDocumentComponent entity, String refString );

    <T extends ReferenceableWSDLComponent> WSDLReference<T> create(
            T target, Class<T> clazz, AbstractDocumentComponent entity,
            String refString );
    
    AttributesType.AttrType getAttributeType();

}

abstract class AbstractGlobalReferenceFactory implements WSDLReferenceFactory {

    public <T extends ReferenceableWSDLComponent> WSDLReference<T>
            createUnresolvedReference( Class<T> clazz,
            AbstractDocumentComponent entity,
            String refString ) 
    {
        return new GlobalWSDLReferenceImpl<T>(clazz, entity, refString, this);
    }
    
    public <T extends ReferenceableWSDLComponent> WSDLReference<T> create( 
                T target, Class<T> clazz, AbstractDocumentComponent entity, 
                String refString )
    {
        return create( target, clazz, entity );
    }
    
    public <T extends ReferenceableWSDLComponent> WSDLReference<T> create( 
                T target, Class<T> clazz, AbstractDocumentComponent entity )
    {
        return new GlobalWSDLReferenceImpl<T>( target, clazz, entity, this);
    }
    
    public AttributesType.AttrType getAttributeType() {
        return AttributesType.AttrType.QNAME;
    }    
}

/**
 * This abstract class ONLY for Referenceable elements in WSDL that is Namable.
 * For others one need to use another implementation.
 * 
 * Actually curently WSDL don't have not Nameble refrenceable elements.
 * @author ads
 *
 */
abstract class AbstractNamedReferenceFactory implements WSDLReferenceFactory {

    public <T extends ReferenceableWSDLComponent> WSDLReference<T> 
        createUnresolvedReference( Class<T> clazz, AbstractDocumentComponent entity, 
                String refString )
    {
        return new WSDLReferenceImpl<T>(null, clazz, entity, refString , this );
    }
    
    public <T extends ReferenceableWSDLComponent> WSDLReference<T> create( 
                T target,Class<T> clazz, AbstractDocumentComponent entity, 
                String refString )
    {
        return new WSDLReferenceImpl<T>( target, clazz, entity ,refString , this );
    }
    
    public <T extends ReferenceableWSDLComponent> WSDLReference<T> create( 
                T target, Class<T> clazz, AbstractDocumentComponent entity )
    {
        assert target instanceof Nameable;
        return new WSDLReferenceImpl<T>( target, clazz, entity , 
                ((Nameable) target ).getName(), this  );
    }
    
    public AttributesType.AttrType getAttributeType() {
        return AttributesType.AttrType.NCNAME;
    }
}

class PartnerLinkTypeResolver extends AbstractGlobalReferenceFactory {
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.xslt.tmap.model.impl.WSDLReferenceResolver#isApplicable(java.lang.Class)
         */
    public <T extends ReferenceableWSDLComponent> boolean isApplicable(
            Class<T> clazz ) {
        return PartnerLinkType.class.isAssignableFrom(clazz);
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.xslt.tmap.model.impl.WSDLReferenceResolver#resolve(java.lang.Class, org.netbeans.modules.bpel.model.impl.AbstractDocumentComponent, java.lang.String)
         */
    public <T extends ReferenceableWSDLComponent> T resolve(
            AbstractNamedComponentReference<T> reference ) 
    {
        String refString = reference.getRefString();
        Class<T> clazz = reference.getType();
        AbstractDocumentComponent entity = 
            (AbstractDocumentComponent) reference.getParent(); 
        
        String[] splited = new String[2];
        
        splitQName( refString , splited );
        
        Collection<WSDLModel> models = WSDLReferenceBuilder.getWSDLModels(entity, 
                splited[0] );
        for (WSDLModel model : models) {
            List<PartnerLinkType> list = model.getDefinitions()
                .getExtensibilityElements(PartnerLinkType.class);
            for (PartnerLinkType  partnerLink : list) {
                if ( splited[1].equals( partnerLink.getName()) ){
                    return clazz.cast(partnerLink);
                }
            }
        }
        return null;
    }

}

class RoleResolver extends AbstractNamedReferenceFactory {

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.tmap.model.impl.WSDLReferenceResolver#isApplicable(java.lang.Class)
     */
    public <T extends ReferenceableWSDLComponent> boolean isApplicable( 
            Class<T> clazz ) 
    {
        return Role.class.isAssignableFrom(clazz);
    }

    public <T extends ReferenceableWSDLComponent> T 
            resolve(AbstractNamedComponentReference<T> reference) 
    {
        String refString = reference.getRefString();
        Class<T> clazz = reference.getType();
        AbstractDocumentComponent entity = (AbstractDocumentComponent) reference
                .getParent();
        assert entity instanceof PartnerLinkTypeReference;
        WSDLReference<PartnerLinkType> pltRef = 
            ((PartnerLinkTypeReference) entity).getPartnerLinkType();
        if (pltRef == null) {
            return null;
        }
        PartnerLinkType partnerLinkType = pltRef.get();
        if (partnerLinkType == null) {
            return null;
        }
        Role role = partnerLinkType.getRole1();
        if ( role!=null && refString.equals( role.getName()) ){
            return clazz.cast(role);
        }
        role = partnerLinkType.getRole2();
        if ( role!= null && refString.equals( role.getName()) ){
            return clazz.cast(role);
        }
        return null;
    }

    
// TODO m | r    
//////    /* (non-Javadoc)
//////     * @see org.netbeans.modules.xslt.tmap.model.impl.WSDLReferenceResolver#resolve(java.lang.Class, org.netbeans.modules.bpel.model.impl.AbstractDocumentComponent, java.lang.String)
//////     */
//////    public <T extends ReferenceableWSDLComponent> T resolve( 
//////            AbstractNamedComponentReference<T> reference )
//////    {
//////        String refString = reference.getRefString();
//////        Class<T> clazz = reference.getType();
//////        AbstractDocumentComponent entity = (AbstractDocumentComponent) reference
//////                .getParent();
//////        assert entity instanceof PartnerLink;
//////        WSDLReference<PartnerLinkType> ref = 
//////            ((PartnerLink) entity).getPartnerLinkType();
//////        if (ref == null) {
//////            return null;
//////        }
//////        PartnerLinkType partnerLinkType = ref.get();
//////        if (partnerLinkType == null) {
//////            return null;
//////        }
//////        Role role = partnerLinkType.getRole1();
//////        if ( role!=null && refString.equals( role.getName()) ){
//////            return clazz.cast(role);
//////        }
//////        role = partnerLinkType.getRole2();
//////        if ( role!= null && refString.equals( role.getName()) ){
//////            return clazz.cast(role);
//////        }
//////        return null;
//////    }
    
}

class OperationResolver extends AbstractNamedReferenceFactory {

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.tmap.model.impl.WSDLReferenceResolver#isApplicable(java.lang.Class)
     */
    public <T extends ReferenceableWSDLComponent> boolean isApplicable( 
            Class<T> clazz ) 
    {
        return Operation.class.isAssignableFrom(clazz);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.tmap.model.impl.WSDLReferenceResolver#resolve(java.lang.Class, org.netbeans.modules.bpel.model.impl.AbstractDocumentComponent, java.lang.String)
     */
    public <T extends ReferenceableWSDLComponent> T resolve( 
            AbstractNamedComponentReference<T> reference )
    {
        String refString = reference.getRefString();
        Class<T> clazz = reference.getType();
        
        AbstractDocumentComponent entity = (AbstractDocumentComponent) reference
                .getParent();
        
        Collection<Operation> collection = null;
        
        if (entity instanceof org.netbeans.modules.xslt.tmap.model.api.Operation) {
            collection = resolveByTransformOperation(entity);
        }
        
        if (entity instanceof PartnerLinkTypeReference) {
            collection = resolveByPartnerLink(entity);
//            ((PartnerLinkTypeReference)entity)
        }
        
//        if (entity instanceof PortTypeReference) {
//            collection = resolveByPortType(entity);
//        }

////        if ( collection == null || collection.size()==0 ) {
////            collection = resolveByPartnerLink(entity);
////        }
////        
        if ( collection == null ){
            return null;
        }
        for (Operation operation : collection) {
            if ( refString.equals( operation.getName()) ){
                return clazz.cast(operation);
            }
        }
        return null;
    }

    private Collection<Operation> resolveByTransformOperation(
            AbstractDocumentComponent entity) 
    {
        Component parent = entity == null ? null : entity.getParent();
        if (parent == null) {
            return null;
        }
        
        assert parent instanceof AbstractDocumentComponent;
        return resolveByPartnerLink((AbstractDocumentComponent)parent);
    }
    
    private Collection<Operation> resolveByPartnerLink( 
            AbstractDocumentComponent entity ) 
    {
        if ( ! (entity instanceof PartnerLinkTypeReference) ){
            return null;
        }
        Collection<Operation> collection;
//        WSDLReference<PartnerLinkType> ref = (( PartnerLinkTypeReference)entity ).
//            getPartnerLinkType();
        
        WSDLReference<Role> refRole = (( PartnerLinkTypeReference)entity ).getRole();
        
        Role role = null;
        if (refRole != null) {
            role = refRole.get();
        }
        
        PortType wsdlPortType = null;
        if (role != null) {
            NamedComponentReference<PortType> portTypeRef = role.getPortType();
            if (portTypeRef != null) {
                wsdlPortType = portTypeRef.get();
            }
        }

        if ( wsdlPortType == null ){
            return null;
        }
        collection = wsdlPortType.getOperations();
        return collection;
    }

////    private Collection<Operation> resolveByPortType( 
////            AbstractDocumentComponent entity ) 
////    {
////        Collection<Operation> collection;
////        WSDLReference<PortType> ref = ((PortTypeReference) entity)
////                .getPortType();
////        if (ref == null) {
////            return null;
////        }
////        PortType portType = ref.get();
////        if (portType == null) {
////            return null;
////        }
////        collection = portType.getOperations();
////        return collection;
////    }
}

class PartResolver extends AbstractGlobalReferenceFactory {
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.xslt.tmap.model.impl.WSDLReferenceResolver#isApplicable(java.lang.Class)
         */
    public <T extends ReferenceableWSDLComponent> boolean isApplicable(
            Class<T> clazz ) {
        return Part.class.isAssignableFrom(clazz);
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.xslt.tmap.model.impl.WSDLReferenceResolver#resolve(java.lang.Class, org.netbeans.modules.bpel.model.impl.AbstractDocumentComponent, java.lang.String)
         */
    public <T extends ReferenceableWSDLComponent> T resolve(
            AbstractNamedComponentReference<T> reference ) 
    {
        String refString = reference.getRefString();
        Class<T> clazz = reference.getType();
        AbstractDocumentComponent parentComponent = reference.getParent(); 
        
        Part part = null;
        if (parentComponent instanceof Transform) {
            part = resolveByTransform(parentComponent, refString);
        }
        
        if (parentComponent instanceof Param) {
            part = resolveByParam(parentComponent, refString);
        }
        return part != null ? clazz.cast(part) : null;
    }
    
    private Part resolveByParam( 
            AbstractDocumentComponent component, String refStr ) 
    {
        if ( ! (component instanceof Param) || refStr == null){
            return null;
        }

        return resolveByVarRef((( Param)component ).getVariableReference(), refStr);
    }

    private Part resolveByTransform( 
            AbstractDocumentComponent component, String refStr ) 
    {
        if ( ! (component instanceof Transform) || refStr == null){
            return null;
        }

        VariableReference varRef = ((Transform)component).getResult();
        if (varRef == null || !refStr.equals(varRef.getRefString())) {
            varRef = ((Transform)component).getSource();
        }
        if (!refStr.equals(varRef.getRefString())) {
            return null;
        }
        
        return resolveByVarRef(varRef, refStr);
    }

    private Part resolveByVarRef( 
            VariableReference varRef, String refStr ) 
    {
        if (varRef == null) {
            return null;
        }
        
        Part part = null;
        
        Variable var = varRef == null ? null : varRef.getReferencedVariable();
        Message message = null;
        if (var != null) {
            Reference<Message> messageRef = var.getMessage();
            message = messageRef == null ? null : messageRef.get();
        }
        
        Collection<Part> parts = null;
        if (message != null) {
            parts  = message.getParts();
        }
        
        String partRefStr = VariableReferenceImpl.getPartName(refStr);
        if (parts != null && partRefStr != null) {
            for (Part partElem : parts) {
                if (partElem != null && partRefStr.equals(partElem.getName())) {
                    part = partElem;
                    break;
                }
            }
        }
        
        return part;
    }
}

//////
//////class OperationResolver extends AbstractNamedReferenceFactory {
//////
//////    /* (non-Javadoc)
//////     * @see org.netbeans.modules.xslt.tmap.model.impl.WSDLReferenceResolver#isApplicable(java.lang.Class)
//////     */
//////    public <T extends ReferenceableWSDLComponent> boolean isApplicable( 
//////            Class<T> clazz ) 
//////    {
//////        return Operation.class.isAssignableFrom(clazz);
//////    }
//////
//////    /* (non-Javadoc)
//////     * @see org.netbeans.modules.xslt.tmap.model.impl.WSDLReferenceResolver#resolve(java.lang.Class, org.netbeans.modules.bpel.model.impl.AbstractDocumentComponent, java.lang.String)
//////     */
//////    public <T extends ReferenceableWSDLComponent> T resolve( 
//////            AbstractNamedComponentReference<T> reference )
//////    {
//////        String refString = reference.getRefString();
//////        Class<T> clazz = reference.getType();
//////        AbstractDocumentComponent entity = (AbstractDocumentComponent) reference
//////                .getParent();
//////        
//////        Collection<Operation> collection = null;
//////        if (entity instanceof PortTypeReference) {
//////            collection = resolveByPortType(entity);
//////        }
//////
//////        if ( collection == null || collection.size()==0 ) {
//////            collection = resolveByPartnerLink(entity);
//////        }
//////        
//////        if ( collection == null ){
//////            return null;
//////        }
//////        for (Operation operation : collection) {
//////            if ( refString.equals( operation.getName()) ){
//////                return clazz.cast(operation);
//////            }
//////        }
//////        return null;
//////    }
//////
//////    private Collection<Operation> resolveByPartnerLink( 
//////            AbstractDocumentComponent entity ) 
//////    {
//////        if ( ! (entity instanceof PartnerLinkReference) ){
//////            return null;
//////        }
//////        Collection<Operation> collection;
//////        BpelReference<PartnerLink> ref = (( PartnerLinkReference )entity ).
//////            getPartnerLink();
//////        
//////        NamedComponentReference<PortType> portTypeRef = 
//////            Utils.getPortTypeRef( ref , entity );
//////        if ( portTypeRef == null ){
//////            return null;
//////        }
//////        PortType wsdlPortType = portTypeRef.get();
//////        if ( wsdlPortType == null ){
//////            return null;
//////        }
//////        collection = wsdlPortType.getOperations();
//////        return collection;
//////    }
//////
//////    private Collection<Operation> resolveByPortType( 
//////            AbstractDocumentComponent entity ) 
//////    {
//////        Collection<Operation> collection;
//////        WSDLReference<PortType> ref = ((PortTypeReference) entity)
//////                .getPortType();
//////        if (ref == null) {
//////            return null;
//////        }
//////        PortType portType = ref.get();
//////        if (portType == null) {
//////            return null;
//////        }
//////        collection = portType.getOperations();
//////        return collection;
//////    }

    public static void splitQName( String qName , String[] result ){
        assert qName!=null;
        assert result != null;
        String[] parts = qName.split(":"); //NOI18N
        String prefix;
        String localName;
        if (parts.length == 2) {
            prefix = parts[0];
            localName = parts[1];
        } else {
            prefix = null;
            localName = parts[0];
        }
        if ( result.length >0 ){
            result[0] = prefix;
        }
        if ( result.length >1 ){
            result[1]=localName;
        }
    }

}
