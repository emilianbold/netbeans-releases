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
package org.netbeans.modules.javascript2.editor.index;

import java.util.*;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.javascript2.editor.model.Identifier;
import org.netbeans.modules.javascript2.editor.model.JsElement;
import org.netbeans.modules.javascript2.editor.model.JsFunction;
import org.netbeans.modules.javascript2.editor.model.JsObject;
import org.netbeans.modules.javascript2.editor.model.TypeUsage;
import org.netbeans.modules.javascript2.editor.model.impl.JsElementImpl;
import org.netbeans.modules.javascript2.editor.model.impl.ModelUtils;
import org.netbeans.modules.javascript2.editor.model.impl.TypeUsageImpl;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Pisl
 */
public class IndexedElement extends JsElementImpl {
    
    private final JsElement.Kind jsKind;
    
    public IndexedElement(FileObject fileObject, String name, boolean isDeclared, JsElement.Kind kind, OffsetRange offsetRange, Set<Modifier> modifiers) {
        super(fileObject, name, isDeclared, offsetRange, modifiers);
        this.jsKind = kind;
    }

    @Override
    public Kind getJSKind() {
        return this.jsKind;
    }
    
    public static IndexDocument createDocument(JsObject object, IndexingSupport support, Indexable indexable) {
        IndexDocument elementDocument = support.createDocument(indexable);
        elementDocument.addPair(JsIndex.FIELD_BASE_NAME, object.getName(), true, true);
        elementDocument.addPair(JsIndex.FIELD_FQ_NAME,  ModelUtils.createFQN(object), true, true);
        elementDocument.addPair(JsIndex.FIELD_JS_KIND, Integer.toString(object.getJSKind().getId()), true, true);
        elementDocument.addPair(JsIndex.FIELD_IS_GLOBAL, (ModelUtils.isGlobal(object.getParent()) ? "1" : "0"), true, true);
        elementDocument.addPair(JsIndex.FIELD_IS_DECLARED, (object.isDeclared() ? "1" : "0"), true, true);
        elementDocument.addPair(JsIndex.FIELD_OFFSET, Integer.toString(object.getOffset()), true, true);            
        for (JsObject property : object.getProperties().values()) {
            elementDocument.addPair(JsIndex.FIELD_PROPERTY, codeProperty(property), false, true);
        }
        StringBuilder sb = new StringBuilder();
        for (TypeUsage type : object.getAssignments()) {
            sb.append(type.getType());
            sb.append(":"); //NOI18N
            sb.append(type.getOffset());
            sb.append("|");
        }
        elementDocument.addPair(JsIndex.FIELD_ASSIGNMENS, sb.toString(), false, true);
        
        if (object.getJSKind() == JsElement.Kind.METHOD
                || object.getJSKind() == JsElement.Kind.FUNCTION
                || object.getJSKind() == JsElement.Kind.CONSTRUCTOR) {
            sb = new StringBuilder();
            for(TypeUsage type : ((JsFunction)object).getReturnTypes()) {
                sb.append(type.getType());
                sb.append(":"); //NOI18N
                sb.append(type.getOffset());
                sb.append("|");
            }
            elementDocument.addPair(JsIndex.FIELD_RETURN_TYPES, sb.toString(), false, true);
        }
        return elementDocument;
    }
    
    public static IndexedElement create(IndexResult indexResult) {
        FileObject fo = indexResult.getFile();
        String name = indexResult.getValue(JsIndex.FIELD_BASE_NAME);
        boolean isDeclared = "1".equals(indexResult.getValue(JsIndex.FIELD_IS_DECLARED)); //NOI18N
        JsElement.Kind kind = JsElement.Kind.fromId(Integer.parseInt(indexResult.getValue(JsIndex.FIELD_JS_KIND)));
        int offset = Integer.parseInt(indexResult.getValue(JsIndex.FIELD_OFFSET));
        IndexedElement result = new IndexedElement(fo, name, isDeclared, kind, new OffsetRange(offset, offset + name.length()), EnumSet.of(Modifier.PUBLIC));
        return result;
    }
    
