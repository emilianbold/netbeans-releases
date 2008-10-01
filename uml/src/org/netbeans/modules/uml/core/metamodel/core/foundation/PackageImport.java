/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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


package org.netbeans.modules.uml.core.metamodel.core.foundation;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.profiles.Profile;
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
                if (proj != null || pack instanceof Profile)
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



