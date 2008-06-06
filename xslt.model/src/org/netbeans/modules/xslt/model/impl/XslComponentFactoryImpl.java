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
import org.netbeans.modules.xslt.model.AttributeSet;
import org.netbeans.modules.xslt.model.CallTemplate;
import org.netbeans.modules.xslt.model.Choose;
import org.netbeans.modules.xslt.model.Copy;
import org.netbeans.modules.xslt.model.CopyOf;
import org.netbeans.modules.xslt.model.Document;
import org.netbeans.modules.xslt.model.Element;
import org.netbeans.modules.xslt.model.ForEach;
import org.netbeans.modules.xslt.model.If;
import org.netbeans.modules.xslt.model.Import;
import org.netbeans.modules.xslt.model.Include;
import org.netbeans.modules.xslt.model.Key;
import org.netbeans.modules.xslt.model.LiteralResultElement;
import org.netbeans.modules.xslt.model.Namespace;
import org.netbeans.modules.xslt.model.Number;
import org.netbeans.modules.xslt.model.Otherwise;
import org.netbeans.modules.xslt.model.Output;
import org.netbeans.modules.xslt.model.Param;
import org.netbeans.modules.xslt.model.Sequence;
import org.netbeans.modules.xslt.model.Sort;
import org.netbeans.modules.xslt.model.Stylesheet;
import org.netbeans.modules.xslt.model.Template;
import org.netbeans.modules.xslt.model.Text;
import org.netbeans.modules.xslt.model.ValueOf;
import org.netbeans.modules.xslt.model.Variable;
import org.netbeans.modules.xslt.model.When;
import org.netbeans.modules.xslt.model.WithParam;
import org.netbeans.modules.xslt.model.XslComponent;
import org.netbeans.modules.xslt.model.XslComponentFactory;


/**
 * @author ads
 *
 */
class XslComponentFactoryImpl implements XslComponentFactory {

    XslComponentFactoryImpl( XslModelImpl model ) {
        myModel = model;
        myBuilder = new ThreadLocal<XslComponentsBuildVisitor>();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslComponentFactory#createApplyTemplates()
     */
    public ApplyTemplates createApplyTemplates() {
        return new ApplyTemplatesImpl( getModel() );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslComponentFactory#createAttribute()
     */
    public Attribute createAttribute() {
        return new AttributeImpl( getModel() );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslComponentFactory#createAttributeSet()
     */
    public AttributeSet createAttributeSet() {
        return new AttributeSetImpl( getModel() );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslComponentFactory#createCallTemplate()
     */
    public CallTemplate createCallTemplate() {
        return new CallTemplateImpl( getModel() );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslComponentFactory#createChoose()
     */
    public Choose createChoose() {
        return new ChooseImpl( getModel() );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslComponentFactory#createCopy()
     */
    public Copy createCopy() {
        return new CopyImpl( getModel() );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslComponentFactory#createCopyOf()
     */
    public CopyOf createCopyOf() {
        return new CopyOfImpl( getModel() );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslComponentFactory#createDocument()
     */
    public Document createDocument() {
        return new DocumentImpl( getModel() );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslComponentFactory#createElement()
     */
    public Element createElement() {
        return new ElementImpl( getModel() );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslComponentFactory#createForEach()
     */
    public ForEach createForEach() {
        return new ForEachImpl( getModel() );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslComponentFactory#createIf()
     */
    public If createIf() {
        return new IfImpl( getModel() );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslComponentFactory#createInclude()
     */
    public Include createInclude() {
        return new IncludeImpl( getModel() );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslComponentFactory#createKey()
     */
    public Key createKey() {
        return new KeyImpl( getModel() );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslComponentFactory#createLiteralResultElement()
     */
    public LiteralResultElement createLiteralResultElement( String name ,
            String namespaceUri ) 
    {
        org.w3c.dom.Element element = getModel().getDocument().
            createElementNS( namespaceUri, name );
        return new LiteralResultElementImpl( getModel() , element );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslComponentFactory#createNamespace()
     */
    public Namespace createNamespace() {
        return new NamespaceImpl( getModel() );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslComponentFactory#createNumber()
     */
    public Number createNumber() {
        return new NumberImpl( getModel() );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslComponentFactory#createOtherwise()
     */
    public Otherwise createOtherwise() {
        return new OtherwiseImpl( getModel() );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslComponentFactory#createOutput()
     */
    public Output createOutput() {
        return new OutputImpl( getModel() );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslComponentFactory#createParam()
     */
    public Param createParam() {
        return new ParamImpl( getModel() );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslComponentFactory#createSequence()
     */
    public Sequence createSequence() {
        return new SequenceImpl( getModel() );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslComponentFactory#createSort()
     */
    public Sort createSort() {
        return new SortImpl( getModel() );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslComponentFactory#createStylesheet()
     */
    public Stylesheet createStylesheet() {
        return new StylesheetImpl( getModel() );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslComponentFactory#createTemplate()
     */
    public Template createTemplate() {
        return new TemplateImpl( getModel() );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslComponentFactory#createText()
     */
    public Text createText() {
        return new TextImpl( getModel() );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslComponentFactory#createValueOf()
     */
    public ValueOf createValueOf() {
        return new ValueOfImpl( getModel() );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslComponentFactory#createVariable()
     */
    public Variable createVariable() {
        return new VariableImpl( getModel() );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslComponentFactory#createWhen()
     */
    public When createWhen() {
        return new WhenImpl( getModel() );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslComponentFactory#createWithParam()
     */
    public WithParam createWithParam() {
        return new WithParamImpl( getModel() );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslComponentFactory#createImport()
     */
    public Import createImport() {
        return new ImportImpl( getModel() );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.xam.dom.ComponentFactory#create(org.w3c.dom.Element, org.netbeans.modules.xml.xam.dom.DocumentComponent)
     */
    public XslComponent create( org.w3c.dom.Element child, XslComponent parent )
    {
        XslComponentsBuildVisitor visitor = getBuilder( );
        return visitor.createSubComponent( parent , child  );
    }
    
    private XslComponentsBuildVisitor getBuilder() {
        XslComponentsBuildVisitor visitor = myBuilder.get();
        if ( visitor == null ) {
            visitor = new XslComponentsBuildVisitor( getModel());
            myBuilder.set( visitor );
        }
        visitor.init();
        return visitor;
    }
    
    private XslModelImpl getModel() {
        return myModel;
    }
    
    private XslModelImpl myModel;
    
    private ThreadLocal<XslComponentsBuildVisitor> myBuilder;

}
