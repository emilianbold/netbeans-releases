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

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 */
public class REClass extends REClassElement implements IREClass
{
    /**
     * Retrieves the name of the package that contains the class.  If the
     * class does not belong to a package the name will be an empty string.
     * @param pVal [out] The package that owns the class.
     */
    public String getPackage()
    {
        String pack = null;
        ITokenDescriptor desc = getTokenDescriptor("Package");
        if (desc != null)
            pack = desc.getValue();
        // Get then out class name as well (if there is an outer
        // class).  The package name is 
        // <Package Structure>::<Outer Class Strucuter>
        Node node = getEventData();
        Node parent = node != null? node.getParent() : null;
        if (parent != null)
        {
            // When the parent is a package we do not want to continue to search for
            // the name.  The package token descriptor has all of the information 
            // that we need.
            if (!isPackageNode(parent))
            {
                String packageStr = getPackageName(parent);
                if ((packageStr != null) && (packageStr.length() > 0))
                {
                   if((pack != null) && (pack.length() > 0))
                   {
                      pack = pack + "::" + packageStr;
                   }
                   else
                   {
                      pack = packageStr;
                   }
                }
            }
        }
        return pack;
    }

    /**
     * Retrieves the name of a class' package by traversing the parent nodes.
     * If the parent node has a <em>name</em> attribute, the name will be 
     * appended to the package name.
     * @param pNode [in] The start node used to find the package name.
     * @param name [out] The name of the package.
     */
    private String getPackageName(Node node)
    {
        String name = "";
        if (node != null && !(node instanceof Document))
        {
            Node parent = node.getParent();
            if (parent != null)
                name = getPackageName(parent);
            
            String type = ((Element)node).getQualifiedName();
            if ("UML:Class".equals(type))
            {
                String nameS = XMLManip.getAttributeValue(node, "name");
                name = name.length() > 0? (name + "::" + nameS) : nameS;
            }
        }
        return name;
    }

    /**
     * @param parent
     * @return
     */
    private boolean isPackageNode(Node parent)
    {
        if (parent != null)
        {
            String nodename = ((Element)parent).getQualifiedName();
            if ("UML:Package".equals(nodename))
                return true;
            else if ("UML:Element.ownedElement".equals(nodename))
                return isPackageNode(parent.getParent());
        }
        return false;
    }

    /**
     * Specifies whether the element may not have a direct instance. True 
     * indicates that an instance of the element must be an instance of a 
     * child of the element. False indicates that there may an instance of 
     * the element that is not an instance of a child.
     * @param pVal [out] true if abstract, false otherwise.
     */
    public boolean getIsAbstract()
    {
        return XMLManip.getAttributeBooleanValue(getEventData(), "isAbstract");
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass#getIsLeaf()
     */
    public boolean getIsLeaf()
    {
        return XMLManip.getAttributeBooleanValue(getEventData(), "isLeaf");
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass#getOperations()
     */
    public ETList<IREOperation> getOperations()
    {
        REXMLCollection<IREOperation> coll =
                new REXMLCollection<IREOperation>(
                    REOperation.class,
                    "UML:Element.ownedElement/UML:Operation");
        try
        {
            coll.setDOMNode(getEventData());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return coll;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass#getAttributes()
     */
    public ETList<IREAttribute> getAttributes()
    {
        REXMLCollection<IREAttribute> coll =
                new REXMLCollection<IREAttribute>(
                    REAttribute.class,
                    "UML:Element.ownedElement/UML:Attribute");
        try
        {
            coll.setDOMNode(getEventData());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return coll;
    }

    /**
     * Retrieves the collection of super classes for the class..
     * @param pVal [out] The class's super classes.
     */
    public IREGeneralization getGeneralizations()
    {
        IREGeneralization gen = new REGeneralization();
        gen.setDOMNode(getGeneralization(getEventData()));
        return gen;
    }
    
    /**
     * Retrieves the token descriptors for the parser event.  The token descriptors will 
     * be contained by the specified node.
     * @param node The DOM node to search.
     * @param pVal The token descriptors for the parser data.
     * @return S_OK if successful.
     */
    private Node getGeneralization(Node node)
    {
        Node s = XMLManip.selectSingleNode(node, "TokenDescriptors/TGeneralization");
        if (s == null)
        {
            // The library file is screwed up.  So until we formalize the library structure
            // the library file will remain screwed up.
            s = XMLManip.selectSingleNode(node, "TGeneralization");
        }
        return s;
    }
    
    /**
     * Retrieves the token descriptors for the parser event.  The token descriptors will 
     * be contained by the specified node.
     * @param node The DOM node to search.
     * @param pVal The token descriptors for the parser data.
     * @return S_OK if successful.
     */
    private Node getRealization(Node node)
    {
        Node s = XMLManip.selectSingleNode(node, "TokenDescriptors/TRealization");
        if (s == null)
        {
            // The library file is screwed up.  So until we formalize the library structure
            // the library file will remain screwed up.
            s = XMLManip.selectSingleNode(node, "TRealization");
        }
        return s;
    }

    /**
     * Retrieves the collection of implemented interfaces.
     * @param pVal [out] The interfaces that are implemented by he class.
     */
    public IRERealization getRealizations()
    {
        IRERealization real = new RERealization();
        real.setDOMNode(getRealization(getEventData()));
        return real;
    }

    /**
     * Retrieves all inner classes and interfaces.
     * @param pVal [out] A collection of inner classes.
     */
    public ETList<IREClass> getAllInnerClasses()
    {
        REClasses c = new REClasses();
        try
        {
            c.setDOMNode(getEventData());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return c;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.IREClass#getIsInterface()
     */
    public boolean getIsInterface()
    {
        return "UML:Interface".equals(getEventData().getName());
    }
}
