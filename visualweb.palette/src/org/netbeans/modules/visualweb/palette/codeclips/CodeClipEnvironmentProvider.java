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
/*
 * CodeClipEnvironmentProvider.java
 *
 * Created on July 27, 2006, 10:16 AM
 *
 * The is the environment provider for all codeclips.  In the System FileSystem
 * this file is specified to be used whenever loading codeclips as defined by
 * the codeclip dtd.
 *
 * @author Joelle Lam <joelle.lam@sun.com>
 * @version %I%, %G%
 * @see layer.xml
 */

package org.netbeans.modules.visualweb.palette.codeclips;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.loaders.Environment;
import org.openide.loaders.XMLDataObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.xml.EntityCatalog;
import org.openide.xml.XMLUtil;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 *

 */
public class CodeClipEnvironmentProvider implements Environment.Provider {

    private static CodeClipEnvironmentProvider createProvider() {
        return new CodeClipEnvironmentProvider();
    }

    private CodeClipEnvironmentProvider() {
    }

// ----------------   Environment.Provider ----------------------------

    public Lookup getEnvironment(DataObject obj) {

        CodeClipNodeFactory nodeFactory = new CodeClipNodeFactory((XMLDataObject)obj);
        return nodeFactory.getLookup();
    }


    private static class CodeClipNodeFactory implements InstanceContent.Convertor {

        private XMLDataObject xmlDataObject = null;

        private Lookup lookup = null;

        Reference<CodeClipItemNode> refNode = new WeakReference<CodeClipItemNode>(null);

        CodeClipNodeFactory(XMLDataObject obj) {

            xmlDataObject = obj;

            InstanceContent content = new InstanceContent();
            content.add(Node.class, this);

            lookup = new AbstractLookup(content);
        }

        Lookup getLookup() {
            return lookup;
        }

        // ----------------   InstanceContent.Convertor ----------------------------

        public Class type(Object obj) {
            return (Class)obj;
        }

        public String id(Object obj) {
            return obj.toString();
        }

        public String displayName(Object obj) {
            return ((Class)obj).getName();
        }

        public Object convert(Object obj) {
            Object o = null;
            if (obj == Node.class) {
                try {
                    o = getInstance();
                } catch (Exception ex) {
                    ErrorManager.getDefault().notify(ex);
                }
            }

            return o;
        }

        // ----------------   helper methods  ----------------------------

        public synchronized CodeClipItemNode getInstance() {

            CodeClipItemNode node = refNode.get();
            if (node != null)
                return node;

            FileObject file = xmlDataObject.getPrimaryFile();
            if (file.getSize() == 0L) // item file is empty
                return null;

            CodeClipHandler handler = new CodeClipHandler();
            try {
                XMLReader reader = XMLUtil.createXMLReader(true);
                reader.setContentHandler(handler);
                reader.setEntityResolver(EntityCatalog.getDefault());
                String urlString = xmlDataObject.getPrimaryFile().getURL().toExternalForm();
                InputStream inputStream = xmlDataObject.getPrimaryFile().getInputStream();
                InputStreamReader in = new InputStreamReader(inputStream, "UTF8");
                InputSource is =new InputSource(in);
                //Force UTF8 Reading.
                //InputSource is = new InputSource(xmlDataObject.getPrimaryFile().getInputStream());
                is.setSystemId(urlString);
                reader.parse(is);
            }
            catch (SAXException saxe) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, saxe);
            } 
            catch (IOException ioe) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
            }

            node = createPaletteItemNode(handler);
            refNode = new WeakReference<CodeClipItemNode>(node);

            return node;
        }

        private CodeClipItemNode createPaletteItemNode(CodeClipHandler handler) {
            
            String name = xmlDataObject.getName();
            InstanceContent ic = new InstanceContent();   
          
//            if (handler.getBody() != null && handler.getBundleName() != null)  {   
              if (handler.getBody() != null)  {  
                ArrayList<Object> codeclipArray = new ArrayList<Object>(2);
                codeclipArray.add(0, handler.getBundleName());
                codeclipArray.add(1, handler.getBody());
                codeclipArray.add(2, handler.getDisplayNameKey());                
                ic.add( codeclipArray , ActiveEditorDropCodeClipProvider.getInstance()); 
           }
           
            CodeClipItemNode node = new CodeClipItemNode(
                    new DataNode(xmlDataObject, Children.LEAF), 
                    name, 
                    handler.getBundleName(), 
                    handler.getDisplayNameKey(),
                    handler.getTooltipKey(), 
                    handler.getIcon16URL(), 
                    handler.getIcon32URL(), 
                    handler.getBody(),
                    ic
            );

            return node;
        }
    }        

}
