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
package org.netbeans.modules.javascript2.editor.spi.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.javascript2.editor.model.DeclarationScope;
import org.netbeans.modules.javascript2.editor.model.Identifier;
import org.netbeans.modules.javascript2.editor.model.JsArray;
import org.netbeans.modules.javascript2.editor.model.JsElement;
import org.netbeans.modules.javascript2.editor.model.JsFunction;
import org.netbeans.modules.javascript2.editor.model.JsObject;
import org.netbeans.modules.javascript2.editor.model.Model;
import org.netbeans.modules.javascript2.editor.model.Occurrence;
import org.netbeans.modules.javascript2.editor.model.TypeUsage;
import org.netbeans.modules.javascript2.editor.model.impl.ModelElementFactoryAccessor;
import org.netbeans.modules.javascript2.editor.model.impl.IdentifierImpl;
import org.netbeans.modules.javascript2.editor.model.impl.JsArrayReference;
import org.netbeans.modules.javascript2.editor.model.impl.JsFunctionImpl;
import org.netbeans.modules.javascript2.editor.model.impl.JsFunctionReference;
import org.netbeans.modules.javascript2.editor.model.impl.JsObjectImpl;
import org.netbeans.modules.javascript2.editor.model.impl.JsObjectReference;
import org.netbeans.modules.javascript2.editor.model.impl.TypeUsageImpl;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Hejl
 */
public final class ModelElementFactory {

    static {
        ModelElementFactoryAccessor.setDefault(new ModelElementFactoryAccessor() {

            @Override
            public ModelElementFactory createModelElementFactory() {
                return new ModelElementFactory();
            }
        });
    }

    private ModelElementFactory() {
        super();
    }

    public JsFunction newGlobalObject(FileObject fileObject, int length) {
        return JsFunctionImpl.createGlobal(fileObject, length, null);
    }

    public JsObject loadGlobalObject(FileObject fileObject, int length, String sourceLabel) throws IOException {
        InputStream is = fileObject.getInputStream();
        try {
            return loadGlobalObject(is, sourceLabel);
        } finally {
            is.close();
        }
    }

    public JsObject loadGlobalObject(InputStream is, String sourceLabel) throws IOException {
        JsFunction global = newGlobalObject(null, Integer.MAX_VALUE);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8")); // NOI18N
        try {
            for (JsObject object : Model.readModel(reader, global, sourceLabel)) {
                putGlobalProperty(global, object);
            }
            return global;
        } finally {
            reader.close();
        }
    }

    public JsObject putGlobalProperty(JsFunction global, JsObject property) {
        if (property.getParent() != global) {
            throw new IllegalArgumentException("Property is not child of global");
        }
        JsObject wrapped;
        if (property instanceof JsFunction) {
            GlobalFunction real = new GlobalFunction((JsFunction) property);
            real.setParentScope(global);
            real.setParent(global);
            wrapped = real;
        } else {
            GlobalObject real = new GlobalObject(property);
            real.setParent(global);
            wrapped = real;
        }
        global.addProperty(wrapped.getName(), wrapped);
        return wrapped;
    }
    
    public JsObject newObject(JsObject parent, String name, OffsetRange offsetRange,
            boolean isDeclared) {
        return new JsObjectImpl(parent, new IdentifierImpl(name, offsetRange), offsetRange, isDeclared, null, null);
    }

    public JsFunction newFunction(DeclarationScope scope, JsObject parent, String name, Collection<String> params) {
        List<Identifier> realParams = new ArrayList<Identifier>();
        for (String param : params) {
            realParams.add(new IdentifierImpl(param, OffsetRange.NONE));
        }
        return new JsFunctionImpl(scope, parent, new IdentifierImpl(name, OffsetRange.NONE),
                realParams, OffsetRange.NONE, null, null);
    }

    public JsObject newReference(JsObject parent, String name, OffsetRange offsetRange,
            JsObject original, boolean isDeclared, @NullAllowed Set<Modifier> modifiers) {
        if (original instanceof JsFunction) {
            return new JsFunctionReference(parent, new IdentifierImpl(name, offsetRange),
                    (JsFunction) original, isDeclared, modifiers);
        } else if (original instanceof JsArray) {
            return new JsArrayReference(parent, new IdentifierImpl(name, offsetRange),
                    (JsArray) original, isDeclared, modifiers);
        }
        return new JsObjectReference(parent, new IdentifierImpl(name, offsetRange),
                original, isDeclared, modifiers);
    }