    public static Collection<IndexedElement> createProperties(IndexResult indexResult) {
        Collection<IndexedElement> result = new ArrayList<IndexedElement>();
        FileObject fo = indexResult.getFile();
        for(String sProperty : indexResult.getValues(JsIndex.FIELD_PROPERTY)) {
            result.add(decodeProperty(sProperty, fo));
        }
        return result;
    }
    
    public static Collection<TypeUsage> getAssignments(IndexResult indexResult) {
        Collection<TypeUsage> result = new ArrayList<TypeUsage>();
        String text = indexResult.getValue(JsIndex.FIELD_ASSIGNMENS);
        if (text != null) {
            for (StringTokenizer st = new StringTokenizer(text, "|"); st.hasMoreTokens();) {
                String token = st.nextToken();
                int index = token.indexOf(':');
                String type = token.substring(0, index);
                String offset = token.substring(index + 1);
                result.add(new TypeUsageImpl(type, Integer.parseInt(offset), true));
            }
        }
        return result;
    }
    
    public static Collection<TypeUsage> getReturnTypes(IndexResult indexResult) {
        Collection<TypeUsage> result = new ArrayList<TypeUsage>();
        String text = indexResult.getValue(JsIndex.FIELD_RETURN_TYPES);
        if (text != null) {
            for (StringTokenizer st = new StringTokenizer(text, "|"); st.hasMoreTokens();) {
                String token = st.nextToken();
                int index = token.indexOf(':');
                String type = token.substring(0, index);
                String offset = token.substring(index + 1);
                result.add(new TypeUsageImpl(type, Integer.parseInt(offset), true));
            }
        }
        return result;
    }
    
    private static String codeProperty(JsObject property) {
        StringBuilder result = new StringBuilder();
        JsElement.Kind jsKind = property.getJSKind();
        result.append(property.getName()).append(';');  //NOI18N
        result.append(jsKind.getId()).append(';');  //NOI18N
        result.append(property.isDeclared() ? "1" : "0").append(';'); //NOI18N
        if (jsKind == JsElement.Kind.FUNCTION || jsKind == JsElement.Kind.METHOD || jsKind == JsElement.Kind.CONSTRUCTOR) {
            for (Iterator<? extends JsObject> it = ((JsFunction)property).getParameters().iterator(); it.hasNext();) {
                JsObject parametr = it.next();
                result.append(parametr.getName());
                if (it.hasNext()) {
                    result.append(',');
                }
            }
        }
        
        return result.toString();
    }
    
    private static IndexedElement decodeProperty(String text, FileObject fo) {
        StringTokenizer st = new StringTokenizer(text, ";");
        String name = st.nextToken();
        JsElement.Kind jsKind = JsElement.Kind.fromId(Integer.parseInt(st.nextToken()));
        boolean isDeclared = "1".equals(st.nextToken());
        if (st.hasMoreTokens()) {
            String paramsText = st.nextToken();
            if (jsKind == JsElement.Kind.FUNCTION || jsKind == JsElement.Kind.METHOD || jsKind == JsElement.Kind.CONSTRUCTOR) {
                Collection<String> parameters = new ArrayList();
                for (StringTokenizer stringTokenizer = new StringTokenizer(paramsText, ","); stringTokenizer.hasMoreTokens();) {
                    parameters.add(stringTokenizer.nextToken());
                }
                return new FunctionIndexedElement(fo, name, jsKind, OffsetRange.NONE, EnumSet.of(Modifier.PUBLIC), parameters);
            }
        }
        return new IndexedElement(fo, name, isDeclared, jsKind,OffsetRange.NONE, EnumSet.of(Modifier.PUBLIC));
    }
    
    public static class FunctionIndexedElement extends IndexedElement {
        private final Collection<String> parameters;
        
        public FunctionIndexedElement(FileObject fileObject, String name, Kind kind, OffsetRange offsetRange, Set<Modifier> modifiers, Collection<String> parameters) {
            super(fileObject, name, true, kind, offsetRange, modifiers);
            this.parameters = parameters;
        }
        
        public Collection<String> getParameters() {
            return this.parameters;
        }
    }
}
