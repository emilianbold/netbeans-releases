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
import org.netbeans.modules.javascript2.editor.model.Identifier;
import org.netbeans.modules.javascript2.editor.model.JsObject;
import org.netbeans.modules.javascript2.editor.model.Occurrence;

/**
 *
 * @author Petr Pisl
 */
public class JsObjectImpl extends JsElementImpl implements JsObject {

    final private HashMap<String, JsObject> properties;
    final private Identifier declarationName;
    final private JsObject parent;
    final private List<Occurrence> occurrences;
    final private HashMap<Integer, Collection<String>> assignments;
    private boolean isDeclared;
    
    public JsObjectImpl(JsObject parent, Identifier name, OffsetRange offsetRange) {
        super((parent != null ? parent.getFileObject() : null), name.getName(), offsetRange, EnumSet.of(Modifier.PUBLIC));
        this.properties = new HashMap<String, JsObject>();
        this.declarationName = name;
        this.parent = parent;
        this.isDeclared = false;
        this.occurrences = new ArrayList<Occurrence>();
        this.assignments = new HashMap<Integer, Collection<String>>();
    }
    
    @Override
    public Identifier getDeclarationName() {
        return declarationName;
    }

    @Override
    public Kind getJSKind() {
        if (parent == null) {
            // global object
            return Kind.FILE;
        }
        if (getProperties().isEmpty()) {
            if (getParent().getParent() == null || getModifiers().contains(Modifier.PRIVATE)) {
                // variable or the global object
                return Kind.VARIABLE;
            }
            return Kind.PROPERTY;
        }
        return Kind.OBJECT;
    }
    
    @Override 
    public Map<String, ? extends JsObject> getProperties() {
        return properties;
    }

    @Override
    public void addProperty(String name, JsObject property) {
        properties.put(name, property);
    }

    @Override
    public JsObject getProperty(String name) {
        return properties.get(name);
    }

    @Override
    public JsObject getParent() {
        return parent;
    }

    @Override
    public boolean isDeclared() {
        return isDeclared;
    }

    public void setDeclared(boolean isDeclared) {
        this.isDeclared = isDeclared;
    }

    @Override
    public int getOffset() {
        return declarationName.getOffsetRange().getStart();
    }

    @Override
    public List<Occurrence> getOccurrences() {
        return occurrences;
    }
    
    public void addOccurrence(OffsetRange offsetRange) {
        occurrences.add(new OccurrenceImpl(offsetRange, this));
    }
    
    public void addAssignment(String typeName, int offset){
        System.out.println("addingSignment: " + typeName + ", " + offset);
        Collection<String> types = assignments.get(offset);
        if (types == null) {
            types = new ArrayList<String>();
            assignments.put(offset, types);
        }
        types.add(typeName);
    }

    @Override
    public Collection<String> getAssignmentTypeNames(int offset) {
        Collection<String> result = Collections.EMPTY_LIST;
        int closeOffset = -1;
        for(Integer position : assignments.keySet()) {
            if (closeOffset < position && closeOffset <= offset) {
                closeOffset = position;
                result = assignments.get(position);
            }
        }
        
        return result;
    }
}
