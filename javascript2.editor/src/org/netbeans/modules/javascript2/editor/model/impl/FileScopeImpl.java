/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor.model.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.javascript2.editor.model.FileScope;
import org.netbeans.modules.javascript2.editor.model.FunctionScope;
import org.netbeans.modules.javascript2.editor.model.Identifier;
import org.netbeans.modules.javascript2.editor.model.JsElement;
import org.netbeans.modules.javascript2.editor.model.ObjectScope;
import org.netbeans.modules.javascript2.editor.model.Scope;
import org.netbeans.modules.javascript2.editor.parser.JsParserResult;

/**
 *
 * @author Petr Pisl
 */
public class FileScopeImpl extends ScopeImpl implements FileScope {
    Hashtable<String, Scope> logicalElements;
    
    public FileScopeImpl(JsParserResult result, String name) {
        super(null, JsElement.Kind.FILE, 
                result.getSnapshot().getSource().getFileObject(), name, new OffsetRange(0, 0),
                Collections.<Modifier>emptySet());
        logicalElements = new Hashtable<String, Scope>();
    }
    
    FileScopeImpl(JsParserResult result) {
        this(result, result.getRoot().getName());
    }

    @Override
    public Collection<? extends Scope> getLogicalElements() {
        return logicalElements.values();
    }
    
    protected void addObject(String fqName, Scope object){
        logicalElements.put(fqName, object);
    }
    
    protected void addMethod(FunctionScope function) {
        List<Identifier> identifiers = function.getFQDeclarationName();
        
        switch (function.getJSKind()) {
            case FUNCTION:
                logicalElements.put(function.getName(), function);
                break;
            case CONSTRUCTOR:
                String objectName = ModelUtils.getNameWithoutPrototype(identifiers);
                ObjectScopeImpl logicalObject = new ObjectScopeImpl(this, identifiers, function.getBlockRange());
                logicalElements.put(objectName.toString(), logicalObject);
                logicalObject.addElement((FunctionScopeImpl) function);
                break;
            case METHOD:
                if (identifiers.size() == 1
                        && (function.getInElement() instanceof FunctionScope
                        || function.getInElement() instanceof ObjectScope)) {
                    // these methods are already in
                    break;
                }
                objectName = ModelUtils.getObjectName(function);
                logicalObject = (ObjectScopeImpl) logicalElements.get(objectName);
                if (logicalObject == null) {
                    logicalObject = new ObjectScopeImpl(this, identifiers, function.getBlockRange());
                    logicalElements.put(objectName, logicalObject);
                }
                logicalObject.addElement((FunctionScopeImpl) function);
                break;
        }

    }
}
