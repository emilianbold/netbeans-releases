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

import java.util.Collections;
import java.util.Set;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.javascript2.editor.lexer.CommonTokenId;
import org.netbeans.modules.javascript2.editor.model.JsElement;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Pisl
 */
public abstract class JsElementImpl implements JsElement {

    private FileObject fileObject;
    private String name;
    private OffsetRange offsetRange;
    private Set<Modifier> modifiers;
    private boolean isDeclared;
    
    public JsElementImpl(FileObject fileObject, String name, boolean isDeclared, OffsetRange offsetRange, Set<Modifier> modifiers) {
        this.fileObject = fileObject;
        this.name = name;
        this.offsetRange = offsetRange;
        this.modifiers = modifiers;
        this.isDeclared = isDeclared;
    }
    
    public JsElementImpl(FileObject fileObject, String name, boolean isDeclared, OffsetRange offsetRange) {
        this(fileObject, name, isDeclared, offsetRange, Collections.<Modifier>emptySet());
    }
           
    @Override
    public ElementKind getKind() {
        ElementKind result = ElementKind.OTHER;
        switch (getJSKind()) {
            case CONSTRUCTOR: 
                result = ElementKind.CONSTRUCTOR;
                break;
            case METHOD:
            case FUNCTION:
            case PROPERTY_GETTER:
            case PROPERTY_SETTER:
                result = ElementKind.METHOD;
                break;
            case OBJECT:
            case ANONYMOUS_OBJECT:
            case OBJECT_LITERAL:
                result = ElementKind.CLASS;
                break;
            case PROPERTY:
                result = ElementKind.FIELD;
                break;
            case FILE:
                result = ElementKind.FILE;
                break;
            case PARAMETER:
                result = ElementKind.PARAMETER;
                break;
            case VARIABLE:
                result = ElementKind.VARIABLE;
                break;
            case FIELD:
                result = ElementKind.FIELD;
                break;
        }
        return result;
    }
    
    @Override
    public FileObject getFileObject() {
        return fileObject;
    }
    
    protected void setFileObject(FileObject fileObject) {
        this.fileObject = fileObject;
    }

    @Override
    public String getMimeType() {
        return CommonTokenId.JAVASCRIPT_MIME_TYPE;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getIn() {
        return null;
    }

    @Override
    public boolean isDeclared() {
        return isDeclared;
    }

    public void setDeclared(boolean isDeclared) {
        this.isDeclared = isDeclared;
    }
   
    @Override
    public OffsetRange getOffsetRange(ParserResult result) {
        return offsetRange;
    }

    @Override
    public int getOffset() {
        return offsetRange.getStart();
    }
    
    @Override
    public Set<Modifier> getModifiers() {
        return modifiers;
    }

    @Override
    public boolean signatureEquals(ElementHandle handle) {
        return false;
    }
    
    public void addModifier(Modifier modifier) {
        modifiers.add(modifier);
    }
    
}
