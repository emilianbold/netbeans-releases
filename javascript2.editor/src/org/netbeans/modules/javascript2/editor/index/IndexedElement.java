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
    private final String fqn;
    
    public IndexedElement(FileObject fileObject, String name, String fqn, boolean isDeclared, JsElement.Kind kind, OffsetRange offsetRange, Set<Modifier> modifiers) {
        super(fileObject, name, isDeclared, offsetRange, modifiers);
        this.jsKind = kind;
        this.fqn = fqn;
    }

    @Override
    public Kind getJSKind() {
        return this.jsKind;
    }
    
    public String getFQN() {
        return this.fqn;
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
        
        if (object.getJSKind().isFunction()) {
            sb = new StringBuilder();
            for(TypeUsage type : ((JsFunction)object).getReturnTypes()) {
                sb.append(type.getType());
                sb.append(","); //NOI18N
                sb.append(type.getOffset());
                sb.append("|");
            }
            elementDocument.addPair(JsIndex.FIELD_RETURN_TYPES, sb.toString(), false, true);
            elementDocument.addPair(JsIndex.FIELD_PARAMETERS, codeParameters(((JsFunction)object).getParameters()), false, true);
        }
        
        return elementDocument;
    }
    
    public static IndexedElement create(IndexResult indexResult) {
        FileObject fo = indexResult.getFile();
        String name = indexResult.getValue(JsIndex.FIELD_BASE_NAME);
        String fqn = indexResult.getValue(JsIndex.FIELD_FQ_NAME);
        boolean isDeclared = "1".equals(indexResult.getValue(JsIndex.FIELD_IS_DECLARED)); //NOI18N
        JsElement.Kind kind = JsElement.Kind.fromId(Integer.parseInt(indexResult.getValue(JsIndex.FIELD_JS_KIND)));
        int offset = Integer.parseInt(indexResult.getValue(JsIndex.FIELD_OFFSET));
        IndexedElement result;
        if (!kind.isFunction()) {
            result = new IndexedElement(fo, name, fqn, isDeclared, kind, new OffsetRange(offset, offset + name.length()), EnumSet.of(Modifier.PUBLIC));
        } else {
            Collection<TypeUsage> returnTypes = getReturnTypes(indexResult);
            Collection<String>rTypes = new ArrayList<String>();
            for (TypeUsage type : returnTypes) {
                rTypes.add(type.getType());
            }
            String paramText = indexResult.getValue(JsIndex.FIELD_PARAMETERS);
            LinkedHashMap<String, Collection<String>> params  = decodeParameters(paramText);
            result = new FunctionIndexedElement(fo, name, fqn, kind, new OffsetRange(offset, offset + name.length()), EnumSet.of(Modifier.PUBLIC), params, rTypes);
        }
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
                int index = token.indexOf(',');
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
        if (jsKind.isFunction()) {
            result.append(codeParameters(((JsFunction)property).getParameters()));
            result.append(";");
            for (Iterator<? extends TypeUsage> it = ((JsFunction)property).getReturnTypes().iterator(); it.hasNext();) {
                TypeUsage type = it.next();
                result.append(type.getType());
                if (it.hasNext()) {
                    result.append(',');
                }
            }
        }
        
        return result.toString();
    }
    
    private static String codeParameters(Collection<? extends JsObject> params) {
        StringBuilder result = new StringBuilder();
        for (Iterator<? extends JsObject> it = params.iterator(); it.hasNext();) {
            JsObject parametr = it.next();
            result.append(parametr.getName());
            result.append(":");
            for (Iterator<? extends TypeUsage> itType = parametr.getAssignmentForOffset(parametr.getOffset() + 1).iterator(); itType.hasNext();) {
                TypeUsage type = itType.next();
                result.append(type.getType());
                if (itType.hasNext()) {
                    result.append("|");
                }
            }
            if (it.hasNext()) {
                result.append(',');
            }
        }
        return result.toString();
    }
    
    private static LinkedHashMap<String, Collection<String>> decodeParameters(String paramsText) {
        LinkedHashMap<String, Collection<String>> parameters = new LinkedHashMap<String, Collection<String>>();
        for (StringTokenizer stringTokenizer = new StringTokenizer(paramsText, ","); stringTokenizer.hasMoreTokens();) {
            String param = stringTokenizer.nextToken();
            int index = param.indexOf(':');
            Collection<String> types = new ArrayList<String>();
            String paramName;
            if (index > 0) {
                paramName = param.substring(0, index);
                String typesText = param.substring(index + 1);
                for (StringTokenizer stParamType = new StringTokenizer(typesText, "|"); stParamType.hasMoreTokens();) {
                    types.add(stParamType.nextToken());
                }
            } else {
                paramName = param;
            }
            parameters.put(paramName, types);
        }
        return parameters;
    }
    
    private static IndexedElement decodeProperty(String text, FileObject fo) {
        String[] parts = text.split(";");
        String name = parts[0];
        JsElement.Kind jsKind = JsElement.Kind.fromId(Integer.parseInt(parts[1]));
        boolean isDeclared = "1".equals(parts[2]);
        if (parts.length > 3) {
            if (jsKind.isFunction()) {
                String paramsText = parts[3];
                LinkedHashMap<String, Collection<String>> parameters = decodeParameters(paramsText);
                Collection<String> returnTypes = new ArrayList();
                String returnTypesText = parts[4];
                for (StringTokenizer stringTokenizer = new StringTokenizer(returnTypesText, ","); stringTokenizer.hasMoreTokens();) {
                    returnTypes.add(stringTokenizer.nextToken());
                }
                return new FunctionIndexedElement(fo, name, null, jsKind, OffsetRange.NONE, EnumSet.of(Modifier.PUBLIC), parameters, returnTypes);
            }
        }
        return new IndexedElement(fo, name, null, isDeclared, jsKind,OffsetRange.NONE, EnumSet.of(Modifier.PUBLIC));
    }
    
    public static class FunctionIndexedElement extends IndexedElement {
        private final LinkedHashMap<String, Collection<String>> parameters;
        private final Collection<String> returnTypes;
        
        public FunctionIndexedElement(FileObject fileObject, String name, String fqn, Kind kind, OffsetRange offsetRange, Set<Modifier> modifiers, LinkedHashMap<String, Collection<String>> parameters, Collection<String> returnTypes) {
            super(fileObject, name, fqn, true, kind, offsetRange, modifiers);
            this.parameters = parameters;
            this.returnTypes = returnTypes;
        }
        
        public LinkedHashMap<String, Collection<String>> getParameters() {
            return this.parameters;
        }
        
        public Collection<String> getReturnTypes() {
            return this.returnTypes;
        }
    }
}
