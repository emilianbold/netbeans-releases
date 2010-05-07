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
package org.netbeans.editor.ext.html.parser;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.editor.ext.html.dtd.DTD;

/**
 * Html parser result.
 *
 * @author mfukala@netbeans.org
 */
public class SyntaxParserResult {

    /** special namespace which can be used for obtaining a parse tree
     * of tags with undeclared namespace.
     */
    public static String UNDECLARED_TAGS_NAMESPACE = "undeclared_tags_namespace"; //NOI18N
    private static String UNDECLARED_TAGS_PREFIX = "undeclared_tags_prefix"; //NOI18N

    private static final String FALLBACK_DOCTYPE =
            "-//W3C//DTD HTML 4.01 Transitional//EN";  // NOI18N
    private SyntaxParserContext context;

    private String publicID;

    //ns URI to AstNode map
    private Map<String, AstNode> astRoots;

    //ns URI to PREFIX map
    private Map<String, String> namespaces;

    public SyntaxParserResult(SyntaxParserContext context) {
        this.context = context;
    }

    public CharSequence getSource() {
        return context.getSourceText();
    }

    public List<SyntaxElement> getElements() {
        return context.getElements();
    }

    public synchronized AstNode getASTRoot() {
        return getASTRoot(null); //get default ns tree
    }

    //naive implementation:
    //doesn't take namespaces overriding into account
    //doesn't take namespaces context into account
    //doesn't support multiple prefixes for one namespace
    public synchronized AstNode getASTRoot(String namespace) {
        return getASTRoot(namespace, null);
    }

    /** the defaultDTD is used when no DTD is determined based on the
     * doctype declaration in the source */
    public synchronized AstNode getASTRoot(String namespace, DTD defaultDTD) {
        //compatibility, test
        if(namespace == null && defaultDTD == null) {
            //scecify default dtd
            defaultDTD = getFallbackDTD();
        }


        if(astRoots == null) {
             astRoots = new HashMap<String, AstNode>();
        }

        if(astRoots.containsKey(namespace)) {
            return astRoots.get(namespace);
        } else {
            //filter the elements
            List<SyntaxElement> filtered = new ArrayList<SyntaxElement>();

            String prefix = UNDECLARED_TAGS_NAMESPACE.equals(namespace) ?
                UNDECLARED_TAGS_PREFIX:
                getDeclaredNamespaces().get(namespace);
            
            for(SyntaxElement e : getElements()) {
                if(e.type() == SyntaxElement.TYPE_TAG || e.type() == SyntaxElement.TYPE_ENDTAG) {
                    SyntaxElement.Tag tag = (SyntaxElement.Tag)e;
                    String tagNamePrefix = getTagNamePrefix(tag);
                    if((tagNamePrefix == null && prefix == null) ||
                            (tagNamePrefix != null && UNDECLARED_TAGS_PREFIX.equals(prefix)  && !getDeclaredNamespaces().containsValue(tagNamePrefix)) || //unknown prefixed tags falls to the 'undeclared tags parse tree'
                            (tagNamePrefix != null && prefix != null && tagNamePrefix.equals(prefix))) {
                        //either the tag has no prefix and the prefix is null
                        //or the prefix matches
                        filtered.add(e);
                    }
                } else {
                    //do not filter the other types
                    filtered.add(e);
                }
            }

            DTD dtd = null;
            if(getDTDNoFallback() != null) {
                if(defaultDTD != null) {
                    //DTD found for this file and preferred dtd is specified
                    //this means we are trying to parse html or xhtml tags
                    dtd = getDTDNoFallback();
                } else {
                    //DTD found, but no preferred dtd specifes,
                    //this means that one wants to parse some non-html stuff
                    dtd = null;
                }
            } else {
                if(defaultDTD != null) {
                    dtd = defaultDTD;
                }
            }

            AstNode root = SyntaxTree.makeTree(context.clone().setElements(filtered).setDTD(dtd));
            root.setProperty(AstNode.NAMESPACE_PROPERTY, namespace); //NOI18N
            astRoots.put(namespace, root);
            return root;
        }
    }

