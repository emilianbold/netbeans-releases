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

import org.netbeans.modules.xslt.model.ApplyTemplates;
import org.netbeans.modules.xslt.model.Attribute;
import org.netbeans.modules.xslt.model.CallTemplate;
import org.netbeans.modules.xslt.model.Choose;
import org.netbeans.modules.xslt.model.Document;
import org.netbeans.modules.xslt.model.ForEach;
import org.netbeans.modules.xslt.model.If;
import org.netbeans.modules.xslt.model.Key;
import org.netbeans.modules.xslt.model.LiteralResultElement;
import org.netbeans.modules.xslt.model.Namespace;
import org.netbeans.modules.xslt.model.Otherwise;
import org.netbeans.modules.xslt.model.Param;
import org.netbeans.modules.xslt.model.Sequence;
import org.netbeans.modules.xslt.model.SequenceConstructor;
import org.netbeans.modules.xslt.model.Sort;
import org.netbeans.modules.xslt.model.Stylesheet;
import org.netbeans.modules.xslt.model.Template;
import org.netbeans.modules.xslt.model.ValueOf;
import org.netbeans.modules.xslt.model.Variable;
import org.netbeans.modules.xslt.model.When;
import org.netbeans.modules.xslt.model.WithParam;
import org.netbeans.modules.xslt.model.XslComponent;
import org.netbeans.modules.xslt.model.XslVisitorAdapter;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
class XslComponentsBuildVisitor extends XslVisitorAdapter {

    XslComponentsBuildVisitor( XslModelImpl model ){
        assert model != null;
        myModel = model;
    }

