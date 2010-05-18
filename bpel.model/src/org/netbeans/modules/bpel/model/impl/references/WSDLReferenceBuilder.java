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

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.FromPart;
import org.netbeans.modules.bpel.model.api.FromPartContainer;
import org.netbeans.modules.bpel.model.api.Invoke;
import org.netbeans.modules.bpel.model.api.OnEvent;
import org.netbeans.modules.bpel.model.api.OperationReference;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.PartnerLinkReference;
import org.netbeans.modules.bpel.model.api.PortTypeReference;
import org.netbeans.modules.bpel.model.api.Reply;
import org.netbeans.modules.bpel.model.api.ToPart;
import org.netbeans.modules.bpel.model.api.ToPartContainer;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.VariableReference;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.model.api.references.MappedReference;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.xml.xpath.ext.schema.ExNamespaceContext;
import org.netbeans.modules.bpel.model.impl.BpelEntityImpl;
import org.netbeans.modules.bpel.model.api.support.Utils;
import org.netbeans.modules.bpel.model.xam.spi.ExternalModelRetriever;
import org.netbeans.modules.xml.wsdl.model.Fault;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.CorrelationProperty;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;
import org.netbeans.modules.xml.xam.Nameable;
import org.netbeans.modules.xml.xam.NamedReferenceable;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;

/**
 * @author ads
 */
public final class WSDLReferenceBuilder {
    
    private WSDLReferenceBuilder() {
        Result result = Lookup.getDefault().lookup(new Lookup.Template(ExternalModelRetriever.class));
        myRetrievers = result.allInstances();
        
        myCollection = new ArrayList<WSDLReferenceFactory>();
        myCollection.add( new PartResolver() );
        myCollection.add( new MessageResolver() );
        myCollection.add( new PartnerLinkTypeResolver() );
        myCollection.add( new PortTypeResolver() );
        myCollection.add( new CorrelationPropertyResolver() );
        myCollection.add( new RoleResolver() );
        myCollection.add( new OperationResolver() );
    }
    
    public static WSDLReferenceBuilder getInstance(){
        return INSTANCE;
    }
    
    public <T extends ReferenceableWSDLComponent> 
        org.netbeans.modules.bpel.model.impl.references.WSDLReference<T> build( 
            Class<T> clazz , BpelEntityImpl entity , Attribute attr )
    {
        org.netbeans.modules.bpel.model.impl.references.WSDLReference<T> ref = 
            build( clazz , entity , entity.getAttribute( attr ) );
        if ( ref instanceof MappedReference ){
            ((MappedReference)ref).setAttribute( attr );
        }
        return ref;
    }
    