    public JsObject newReference(String name, JsObject original, boolean isDeclared) {
        if (original instanceof JsFunction) {
            return new OriginalParentFunctionReference(new IdentifierImpl(name, OffsetRange.NONE), (JsFunction) original, isDeclared);
        } else if (original instanceof JsArray) {
            return new OriginalParentArrayReference(new IdentifierImpl(name, OffsetRange.NONE), (JsArray) original, isDeclared);
        }
        return new OriginalParentObjectReference(new IdentifierImpl(name, OffsetRange.NONE), original, isDeclared);
    }
    
    public TypeUsage newType(String name, int offset, boolean resolved) {
        return new TypeUsageImpl(name, offset, resolved);
    }

    private static class OriginalParentFunctionReference extends JsFunctionReference {

        public OriginalParentFunctionReference(Identifier declarationName, JsFunction original,
                boolean isDeclared) {
            super(original.getParent(), declarationName, original, isDeclared, null);
        }

        @Override
        public JsObject getParent() {
            return getOriginal().getParent();
        }
    }
    
    private static class OriginalParentArrayReference extends JsArrayReference {

        public OriginalParentArrayReference(Identifier declarationName, JsArray original,
                boolean isDeclared) {
            super(original.getParent(), declarationName, original, isDeclared, null);
        }

        @Override
        public JsObject getParent() {
            return getOriginal().getParent();
        }
    }

    private static class OriginalParentObjectReference extends JsObjectReference {

        public OriginalParentObjectReference(Identifier declarationName, JsObject original, boolean isDeclared) {
            super(original.getParent(), declarationName, original, isDeclared, null);
        }

        @Override
        public JsObject getParent() {
            return getOriginal().getParent();
        }
    }

    private static class GlobalObject implements JsObject {

        private final JsObject delegate;

        private JsObject parent;

        public GlobalObject(JsObject delegate) {
            this.delegate = delegate;
            this.parent = delegate.getParent();
        }

        @Override
        public JsObject getParent() {
            return this.parent;
        }

        public void setParent(JsObject parent) {
            this.parent = parent;
        }

        // pure delegation follows

        @Override
        public Identifier getDeclarationName() {
            return delegate.getDeclarationName();
        }

        @Override
        public Map<String, ? extends JsObject> getProperties() {
            return delegate.getProperties();
        }

        @Override
        public void addProperty(String name, JsObject property) {
            delegate.addProperty(name, property);
        }

        @Override
        public JsObject getProperty(String name) {
            return delegate.getProperty(name);
        }

        @Override
        public List<Occurrence> getOccurrences() {
            return delegate.getOccurrences();
        }

        @Override
        public void addOccurrence(OffsetRange offsetRange) {
            delegate.addOccurrence(offsetRange);
        }

        @Override
        public String getFullyQualifiedName() {
            return delegate.getFullyQualifiedName();
        }

        @Override
        public Collection<? extends TypeUsage> getAssignmentForOffset(int offset) {
            return delegate.getAssignmentForOffset(offset);
        }

        @Override
        public Collection<? extends TypeUsage> getAssignments() {
            return delegate.getAssignments();
        }

        @Override
        public void addAssignment(TypeUsage typeName, int offset) {
            delegate.addAssignment(typeName, offset);
        }

        @Override
        public boolean isAnonymous() {
            return delegate.isAnonymous();
        }

        @Override
        public boolean isDeprecated() {
            return delegate.isDeprecated();
        }

        @Override
        public boolean hasExactName() {
            return delegate.hasExactName();
        }

        @Override
        public String getDocumentation() {
            return delegate.getDocumentation();
        }

        @Override
        public int getOffset() {
            return delegate.getOffset();
        }

        @Override
        public OffsetRange getOffsetRange() {
            return delegate.getOffsetRange();
        }

        @Override
        public Kind getJSKind() {
            return delegate.getJSKind();
        }

        @Override
        public boolean isDeclared() {
            return delegate.isDeclared();
        }

        @Override
        public String getSourceLabel() {
            return delegate.getSourceLabel();
        }

        @Override
        public boolean isPlatform() {
            return delegate.isPlatform();
        }

        @Override
        public FileObject getFileObject() {
            return delegate.getFileObject();
        }

        @Override
        public String getMimeType() {
            return delegate.getMimeType();
        }

        @Override
        public String getName() {
            return delegate.getName();
        }

        @Override
        public String getIn() {
            return delegate.getIn();
        }

