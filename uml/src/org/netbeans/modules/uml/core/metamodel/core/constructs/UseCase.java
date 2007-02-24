/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.uml.core.metamodel.core.constructs;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.basic.basicactions.BehavioredClassifier;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementConnector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IBackPointer;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.TypedFactoryRetriever;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author Aztec
 */
public class UseCase extends BehavioredClassifier implements IUseCase
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.constructs.IUseCase#addExtend(org.netbeans.modules.uml.core.metamodel.core.constructs.IExtend)
     */
    public void addExtend(IExtend extend)
    {
        addElement(extend);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.constructs.IUseCase#addExtendedBy(org.netbeans.modules.uml.core.metamodel.core.constructs.IExtend)
     */
    public void addExtendedBy(final IExtend extend)
    {
        new ElementConnector< IUseCase >().addChildAndConnect( 
                            this, 
                            true, 
                            "extendedBy",
                            "extendedBy", 
                            extend, 
                            new IBackPointer<IUseCase>() {
                                public void execute(IUseCase useCase) {
                                    extend.setBase(useCase);
                                }
                            } 
         );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.constructs.IUseCase#addExtensionPoint(org.netbeans.modules.uml.core.metamodel.core.constructs.IExtensionPoint)
     */
    public void addExtensionPoint(IExtensionPoint extPt)
    {
        addFeature(extPt);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.constructs.IUseCase#addInclude(org.netbeans.modules.uml.core.metamodel.core.constructs.IInclude)
     */
    public void addInclude(IInclude include)
    {
        addElement(include);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.constructs.IUseCase#addIncludedBy(org.netbeans.modules.uml.core.metamodel.core.constructs.IInclude)
     */
    public void addIncludedBy(final IInclude include)
    {
        new ElementConnector< IUseCase >().addChildAndConnect( 
                            this, 
                            true, 
                            "includedBy",
                            "includedBy", 
                            include, 
                            new IBackPointer<IUseCase>() {
                                public void execute(IUseCase useCase) {
                                    include.setAddition(useCase);
                                }
                            } 
         );

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.constructs.IUseCase#addUseCaseDetail(org.netbeans.modules.uml.core.metamodel.core.constructs.IUseCaseDetail)
     */
    public void addUseCaseDetail(IUseCaseDetail useCaseDetail)
    {
        addOwnedElement(useCaseDetail);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.constructs.IUseCase#createExtensionPoint()
     */
    public IExtensionPoint createExtensionPoint()
    {
        TypedFactoryRetriever<IExtensionPoint> retriever = 
                                        new TypedFactoryRetriever<IExtensionPoint>();
        return retriever.createType("ExtensionPoint");
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.constructs.IUseCase#createUseCaseDetail()
     */
    public IUseCaseDetail createUseCaseDetail()
    {
        TypedFactoryRetriever<IUseCaseDetail> retriever = 
                                        new TypedFactoryRetriever<IUseCaseDetail>();
        return retriever.createType("UseCaseDetail");
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.constructs.IUseCase#getDetails()
     */
    public ETList <IUseCaseDetail> getDetails()
    {
        ElementCollector< IUseCaseDetail > col =  new ElementCollector< IUseCaseDetail >();
        return col.retrieveElementCollection( (IElement)this,"UML:Element.ownedElement/UML:UseCaseDetail", IUseCaseDetail.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.constructs.IUseCase#getExtendedBy()
     */
    public ETList <IExtend> getExtendedBy()
    {
        ElementCollector< IExtend > col=  new ElementCollector< IExtend >();
        return col.retrieveElementCollectionWithAttrIDs(this,"extendedBy", IExtend.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.constructs.IUseCase#getExtends()
     */
    public ETList <IExtend> getExtends()
    {
        ElementCollector< IExtend > col=  new ElementCollector< IExtend >();
        return col.retrieveElementCollection((IElement)this,"UML:Element.ownedElement/UML:Extend", IExtend.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.constructs.IUseCase#getExtensionPoints()
     */
    public ETList <IExtensionPoint> getExtensionPoints()
    {
        ElementCollector< IExtensionPoint > col=  new ElementCollector< IExtensionPoint >();
        return col.retrieveElementCollection((IElement)this,"UML:Element.ownedElement/UML:ExtensionPoint", IExtensionPoint.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.constructs.IUseCase#getIncludedBy()
     */
    public ETList <IInclude> getIncludedBy()
    {
        ElementCollector< IInclude > col=  new ElementCollector< IInclude >();
        return col.retrieveElementCollectionWithAttrIDs(this,"includedBy", IInclude.class);

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.constructs.IUseCase#getIncludes()
     */
    public ETList <IInclude> getIncludes()
    {
        ElementCollector< IInclude > col=  new ElementCollector< IInclude >();
        return col.retrieveElementCollection((IElement)this,"UML:Element.ownedElement/UML:Include", IInclude.class);

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.constructs.IUseCase#removeExtend(org.netbeans.modules.uml.core.metamodel.core.constructs.IExtend)
     */
    public void removeExtend(IExtend extend)
    {
       removeElement(extend);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.constructs.IUseCase#removeExtendedBy(org.netbeans.modules.uml.core.metamodel.core.constructs.IExtend)
     */
    public void removeExtendedBy(final IExtend extend)
    {
        new ElementConnector< IUseCase >()
        .removeByID( 
                    this, 
                    extend, 
                    "extendedBy",
                    new IBackPointer<IUseCase>() {
                        public void execute(IUseCase useCase) {
                            extend.setBase(useCase);
                        }
                    }
        );

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.constructs.IUseCase#removeExtensionPoint(org.netbeans.modules.uml.core.metamodel.core.constructs.IExtensionPoint)
     */
    public void removeExtensionPoint(IExtensionPoint extPt)
    {
        removeFeature(extPt);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.constructs.IUseCase#removeInclude(org.netbeans.modules.uml.core.metamodel.core.constructs.IInclude)
     */
    public void removeInclude(IInclude include)
    {
        removeElement(include);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.constructs.IUseCase#removeIncludedBy(org.netbeans.modules.uml.core.metamodel.core.constructs.IInclude)
     */
    public void removeIncludedBy(final IInclude include)
    {
        new ElementConnector< IUseCase >()
        .removeByID( 
                    this, 
                    include, 
                    "includedBy",
                    new IBackPointer<IUseCase>() {
                        public void execute(IUseCase useCase) {
                            include.setAddition(useCase);
                        }
                    }
        );

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.constructs.IUseCase#removeUseCaseDetail(org.netbeans.modules.uml.core.metamodel.core.constructs.IUseCaseDetail)
     */
    public void removeUseCaseDetail(IUseCaseDetail useCaseDetail)
    {
        removeOwnedElement(useCaseDetail);
    }

    public void establishNodePresence(Document doc, Node node)
    {
        buildNodePresence("UML:UseCase", doc, node);
    }

    public void delete()
    {
        super.delete();
        ETList <IExtend> extendedBy =  getExtendedBy();
        for (int i=0; i<extendedBy.size(); i++)
            extendedBy.get(i).delete();
        
        ETList <IInclude> includedBy = getIncludedBy();
        for (int i=0; i<includedBy.size(); i++)
            includedBy.get(i).delete();
    }

}
