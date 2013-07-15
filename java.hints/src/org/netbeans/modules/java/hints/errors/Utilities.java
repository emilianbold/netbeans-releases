/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-22010 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2010 Sun
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

import java.util.logging.Level;
import java.io.CharConversionException;
import org.openide.xml.XMLUtil;
import java.util.logging.Logger;
import javax.lang.model.element.Name;
import com.sun.source.tree.ThrowTree;
import java.util.Stack;
import com.sun.source.tree.BreakTree;
import com.sun.source.tree.ContinueTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.TryTree;
import com.sun.source.util.TreePathScanner;
import org.netbeans.api.java.source.TreeUtilities;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreeScanner;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ElementUtilities.ElementAcceptor;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.ModificationResult.Difference;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.editor.GuardedDocument;
import org.netbeans.editor.MarkBlock;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.text.NbDocument;
import org.openide.text.PositionRef;
import org.openide.util.Exceptions;

import static com.sun.source.tree.Tree.Kind.*;
import org.openide.util.Pair;

/**
 *
 * @author Jan Lahoda
 */
public class Utilities {
    public  static final String JAVA_MIME_TYPE = "text/x-java";
    private static final String DEFAULT_NAME = "name";

    public Utilities() {
    }

    public static String guessName(CompilationInfo info, TreePath tp) {
        return guessName(info, tp, tp);
    }

    public static String guessName(CompilationInfo info, TreePath tp, TreePath scope) {
        return guessName(info, tp, scope, null, null);
    }

    public static String guessName(CompilationInfo info, TreePath tp, TreePath scope, String prefix, String suffix) {
        String name = getName(tp.getLeaf());
        
        if (name == null) {
            return DEFAULT_NAME;
        }
        
        Scope s = info.getTrees().getScope(scope);
        
        return makeNameUnique(info, s, name, prefix, suffix);
    }
    
    private static final Map<String, String> TYPICAL_KEYWORD_CONVERSIONS = new HashMap<String, String>() {{
        put("class", "clazz");
        put("interface", "intf");
        put("new", "nue");
        put("static", "statik");
    }};
    
    public static String makeNameUnique(CompilationInfo info, Scope s, String name, String prefix, String suffix) {
        if(prefix != null && prefix.length() > 0) {
            if(Character.isAlphabetic(prefix.charAt(prefix.length()-1))) {
                StringBuilder nameSb = new StringBuilder(name);
                nameSb.setCharAt(0, Character.toUpperCase(nameSb.charAt(0)));
                name = nameSb.toString();
            }
        }
        
        boolean cont;
        String proposedName;
        int counter = 0;
        do {
            proposedName = safeString(prefix) + name + (counter != 0 ? String.valueOf(counter) : "") + safeString(suffix);
            
            cont = false;
            
            String converted = TYPICAL_KEYWORD_CONVERSIONS.get(proposedName);
            
            if (converted != null) {
                proposedName = converted;
            } else if (SourceVersion.isKeyword(proposedName)) {
                counter++;
                cont = true;
                continue;
            }
            
            for (Element e : info.getElementUtilities().getLocalMembersAndVars(s, new VariablesFilter())) {
                if (proposedName.equals(e.getSimpleName().toString())) {
                    counter++;
                    cont = true;
                    break;
                }
            }
        } while(cont);
        
        return proposedName;
    }
    
    private static String safeString(String str) {
        return str == null ? "" : str;
    }

    public static String makeNameUnique(CompilationInfo info, Scope s, String name) {
        return makeNameUnique(info, s, name, null, null);
    }

