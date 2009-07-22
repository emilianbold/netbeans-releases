/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.java.hints.errors;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementUtilities.ElementAcceptor;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.ModificationResult.Difference;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.editor.GuardedDocument;
import org.netbeans.editor.MarkBlock;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.openide.filesystems.FileObject;
import org.openide.text.NbDocument;
import org.openide.text.PositionRef;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Lahoda
 */
public class Utilities {
    private static final String DEFAULT_NAME = "name";

    public Utilities() {
    }

    public static String guessName(CompilationInfo info, TreePath tp) {
        ExpressionTree et = (ExpressionTree) tp.getLeaf();
        String name = getName(et);
        
        if (name == null) {
            if(et instanceof LiteralTree) {
                Object guess = ((LiteralTree) et).getValue();
                if (guess != null && guess instanceof String)
                    return guessLiteralName((String) guess);
            } 
            return DEFAULT_NAME;
        }
        
        Scope s = info.getTrees().getScope(tp);
        int counter = 0;
        boolean cont = true;
        String proposedName = name;
        
        while (cont) {
            proposedName = name + (counter != 0 ? String.valueOf(counter) : "");
            
            cont = false;
            
            for (Element e : info.getElementUtilities().getLocalMembersAndVars(s, new VariablesFilter())) {
                if (proposedName.equals(e.getSimpleName().toString())) {
                    counter++;
                    cont = true;
                    break;
                }
            }
        }
        
        return proposedName;
    }

    private static String guessLiteralName(String str) {
        StringBuffer sb = new StringBuffer();
        if(str.length() == 0)
            return DEFAULT_NAME;
        char first = str.charAt(0);
        if(Character.isJavaIdentifierStart(str.charAt(0)))
            sb.append(first);

        for (int i = 1; i < str.length(); i++) {
            char ch = str.charAt(i);
            if(ch == ' ') {
                sb.append('_');
                continue;
            }
            if (Character.isJavaIdentifierPart(ch))
                sb.append(ch);
            if (i > 40)
                break;
        }
        if (sb.length() == 0)
            return DEFAULT_NAME;
        else
            return sb.toString();
    }
    
    /**
     * @param tp tested {@link TreePath}
     * @return true if <code>tp</code> is an IDENTIFIER in a VARIABLE in an ENHANCED_FOR_LOOP
     */
    public static boolean isEnhancedForLoopIdentifier(TreePath tp) {
        if (tp == null || tp.getLeaf().getKind() != Kind.IDENTIFIER)
            return false;
        TreePath parent = tp.getParentPath();
        if (parent == null || parent.getLeaf().getKind() != Kind.VARIABLE)
            return false;
        TreePath context = parent.getParentPath();
        if (context == null || context.getLeaf().getKind() != Kind.ENHANCED_FOR_LOOP)
            return false;
        return true;
    }

    /**
     *
     * @param info context {@link CompilationInfo}
     * @param iterable tested {@link TreePath}
     * @return generic type of an {@link Iterable} or {@link ArrayType} at a TreePath
     */
    public static TypeMirror getIterableGenericType(CompilationInfo info, TreePath iterable) {
        TypeElement iterableElement = info.getElements().getTypeElement("java.lang.Iterable"); //NOI18N
        if (iterableElement == null) {
            return null;
        }
        TypeMirror iterableType = info.getTrees().getTypeMirror(iterable);
        if (iterableType == null) {
            return null;
        }
        TypeMirror designedType = null;
        if (iterableType.getKind() == TypeKind.DECLARED) {
            DeclaredType declaredType = (DeclaredType) iterableType;
            if (!info.getTypes().isSubtype(info.getTypes().erasure(declaredType), info.getTypes().erasure(iterableElement.asType()))) {
                return null;
            }
            ExecutableElement iteratorMethod = (ExecutableElement) iterableElement.getEnclosedElements().get(0);
            ExecutableType iteratorMethodType = (ExecutableType) info.getTypes().asMemberOf(declaredType, iteratorMethod);
            List<? extends TypeMirror> typeArguments = ((DeclaredType) iteratorMethodType.getReturnType()).getTypeArguments();
            if (!typeArguments.isEmpty()) {
                designedType = typeArguments.get(0);
            }
        } else if (iterableType.getKind() == TypeKind.ARRAY) {
            designedType = ((ArrayType) iterableType).getComponentType();
        }
        if (designedType == null) {
            return null;
        }
        return resolveCapturedType(info, designedType);
    }

