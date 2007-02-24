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


package org.netbeans.modules.uml.core.metamodel.core.foundation;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.typemanagement.ITypeManager;

/**
 * @author sumitabhk
 *
 */
public class PackageImport extends DirectedRelationship implements IPackageImport
{
    
    /**
     *
     */
    public PackageImport()
    {
        super();
    }
    
    /**
     *
     * Description.
     *
     * @param package[out] The Package to import.
     *
     * @return S_OK
     */
    public IPackage getImportingPackage()
    {
        IPackage dummy = null;
        return getSpecificElement("importingPackage", dummy, IPackage.class);
    }
    
    /**
     *
     * Description.
     *
     * @param package[in]
     *
     * @return S_OK
     */
    public void setImportingPackage(final IPackage pack)
    {
        PreventElementReEntrance reEnt = new PreventElementReEntrance(this);
        try
        {
            if (!reEnt.isBlocking())
            {
                
                RelationshipEventsHelper help = new RelationshipEventsHelper(this);
                if( help.firePreEndModified( "importingPackage", pack, null ))
                {
                    final IPackage packag = pack;
                    IElement retEle = setSingleElementAndConnect(packag, "importingPackage",
                            new IBackPointer<IPackage>()
                    {
                        public void execute(IPackage obj)
                        {
                            obj.addPackageImport(PackageImport.this, (INamespace)pack.getOwner());
                        }
                    },
                            new IBackPointer<IPackage>()
                    {
                        public void execute(IPackage obj)
                        {
                            obj.removePackageImport(PackageImport.this);
                        }
                    }
                    );
                    
                    help.fireEndModified();
                }
            }
        }
        catch (Exception e)
        {
        }
        finally
        {
            reEnt.releaseBlock();
        }
    }
    
    /**
     * Gets the Package that is imported by an importing Package.
     *
     * @param package[in] The Package to import
     *
     * @warning NOTE: Be aware that the XML node that package represents will
     * be cloned BEFORE being set onto this PackageImport. The cloned node
     * will actually be placed back into package, added to this PackageImport,
     * then set back  to the original node. This is because the call to
     * AddChild() that this  method calls internally calls the low level
     * appendChild() method, which results in the removal of the node (in
     * this case, the node that package represents ) from the original document.
     * This is NOT what we want when importing packages from other projects.
     *
     * @result HRESULT
     */
    public IPackage getImportedPackage()
    {
        ElementCollector< IPackage > col = new ElementCollector< IPackage >();
        return col.retrieveSingleElement(m_Node, "UML:PackageImport.importedPackage/*", IPackage.class);
    }
    
    /**
     *
     * Sets package as the Package that is being imported.
     *
     * @param package[in] The Package to import.
     *
     * @return HRESULT
     * @warning NOTE: Be aware that the XML node that package represents will be cloned
     *          BEFORE being set onto this PackageImport. The cloned node will actually
     *          be placed back into package, added to this PackageImport, then set back
     *          to the original node. This is because the call to AddChild() that this
     *          method calls internally calls the low level appendChild() method, which
     *          results in the removal of the node ( in this case, the node that package
     *          represents ) from the original document. This is NOT what we want when
     *          importing packages from other projects.
     *
     */
    public void setImportedPackage(IPackage pack)
    {
        RelationshipEventsHelper help = new RelationshipEventsHelper(this);
        if( help.firePreEndModified( "UML:PackageImport.importedPackage/*", null, pack ))
        {
            Node node = pack.getNode();
            if (node != null)
            {
                // See the warning above...
                Node clonedNode = (Node)node.clone();
                pack.setNode(clonedNode);
                addChild( "UML:PackageImport.importedPackage", "UML:PackageImport.importedPackage", pack );
                pack.setNode(node);
                
                IProject proj = pack.getProject();
                if (proj != null)
                {
                    ITypeManager typeMan = UMLXMLManip.getTypeManager(this);
                    if (typeMan != null)
                    {
                        typeMan.addExternalType(pack);
                        
                        // Get the immediate owned elements of the package and add
                        // those to the type manager as well
                        ETList<INamedElement> ownedElems = pack.getOwnedElements();
                        if (ownedElems != null)
                        {
                            int count = ownedElems.size();
                            for (int i=0; i<count; i++)
                            {
                                INamedElement nElem = ownedElems.get(i);
                                typeMan.addExternalType(nElem);
                            }
                        }
                    }
                }
                
                help.fireEndModified();
            }
        }
        else
        {
            //cancel the event
        }
    }
    
    /**
     *
     * Attempts to retrieve the elements in the imported elements that
     * have a name that matches the passed in string.
     *
     * @param name[in] The name to match against
     * @param foundElements[out] The collection of found elements, else 0 if
     *									  nothing is found
     *
     * @return HRESULTs
     */
    public ETList<INamedElement> findByName(String name)
    {
        ETList<INamedElement> foundEles = null;
        IPackage pack = getImportedPackage();
        if (pack != null)
        {
            String elName = pack.getName();
            if (elName.equals(name))
            {
                foundEles = new ETArrayList<INamedElement>();
                foundEles.add(pack);
            }
            else
            {
                foundEles = findTypeInNamespace(pack, name);
            }
        }
        return foundEles;
    }
    
    /**
     *
     * Attempts to find a type by the passed in name in the passed
     * in namespace object.
     *
     * @param space[in] The namespace to search
     * @param name[in] The name to match against.
     * @param foundElements[in] The collection that has been added
     *									  to if a type in space has been found
     *									  that matches name.
     *
     * @return HRESULTs
     *	@warning foundElements must have already been allocated, as this
     *				routine simply adds to it.
     */
    private ETList<INamedElement> findTypeInNamespace(INamespace space, String name)
    {
        return space.getOwnedElementsByName(name);
    }
    
    /**
     * Establishes the appropriate XML elements for this UML type.
     *
     * [in] The document where this element will reside
     * [in] The element's parent node.
     *
     * @return HRESULT
     */
    public void establishNodePresence( Document doc, Node parent )
    {
        buildNodePresence( "UML:PackageImport", doc, parent );
        Document doc1 = getDocument();
        if (doc1 != null)
        {
            Node node = XMLManip.createElement(doc, "UML:PackageImport.importedPackage");
            //Node out = m_Node.appendChild(node);
            node.setParent((org.dom4j.Element)m_Node);
        }
    }
    
}



