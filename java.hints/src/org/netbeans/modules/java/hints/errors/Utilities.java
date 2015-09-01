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
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ForLoopTree;
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
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.SynchronizedTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.util.SourcePositions;
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
import javax.lang.model.type.IntersectionType;
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
import com.sun.tools.javac.api.JavacScope;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.comp.Attr;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Enter;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.JCDiagnostic;
import com.sun.tools.javac.util.Log;
import java.net.URI;
import javax.lang.model.type.UnionType;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import org.netbeans.api.java.source.CodeStyle;
import org.netbeans.api.java.source.CodeStyleUtils;
import org.netbeans.modules.java.source.JavaSourceAccessor;
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
        return guessName(info, org.netbeans.modules.editor.java.Utilities.varNameSuggestion(tp.getLeaf()), scope, prefix, suffix, false);
    }
    
    public static String guessName(CompilationInfo info, String name, TreePath scope, String prefix, String suffix, boolean acceptExistingPrefixes) {
        
        if (name == null) {
            return DEFAULT_NAME;
        }
        
        Scope s = info.getTrees().getScope(scope);
        
        return makeNameUnique(info, s, name, Collections.<String>emptySet(), prefix, suffix, acceptExistingPrefixes);
    }
    
    private static final Map<String, String> TYPICAL_KEYWORD_CONVERSIONS = new HashMap<String, String>() {{
        put("class", "clazz");
        put("interface", "intf");
        put("new", "nue");
        put("static", "statik");
    }};
    
    public static String makeNameUnique(CompilationInfo info, Scope s, String name, String prefix, String suffix) {
        return makeNameUnique(info, s, name, Collections.<String>emptySet(), prefix, suffix);
    }
    
    public static String makeNameUnique(CompilationInfo info, Scope s, String name, Set<String> usedVariables, String prefix, String suffix) {
        return makeNameUnique(info, s, name, usedVariables, prefix, suffix, false);
    }
    
    /**
     * Creates an unique name.
     * The method takes the proposed `name' and ensures it is unique with respect to `usedVariables' and contents of the target scope `s'.
     * The `prefix' and `suffix' are joined with the base name. If prefix ends with a letter and name starts with letter, the resulting name
     * will be converted to CamelCase according to coding conventions. If `acceptExisting' is true, the name will not be decorated, if it
     * already contains both prefix AND suffix. Names that are the same as keywords are avoided and changed.
     * 
     * @param info compilation info
     * @param s target scope for uniqueness checks
     * @param name proposed base name
     * @param usedVariables other to-be-introduced names, in addition to scope contents, to be avoided
     * @param prefix the desired prefix or {@code null}
     * @param suffix the desired suffix or {@code null}
     * @param acceptExisting true, if existing prefix and suffix in the `name' should be accepted
     * @return unique name that contains the prefix and suffix if they are specified.
     */
    public static String makeNameUnique(CompilationInfo info, Scope s, String name, Set<String> usedVariables, String prefix, String suffix, boolean acceptExisting) {
        boolean prefixOK = false;
        boolean suffixOK = false;
        
        if (acceptExisting) {
            if (prefix != null) {
                if (!(prefixOK = prefix.isEmpty())) {
                    // prefixOK is now false
                    if (name.startsWith(prefix)) {
                        int pl = prefix.length();
                        if(Character.isAlphabetic(prefix.charAt(pl-1))) {
                            if (name.length() > pl && Character.isUpperCase(name.charAt(pl))) {
                                prefixOK = true;
                            }
                        } else {
                            prefixOK = true;
                        }
                    }
                }
            }
            if (suffix != null && (suffix.isEmpty() || name.endsWith(suffix))) {
                suffixOK = true;
            }
        }
        if (prefixOK && suffixOK) {
            prefix = suffix = ""; // NOI18N
        }
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
            }
            
            if (SourceVersion.isKeyword(proposedName) || usedVariables.contains(proposedName)) {
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
                        final Position start = d.getStartPosition();
                        Document doc = d.openDocument();

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
        if (tm == null) {
            return tm;
        }
        TypeMirror type = resolveCapturedTypeInt(info, tm);
        if (type == null) {
            return tm;
        }
        if (type.getKind() == TypeKind.WILDCARD) {
            TypeMirror tmirr = ((WildcardType) type).getExtendsBound();
            if (tmirr != null)
                return tmirr;
            else { //no extends, just '?'
                TypeElement te = info.getElements().getTypeElement("java.lang.Object"); // NOI18N
                return te == null ? null : te.asType();
            }
                
        }
        
        return type;
    }
    
    /**
     * Note: may return {@code null}, if an intersection type is encountered, to indicate a 
     * real type cannot be created.
     */
    private static TypeMirror resolveCapturedTypeInt(CompilationInfo info, TypeMirror tm) {
        if (tm == null) return tm;
        
        TypeMirror orig = SourceUtils.resolveCapturedType(tm);

        if (orig != null) {
            tm = orig;
        }
        
        if (tm.getKind() == TypeKind.WILDCARD) {
            TypeMirror extendsBound = ((WildcardType) tm).getExtendsBound();
            TypeMirror superBound = ((WildcardType) tm).getSuperBound();
            if (extendsBound != null || superBound != null) {
                TypeMirror rct = resolveCapturedTypeInt(info, extendsBound != null ? extendsBound : superBound);
                if (rct != null) {
                    switch (rct.getKind()) {
                        case WILDCARD:
                            return rct;
                        case ARRAY:
                        case DECLARED:
                        case ERROR:
                        case TYPEVAR:
                        case OTHER:
                            return info.getTypes().getWildcardType(
                                    extendsBound != null ? rct : null, superBound != null ? rct : null);
                    }
                } else {
                    // propagate failure out of all wildcards
                    return null;
                }
            }
        } else if (tm.getKind() == TypeKind.INTERSECTION) {
            return null;
        }
        
        if (tm.getKind() == TypeKind.DECLARED) {
            DeclaredType dt = (DeclaredType) tm;
            List<TypeMirror> typeArguments = new LinkedList<TypeMirror>();
            
            for (TypeMirror t : dt.getTypeArguments()) {
                TypeMirror targ = resolveCapturedTypeInt(info, t);
                if (targ == null) {
                    // bail out, if the type parameter is a wildcard, it's probably not possible
                    // to create a proper parametrized type from it
                    if (t.getKind() == TypeKind.WILDCARD || t.getKind() == TypeKind.INTERSECTION) {
                        return null;
                    }
                    // use rawtype
                    typeArguments.clear();
                    break;
                }
                typeArguments.add(targ);
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
            TypeMirror tm2 = resolveCapturedTypeInt(info, at.getComponentType());
            return info.getTypes().getArrayType(tm2 != null ? tm2 : tm);
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
    
    /**
     * Finds the owner method, constructor, optionally initializer or lambda.
     * The behaviour depends on the 'lambdaOrInitializer' parameter; if false,
     * it will find the immediate owning method or constructor. If the code is
     * NOT directly nested in a method or constructor (i.e. in a lambda, initializer),
     * null is returned.
     * <p/>
     * If the parameter is true, the method is also able to return Lambda expression
     * tree or initializer tree.
     * <p/>
     * At any rate, the search stops at class boundaries.
     * 
     * @param ctx the context
     * @param from pat to start search from (upwards)
     * @param lambdaOrInitializer also return lambdas or initializers if true.
     * @return the direct owning executable block or {@code null} if the owning exec
     * block does not satisfy the filter.
     */
    @SuppressWarnings({"AssignmentToMethodParameter", "NestedAssignment"})
    public static TreePath findOwningExecutable(HintContext ctx, TreePath from, boolean lambdaOrInitializer) {
        Tree.Kind k = null;
        
        OUTER: while (from != null && !(TreeUtilities.CLASS_TREE_KINDS.contains(k = from.getLeaf().getKind()))) {
            switch (k) {
                case METHOD:
                    break OUTER;
                case LAMBDA_EXPRESSION:
                    return lambdaOrInitializer ? from : null;
                case BLOCK: {
                    TreePath par = from.getParentPath();
                    Tree l = par.getLeaf();
                    if (TreeUtilities.CLASS_TREE_KINDS.contains(l.getKind())) {
                        return lambdaOrInitializer ? from : null;
                    }
                }
            }
            from = from.getParentPath();
        }
        return (from == null || k != Kind.METHOD) ?
                null : from;
    }

    /**
     * Finds the top-level block or expression that contains the 'from' path.
     * The result could be a 
     * <ul>
     * <li>BlockTree representing method body
     * <li>ExpressionTree representing field initializer
     * <li>BlockTree representing class initializer
     * <li>ExpressionTree representing lambda expression
     * <li>BlockTree representing lambda expression
     * </ul>
     * @param from start from 
     * @return nearest enclosing top-level block/expression as defined above.
     */
    public static TreePath findTopLevelBlock(TreePath from) {
        if (from.getLeaf().getKind() == Tree.Kind.COMPILATION_UNIT) {
            return null;
        }
        TreePath save = null;
        
        while (from != null) {
            Tree.Kind k = from.getParentPath().getLeaf().getKind();
            if (k == Kind.METHOD || k == Kind.LAMBDA_EXPRESSION) {
                return from;
            } else if (k == Kind.VARIABLE) {
                save = from;
            } else if (TreeUtilities.CLASS_TREE_KINDS.contains(k)) {
                if (save != null) {
                    // variable initializer from the previous iteration
                    return save;
                }
                if (from.getLeaf().getKind() == Kind.BLOCK) {
                    // parent is class, from is block -> initializer
                    return from;
                }
                return null;
            } else {
                save = null;
            }
            from = from.getParentPath();
        }
        return null;
    }

    public static boolean isInConstructor(HintContext ctx) {
        TreePath method = findOwningExecutable(ctx, ctx.getPath(), false);
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
        
        CodeStyle codeStyle = CodeStyle.getDefault(info.getFileObject());
        
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
                if (tm == null) {
                    return null;
                }
            }

            argumentTypes.add(tm);

            String proposedName = null;
            Element elem = info.getTrees().getElement(argPath);

            if (elem != null && elem.getKind() == ElementKind.ENUM_CONSTANT) {
                proposedName = firstToLower(elem.getEnclosingElement().getSimpleName().toString());
            }

            if (proposedName == null) {
                proposedName = org.netbeans.modules.editor.java.Utilities.varNameSuggestion(arg);
            }

            if (proposedName == null) {
                proposedName = org.netbeans.modules.java.hints.errors.Utilities.getName(tm);
            }

            if (proposedName == null) {
                proposedName = "arg"; // NOI18N
            }
            
            String augmentedName = CodeStyleUtils.addPrefixSuffix(proposedName, codeStyle.getParameterNamePrefix(), codeStyle.getParameterNameSuffix());

            if (usedArgumentNames.contains(augmentedName)) {
                int num = 0;

                while (usedArgumentNames.contains(augmentedName = CodeStyleUtils.addPrefixSuffix(proposedName + num, codeStyle.getParameterNamePrefix(), codeStyle.getParameterNameSuffix()))) {
                    num++;
                }
            }

            argumentNames.add(augmentedName);
            usedArgumentNames.add(augmentedName);
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
        public Boolean scan(TreePath path, Void p) {
            seenTrees.add(path.getLeaf());
            return super.scan(path, p);
        }

        @Override
        public Boolean scan(Tree tree, Void p) {
            seenTrees.add(tree);
            return super.scan(tree, p);
        }

        /**
         * Hardcoded check for System.exit(), Runtime.exit and Runtime.halt which also terminates the processing. 
         * TODO: some configuration (project-level ?) could add also different exit methods.
         */
        @Override
        public Boolean visitMethodInvocation(MethodInvocationTree node, Void p) {
            Element el = info.getTrees().getElement(getCurrentPath());
            if (isSystemExit(info, el)) {
                return true;
            }
            return super.visitMethodInvocation(node, p);
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
    
    public static boolean isValidElement(Element e) {
        return e != null && isValidType(e.asType());
    }
    
    public static boolean isValidType(TypeMirror m) {
        return m != null && (m.getKind() != TypeKind.OTHER && m.getKind() != TypeKind.ERROR);
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

        if (sourcePackage != null && sourcePackage.equals(targetPackage)) {
            return Visibility.PACKAGE_PRIVATE;
        }

        //TODO: protected?
        return Visibility.PUBLIC;
    }
    
    private static final EnumSet VARIABLE_KINDS = EnumSet.of(
            ElementKind.LOCAL_VARIABLE, ElementKind.ENUM_CONSTANT, ElementKind.FIELD, ElementKind.PARAMETER,
            ElementKind.RESOURCE_VARIABLE, ElementKind.EXCEPTION_PARAMETER, ElementKind.TYPE_PARAMETER);
    
    public static boolean isSymbolUsed(CompilationInfo info, TreePath target, CharSequence variableName, Scope localScope) {
        SourcePositions[] pos = new SourcePositions[1];
        Tree t = info.getTreeUtilities().parseExpression(variableName.toString(), pos);
        TypeMirror tm = info.getTreeUtilities().attributeTree(t, localScope);
        Element el = info.getTrees().getElement(new TreePath(target, t));
        if (el == null) {
            return false;
        }
        ElementKind k = el.getKind();
        return VARIABLE_KINDS.contains(k);
    }

    /**
     * Determines if the element corresponds to never-returning, terminating method.
     * System.exit, Runtime.exit, Runtime.halt are checked. The passed element is
     * usually a result of {@code CompilationInfo.getTrees().getElement(path)}.
     * 
     * @param info context
     * @param e element to check
     * @return true, if the element corrresponds to a VM-exiting method
     */
    public static boolean isSystemExit(CompilationInfo info, Element e) {
        if (e == null || e.getKind() != ElementKind.METHOD) {
            return false;
        }
        ExecutableElement ee = (ExecutableElement)e;
        Name n = ee.getSimpleName();
        if (n.contentEquals("exit") || n.contentEquals("halt")) { // NOI18N
            TypeElement tel = info.getElementUtilities().enclosingTypeElement(e);
            if (tel == null) {
                return false;
            }
            Name ofqn = tel.getQualifiedName();
            if (ofqn.contentEquals("java.lang.System") || ofqn.contentEquals("java.lang.Runtime")) { // NOI18N
                return true;
            }
        }
        return false;
    }

    /**
     * Helper that retrieves all caught exception from a conventional catch
     * clause or from catch clause that contain alternatives. Empty collection
     * is returned in the case of an error.
     * 
     * @param ct catch clause
     * @return exception list, never null.
     */
    public static List<? extends TypeMirror> getUnionExceptions(CompilationInfo info, TreePath cP, CatchTree ct) {
        if (ct.getParameter() == null) {
            return Collections.emptyList();
        }
        TypeMirror exT = info.getTrees().getTypeMirror(new TreePath(cP, ct.getParameter()));
        return getCaughtExceptions(exT);
    }

    private static List<? extends TypeMirror> getCaughtExceptions(TypeMirror caught) {
        if (caught == null) {
            return Collections.emptyList();
        }
        switch (caught.getKind()) {
            case UNION: {
                boolean cloned = false;
                List<? extends TypeMirror> types = ((UnionType) caught).getAlternatives();
                int i = types.size() - 1;
                for (; i >= 0; i--) {
                    TypeMirror m = types.get(i);
                    TypeKind mk = m.getKind();
                    if (mk == null || mk != TypeKind.DECLARED) {
                        if (!cloned) {
                            types = new ArrayList<TypeMirror>(types);
                        }
                        types.remove(i);
                    }
                }
                
                return types;
            }
            case DECLARED:
                return Collections.singletonList(caught);
            default:
                return Collections.emptyList();
        }
    }

    private static final Set<String> PRIMITIVE_NAMES = new HashSet<String>(7);
    
    static {
        PRIMITIVE_NAMES.add("java.lang.Integer"); // NOI18N
        PRIMITIVE_NAMES.add("java.lang.Character"); // NOI18N
        PRIMITIVE_NAMES.add("java.lang.Long"); // NOI18N
        PRIMITIVE_NAMES.add("java.lang.Byte"); // NOI18N
        PRIMITIVE_NAMES.add("java.lang.Short"); // NOI18N
        PRIMITIVE_NAMES.add("java.lang.Boolean"); // NOI18N
        PRIMITIVE_NAMES.add("java.lang.Float"); // NOI18N
        PRIMITIVE_NAMES.add("java.lang.Double"); // NOI18N
    }

    public static TypeKind getPrimitiveKind(CompilationInfo ci, TypeMirror tm) {
        if (tm == null) {
            return null;
        }
        if (tm.getKind().isPrimitive()) {
            return tm.getKind();
        } else if (isPrimitiveWrapperType(tm)) {
            return ci.getTypes().unboxedType(tm).getKind();
        } 
        return null;
    }
    
    public static TypeMirror unboxIfNecessary(CompilationInfo ci, TypeMirror tm) {
        if (isPrimitiveWrapperType(tm)) {
            return ci.getTypes().unboxedType(tm);
        } else {
            return tm;
        }
    }
    
    public static boolean isPrimitiveWrapperType(TypeMirror tm) {
        if (tm == null || tm.getKind() != TypeKind.DECLARED) { 
            return false;
        }
        Element el = ((DeclaredType)tm).asElement();
        if (el == null || el.getKind() != ElementKind.CLASS) {
            return false;
        }
        String s = ((TypeElement)el).getQualifiedName().toString();
        return PRIMITIVE_NAMES.contains(s); // NOI18N
    }
    
    /**
     * Attempts to resolve a method or a constructor call with an altered argument tree.
     * 
     * @param ci the context
     * @param invPath path to the method invocation node
     * @param origPath path to the Tree within method's arguments which should be replaced
     * @param valPath the replacement tree
     * @return 
     */
    public static boolean checkAlternativeInvocation(CompilationInfo ci, TreePath invPath, 
            TreePath origPath,
            TreePath valPath, String customPrefix) {
        Tree l = invPath.getLeaf();
        Tree sel;
        
        if (l.getKind() == Tree.Kind.NEW_CLASS) {
            NewClassTree nct = (NewClassTree)invPath.getLeaf();
            sel = nct.getIdentifier();
        } else if (l.getKind() == Tree.Kind.METHOD_INVOCATION) {
            MethodInvocationTree mit = (MethodInvocationTree)invPath.getLeaf();
            sel = mit.getMethodSelect();
        } else {
            return false;
        }
        
        return resolveAlternativeInvocation(ci, invPath, 
                origPath, sel, valPath, customPrefix);
    }
    
    private static Tree getInvocationIdentifier(Tree inv) {
        if (inv.getKind() == Tree.Kind.METHOD_INVOCATION) {
            return ((MethodInvocationTree)inv).getMethodSelect();
        } else if (inv.getKind() == Tree.Kind.NEW_CLASS) {
            return ((NewClassTree)inv).getIdentifier();
        } else {
            return null;
        }
    }
    
    // -------------------------------------------------------------------------------------
    // To be moved to java.source.base TreeUtilities
    private static final class DummyJFO extends SimpleJavaFileObject {
        private DummyJFO() {
            super(URI.create("dummy.java"), JavaFileObject.Kind.SOURCE); // NOI18N
        }
        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            return "";
        }
    };

    private static TypeMirror attributeTree(JavacTaskImpl jti, Tree tree, Scope scope, 
            final List<Diagnostic<? extends JavaFileObject>> errors, @NullAllowed final Diagnostic.Kind filter) {
        Log log = Log.instance(jti.getContext());
        JavaFileObject prev = log.useSource(new DummyJFO());
        Enter enter = Enter.instance(jti.getContext());
        
        Log.DiagnosticHandler discardHandler = new Log.DiscardDiagnosticHandler(log) {
            private Diagnostic.Kind f = filter == null ? Diagnostic.Kind.ERROR : filter;
            @Override
            public void report(JCDiagnostic diag) {
                if (diag.getKind().compareTo(f) >= 0) {
                    errors.add(diag);
                }
            }            
        };
        try {
            enter.shadowTypeEnvs(true);
            Attr attr = Attr.instance(jti.getContext());
            Env<AttrContext> env = ((JavacScope) scope).getEnv();
            if (tree instanceof JCTree.JCExpression) {
                return attr.attribExpr((JCTree) tree,env, Type.noType);
            }
            return attr.attribStat((JCTree) tree,env);
        } finally {
            log.useSource(prev);
            log.popDiagnosticHandler(discardHandler);
            enter.shadowTypeEnvs(false);
        }
    }
    // -------------------------------------------------------------------------------------

    private static boolean resolveAlternativeInvocation(
            CompilationInfo ci, TreePath invPath, 
            TreePath origPath,
            Tree sel, TreePath valPath, String customPrefix) {
        CharSequence source = ci.getSnapshot().getText();
        Element e = ci.getTrees().getElement(invPath);
        if (!(e instanceof ExecutableElement)) {
            return false;
        }
        SourcePositions sp = ci.getTrees().getSourcePositions();
        
        int invOffset = (int)sp.getEndPosition(ci.getCompilationUnit(), sel) - 1;
        int origExpStart = (int)sp.getStartPosition(ci.getCompilationUnit(), 
                origPath.getLeaf());
        int origExpEnd = (int)sp.getEndPosition(ci.getCompilationUnit(), 
                origPath.getLeaf());
        
        if (invOffset < 0 || origExpStart < 0 || origExpEnd < 0) {
            return false;
        }
        TreePath exp = invPath;
        boolean statement = false;
        
        // try to minimize the parsed content: find the nearest expression that breaks the type inference,
        // typically break if the method is contained within a condition of a switch/if/loop.
        out: do {
            boolean breakPrev = false;
            TreePath previousPath = exp;
            Tree previous = exp.getLeaf();
            exp = exp.getParentPath();
            Tree t = exp.getLeaf();
            Class c = t.getKind().asInterface();
            if (c == CompoundAssignmentTree.class ||
                c == AssignmentTree.class) {
                break;
            }
            switch (t.getKind()) {
                case VARIABLE: {
                    // the declaration is omitted so that the name will not clash
                    // with existing names in the source
                    // PENDING: what if tested expression is the initializer and
                    // invalid typecast makes the value not assignable to the variable ?
                    VariableTree vt = (VariableTree)t;
                    if (vt.getInitializer() == previous) {
                        breakPrev = true;
                    }
                    break;
                }
                case CONDITIONAL_EXPRESSION: {
                    // if the tree is the condition part, then we're done and the result is a boolean.
                    ConditionalExpressionTree ctree = (ConditionalExpressionTree)t;
                    if (ctree.getCondition() == previous) {
                        breakPrev = true;
                    }
                    break;
                }
                case DO_WHILE_LOOP: {
                    DoWhileLoopTree dlp = (DoWhileLoopTree)t;
                    if (dlp.getCondition() == previous) {
                        breakPrev = true;
                    }
                    break;
                }
                case FOR_LOOP: {
                    ForLoopTree flp =(ForLoopTree)t;
                    if (previous == flp.getCondition()) {
                        breakPrev = true;
                    }
                    break;
                }
                    
                case ENHANCED_FOR_LOOP: {
                    EnhancedForLoopTree eflp = (EnhancedForLoopTree)t;
                    if (previous == eflp.getExpression()) {
                        breakPrev = true;
                    }
                    break;
                }
                case SWITCH: {
                    SwitchTree st = (SwitchTree)t;
                    if (previous == st.getExpression()) {
                        breakPrev = true;
                    }
                    break;
                }
                case SYNCHRONIZED: {
                    SynchronizedTree st = (SynchronizedTree)t;
                    if (previous == st.getExpression()) {
                        breakPrev = true;
                    }
                    break;
                }
                case WHILE_LOOP: {
                    WhileLoopTree wlt = (WhileLoopTree)t;
                    if (previous == wlt.getCondition()) {
                        breakPrev = true;
                    }
                    break;
                }
                case IF: {
                    IfTree it = (IfTree)t;
                    if (previous == it.getCondition()) {
                        breakPrev = true;
                    }
                    break;
                }
            }
            if (breakPrev) {
                exp = previousPath;
                break;
            }
            if (StatementTree.class.isAssignableFrom(c)) {
                statement = true;
                break;
            }
        } while (exp.getParentPath()!= null);
        TreePath stPath = exp;
        if (!statement) {
            while (stPath != null && !(stPath.getLeaf() instanceof StatementTree)) {
                stPath = stPath.getParentPath();
            }
        }
        if (stPath == null) {
            return false;
        }

        int baseIndex = (int)sp.getStartPosition(ci.getCompilationUnit(), exp.getLeaf());
        if (baseIndex < 0) {
            return false;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(source.subSequence(baseIndex, origExpStart));
        // instead of the boxing expression, append only the value expression, in parenthesis
        sb.append("("); // NOI18N
        if (customPrefix != null) {
            sb.append(customPrefix);
        }
        int valStart = (int)sp.getStartPosition(ci.getCompilationUnit(), valPath.getLeaf());
        int valEnd = (int)sp.getEndPosition(ci.getCompilationUnit(), valPath.getLeaf());
        int expEndPos = (int)sp.getEndPosition(ci.getCompilationUnit(), exp.getLeaf());
        if (valStart < 0 || valEnd < 0 || expEndPos < 0) {
            return false;
        }
        sb.append(source.subSequence(valStart, valEnd)).append(")"); // NOI18N
        sb.append(source.subSequence(origExpEnd, expEndPos));
        
        SourcePositions[] nsp = new SourcePositions[1];
        Tree t;
        if (statement) {
            sb.append(";"); // NOI18N
            t = ci.getTreeUtilities().parseStatement(sb.toString(), nsp);
        } else {
            t = ci.getTreeUtilities().parseExpression(sb.toString(), nsp);
        }
        Scope s = ci.getTreeUtilities().scopeFor(Math.max(0, expEndPos - 1));
        List<Diagnostic<? extends JavaFileObject>> diags = new ArrayList<>();
        attributeTree(JavaSourceAccessor.getINSTANCE().getJavacTask(ci), t, s, diags, null);
        if (!diags.isEmpty()) {
            return false;
        }
        TreePath newPath = new TreePath(exp.getParentPath(), t);
        // path for the method invocation within the newly formed expression or statement.
        // the +1 ensures that we are inside the method invocation subtree (method has >= 1 char as ident)
        TreePath newInvPath = ci.getTreeUtilities().pathFor(newPath, invOffset - baseIndex + 1, nsp[0]);
        while (newInvPath != null && 
            newInvPath.getLeaf().getKind() != Tree.Kind.METHOD_INVOCATION &&
            newInvPath.getLeaf().getKind() != Tree.Kind.NEW_CLASS) {
            newInvPath = newInvPath.getParentPath();
        }
        if (newInvPath == null) {
            return false;
        }
        
        TreePath orig = new TreePath(invPath, getInvocationIdentifier(invPath.getLeaf()));
        TreePath alt = new TreePath(newInvPath, getInvocationIdentifier(newInvPath.getLeaf()));
        
        TypeMirror origType = ci.getTrees().getTypeMirror(orig);
        TypeMirror altType = ci.getTrees().getTypeMirror(alt);
        return altType != null &&  ci.getTypes().isSameType(altType, origType);
//        
//        Element me = ci.getTrees().getElement(newInvPath);
//        return me != null && (me.getKind() == ElementKind.CONSTRUCTOR || me.getKind() == ElementKind.METHOD) ?
//                (ExecutableElement)me : null;
    }
    
    public static boolean isJavaString(CompilationInfo ci, TypeMirror m) {
        if (m == null || m.getKind() != TypeKind.DECLARED) {
            return false;
        } 
        Element e = ((DeclaredType)m).asElement();
        return (e.getKind() == ElementKind.CLASS && ((TypeElement)e).getQualifiedName().contentEquals("java.lang.String")); // NOI18N
    }

    /**
     * Strips the variable name off prefixes and suffixes configured in the coding style. Handles 
     * fields, local variables, parameters and constants.
     * 
     * @param style the code style
     * @param el the variable element 
     * @return name stripped of prefixes/suffices
     */
    public static String stripVariableName(CodeStyle style, VariableElement el) {
        String n = el.getSimpleName().toString();
        switch (el.getKind()) {
            case PARAMETER:
                return stripVariableName(n, style.getParameterNamePrefix(), style.getParameterNameSuffix());
                
            case LOCAL_VARIABLE:
            case RESOURCE_VARIABLE:
            case EXCEPTION_PARAMETER:
                return stripVariableName(n, style.getLocalVarNamePrefix(), style.getLocalVarNameSuffix());
                
            case FIELD: {
//                boolean c = el.getModifiers().containsAll(EnumSet.of(Modifier.FINAL, Modifier.STATIC));
//                if (!c) {
//                    break;
//                }
                // fall through to enum constant
            }
            case ENUM_CONSTANT:
                return stripVariableName(n, style.getFieldNamePrefix(), style.getFieldNameSuffix());
                
            default:
                return n;
        }
    }
    
    private static String stripVariableName(String n, String prefix, String suffix) {
        if (!prefix.isEmpty() && n.startsWith(prefix) && n.length() > prefix.length()) {
            if (Character.isLetter(prefix.charAt(prefix.length() - 1))) {
                // decapitalize the first letter in n
                n = Character.toLowerCase(n.charAt(prefix.length())) + n.substring(prefix.length() + 1);
            } else {
                n = n.substring(prefix.length());
            }
        }
        if (!suffix.isEmpty() && n.endsWith(suffix) && n.length() > suffix.length()) {
            n = n.substring(0, n.length() - suffix.length());
        }
        return n;
    }
    
    public static List<TreePath> getStatementPaths(TreePath firstLeaf) {
        switch (firstLeaf.getParentPath().getLeaf().getKind()) {
            case BLOCK:
                return getTreePaths(firstLeaf.getParentPath(), ((BlockTree) firstLeaf.getParentPath().getLeaf()).getStatements());
            case CASE:
                return getTreePaths(firstLeaf.getParentPath(), ((CaseTree) firstLeaf.getParentPath().getLeaf()).getStatements());
            default:
                return Collections.singletonList(firstLeaf);
        }
    }
    
    private static List<TreePath> getTreePaths(TreePath parent, List<? extends Tree> trees) {
        List<TreePath> ll = new ArrayList<TreePath>(trees.size());
        for (Tree t : trees) {
            ll.add(new TreePath(parent, t));
        }
        return ll;
    }

    /**
     * Determines if assignment looses precision.
     * Works only for primitive types, false for references.
     * 
     * @param from the assigned value type
     * @param to the target type
     * @return true, if precision is lost
     */
    public static boolean loosesPrecision(TypeMirror from, TypeMirror to) {
        if (!from.getKind().isPrimitive() || !to.getKind().isPrimitive()) {
            return false;
        }
        if (to.getKind() == TypeKind.CHAR) {
            return true;
        } else if (from.getKind() == TypeKind.CHAR) {
            return to.getKind() == TypeKind.BYTE || to.getKind() == TypeKind.SHORT;
        }
        return to.getKind().ordinal() < from.getKind().ordinal();
    }

    /**
     * Finds conflicting declarations of methods within type.
     * Given a class `clazz' and a set of methods, finds possible conflicts in the class. For each method,
     * the class is inspected for declarations with the same name, and the same erased types.
     * <p/>
     * Empty Map is returned if no conflicts are found.
     * 
     * @param clazz target class
     * @param methods methods to check
     * @return detected conflicts.
     */
    public static Map<? extends ExecutableElement, ? extends ExecutableElement>  findConflictingMethods(CompilationInfo info, TypeElement clazz, Iterable<? extends ExecutableElement> methods) {
        final Map<Name, Collection<ExecutableElement>> currentByName = new HashMap<>();
        Map<ExecutableElement, ExecutableElement> ret = new HashMap<>();
        
        for (Element e : clazz.getEnclosedElements()) {
            if (e.getKind() != ElementKind.METHOD) {
                continue;
            }
            ExecutableElement ee = (ExecutableElement)e;
            Name n = ee.getSimpleName();
            Collection<ExecutableElement> named = currentByName.get(n);
            if (named == null) {
                named = new ArrayList<>(3);
                currentByName.put(n, named);
            }
            named.add(ee);
        }
        oneMethod: for(ExecutableElement method : methods) {
            DeclaredType asMemberOf = (DeclaredType)clazz.asType();
            ExecutableType et;
            try {
                et = (ExecutableType)info.getTypes().asMemberOf(asMemberOf, method);
            } catch (IllegalArgumentException iae) {
                continue;
            }
            Collection<ExecutableElement> candidates = currentByName.get(method.getSimpleName());
            if (candidates != null) {
                check: for (ExecutableElement e : candidates) {
                    if (e.getKind() != ElementKind.METHOD) {
                        continue;
                    }
                    ExecutableElement ee = (ExecutableElement)e;
                    if (!ee.getSimpleName().equals(method.getSimpleName())) {
                        continue;
                    }
                    if (ee.getParameters().size() != et.getParameterTypes().size()) {
                        continue;
                    }
                    for (int i = 0; i < ee.getParameters().size(); i++) {
                        TypeMirror t1 = ee.getParameters().get(i).asType();
                        TypeMirror t2 = et.getParameterTypes().get(i);
                        if (!info.getTypes().isSameType(
                                info.getTypes().erasure(t1),
                                info.getTypes().erasure(t2)
                        )) {
                            continue check;
                        }
                    }
                    // skip
                    ret.put(method, e);
                }
            }
        }
        return ret;
    }

}