    XslComponent createSubComponent( XslComponent parent , Element element ) 
    {
        myElement = element;
        String namespace = element.getNamespaceURI();
        if ( namespace == null && parent instanceof XslComponentImpl ) {
            namespace = ((XslComponentImpl)parent).
                lookupNamespaceURI(element.getPrefix());
        }
        
        
        if (XslComponent.XSL_NAMESPACE.equals(namespace)) {
            if (parent == null) {
                if (XslElements.STYLESHEET.getName().equals(
                        getElement().getLocalName())
                        || XslElements.TRANSFORM.getName().equals(
                                getElement().getLocalName()))
                {
                    setResult(new StylesheetImpl(getModel(), element));
                }
            }
            else {
                parent.accept(this);
            }
        }
        else {
            createNonXslComponentChild( parent );
        }
        return myResult;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslVisitorAdapter#visit(org.netbeans.modules.xslt.model.Stylesheet)
     */
    @Override
    public void visit( Stylesheet stylesheet )
    {
        if ( isAcceptable( XslElements.IMPORT) ) {
            setResult( new ImportImpl( getModel() , getElement()) );
        }
        else if ( isAcceptable( XslElements.VARIABLE )) {
            setResult( new VariableImpl( getModel() , getElement()) );
        }
        else if ( isAcceptable( XslElements.PARAM )) {
            setResult( new ParamImpl( getModel() , getElement()) );
        }
        else {
            createDeclaration(stylesheet );
        }
            
    }
    
    @Override
    public void visit( Variable variable ) {
        visitSequenceConstructor( variable );
    }
    
    @Override
    public void visit( Document document ) {
        visitSequenceConstructor( document );
    }
    
    @Override
    public void visit( org.netbeans.modules.xslt.model.Element element ) {
        visitSequenceConstructor( element );
    }
    
    @Override
    public void visit( Attribute attribute ) {
        visitSequenceConstructor( attribute );
    }
    
    @Override
    public void visit( ForEach forEach ) {
        if ( visitSequenceConstructor( forEach ) ) {
            return;
        }
        if ( isAcceptable( XslElements.SORT )) {
            setResult( new SortImpl( getModel() , getElement()) );
        }
    }
    
    @Override
    public void visit( If iff ) {
        visitSequenceConstructor( iff );
    }
    
    @Override
    public void visit( Key key ) {
        visitSequenceConstructor( key );
    }
    
    @Override
    public void visit( Namespace namespace ) {
        visitSequenceConstructor( namespace );
    }
    
    @Override
    public void visit( Otherwise otherwise ) {
        visitSequenceConstructor( otherwise );
    }
    
    @Override
    public void visit( Param param ) {
        visitSequenceConstructor( param );
    }
    
    @Override
    public void visit( Sequence sequence ) {
        visitSequenceConstructor( sequence );
    }
    
    @Override
    public void visit( Sort sort ) {
        visitSequenceConstructor( sort );
    }
    
    @Override
    public void visit( Template template ) {
        if ( visitSequenceConstructor( template ) ) {
            return;
        }
        if ( isAcceptable( XslElements.PARAM )) {
            setResult( new ParamImpl( getModel() , getElement()) );
        }
    }
    
    @Override
    public void visit( ValueOf valueOf ) {
        visitSequenceConstructor( valueOf );
    }
    
    @Override
    public void visit( When when ) {
        visitSequenceConstructor( when );
    }
    
    @Override
    public void visit( Choose choose ) {
        if ( isAcceptable( XslElements.WHEN )) {
            setResult( new WhenImpl( getModel() , getElement() ) );
        }
        else if ( isAcceptable( XslElements.OTHERWISE )) {
            setResult( new OtherwiseImpl( getModel() , getElement() ) );
        }
    }
    
    @Override
    public void visit( CallTemplate callTemplate ) {
        if ( isAcceptable( XslElements.WHITH_PARAM )) {
            setResult( new WithParamImpl( getModel() , getElement() ) );
        }
    }

    @Override
    public void visit( ApplyTemplates applyTemplates ) {
        if ( isAcceptable( XslElements.WHITH_PARAM )) {
            setResult( new WithParamImpl( getModel() , getElement() ) );
        }
        else if ( isAcceptable( XslElements.SORT )) {
            setResult( new SortImpl( getModel() , getElement() ) );
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslVisitorAdapter#visit(org.netbeans.modules.xslt.model.LiteralResultElement)
     */
    @Override
    public void visit( LiteralResultElement element )
    {
        visitSequenceConstructor( element );
    }

    private boolean visitSequenceConstructor( SequenceConstructor constructor  ) {
        if ( isAcceptable( XslElements.APPLY_TEMPLATES)) {
            setResult( new ApplyTemplatesImpl( getModel() , getElement() ) );
        }
        else if ( isAcceptable( XslElements.ATTRIBUTE) ) {
            setResult( new AttributeImpl( getModel() , getElement()) );
        }
        else if ( isAcceptable( XslElements.CALL_TEMPLATE) ) {
            setResult( new CallTemplateImpl( getModel() , getElement()) );
        }
        else if ( isAcceptable( XslElements.CHOOSE) ) {
            setResult( new ChooseImpl( getModel() , getElement()) );
        }
        else if ( isAcceptable( XslElements.COPY) ) {
            setResult( new CopyImpl( getModel() , getElement()) );
        }
        else if ( isAcceptable( XslElements.COPY_OF) ) {
            setResult( new CopyOfImpl( getModel() , getElement()) );
        }
        else if ( isAcceptable( XslElements.DOCUMENT) ) {
            setResult( new DocumentImpl( getModel() , getElement()) );
        }
        else if ( isAcceptable( XslElements.ELEMENT ) ) {
            setResult( new ElementImpl( getModel() , getElement()) );
        }
        else if ( isAcceptable( XslElements.FOR_EACH) ) {
            setResult( new ForEachImpl( getModel() , getElement()) );
        }
        else if ( isAcceptable( XslElements.IF ) ) {
            setResult( new IfImpl( getModel() , getElement()) );
        }
        else if ( isAcceptable( XslElements.NAMESPACE ) ) {
            setResult( new NamespaceImpl( getModel() , getElement()) );
        }
        else if ( isAcceptable( XslElements.NUMBER ) ) {
            setResult( new NumberImpl( getModel() , getElement()) );
        }
        else if ( isAcceptable( XslElements.SEQUENCE ) ) {
            setResult( new SequenceImpl( getModel() , getElement()) );
        }
        else if ( isAcceptable( XslElements.TEXT ) ) {
            setResult( new TextImpl( getModel() , getElement()) );
        }
        else if ( isAcceptable( XslElements.VALUE_OF ) ) {
            setResult( new ValueOfImpl( getModel() , getElement()) );
        }
        return myResult!= null;
    }
        
    private void createDeclaration( Stylesheet stylesheet ) {
        if ( isAcceptable( XslElements.ATTRIBUTE_SET ) ) {
            setResult( new AttributeSetImpl( getModel() , getElement()) );
        }
        else if ( isAcceptable( XslElements.INCLUDE ) ) {
            setResult( new IncludeImpl( getModel() , getElement() ) );
        }
        else if ( isAcceptable( XslElements.KEY ) ) {
            setResult( new KeyImpl( getModel() , getElement() ) );
        }
        else if ( isAcceptable( XslElements.TEMPLATE ) ) {
            setResult( new TemplateImpl( getModel() , getElement() ) );
        }
        else if ( isAcceptable( XslElements.OUTPUT ) ) {
            setResult( new OutputImpl( getModel() , getElement() ) );
        }
    }
    

    private void createNonXslComponentChild( XslComponent parent ) {
        if ( parent instanceof SequenceConstructor ) {
            setResult( new LiteralResultElementImpl( getModel() , getElement() ) );
        }
    }
    
    private void setResult( XslComponent component ) {
        myResult = component;
    }
    
    private boolean isAcceptable( XslElements element ) {
        return element.getName().equals( getLocalName() );
    }
    
    private String getLocalName() {
        return getElement().getLocalName();
    }
    
    void init(){
        myResult = null;
        myElement = null;
    }
    
    private XslModelImpl getModel() {
        return myModel;
    }
    
    private Element getElement() {
        return myElement;
    }

    private Element myElement;
    
    private XslModelImpl myModel;
    
    private XslComponent myResult;
}
