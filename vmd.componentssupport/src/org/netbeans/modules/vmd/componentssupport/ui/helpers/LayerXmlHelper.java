/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.vmd.componentssupport.ui.helpers;

import java.io.IOException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import org.openide.filesystems.FileObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 *
 * @author avk
 */
public class LayerXmlHelper extends XmlHelper{

    public static final String TEMPLATE_LAYER_XML   = "layer.xml"; //NOI18N
    
    // browsing layer.xml dom tree
    //// names
    protected static final String LAYER_FILESYSTEM    = "filesystem";             //NOI18N
    protected static final String LAYER_FOLDER        = "folder";                 //NOI18N
    protected static final String LAYER_FILE        = "file";                 //NOI18N
    protected static final String LAYER_NAME          = "name";                   //NOI18N
    protected static final String LAYER_URL           = "url";                    //NOI18N
        
    //// xpaths to tags
    private static final String LAYER_XPATH_FILESYSTEM 
                                                    = "/"+LAYER_FILESYSTEM;     //NOI18N

    protected static Node goToFilesystemNode(Document doc, XPath xpath, Node parent)
            throws XPathExpressionException 
    {
        String expression = LAYER_XPATH_FILESYSTEM;
        Node fsNode = (Node) xpath.evaluate(expression, parent, XPathConstants.NODE);
        if (fsNode == null) {
            fsNode = doc.createElement(LAYER_FILESYSTEM);
            parent.appendChild(fsNode);
        }
        return fsNode;
    }
    
    protected static FileObject createLayerXml(FileObject prjDir, String layerXmlPath) 
            throws IOException
    {
        FileObject template = getTemplate(TEMPLATE_LAYER_XML);
        return doCopyFile(prjDir, layerXmlPath, template, null);
    }
    
}
