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

import com.oracle.nashorn.ir.FunctionNode;
import com.oracle.nashorn.ir.ObjectNode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.javascript2.editor.model.*;


/**
 *
 * @author Petr Pisl
 */
public final class ModelElementFactory {
    
    private ModelElementFactory() {
        
    }
    
    static FunctionScopeImpl create(final FunctionNode function, final ModelBuilder context) {
        final Scope currentScope = context.getCurrentScope();
        FunctionScopeImpl result = new FunctionScopeImpl(currentScope, function);
        return result;
    }
    
    static FunctionScopeImpl create(final FunctionNode function, List<Identifier> name, final ModelBuilder context) {
        assert name != null;
        final Scope currentScope = context.getCurrentScope();
        JsElement.Kind functionType = getFunctionType(function);
        FunctionScopeImpl result = new FunctionScopeImpl(currentScope, name, function, functionType);
        FileScopeImpl fileScope = ModelUtils.getFileScope(currentScope);
        fileScope.addMethod(result);
        return result;
    }
    
    /**
     * It decide, whether the function can be a constructor according this algorithm.
     *    1. If there are defined method inside.
     * 
     * @param function
     * @return true if the function should be treated as constructor
     */
    private static  JsElement.Kind getFunctionType(FunctionNode function) {
        JsElement.Kind type = JsElement.Kind.FUNCTION;
        if (function.getFunctions().size() > 0) {
            type = JsElement.Kind.CONSTRUCTOR;
        } else {
            if (function.getIdent().getStart() == function.getIdent().getFinish()) {
                type = JsElement.Kind.METHOD;
            }
        }
        return type;
    }
    
    static ObjectScopeImpl create(final ObjectNode object, List<Identifier> fqName, final ModelBuilder context) {
        final Scope currentScope = context.getCurrentScope();
        ObjectScopeImpl result = ModelUtils.findObjectWithName(currentScope, fqName.get(0).getName());
        
        if (fqName.size() == 1) {
            ObjectScopeImpl newObject;
            if (result != null) {
                ((ScopeImpl)result.getInElement()).removeElement(result);
                if (result.getInElement() instanceof FileScopeImpl) {
                    ((FileScopeImpl)result.getInElement()).getLogicalElements().remove(result);
                }
                newObject =  new ObjectScopeImpl((Scope)result.getInElement(), object, fqName);
                for (ModelElement element : result.getElements()) {
                    newObject.addElement((ModelElementImpl)element);
                }
            } else {
                newObject = new ObjectScopeImpl(currentScope, object, fqName);
            }
            result = newObject;
            if (currentScope instanceof FileScope) {
                ((FileScopeImpl)currentScope).addObject(result);
            }
        } else {
            List<Identifier> fqNameOfCreated = new ArrayList<Identifier>(fqName.size());
            fqNameOfCreated.add(fqName.get(0));
            if (result == null) {
                FileScope fScope = ModelUtils.getFileScope(currentScope); 
                result = new ObjectScopeImpl(fScope, fqNameOfCreated,fqNameOfCreated.get(0).getOffsetRange());
                ((FileScopeImpl)fScope).addObject(result);
            }
            ModelElement inElement = result;
            for(int i = 1; i < fqName.size(); i++) {
                ModelElement me = ModelUtils.getFirst(ModelUtils.getFirst(result.getElements(), fqName.get(i).getName()));
                fqNameOfCreated.add(fqName.get(i));
                if (me == null) {
                    result = new ObjectScopeImpl(result, fqNameOfCreated, fqName.get(i).getOffsetRange()); 
                } else {
                    result = (ObjectScopeImpl)me;
                    if (result.isLogical() && (i == fqName.size() - 1)) {
                        ((ScopeImpl)inElement).removeElement(result);
                        if (result.getInElement() instanceof FileScopeImpl) {
                            ((FileScopeImpl) result.getInElement()).getLogicalElements().remove(result);
                        }
                        ObjectScopeImpl newObject =  new ObjectScopeImpl((Scope)inElement, object, fqName);
                        for (ModelElement element : result.getElements()) {
                            newObject.addElement((ModelElementImpl)element);
                        }
                        result = newObject;
                    }
                }
                inElement = result;
            }
        }
        
        return result;
    }
    
    static Field createField(List<Identifier> fqName, final ModelBuilder context) {
        FieldImpl result;
        final Scope currentScope = context.getCurrentScope();

        ObjectScope object = ModelUtils.findObjectWithName(currentScope, fqName.get(0).getName());
        List<Identifier> fqNameOfCreated = new ArrayList<Identifier>(fqName.size() - 1);
        fqNameOfCreated.add(fqName.get(0));
        if (object == null) {
            FileScope fScope = ModelUtils.getFileScope(currentScope);
            object = new ObjectScopeImpl(fScope, fqNameOfCreated, fqNameOfCreated.get(0).getOffsetRange());
            ((FileScopeImpl) fScope).addObject(object);
        }
        for (int i = 1; i < fqName.size() - 1; i++) {
            ModelElement me = ModelUtils.getFirst(ModelUtils.getFirst(object.getElements(), fqName.get(i).getName()));
            fqNameOfCreated.add(fqName.get(i));
            if (me == null) {
                object = new ObjectScopeImpl(object, fqNameOfCreated, fqName.get(i).getOffsetRange());
            } else {
                object = (ObjectScopeImpl) me;
            }
        }
        
        Identifier fieldName = fqName.get(fqName.size() - 1);
        ModelElement me = ModelUtils.getFirst(ModelUtils.getFirst(object.getElements(), fieldName.getName()));
        if (me == null) {
            result = new FieldImpl(object, fieldName);
            ((ObjectScopeImpl)object).addElement(result);
        } else {
            result = (FieldImpl)me;
        }
        return result;
    }
}
