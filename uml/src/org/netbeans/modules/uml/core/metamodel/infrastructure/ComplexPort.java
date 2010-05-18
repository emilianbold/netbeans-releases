/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

/*
 * File       : ComplexPort.java
 * Created on : Dec 5, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.metamodel.infrastructure;

import org.dom4j.Element;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IProtocolStateMachine;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IDependency;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IFlow;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IReference;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ITaggedValue;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeature;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.StructuralFeature;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguage;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author Aztec
 */
public class ComplexPort extends StructuralFeature implements IComplexPort
{
    IPort m_Port = new Port();
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.NamedElement#addClientDependency(org.netbeans.modules.uml.core.metamodel.core.foundation.IDependency)
     */
    public void addClientDependency(IDependency dep)
    {
        m_Port.addClientDependency(dep);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#addElement(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
     */
    public IElement addElement(IElement elem)
    {
        return m_Port.addElement(elem);
    }

    /**
     * @param end
     */
    public void addEnd(IConnectorEnd end)
    {
        m_Port.addEnd(end);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#addOwnedConstraint(org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint)
     */
    public void addOwnedConstraint(IConstraint constraint)
    {
        m_Port.addOwnedConstraint(constraint);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#addPresentationElement(org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement)
     */
    public IPresentationElement addPresentationElement(IPresentationElement elem)
    {
        return m_Port.addPresentationElement(elem);
    }

    /**
     * @param pInter
     */
    public void addProvidedInterface(IInterface pInter)
    {
        m_Port.addProvidedInterface(pInter);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.RedefinableElement#addRedefinedElement(org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement)
     */
    public long addRedefinedElement(IRedefinableElement element)
    {
        return m_Port.addRedefinedElement(element);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.RedefinableElement#addRedefiningElement(org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement)
     */
    public long addRedefiningElement(IRedefinableElement element)
    {
        return m_Port.addRedefiningElement(element);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#addReferencingReference(org.netbeans.modules.uml.core.metamodel.core.foundation.IReference)
     */
    public IReference addReferencingReference(IReference ref)
    {
        return m_Port.addReferencingReference(ref);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#addReferredReference(org.netbeans.modules.uml.core.metamodel.core.foundation.IReference)
     */
    public IReference addReferredReference(IReference ref)
    {
        return m_Port.addReferredReference(ref);
    }

    /**
     * @param pInter
     */
    public void addRequiredInterface(IInterface pInter)
    {
        m_Port.addRequiredInterface(pInter);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#addSourceFile(java.lang.String)
     */
    public void addSourceFile(String fileName)
    {
        m_Port.addSourceFile(fileName);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#addSourceFlow(org.netbeans.modules.uml.core.metamodel.core.foundation.IFlow)
     */
    public void addSourceFlow(IFlow flow)
    {
        m_Port.addSourceFlow(flow);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.NamedElement#addSupplierDependency(org.netbeans.modules.uml.core.metamodel.core.foundation.IDependency)
     */
    public void addSupplierDependency(IDependency dep)
    {
        m_Port.addSupplierDependency(dep);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#addTaggedValue(java.lang.String, java.lang.String)
     */
    public ITaggedValue addTaggedValue(String tagName, String value)
    {
        return m_Port.addTaggedValue(tagName, value);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#addTargetFlow(org.netbeans.modules.uml.core.metamodel.core.foundation.IFlow)
     */
    public void addTargetFlow(IFlow flow)
    {
        m_Port.addTargetFlow(flow);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#applyNewStereotypes(java.lang.String)
     */
    public void applyNewStereotypes(String name)
    {
        m_Port.applyNewStereotypes(name);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#applyStereotype(java.lang.Object)
     */
    public void applyStereotype(Object stereotype)
    {
        m_Port.applyStereotype(stereotype);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#applyStereotype2(java.lang.String)
     */
    public Object applyStereotype2(String name)
    {
        return m_Port.applyStereotype2(name);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#createConstraint(java.lang.String, java.lang.String)
     */
    public IConstraint createConstraint(String name, String expr)
    {
        return m_Port.createConstraint(name, expr);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.VersionableElement#delete()
     */
    public void delete()
    {
        m_Port.delete();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#deleteFlowRelations()
     */
    public void deleteFlowRelations()
    {
        m_Port.deleteFlowRelations();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#deleteReferenceRelations()
     */
    public void deleteReferenceRelations()
    {
        m_Port.deleteReferenceRelations();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.VersionableElement#duplicate()
     */
    public IVersionableElement duplicate()
    {
        return m_Port.duplicate();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.Feature#duplicateToClassifier(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
     */
    public IFeature duplicateToClassifier(IClassifier destination)
    {
        return m_Port.duplicateToClassifier(destination);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object arg0)
    {
        return m_Port.equals(arg0);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.NamedElement#getAlias()
     */
    public String getAlias()
    {
        return m_Port.getAlias();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#getAllTaggedValues()
     */
    public ETList<ITaggedValue> getAllTaggedValues()
    {
        return m_Port.getAllTaggedValues();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#getAppliedStereotypes()
     */
    public ETList<Object> getAppliedStereotypes()
    {
        return m_Port.getAppliedStereotypes();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#getAppliedStereotypesAsString()
     */
    public ETList<String> getAppliedStereotypesAsString()
    {
        return m_Port.getAppliedStereotypesAsString();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#getAppliedStereotypesAsString(boolean)
     */
    public String getAppliedStereotypesAsString(boolean honorAliasing)
    {
        return m_Port.getAppliedStereotypesAsString(honorAliasing);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#getAssociatedArtifactCount()
     */
    public long getAssociatedArtifactCount()
    {
        return m_Port.getAssociatedArtifactCount();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#getAssociatedArtifacts()
     */
    public ETList<IElement> getAssociatedArtifacts()
    {
        return m_Port.getAssociatedArtifacts();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.NamedElement#getClientDependencies()
     */
    public ETList<IDependency> getClientDependencies()
    {
        return m_Port.getClientDependencies();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.NamedElement#getClientDependenciesByType(java.lang.String)
     */
    public ETList<IDependency> getClientDependenciesByType(String type)
    {
        return m_Port.getClientDependenciesByType(type);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.NamedElement#getClientDependencyCount()
     */
    public long getClientDependencyCount()
    {
        return m_Port.getClientDependencyCount();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#getDocumentation()
     */
    public String getDocumentation()
    {
        return m_Port.getDocumentation();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.VersionableElement#getDOM4JNode()
     */
    public Node getDOM4JNode()
    {
        return m_Port.getDOM4JNode();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#getElementCount()
     */
    public long getElementCount()
    {
        return m_Port.getElementCount();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.VersionableElement#getElementNode()
     */
    public Element getElementNode()
    {
        return m_Port.getElementNode();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#getElements()
     */
    public ETList<IElement> getElements()
    {
        return m_Port.getElements();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#getElementType()
     */
    public String getElementType()
    {
        return m_Port.getElementType();
    }

    /**
     * 
     */
    public ETList<IConnectorEnd> getEnds()
    {
        return m_Port.getEnds();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#getExpandedElementType()
     */
    public String getExpandedElementType()
    {
        return m_Port.getExpandedElementType();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.Feature#getFeaturingClassifier()
     */
    public IClassifier getFeaturingClassifier()
    {
        return m_Port.getFeaturingClassifier();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.NamedElement#getFullyQualifiedName(boolean)
     */
    public String getFullyQualifiedName(boolean useProjName)
    {
        return m_Port.getFullyQualifiedName(useProjName);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.RedefinableElement#getIsFinal()
     */
    public boolean getIsFinal()
    {
        return m_Port.getIsFinal();
    }

    /**
     * @param pInter
     * @return
     */
    public boolean getIsProvidedInterface(IInterface pInter)
    {
        return m_Port.getIsProvidedInterface(pInter);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.RedefinableElement#getIsRedefined()
     */
    public boolean getIsRedefined()
    {
        return m_Port.getIsRedefined();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.RedefinableElement#getIsRedefining()
     */
    public boolean getIsRedefining()
    {
        return m_Port.getIsRedefining();
    }

    /**
     * @param pInter
     * @return
     */
    public boolean getIsRequiredInterface(IInterface pInter)
    {
        return m_Port.getIsRequiredInterface(pInter);
    }

    /**
     * @return
     */
    public boolean getIsService()
    {
        return m_Port.getIsService();
    }

    /**
     * @return
     */
    public boolean getIsSignal()
    {
        return m_Port.getIsSignal();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.Feature#getIsStatic()
     */
    public boolean getIsStatic()
    {
        return m_Port.getIsStatic();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#getLanguages()
     */
    public ETList<ILanguage> getLanguages()
    {
        return m_Port.getLanguages();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.VersionableElement#getLineNumber()
     */
    public int getLineNumber()
    {
        return m_Port.getLineNumber();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.NamedElement#getName()
     */
    public String getName()
    {
        return m_Port.getName();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.NamedElement#getNamespace()
     */
    public INamespace getNamespace()
    {
        return m_Port.getNamespace();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.NamedElement#getNameWithAlias()
     */
    public String getNameWithAlias()
    {
        return m_Port.getNameWithAlias();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.VersionableElement#getNode()
     */
    public Node getNode()
    {
        return m_Port.getNode();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#getNumAppliedStereotypes()
     */
    public int getNumAppliedStereotypes()
    {
        return m_Port.getNumAppliedStereotypes();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#getOwnedConstraints()
     */
    public ETList<IConstraint> getOwnedConstraints()
    {
        return m_Port.getOwnedConstraints();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#getOwner()
     */
    public IElement getOwner()
    {
        return m_Port.getOwner();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#getOwningPackage()
     */
    public IPackage getOwningPackage()
    {
        return m_Port.getOwningPackage();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#getPresentationElementById(java.lang.String)
     */
    public IPresentationElement getPresentationElementById(String id)
    {
        return m_Port.getPresentationElementById(id);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#getPresentationElementCount()
     */
    public long getPresentationElementCount()
    {
        return m_Port.getPresentationElementCount();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#getPresentationElements()
     */
    public ETList<IPresentationElement> getPresentationElements()
    {
        return m_Port.getPresentationElements();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#getProject()
     */
    public IProject getProject()
    {
        return m_Port.getProject();
    }

    /**
     * @return
     */
    public IProtocolStateMachine getProtocol()
    {
        return m_Port.getProtocol();
    }

    /**
     * 
     */
    public ETList<IInterface> getProvidedInterfaces()
    {
        return m_Port.getProvidedInterfaces();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.NamedElement#getQualifiedName()
     */
    public String getQualifiedName()
    {
        return m_Port.getQualifiedName();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.NamedElement#getQualifiedName2()
     */
    public String getQualifiedName2()
    {
        return m_Port.getQualifiedName2();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.RedefinableElement#getRedefinedElementCount()
     */
    public long getRedefinedElementCount()
    {
        return m_Port.getRedefinedElementCount();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.RedefinableElement#getRedefinedElements()
     */
    public ETList<IRedefinableElement> getRedefinedElements()
    {
        return m_Port.getRedefinedElements();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.RedefinableElement#getRedefiningElementCount()
     */
    public long getRedefiningElementCount()
    {
        return m_Port.getRedefiningElementCount();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.RedefinableElement#getRedefiningElements()
     */
    public ETList<IRedefinableElement> getRedefiningElements()
    {
        return m_Port.getRedefiningElements();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#getReferencingReferenceCount()
     */
    public long getReferencingReferenceCount()
    {
        return m_Port.getReferencingReferenceCount();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#getReferencingReferences()
     */
    public ETList<IReference> getReferencingReferences()
    {
        return m_Port.getReferencingReferences();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#getReferredReferenceCount()
     */
    public long getReferredReferenceCount()
    {
        return m_Port.getReferredReferenceCount();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#getReferredReferences()
     */
    public ETList<IReference> getReferredReferences()
    {
        return m_Port.getReferredReferences();
    }

    /**
     * 
     */
    public ETList<IInterface> getRequiredInterfaces()
    {
        return m_Port.getRequiredInterfaces();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#getSourceFiles()
     */
    public ETList<IElement> getSourceFiles()
    {
        return m_Port.getSourceFiles();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#getSourceFiles2(java.lang.String)
     */
    public ETList<IElement> getSourceFiles2(String language)
    {
        return m_Port.getSourceFiles2(language);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#getSourceFiles3(org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguage)
     */
    public ETList<IElement> getSourceFiles3(ILanguage language)
    {
        return m_Port.getSourceFiles3(language);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#getSourceFlowCount()
     */
    public long getSourceFlowCount()
    {
        return m_Port.getSourceFlowCount();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#getSourceFlows()
     */
    public ETList<IFlow> getSourceFlows()
    {
        return m_Port.getSourceFlows();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.NamedElement#getSupplierDependencies()
     */
    public ETList<IDependency> getSupplierDependencies()
    {
        return m_Port.getSupplierDependencies();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.NamedElement#getSupplierDependenciesByType(java.lang.String)
     */
    public ETList<IDependency> getSupplierDependenciesByType(String type)
    {
        return m_Port.getSupplierDependenciesByType(type);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.NamedElement#getSupplierDependencyCount()
     */
    public long getSupplierDependencyCount()
    {
        return m_Port.getSupplierDependencyCount();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#getTaggedValueByName(java.lang.String)
     */
    public ITaggedValue getTaggedValueByName(String tagName)
    {
        return m_Port.getTaggedValueByName(tagName);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#getTaggedValueCount()
     */
    public long getTaggedValueCount()
    {
        return m_Port.getTaggedValueCount();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#getTaggedValues()
     */
    public ETList<ITaggedValue> getTaggedValues()
    {
        return m_Port.getTaggedValues();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#getTaggedValuesByName(java.lang.String)
     */
    public ETList<ITaggedValue> getTaggedValuesByName(String tagName)
    {
        return m_Port.getTaggedValuesByName(tagName);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#getTargetFlowCount()
     */
    public long getTargetFlowCount()
    {
        return m_Port.getTargetFlowCount();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#getTargetFlows()
     */
    public ETList<IFlow> getTargetFlows()
    {
        return m_Port.getTargetFlows();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#getTopLevelId()
     */
    public String getTopLevelId()
    {
        return m_Port.getTopLevelId();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.VersionableElement#getVersionedFileName()
     */
    public String getVersionedFileName()
    {
        return m_Port.getVersionedFileName();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.VersionableElement#getVersionedURI()
     */
    public String getVersionedURI()
    {
        return m_Port.getVersionedURI();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.NamedElement#getVisibility()
     */
    public int getVisibility()
    {
        return m_Port.getVisibility();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.VersionableElement#getXMIID()
     */
    public String getXMIID()
    {
        return m_Port.getXMIID();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode()
    {
        return m_Port.hashCode();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#inSameProject(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
     */
    public boolean inSameProject(IElement elem)
    {
        return m_Port.inSameProject(elem);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.NamedElement#isAliased()
     */
    public boolean isAliased()
    {
        return m_Port.isAliased();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.VersionableElement#isDeleted()
     */
    public boolean isDeleted()
    {
        return m_Port.isDeleted();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.VersionableElement#isDirty()
     */
    public boolean isDirty()
    {
        return m_Port.isDirty();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.VersionableElement#isMarkForExtraction()
     */
    public boolean isMarkForExtraction()
    {
        return m_Port.isMarkForExtraction();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.NamedElement#isNameSame(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature)
     */
    public boolean isNameSame(IBehavioralFeature feature)
    {
        return m_Port.isNameSame(feature);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#isOwnedElement(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
     */
    public boolean isOwnedElement(IElement elem)
    {
        return m_Port.isOwnedElement(elem);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#isOwnedElement(java.lang.String)
     */
    public boolean isOwnedElement(String id)
    {
        return m_Port.isOwnedElement(id);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#isPresent(org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement)
     */
    public boolean isPresent(IPresentationElement elem)
    {
        return m_Port.isPresent(elem);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.VersionableElement#isSame(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement)
     */
    public boolean isSame(IVersionableElement elem)
    {
        return m_Port.isSame(elem);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.VersionableElement#isVersioned()
     */
    public boolean isVersioned()
    {
        return m_Port.isVersioned();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.Feature#moveToClassifier(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
     */
    public void moveToClassifier(IClassifier destination)
    {
        m_Port.moveToClassifier(destination);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.VersionableElement#prepareNode(org.dom4j.Node)
     */
    public void prepareNode(Node node)
    {
        m_Port.prepareNode(node);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.NamedElement#removeClientDependency(org.netbeans.modules.uml.core.metamodel.core.foundation.IDependency)
     */
    public void removeClientDependency(IDependency dep)
    {
        m_Port.removeClientDependency(dep);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#removeElement(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
     */
    public IElement removeElement(IElement elem)
    {
        return m_Port.removeElement(elem);
    }

    /**
     * @param end
     */
    public void removeEnd(IConnectorEnd end)
    {
        m_Port.removeEnd(end);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#removeOwnedConstraint(org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint)
     */
    public void removeOwnedConstraint(IConstraint constraint)
    {
        m_Port.removeOwnedConstraint(constraint);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#removePresentationElement(org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement)
     */
    public void removePresentationElement(IPresentationElement elem)
    {
        m_Port.removePresentationElement(elem);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#removePresentationElements()
     */
    public void removePresentationElements()
    {
        m_Port.removePresentationElements();
    }

    /**
     * @param end
     */
    public void removeProvidedInterface(IInterface end)
    {
        m_Port.removeProvidedInterface(end);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.RedefinableElement#removeRedefinedElement(org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement)
     */
    public long removeRedefinedElement(IRedefinableElement element)
    {
        return m_Port.removeRedefinedElement(element);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.RedefinableElement#removeRedefiningElement(org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement)
     */
    public long removeRedefiningElement(IRedefinableElement element)
    {
        return m_Port.removeRedefiningElement(element);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#removeReferencingReference(org.netbeans.modules.uml.core.metamodel.core.foundation.IReference)
     */
    public void removeReferencingReference(IReference ref)
    {
        m_Port.removeReferencingReference(ref);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#removeReferredReference(org.netbeans.modules.uml.core.metamodel.core.foundation.IReference)
     */
    public void removeReferredReference(IReference ref)
    {
        m_Port.removeReferredReference(ref);
    }

    /**
     * @param end
     */
    public void removeRequiredInterface(IInterface end)
    {
        m_Port.removeRequiredInterface(end);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#removeSourceFile(java.lang.String)
     */
    public void removeSourceFile(String fileName)
    {
        m_Port.removeSourceFile(fileName);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#removeSourceFlow(org.netbeans.modules.uml.core.metamodel.core.foundation.IFlow)
     */
    public void removeSourceFlow(IFlow flow)
    {
        m_Port.removeSourceFlow(flow);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#removeStereotype(java.lang.Object)
     */
    public void removeStereotype(Object stereotype)
    {
        m_Port.removeStereotype(stereotype);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#removeStereotype2(java.lang.String)
     */
    public void removeStereotype2(String name)
    {
        m_Port.removeStereotype2(name);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#removeStereotypes()
     */
    public void removeStereotypes()
    {
        m_Port.removeStereotypes();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.NamedElement#removeSupplierDependency(org.netbeans.modules.uml.core.metamodel.core.foundation.IDependency)
     */
    public void removeSupplierDependency(IDependency dep)
    {
        m_Port.removeSupplierDependency(dep);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#removeTaggedValue(org.netbeans.modules.uml.core.metamodel.core.foundation.ITaggedValue)
     */
    public void removeTaggedValue(ITaggedValue tag)
    {
        m_Port.removeTaggedValue(tag);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#removeTargetFlow(org.netbeans.modules.uml.core.metamodel.core.foundation.IFlow)
     */
    public void removeTargetFlow(IFlow flow)
    {
        m_Port.removeTargetFlow(flow);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.VersionableElement#removeVersionInformation()
     */
    public void removeVersionInformation()
    {
        m_Port.removeVersionInformation();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#retrieveAppliedStereotype(java.lang.String)
     */
    public Object retrieveAppliedStereotype(String name)
    {
        return m_Port.retrieveAppliedStereotype(name);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.VersionableElement#safeDelete()
     */
    public boolean safeDelete()
    {
        return m_Port.safeDelete();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.VersionableElement#saveIfVersioned()
     */
    public boolean saveIfVersioned()
    {
        return m_Port.saveIfVersioned();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.NamedElement#setAlias(java.lang.String)
     */
    public void setAlias(String str)
    {
        m_Port.setAlias(str);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.VersionableElement#setDirty(boolean)
     */
    public void setDirty(boolean b)
    {
        m_Port.setDirty(b);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#setDocumentation(java.lang.String)
     */
    public void setDocumentation(String doc)
    {
        m_Port.setDocumentation(doc);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.VersionableElement#setDom4JNode(org.dom4j.Node)
     */
    public void setDom4JNode(Node n)
    {
        m_Port.setDom4JNode(n);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.Feature#setFeaturingClassifier(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
     */
    public void setFeaturingClassifier(IClassifier value)
    {
        m_Port.setFeaturingClassifier(value);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.RedefinableElement#setIsFinal(boolean)
     */
    public void setIsFinal(boolean value)
    {
        m_Port.setIsFinal(value);
    }

    /**
     * @param value
     */
    public void setIsService(boolean value)
    {
        m_Port.setIsService(value);
    }

    /**
     * @param value
     */
    public void setIsSignal(boolean value)
    {
        m_Port.setIsSignal(value);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.Feature#setIsStatic(boolean)
     */
    public void setIsStatic(boolean value)
    {
        m_Port.setIsStatic(value);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.VersionableElement#setLineNumber(int)
     */
    public void setLineNumber(int num)
    {
        m_Port.setLineNumber(num);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.VersionableElement#setMarkForExtraction(boolean)
     */
    public void setMarkForExtraction(boolean b)
    {
        m_Port.setMarkForExtraction(b);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.NamedElement#setName(java.lang.String)
     */
    public void setName(String str)
    {
        m_Port.setName(str);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.NamedElement#setNamespace(org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace)
     */
    public void setNamespace(INamespace space)
    {
        m_Port.setNamespace(space);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.NamedElement#setNameWithAlias(java.lang.String)
     */
    public void setNameWithAlias(String newVal)
    {
        m_Port.setNameWithAlias(newVal);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.StructuralFeature#setNode(org.dom4j.Node)
     */
    public void setNode(Node n)
    {
        super.setNode(n);
        m_Port.setNode(n);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#setOwner(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
     */
    public void setOwner(IElement elem)
    {
        m_Port.setOwner(elem);
    }

    /**
     * @param value
     */
    public void setProtocol(IProtocolStateMachine value)
    {
        m_Port.setProtocol(value);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.VersionableElement#setVersionedFileName(java.lang.String)
     */
    public void setVersionedFileName(String str)
    {
        m_Port.setVersionedFileName(str);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.NamedElement#setVisibility(int)
     */
    public void setVisibility(int vis)
    {
        m_Port.setVisibility(vis);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.VersionableElement#setXMIID(java.lang.String)
     */
    public void setXMIID(String str)
    {
        m_Port.setXMIID(str);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.Element#topLevelId()
     */
    public String topLevelId()
    {
        return m_Port.topLevelId();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return m_Port.toString();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.VersionableElement#verifyInMemoryStatus()
     */
    public boolean verifyInMemoryStatus()
    {
        return m_Port.verifyInMemoryStatus();
    }

}
