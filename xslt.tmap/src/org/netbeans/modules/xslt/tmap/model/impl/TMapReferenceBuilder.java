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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.xam.AbstractComponent;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.netbeans.modules.xslt.tmap.model.api.Invoke;
import org.netbeans.modules.xslt.tmap.model.api.MappedReference;
import org.netbeans.modules.xslt.tmap.model.api.Operation;
import org.netbeans.modules.xslt.tmap.model.api.Param;
import org.netbeans.modules.xslt.tmap.model.api.TMapAttributes;
import org.netbeans.modules.xslt.tmap.model.api.TMapReference;
import org.netbeans.modules.xslt.tmap.model.api.TMapReferenceable;
import org.netbeans.modules.xslt.tmap.model.api.Transform;
import org.netbeans.modules.xslt.tmap.model.api.Variable;
import org.netbeans.modules.xslt.tmap.model.api.VariableDeclarator;
import org.netbeans.modules.xslt.tmap.model.api.VariableReference;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class TMapReferenceBuilder {

    private TMapReferenceBuilder() {
        myCollection = new LinkedList<TMapReferenceFactory>();
        myCollection.add( new VariableReferenceFactory() );
    }
    
    public static TMapReferenceBuilder getInstance() {
        return INSTANCE;
    }
    
    public <T extends TMapReferenceable> TMapReference<T> 
            build( Class<T> clazz ,AbstractDocumentComponent component , 
                    Attribute attr )
    {
        TMapReference<T> ref = build( clazz , component , component.getAttribute( attr ));
        if ( ref instanceof MappedReference ) {
            ((MappedReference)ref).setAttribute( attr );
        }
        return ref;
    }
    
    public <T extends TMapReferenceable> TMapReference<T> build( 
            Class<T> clazz ,AbstractComponent component , String refString )
    {
        if ( refString == null ){
            return null;
        }
        for (TMapReferenceFactory resolver : myCollection) {
            if ( resolver.isApplicable( clazz )){
                return resolver.createUnresolvedReference( clazz , component , 
                        refString );
            }
        }
        return null;
    }
    
    public <T extends TMapReferenceable> TMapReference<T> build( 
            T target, Attribute targetAttr, Class<T> clazz , 
            AbstractComponent component, Part part)
    {
        for (TMapReferenceFactory resolver : myCollection) {
            if ( resolver.isApplicable( clazz )){
                return resolver.create( target, targetAttr, clazz , component, part );
            }
        }
        return null;
    }
    
    public void setAttribute( TMapReference ref , Attribute attr ) {
        if ( ref instanceof MappedReference ) {
            ((MappedReference)ref).setAttribute( attr );
        }
    }
    
    public AttributesType.AttrType getAttributeType( Attribute attr ) {
        return AttributesType.AttrType.NCNAME;
    }
    
    interface TMapResolver {
        <T extends TMapReferenceable> T resolve( AbstractReference<T> ref );
        <T extends TMapReferenceable> boolean haveRefString( 
                Reference<T> ref , T component );
    }

    private static final TMapReferenceBuilder INSTANCE = new TMapReferenceBuilder();
    
    private Collection<TMapReferenceFactory> myCollection;
}


interface TMapReferenceFactory extends TMapReferenceBuilder.TMapResolver {
    
    <T extends TMapReferenceable> boolean isApplicable( Class<T> clazz);
    
    <T extends TMapReferenceable> TMapReference<T> create( T target,
            Attribute targetAttr, Class<T> clazz , AbstractComponent component, Part part );
    
    <T extends TMapReferenceable> TMapReference<T> 
        createUnresolvedReference( Class<T> clazz, AbstractComponent component, 
                String refString );
    
    <T extends TMapReferenceable> TMapReference<T> create( T target,
            Class<T> clazz, AbstractComponent component, String refString );
    
    AttributesType.AttrType getAttributeType();
}