    public static String getName(TypeMirror tm) {
        if (tm.getKind().isPrimitive()) {
            return "" + Character.toLowerCase(tm.getKind().name().charAt(0));
        }
        
        switch (tm.getKind()) {
            case DECLARED:
                DeclaredType dt = (DeclaredType) tm;
                return firstToLower(dt.asElement().getSimpleName().toString());
            case ARRAY:
                return getName(((ArrayType) tm).getComponentType());
            default:
                return DEFAULT_NAME;
        }
    }
    
    public static String getName(ExpressionTree et) {
        return getName((Tree) et);
    }
    
    public static String getName(Tree et) {
        return adjustName(getNameRaw(et));
    }
    
    private static String getNameRaw(Tree et) {
        if (et == null)
            return null;

        switch (et.getKind()) {
        case IDENTIFIER:
            return ((IdentifierTree) et).getName().toString();
        case METHOD_INVOCATION:
            return getName(((MethodInvocationTree) et).getMethodSelect());
        case MEMBER_SELECT:
            return ((MemberSelectTree) et).getIdentifier().toString();
        case NEW_CLASS:
            return firstToLower(getName(((NewClassTree) et).getIdentifier()));
        case PARAMETERIZED_TYPE:
            return firstToLower(getName(((ParameterizedTypeTree) et).getType()));
        default:
            return null;
        }
    }
    
    static String adjustName(String name) {
        if (name == null)
            return null;
        
        String shortName = null;
        
        if (name.startsWith("get") && name.length() > 3) {
            shortName = name.substring(3);
        }
        
        if (name.startsWith("is") && name.length() > 2) {
            shortName = name.substring(2);
        }
        
        if (shortName != null) {
            return firstToLower(shortName);
        }
        
        if (SourceVersion.isKeyword(name)) {
            return "a" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
        } else {
            return name;
        }
    }
    
    private static String firstToLower(String name) {
        if (name.length() == 0)
            return null;
        
        String cand = Character.toLowerCase(name.charAt(0)) + name.substring(1);
        
        if (SourceVersion.isKeyword(cand)) {
            cand = "a" + name;
        }
        
        return cand;
    }
    
    private static final class VariablesFilter implements ElementAcceptor {
        
        private static final Set<ElementKind> ACCEPTABLE_KINDS = EnumSet.of(ElementKind.ENUM_CONSTANT, ElementKind.EXCEPTION_PARAMETER, ElementKind.FIELD, ElementKind.LOCAL_VARIABLE, ElementKind.PARAMETER);
        
        public boolean accept(Element e, TypeMirror type) {
            return ACCEPTABLE_KINDS.contains(e.getKind());
        }
        
    }

    /**
     * Commits changes and provides selection bounds
     *
     * @param target target FileObject
     * @param diff set of changes made by ModificationTask
     * @param tag mark used for selection of generated text
     * @return set of changes made by hint
     * @throws java.io.IOException
     */
    public static ChangeInfo commitAndComputeChangeInfo(FileObject target, final ModificationResult diff, final Object tag) throws IOException {
        List<? extends Difference> differences = diff.getDifferences(target);
        ChangeInfo result = null;
        
        diff.commit();
        
        try {
            if (differences != null) {
                for (Difference d : differences) {
                    if (d.getNewText() != null) { //to filter out possible removes
                        final PositionRef start = d.getStartPosition();
                        Document doc = start.getCloneableEditorSupport().getDocument();

                        if (doc == null) {
                            doc = start.getCloneableEditorSupport().openDocument();
                        }
                        
                        final Position[] pos = new Position[2];
                        final Document fdoc = doc;
                        
                        doc.render(new Runnable() {
                            public void run() {
                                try {
                                    int[] span = diff.getSpan(tag);
                                    if(span != null) {
                                        pos[0] = fdoc.createPosition(span[0]);
                                        pos[1] = fdoc.createPosition(span[1]);
                                    } else {
                                        pos[0] = NbDocument.createPosition(fdoc, start.getOffset(), Position.Bias.Backward);
                                        pos[1] = pos[0];
                                    }
                                } catch (BadLocationException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            }
                        });
                        
                        if (pos[0] != null) {
                            result = new ChangeInfo(target, pos[0], pos[1]);
                        }
                        
                        break;
                    }
                }
            }
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
        
        return result;
    }
    
    public static boolean isMethodHeaderInsideGuardedBlock(CompilationInfo info, MethodTree method) {
        try {
            Document doc = info.getDocument();

            if (doc instanceof GuardedDocument) {
                GuardedDocument bdoc = (GuardedDocument) doc;
                int methodStart = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), method);
                int methodEnd = (int) info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), method);

                return (bdoc.getGuardedBlockChain().compareBlock(methodStart, methodEnd) & MarkBlock.OVERLAP) != 0;
            }