    private String getTagNamePrefix(SyntaxElement.Tag tag) {
        String tName = tag.getName();
        int colonPrefix = tName.indexOf(':');
        if(colonPrefix == -1) {
            return null;
        } else {
            return tName.substring(0, colonPrefix);
        }
    }

    public synchronized String getPublicID() {
        if (this.publicID == null) {
            for (SyntaxElement e : getElements()) {
                if (e.type() == SyntaxElement.TYPE_DECLARATION) {
                    String _publicID = ((SyntaxElement.Declaration) e).getPublicIdentifier();
                    if (_publicID != null) {
                        this.publicID = _publicID;
                        break;
                    }
                }
            }
        }
        return this.publicID;
    }

    /** Returns a map of namespace URI to prefix used in the document
     * Not only globaly registered namespace (root tag) are taken into account.
     */
    // URI to prefix map
    public synchronized Map<String, String> getDeclaredNamespaces() {
        if (namespaces == null) {
            this.namespaces = new HashMap<String, String>();
            for (SyntaxElement se : getElements()) {
                if (se.type() == SyntaxElement.TYPE_TAG) {
                    SyntaxElement.Tag tag = (SyntaxElement.Tag) se;
                    for (SyntaxElement.TagAttribute attr : tag.getAttributes()) {
                        String attrName = attr.getName();
                        if (attrName.startsWith("xmlns")) {
                            int colonIndex = attrName.indexOf(':');
                            String nsPrefix = colonIndex == -1 ? null : attrName.substring(colonIndex + 1);
                            String value = attr.getValue();
                            //do not overwrite already existing entry
                            if(!namespaces.containsKey(dequote(value))) {
                                namespaces.put(dequote(value), nsPrefix);
                            }
                        }
                    }
                }
            }
        }

//        //add default xhtml namespace if necessary
//        if(!namespaces.containsKey(Utils.XHTML_NAMESPACE)) {
//            namespaces.put(Utils.XHTML_NAMESPACE, null);
//        }

        return namespaces;
    }

    @Deprecated
    public Map<String, URI> getGlobalNamespaces() {
        Map<String, URI> _namespaces = new HashMap<String, URI>();
        AstNode root = getASTRoot();
        //scan all root children (real document root) for namespaces
        for (AstNode n : root.children()) {
            if (n.type() == AstNode.NodeType.OPEN_TAG) {
                for (String attrName : n.getAttributeKeys()) {
                    if (attrName.startsWith("xmlns")) { //NOI18N
                        int colonIndex = attrName.indexOf(':');//NOI18N
                        String nsPrefix = colonIndex == -1 ? "" : attrName.substring(colonIndex + 1);//NOI18N
                        AstNode.Attribute attr = n.getAttribute(attrName);
                        try {
                            _namespaces.put(nsPrefix, new URI(attr.unquotedValue()));
                        } catch (URISyntaxException ex) {
                            //TODO - report error in the editor
                        }
                    }
                }
            }
        }
        return _namespaces;
    }

     private DTD getDTDNoFallback() {
        if (getPublicID() == null) {
            //return context DTD in case that the context creator wants to explicitly
            //define the fallback DTD
            if(context.getDTD() != null) {
                return context.getDTD();
            }
        } else {
            return org.netbeans.editor.ext.html.dtd.Registry.getDTD(getPublicID(), null);
        }

        return null;
    }

    public DTD getDTD() {
        if (getPublicID() == null) {
            //return context DTD in case that the context creator wants to explicitly
            //define the fallback DTD
            if(context.getDTD() != null) {
                return context.getDTD();
            } else {
                return getFallbackDTD();
            }
        } else {
            DTD dtd = org.netbeans.editor.ext.html.dtd.Registry.getDTD(getPublicID(), null);
            return dtd != null ? dtd : getFallbackDTD();
        }
    }

    private DTD getFallbackDTD() {
        return org.netbeans.editor.ext.html.dtd.Registry.getDTD(FALLBACK_DOCTYPE, null);
    }

     private static String dequote(String text) {
        if(text.length() < 2) {
            return text;
        } else {
            if((text.charAt(0) == '\'' || text.charAt(0) == '"') &&
                (text.charAt(text.length() - 1) == '\'' || text.charAt(text.length() - 1) == '"')) {
                return text.substring(1, text.length() - 1);
            }
        }
        return text;
    }


}