abstract class AbstractTMapVarReferenceFactory implements 
        TMapReferenceFactory
{

    public  <T extends TMapReferenceable> TMapReference<T> create( T target, 
            Class<T> clazz, AbstractComponent component, String refString ) 
    {
        assert target instanceof VariableDeclarator;
        return (TMapReference<T>) new VariableReferenceImpl( (VariableDeclarator)target, component , refString ,this );
    }

    public AttributesType.AttrType getAttributeType(){
        return AttributesType.AttrType.NCNAME;
    }
    
    public <T extends TMapReferenceable> TMapReference<T> createUnresolvedReference(
            Class<T> clazz, AbstractComponent component, String refString ) 
    {       VariableReference varRef = new VariableReferenceImpl(component , refString , this);
            
        return (TMapReference<T>) new VariableReferenceImpl(component , refString , this);
    }

    public <T extends TMapReferenceable> boolean haveRefString( 
                Reference<T> ref , T component )
    {
        assert component instanceof VariableDeclarator;
        if (ref instanceof VariableReference 
                && component instanceof VariableDeclarator) 
        {
            return haveInputVarRef((VariableReference)ref, (VariableDeclarator)component) 
                || haveOutputVarRef((VariableReference)ref, (VariableDeclarator)component);
        }
        return false;
    }

    private boolean compare(Variable var, VariableReference varRef) {
        boolean isEqual = false;
        if (var != null && varRef != null) {
            String refString = varRef.getRefString();
            if (refString != null) {
                int lastDotIndex = refString.lastIndexOf(".");
                refString = lastDotIndex > 0 ? refString.substring(0,lastDotIndex) : null;
            }

            isEqual = refString != null && refString.equals(var.getName());
        }
        
        return isEqual;
    }
    
    private boolean haveInputVarRef( 
            VariableReference ref, VariableDeclarator component) 
    {
        return compare(component.getInputVariable(), ref);
    }

    private boolean haveOutputVarRef( 
            VariableReference ref, VariableDeclarator component) 
    {
        return compare(component.getOutputVariable(), ref);
    }
    
    public <T extends TMapReferenceable> TMapReference<T> create( T target, 
            Attribute targetAttr, Class<T> clazz, AbstractComponent component, 
            Part part )
    {
        assert target instanceof VariableDeclarator;
        assert TMapAttributes.INPUT_VARIABLE.equals(targetAttr)  
                || TMapAttributes.OUTPUT_VARIABLE.equals(targetAttr);
        
        String refString = ((VariableDeclarator)target).getAttribute(targetAttr);
        refString = refString == null || part == null 
                ? null 
                : VariableReferenceImpl.getVarRefString(refString, part.getName());
        
        return (TMapReference<T>)( refString == null 
                ? null : new VariableReferenceImpl( 
                        (VariableDeclarator)target, component, refString, this));
    }
}

class VariableReferenceFactory extends AbstractTMapVarReferenceFactory {
    
    public <T extends TMapReferenceable> boolean isApplicable(Class<T> clazz) {
        return VariableDeclarator.class.isAssignableFrom( clazz );
    }

    public <T extends TMapReferenceable> T resolve(AbstractReference<T> ref) {
        AbstractDocumentComponent parentComponent = (AbstractDocumentComponent) ref
                .getParent();
        String refString = ref.getRefString();
        Class<T> clazz = ref.getType();
        
        if (!(ref instanceof VariableReference)) {
            return null;
        }
        
        String varName = VariableReferenceImpl.getVarName(refString);
        
        Operation operation = null;
        if (parentComponent instanceof Param) {
            parentComponent = (AbstractDocumentComponent)parentComponent.getParent();
        }
        
        if (parentComponent instanceof Transform) {
            operation = (Operation) ((Transform)parentComponent).getParent();
            VariableDeclarator varCont =  resolveByOperation(operation, varName);
            return clazz.cast(varCont);
        } 
        
        return null;
    }
    
    private VariableDeclarator resolveByOperation(Operation operation, String varName) {
        if (operation == null || varName == null) {
            return null;
        }
        if (isVariableDeclarator(operation, varName)) {
            return operation;
        }
        
        List<Invoke> invokes = operation.getInvokes();
        if (invokes != null && invokes.size() > 0) {
            for (Invoke invoke : invokes) {
                if (isVariableDeclarator(invoke, varName)) {
                    return invoke;
                }
            }
        }
        
        return null;
    }
    
    private boolean isVariableDeclarator(VariableDeclarator varContainer, String varName) {
        boolean isDeclarator = false;
        if (varContainer == null || varName == null) {
            return isDeclarator;
        }
        
        Variable tmpVar = varContainer.getInputVariable();
        isDeclarator = tmpVar != null && varName.equals(tmpVar.getName());
        
        if (!isDeclarator) {
            tmpVar = varContainer.getOutputVariable();
            isDeclarator = tmpVar != null && varName.equals(tmpVar.getName());
        }
        
        return isDeclarator;
    }
    
}


