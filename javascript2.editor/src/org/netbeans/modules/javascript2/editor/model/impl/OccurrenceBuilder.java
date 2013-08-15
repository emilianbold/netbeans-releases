/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor.model.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.javascript2.editor.doc.spi.DocParameter;
import org.netbeans.modules.javascript2.editor.doc.spi.JsComment;
import org.netbeans.modules.javascript2.editor.doc.spi.JsDocumentationHolder;
import org.netbeans.modules.javascript2.editor.model.DeclarationScope;
import org.netbeans.modules.javascript2.editor.model.Identifier;
import org.netbeans.modules.javascript2.editor.model.JsElement;
import org.netbeans.modules.javascript2.editor.model.JsFunction;
import org.netbeans.modules.javascript2.editor.model.JsObject;
import org.netbeans.modules.javascript2.editor.model.JsWith;
import org.netbeans.modules.javascript2.editor.parser.JsParserResult;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Pisl
 */
public class OccurrenceBuilder {
    private static class Item {
        final DeclarationScope scope;
        final JsObject currentParent;
        final boolean isFunction;
        final boolean leftSite;
        final OffsetRange range;

        public Item(OffsetRange range, DeclarationScope scope, JsObject currentParent, boolean isFunction, boolean leftSite) {
            this.scope = scope;
            this.currentParent = currentParent;
            this.isFunction = isFunction;
            this.leftSite = leftSite;
            this.range = range;
        }
    }
    private final Map<String, Map<OffsetRange, Item>> holder;
    private final JsParserResult parserResult;
    
    public OccurrenceBuilder(JsParserResult parserResult) {
        holder = new HashMap<String, Map<OffsetRange, Item>>();
        this.parserResult = parserResult;
    }
    
    public void addOccurrence(String name, OffsetRange range, DeclarationScope whereUsed, JsObject currentParent, boolean isFunction, boolean leftSite) {
        Map<OffsetRange, Item> items = holder.get(name);
        if (items == null) {
            items = new HashMap<OffsetRange, Item>(1);
            holder.put(name, items);
        }
        if (!items.containsKey(range)) {
            items.put(range, new Item(range, whereUsed, currentParent, isFunction, leftSite));
        }
    }
    
    public void processOccurrences(JsObject global) {
        for (String name : holder.keySet()) {
            Map<OffsetRange, Item> items = holder.get(name);
            for (Item item : items.values()) {
                processOccurrence(global, name, item);
            }
        }
        holder.clear(); // we don't need to keep it anymore.
    }

    private void processOccurrence(JsObject global, String name, Item item) {
        JsObject property = null;
        JsObject parameter = null;
        DeclarationScope scope = item.scope;
        JsObject parent = item.currentParent;
        if (!(parent instanceof JsWith || (parent.getParent() != null && parent.getParent() instanceof JsWith))) {
            while (scope != null && property == null && parameter == null) {
                JsFunction function = (JsFunction)scope;
                property = function.getProperty(name);
                parameter = function.getParameter(name);
                scope = scope.getParentScope();
            }
            if(parameter != null) {
                if (property == null) {
                    property = parameter;
                } else {
                    if(property.getJSKind() != JsElement.Kind.VARIABLE) {
                        property = parameter;
                    }
                }
            }
        } else {
            if (!(parent instanceof JsWith) && (parent.getParent() != null && parent.getParent() instanceof JsWith)) {
                parent = parent.getParent();
            }
            property = parent.getProperty(name);
        }

        if (property != null) {

            // occurence in the doc
            addDocNameOccurence(((JsObjectImpl)property));
            addDocTypesOccurence(((JsObjectImpl)property));

            ((JsObjectImpl)property).addOccurrence(item.range);
        } else {
            // it's a new global variable?
            IdentifierImpl nameIden = ModelElementFactory.create(parserResult, name, item.range.getStart(), item.range.getEnd());
            if (nameIden != null) {
                JsObjectImpl newObject;
                if (!(parent instanceof JsWith)) {
                        parent = global;
                }
                if (!item.isFunction) {
                    newObject = new JsObjectImpl(parent, nameIden, nameIden.getOffsetRange(),
                            item.leftSite, parserResult.getSnapshot().getMimeType(), null);
                } else {
                    FileObject fo = parserResult.getSnapshot().getSource().getFileObject();
                    newObject = new JsFunctionImpl(fo, parent, nameIden, Collections.EMPTY_LIST,
                            parserResult.getSnapshot().getMimeType(), null);
                }
                newObject.addOccurrence(nameIden.getOffsetRange());
                parent.addProperty(nameIden.getName(), newObject);
                addDocNameOccurence(newObject);
                addDocTypesOccurence(newObject);
            }
        }

    }
    
    private void addDocNameOccurence(JsObjectImpl jsObject) {
        JsDocumentationHolder holder = parserResult.getDocumentationHolder();
        JsComment comment = holder.getCommentForOffset(jsObject.getOffset(), holder.getCommentBlocks());
        if (comment != null) {
            for (DocParameter docParameter : comment.getParameters()) {
                Identifier paramName = docParameter.getParamName();
                String name = (docParameter.getParamName() == null) ? "" : docParameter.getParamName().getName(); //NOI18N
                if (name.equals(jsObject.getName())) {
                    jsObject.addOccurrence(paramName.getOffsetRange());
                }
            }
        }
    }

    private void addDocTypesOccurence(JsObjectImpl jsObject) {
        JsDocumentationHolder holder = parserResult.getDocumentationHolder();
        if (holder.getOccurencesMap().containsKey(jsObject.getName())) {
            for (OffsetRange offsetRange : holder.getOccurencesMap().get(jsObject.getName())) {
                ((JsObjectImpl)jsObject).addOccurrence(offsetRange);
            }
        }
    }
}