            return false;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return false;
        }
    }
    
    public static TypeMirror resolveCapturedType(CompilationInfo info, TypeMirror tm) {
        TypeMirror type = resolveCapturedTypeInt(info, tm);
        
        if (type.getKind() == TypeKind.WILDCARD) {
            TypeMirror tmirr = ((WildcardType) type).getExtendsBound();
            if (tmirr != null)
                return tmirr;
            else { //no extends, just '?'
                return info.getElements().getTypeElement("java.lang.Object").asType(); // NOI18N
            }
                
        }
        
        return type;
    }
    
    private static TypeMirror resolveCapturedTypeInt(CompilationInfo info, TypeMirror tm) {
        TypeMirror orig = SourceUtils.resolveCapturedType(tm);
        
        if (orig != null) {
            return orig;
        }
        
        if (tm.getKind() == TypeKind.DECLARED) {
            DeclaredType dt = (DeclaredType) tm;
            List<TypeMirror> typeArguments = new LinkedList<TypeMirror>();
            
            for (TypeMirror t : dt.getTypeArguments()) {
                typeArguments.add(resolveCapturedTypeInt(info, t));
            }
            
            final TypeMirror enclosingType = dt.getEnclosingType();
            if (enclosingType.getKind() == TypeKind.DECLARED) {
                return info.getTypes().getDeclaredType((DeclaredType) enclosingType, (TypeElement) dt.asElement(), typeArguments.toArray(new TypeMirror[0]));
            } else {
                return info.getTypes().getDeclaredType((TypeElement) dt.asElement(), typeArguments.toArray(new TypeMirror[0]));
            }
        }

        if (tm.getKind() == TypeKind.ARRAY) {
            ArrayType at = (ArrayType) tm;

            return info.getTypes().getArrayType(resolveCapturedTypeInt(info, at.getComponentType()));
        }
        
        return tm;
    }
    
    public static <T extends Tree> T copyComments(WorkingCopy wc, Tree from, T to) {
        TreeMaker make = wc.getTreeMaker();
        
        for (Comment c : wc.getTreeUtilities().getComments(from, true)) {
            make.addComment(to, c, true);
        }

        for (Comment c : wc.getTreeUtilities().getComments(from, false)) {
            make.addComment(to, c, false);
        }
        
        return to;
    }

    /**
     * Convert typemirror of an anonymous class to supertype/iface
     * 
     * @return typemirror of supertype/iface, initial tm if not anonymous
     */
    public static TypeMirror convertIfAnonymous(TypeMirror tm) {
        //anonymous class?
        Set<ElementKind> fm = EnumSet.of(ElementKind.METHOD, ElementKind.FIELD);
        if (tm instanceof DeclaredType) {
            Element el = ((DeclaredType) tm).asElement();
            if (el.getSimpleName().length() == 0 || fm.contains(el.getEnclosingElement().getKind())) {
                List<? extends TypeMirror> interfaces = ((TypeElement) el).getInterfaces();
                if (interfaces.isEmpty()) {
                    tm = ((TypeElement) el).getSuperclass();
                } else {
                    tm = interfaces.get(0);
                }
            }
        }
        return tm;
    }
}
