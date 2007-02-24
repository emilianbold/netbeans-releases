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
 * File       : GroupAction.java
 * Created on : Sep 17, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.metamodel.common.commonactions;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.basic.basicactions.IAction;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IDependency;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.Namespace;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;

/**
 * @author Aztec
 */
public class GroupAction extends CompositeAction implements IGroupAction
{
    INamespace namespace = new Namespace();

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement#setNode(org.dom4j.Node)
     */
    public void setNode(Node n)
    {
        super.setNode(n);
        namespace.setNode(n);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.IGroupAction#addSubAction(org.netbeans.modules.uml.core.metamodel.basic.basicactions.IAction)
     */
    public void addSubAction(IAction pAction)
    {
        addElement(pAction);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.IGroupAction#addVariable(org.netbeans.modules.uml.core.metamodel.common.commonactions.IVariable)
     */
    public void addVariable(IVariable pVar)
    {
        addElement(pVar);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.IGroupAction#getSubActions()
     */
    public ETList<IAction> getSubActions()
    {
        return new ElementCollector< IAction >()
            .retrieveElementCollection((IElement)this, "UML:Element.ownedElement/*[not( name(.) = 'UML:Variable')]", IAction.class);

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.IGroupAction#getVariables()
     */
    public ETList<IVariable> getVariables()
    {
        return new ElementCollector< IVariable >()
            .retrieveElementCollection((IElement)this, "UML:Element.ownedElement/UML:Variable", IVariable.class);

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.IGroupAction#removeSubAction(org.netbeans.modules.uml.core.metamodel.basic.basicactions.IAction)
     */
    public void removeSubAction(IAction pAction)
    {
        removeElement(pAction);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.common.commonactions.IGroupAction#removeVariable(org.netbeans.modules.uml.core.metamodel.common.commonactions.IVariable)
     */
    public void removeVariable(IVariable pVar)
    {
        removeElement(pVar);
    }
    
    public void establishNodePresence(Document doc, Node node)
    {
        buildNodePresence("UML:GroupAction", doc, node);
    }     

    ///////// INamespace delegate methods /////////
    public boolean isAliased()
    {
        return namespace.isAliased();
    }

    public boolean isNameSame(IBehavioralFeature feature)
    {
        return namespace.isNameSame(feature);
    }

    public INamespace getNamespace()
    {
        return namespace.getNamespace();
    }

    public ETList<IDependency> getClientDependencies()
    {
        return namespace.getClientDependencies();
    }

    public ETList<IDependency> getClientDependenciesByType(String string)
    {
        return namespace.getClientDependenciesByType(string);
    }

    public ETList<INamedElement> getOwnedElements()
    {
        return namespace.getOwnedElements();
    }

    public ETList<INamedElement> getOwnedElementsByName(String string)
    {
        return namespace.getOwnedElementsByName(string);
    }

    public ETList<IDependency> getSupplierDependencies()
    {
        return namespace.getSupplierDependencies();
    }

    public ETList<IDependency> getSupplierDependenciesByType(String string)
    {
        return namespace.getSupplierDependenciesByType(string);
    }

    public ETList<INamedElement> getVisibleMembers()
    {
        return namespace.getVisibleMembers();
    }

    public int getVisibility()
    {
        return namespace.getVisibility();
    }

    public String getAlias()
    {
        return namespace.getAlias();
    }

    public String getFullyQualifiedName(boolean par1)
    {
        return namespace.getFullyQualifiedName(par1);
    }

    public String getName()
    {
        return namespace.getName();
    }

    public String getQualifiedName()
    {
        return namespace.getQualifiedName();
    }

    public String getQualifiedName2()
    {
        return namespace.getQualifiedName2();
    }

    public long getClientDependencyCount()
    {
        return namespace.getClientDependencyCount();
    }

    public long getOwnedElementCount()
    {
        return namespace.getOwnedElementCount();
    }

    public long getSupplierDependencyCount()
    {
        return namespace.getSupplierDependencyCount();
    }

    public long getVisibleMemberCount()
    {
        return namespace.getVisibleMemberCount();
    }

    public void addClientDependency(IDependency dependency)
    {
        namespace.addClientDependency(dependency);
    }

    public boolean addOwnedElement(INamedElement element)
    {
        return namespace.addOwnedElement(element);
    }

    public void addSupplierDependency(IDependency dependency)
    {
        namespace.addSupplierDependency(dependency);
    }

    public void addVisibleMember(INamedElement element)
    {
        namespace.addVisibleMember(element);
    }

    public void removeClientDependency(IDependency dependency)
    {
        namespace.removeClientDependency(dependency);
    }

    public void removeOwnedElement(INamedElement element)
    {
        namespace.removeOwnedElement(element);
    }

    public void removeSupplierDependency(IDependency dependency)
    {
        namespace.removeSupplierDependency(dependency);
    }

    public void removeVisibleMember(INamedElement element)
    {
        namespace.removeVisibleMember(element);
    }

    public void setAlias(String string)
    {
        namespace.setAlias(string);
    }

    public void setName(String string)
    {
        namespace.setName(string);
    }

    public void setNamespace(INamespace namespace)
    {
        namespace.setNamespace(namespace);
    }

    public void setVisibility(int par1)
    {
        namespace.setVisibility(par1);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement#setNameWithAlias(java.lang.String)
     */
    public void setNameWithAlias(String newVal)
    {
        namespace.setNameWithAlias(newVal);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement#getNameWithAlias()
     */
    public String getNameWithAlias()
    {
        return namespace.getNameWithAlias();
    }

	public IPackage createPackageStructure(String packageStructure)
	{
		return namespace.createPackageStructure(packageStructure);
	}
}
