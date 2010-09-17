/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.modelimpl.csm;

import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstUtil;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableBase;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;


/**
 *
 * @author Alexander Simon
 */
public class NameHolder {
    private final CharSequence name;
    private int start = 0;
    private int end = 0;

    private NameHolder(AST ast, int kind) {
        switch (kind) {
            case 1:
                name = "~"+findFunctionName(ast); // NOI18N
                break;
            case 2:
                name =findDestructorDefinitionName(ast);
                break;
            default:
                name = findFunctionName(ast);
        }
    }

    private NameHolder(CharSequence name) {
        this.name = name;
    }

    public static NameHolder createName(CharSequence name) {
        return new NameHolder(name);
    }

    public static NameHolder createFunctionName(AST ast) {
        return new NameHolder(ast, 0);
    }

    public static NameHolder createDestructorName(AST ast) {
        return new NameHolder(ast, 1);
    }

    public static NameHolder createDestructorDefinitionName(AST ast) {
        return new NameHolder(ast, 2);
    }

    public CharSequence getName(){
        return name;
    }

    public int getStartOffset(){
        return start;
    }

    public int getEndOffset(){
        return end;
    }

    public void addReference(CsmFile file, final CsmObject decl) {
        if (file instanceof FileImpl) {
            if (start > 0) {
                final FileImpl fileImpl = (FileImpl) file;
                CsmReference ref = new CsmReference() {

                    @Override
                    public CsmReferenceKind getKind() {
                        return CsmReferenceKind.DECLARATION;
                    }

                    @Override
                    public CsmObject getReferencedObject() {
                        return decl;
                    }

                    @Override
                    public CsmObject getOwner() {
                        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
                    }

                    @Override
                    public CsmFile getContainingFile() {
                        return fileImpl;
                    }

                    @Override
                    public int getStartOffset() {
                        return start;
                    }

                    @Override
                    public int getEndOffset() {
                        return end;
                    }

                    @Override
                    public Position getStartPosition() {
                        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
                    }

                    @Override
                    public Position getEndPosition() {
                        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
                    }

                    @Override
                    public CharSequence getText() {
                        return name;
                    }
                };
                fileImpl.addReference(ref, decl);
            }
        }
        
    }

    private CharSequence findDestructorDefinitionName(AST node) {
        AST token = node.getFirstChild();
        if (token != null) {
            token = AstUtil.findSiblingOfType(token, CPPTokenTypes.CSM_QUALIFIED_ID);
        }
        if (token != null) {
            token = AstUtil.findChildOfType(token, CPPTokenTypes.TILDE);
            if (token != null) {
                start = OffsetableBase.getStartOffset(token);
                end = OffsetableBase.getEndOffset(token);
                token = token.getNextSibling();
                if (token != null && token.getType() == CPPTokenTypes.ID) {
                    end = OffsetableBase.getEndOffset(node);
                    return "~" + token.getText(); // NOI18N
                }
            }
        }
        return "~"; // NOI18N
    }

    private CharSequence findFunctionName(AST ast) {
        if( CastUtils.isCast(ast) ) {
            return getFunctionName(ast);
        }
        AST token = AstUtil.findMethodName(ast);
        if (token != null){
            return extractName(token);
        }
        return "";
    }