    private static String guessLiteralName(String str) {
        if(str.isEmpty())
            return null;
        
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if(ch == ' ') {
                sb.append('_');
            } else if (sb.length() == 0 ? Character.isJavaIdentifierStart(ch) : Character.isJavaIdentifierPart(ch))
                sb.append(ch);
            if (sb.length() > 40)
                break;
        }
        if (sb.length() == 0)
            return null;
        else
            return sb.toString();
    }
    
    public static String toConstantName(String camelCaseName) {
        StringBuilder result = new StringBuilder();
        char[] chars = camelCaseName.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];

            if (Character.isUpperCase(c) && i > 0) {
                if (Character.isLowerCase(chars[i - 1])) {
                    result.append('_');
                } else if (i + 1 < chars.length && Character.isLowerCase(chars[i + 1])) {
                    result.append('_');
                }
            }
            
            result.append(Character.toUpperCase(c));
        }

        return result.toString();
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
            } else {
                TypeElement jlObject = info.getElements().getTypeElement("java.lang.Object");

                if (jlObject != null) {
                    designedType = jlObject.asType();
                }
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
            return getNameRaw(((MethodInvocationTree) et).getMethodSelect());
        case MEMBER_SELECT:
            return ((MemberSelectTree) et).getIdentifier().toString();
        case NEW_CLASS:
            return firstToLower(getNameRaw(((NewClassTree) et).getIdentifier()));
        case PARAMETERIZED_TYPE:
            return firstToLower(getNameRaw(((ParameterizedTypeTree) et).getType()));
        case STRING_LITERAL:
            String name = guessLiteralName((String) ((LiteralTree) et).getValue());
            if(name == null) {
                return firstToLower(String.class.getSimpleName());
            } else {
                return firstToLower(name);
            }
        case VARIABLE:
            return ((VariableTree) et).getName().toString();
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

        StringBuilder result = new StringBuilder();
        boolean toLower = true;
        char last = Character.toLowerCase(name.charAt(0));

        for (int i = 1; i < name.length(); i++) {
            if (toLower && (Character.isUpperCase(name.charAt(i)) || name.charAt(i) == '_')) {
                result.append(Character.toLowerCase(last));
            } else {
                result.append(last);
                toLower = false;
            }
            last = name.charAt(i);

        }

        result.append(toLower ? Character.toLowerCase(last) : last);
        
        if (SourceVersion.isKeyword(result)) {
            return "a" + name;
        } else {
            return result.toString();
        }
    }
    
    public static final class VariablesFilter implements ElementAcceptor {
        
        private static final Set<ElementKind> ACCEPTABLE_KINDS = EnumSet.of(ElementKind.ENUM_CONSTANT, ElementKind.EXCEPTION_PARAMETER, ElementKind.FIELD, ElementKind.LOCAL_VARIABLE, ElementKind.PARAMETER);
        
        public boolean accept(Element e, TypeMirror type) {
            return ACCEPTABLE_KINDS.contains(e.getKind());
        }
        
    }

    public static final String TAG_SELECT = "select";
    public static ChangeInfo commitAndComputeChangeInfo(FileObject target, final ModificationResult diff) throws IOException {
        return commitAndComputeChangeInfo(target, diff, TAG_SELECT);
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
        if (!target.canWrite()) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(Utilities.class, "ERR_ReadOnlyTargetFile", FileUtil.getFileDisplayName(target)), NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notifyLater(nd);
            
            return null;
        }
        
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
                                    if (tag != null) {
                                        int[] span = diff.getSpan(tag);
                                        if(span != null) {
                                            pos[0] = fdoc.createPosition(span[0]);
                                            pos[1] = fdoc.createPosition(span[1]);
                                        }
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
        if (tm == null) return tm;
        
        TypeMirror orig = SourceUtils.resolveCapturedType(tm);

        if (orig != null) {
            tm = orig;
        }
        
        if (tm.getKind() == TypeKind.WILDCARD) {
            TypeMirror extendsBound = ((WildcardType) tm).getExtendsBound();
            TypeMirror rct = resolveCapturedTypeInt(info, extendsBound != null ? extendsBound : ((WildcardType) tm).getSuperBound());
            if (rct != null) {
                return rct.getKind() == TypeKind.WILDCARD ? rct : info.getTypes().getWildcardType(extendsBound != null ? rct : null, extendsBound == null ? rct : null);
            }
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
                if (dt.asElement() == null) return dt;
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
        copyComments(wc, from, to, true);
        copyComments(wc, from, to, false);
        
        return to;
    }

    public static <T extends Tree> T copyComments(WorkingCopy wc, Tree from, T to, boolean preceding) {
        GeneratorUtilities.get(wc).copyComments(from, to, preceding);
        
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
            //XXX: the null check is needed for lambda type, not covered by test:
            if (el != null && (el.getSimpleName().length() == 0 || fm.contains(el.getEnclosingElement().getKind()))) {
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

    public static List<List<TreePath>> splitStringConcatenationToElements(CompilationInfo info, TreePath tree) {
        return sortOut(info, linearize(tree));
    }

    //where:
    private static List<TreePath> linearize(TreePath tree) {
        List<TreePath> todo = new LinkedList<TreePath>();
        List<TreePath> result = new LinkedList<TreePath>();

        todo.add(tree);

        while (!todo.isEmpty()) {
            TreePath tp = todo.remove(0);

            if (tp.getLeaf().getKind() != Kind.PLUS) {
                result.add(tp);
                continue;
            }

            BinaryTree bt = (BinaryTree) tp.getLeaf();

            todo.add(0, new TreePath(tp, bt.getRightOperand()));
            todo.add(0, new TreePath(tp, bt.getLeftOperand()));
        }

        return result;
    }

    private static List<List<TreePath>> sortOut(CompilationInfo info, List<TreePath> trees) {
        List<List<TreePath>> result = new LinkedList<List<TreePath>>();
        List<TreePath> currentCluster = new LinkedList<TreePath>();

        for (TreePath t : trees) {
            if (isConstantString(info, t, true)) {
                currentCluster.add(t);
            } else {
                if (!currentCluster.isEmpty()) {
                    result.add(currentCluster);
                    currentCluster = new LinkedList<TreePath>();
                }
                result.add(new LinkedList<TreePath>(Collections.singletonList(t)));
            }
        }

        if (!currentCluster.isEmpty()) {
            result.add(currentCluster);
        }

        return result;
    }

    public static boolean isConstantString(CompilationInfo info, TreePath tp) {
        return isConstantString(info, tp, false);
    }

    public static boolean isConstantString(CompilationInfo info, TreePath tp, boolean acceptsChars) {
        if (tp.getLeaf().getKind() == Kind.STRING_LITERAL) return true;
        if (acceptsChars && tp.getLeaf().getKind() == Kind.CHAR_LITERAL) return true;

        Element el = info.getTrees().getElement(tp);

        if (el != null && (el.getKind() == ElementKind.FIELD || el.getKind() == ElementKind.LOCAL_VARIABLE)
                && ((VariableElement) el).getConstantValue() instanceof String) {
            return true;
        }

        if (tp.getLeaf().getKind() != Kind.PLUS) {
            return false;
        }

        List<List<TreePath>> sorted = splitStringConcatenationToElements(info, tp);

        if (sorted.size() != 1) {
            return false;
        }

        List<TreePath> part = sorted.get(0);

        for (TreePath c : part) {
            if (isConstantString(info, c, acceptsChars))
                return true;
        }

        return false;
    }

    public static boolean isStringOrCharLiteral(Tree t) {
        return t != null && (t.getKind() == Kind.STRING_LITERAL || t.getKind() == Kind.CHAR_LITERAL);
    }

    public static @NonNull Collection<? extends TreePath> resolveFieldGroup(@NonNull CompilationInfo info, @NonNull TreePath variable) {
        Tree leaf = variable.getLeaf();

        if (leaf.getKind() != Kind.VARIABLE) {
            return Collections.singleton(variable);
        }

        TreePath parentPath = variable.getParentPath();
        Iterable<? extends Tree> children;

        switch (parentPath.getLeaf().getKind()) {
            case BLOCK: children = ((BlockTree) parentPath.getLeaf()).getStatements(); break;
            case ANNOTATION_TYPE:
            case CLASS:
            case ENUM:
            case INTERFACE:
                children = ((ClassTree) parentPath.getLeaf()).getMembers(); break;
            case CASE:  children = ((CaseTree) parentPath.getLeaf()).getStatements(); break;
            default:    children = Collections.singleton(leaf); break;
        }

        List<TreePath> result = new LinkedList<TreePath>();
        ModifiersTree currentModifiers = ((VariableTree) leaf).getModifiers();

        for (Tree c : children) {
            if (c.getKind() != Kind.VARIABLE) continue;

            if (((VariableTree) c).getModifiers() == currentModifiers) {
                result.add(new TreePath(parentPath, c));
            }
        }
        
        return result;
    }

    public static String shortDisplayName(CompilationInfo info, ExpressionTree expression) {
        return new HintDisplayNameVisitor(info).scan(expression, null);
    }
    
    private static final Map<Kind, String> operator2DN;

    static {
        operator2DN = new HashMap<Kind, String>();

        operator2DN.put(AND, "&");
        operator2DN.put(XOR, "^");
        operator2DN.put(OR, "|");
        operator2DN.put(CONDITIONAL_AND, "&&");
        operator2DN.put(CONDITIONAL_OR, "||");
        operator2DN.put(MULTIPLY_ASSIGNMENT, "*=");
        operator2DN.put(DIVIDE_ASSIGNMENT, "/=");
        operator2DN.put(REMAINDER_ASSIGNMENT, "%=");
        operator2DN.put(PLUS_ASSIGNMENT, "+=");
        operator2DN.put(MINUS_ASSIGNMENT, "-=");
        operator2DN.put(LEFT_SHIFT_ASSIGNMENT, "<<=");
        operator2DN.put(RIGHT_SHIFT_ASSIGNMENT, ">>=");
        operator2DN.put(UNSIGNED_RIGHT_SHIFT_ASSIGNMENT, ">>>=");
        operator2DN.put(AND_ASSIGNMENT, "&=");
        operator2DN.put(XOR_ASSIGNMENT, "^=");
        operator2DN.put(OR_ASSIGNMENT, "|=");
        operator2DN.put(BITWISE_COMPLEMENT, "~");
        operator2DN.put(LOGICAL_COMPLEMENT, "!");
        operator2DN.put(MULTIPLY, "*");
        operator2DN.put(DIVIDE, "/");
        operator2DN.put(REMAINDER, "%");
        operator2DN.put(PLUS, "+");
        operator2DN.put(MINUS, "-");
        operator2DN.put(LEFT_SHIFT, "<<");
        operator2DN.put(RIGHT_SHIFT, ">>");
        operator2DN.put(UNSIGNED_RIGHT_SHIFT, ">>>");
        operator2DN.put(LESS_THAN, "<");
        operator2DN.put(GREATER_THAN, ">");
        operator2DN.put(LESS_THAN_EQUAL, "<=");
        operator2DN.put(GREATER_THAN_EQUAL, ">=");
        operator2DN.put(EQUAL_TO, "==");
        operator2DN.put(NOT_EQUAL_TO, "!=");
    }

    private static class HintDisplayNameVisitor extends TreeScanner<String, Void> {

        private CompilationInfo info;

        public HintDisplayNameVisitor(CompilationInfo info) {
            this.info = info;
        }

        public @Override String visitIdentifier(IdentifierTree tree, Void v) {
            return "..." + tree.getName().toString();
        }

        public @Override String visitMethodInvocation(MethodInvocationTree tree, Void v) {
            ExpressionTree methodSelect = tree.getMethodSelect();

            return "..." + simpleName(methodSelect) + "(...)"; // NOI18N
        }

        public @Override String visitArrayAccess(ArrayAccessTree node, Void p) {
            return "..." + simpleName(node.getExpression()) + "[]"; // NOI18N
        }

        public @Override String visitNewClass(NewClassTree nct, Void p) {
            return "...new " + simpleName(nct.getIdentifier()) + "(...)"; // NOI18N
        }

        @Override
        public String visitNewArray(NewArrayTree nct, Void p) {
            return "...new " + simpleName(nct.getType()) + "[...]"; // NOI18N
        }

        @Override
        public String visitBinary(BinaryTree node, Void p) {
            String dn = operator2DN.get(node.getKind());

            return scan(node.getLeftOperand(), p) + dn + scan(node.getRightOperand(), p);
        }

        @Override
        public String visitLiteral(LiteralTree node, Void p) {
            if (node.getValue() instanceof String)
                return "...";

            int start = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), node);
            int end   = (int) info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), node);

            if (start < 0 || end < 0 || end < start) {
                return node.toString();
            }
            
            return info.getText().substring(start, end);
        }

        private String simpleName(Tree t) {
            if (t.getKind() == Kind.IDENTIFIER) {
                return ((IdentifierTree) t).getName().toString();
            }

            if (t.getKind() == Kind.MEMBER_SELECT) {
                return ((MemberSelectTree) t).getIdentifier().toString();
            }

            if (t.getKind() == Kind.METHOD_INVOCATION) {
                return scan(t, null);
            }

            if (t.getKind() == Kind.PARAMETERIZED_TYPE) {
                return simpleName(((ParameterizedTypeTree) t).getType()) + "<...>"; // NOI18N
            }

            if (t.getKind() == Kind.ARRAY_ACCESS) {
                return simpleName(((ArrayAccessTree) t).getExpression()) + "[]"; //NOI18N
            }

            if (t.getKind() == Kind.PARENTHESIZED) {
                return "(" + simpleName(((ParenthesizedTree)t).getExpression()) + ")"; //NOI18N
            }

            if (t.getKind() == Kind.TYPE_CAST) {
                return simpleName(((TypeCastTree)t).getType());
            }

            if (t.getKind() == Kind.ARRAY_TYPE) {
                return simpleName(((ArrayTypeTree)t).getType());
            }

            if (t.getKind() == Kind.PRIMITIVE_TYPE) {
                return ((PrimitiveTypeTree) t).getPrimitiveTypeKind().name().toLowerCase();
            }
            
            throw new IllegalStateException("Currently unsupported kind of tree: " + t.getKind()); // NOI18N
        }
    }

    public static TreePath findEnclosingMethodOrConstructor(HintContext ctx, TreePath from) {
        while (from != null && from.getLeaf().getKind() != Kind.METHOD && !TreeUtilities.CLASS_TREE_KINDS.contains(from.getLeaf().getKind())) {
            from = from.getParentPath();
        }

        if (from != null && from.getLeaf().getKind() == Kind.METHOD) {
            return from;
        }

        return null;
    }

    public static boolean isInConstructor(HintContext ctx) {
        TreePath method = findEnclosingMethodOrConstructor(ctx, ctx.getPath());
        if (method == null) return false;
        Element enclosingMethodElement = ctx.getInfo().getTrees().getElement(method);
        return (enclosingMethodElement != null &&
                enclosingMethodElement.getKind() == ElementKind.CONSTRUCTOR);
    }

    public static Pair<List<? extends TypeMirror>, List<String>> resolveArguments(CompilationInfo info, TreePath invocation, List<? extends ExpressionTree> realArguments, Element target) {
        MethodArguments ma = resolveArguments(info, invocation, realArguments, target, null);
        
        if (ma == null) return null;
        
        return Pair.<List<? extends TypeMirror>, List<String>>of(ma.parameterTypes, ma.parameterNames);
    }
    
    public static MethodArguments resolveArguments(CompilationInfo info, TreePath invocation, List<? extends ExpressionTree> realArguments, Element target, TypeMirror returnType) {
        List<TypeMirror> argumentTypes = new LinkedList<TypeMirror>();
        List<String>     argumentNames = new LinkedList<String>();
        List<Element>    usedLocalTypeVariables = new ArrayList<Element>();
        Set<String>      usedArgumentNames = new HashSet<String>();
        
        TreePath enclosingMethod = invocation;
        
        while (enclosingMethod != null && !TreeUtilities.CLASS_TREE_KINDS.contains(enclosingMethod.getLeaf().getKind()) && enclosingMethod.getLeaf().getKind() != Kind.METHOD) {
            enclosingMethod = enclosingMethod.getParentPath();
        }
        
        ExecutableElement method = null;
        
        if (enclosingMethod != null && enclosingMethod.getLeaf().getKind() == Kind.METHOD) {
            Element el = info.getTrees().getElement(enclosingMethod);
            
            if (el != null && (el.getKind() == ElementKind.METHOD || el.getKind() == ElementKind.CONSTRUCTOR)) {
                method = (ExecutableElement) el;
            }
        }

        if (returnType != null) {
            if (!verifyTypeVarAccessible(method, returnType, usedLocalTypeVariables, target)) return null;
        } else {
            method = null;
        }
        
        for (ExpressionTree arg : realArguments) {
            TreePath argPath = new TreePath(invocation, arg);
            TypeMirror tm = info.getTrees().getTypeMirror(argPath);

            //anonymous class?
            tm = Utilities.convertIfAnonymous(tm);

            if (tm == null || tm.getKind() == TypeKind.NONE || containsErrorsRecursively(tm)) {
                return null;
            }
            
            tm = resolveCapturedType(info, tm);

            if (!verifyTypeVarAccessible(method, tm, usedLocalTypeVariables, target)) return null;

            if (tm.getKind() == TypeKind.NULL) {
                tm = info.getElements().getTypeElement("java.lang.Object").asType(); // NOI18N
            }

            argumentTypes.add(tm);

            String proposedName = null;
            Element elem = info.getTrees().getElement(argPath);

            if (elem != null && elem.getKind() == ElementKind.ENUM_CONSTANT) {
                proposedName = firstToLower(elem.getEnclosingElement().getSimpleName().toString());
            }

            if (proposedName == null) {
                proposedName = org.netbeans.modules.java.hints.errors.Utilities.getName(arg);
            }

            if (proposedName == null) {
                proposedName = org.netbeans.modules.java.hints.errors.Utilities.getName(tm);
            }

            if (proposedName == null) {
                proposedName = "arg"; // NOI18N
            }

            if (usedArgumentNames.contains(proposedName)) {
                int num = 0;

                while (usedArgumentNames.contains(proposedName + num)) {
                    num++;
                }

                proposedName = proposedName + num;
            }

            argumentNames.add(proposedName);
            usedArgumentNames.add(proposedName);
        }
        
        List<TypeMirror> typeParamTypes = new LinkedList<TypeMirror>();
        List<String>     typeParamNames = new LinkedList<String>();
        
        if (method != null) {
            for (TypeParameterElement methodTP : method.getTypeParameters()) {
                if (!usedLocalTypeVariables.contains(methodTP)) continue;

                typeParamTypes.add(methodTP.asType());
                typeParamNames.add(methodTP.getSimpleName().toString());
            }
        }

        return new MethodArguments(argumentTypes, argumentNames, typeParamTypes, typeParamNames);
    }
    
    public static class MethodArguments {
        public final List<? extends TypeMirror> parameterTypes;
        public final List<String> parameterNames;
        public final List<? extends TypeMirror> typeParameterTypes;
        public final List<String> typeParameterNames;
        public MethodArguments(List<? extends TypeMirror> parameterTypes, List<String> parameterNames, List<? extends TypeMirror> typeParameterTypes, List<String> typeParameterNames) {
            this.parameterTypes = parameterTypes;
            this.parameterNames = parameterNames;
            this.typeParameterTypes = typeParameterTypes;
            this.typeParameterNames = typeParameterNames;
        }
    }

    private static boolean verifyTypeVarAccessible(ExecutableElement method, TypeMirror forType, List<Element> usedLocalTypeVariables, Element target) {
        Collection<TypeVariable> typeVars = Utilities.containedTypevarsRecursively(forType);
        
        if (method != null) {
            for (Iterator<TypeVariable> it = typeVars.iterator(); it.hasNext(); ) {
                TypeVariable tvar = it.next();
                Element tvarEl = tvar.asElement();

                if (method.getTypeParameters().contains(tvarEl)) {
                    usedLocalTypeVariables.add(tvarEl);
                    it.remove();
                }
            }
        }
        
        return allTypeVarsAccessible(typeVars, target);
    }

    //XXX: currently we cannot fix:
    //xxx = new ArrayList<Unknown>();
    //=>
    //ArrayList<Unknown> xxx;
    //xxx = new ArrayList<Unknown>();
    public static boolean containsErrorsRecursively(TypeMirror tm) {
        switch (tm.getKind()) {
            case ERROR:
                return true;
            case DECLARED:
                DeclaredType type = (DeclaredType) tm;

                for (TypeMirror t : type.getTypeArguments()) {
                    if (containsErrorsRecursively(t))
                        return true;
                }

                return false;
            case ARRAY:
                return containsErrorsRecursively(((ArrayType) tm).getComponentType());
            case WILDCARD:
                if (((WildcardType) tm).getExtendsBound() != null && containsErrorsRecursively(((WildcardType) tm).getExtendsBound())) {
                    return true;
                }
                if (((WildcardType) tm).getSuperBound() != null && containsErrorsRecursively(((WildcardType) tm).getSuperBound())) {
                    return true;
                }
                return false;
            case OTHER:
                return true;
            default:
                return false;
        }
    }

    public static boolean exitsFromAllBranchers(CompilationInfo info, TreePath from) {
        ExitsFromAllBranches efab = new ExitsFromAllBranches(info);

        return efab.scan(from, null) == Boolean.TRUE;
    }

    private static final class ExitsFromAllBranches extends TreePathScanner<Boolean, Void> {

        private CompilationInfo info;
        private final Set<Tree> seenTrees = new HashSet<Tree>();
        private final Stack<Set<TypeMirror>> caughtExceptions = new Stack<Set<TypeMirror>>();

        public ExitsFromAllBranches(CompilationInfo info) {
            this.info = info;
        }

        @Override
        public Boolean scan(Tree tree, Void p) {
            seenTrees.add(tree);
            return super.scan(tree, p);
        }

        @Override
        public Boolean visitIf(IfTree node, Void p) {
            return scan(node.getThenStatement(), null) == Boolean.TRUE && scan(node.getElseStatement(), null) == Boolean.TRUE;
        }

        @Override
        public Boolean visitReturn(ReturnTree node, Void p) {
            return true;
        }

        @Override
        public Boolean visitBreak(BreakTree node, Void p) {
            return !seenTrees.contains(info.getTreeUtilities().getBreakContinueTarget(getCurrentPath()));
        }

        @Override
        public Boolean visitContinue(ContinueTree node, Void p) {
            return !seenTrees.contains(info.getTreeUtilities().getBreakContinueTarget(getCurrentPath()));
        }

        @Override
        public Boolean visitClass(ClassTree node, Void p) {
            return false;
        }

        @Override
        public Boolean visitTry(TryTree node, Void p) {
            Set<TypeMirror> caught = new HashSet<TypeMirror>();

            for (CatchTree ct : node.getCatches()) {
                TypeMirror t = info.getTrees().getTypeMirror(new TreePath(new TreePath(getCurrentPath(), ct), ct.getParameter()));

                if (t != null) {
                    caught.add(t);
                }
            }

            caughtExceptions.push(caught);
            
            try {
                return scan(node.getBlock(), p) == Boolean.TRUE || scan(node.getFinallyBlock(), p) == Boolean.TRUE;
            } finally {
                caughtExceptions.pop();
            }
        }

        @Override
        public Boolean visitThrow(ThrowTree node, Void p) {
            TypeMirror type = info.getTrees().getTypeMirror(new TreePath(getCurrentPath(), node.getExpression()));
            boolean isCaught = false;

            OUTER: for (Set<TypeMirror> caught : caughtExceptions) {
                for (TypeMirror c : caught) {
                    if (info.getTypes().isSubtype(type, c)) {
                        isCaught = true;
                        break OUTER;
                    }
                }
            }

            return super.visitThrow(node, p) == Boolean.TRUE || !isCaught;
        }

    }

    public static @NonNull Collection<TypeVariable> containedTypevarsRecursively(@NullAllowed TypeMirror tm) {
        if (tm == null) {
            return Collections.emptyList();
        }

        Collection<TypeVariable> typeVars = new LinkedList<TypeVariable>();

        containedTypevarsRecursively(tm, typeVars);

        return typeVars;
    }

    private static void containedTypevarsRecursively(@NonNull TypeMirror tm, @NonNull Collection<TypeVariable> typeVars) {
        switch (tm.getKind()) {
            case TYPEVAR:
                typeVars.add((TypeVariable) tm);
                break;
            case DECLARED:
                DeclaredType type = (DeclaredType) tm;
                for (TypeMirror t : type.getTypeArguments()) {
                    containedTypevarsRecursively(t, typeVars);
                }

                break;
            case ARRAY:
                containedTypevarsRecursively(((ArrayType) tm).getComponentType(), typeVars);
                break;
            case WILDCARD:
                if (((WildcardType) tm).getExtendsBound() != null) {
                    containedTypevarsRecursively(((WildcardType) tm).getExtendsBound(), typeVars);
                }
                if (((WildcardType) tm).getSuperBound() != null) {
                    containedTypevarsRecursively(((WildcardType) tm).getSuperBound(), typeVars);
                }
                break;
        }
    }

    public static boolean allTypeVarsAccessible(Collection<TypeVariable> typeVars, Element target) {
        if (target == null) {
            return typeVars.isEmpty();
        }
        
        Set<TypeVariable> targetTypeVars = new HashSet<TypeVariable>();

        OUTER: while (target.getKind() != ElementKind.PACKAGE) {
            Iterable<? extends TypeParameterElement> tpes;

            switch (target.getKind()) {
                case ANNOTATION_TYPE:
                case CLASS:
                case ENUM:
                case INTERFACE:
                    tpes = ((TypeElement) target).getTypeParameters();
                    break;
                case METHOD:
                case CONSTRUCTOR:
                    tpes = ((ExecutableElement) target).getTypeParameters();
                    break;
                default:
                    break OUTER;
            }

            for (TypeParameterElement tpe : tpes) {
                targetTypeVars.add((TypeVariable) tpe.asType());
            }

            if (target.getModifiers().contains(Modifier.STATIC)) {
                break;
            }

            target = target.getEnclosingElement();
        }

        return targetTypeVars.containsAll(typeVars);
    }

    public static String target2String(TypeElement target) {
        final Name qualifiedName = target.getQualifiedName(); //#130759
        if (qualifiedName == null) {
            Logger.getLogger(Utilities.class.getName()).warning("Target qualified name could not be resolved."); //NOI18N
            return ""; //NOI18N
        } else {
            String qnString = qualifiedName.toString();
            if (qnString.length() == 0) {
                //probably an anonymous class
                qnString = target.asType().toString();
            }

            try {
                qnString = XMLUtil.toElementContent(qnString);
            } catch (CharConversionException ex) {
                Logger.getLogger(Utilities.class.getName()).log(Level.FINE, null, ex);
            }

            return qnString;
        }
    }

    public static Visibility effectiveVisibility(TreePath tp) {
        Visibility result = null;

        while (tp != null) {
            Visibility current = Visibility.forTree(tp.getLeaf());

            if (current != null) {
                if (result != null) result = result.enclosedBy(current);
                else result = current;
            }
            
            tp = tp.getParentPath();
        }

        return result;
    }

    public enum Visibility {
        PRIVATE(EnumSet.of(Modifier.PRIVATE)),
        PACKAGE_PRIVATE(EnumSet.noneOf(Modifier.class)),
        PROTECTED(EnumSet.of(Modifier.PROTECTED)),
        PUBLIC(EnumSet.of(Modifier.PUBLIC));
        private final Set<Modifier> modifiers;
        private Visibility(Set<Modifier> modifiers) {
            this.modifiers = modifiers;
        }
        public Visibility enclosedBy(Visibility encl) {
            return Visibility.values()[Math.min(ordinal(), encl.ordinal())];
        }
        public Set<Modifier> getRequiredModifiers() {
            return modifiers;
        }
        public static Visibility forModifiers(ModifiersTree mt) {
            if (mt.getFlags().contains(Modifier.PUBLIC)) return PUBLIC;
            if (mt.getFlags().contains(Modifier.PROTECTED)) return PROTECTED;
            if (mt.getFlags().contains(Modifier.PRIVATE)) return PRIVATE;
            return PACKAGE_PRIVATE;
        }
        public static Visibility forElement(Element el) {
            if (el.getModifiers().contains(Modifier.PUBLIC)) return PUBLIC;
            if (el.getModifiers().contains(Modifier.PROTECTED)) return PROTECTED;
            if (el.getModifiers().contains(Modifier.PRIVATE)) return PRIVATE;
            return PACKAGE_PRIVATE;
        }
        public static Visibility forTree(Tree t) {
            switch (t.getKind()) {
                case ANNOTATION_TYPE:
                case CLASS:
                case ENUM:
                case INTERFACE: return forModifiers(((ClassTree) t).getModifiers());
                case VARIABLE: return forModifiers(((VariableTree) t).getModifiers());
                case METHOD: return forModifiers(((MethodTree) t).getModifiers());
                default: return null;
            }
        }
    }

    /**
     * Detects if targets file is non-null and writable
     * @return true if target's file is writable
     */
    public static boolean isTargetWritable(@NonNull TypeElement target, @NonNull CompilationInfo info) {
        TypeElement outermostType = info.getElementUtilities().outermostTypeElement(target);
        FileObject fo = SourceUtils.getFile(ElementHandle.create(outermostType), info.getClasspathInfo());
	if(fo != null && fo.canWrite())
	    return true;
	else
	    return false;
    }


    public static Visibility getAccessModifiers(@NonNull CompilationInfo info, @NullAllowed TypeElement source, @NonNull TypeElement target) {
        if (target.getKind().isInterface()) {
            return Visibility.PUBLIC;
        }

        TypeElement outterMostSource = source != null ? info.getElementUtilities().outermostTypeElement(source) : null;
        TypeElement outterMostTarget = info.getElementUtilities().outermostTypeElement(target);

        if (outterMostTarget.equals(outterMostSource)) {
            return Visibility.PRIVATE;
        }

        Element sourcePackage;

        if (outterMostSource != null) {
            sourcePackage = outterMostSource.getEnclosingElement();
        } else if (info.getCompilationUnit().getPackageName() != null) {
            sourcePackage = info.getTrees().getElement(new TreePath(new TreePath(info.getCompilationUnit()), info.getCompilationUnit().getPackageName()));
        } else {
            sourcePackage = info.getElements().getPackageElement("");
        }

        Element targetPackage = outterMostTarget.getEnclosingElement();

        if (sourcePackage.equals(targetPackage)) {
            return Visibility.PACKAGE_PRIVATE;
        }

        //TODO: protected?
        return Visibility.PUBLIC;
    }
    
    public static boolean isVariableShadowedInScope(CharSequence variableName, Scope localScope) {
        if (localScope == null) {
            return false;
        }

        if (areAnyLocalVariablesShadowed(variableName, localScope)) {
            return true;
        }

        if (areEnclosingMethodParamsShadowed(variableName, localScope)) {
            return true;
        }

        return false;
    }

    private static boolean areAnyLocalVariablesShadowed(CharSequence variableName, Scope localScope) {
        for (Element e : localScope.getLocalElements()) {
            if (e.getKind() == ElementKind.LOCAL_VARIABLE
                    && e.getSimpleName().contentEquals(variableName)) {
                return true;
            }
        }
        return false;
    }

    private static boolean areEnclosingMethodParamsShadowed(CharSequence variableName, Scope localScope) {
        if (localScope.getEnclosingMethod() != null) {
            ExecutableElement enclMethod = localScope.getEnclosingMethod();
            for (VariableElement varElement : enclMethod.getParameters()) {
                if (varElement.getSimpleName().contentEquals(variableName)) {
                    return true;
                }
            }
        }
        return false;
    }
}
