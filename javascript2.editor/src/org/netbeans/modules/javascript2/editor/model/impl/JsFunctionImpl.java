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
package org.netbeans.modules.javascript2.editor.model.impl;

import java.util.*;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.javascript2.editor.model.*;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Pisl
 */
public class JsFunctionImpl extends DeclarationScopeImpl implements JsFunction {

    final private HashMap <String, JsObject> parametersByName;
    final private List<JsObject> parameters;
    final private Set<TypeUsage> returnTypes;
    private boolean areReturnTypesResolved;
    private boolean isAnonymous;
    
    
    public JsFunctionImpl(DeclarationScope scope, JsObject parentObject, Identifier name, List<Identifier> parameters, OffsetRange offsetRange) {
        super(scope, parentObject, name, offsetRange);
        this.parametersByName = new HashMap<String, JsObject>(parameters.size());
        this.parameters = new ArrayList<JsObject>(parameters.size());
        for (Identifier identifier : parameters) {
            JsObject parameter = new ParameterObject(this, identifier);
            this.parametersByName.put(identifier.getName(), parameter);
            this.parameters.add(parameter);
        }
        this.isAnonymous = false;
        this.returnTypes = new HashSet<TypeUsage>();
        setDeclared(true);
        this.areReturnTypesResolved = false;
        if (parentObject != null) {
            // creating arguments variable
            JsObjectImpl arguments = new JsObjectImpl(this, 
                    new IdentifierImpl("arguments", new OffsetRange(name.getOffsetRange().getStart(), name.getOffsetRange().getStart())), 
                    name.getOffsetRange(),  false, EnumSet.of(Modifier.PRIVATE)); // NOI8N
            arguments.addAssignment(new TypeUsageImpl("Arguments", getOffset(), true), getOffset());    // NOI18N
            this.addProperty(arguments.getName(), arguments);
        }
    }
    
    public static JsFunctionImpl createGlobal(FileObject fileObject, int length) {
        String name = fileObject != null ? fileObject.getName() : "VirtualSource"; //NOI18N
        Identifier ident = new IdentifierImpl(name, new OffsetRange(0, length));
        return new JsFunctionImpl(fileObject, ident);
    }
    
    private JsFunctionImpl(FileObject file, Identifier name) {
        this(null, null, name, Collections.EMPTY_LIST, name.getOffsetRange());
        this.setFileObject(file);
    }
    
    protected JsFunctionImpl(FileObject file, JsObject parentObject, Identifier name, List<Identifier> parameters) {
        this(null, parentObject, name, parameters, name.getOffsetRange());
        this.setFileObject(file);
        this.setDeclared(false);
    }
    
    @Override
    public Collection<? extends JsObject> getParameters() {
        return this.parameters;
    }

    @Override
    public Kind getJSKind() {
        if (getParent() == null) {
            // global function
            return JsElement.Kind.FILE;
        }
        if (getName().startsWith("get ")) { //NOI18N
            return JsElement.Kind.PROPERTY_GETTER;
        }
        if (getName().startsWith("set ")) { //NOI18N
            return JsElement.Kind.PROPERTY_SETTER;
        }
        if (getParent() != null && getParent() instanceof JsFunction) {
            for (JsObject property : getProperties().values()) {
                if (property.isDeclared() 
                        && (property.getModifiers().contains(Modifier.PROTECTED)
                        || (property.getModifiers().contains(Modifier.PUBLIC) &&  !property.getModifiers().contains(Modifier.STATIC)))
                        && !isAnonymous()) {
                    if(!getParent().getName().equals("prototype")) {
                        return JsElement.Kind.CONSTRUCTOR;
                    }
                }
            }
        }

        JsElement.Kind result = JsElement.Kind.FUNCTION;

        if (getParent().getJSKind() != JsElement.Kind.FILE) {
            result = JsElement.Kind.METHOD;
        }
        return result;
    }

    @Override
    public boolean isAnonymous() {
        return isAnonymous;
    }

    public void setAnonymous(boolean isAnonymous) {
        this.isAnonymous = isAnonymous;
    }

    @Override
    public JsObject getParameter(String name) {
        JsObject result = parametersByName.get(name);
        return result;
    }

    @Override
    public Collection<? extends TypeUsage> getReturnTypes() {
        Collection<TypeUsage> returns = new HashSet();
        for(TypeUsage type : returnTypes) {
             if (((TypeUsageImpl)type).isResolved()) {
                returns.add(type);
            } else {
                 JsObject jsObject = ModelUtils.findJsObjectByName(ModelUtils.getGlobalObject(this), type.getType());
                 if(jsObject != null) {
                     returns.addAll(resolveAssignments(jsObject, type.getOffset()));
                 }
            }
        }
        return returns;
    }    
        
    public void addReturnType(TypeUsage type) {
        this.returnTypes.add(type);
    }
    
    public void addReturnType(Collection<TypeUsage> types) {
        this.returnTypes.addAll(types);
    }
    
    public boolean areReturnTypesEmpty() {
        return returnTypes.isEmpty();
    }

    @Override
    public void resolveTypes() {
        super.resolveTypes();
        HashSet<String> nameReturnTypes = new HashSet<String>();
        Collection<TypeUsage> resolved = new ArrayList();
        for (TypeUsage type : returnTypes) {
            if (!(type.getType().equals(Type.UNRESOLVED) && returnTypes.size() > 1)) {
                if (!((TypeUsageImpl) type).isResolved()) {
                    for(TypeUsage rType : ModelUtils.resolveTypeFromSemiType(this, type)) {
                        if(!nameReturnTypes.contains(type.getType())) {
                            resolved.add(rType);
                            nameReturnTypes.add(rType.getType());
                        } 
                    }
                    resolved.addAll(ModelUtils.resolveTypeFromSemiType(this, type));
                } else {
                    if (!nameReturnTypes.contains(type.getType())) {
                        resolved.add(type);
                        nameReturnTypes.add(type.getType());
                    }
                }
            }
        }
        
        JsObject global = ModelUtils.getGlobalObject(this);
        for (TypeUsage type : resolved) {
            if (type.getOffset() > 0) {
                JsObject jsObject = ModelUtils.findJsObjectByName(global, type.getType());
                if (jsObject != null) {
                    ((JsObjectImpl)jsObject).addOccurrence(new OffsetRange(type.getOffset(), type.getOffset() + type.getType().length()));
                }
            }
        }
        returnTypes.clear();
        returnTypes.addAll(resolved);
    }
    
    
}