    private String getFunctionName(AST ast) {
	AST operator = AstUtil.findChildOfType(ast, CPPTokenTypes.LITERAL_OPERATOR);
	if( operator == null ) {
            // error in AST
	    return "operator ???"; // NOI18N
	}
        start = OffsetableBase.getStartOffset(operator);
        end = OffsetableBase.getEndOffset(operator);
	StringBuilder sb = new StringBuilder(operator.getText());
	sb.append(' ');
	begin:
	for( AST next = operator.getNextSibling(); next != null; next = next.getNextSibling() ) {
	    switch( next.getType() ) {
		case CPPTokenTypes.CSM_TYPE_BUILTIN:
		case CPPTokenTypes.CSM_TYPE_COMPOUND:
		    sb.append(' ');
		    addTypeText(next, sb);
                    break;
		case CPPTokenTypes.CSM_PTR_OPERATOR:
		    addTypeText(next, sb);
                    break;
		case CPPTokenTypes.LPAREN:
		    break begin;
		case CPPTokenTypes.AMPERSAND:
		case CPPTokenTypes.STAR:
		case CPPTokenTypes.LITERAL_const:
                case CPPTokenTypes.LITERAL___const:
                case CPPTokenTypes.LITERAL___const__:
                    end = OffsetableBase.getEndOffset(next);
		    sb.append(next.getText());
		    break;
		default:
		    sb.append(' ');
                    end = OffsetableBase.getEndOffset(next);
		    sb.append(next.getText());
	    }
	}
	return sb.toString();
    }

    private void addTypeText(AST ast, StringBuilder sb) {
	if( ast == null ) {
	    return;
	}
	for( AST child = ast.getFirstChild(); child != null; child = child.getNextSibling() ) {
	    if( CPPTokenTypes.CSM_START <= child.getType() && child.getType() <= CPPTokenTypes.CSM_END ) {
		addTypeText(child, sb);
	    }
	    else {
                end = OffsetableBase.getEndOffset(child);
		String text = child.getText();
		assert text != null;
		assert text.length() > 0;
		if( sb.length() > 0 ) {
		    if( Character.isLetterOrDigit(sb.charAt(sb.length() - 1)) ) {
			if( Character.isLetterOrDigit(text.charAt(0)) ) {
			    sb.append(' ');
			}
		    }
		}
		sb.append(text);
	    }
	}
    }

    private CharSequence extractName(AST token){
        int type = token.getType();
        if( type == CPPTokenTypes.ID ) {
            start = OffsetableBase.getStartOffset(token);
            end = OffsetableBase.getEndOffset(token);
            return AstUtil.getText(token);
        } else if( type == CPPTokenTypes.CSM_QUALIFIED_ID ) {
            AST last = AstUtil.getLastChild(token);
            if( last != null) {
                if (last.getType() == CPPTokenTypes.GREATERTHAN) {
                    AST lastId = null;
                    int level = 0;
                    for (AST token2 = token.getFirstChild(); token2 != null; token2 = token2.getNextSibling()) {
                        int type2 = token2.getType();
                        switch (type2) {
                            case CPPTokenTypes.ID:
                                lastId = token2;
                                break;
                            case CPPTokenTypes.GREATERTHAN:
                                level--;
                                break;
                            case CPPTokenTypes.LESSTHAN:
                                level++;
                                break;
                            default:
                                if (level == 0) {
                                    lastId = null;
                                }
                        }
                    }
                    if (lastId != null) {
                        last = lastId;
                    }
                }
                if( last.getType() == CPPTokenTypes.ID ) {
                    start = OffsetableBase.getStartOffset(last);
                    end = OffsetableBase.getEndOffset(last);
                    return AstUtil.getText(last);
                } else {
//		    if( first.getType() == CPPTokenTypes.LITERAL_OPERATOR ) {
                    AST operator = AstUtil.findChildOfType(token, CPPTokenTypes.LITERAL_OPERATOR);
                    if( operator != null ) {
                        start = OffsetableBase.getStartOffset(operator);
                        end = OffsetableBase.getEndOffset(operator);
                        StringBuilder sb = new StringBuilder(operator.getText());
                        sb.append(' ');
                        for( AST next = operator.getNextSibling(); next != null; next = next.getNextSibling() ) {
                            sb.append(next.getText());
                            end = OffsetableBase.getEndOffset(next);
                        }
                        return sb.toString();
                    } else {
                        AST first = token.getFirstChild();
                        if (first.getType() == CPPTokenTypes.ID) {
                            start = OffsetableBase.getStartOffset(first);
                            end = OffsetableBase.getEndOffset(first);
                            return AstUtil.getText(first);
                        }
                    }
                }
            }
        }
        return ""; // NOI18N
    }
}
