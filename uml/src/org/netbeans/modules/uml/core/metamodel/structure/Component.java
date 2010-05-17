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

package org.netbeans.modules.uml.core.metamodel.structure;

import org.dom4j.Document;
import org.dom4j.Node;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IStateMachine;
import org.netbeans.modules.uml.core.metamodel.core.constructs.Class;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementConnector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IBackPointer;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IDependency;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementImport;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRedefinableElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.UMLXMLManip;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IConnector;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IPart;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IPort;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IStructuredClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterableElement;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public class Component extends Class implements IComponent
{
    public ETList<IArtifact> getArtifacts()
    {
        ElementCollector<IArtifact> collector = new ElementCollector<IArtifact>();
        return collector.retrieveElementCollectionWithAttrIDs(this,"implementingArtifact", IArtifact.class);
    }
    
    public void removeArtifact( IArtifact artifact )
    {
        final IArtifact art = artifact;
        new ElementConnector<INamedElement>().removeByID
                (
                this,art,"implementingArtifact",
                new IBackPointer<INamedElement>()
        {
            public void execute(INamedElement obj)
            {
                art.removeImplementedElement(obj);
            }
        }
        );
    }
    
    public void addArtifact( IArtifact artifact )
    {
        final IArtifact art = artifact;
        new ElementConnector<INamedElement>().addChildAndConnect(
                this, true, "implementingArtifact",
                "implementingArtifact", art,
                new IBackPointer<INamedElement>()
        {
            public void execute(INamedElement obj)
            {
                art.addImplementedElement(obj);
            }
        }
        );
    }
    
    public ETList<INode> getNodes()
    {
        ElementCollector<INode> collector = new ElementCollector<INode>();
        return collector.retrieveElementCollectionWithAttrIDs(this,"location", INode.class);
    }
    
    public void removeNode( INode inNode )
    {
        final INode node = inNode;
        new ElementConnector<INamedElement>().removeByID
                (
                this,node,"location",
                new IBackPointer<INamedElement>()
        {
            public void execute(INamedElement obj)
            {
                node.removeDeployedElement(obj);
            }
        }
        );
    }
    
    public void addNode( INode inNode )
    {
        final INode node = inNode;
        new ElementConnector<INamedElement>().addChildAndConnect(
                this, true, "location",
                "location", node,
                new IBackPointer<INamedElement>()
        {
            public void execute(INamedElement obj)
            {
                node.addDeployedElement(obj);
            }
        }
        );
    }
    
    public ETList<IPart> getInternalClassifiers()
    {
        ElementCollector<IPart> collector = new ElementCollector<IPart>();
        return collector.retrieveElementCollectionWithAttrIDs(this,"internalClassifier", IPart.class);
    }
    
    public void removeInternalClassifier( IPart internalPart )
    {
        removeElementByID(internalPart,"internalClassifier");
    }
    
    public void addInternalClassifier( IPart internalPart )
    {
        addElementByID(internalPart,"internalClassifier");
    }
    
    public ETList<IConnector> getInternalConnectors()
    {
        ElementCollector<IConnector> coll = new ElementCollector<IConnector>();
        return coll.retrieveElementCollection(m_Node,"UML:Component.internalConnector/*", IConnector.class);
    }
    
    public void removeInternalConnector( IConnector connector )
    {
        UMLXMLManip.removeChild(m_Node, connector);
    }
    
    public void addInternalConnector( IConnector connector )
    {
        addChild("UML:Component.internalConnector",
                "UML:Component.internalConnector",connector);
    }
    
    public ETList<IElementImport> getElementImports()
    {
        ElementCollector<IElementImport> coll = new ElementCollector<IElementImport>();
        return coll.retrieveElementCollection(m_Node,"UML:Component.elementImport/*", IElementImport.class);
    }
    
    public void removeElementImport( IElementImport element )
    {
        UMLXMLManip.removeChild(m_Node, element);
    }
    
    public void addElementImport( IElementImport element )
    {
        addChild("UML:Component.elementImport",
                "UML:Component.elementImport",element);
    }
    
    public IStateMachine getSpecifyingStateMachine()
    {
        ElementCollector<IStateMachine> collector =
                new ElementCollector<IStateMachine>();
        return collector.retrieveSingleElement(m_Node,
                "UML:Component.specifyingStateMachine/*", IStateMachine.class);
    }
    
    public void setSpecifyingStateMachine( IStateMachine value )
    {
        addChild("UML:Component.specifyingStateMachine",
                "UML:Component.specifyingStateMachine",value);
    }
    
    public ETList<IPort> getExternalInterfaces()
    {
        ElementCollector<IPort> coll = new ElementCollector<IPort>();
        return coll.retrieveElementCollection(m_Node,"UML:Component.externalInterface/*", IPort.class);
    }
    
    public void removeExternalInterface( IPort ext )
    {
        UMLXMLManip.removeChild(m_Node, ext);
    }
    
    public void addExternalInterface( IPort ext )
    {
        addChild("UML:Component.externalInterface",
                "UML:Component.externalInterface",ext);
    }
    
    public int getInstantiation()
    {
        return super.getInstantiationKindValue("instantiation");
    }
    
    public void setInstantiation( /* InstantiationKind */ int value )
    {
        super.setInstantiationValue("instantiation",value);
    }
    
    public void addDeploymentSpecification( IDeploymentSpecification pSpec )
    {
        final IDeploymentSpecification spec = pSpec;
        new ElementConnector<IComponent>().addChildAndConnect(
                this, true, "deploymentSpecification",
                "deploymentSpecification", spec,
                new IBackPointer<IComponent>()
        {
            public void execute(IComponent obj)
            {
                spec.setConfiguredComponent(obj);
            }
        }
        );
    }
    
    public void removeDeploymentSpecification( IDeploymentSpecification pSpec )
    {
        final IDeploymentSpecification spec = pSpec;;
        new ElementConnector<IComponent>().removeByID
                (
                this,spec,"deploymentSpecification",
                new IBackPointer<IComponent>()
        {
            public void execute(IComponent obj)
            {
                spec.setConfiguredComponent(obj);
            }
        }
        );
    }
    
    public ETList<IDeploymentSpecification> getDeploymentSpecifications()
    {
        ElementCollector<IDeploymentSpecification> collector =
                new ElementCollector<IDeploymentSpecification>();
        return collector.retrieveElementCollectionWithAttrIDs(this,"deploymentSpecification", IDeploymentSpecification.class);
    }
    
    public void addAssembly( IComponentAssembly assembly )
    {
        final IComponentAssembly assem = assembly;
        new ElementConnector<IComponent>().addChildAndConnect(
                this, true, "assembly",
                "assembly",assem,
                new IBackPointer<IComponent>()
        {
            public void execute(IComponent obj)
            {
                assem.addComponent(obj);
            }
        }
        );
    }
    
    public void removeAssembly( IComponentAssembly assembly )
    {
        final IComponentAssembly assem = assembly;
        new ElementConnector<IComponent>().removeByID
                (
                this,assem,"assembly",
                new IBackPointer<IComponent>()
        {
            public void execute(IComponent obj)
            {
                assem.removeComponent(obj);
            }
        }
        );
    }
    
    public ETList<IComponentAssembly> getAssemblies()
    {
        ElementCollector<IComponentAssembly> collector =
                new ElementCollector<IComponentAssembly>();
        return collector.retrieveElementCollectionWithAttrIDs(this,"assembly", IComponentAssembly.class);
    }
    
    public ETList<String> getCollidingNamesForElement()
    {
        Node node = getNode();
        ETList<String> retVal = new ETArrayList<String>();
        if (node != null)
        {
            String elementType = node.getName();
            if (elementType != null)
            {
                String s = "UML:" + elementType;
                retVal.add(s);
            }
        }
        return retVal;
    }
    
    /**
     * Establishes the appropriate XML elements for this UML type.
     *
     * [in] The document where this element will reside
     * [in] The element's parent node.
     */
    public void establishNodePresence(Document doc, Node parent)
    {
        buildNodePresence("UML:Component", doc, parent);
    }
    
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.structure.IComponent#isInternalClassifier(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier)
    */
    public boolean getIsInternalClassifier(IClassifier classifier)
    {
        if( null == classifier ) throw new IllegalArgumentException();
        if( null == m_Node ) throw new IllegalArgumentException();
        
        boolean bIsInternal = false;
        
        if (classifier instanceof IStructuredClassifier)
        {
            IStructuredClassifier structClass = (IStructuredClassifier)classifier;
            
            ETList< IPart > parts = structClass.getParts();
            if( parts != null )
            {
                int num = parts.getCount();
                for( int x = 0; x < num; x++ )
                {
                    IPart part = parts.item( x );
                    assert ( part != null );
                    
                    if( part != null )
                    {
                        boolean isWhole = part.getIsWhole();
                        if( isWhole )
                        {
                            String partID = part.getXMIID();
                            assert ( partID.length() > 0 );
                            
                            if( partID.length() > 0 )
                            {
                                String query = "@internalClassifier[ contains( ., \"";
                                query += partID;
                                query +=  "\" ]";
                                
                                Node foundNode = m_Node.selectSingleNode( query );
                                if( foundNode != null )
                                {
                                    bIsInternal = true;
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }
        
        return bIsInternal;
    }
    
    public void delete()
    {
        ETList<IPort> interfaces = getExternalInterfaces();
        for(IPort port: interfaces)
        {
            port.delete();
        }
        super.delete();
    }
}


