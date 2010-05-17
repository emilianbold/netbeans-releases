/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.groovy.editor.api.elements;

import groovyjarjarasm.asm.Opcodes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.groovy.editor.api.AstUtilities;
import org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId;
import org.netbeans.modules.groovy.editor.api.lexer.LexUtilities;
import org.netbeans.modules.groovy.editor.api.parser.GroovyParserResult;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Adamek
 */
public abstract class AstElement extends GroovyElement {

    protected final ASTNode node;

    protected final GroovyParserResult info;

    protected List<AstElement> children;

    protected String name;

    protected Set<Modifier> modifiers;

    protected String in;

    protected String signature;
    
    public AstElement(GroovyParserResult info, ASTNode node) {
        this.info = info;
        this.node = node;
    }
    
    public List<AstElement> getChildren() {
        if (children == null) {
            return Collections.<AstElement>emptyList();
        }

        return children;
    }

    public void addChild(AstElement child) {
        if (children == null) {
            children = new ArrayList<AstElement>();
        }

        children.add(child);
    }

    public String getSignature() {
        if (signature == null) {
            StringBuilder sb = new StringBuilder();
            String clz = getIn();
            if (clz != null && clz.length() > 0) {
                sb.append(clz);
                sb.append("."); // NOI18N
            }
            sb.append(getName());
            signature = sb.toString();
        }

        return signature;
    }

    public abstract String getName();

    public ASTNode getNode() {
        return node;
    }
    
    public String getIn() {
        return in;
    }

    public ElementKind getKind() {
        return ElementKind.OTHER;
    }

    public Set<Modifier> getModifiers() {
        if (modifiers == null) {
            int flags = -1;
            if (node instanceof FieldNode) {
                flags = ((FieldNode) node).getModifiers();
            } else if (node instanceof MethodNode) {
                flags = ((MethodNode) node).getModifiers();
            }
            if (flags != -1) {
                Set<Modifier> result = EnumSet.noneOf(Modifier.class);
                if ((flags & Opcodes.ACC_PUBLIC) != 0) {
                    result.add(Modifier.PUBLIC);
                }
                if ((flags & Opcodes.ACC_PROTECTED) != 0) {
                    result.add(Modifier.PROTECTED);
                }
                if ((flags & Opcodes.ACC_PRIVATE) != 0) {
                    result.add(Modifier.PRIVATE);
                }
                if ((flags & Opcodes.ACC_STATIC) != 0) {
                    result.add(Modifier.STATIC);
                }
                modifiers = result;
            } else {
                modifiers = Collections.<Modifier>emptySet();
            }
        }

        return modifiers;
    }

    public void setIn(String in) {
        this.in = in;
    }
    
    public boolean signatureEquals (final ElementHandle handle) {
        if (handle instanceof AstElement) {
                return this.equals(handle);
            }
        return false;
    }
    
    // FIXME: This is an empty implementations to make a 
    // AstElement a ElementHandle. Seems not to affect others. Sure?
    
    public FileObject getFileObject() {
        return null;
    }

    public GroovyParserResult getParseResult() {
        return info;
    }

    public String getMimeType() {
        return GroovyTokenId.GROOVY_MIME_TYPE;
    }
    

    public static AstElement create(GroovyParserResult info, ASTNode node) {
        if (node instanceof MethodNode) {
            return new AstMethodElement(info, node);
        }
        return null;
    }

    @Override
    public String toString() {
        return getKind() + "<" + getName() + ">";
    }

    @Override
    public OffsetRange getOffsetRange(ParserResult result) {
        GroovyParserResult parserResult = AstUtilities.getParseResult(result);
        // FIXME resolve handle
        Element object = ElementHandleSupport.resolveHandle(parserResult, ElementHandleSupport.createHandle(result, this));

        if (object instanceof AstElement) {
             BaseDocument doc = (BaseDocument) result.getSnapshot().getSource().getDocument(false);
             if (doc != null) {
                AstElement astElement = (AstElement) object;
                OffsetRange range = AstUtilities.getRange(astElement.getNode(), doc);
                return LexUtilities.getLexerOffsets(parserResult, range);
             }
             return OffsetRange.NONE;
        } else if (object != null) {
            Logger logger = Logger.getLogger(AstElement.class.getName());
            logger.log(Level.WARNING, "Foreign element: " + object + " of type " + //NOI18N
                    ((object != null) ? object.getClass().getName() : "null")); //NOI18N
        } else {
            if (getNode() != null) {
                BaseDocument doc = (BaseDocument) result.getSnapshot().getSource().getDocument(false);
                if (doc != null) {
                    OffsetRange astRange = AstUtilities.getRange(getNode(), doc);
                    if (astRange != OffsetRange.NONE) {
                        GroovyParserResult oldInfo = info;
                        if (oldInfo == null) {
                            oldInfo = parserResult;
                        }
                        return LexUtilities.getLexerOffsets(oldInfo, astRange);
                    } else {
                        return OffsetRange.NONE;
                    }
                }
            }
        }

        return OffsetRange.NONE;
    }
}