    public <T extends ReferenceableWSDLComponent> 
        org.netbeans.modules.bpel.model.impl.references.WSDLReference<T> build( 
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
    
    public <T extends ReferenceableWSDLComponent> 
        org.netbeans.modules.bpel.model.impl.references.WSDLReference<T> build( 
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
    public BpelAttributesType.AttrType getAttributeType( Attribute attr ) {
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
    
    public void setAttribute( WSDLReference ref , Attribute attr ) {
        if ( ref instanceof MappedReference ) {
            ((MappedReference)ref).setAttribute( attr );
        }
    }
    
    static Collection<WSDLModel> getWSDLModels(AbstractDocumentComponent entity, String prefix) {
        assert entity instanceof BpelEntity;
//System.out.println();
//System.out.println();
//System.out.println("===== GET WSDL models: " + prefix +" " + entity);
        ExNamespaceContext context = ((BpelEntity)entity).getNamespaceContext();
//System.out.println("              context: " + context);
        return getWSDLModels(((BpelEntity)entity).getBpelModel(), context.getNamespaceURI( prefix ) );
    }
    
    static Collection<WSDLModel> getWSDLModels(BpelModel model, String namespace) {
        return getInstance().getModels(model, namespace);
    }

    private Collection<WSDLModel> getModels(BpelModel model, String namespace) {
//System.out.println();
//System.out.println("===== get wsdl models: " + namespace +" " + model);
        Collection<WSDLModel> ret = new ArrayList<WSDLModel>();

        if (myRetrievers.size() == 1) {
//System.out.println("===== 11");
            return ((ExternalModelRetriever)myRetrievers.iterator().next()).getWSDLModels(model, namespace);
        }
        for (Object obj : myRetrievers) {
//System.out.println("=====   see: " + obj);
            ExternalModelRetriever retriever = (ExternalModelRetriever) obj;
            Collection<WSDLModel> collection = retriever.getWSDLModels(model, namespace);
//System.out.println("=====        size: " + collection.size());
            ret.addAll(collection);
        }
        return ret;
    }
    
    interface WSDLResolver {
        <T extends ReferenceableWSDLComponent> T resolve(
                AbstractNamedComponentReference<T> reference );
    }
        

    private static final WSDLReferenceBuilder INSTANCE = new WSDLReferenceBuilder();
    
    private static Collection myRetrievers;
    
    private Collection<WSDLReferenceFactory> myCollection;
}

/*
 * Could be consider to do this as service spi and move impls declarations 
 * into service file.  
 */

interface WSDLReferenceFactory extends WSDLReferenceBuilder.WSDLResolver {

    <T extends ReferenceableWSDLComponent> boolean isApplicable( Class<T> clazz );

    <T extends ReferenceableWSDLComponent> 
        org.netbeans.modules.bpel.model.impl.references.WSDLReference<T> create(
            T target, Class<T> clazz, AbstractDocumentComponent entity );

    <T extends ReferenceableWSDLComponent> 
        org.netbeans.modules.bpel.model.impl.references.WSDLReference<T> 
        createUnresolvedReference( Class<T> clazz, 
                AbstractDocumentComponent entity, String refString );

    <T extends ReferenceableWSDLComponent> 
        org.netbeans.modules.bpel.model.impl.references.WSDLReference<T> create(
            T target, Class<T> clazz, AbstractDocumentComponent entity,
            String refString );
    
    BpelAttributesType.AttrType getAttributeType();

}

abstract class AbstractGlobalReferenceFactory implements WSDLReferenceFactory {

    public <T extends ReferenceableWSDLComponent> 
        org.netbeans.modules.bpel.model.impl.references.WSDLReference<T> 
        createUnresolvedReference( Class<T> clazz, AbstractDocumentComponent entity,
                String refString )
    {
        return new GlobalWSDLReferenceImpl<T>(clazz, entity, refString , this );
    }
    
    public <T extends ReferenceableWSDLComponent> 
        org.netbeans.modules.bpel.model.impl.references.WSDLReference<T> create( 
                T target, Class<T> clazz, AbstractDocumentComponent entity, 
                String refString )
    {
        return create( target, clazz, entity );
    }
    
    public <T extends ReferenceableWSDLComponent> 
        org.netbeans.modules.bpel.model.impl.references.WSDLReference<T> create( 
                T target, Class<T> clazz, AbstractDocumentComponent entity )
    {
        return new GlobalWSDLReferenceImpl<T>( target, clazz, entity , this );
    }
    
    public BpelAttributesType.AttrType getAttributeType() {
        return BpelAttributesType.AttrType.QNAME;
    }    
    
}

/**
 * This abstract class ONLY for Referenceable elements in WSDL that is Namable.
 * For others one need to use another implementation.
 * 
 * Actually curently WSDL don't have not Nameble refrenceable elements.
 * But BPEL OM f.e. have such elements.  
 * @author ads
 *
 */
abstract class AbstractNamedReferenceFactory implements WSDLReferenceFactory {

    public <T extends ReferenceableWSDLComponent> 
        org.netbeans.modules.bpel.model.impl.references.WSDLReference<T> 
        createUnresolvedReference( Class<T> clazz, AbstractDocumentComponent entity, 
                String refString )
    {
        return new WSDLReferenceImpl<T>(null, clazz, entity, refString , this );
    }
    
    public <T extends ReferenceableWSDLComponent> 
        org.netbeans.modules.bpel.model.impl.references.WSDLReference<T> create( 
                T target,Class<T> clazz, AbstractDocumentComponent entity, 
                String refString )
    {
        return new WSDLReferenceImpl<T>( target, clazz, entity ,refString , this );
    }
    
    public <T extends ReferenceableWSDLComponent> 
        org.netbeans.modules.bpel.model.impl.references.WSDLReference<T> create( 
                T target, Class<T> clazz, AbstractDocumentComponent entity )
    {
        assert target instanceof Nameable;
        return new WSDLReferenceImpl<T>( target, clazz, entity , 
                ((Nameable) target ).getName(), this  );
    }
    
    public BpelAttributesType.AttrType getAttributeType() {
        return BpelAttributesType.AttrType.NCNAME;
    }
}

class PartResolver extends AbstractNamedReferenceFactory {

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.WSDLReferenceResplver#isApplicable(java.lang.Class)
     */
    public <T extends ReferenceableWSDLComponent> boolean isApplicable( 
            Class<T> clazz ) 
    {
        return Part.class.isAssignableFrom(clazz);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.WSDLReferenceResplver#resolve(java.lang.Class, org.netbeans.modules.bpel.model.api.BpelEntity, java.lang.String)
     */
//    public <T extends ReferenceableWSDLComponent> T resolve( 
//            AbstractNamedComponentReference<T> reference ) 
//    {
//        AbstractDocumentComponent entity = 
//            (AbstractDocumentComponent)reference.getParent();
//        String refString = reference.getRefString();
//        Class<T> clazz = reference.getType();
//        
//        BpelReference<VariableDeclaration> ref = null;
//        if (entity instanceof VariableReference) {
//            ref = ((VariableReference) entity).getVariable();
//        }
//        else if (entity instanceof FromPart) {
//            ref = ((FromPart) entity).getToVariable();
//        }
//        else if (entity instanceof ToPart) {
//            ref = ((ToPart) entity).getFromVariable();
//        }
//        if (ref == null) {
//            return null;
//        }
//        VariableDeclaration decl = ref.get();
//        if ( decl == null ){
//            return null;
//        }
//        WSDLReference<Message> wsdlRef = decl.getMessageType();
//        if (wsdlRef == null) {
//            return null;
//        }
//        Message message = wsdlRef.get();
//        if (message == null) {
//            return null;
//        }
//        T result = null;
//        for (NamedReferenceable referenceable : message.getParts()) {
//            if (refString.equals(referenceable.getName())) {
//                result = clazz.cast(referenceable);
//                break;
//            }
//        }
//        return result;
//    }
    

    public <T extends ReferenceableWSDLComponent> T resolve( 
            AbstractNamedComponentReference<T> reference ) 
    {
        AbstractDocumentComponent entity = 
            (AbstractDocumentComponent)reference.getParent();
        String refString = reference.getRefString();
        Class<T> clazz = reference.getType();
        
        Message message = getMessage(entity);
        if (message == null) {
            return null;
        }
        
        T result = null;
        for (NamedReferenceable referenceable : message.getParts()) {
            if (refString.equals(referenceable.getName())) {
                result = clazz.cast(referenceable);
                break;
            }
        }
        return result;
    }
    
    

    private Message getMessage(AbstractDocumentComponent entity) {
        if (entity == null) {
            return null;
        } if (entity instanceof VariableReference) {
            BpelReference<VariableDeclaration> reference = ((VariableReference) 
                    entity).getVariable();
            if (reference == null) {
                return null;
            }
            
            VariableDeclaration variableDeclaration = reference.get();
            if (variableDeclaration == null) {
                return null;
            }
            
            WSDLReference<Message> messageReference = variableDeclaration
                    .getMessageType();
            return (messageReference == null) ? null : messageReference.get();
        } if (entity instanceof FromPart) {
            FromPartContainer fromPartContainer = (FromPartContainer) entity
                    .getParent();
            if (fromPartContainer == null) {
                return null;
            }
            
            OperationReference operationReference = (OperationReference) 
                    fromPartContainer.getParent();

            Operation operation = getOperation(operationReference);
            
            return (operationReference instanceof Invoke) 
                    ? getOutputMessage(operation)
                    : getInputMessage(operation);
        } else if (entity instanceof ToPart) {
            ToPartContainer toPartContainer = (ToPartContainer) entity
                    .getParent();
            if (toPartContainer == null) {
                return null;
            }
            
            OperationReference operationReference = (OperationReference)
                    toPartContainer.getParent();

            Operation operation = getOperation(operationReference);
            
            if (operationReference == null) {
                return null;
            } 
            
            if (operationReference instanceof Invoke) {
                return getInputMessage(operation);
            }
            
            if (operationReference instanceof Reply) {
                Reply reply = (Reply) operationReference;
                QName faultName = reply.getFaultName();
                
                if (faultName != null) {
                    return getFaultMessage(operation, faultName);
                }
            }
            
            return getOutputMessage(operation);
        }
        
        return null;
    }
    
    
    private Operation getOperation(OperationReference reference) {
        if (reference == null) {
            return null;
        }
        
        WSDLReference<Operation> operationReference = reference.getOperation();
        return (operationReference == null) ? null : operationReference.get();
    }
    
    
    private Message getFaultMessage(Operation operation, QName faultName) {
        if (operation == null) {
            return null;
        }
        
        if (faultName == null) {
            return null;
        }
        
        String faultNameLocalPart = faultName.getLocalPart();
        if (faultNameLocalPart == null) {
            return null;
        }
        
        Collection<Fault> faults = operation.getFaults();
        if (faults == null || faults.isEmpty()) {
            return null;
        }
        
        Message message = null;
        
        for (Fault fault : faults) {
            String name = fault.getName();
            if (name != null && name.equals(faultNameLocalPart)) {
                NamedComponentReference<Message> messageReference = fault
                        .getMessage();
                
                message = (messageReference == null) ? null
                        : messageReference.get();
                
                if (message != null) {
                    break;
                }
            }
        }
        
        return message;
    }
    
    
    private Message getInputMessage(Operation operation) {
        if (operation == null) {
            return null;
        }
        
        Input input = operation.getInput();
        if (input == null) {
            return null;
        }
        
        NamedComponentReference<Message> messageReference = input.getMessage();
        return (messageReference == null) ? null : messageReference.get();
    }
    
    
    private Message getOutputMessage(Operation operation) {
        if (operation == null) {
            return null;
        }
        
        Output output = operation.getOutput();
        if (output == null) {
            return null;
        }
        
        NamedComponentReference<Message> messageReference = output.getMessage();
        return (messageReference == null) ? null : messageReference.get();
    }
}

class MessageResolver extends AbstractGlobalReferenceFactory {

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.WSDLReferenceResplver#isApplicable(java.lang.Class)
     */
    public <T extends ReferenceableWSDLComponent> boolean isApplicable( 
            Class<T> clazz ) 
    {
        return Message.class.isAssignableFrom(clazz);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.WSDLReferenceResplver#resolve(java.lang.Class, org.netbeans.modules.bpel.model.impl.AbstractDocumentComponent, java.lang.String)
     */
    public <T extends ReferenceableWSDLComponent> T resolve( 
            AbstractNamedComponentReference<T> reference ) 
    {
        String refString = reference.getRefString();
        Class<T> clazz = reference.getType();
        AbstractDocumentComponent entity = (AbstractDocumentComponent) reference.getParent();
        
        if (entity instanceof FromPart && entity.getParent() instanceof OnEvent) {
            OnEvent onEvent = (OnEvent) entity.getParent();
            return clazz.cast(resolve(onEvent));
        }
        String[] splited = new String[2];
        Utils.splitQName( refString , splited );
        Collection<WSDLModel> models = WSDLReferenceBuilder.getWSDLModels(entity, splited[0] );
        for (WSDLModel model : models) {
            Collection<Message> collection = model.getDefinitions().getMessages();
            for (Message message : collection) {
                if ( splited[1].equals( message.getName()) ){
                    return clazz.cast(message);
                }
            }
        }
        return null;
    }
    
    private Message resolve( OnEvent onEvent ) {
           WSDLReference<Operation> opRef = onEvent.getOperation();
           if ( opRef == null ) {
               return null;
           }
           Operation operation = opRef.get();
           if ( operation == null ) {
               return null;
           }
           Input input = operation.getInput();
           if ( input == null ) {
               return null;
           }
           NamedComponentReference<Message> messageRef = input.getMessage();
           if ( messageRef == null ) {
               return null;
           }
           return messageRef.get();
       }

}

class PartnerLinkTypeResolver extends AbstractGlobalReferenceFactory {

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.WSDLReferenceResplver#isApplicable(java.lang.Class)
     */
    public <T extends ReferenceableWSDLComponent> boolean isApplicable( 
            Class<T> clazz ) 
    {
        return PartnerLinkType.class.isAssignableFrom(clazz);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.WSDLReferenceResplver#resolve(java.lang.Class, org.netbeans.modules.bpel.model.impl.AbstractDocumentComponent, java.lang.String)
     */
    public <T extends ReferenceableWSDLComponent> T resolve( 
            AbstractNamedComponentReference<T> reference ) 
    {
        String refString = reference.getRefString();
        Class<T> clazz = reference.getType();
        AbstractDocumentComponent entity = 
            (AbstractDocumentComponent) reference.getParent(); 
        
        String[] splited = new String[2];
        Utils.splitQName( refString , splited );
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

class PortTypeResolver extends AbstractGlobalReferenceFactory {

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.WSDLReferenceResplver#isApplicable(java.lang.Class)
     */
    public <T extends ReferenceableWSDLComponent> boolean isApplicable( 
            Class<T> clazz ) 
    {
        return PortType.class.isAssignableFrom(clazz);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.WSDLReferenceResplver#resolve(java.lang.Class, org.netbeans.modules.bpel.model.impl.AbstractDocumentComponent, java.lang.String)
     */
    public <T extends ReferenceableWSDLComponent> T resolve(AbstractNamedComponentReference<T> reference) {
//System.out.println();
//System.out.println();
//System.out.println();
//System.out.println("@@@@@@@ RESOLVE PORT TYPE: " + reference + " " + reference.getClass().getName());
        String refString = reference.getRefString();
//System.out.println("    ++ refString: " + refString);
        Class<T> clazz = reference.getType();
//System.out.println("    ++ clazz: " + clazz);
        AbstractDocumentComponent entity = (AbstractDocumentComponent) reference.getParent(); 
//System.out.println("    ++ entity: " + entity);
        String[] splited = new String[2];
        Utils.splitQName(refString, splited);
//System.out.println("    ++ splited: " + splited[0] + " " + splited[1]);
        Collection<WSDLModel> models = WSDLReferenceBuilder.getWSDLModels(entity, splited[0]);
//System.out.println("    ++ 1: " + models.size());

        for (WSDLModel model : models) {
//System.out.println("    ++ see model: " + model);
            Collection<PortType> collection = model.getDefinitions().getPortTypes();

            for (PortType portType : collection) {
//System.out.println("    ++ see portType: " + portType);
                if (splited[1].equals(portType.getName())) {
//System.out.println("    ++: " + clazz.cast(portType));
                    return clazz.cast(portType);
                }
            }
        }
//System.out.println("    ++.");
        return null;
    }
}

class CorrelationPropertyResolver extends AbstractGlobalReferenceFactory {

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.WSDLReferenceResplver#isApplicable(java.lang.Class)
     */
    public <T extends ReferenceableWSDLComponent> boolean isApplicable( 
            Class<T> clazz ) 
    {
        return CorrelationProperty.class.isAssignableFrom(clazz);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.WSDLReferenceResplver#resolve(java.lang.Class, org.netbeans.modules.bpel.model.impl.AbstractDocumentComponent, java.lang.String)
     */
    public <T extends ReferenceableWSDLComponent> T resolve( 
            AbstractNamedComponentReference<T> reference )
    {
        String refString = reference.getRefString();
        Class<T> clazz = reference.getType();
        AbstractDocumentComponent entity = (AbstractDocumentComponent) reference
                .getParent();
                
        String[] splited = new String[2];
        Utils.splitQName( refString , splited );
        Collection<WSDLModel> models = WSDLReferenceBuilder.getWSDLModels(entity, 
                splited[0] );
        for (WSDLModel model : models) {
            List<CorrelationProperty> list = model.getDefinitions()
                .getExtensibilityElements(CorrelationProperty.class);
            for (CorrelationProperty  property : list) {
                if ( splited[1].equals( property.getName()) ){
                    return clazz.cast(property);
                }
            }
        }
        return null;
    }

}

class RoleResolver extends AbstractNamedReferenceFactory {

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.WSDLReferenceResplver#isApplicable(java.lang.Class)
     */
    public <T extends ReferenceableWSDLComponent> boolean isApplicable( 
            Class<T> clazz ) 
    {
        return Role.class.isAssignableFrom(clazz);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.WSDLReferenceResplver#resolve(java.lang.Class, org.netbeans.modules.bpel.model.impl.AbstractDocumentComponent, java.lang.String)
     */
    public <T extends ReferenceableWSDLComponent> T resolve(AbstractNamedComponentReference<T> reference) {
//System.out.println();
//System.out.println();
//System.out.println();
//System.out.println("@@@@@@@ RESOLVE ROLE: " + reference);
        String refString = reference.getRefString();
        Class<T> clazz = reference.getType();
        AbstractDocumentComponent entity = (AbstractDocumentComponent) reference.getParent();
        assert entity instanceof PartnerLink;
        WSDLReference<PartnerLinkType> ref = ((PartnerLink) entity).getPartnerLinkType();
//System.out.println("   1");

        if (ref == null) {
            return null;
        }
//System.out.println("   2");
        PartnerLinkType partnerLinkType = ref.get();
        
        if (partnerLinkType == null) {
            return null;
        }
//System.out.println("   3");
        Role role = partnerLinkType.getRole1();
        
        if ( role!=null && refString.equals( role.getName()) ){
            return clazz.cast(role);
        }
//System.out.println("   4");
        role = partnerLinkType.getRole2();
        
        if ( role!= null && refString.equals( role.getName()) ){
            return clazz.cast(role);
        }
//System.out.println("   5");
        return null;
    }
    
}

class OperationResolver extends AbstractNamedReferenceFactory {

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.WSDLReferenceResplver#isApplicable(java.lang.Class)
     */
    public <T extends ReferenceableWSDLComponent> boolean isApplicable( 
            Class<T> clazz ) 
    {
        return Operation.class.isAssignableFrom(clazz);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.impl.WSDLReferenceResplver#resolve(java.lang.Class, org.netbeans.modules.bpel.model.impl.AbstractDocumentComponent, java.lang.String)
     */
    public <T extends ReferenceableWSDLComponent> T resolve(AbstractNamedComponentReference<T> reference) {
//System.out.println();
//System.out.println();
//System.out.println();
//System.out.println("@@@@@@@ RESOLVE OPERATION: " + reference + " " + reference.getClass().getName());
        String refString = reference.getRefString();
        Class<T> clazz = reference.getType();
        AbstractDocumentComponent entity = (AbstractDocumentComponent) reference.getParent();
        Collection<Operation> collection = null;

//System.out.println("   entity " + entity + " " + entity.getClass().getName());
        if (entity instanceof PortTypeReference) {
            collection = resolveByPortType(entity);
//System.out.println("   1: " + collection);
        }
//System.out.println("   2");
        if (collection == null || collection.size()==0 ) {
            collection = resolveByPartnerLink(entity);
//System.out.println("   2: " + collection);
        }
//System.out.println("   3");
        if (collection == null) {
            return null;
        }
//System.out.println("   4");
        for (Operation operation : collection) {
            if ( refString.equals( operation.getName()) ){
                return clazz.cast(operation);
            }
        }
        return null;
    }

    private Collection<Operation> resolveByPartnerLink(AbstractDocumentComponent entity) {
//System.out.println();
//System.out.println("   << ResolveByPartnerLink: " + entity + " " + entity.getClass().getName());

        if ( ! (entity instanceof PartnerLinkReference) ){
            return null;
        }
//System.out.println("    << 1");
        Collection<Operation> collection;
        BpelReference<PartnerLink> ref = ((PartnerLinkReference) entity).getPartnerLink();
        NamedComponentReference<PortType> portTypeRef = Utils.getPortTypeRef(ref, entity);
//System.out.println("    << 2");

        if (portTypeRef == null ){
            return null;
        }
//System.out.println("    << 3");
        PortType wsdlPortType = portTypeRef.get();

        if ( wsdlPortType == null ){
            return null;
        }
//System.out.println("    << 4");
        return wsdlPortType.getOperations();
    }

    private Collection<Operation> resolveByPortType(AbstractDocumentComponent entity) {
//System.out.println();
//System.out.println("   << ResolveByPortType: " + entity + " " + entity.getClass().getName());
        WSDLReference<PortType> ref = ((PortTypeReference) entity).getPortType();

//System.out.println("    << 1: " + ref.getClass().getName());

        if (ref == null) {
//System.out.println("    << 1");
            return null;
        }
        PortType portType = ref.get();
//System.out.println("    << 2: " + portType);

        if (portType == null) {
//System.out.println("    << 2");
            return null;
        }
//System.out.println("    << 3: " + portType.getOperations());
        return portType.getOperations();
    }
}
