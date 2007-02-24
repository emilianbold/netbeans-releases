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

/*
 * File       : DerivationClassifier.java
 * Created on : Dec 5, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.metamodel.infrastructure;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.core.foundation.Dependency;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IDependency;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IFlow;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IReference;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ITaggedValue;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.Classifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.Derivation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IDerivation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IUMLBinding;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguage;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author Aztec
 */
public class DerivationClassifier
    extends Classifier
    implements IDerivationClassifier
{
    IDerivation m_derivation = new Derivation();
    
    /**
     * Establishes the appropriate XML elements for this UML type.
     *
     * [in] The document where this element will reside
     * [in] The element's parent node.
     */ 
    public void establishNodePresence(Document doc, Node parent)
    {
        buildNodePresence("UML:DerivationClassifier",doc,parent);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement#setNode(org.dom4j.Node)
     */
    public void setNode(Node n)
    {
        super.setNode(n);
        
        m_derivation.setNode(n);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IDerivation#addBinding(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IUMLBinding)
     */
    public void addBinding(IUMLBinding pBind)
    {
        m_derivation.addBinding(pBind);

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IDerivation#getBindings()
     */
    public ETList<IUMLBinding> getBindings()
    {
        return m_derivation.getBindings();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IDerivation#getDerivedClassifier()
     */
    public IClassifier getDerivedClassifier()
    {
        return m_derivation.getDerivedClassifier();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterableElement#getTemplate()
     */
    public IClassifier getTemplate()
    {
        return m_derivation.getTemplate();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IDerivation#removeBinding(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IUMLBinding)
     */
    public void removeBinding(IUMLBinding pBind)
    {
        m_derivation.removeBinding(pBind);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IDerivation#setDerivedClassifier(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
     */
    public void setDerivedClassifier(IClassifier value)
    {
        m_derivation.setDerivedClassifier(value);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterableElement#setTemplate(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
     */
    public void setTemplate(IClassifier classifier)
    {
        m_derivation.setTemplate(classifier);
    }

    /**
     * @param elem
     */
    public void addSource(IElement elem)
    {
        m_derivation.addSource(elem);
    }

    /**
     * @param elem
     */
    public void addTarget(IElement elem)
    {
        m_derivation.addTarget(elem);
    }

    /**
     * @return
     */
    public INamedElement getClient()
    {
        return m_derivation.getClient();
    }

    /**
     * 
     */
    public ETList<IElement> getRelatedElements()
    {
        return m_derivation.getRelatedElements();
    }

    /**
     * @return
     */
    public long getSourceCount()
    {
        return m_derivation.getSourceCount();
    }

    /**
     * 
     */
    public ETList<IElement> getSources()
    {
        return m_derivation.getSources();
    }

    /**
     * @return
     */
    public INamedElement getSupplier()
    {
        return m_derivation.getSupplier();
    }

    /**
     * @return
     */
    public long getTargetCount()
    {
        return m_derivation.getTargetCount();
    }

    /**
     * 
     */
    public ETList<IElement> getTargets()
    {
        return m_derivation.getTargets();
    }

    /**
     * @param elem
     */
    public void removeSource(IElement elem)
    {
        m_derivation.removeSource(elem);
    }

    /**
     * @param elem
     */
    public void removeTarget(IElement elem)
    {
        m_derivation.removeTarget(elem);
    }

    /**
     * @param elem
     */
    public void setClient(INamedElement elem)
    {
        m_derivation.setClient(elem);
    }

    /**
     * @param elem
     */
    public void setSupplier(INamedElement elem)
    {
        m_derivation.setSupplier(elem);
    }

}
