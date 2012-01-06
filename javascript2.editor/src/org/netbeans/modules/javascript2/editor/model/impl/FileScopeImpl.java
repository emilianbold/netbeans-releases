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

import java.util.*;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.javascript2.editor.model.*;
import org.netbeans.modules.javascript2.editor.parser.JsParserResult;

/**
 *
 * @author Petr Pisl
 */
public class FileScopeImpl extends VariableScopeImpl implements FileScope {
    private Hashtable<String, Scope> logicalElements;
    
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
    
    protected void addObject(ObjectScope object){
        List<Identifier> identifiers = object.getFQDeclarationName();
        String fqName = ModelUtils.getNameWithoutPrototype(identifiers);
        logicalElements.put(fqName, object);
    }
    
    protected void addMethod(FunctionScope function) {
        List<Identifier> identifiers = function.getFQDeclarationName();
        
        switch (function.getJSKind()) {
            case FUNCTION:
                logicalElements.put(function.getName(), function);
                break;
            case CONSTRUCTOR:
                ObjectScopeImpl logicalObject = identifiers.size() == 1 
                        ? new ObjectScopeImpl(this, identifiers, function.getBlockRange())
                        : findOrCreateLogicaObject(this, identifiers, OffsetRange.NONE);
                if (!logicalElements.containsKey(identifiers.get(0).getName())) {
                    logicalElements.put(identifiers.get(0).getName(), logicalObject);
                }
                logicalObject.addElement((FunctionScopeImpl) function);
                break;
            case METHOD:
                if (identifiers.size() == 1
                        && (function.getInElement() instanceof FunctionScope
                        || function.getInElement() instanceof ObjectScope)) {
                    // these methods are already in
                    break;
                }
                List<Identifier> objectName = new ArrayList<Identifier>();
                
                if (identifiers.size() > 1) {
                    for(int i = 0; i < identifiers.size() - 2; i++) {
                        objectName.add(identifiers.get(i));
                    }
                    if(!identifiers.get(identifiers.size() - 2).getName().equals("prototype")) {
                        objectName.add(identifiers.get(identifiers.size() - 2));
                    }
                    logicalObject = identifiers.size() == 1 
                        ? new ObjectScopeImpl(this, identifiers, function.getBlockRange())
                        : findOrCreateLogicaObject(this, objectName, OffsetRange.NONE);
                } else {
                    logicalObject = (ObjectScopeImpl) logicalElements.get(identifiers.get(0).getName());
                }
                if (logicalObject == null) {
                    logicalObject = new ObjectScopeImpl(this, identifiers, function.getBlockRange());
                    logicalElements.put(identifiers.get(0).getName(), logicalObject);
                }
                FunctionScopeImpl functionImpl = (FunctionScopeImpl) function;
                logicalObject.addElement(functionImpl);
                if (logicalObject.isLogical() && !ModelUtils.isPrototype(functionImpl)) {
                    functionImpl.setStatic(true);
                }
                break;
        }

    }
    
    private ObjectScopeImpl findOrCreateLogicaObject(FileScope fScope, List<Identifier> fqName, OffsetRange range) {
        ObjectScopeImpl result = ModelUtils.findObjectWithName(fScope, fqName.get(0).getName());
        List<Identifier> fqNameOfCreated = new ArrayList<Identifier>(fqName.size());
        fqNameOfCreated.add(fqName.get(0));
        if (result == null) {
            result = new ObjectScopeImpl(fScope, fqNameOfCreated, range);
            ((FileScopeImpl) fScope).addObject(result);
        }
        for (int i = 1; i < fqName.size(); i++) {
            ModelElement me = ModelUtils.getFirst(ModelUtils.getFirst(result.getElements(), fqName.get(i).getName()));
            fqNameOfCreated.add(fqName.get(i));
            if (me == null) {
                result = new ObjectScopeImpl(result, fqNameOfCreated, range);
            } else {
                result = (ObjectScopeImpl) me;
            }
        }
        return result;
    }
}
