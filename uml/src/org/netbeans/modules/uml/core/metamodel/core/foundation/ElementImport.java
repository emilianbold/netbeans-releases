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

import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.typemanagement.ITypeManager;

/**
 * @author sumitabhk
 *
 */
public class ElementImport extends DirectedRelationship implements IElementImport
{

    /**
     *
     */
    public ElementImport()
    {
        super();
    }

    /**
     * Gets the Package that relies on a PackageableElement from
     * another Package
     *
     * @param package[out]
     *
     * @return HRESULT
     */
    public IPackage getImportingPackage()
    {
        IPackage dummy = null;
        return getSpecificElement("importingPackage", dummy, IPackage.class);
    }
    
    /**
     * Sets the Package that relies on a PackageableElement from
     * another Package
     *
     * @param package[in]
     *
     * @return HRESULT
     */
    public void setImportingPackage(IPackage pack)
    {
        PreventElementReEntrance reEnt = new PreventElementReEntrance(this);
        try
        {
            if (!reEnt.isBlocking())
            {
                RelationshipEventsHelper help = new RelationshipEventsHelper(this);
                if (help.firePreEndModified("importingPackage", pack, null))
                {
                    final IPackage packag = pack;
                    addChildAndConnect(
                            true, "importingPackage",
                            "importingPackage", packag,
                            new IBackPointer<IElementImport>()
                    {
                        public void execute(IElementImport obj)
                        {
                            packag.addElementImport(obj, (INamespace)obj.getOwner());
                        }
                    }
                    );
                    help.fireEndModified();
                }
                else
                {
                    //cancel the event
                }
            }
        }
        catch(Exception e)
        {
        }
        finally
        {
            reEnt.releaseBlock();
        }
    }
    
    /**
     * Gets the PackageableElement that an importingPackage imports
     *
     * @param element[out]
     *
     * @return HRESULT
     */
    public IAutonomousElement getImportedElement()
    {
        ElementCollector< IAutonomousElement > col = new ElementCollector< IAutonomousElement >();
        return col.retrieveSingleElement(m_Node, "UML:ElementImport.importedElement/*", IAutonomousElement.class);
    }
    
    /**
     * Sets the PackageableElement that an importingPackage imports. This ElementImport
     * must already belong to a namespace!
     *
     * @param element[in]
     *
     * @return HRESULT
     * @warning NOTE: Be aware that the XML node that package represents will be cloned
     *          BEFORE being set onto this ElementImport. The cloned node will actually
     *          be placed back into element, added to this ElementImport, then set back
     *          to the original node. This is because the call to AddChild() that this
     *          method calls internally calls the low level appendChild() method, which
     *          results in the removal of the node ( in this case, the node that package
     *          represents ) from the original document. This is NOT what we want when
     *          importing elements from other projects.
     */
    public void setImportedElement(IAutonomousElement elem)
    {
        RelationshipEventsHelper help = new RelationshipEventsHelper(this);
        if (help.firePreEndModified("UML:ElementImport.importedElement/*", null, elem))
        {
            Node node = elem.getNode();
            
            // See the warning above...
            Node clonedNode = (Node)node.clone();
            clonedNode.setDocument(node.getDocument());
            elem.setNode(clonedNode);
            ITypeManager tMan = UMLXMLManip.getTypeManager(this);
            if (tMan != null)
            {
                tMan.addExternalType(elem);
            }
            
            addChild("UML:ElementImport.importedElement", "UML:ElementImport.importedElement", elem);
            elem.setNode(node);
            help.fireEndModified();
        }
        else
        {
            //cancel the event.
        }
    }
    
    /**
     * Gets the visibility of the imported PackageableElement within the
     * importing Package.
     *
     * @param pVal[out]
     *
     * @return HRESULT
     */
    public int getVisibility()
    {
        return getVisibilityKindValue("visibility");
    }
    
    /**
     * Sets the visibility of the imported PackageableElement within the
     * importing Package.
     *
     * @param newVal[out]
     *
     * @return HRESULT
     */
    public void setVisibility(int val)
    {
        setVisibilityKindValue("visibility", val);
    }
    
    /**
     * Gets the name of an imported PackageableElement that is to
     * be used instead of its name within the importing Package.  By
     * default, no alias is used.
     *
     * @param pVal[out]
     *
     * @return HRESULT
     */
    public String getAlias()
    {
        return getAttributeValue("alias");
    }
    
    /**
     * Sets the name of an imported PackageableElement that is to
     * be used instead of its name within the importing Package.  By
     * default, no alias is used.
     *
     * @param newVal[in]
     *
     * @return HRESULT
     */
    public void setAlias(String val)
    {
        setAttributeValue("alias", val);
    }
    
    /**
     * Establishes the appropriate XML elements for this UML type.
     *
     * [in] The document where this element will reside
     * [in] The element's parent node.
     *
     * @return HRESULT
     */
    public void establishNodePresence(Document doc, Node parent)
    {
        buildNodePresence( "UML:ElementImport", doc, parent );
        Document doc2 = getDocument();
        if (doc2 != null)
        {
            Node n = XMLManip.createElement(doc2, "UML:ElementImport.importedElement");
            n.setParent((org.dom4j.Element)m_Node);
            //m_Node.appendChild(n);
        }
    }
}



