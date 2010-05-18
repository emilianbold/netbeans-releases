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

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author avk
 */
public class ProjectXmlHelper extends XmlHelper{

        //// project.xml tags
        private static final String PROJECT_TAG_MODULE_DEPENDENCIES 
                        = "module-dependencies"; // NOI18N
        private static final String PROJECT_TAG_DEPENDENCY 
                        = "dependency"; // NOI18N
        private static final String PROJECT_TAG_CODE_NAME_BASE 
                        = "code-name-base"; // NOI18N
        private static final String PROJECT_TAG_BUILD_PREREQUISITE 
                        = "build-prerequisite"; // NOI18N
        private static final String PROJECT_TAG_COMPILE_DEPENDENCY 
                        = "compile-dependency"; // NOI18N
        private static final String PROJECT_TAG_RUN_DEPENDENCY 
                        = "run-dependency"; // NOI18N
        private static final String PROJECT_TAG_SPECIFICATION_VERSION 
                        = "specification-version"; // NOI18N
    
    //// xpaths to tags
    private static final String PROJECT_XPATH_TAG_DATA 
                        = "/project/configuration/data";     //NOI18N
        
    public static Node getPrimaryConfigurationData(XPath xpath, Element parent) 
            throws XPathExpressionException 
    {
        String expression = PROJECT_XPATH_TAG_DATA;
        Node confData = (Node) xpath.evaluate(expression, parent, XPathConstants.NODE);
        assert confData != null;
        return confData;
    }

    public static Node goToModuleDependencies(Document doc, XPath xpath, Node parent) 
            throws XPathExpressionException 
    {
        return goAndCreateNode(doc, xpath, parent, PROJECT_TAG_MODULE_DEPENDENCIES);
    }
    
    public static Node testAndAddDependency(Document doc, XPath xpath, 
            Node modDepsNode, String moduleName, String moduleVersion) 
            throws XPathExpressionException
    {
        Node node = getDependency(xpath, modDepsNode, moduleName);
        if (node == null){
            node = addDependency(doc, xpath, modDepsNode, moduleName, moduleVersion);
        }
        return node;
    }
    
    public static Node getDependency(XPath xpath, Node modDepsNode, 
            String moduleName) 
            throws XPathExpressionException 
    {
        String expression = "./" + PROJECT_TAG_DEPENDENCY +                     // NOI18N
                        "[" + PROJECT_TAG_CODE_NAME_BASE + "=\"" + moduleName + "\"]"; // NOI18N
        Node node = (Node) xpath.evaluate(expression, modDepsNode, XPathConstants.NODE);
        
        return node;
    }
    
    public static Node addDependency(Document doc, XPath xpath, Node modDepsNode, 
            String moduleName, String moduleVersion) 
            throws XPathExpressionException 
    {

        Element dependency = doc.createElement(PROJECT_TAG_DEPENDENCY);

        Element cnb = doc.createElement(PROJECT_TAG_CODE_NAME_BASE);
        cnb.setTextContent(moduleName);
        dependency.appendChild(cnb);

        Element prerequisite = doc.createElement(PROJECT_TAG_BUILD_PREREQUISITE);
        dependency.appendChild(prerequisite);

        Element compile = doc.createElement(PROJECT_TAG_COMPILE_DEPENDENCY);
        dependency.appendChild(compile);

        Element run = doc.createElement(PROJECT_TAG_RUN_DEPENDENCY);
        Element version = doc.createElement(PROJECT_TAG_SPECIFICATION_VERSION);
        version.setTextContent(moduleVersion);
        run.appendChild(version);
        dependency.appendChild(run);

        modDepsNode.appendChild(dependency);
        return dependency;
    }
    
    private static Node goAndCreateNode(Document doc, XPath xpath, Node parent, String name) 
            throws XPathExpressionException
    {
        String expression = "./" + name; // NOI18N
        Node node = (Node) xpath.evaluate(expression, parent, XPathConstants.NODE);
        if (node == null){
            Element el = doc.createElement(name);
            parent.appendChild(el);
            node = el;
        }
        return node;
    }

}
