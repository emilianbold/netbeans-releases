/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.navigator;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.lib.api.elements.Attribute;
import org.netbeans.modules.html.editor.lib.api.elements.Node;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.netbeans.modules.html.editor.lib.api.model.HtmlModel;
import org.netbeans.modules.html.editor.lib.api.model.HtmlModelFactory;
import org.netbeans.modules.html.editor.lib.api.model.HtmlTag;
import org.netbeans.modules.html.editor.lib.api.model.HtmlTagAttribute;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node.Property;
import org.openide.nodes.Node.PropertySet;
import org.openide.nodes.PropertySupport;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author marekfukala
 */
@NbBundle.Messages({
    "edit.attribute.tooltip=You can edit the value or delete the attribute by setting an empty value",
    "new.attribute.tooltip=You can add the attribute by setting its value",
    "element.element.attributes.title=Element Attributes"
})
public class HtmlElementProperties {

    /**
     * A {@link Node} representing an HTML source code element.
     * 
     * The node needs to provide its own lookup with at least
     * {@link DataObject} since the NavigatorController's {@link Lookup} 
     * provided to the clients in NavigatorPanel.panelActivated(...)
     * contains content of lookups from all activated nodes got from the
     * active {@link TopComponent}. 
     * 
     * Then the CSL navigator listens on {@link Lookup#lookupAll(DataObject.class)} 
     * result to ensure content update when editor's {@link TopComponent}s bound 
     * to the same {@link NavigatorPanel} are switched.
     *
     */

    public static class PropertiesPropertySet extends PropertySet {

        private OpenTag openTag;
        private HtmlParserResult res;

        public PropertiesPropertySet(HtmlParserResult res, OpenTag openTag) {
            this.res = res;
            this.openTag = openTag;
            setName(Bundle.element_element_attributes_title());
        }

        @Override
        public Property<String>[] getProperties() {
            Snapshot s = res.getSnapshot();
            Document doc = s.getSource().getDocument(false);
            Collection<Property> props = new ArrayList<Property>();
            Collection<String> existingAttrNames = new HashSet<String>();
            for (Attribute a : openTag.attributes()) {
                props.add(new AttributeProperty(doc, s, a));
                existingAttrNames.add(a.name().toString().toLowerCase(Locale.ENGLISH));
            }
            HtmlModel model = HtmlModelFactory.getModel(res.getHtmlVersion());
            HtmlTag tagModel = model.getTag(openTag.name().toString());
            if (tagModel != null) {
                List<String> attrNames = new ArrayList<String>();
                for (HtmlTagAttribute htmlTagAttr : tagModel.getAttributes()) {
                    String name = htmlTagAttr.getName().toLowerCase();
                    if (!existingAttrNames.contains(name)) {
                        attrNames.add(name);
                    }
                }
                
                Collections.sort(attrNames);
                for(String attrName : attrNames) {
                    props.add(new NewAttributeProperty(doc, s, attrName, openTag));
                }
            }

            return props.toArray(new Property[]{});
        }
    }

    private static class AttributeProperty extends PropertySupport<String> {

        private Attribute attr;
        private Document doc;
        private Snapshot snap;

        public AttributeProperty(Document doc, Snapshot snap, Attribute attr) {
            super(attr.name().toString(), String.class, attr.name().toString(), Bundle.edit_attribute_tooltip(), true, doc != null);
            this.doc = doc;
            this.snap = snap;
            this.attr = attr;
        }

        @Override
        public String getHtmlDisplayName() {
            return new StringBuilder()
                    .append("<b>")
                    .append(attr.name())
                    .append("</b>")
                    .toString();
        }
        
        @Override
        public String getValue() throws IllegalAccessException, InvocationTargetException {
            return attr.unquotedValue().toString();
        }

        @Override
        public void setValue(final String val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            int astFrom, astTo;
            if (val.length() == 0) {
                //remove the whole attribute=value pair
                astFrom = attr.nameOffset() - 1; //there must be a WS before so lets remove it
                astTo = attr.valueOffset() + attr.value().length();
            } else {
                //modify
                astFrom = attr.valueOffset() + (attr.isValueQuoted() ? 1 : 0);
                astTo = astFrom + attr.unquotedValue().length();
            }

            final int docFrom = snap.getOriginalOffset(astFrom);
            final int docTo = snap.getOriginalOffset(astTo);

            if (docFrom != -1 && docTo != -1) {
                ((BaseDocument) doc).runAtomicAsUser(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            doc.remove(docFrom, docTo - docFrom);
                            if (val.length() > 0) {
                                doc.insertString(docFrom, val, null);
                            }
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                });
            }
        }
    }

    private static class NewAttributeProperty extends PropertySupport<String> {

        private static final String EMPTY = "";
        private String attrName;
        private Document doc;
        private Snapshot snap;
        private OpenTag ot;

        public NewAttributeProperty(Document doc, Snapshot snap, String attrName, OpenTag ot) {
            super(attrName, String.class, attrName, Bundle.new_attribute_tooltip(), true, doc != null);
            this.doc = doc;
            this.snap = snap;
            this.ot = ot;
            this.attrName = attrName;
        }

        @Override
        public String getValue() throws IllegalAccessException, InvocationTargetException {
            return EMPTY;
        }

        @Override
        public void setValue(String val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            if (val.length() == 0) {
                return;
            }
            //remove the whole attribute=value pair
            int astFrom = ot.from() + 1 /* "<".length() */ + ot.name().length(); //just after the attribute name

            final int docFrom = snap.getOriginalOffset(astFrom);
            if (docFrom != -1) {
                final StringBuilder insertBuilder = new StringBuilder()
                        .append(' ')
                        .append(attrName)
                        .append('=')
                        .append('"')
                        .append(val)
                        .append('"');
                        
                ((BaseDocument) doc).runAtomicAsUser(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if(doc.getText(docFrom, 1).trim().length() == 0) {
                                //there's already a WS after the insertion place
                            } else {
                                //lets add one more WS
                                insertBuilder.append(' ');
                            }
                            doc.insertString(docFrom, insertBuilder.toString(), null);
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                });
            }
        }
    }
}