        @Override
        public ElementKind getKind() {
            return delegate.getKind();
        }

        @Override
        public Set<Modifier> getModifiers() {
            return delegate.getModifiers();
        }

        @Override
        public boolean signatureEquals(ElementHandle handle) {
            return delegate.signatureEquals(handle);
        }

        @Override
        public OffsetRange getOffsetRange(ParserResult result) {
            return delegate.getOffsetRange(result);
        }
    }

    private static class GlobalFunction implements JsFunction {

        private final JsFunction delegate;

        private DeclarationScope inScope;

        private JsObject parent;

        public GlobalFunction(JsFunction delegate) {
            this.delegate = delegate;
            this.inScope = delegate.getParentScope();
            this.parent = delegate.getParent();
        }

        @Override
        public DeclarationScope getParentScope() {
            return this.inScope;
        }

        protected void setParentScope(DeclarationScope inScope) {
            this.inScope = inScope;
        }

        @Override
        public JsObject getParent() {
            return this.parent;
        }

        public void setParent(JsObject parent) {
            this.parent = parent;
        }

        // pure delegation follows

        @Override
        public JsObject getProperty(String name) {
            return delegate.getProperty(name);
        }
        
        @Override
        public Collection<? extends DeclarationScope> getChildrenScopes() {
            return delegate.getChildrenScopes();
        }

        @Override
        public Collection<? extends JsObject> getParameters() {
            return delegate.getParameters();
        }

        @Override
        public JsObject getParameter(String name) {
            return delegate.getParameter(name);
        }

        @Override
        public void addReturnType(TypeUsage type) {
            delegate.addReturnType(type);
        }

        @Override
        public Collection<? extends TypeUsage> getReturnTypes() {
            return delegate.getReturnTypes();
        }

        @Override
        public Identifier getDeclarationName() {
            return delegate.getDeclarationName();
        }

        @Override
        public Map<String, ? extends JsObject> getProperties() {
            return delegate.getProperties();
        }

        @Override
        public void addProperty(String name, JsObject property) {
            delegate.addProperty(name, property);
        }

        @Override
        public List<Occurrence> getOccurrences() {
            return delegate.getOccurrences();
        }

        @Override
        public void addOccurrence(OffsetRange offsetRange) {
            delegate.addOccurrence(offsetRange);
        }

        @Override
        public String getFullyQualifiedName() {
            return delegate.getFullyQualifiedName();
        }

        @Override
        public Collection<? extends TypeUsage> getAssignmentForOffset(int offset) {
            return delegate.getAssignmentForOffset(offset);
        }

        @Override
        public Collection<? extends TypeUsage> getAssignments() {
            return delegate.getAssignments();
        }

        @Override
        public void addAssignment(TypeUsage typeName, int offset) {
            delegate.addAssignment(typeName, offset);
        }

        @Override
        public boolean isAnonymous() {
            return delegate.isAnonymous();
        }

        @Override
        public boolean isDeprecated() {
            return delegate.isDeprecated();
        }

        @Override
        public boolean hasExactName() {
            return delegate.hasExactName();
        }

        @Override
        public String getDocumentation() {
            return delegate.getDocumentation();
        }

        @Override
        public int getOffset() {
            return delegate.getOffset();
        }

        @Override
        public OffsetRange getOffsetRange() {
            return delegate.getOffsetRange();
        }

        @Override
        public JsElement.Kind getJSKind() {
            return delegate.getJSKind();
        }

        @Override
        public boolean isDeclared() {
            return delegate.isDeclared();
        }

        @Override
        public String getSourceLabel() {
            return delegate.getSourceLabel();
        }

        @Override
        public boolean isPlatform() {
            return delegate.isPlatform();
        }

        @Override
        public FileObject getFileObject() {
            return delegate.getFileObject();
        }

        @Override
        public String getMimeType() {
            return delegate.getMimeType();
        }

        @Override
        public String getName() {
            return delegate.getName();
        }

        @Override
        public String getIn() {
            return delegate.getIn();
        }

        @Override
        public ElementKind getKind() {
            return delegate.getKind();
        }

        @Override
        public Set<Modifier> getModifiers() {
            return delegate.getModifiers();
        }

        @Override
        public boolean signatureEquals(ElementHandle handle) {
            return delegate.signatureEquals(handle);
        }

        @Override
        public OffsetRange getOffsetRange(ParserResult result) {
            return delegate.getOffsetRange(result);
        }

    }
}
