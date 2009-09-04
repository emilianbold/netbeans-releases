/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.jsf.palette.items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.ext.html.parser.AstNode;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.web.core.syntax.spi.JspContextInfo;
import org.netbeans.modules.web.jsf.api.palette.PaletteItem;
import org.netbeans.modules.web.jsf.palette.JSFPaletteUtilities;
import org.netbeans.modules.web.jsps.parserapi.JspParserAPI;
import org.netbeans.modules.web.jsps.parserapi.Node;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.ActiveEditorDrop;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Alexey Butenko
 */
public class MetaData implements ActiveEditorDrop, PaletteItem {

    private HashMap<String, String> properties = new HashMap<String, String>();

    private static final String TAG_NAME = "h:inputText";   //NOI18N
    private static final String NAME_SPACE = "http://java.sun.com/jsf/html";   //NOI18N
    private static final String VALUE_NAME = "value";   //NOI18N

    public boolean handleTransfer(JTextComponent targetComponent) {
        properties.clear();
        findProperties(targetComponent);

        MetaDataCustomizer customizer = new MetaDataCustomizer(this, targetComponent);
        boolean accept = customizer.showDialog();
        if (accept) {
            try {
                String body = createBody(targetComponent);
                JSFPaletteUtilities.insert(body, targetComponent);
            } catch (BadLocationException ble) {
                Exceptions.printStackTrace(ble);
                accept = false;
            }

        }
        return accept;
    }

    public String getDisplayName() {
        return NbBundle.getMessage(MetaData.class, "NAME_jsp-JsfMetadata");
    }

    public void insert(JTextComponent component) {
        handleTransfer(component);
    }

    private String createBody(JTextComponent targetComponent) {
        final StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("<f:metadata>\n");    //NOI18N
        Set<Entry<String,String>> set = properties.entrySet();
        for (Entry<String, String> entry : set) {
            stringBuffer.append("   <f:viewParam id='"+entry.getKey()+"' value='"+entry.getValue()+"'/>\n");    //NOI18N
        }
        stringBuffer.append("</f:metadata>\n");    //NOI18N
        return stringBuffer.toString();
    }

    public HashMap<String, String> getProperties() {
        return properties;
    }
    public String addProperty(String name, String value) {
        return properties.put(name, value);
    }

    public String removeProperty(String key) {
        return properties.remove(key);
    }
    
    private void findProperties(JTextComponent target){
        BaseDocument doc = (BaseDocument) target.getDocument();
        DataObject dobj = NbEditorUtilities.getDataObject(doc);
        if (dobj != null) {
            FileObject fobj = (FileObject) NbEditorUtilities.getDataObject(doc).getPrimaryFile();
            if(fobj.getMIMEType().equals("text/xhtml")) {   //NOI18N
                //we are in an xhtml file
                Source source = Source.create(doc);
                final int offset = target.getCaretPosition();
                try {
                    ParserManager.parse(Collections.singleton(source), new UserTask() {
                        @Override
                        public void run(ResultIterator resultIterator) throws Exception {
                            Result _result = resultIterator.getParserResult(offset);
                            if(_result instanceof HtmlParserResult) {
                                HtmlParserResult result = (HtmlParserResult)_result;
//                                int astOffset = result.getSnapshot().getEmbeddedOffset(offset);
                                if (result.getNamespaces().containsKey(NAME_SPACE)) {

                                    List<AstNode> foundNodes = findValue(result.root(NAME_SPACE).children(), TAG_NAME, new ArrayList<AstNode>());

                                    for (AstNode node : foundNodes) {
                                        String value = node.getAttribute(VALUE_NAME).unquotedValue();
                                        String key = generateKey(value);
                                        properties.put(key, value);
                                    }
                                }
                            }
                        }
                    });
                } catch (ParseException ex) {
                    Exceptions.printStackTrace(ex);
                }


            } else {
                //try if in a JSP...
                JspContextInfo contextInfo = JspContextInfo.getContextInfo(fobj);
                if (contextInfo != null) {
                    JspParserAPI.ParseResult result = contextInfo.getCachedParseResult(fobj, false, true);
                    if (result != null) {
                        Node.Nodes nodes = result.getNodes();
                        List<Node> foundNodes = new ArrayList<Node>();
                        foundNodes=findValue(nodes, TAG_NAME, foundNodes);
                        for (Node node: foundNodes) {
                            String ref_val = node.getAttributeValue(VALUE_NAME);
                            String key = generateKey(ref_val);
                            properties.put(key, ref_val);
                        }
                    }
                }

            }
        }

    }
    private List<AstNode> findValue(List<AstNode> nodes, String tagName, List<AstNode> foundNodes) {
        if (nodes == null) {
            return foundNodes;
        }
        for (int i = 0; i < nodes.size(); i++) {
            AstNode node = nodes.get(i);
            if (tagName.equals(node.name())) {
                foundNodes.add(node);
            } else {
                foundNodes = findValue(node.children(), tagName, foundNodes);
            }

        }
        return foundNodes;
    }
    private List<Node> findValue(Node.Nodes nodes, String tagName, List<Node> foundNodes) {
        if (nodes == null)
            return foundNodes;
        for (int i=0;i<nodes.size();i++) {
            Node node = nodes.getNode(i);
            if (tagName.equals(node.getQName())){
                foundNodes.add(node);
            } else {
                foundNodes = findValue(node.getBody(), tagName, foundNodes);
            }
        }
        return foundNodes;
    }

    private String generateKey(String value) {
        if (value.startsWith("#{")) {    //NOI18N
            value = value.substring(2, value.length()-1);
        }
        String result = value.substring(value.lastIndexOf(".")+1,value.length()).toLowerCase();
        int i=0;
        String tmp = result;
        while (properties.get(tmp) != null) {
            i++;
            tmp=result+i;
        }
        return result;
    }

}
