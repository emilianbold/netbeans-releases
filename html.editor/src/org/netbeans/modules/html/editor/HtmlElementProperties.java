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
package org.netbeans.modules.html.editor;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.html.api.HtmlEditorSupportControl;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.lib.api.elements.Attribute;
import org.netbeans.modules.html.editor.lib.api.elements.ElementType;
import org.netbeans.modules.html.editor.lib.api.elements.Node;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.CursorMovedSchedulerEvent;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node.Property;
import org.openide.nodes.Node.PropertySet;
import org.openide.nodes.PropertySupport;
import org.openide.util.Exceptions;

/**
 *
 * @author marekfukala
 */
public class HtmlElementProperties {

    static void parsed(HtmlParserResult result, SchedulerEvent event) {
        try {
            FileObject file = result.getSnapshot().getSource().getFileObject();
            if (file == null) {
                return;
            }

            DataObject dobj = DataObject.find(file);
            HtmlEditorSupportControl control = HtmlEditorSupportControl.Query.get(dobj);
            if (control == null) {
                return;
            }

            //filter out embedded html cases
            int caretPosition = ((CursorMovedSchedulerEvent) event).getCaretOffset();
            Node node = result.findBySemanticRange(caretPosition, true);
            if (node != null) {
                if (node.type() == ElementType.OPEN_TAG) { //may be root node!
                    OpenTag ot = (OpenTag) node;
                    org.openide.nodes.Node elementNode = new OpenTagNode(result.getSnapshot(), ot);
                    control.setNode(elementNode);
                }
            }
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }

    }

    private static class OpenTagNode extends AbstractNode {

        private OpenTag openTag;
        private Snapshot snap;

        public OpenTagNode(Snapshot snap, OpenTag openTag) {
            super(Children.LEAF);
            this.snap = snap;
            this.openTag = openTag;

            setDisplayName(openTag.name().toString());
        }

        @Override
        public PropertySet[] getPropertySets() {
            PropertySet[] sets = new PropertySet[]{new OpenTagPropertySet(snap, openTag)};
            return sets;
        }
    }

    private static class OpenTagPropertySet extends PropertySet {

        private OpenTag openTag;
        private Snapshot snap;

        public OpenTagPropertySet(Snapshot snap, OpenTag openTag) {
            this.snap = snap;
            this.openTag = openTag;
        }

        @Override
        public Property<String>[] getProperties() {
            Collection<Property> props = new ArrayList<Property>();
            for (Attribute a : openTag.attributes()) {
                props.add(new AttributeProperty(snap.getSource().getDocument(false), snap, a));
            }
            return props.toArray(new Property[]{});
        }
    }

    private static class AttributeProperty extends PropertySupport<String> {

        private Attribute attr;
        private Document doc;
        private Snapshot snap;

        public AttributeProperty(Document doc, Snapshot snap, Attribute attr) {
            super(attr.name().toString(), String.class, attr.name().toString(), null, true, doc != null);
            this.doc = doc;
            this.snap = snap;
            this.attr = attr;
        }

        @Override
        public String getValue() throws IllegalAccessException, InvocationTargetException {
            return attr.unquotedValue().toString();
        }

        @Override
        public void setValue(final String val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            int astFrom = attr.valueOffset() + (attr.isValueQuoted() ? 1 : 0);
            int astTo = astFrom + attr.unquotedValue().length();

            final int docFrom = snap.getOriginalOffset(astFrom);
            final int docTo = snap.getOriginalOffset(astTo);

            if (docFrom != -1 && docTo != -1) {
                ((BaseDocument) doc).runAtomicAsUser(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            doc.remove(docFrom, docTo - docFrom);
                            doc.insertString(docFrom, val, null);
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                });
            }
        }
    }
}
