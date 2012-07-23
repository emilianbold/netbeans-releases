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
package org.netbeans.modules.javafx2.editor.completion.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;

/**
 * Represents a single FXML source file.
 * 
 * @author sdedic
 */
public final class FxModel extends FxNode {
    /**
     * Import declarations
     */
    private List<ImportDecl>           imports = Collections.EMPTY_LIST;
    
    /**
     * Definitions, keyed by ID
     */
    private Map<String, FxNewInstance>    definitions = Collections.EMPTY_MAP;
    
    /**
     * The declared language for scripting
     */
    private LanguageDecl               language;
    
    /**
     * Root component instance
     */
    @NullAllowed
    private FxObjectBase                 rootComponent;
    
    /**
     * Instance with IDs; both definitions and ordinary instances with fx:id
     */
    private Map<String, ? extends FxInstance>  namedInstances = Collections.emptyMap();
    
    public List<ImportDecl> getImports() {
        return imports;
    }

    public Collection<FxNewInstance> getDefinitions() {
        return definitions.values();
    }
    
    @CheckForNull
    public LanguageDecl getLanguage() {
        return language;
    }

    /**
     * Provides the root component of the FXML. May be null, if root
     * element is missing or does not represent a Component instance
     * 
     * @return root component
     */
    @CheckForNull
    public FxObjectBase getRootComponent() {
        return rootComponent;
    }
    
    FxModel() {
    }
    
    void setLanguage(LanguageDecl lang) {
        this.language = lang;
    }
    
    void setImports(List<ImportDecl> decls) {
        this.imports = Collections.unmodifiableList(decls);
    }
    
    void setDefinitions(List<FxNewInstance> defs) {
        Map<String, FxNewInstance> instances = new LinkedHashMap<String, FxNewInstance>();
        for (FxNewInstance i : defs) {
            instances.put(i.getId(), i);
        }
        this.definitions = Collections.unmodifiableMap(instances);
    }
    
    void setRootComponent(FxObjectBase root) {
        this.rootComponent = root;
    }
    
    public String getTagName() {
        return "<source>"; // NOI18N
    }

    @Override
    public Kind getKind() {
        return Kind.Source;
    }

    @Override
    public void accept(FxNodeVisitor v) {
        v.visitSource(this);
    }
    
    
    void detachChild(FxNode child) {
        if (child instanceof ImportDecl) {
            imports.add((ImportDecl)child);
        } else if (child instanceof LanguageDecl) {
            language = null;
        }
        super.detachChild(child);
    }
    
    void setNamedInstances(Map<String, ? extends FxInstance> instances) {
        this.namedInstances = instances;
    }
    
    @NonNull
    public Set<String> getInstanceNames() {
        return Collections.unmodifiableSet(this.namedInstances.keySet());
    }
    
    @CheckForNull
    public FxInstance getInstance(String id ) {
        return namedInstances.get(id);
    }
    
}
