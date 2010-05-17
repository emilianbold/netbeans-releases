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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.netbeans.modules.xml.xam.ComponentUpdater;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.netbeans.modules.xml.xam.dom.ChangeInfo;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.netbeans.modules.xslt.model.LiteralResultElement;
import org.netbeans.modules.xslt.model.SequenceConstructor;
import org.netbeans.modules.xslt.model.XslComponent;
import org.netbeans.modules.xslt.model.XslModel;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * @author ads
 *
 */
class XslModelImpl extends AbstractDocumentModel<XslComponent> 
    implements XslModel 
{

    XslModelImpl( ModelSource source ) {
        super(source);
        myFactory = new XslComponentFactoryImpl( this );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.xam.dom.AbstractDocumentModel#createRootComponent(org.w3c.dom.Element)
     */
    @Override
    public XslComponent createRootComponent( Element root )
    {
        StylesheetImpl stylesheet = (StylesheetImpl)myFactory.create(root, null);
        if (stylesheet != null) {
            myRoot = stylesheet;
        } else {
            return null;
        }
        return getStylesheet();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.xam.dom.AbstractDocumentModel#getComponentUpdater()
     */
    @Override
    protected ComponentUpdater<XslComponent> getComponentUpdater()
    {
        if ( mySyncUpdateVisitor == null ){
            mySyncUpdateVisitor = new SyncUpdateVisitor(); 
        }
        return mySyncUpdateVisitor;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslModel#getFactory()
     */
    public XslComponentFactoryImpl getFactory() {
        return myFactory;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xslt.model.XslModel#getStylesheet()
     */
    public StylesheetImpl getStylesheet() {
        return (StylesheetImpl) getRootComponent();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.xam.dom.DocumentModel#createComponent(org.netbeans.modules.xml.xam.dom.DocumentComponent, org.w3c.dom.Element)
     */
    public XslComponent createComponent( XslComponent parent, Element element ) {
        return getFactory().create( element , parent);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.xam.dom.DocumentModel#getRootComponent()
     */
    public XslComponent getRootComponent() {
        return myRoot ;
    }
    
    @Override
    public Set<QName> getQNames() {
        return XslElements.allQNames();
    }
    
    public ChangeInfo prepareChangeInfo(List<Node> pathToRoot) {
        ChangeInfo change = super.prepareChangeInfo(pathToRoot);
        DocumentComponent parentComponent = findComponent(change.getRootToParentPath());
        if (parentComponent == null) {
            return change;
        }
        
        // this are conditions where subject element is Literal element 
        if ( parentComponent instanceof SequenceConstructor && 
                !change.isDomainElement() && change.getChangedElement() != null) 
        {
            prepareLiterlElementChange(change, parentComponent);
        }
        else {
            change.setParentComponent( parentComponent );
        }
        return change;
    }

    private void prepareLiterlElementChange( ChangeInfo change, 
            DocumentComponent parentComponent ) 
    {
        if (change.getOtherNonDomainElementNodes() == null
                || change.getOtherNonDomainElementNodes().isEmpty())
        {
            // case add or remove literal result element
            change.setDomainElement(true);
            change.setParentComponent(null);
        }
        else  {
            List<Element> rootToChanged = new ArrayList<Element>(change
                    .getRootToParentPath());
            rootToChanged.add(change.getChangedElement());
            DocumentComponent changedComponent = findComponent(rootToChanged);
            if (changedComponent != null
                    && changedComponent.getClass().isAssignableFrom(
                            LiteralResultElement.class))
            {
                // case literal result element is changed
                change.markNonDomainChildAsChanged();
                change.setParentComponent(null);
            }
        }
    }
    
    private XslComponentFactoryImpl myFactory;
    
    private StylesheetImpl myRoot;
    
    private SyncUpdateVisitor mySyncUpdateVisitor;
    
}
