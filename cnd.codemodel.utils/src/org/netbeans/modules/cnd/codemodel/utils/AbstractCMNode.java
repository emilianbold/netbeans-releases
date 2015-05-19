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

package org.netbeans.modules.cnd.codemodel.utils;

import java.awt.Image;
import org.netbeans.modules.cnd.api.codemodel.CMCursor;
import org.netbeans.modules.cnd.api.codemodel.CMCursorKind;
import org.netbeans.modules.cnd.api.codemodel.CMSourceLocation;
import org.netbeans.modules.cnd.api.codemodel.CMSourceRange;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Lookup;

/**
 * Abstract base class for CMNode. 
 * Disadvantage of (previous version) of CsmNode is that it necessarily stores CMObject.
 * AbstractNode just declares abstract method 
 * CsmObject getCsmObject()
 *
 * @author Vladimir Kvashin
 */
public class AbstractCMNode extends AbstractNode {
    protected CMCursorKind kind;
    protected CMCursor.CXXAccessSpecifier accessSpecifier;
    protected boolean isStatic;
    protected CharSequence name;
    protected CharSequence displayName;
    protected int startLine;
    protected int startCol;
    protected int startOffset;
    protected int endtLine;
    protected int endCol;
    protected int endOffset;
    
    public AbstractCMNode(CMCursor cur, Children children, Lookup lookup) {
        super(children, lookup);
        kind = cur.getKind();
        name = cur.getSpellingName();
        displayName = cur.getDisplayName();
        accessSpecifier = cur.getAccessSpecifier();
        isStatic = cur.isStaticMethod();
        CMSourceRange extent = cur.getExtent();
        CMSourceLocation start = extent.getStart();
        startLine = start.getLine();
        startCol = start.getColumn();
        startOffset = start.getOffset();
        CMSourceLocation end = extent.getEnd();
        endtLine = end.getLine();
        endCol = end.getColumn();
        endOffset = end.getOffset();
    }

    @Override
    public Image getIcon(int param) {
        switch(kind) {
            case InclusionDirective:
                return CMImageLoader.INCLUDE_USER.getImage();
            case Constructor:
                return CMImageLoader.CONSTRUCTOR_PUBLIC.getImage();
            case Destructor:
                return CMImageLoader.DESTRUCTOR_PUBLIC.getImage();
            case CXXMethod:
                switch(accessSpecifier) {
                    case Public:
                        if (isStatic) {
                            return CMImageLoader.METHOD_ST_PUBLIC.getImage();
                        } else {
                            return CMImageLoader.METHOD_PUBLIC.getImage();
                        }
                    case Protected:
                        if (isStatic) {
                            return CMImageLoader.METHOD_ST_PROTECTED.getImage();
                        } else {
                            return CMImageLoader.METHOD_PROTECTED.getImage();
                        }
                    case Private:
                    default:    
                        if (isStatic) {
                            return CMImageLoader.METHOD_ST_PRIVATE.getImage();
                        } else {
                            return CMImageLoader.METHOD_PRIVATE.getImage();
                        }
                }
            case MacroDefinition:
                return CMImageLoader.MACRO.getImage();
            case UnionDecl:
                return CMImageLoader.UNION.getImage();
            case StructDecl:
                return CMImageLoader.STRUCT.getImage();
            case ClassTemplate:
            case ClassTemplatePartialSpecialization:
            case ClassDecl:
                return CMImageLoader.CLASS.getImage();
            case EnumDecl:
                return CMImageLoader.ENUMERATION.getImage();
            case EnumConstantDecl:
                return CMImageLoader.ENUMERATOR.getImage();
            case VarDecl:
                return CMImageLoader.VARIABLE_LOCAL.getImage();
            case ConversionFunction:
            case FunctionTemplate:
            case FunctionDecl:
                return CMImageLoader.FUNCTION_DECLARATION_GLOBAL.getImage();
            case FieldDecl:
                switch(accessSpecifier) {
                    case Public:
                        return CMImageLoader.FIELD_PUBLIC.getImage();
                    case Protected:
                        return CMImageLoader.FIELD_PROTECTED.getImage();
                    case Private:
                    default:    
                        return CMImageLoader.FIELD_PRIVATE.getImage();
                }
            case UnexposedDecl:
                return CMImageLoader.FRIEND_METHOD.getImage();
            case Namespace:
                return CMImageLoader.NAMESPACE.getImage();
            case NamespaceAlias:
                return CMImageLoader.NAMESPACE_ALIAS.getImage();
            case UsingDeclaration:
                return CMImageLoader.USING_DECLARATION.getImage();
            case UsingDirective:
                return CMImageLoader.USING.getImage();
            case TypedefDecl:
                return CMImageLoader.TYPEDEF.getImage();
            default:
                return CMImageLoader.DEFAULT.getImage();
        }
    }

    @Override
    public String getDisplayName() {
        return displayName.toString();
    }

    @Override
    public String getName() {
        return name.toString();
    }

    @Override
    public Image getOpenedIcon(int param) {
        return getIcon(param);
    }
}
