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
package org.netbeans.modules.javascript2.editor.jquery;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.javascript2.editor.model.DeclarationScope;
import org.netbeans.modules.javascript2.editor.model.Identifier;
import org.netbeans.modules.javascript2.editor.model.JsObject;
import org.netbeans.modules.javascript2.editor.model.impl.IdentifierImpl;
import org.netbeans.modules.javascript2.editor.model.impl.JsFunctionImpl;
import org.netbeans.modules.javascript2.editor.model.impl.JsFunctionReference;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;

/**
 *
 * @author Petr Pisl
 */
public class JQueryModel {

    @org.netbeans.api.annotations.common.SuppressWarnings("MS_SHOULD_BE_FINAL")
    public static boolean skipInTest = false;

    private static JQFunctionImpl jQuery = null;
    private static JsFunctionReference rjQuery = null;
    private static JsObject globalObject = null;
    
    public static  JsObject getGlobalObject() {
        if (skipInTest) {
            return null;
        }
        File apiFile = InstalledFileLocator.getDefault().locate(JQueryCodeCompletion.HELP_LOCATION, null, false); //NoI18N
        if (globalObject == null && apiFile != null) {
            globalObject = JsFunctionImpl.createGlobal(FileUtil.toFileObject(apiFile), (int) apiFile.length());
            jQuery =  new JQFunctionImpl((DeclarationScope)globalObject, globalObject, new IdentifierImpl("jQuery", OffsetRange.NONE), Collections.<Identifier>emptyList(), OffsetRange.NONE); // NOI18N
            rjQuery = new JQFunctionReference(new IdentifierImpl("$", OffsetRange.NONE), jQuery, false); // NOI18N
            
            SelectorsLoader.addToModel(apiFile, jQuery);
            jQuery.setInScope((DeclarationScope)globalObject);
            jQuery.setParent(globalObject);
            globalObject.addProperty("jQuery", jQuery); // NOI18N
            globalObject.addProperty("$", rjQuery);     // NOI18N
        }
        return globalObject;
    }
    
    private static class JQFunctionImpl extends JsFunctionImpl {
        private DeclarationScope inScope;
        private JsObject parent;
        
        public JQFunctionImpl(DeclarationScope scope, JsObject parentObject, Identifier name, List<Identifier> parameters, OffsetRange offsetRange) {
            super(scope, parentObject, name, parameters, offsetRange);
            this.inScope = scope;
            this.parent = parentObject;
        }

        @Override
        public DeclarationScope getInScope() {
            return this.inScope;
        }
        
        protected void setInScope(DeclarationScope inScope) {
            this.inScope = inScope;
        }

        @Override
        public JsObject getParent() {
            return this.parent;
        }
        
        protected void setParent(JsObject parent) {
            this.parent = parent;
        }    
    }
    
    private static class JQFunctionReference extends JsFunctionReference {

        public JQFunctionReference(Identifier declarationName, JsFunctionImpl original, boolean isDeclared) {
            super(original.getParent(), declarationName, original, isDeclared);
        }

        @Override
        public JsObject getParent() {
            return getOriginal().getParent();
        }
    }
    
}
