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

package org.netbeans.modules.uml.core.metamodel.core.foundation;

import org.dom4j.Node;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlsupport.PathManip;
import org.netbeans.modules.uml.core.support.umlsupport.URILocator;

/**
 * @author sumitabhk
 *
 */
public class UMLURILocator extends URILocator
{

    private IElement m_ContextElement = null;

    /**
     *
     */
    private UMLURILocator()
    {
        super();
    }
    
    /**
     * UMLURILocator constructor
     *
     * @param[in] contextElement The element that may be queried for its
     *            Project when determining uri locations for elements passed
     *            into the GetVersionedURI() calls of this object.
     */
    public UMLURILocator(IElement contextEle)
    {
        super();
        m_ContextElement = contextEle;
    }
    
    public String getVersionedURI( IVersionableElement element )
    {
        String uri = "";
        
        if( element != null)
        {
            Node node = element.getNode();
            uri = getVersionedURI( node );
        }
        return uri;
    }
    
    /**
     *
     * Retrieves the URI that points at the passed in node. The GetVersionedURI
     * off the UMLXMLManip object will be used first. If that determines that
     * the the URI is simply the node's XMI id, then one further check is made
     * against the context element established on this object ( the UMLURILocator ).
     * If node is found to be in a different Project than the context, the a URI
     * is built that creates a relative path to the node's project from the context's
     * project.
     *
     * @param node[in] The node whose URI we need.
     *
     * @return The URI
     *
     */
    public String getVersionedURI( Node node )
    {
        String uri = "";
        if (node != null)
        {
            ETPairT<String, Boolean> verUri = UMLXMLManip.getVersionedURI(node);
            
            uri = verUri.getParamOne();
            boolean isId = verUri.getParamTwo().booleanValue();
            if (isId)
            {
                uri = validateURI(node, uri, uri);
            }
            else if (uri.length() > 0)
            {
                ETPairT<String, String> obj = uriparts(uri);
                String nodeLoc = obj.getParamTwo();
                String docLoc = obj.getParamOne();
                String contextFileName = getContextFileName();
                
                if (contextFileName.length() > 0)
                {
                    // Need to make sure that the path returned is relative to the context element
                    INamespace disp = UMLXMLManip.getProject( node );
                    if (disp instanceof IProject)
                    {
                        IProject nodeProj = (IProject)disp;
                        if (sameDocument(nodeProj))
                        {
                            //uri = PathManip.retrieveRelativePath(docLoc, contextFileName);
                        }
                        else
                        {
//                            if (disp instanceof IProject)
//                            {                                
                                String nodeFileName = nodeProj.getFileName();
                                if (nodeFileName.length() > 0)
                                {
                                    // First get the absolute path to the doc location of the node, but absolute
                                    // from that node's project
                                    String absolutePath = PathManip.retrieveAbsolutePath(docLoc, nodeFileName);
                                    if (absolutePath.length() > 0)
                                    {
                                        // Now build relative location to the contextFileName
                                        String relPath = PathManip.retrieveRelativePath(absolutePath, contextFileName);
                                        relPath += "#";
                                        relPath += nodeLoc;
                                        uri = decorateURI(relPath);
                                    }
                                }
//                            }
                        }
                    }
                }
            }
        }
        return uri;
    }
    
    /**
     *
     * Retrieves the filename of the Project that m_ContextElement is a part of
     *
     * @return The absolute path to the Project
     *
     */
    public String getContextFileName()
    {
        String fileName = "";
        if (m_ContextElement != null)
        {
            IProject proj = m_ContextElement.getProject();
            if (proj != null)
            {
                fileName = proj.getFileName();
            }
        }
        return fileName;
    }
    
    /**
     *
     * Makes sure that the in coming uri is correct, checking specifically if the node is actually
     * a node being imported from another project
     *
     * @param node[in]         The node to check against
     * @param inComingURI[in]  The uri retrieved from standard means
     * @param xmiID[in]        The XMI id of the node
     *
     * @return The URI
     *
     */
    public String validateURI( Node node, String inComingURI, String xmiID)
    {
        String uri = inComingURI;
        
        // Make an extra check against the context element established in the constructor
        // of this call and the Project node is a part of. We need
        // to see if the Projects are the same. If they are not, then we
        // will build a URI based on nodes Project location as it relates to the context
        // element's project location.
        if (m_ContextElement != null)
        {
            INamespace disp = UMLXMLManip.getProject( node );
            if (disp instanceof IProject)
            {
                IProject nodeProj = (IProject)disp;
                if (!sameDocument(nodeProj))
                {
                    String contextProjFileName = getContextFileName();
                    String nodeProjFileName = nodeProj.getFileName();
                    
                    if (contextProjFileName.length() > 0 && nodeProjFileName.length() > 0)
                    {
                        String relPath = PathManip.retrieveRelativePath(nodeProjFileName, contextProjFileName);
                        relPath += "#id('" ;
                        relPath += xmiID;
                        relPath += "')";
                        uri = decorateURI(relPath);
                    }
                }
            }
        }
        return uri;
    }
    
    /**
     *
     * Determines if the passed in node is part of the same project
     * as the internal context node
     *
     * @param node[in]   The node to test
     *
     * @return true if in the same document, else false otherwise
     *
     */
    public boolean sameDocument(IProject nodeProject)
    {
        boolean isSame = false;
//            if (node != null)
        {
            if (m_ContextElement != null)
            {
//                    INamespace temp = UMLXMLManip.getProject(node);
//                    if (temp != null && temp instanceof IProject)
                {
//                        IProject nodeProject = (IProject)temp;
                    
                    IProject contextProj = m_ContextElement.getProject();
                    if (contextProj != null)
                    {
                        isSame = contextProj.isSame((IProject)nodeProject);
                    }
                }
            }
        }
        return isSame;
    }
}



