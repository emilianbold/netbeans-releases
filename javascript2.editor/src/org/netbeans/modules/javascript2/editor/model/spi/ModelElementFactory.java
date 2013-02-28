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
package org.netbeans.modules.javascript2.editor.model.spi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.javascript2.editor.model.DeclarationScope;
import org.netbeans.modules.javascript2.editor.model.Identifier;
import org.netbeans.modules.javascript2.editor.model.JsFunction;
import org.netbeans.modules.javascript2.editor.model.JsObject;
import org.netbeans.modules.javascript2.editor.model.TypeUsage;
import org.netbeans.modules.javascript2.editor.model.impl.ModelElementFactoryAccessor;
import org.netbeans.modules.javascript2.editor.model.impl.IdentifierImpl;
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

    public JsObject newGlobalObject(FileObject fileObject, int length) {
        return JsFunctionImpl.createGlobal(fileObject, length);
    }
    
    public JsObject newObject(JsObject parent, String name, OffsetRange offsetRange,
            boolean isDeclared) {
        return new JsObjectImpl(parent, new IdentifierImpl(name, offsetRange), offsetRange, isDeclared);
    }

    public JsFunction newFunction(DeclarationScope scope, JsObject parent, String name, Collection<String> params) {
        List<Identifier> realParams = new ArrayList<Identifier>();
        for (String param : params) {
            realParams.add(new IdentifierImpl(param, OffsetRange.NONE));
        }
        return new JsFunctionImpl(scope, parent, new IdentifierImpl(name, OffsetRange.NONE), realParams, OffsetRange.NONE);
    }

    public JsObject newReference(JsObject parent, String name, OffsetRange offsetRange,
            JsObject original, boolean isDeclared) {
        if (original instanceof JsFunction) {
            return new JsFunctionReference(parent, new IdentifierImpl(name, offsetRange), (JsFunction) original, isDeclared);
        }
        return new JsObjectReference(parent, new IdentifierImpl(name, offsetRange), original, isDeclared);
    }

    public JsObject newReference(String name, JsObject original, boolean isDeclared) {
        if (original instanceof JsFunction) {
            return new OriginalParentFunctionReference(new IdentifierImpl(name, OffsetRange.NONE), (JsFunction) original, isDeclared);
        }
        return new OriginalParentObjectReference(new IdentifierImpl(name, OffsetRange.NONE), original, isDeclared);
    }
    
    public TypeUsage newType(String name, int offset, boolean resolved) {
        return new TypeUsageImpl(name, offset, resolved);
    }

    private static class OriginalParentFunctionReference extends JsFunctionReference {

        public OriginalParentFunctionReference(Identifier declarationName, JsFunction original,
                boolean isDeclared) {
            super(original.getParent(), declarationName, original, isDeclared);
        }

        @Override
        public JsObject getParent() {
            return getOriginal().getParent();
        }
    }

    private static class OriginalParentObjectReference extends JsObjectReference {

        public OriginalParentObjectReference(Identifier declarationName, JsObject original, boolean isDeclared) {
            super(original.getParent(), declarationName, original, isDeclared);
        }

        @Override
        public JsObject getParent() {
            return getOriginal().getParent();
        }
    }
}
