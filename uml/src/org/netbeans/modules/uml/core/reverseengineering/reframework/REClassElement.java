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
