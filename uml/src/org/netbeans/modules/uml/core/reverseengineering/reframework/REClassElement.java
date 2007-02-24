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

package org.netbeans.modules.uml.core.reverseengineering.reframework;

import org.dom4j.Element;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IVisibilityKind;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;

/**
 */
public class REClassElement extends ParserData implements IREClassElement
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.IREClassElement#getName()
     */
    public String getName()
    {
        return XMLManip.getAttributeValue(getEventData(), "name");
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.IREClassElement#getVisibility()
     */
    public int getVisibility()
    {
        String vis = XMLManip.getAttributeValue(getEventData(), "visibility");
        if ("public".equals(vis))
            return IVisibilityKind.VK_PUBLIC;
        else if ("private".equals(vis))
            return IVisibilityKind.VK_PRIVATE;
        else if ("protected".equals(vis))
            return IVisibilityKind.VK_PROTECTED;
        return IVisibilityKind.VK_PACKAGE;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.IREClassElement#getComment()
     */
    public String getComment()
    {
        return getTokenDescriptorValue("Comment");
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.IREClassElement#getOwner()
     */
    public IREClass getOwner()
    {
        Node parent = getOwningNamespace();
        if (parent != null)
        {
            // Make sure that the node is a UML:Class node.
            // if the node is not a UML:Class (in other words
            // it is a UML:Package node) then do nothing.
            String name = ((Element) parent).getQualifiedName();
            
            if ("UML:Class".equals(name))
            {
                IREClass c = new REClass();
                c.setEventData(parent);
                return c;
            }
        }
        return null;
    }
    
    /**
     * Retrieves the XML node that acts as a UML namespace to the specified 
     * XML node.  
     * <P>
     * <B>Note:</B>The only XMI nodes that will be owners will be UML:Package
     * and UML:Class.
     * 
     * @param owner [out] The owner of the current class element.
     */
    private Node getOwningNamespace()
    {
        return getOwningNamespace(getEventData());
    }
    
    /**
     * In XMI all elements that are contained inside of a namespace
     * will be wrapped by a UML:Namespece.ownedElement tag.  
     * 
     * Attributes and operation will always be wrapped in a 
     * UML:Classifier.feature tag.  
     * 
     * Since I am only dealing with controlled XMI fragments I will
     * take a short cut.  I know that the only scoping elements will
     * be Packages and classes.  So, I will just go up the owners until
     * I find a class tag or a package tag.  
     * 
     * In the future we may need to verify the structure of the tags.
     * 
     * @param curNode [in] The node that will have its owning namespace retrieved.
     * @param owner [out] The owner node or NULL if one was not found.
     */
    private Node getOwningNamespace(Node node)
    {
        if (node == null) return node;
        
        Node parent = node.getParent();
        if (parent != null)
        {
            String name = ((Element) parent).getQualifiedName();
            return "UML:Package".equals(name) || "UML:Class".equals(name)?
                parent
              : getOwningNamespace(parent);
        }
        return null;
    }
}