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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.editor.java;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;

import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.annotations.common.NonNull;

import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.support.ReferencesCount;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.ext.java.JavaTokenContext;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.java.editor.javadoc.JavadocImports;
import org.netbeans.modules.java.editor.options.CodeCompletionPanel;
import org.openide.filesystems.FileObject;
import org.openide.util.WeakListeners;

/**
 *
 * @author Dusan Balek
 * @author Sam Halliday
 */
public final class Utilities {
    
    private static final String CAPTURED_WILDCARD = "<captured wildcard>"; //NOI18N
    private static final String ERROR = "<error>"; //NOI18N
    private static final String UNKNOWN = "<unknown>"; //NOI18N

    private static boolean caseSensitive = true;
    private static boolean showDeprecatedMembers = true;
    private static boolean guessMethodArguments = true;
    private static boolean autoPopupOnJavaIdentifierPart = true;
    private static boolean javaCompletionExcluderMethods;
    private static String javaCompletionAutoPopupTriggers = null;
    private static String javaCompletionSelectors = null;
    private static String javadocCompletionAutoPopupTriggers = null;
    
    private static final AtomicBoolean inited = new AtomicBoolean(false);
    private static Preferences preferences;
    private static final PreferenceChangeListener preferencesTracker = new PreferenceChangeListener() {
        @Override
        public void preferenceChange(PreferenceChangeEvent evt) {
            String settingName = evt == null ? null : evt.getKey();
            if (settingName == null || SimpleValueNames.COMPLETION_CASE_SENSITIVE.equals(settingName)) {
                caseSensitive = preferences.getBoolean(SimpleValueNames.COMPLETION_CASE_SENSITIVE, false);
            }
            if (settingName == null || SimpleValueNames.SHOW_DEPRECATED_MEMBERS.equals(settingName)) {
                showDeprecatedMembers = preferences.getBoolean(SimpleValueNames.SHOW_DEPRECATED_MEMBERS, true);
            }
            if (settingName == null || CodeCompletionPanel.GUESS_METHOD_ARGUMENTS.equals(settingName)) {
                guessMethodArguments = preferences.getBoolean(CodeCompletionPanel.GUESS_METHOD_ARGUMENTS, CodeCompletionPanel.GUESS_METHOD_ARGUMENTS_DEFAULT);
            }
            if (settingName == null || CodeCompletionPanel.JAVA_AUTO_POPUP_ON_IDENTIFIER_PART.equals(settingName)) {
                autoPopupOnJavaIdentifierPart = preferences.getBoolean(CodeCompletionPanel.JAVA_AUTO_POPUP_ON_IDENTIFIER_PART, CodeCompletionPanel.JAVA_AUTO_POPUP_ON_IDENTIFIER_PART_DEFAULT);
            }
            if (settingName == null || CodeCompletionPanel.JAVA_AUTO_COMPLETION_TRIGGERS.equals(settingName)) {
                javaCompletionAutoPopupTriggers = preferences.get(CodeCompletionPanel.JAVA_AUTO_COMPLETION_TRIGGERS, CodeCompletionPanel.JAVA_AUTO_COMPLETION_TRIGGERS_DEFAULT);
            }
            if (settingName == null || CodeCompletionPanel.JAVA_COMPLETION_SELECTORS.equals(settingName)) {
                javaCompletionSelectors = preferences.get(CodeCompletionPanel.JAVA_COMPLETION_SELECTORS, CodeCompletionPanel.JAVA_COMPLETION_SELECTORS_DEFAULT);
            }
            if (settingName == null || CodeCompletionPanel.JAVADOC_AUTO_COMPLETION_TRIGGERS.equals(settingName)) {
                javadocCompletionAutoPopupTriggers = preferences.get(CodeCompletionPanel.JAVADOC_AUTO_COMPLETION_TRIGGERS, CodeCompletionPanel.JAVADOC_AUTO_COMPLETION_TRIGGERS_DEFAULT);
            }
            if (settingName == null || CodeCompletionPanel.JAVA_COMPLETION_BLACKLIST.equals(settingName)) {
                String blacklist = preferences.get(CodeCompletionPanel.JAVA_COMPLETION_BLACKLIST, CodeCompletionPanel.JAVA_COMPLETION_BLACKLIST_DEFAULT);
                updateExcluder(excludeRef, blacklist);
            }
            if (settingName == null || CodeCompletionPanel.JAVA_COMPLETION_WHITELIST.equals(settingName)) {
                String whitelist = preferences.get(CodeCompletionPanel.JAVA_COMPLETION_WHITELIST, CodeCompletionPanel.JAVA_COMPLETION_WHITELIST_DEFAULT);
                updateExcluder(includeRef, whitelist);
            }
            if (settingName == null || CodeCompletionPanel.JAVA_COMPLETION_EXCLUDER_METHODS.equals(settingName)) {
                javaCompletionExcluderMethods = preferences.getBoolean(CodeCompletionPanel.JAVA_COMPLETION_EXCLUDER_METHODS, CodeCompletionPanel.JAVA_COMPLETION_EXCLUDER_METHODS_DEFAULT);
            }
        }
    };
    
    private static String cachedPrefix = null;
    private static Pattern cachedPattern = null;
    
    public static boolean startsWith(String theString, String prefix) {
        if (theString == null || theString.length() == 0 || ERROR.equals(theString))
            return false;
        if (prefix == null || prefix.length() == 0)
            return true;
        return isCaseSensitive() ? theString.startsWith(prefix) :
            theString.toLowerCase(Locale.ENGLISH).startsWith(prefix.toLowerCase(Locale.ENGLISH));
    }
    
    public static boolean startsWithCamelCase(String theString, String prefix) {
        if (theString == null || theString.length() == 0 || prefix == null || prefix.length() == 0)
            return false;
        if (!prefix.equals(cachedPrefix) || cachedPattern == null) {
            StringBuilder sb = new StringBuilder();
            int lastIndex = 0;
            int index;
            do {
                index = findNextUpper(prefix, lastIndex + 1);
                String token = prefix.substring(lastIndex, index == -1 ? prefix.length(): index);
                sb.append(token); 
                sb.append(index != -1 ? "[\\p{javaLowerCase}\\p{Digit}_\\$]*" : ".*"); // NOI18N         
                lastIndex = index;
            } while (index != -1);
            cachedPrefix = prefix;
            cachedPattern = Pattern.compile(sb.toString());
        }
        return cachedPattern.matcher(theString).matches();
    }
    
    private static int findNextUpper(String text, int offset) {        
        for (int i = offset; i < text.length(); i++) {
            if (Character.isUpperCase(text.charAt(i)))
                return i;
        }
        return -1;
    }

    public static boolean isCaseSensitive() {
        lazyInit();
        return caseSensitive;
    }

    public static void setCaseSensitive(boolean b) {
        lazyInit();
        caseSensitive = b;
    }

    public static boolean isShowDeprecatedMembers() {
        lazyInit();
        return showDeprecatedMembers;
    }

    public static void setShowDeprecatedMembers(boolean b) {
        lazyInit();
        showDeprecatedMembers = b;
    }

    public static boolean guessMethodArguments() {
        lazyInit();
        return guessMethodArguments;
    }

    public static boolean autoPopupOnJavaIdentifierPart() {
        lazyInit();
        return autoPopupOnJavaIdentifierPart;
    }

    public static String getJavaCompletionAutoPopupTriggers() {
        lazyInit();
        return javaCompletionAutoPopupTriggers;
    }

    public static String getJavaCompletionSelectors() {
        lazyInit();
        return javaCompletionSelectors;
    }

    public static String getJavadocCompletionAutoPopupTriggers() {
        lazyInit();
        return javadocCompletionAutoPopupTriggers;
    }

    static private final AtomicReference<Collection<String>> excludeRef = new AtomicReference<Collection<String>>();
    static private final AtomicReference<Collection<String>> includeRef = new AtomicReference<Collection<String>>();

    private static void updateExcluder(AtomicReference<Collection<String>> existing, String updated) {
        Collection<String> nue = new LinkedList<String>();
        if (updated == null || updated.length() == 0) {
            existing.set(nue);
            return;
        }
        String[] entries = updated.split(","); //NOI18N
        for (String entry : entries) {
            if (entry.length() != 0) {
                nue.add(entry);
            }
        }
        existing.set(nue);
    }

    /**
     * @return the user setting for whether the excluder should operate on methods
     */
    public static boolean isExcludeMethods(){
        lazyInit();
        return javaCompletionExcluderMethods;
    }

    /**
     * @param fqn Fully Qualified Name (including method names). Packages names are expected to
     * end in a trailing "." except the default package.
     * @return
     */
    public static boolean isExcluded(final CharSequence fqn) {
        if (fqn == null || fqn.length() == 0) {
            return true;
        }
        lazyInit();
        String s = fqn.toString();
        Collection<String> include = includeRef.get();
        Collection<String> exclude = excludeRef.get();

        if (include != null && !include.isEmpty()) {
            for (String entry : include) {
                if (entry.length() > fqn.length()) {
                    if (entry.startsWith(s)) {
                        return false;
                    }
                } else if (s.startsWith(entry)) {
                    return false;
                }
            }
        }

        if (exclude != null && !exclude.isEmpty()) {
            for (String entry : exclude) {
                if (entry.length() <= fqn.length() && s.startsWith(entry)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static void lazyInit() {
        if (inited.compareAndSet(false, true)) {
            preferences = MimeLookup.getLookup(JavaKit.JAVA_MIME_TYPE).lookup(Preferences.class);
            preferences.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, preferencesTracker, preferences));
            preferencesTracker.preferenceChange(null);
        }
    }

    public static int getImportanceLevel(ReferencesCount referencesCount, ElementHandle<TypeElement> handle) {
        int typeRefCount = 999 - Math.min(referencesCount.getTypeReferenceCount(handle), 999);
        int pkgRefCount = 999;
        String binaryName = SourceUtils.getJVMSignature(handle)[0];
        int idx = binaryName.lastIndexOf('.');
        if (idx > 0) {
            ElementHandle<PackageElement> pkgElement = ElementHandle.createPackageElementHandle(binaryName.substring(0, idx));
            pkgRefCount -= Math.min(referencesCount.getPackageReferenceCount(pkgElement), 999);
        }
        return typeRefCount * 100000 + pkgRefCount * 100 + getImportanceLevel(binaryName);
    }
    
    public static int getImportanceLevel(String fqn) {
        int weight = 50;
        if (fqn.startsWith("java.lang") || fqn.startsWith("java.util")) // NOI18N
            weight -= 10;
        else if (fqn.startsWith("org.omg") || fqn.startsWith("org.apache")) // NOI18N
            weight += 10;
        else if (fqn.startsWith("com.sun") || fqn.startsWith("com.ibm") || fqn.startsWith("com.apple")) // NOI18N
            weight += 20;
        else if (fqn.startsWith("sun") || fqn.startsWith("sunw") || fqn.startsWith("netscape")) // NOI18N
            weight += 30;
        return weight;
    }
    
    public static boolean hasAccessibleInnerClassConstructor(Element e, Scope scope, Trees trees) {
        DeclaredType dt = (DeclaredType)e.asType();
        for (TypeElement inner : ElementFilter.typesIn(e.getEnclosedElements())) {
            if (trees.isAccessible(scope, inner, dt)) {
                DeclaredType innerType = (DeclaredType)inner.asType();
                for (ExecutableElement ctor : ElementFilter.constructorsIn(inner.getEnclosedElements())) {
                    if (trees.isAccessible(scope, ctor, innerType))
                        return true;
                }
            }
        }
        return false;
    }
        
    public static TreePath getPathElementOfKind(Tree.Kind kind, TreePath path) {
        return getPathElementOfKind(EnumSet.of(kind), path);
    }
    
    public static TreePath getPathElementOfKind(Set<Tree.Kind> kinds, TreePath path) {
        while (path != null) {
            if (kinds.contains(path.getLeaf().getKind()))
                return path;
            path = path.getParentPath();
        }
        return null;        
    }        
    
    public static boolean isJavaContext(final JTextComponent component, final int offset, final boolean allowInStrings) {
        Document doc = component.getDocument();
        if (doc instanceof AbstractDocument) {
            ((AbstractDocument)doc).readLock();
        }
        try {
        if (doc.getLength() == 0 && "text/x-dialog-binding".equals(doc.getProperty("mimeType"))) { //NOI18N
            InputAttributes attributes = (InputAttributes) doc.getProperty(InputAttributes.class);
            LanguagePath path = LanguagePath.get(MimeLookup.getLookup("text/x-dialog-binding").lookup(Language.class)); //NOI18N
            Document d = (Document) attributes.getValue(path, "dialogBinding.document"); //NOI18N
            if (d != null)
                return "text/x-java".equals(NbEditorUtilities.getMimeType(d)); //NOI18N
            FileObject fo = (FileObject)attributes.getValue(path, "dialogBinding.fileObject"); //NOI18N
            return "text/x-java".equals(fo.getMIMEType()); //NOI18N
        }
        TokenSequence<JavaTokenId> ts = SourceUtils.getJavaTokenSequence(TokenHierarchy.get(doc), offset);
        if (ts == null)
            return false;        
        if (!ts.moveNext() && !ts.movePrevious())
            return true;
        if (offset == ts.offset())
            return true;
        switch(ts.token().id()) {
            case DOUBLE_LITERAL:
            case FLOAT_LITERAL:
            case FLOAT_LITERAL_INVALID:
                if (ts.token().text().charAt(0) == '.')
                    break;
            case CHAR_LITERAL:
            case INT_LITERAL:
            case INVALID_COMMENT_END:
            case JAVADOC_COMMENT:
            case LONG_LITERAL:
            case LINE_COMMENT:
            case BLOCK_COMMENT:
                return false;
            case STRING_LITERAL:
                return allowInStrings;
        }
        return true;
        } finally {
            if (doc instanceof AbstractDocument) {
                ((AbstractDocument) doc).readUnlock();
            }
        }
    }
    
    @Deprecated public static CharSequence getTypeName(TypeMirror type, boolean fqn) {
        return getTypeName(null, type, fqn, false);
    }
    
    @Deprecated public static CharSequence getTypeName(TypeMirror type, boolean fqn, boolean varArg) {
        return getTypeName(null, type, fqn, varArg);
    }
    
    public static CharSequence getTypeName(CompilationInfo info, TypeMirror type, boolean fqn) {
        return getTypeName(info, type, fqn, false);
    }
    
    public static CharSequence getTypeName(CompilationInfo info, TypeMirror type, boolean fqn, boolean varArg) {
	if (type == null)
            return ""; //NOI18N
        return new TypeNameVisitor(varArg).visit(type, fqn);
    }
    
    public static CharSequence getElementName(Element el, boolean fqn) {
        if (el == null || el.asType().getKind() == TypeKind.NONE)
            return ""; //NOI18N
        return new ElementNameVisitor().visit(el, fqn);
    }
    
    public static Collection<? extends Element> getForwardReferences(TreePath path, int pos, SourcePositions sourcePositions, Trees trees) {
        HashSet<Element> refs = new HashSet<Element>();
        while(path != null) {
            switch(path.getLeaf().getKind()) {
                case BLOCK:
                case ANNOTATION_TYPE:
                case CLASS:
                case ENUM:
                case INTERFACE:
                    return refs;
                case VARIABLE:
                    refs.add(trees.getElement(path));
                    TreePath parent = path.getParentPath();
                    if (TreeUtilities.CLASS_TREE_KINDS.contains(parent.getLeaf().getKind())) {
                        boolean isStatic = ((VariableTree)path.getLeaf()).getModifiers().getFlags().contains(Modifier.STATIC);
                        for(Tree member : ((ClassTree)parent.getLeaf()).getMembers()) {
                            if (member.getKind() == Tree.Kind.VARIABLE && sourcePositions.getStartPosition(path.getCompilationUnit(), member) >= pos &&
                                    (isStatic || !((VariableTree)member).getModifiers().getFlags().contains(Modifier.STATIC)))
                                refs.add(trees.getElement(new TreePath(parent, member)));
                        }
                    }
                    return refs;
                case ENHANCED_FOR_LOOP:
                    EnhancedForLoopTree efl = (EnhancedForLoopTree)path.getLeaf();
                    if (sourcePositions.getEndPosition(path.getCompilationUnit(), efl.getExpression()) >= pos)
                        refs.add(trees.getElement(new TreePath(path, efl.getVariable())));                        
            }
            path = path.getParentPath();
        }
        return refs;
    }
    
    public static List<String> varNamesSuggestions(TypeMirror type, String suggestedName, String prefix, Types types, Elements elements, Iterable<? extends Element> locals, boolean isConst) {
        List<String> result = new ArrayList<String>();
        if (type == null && suggestedName == null)
            return result;
        List<String> vnct = suggestedName != null ? Collections.singletonList(suggestedName) : varNamesForType(type, types, elements, prefix);
        if (isConst) {
            List<String> ls = new ArrayList<String>(vnct.size());
            for (String s : vnct)
                ls.add(getConstName(s));
            vnct = ls;
        }
        String p = prefix;
        while (p != null && p.length() > 0) {
            List<String> l = new ArrayList<String>();
            for(String name : vnct)
                if (startsWith(name, p))
                    l.add(name);
            if (l.isEmpty()) {
                p = nextName(p);
            } else {
                vnct = l;
                prefix = prefix.substring(0, prefix.length() - p.length());
                p = null;
            }
        }
        for (String name : vnct) {
            boolean isPrimitive = type != null && type.getKind().isPrimitive();
            if (prefix != null && prefix.length() > 0) {
                if (isConst) {
                    name = prefix.toUpperCase(Locale.ENGLISH) + '_' + name;
                } else {
                    name = prefix + name.toUpperCase(Locale.ENGLISH).charAt(0) + name.substring(1);
                }
            }
            int cnt = 1;
            String baseName = name;
            while (isClashing(name, type, locals)) {
                if (isPrimitive) {
                    char c = name.charAt(0);
                    name = Character.toString(++c);
                    if (c == 'z') //NOI18N
                        isPrimitive = false;
                } else {
                    name = baseName + cnt++;
                }
            }
            result.add(name);
        }
        return result;
    }

    public static boolean inAnonymousOrLocalClass(TreePath path) {
        if (path == null)
            return false;
        TreePath parentPath = path.getParentPath();
        if (TreeUtilities.CLASS_TREE_KINDS.contains(path.getLeaf().getKind()) && parentPath.getLeaf().getKind() != Tree.Kind.COMPILATION_UNIT && !TreeUtilities.CLASS_TREE_KINDS.contains(parentPath.getLeaf().getKind()))                
            return true;
        return inAnonymousOrLocalClass(parentPath);
    }

    public static Set<Element> getUsedElements(final CompilationInfo info) {
        final Set<Element> ret = new HashSet<Element>();
        final Trees trees = info.getTrees();
        new TreePathScanner<Void, Void>() {

            @Override
            public Void visitIdentifier(IdentifierTree node, Void p) {
                addElement(trees.getElement(getCurrentPath()));
                return null;
            }

            @Override
            public Void visitClass(ClassTree node, Void p) {
                for (Element element : JavadocImports.computeReferencedElements(info, getCurrentPath()))
                    addElement(element);
                return super.visitClass(node, p);
            }

            @Override
            public Void visitMethod(MethodTree node, Void p) {
                for (Element element : JavadocImports.computeReferencedElements(info, getCurrentPath()))
                    addElement(element);
                return super.visitMethod(node, p);
            }

            @Override
            public Void visitVariable(VariableTree node, Void p) {
                for (Element element : JavadocImports.computeReferencedElements(info, getCurrentPath()))
                    addElement(element);
                return super.visitVariable(node, p);
            }

            @Override
            public Void visitCompilationUnit(CompilationUnitTree node, Void p) {
                scan(node.getPackageAnnotations(), p);
                return scan(node.getTypeDecls(), p);
            }
            
            private void addElement(Element element) {
                if (element != null) {
                    ret.add(element);
                }
            }
        }.scan(info.getCompilationUnit(), null);
        return ret;                
    }

    private static List<String> varNamesForType(TypeMirror type, Types types, Elements elements, String prefix) {
        switch (type.getKind()) {
            case ARRAY:
                TypeElement iterableTE = elements.getTypeElement("java.lang.Iterable"); //NOI18N
                TypeMirror iterable = iterableTE != null ? types.getDeclaredType(iterableTE) : null;
                TypeMirror ct = ((ArrayType)type).getComponentType();
                if (ct.getKind() == TypeKind.ARRAY && iterable != null && types.isSubtype(ct, iterable))
                    return varNamesForType(ct, types, elements, prefix);
                List<String> vnct = new ArrayList<String>();
                for (String name : varNamesForType(ct, types, elements, prefix))
                    vnct.add(name.endsWith("s") ? name + "es" : name + "s"); //NOI18N
                return vnct;
            case BOOLEAN:
            case BYTE:
            case CHAR:
            case DOUBLE:
            case FLOAT:
            case INT:
            case LONG:
            case SHORT:
                return Collections.<String>singletonList(type.toString().substring(0, 1));
            case TYPEVAR:
                return Collections.<String>singletonList(type.toString().toLowerCase(Locale.ENGLISH));
            case ERROR:
                String tn = ((ErrorType)type).asElement().getSimpleName().toString();
                if (tn.toUpperCase(Locale.ENGLISH).contentEquals(tn))
                    return Collections.<String>singletonList(tn.toLowerCase(Locale.ENGLISH));
                StringBuilder sb = new StringBuilder();
                ArrayList<String> al = new ArrayList<String>();
                if ("Iterator".equals(tn)) //NOI18N
                    al.add("it"); //NOI18N
                while((tn = nextName(tn)).length() > 0) {
                    al.add(tn);
                    sb.append(tn.charAt(0));
                }
                if (sb.length() > 0) {
                    String s = sb.toString();
                    if (prefix == null || prefix.length() == 0 || s.startsWith(prefix))
                        al.add(s);
                }
                return al;
            case DECLARED:
                iterableTE = elements.getTypeElement("java.lang.Iterable"); //NOI18N
                iterable = iterableTE != null ? types.getDeclaredType(iterableTE) : null;
                tn = ((DeclaredType)type).asElement().getSimpleName().toString();
                if (tn.toUpperCase(Locale.ENGLISH).contentEquals(tn))
                    return Collections.<String>singletonList(tn.toLowerCase(Locale.ENGLISH));
                sb = new StringBuilder();
                al = new ArrayList<String>();
                if ("Iterator".equals(tn)) //NOI18N
                    al.add("it"); //NOI18N
                while((tn = nextName(tn)).length() > 0) {
                    al.add(tn);
                    sb.append(tn.charAt(0));
                }
                if (iterable != null && types.isSubtype(type, iterable)) {
                    List<? extends TypeMirror> tas = ((DeclaredType)type).getTypeArguments();
                    if (tas.size() > 0) {
                        TypeMirror et = tas.get(0);
                        if (et.getKind() == TypeKind.ARRAY || (et.getKind() != TypeKind.WILDCARD && types.isSubtype(et, iterable))) {
                            al.addAll(varNamesForType(et, types, elements, prefix));
                        } else {
                            for (String name : varNamesForType(et, types, elements, prefix))
                                al.add(name.endsWith("s") ? name + "es" : name + "s"); //NOI18N
                        }
                    }
                }
                if (sb.length() > 0) {
                    String s = sb.toString();
                    if (prefix == null || prefix.length() == 0 || s.startsWith(prefix))
                        al.add(s);
                }
                return al;
            case WILDCARD:
                TypeMirror bound = ((WildcardType)type).getExtendsBound();
                if (bound == null)
                    bound = ((WildcardType)type).getSuperBound();
                if (bound != null)
                    return varNamesForType(bound, types, elements, prefix);
        }
        return Collections.<String>emptyList();
    }
    
    private static String getConstName(String s) {
        StringBuilder sb = new StringBuilder();
        boolean prevUpper = true;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isUpperCase(c)) {
                if (!prevUpper)
                    sb.append('_');
                sb.append(c);
                prevUpper = true;
            } else {
                sb.append(Character.toUpperCase(c));
                prevUpper = false;
            }
        }
        return sb.toString();
    }
    
    private static String nextName(CharSequence name) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (Character.isUpperCase(c)) {
                char lc = Character.toLowerCase(c);
                sb.append(lc);
                sb.append(name.subSequence(i + 1, name.length()));
                break;
            }
        }
        return sb.toString();
    }
    
    private static boolean isClashing(String varName, TypeMirror type, Iterable<? extends Element> locals) {
        if (JavaTokenContext.getKeyword(varName) != null)
            return true;
        if (type != null && type.getKind() == TypeKind.DECLARED && ((DeclaredType)type).asElement().getSimpleName().contentEquals(varName))
            return true;
        for (Element e : locals) {
            if ((e.getKind().isField() || e.getKind() == ElementKind.LOCAL_VARIABLE || e.getKind() == ElementKind.RESOURCE_VARIABLE
                    || e.getKind() == ElementKind.PARAMETER || e.getKind() == ElementKind.EXCEPTION_PARAMETER) && varName.contentEquals(e.getSimpleName()))
                return true;
        }
        return false;
    }
    
    private static class TypeNameVisitor extends SimpleTypeVisitor6<StringBuilder,Boolean> {
        
        private boolean varArg;
        private boolean insideCapturedWildcard = false;
        
        private TypeNameVisitor(boolean varArg) {
            super(new StringBuilder());
            this.varArg = varArg;
        }
        
        @Override
        public StringBuilder defaultAction(TypeMirror t, Boolean p) {
            return DEFAULT_VALUE.append(t);
        }
        
        @Override
        public StringBuilder visitDeclared(DeclaredType t, Boolean p) {
            Element e = t.asElement();
            if (e instanceof TypeElement) {
                TypeElement te = (TypeElement)e;
                DEFAULT_VALUE.append((p ? te.getQualifiedName() : te.getSimpleName()).toString());
                Iterator<? extends TypeMirror> it = t.getTypeArguments().iterator();
                if (it.hasNext()) {
                    DEFAULT_VALUE.append("<"); //NOI18N
                    while(it.hasNext()) {
                        visit(it.next(), p);
                        if (it.hasNext())
                            DEFAULT_VALUE.append(", "); //NOI18N
                    }
                    DEFAULT_VALUE.append(">"); //NOI18N
                }
                return DEFAULT_VALUE;                
            } else {
                return DEFAULT_VALUE.append(UNKNOWN); //NOI18N
            }
        }

        @Override
        public StringBuilder visitUnion(UnionType t, Boolean p) {
            Iterator<? extends TypeMirror> it = t.getAlternatives().iterator();
            while(it.hasNext()) {
                visit(it.next(), p);
                if (it.hasNext())
                    DEFAULT_VALUE.append(" | "); //NOI18N
            }
            return DEFAULT_VALUE;
        }
                        
        @Override
        public StringBuilder visitArray(ArrayType t, Boolean p) {
            boolean isVarArg = varArg;
            varArg = false;
            visit(t.getComponentType(), p);
            return DEFAULT_VALUE.append(isVarArg ? "..." : "[]"); //NOI18N
        }

        @Override
        public StringBuilder visitTypeVariable(TypeVariable t, Boolean p) {
            Element e = t.asElement();
            if (e != null) {
                String name = e.getSimpleName().toString();
                if (!CAPTURED_WILDCARD.equals(name))
                    return DEFAULT_VALUE.append(name);
            }
            DEFAULT_VALUE.append("?"); //NOI18N
            if (!insideCapturedWildcard) {
                insideCapturedWildcard = true;
                TypeMirror bound = t.getLowerBound();
                if (bound != null && bound.getKind() != TypeKind.NULL) {
                    DEFAULT_VALUE.append(" super "); //NOI18N
                    visit(bound, p);
                } else {
                    bound = t.getUpperBound();
                    if (bound != null && bound.getKind() != TypeKind.NULL) {
                        DEFAULT_VALUE.append(" extends "); //NOI18N
                        if (bound.getKind() == TypeKind.TYPEVAR)
                            bound = ((TypeVariable)bound).getLowerBound();
                        visit(bound, p);
                    }
                }
                insideCapturedWildcard = false;
            }
            return DEFAULT_VALUE;
        }

        @Override
        public StringBuilder visitWildcard(WildcardType t, Boolean p) {
            int len = DEFAULT_VALUE.length();
            DEFAULT_VALUE.append("?"); //NOI18N
            TypeMirror bound = t.getSuperBound();
            if (bound == null) {
                bound = t.getExtendsBound();
                if (bound != null) {
                    DEFAULT_VALUE.append(" extends "); //NOI18N
                    if (bound.getKind() == TypeKind.WILDCARD)
                        bound = ((WildcardType)bound).getSuperBound();
                    visit(bound, p);
                } else if (len == 0) {
                    bound = SourceUtils.getBound(t);
                    if (bound != null && (bound.getKind() != TypeKind.DECLARED || !((TypeElement)((DeclaredType)bound).asElement()).getQualifiedName().contentEquals("java.lang.Object"))) { //NOI18N
                        DEFAULT_VALUE.append(" extends "); //NOI18N
                        visit(bound, p);
                    }
                }
            } else {
                DEFAULT_VALUE.append(" super "); //NOI18N
                visit(bound, p);
            }
            return DEFAULT_VALUE;
        }

        @Override
        public StringBuilder visitError(ErrorType t, Boolean p) {
            Element e = t.asElement();
            if (e instanceof TypeElement) {
                TypeElement te = (TypeElement)e;
                return DEFAULT_VALUE.append((p ? te.getQualifiedName() : te.getSimpleName()).toString());
            }
            return DEFAULT_VALUE;
        }
    }
    
    private static class ElementNameVisitor extends SimpleElementVisitor6<StringBuilder,Boolean> {
        
        private ElementNameVisitor() {
            super(new StringBuilder());
        }

        @Override
        public StringBuilder visitPackage(PackageElement e, Boolean p) {
            return DEFAULT_VALUE.append((p ? e.getQualifiedName() : e.getSimpleName()).toString());
        }

	@Override
        public StringBuilder visitType(TypeElement e, Boolean p) {
            return DEFAULT_VALUE.append((p ? e.getQualifiedName() : e.getSimpleName()).toString());
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
            if (orig.getKind() == TypeKind.WILDCARD) {
                TypeMirror extendsBound = ((WildcardType) orig).getExtendsBound();
                TypeMirror rct = SourceUtils.resolveCapturedType(extendsBound != null ? extendsBound : ((WildcardType) orig).getSuperBound());
                if (rct != null) {
                    return rct;
                }
            }
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
        
    /**
     * @since 2.12
     */
    public static @NonNull List<ExecutableElement> fuzzyResolveMethodInvocation(CompilationInfo info, TreePath path, List<TypeMirror> proposed, int[] index) {
        assert path.getLeaf().getKind() == Kind.METHOD_INVOCATION || path.getLeaf().getKind() == Kind.NEW_CLASS;
        
        if (path.getLeaf().getKind() == Kind.METHOD_INVOCATION) {
            List<TypeMirror> actualTypes = new LinkedList<TypeMirror>();
            MethodInvocationTree mit = (MethodInvocationTree) path.getLeaf();

            for (Tree a : mit.getArguments()) {
                TreePath tp = new TreePath(path, a);
                actualTypes.add(info.getTrees().getTypeMirror(tp));
            }

            String methodName;
            TypeMirror on;

            switch (mit.getMethodSelect().getKind()) {
                case IDENTIFIER:
                    Scope s = info.getTrees().getScope(path);
                    TypeElement enclosingClass = s.getEnclosingClass();
                    on = enclosingClass != null ? enclosingClass.asType() : null;
                    methodName = ((IdentifierTree) mit.getMethodSelect()).getName().toString();
                    break;
                case MEMBER_SELECT:
                    on = info.getTrees().getTypeMirror(new TreePath(path, ((MemberSelectTree) mit.getMethodSelect()).getExpression()));
                    methodName = ((MemberSelectTree) mit.getMethodSelect()).getIdentifier().toString();
                    break;
                default:
                    throw new IllegalStateException();
            }

            if (on == null || on.getKind() != TypeKind.DECLARED) {
                return Collections.emptyList();
            }
            
            return resolveMethod(info, actualTypes, (DeclaredType) on, false, false, methodName, proposed, index);
        }
        
        if (path.getLeaf().getKind() == Kind.NEW_CLASS) {
            List<TypeMirror> actualTypes = new LinkedList<TypeMirror>();
            NewClassTree nct = (NewClassTree) path.getLeaf();

            for (Tree a : nct.getArguments()) {
                TreePath tp = new TreePath(path, a);
                actualTypes.add(info.getTrees().getTypeMirror(tp));
            }

            TypeMirror on = info.getTrees().getTypeMirror(new TreePath(path, nct.getIdentifier()));
            
            if (on == null || on.getKind() != TypeKind.DECLARED) {
                return Collections.emptyList();
            }
            
            return resolveMethod(info, actualTypes, (DeclaredType) on, false, true, null, proposed, index);
        }
        
        return Collections.emptyList();
    }

    private static Iterable<ExecutableElement> execsIn(CompilationInfo info, TypeElement e, boolean constr, String name) {
        if (constr) {
            return ElementFilter.constructorsIn(info.getElements().getAllMembers(e));
        }
        
        List<ExecutableElement> result = new LinkedList<ExecutableElement>();
        
        for (ExecutableElement ee : ElementFilter.methodsIn(info.getElements().getAllMembers(e))) {
            if (name.equals(ee.getSimpleName().toString())) {
                result.add(ee);
            }
        }
        
        return result;
    }
    
    private static List<ExecutableElement> resolveMethod(CompilationInfo info, List<TypeMirror> foundTypes, DeclaredType on, boolean statik, boolean constr, String name, List<TypeMirror> candidateTypes, int[] index) {
        List<ExecutableElement> found = new LinkedList<ExecutableElement>();
        
        OUTER:
        for (ExecutableElement ee : execsIn(info, (TypeElement) on.asElement(), constr, name)) {
            TypeMirror currType = ((TypeElement) ee.getEnclosingElement()).asType();
            if (!info.getTypes().isSubtype(on, currType) && !on.asElement().equals(((DeclaredType)currType).asElement())) //XXX: fix for #132627, a clearer fix may exist
                continue;
            if (ee.getParameters().size() == foundTypes.size() /*XXX: variable arg count*/) {
                TypeMirror innerCandidate = null;
                int innerIndex = -1;
                ExecutableType et = (ExecutableType) info.getTypes().asMemberOf(on, ee);
                Iterator<? extends TypeMirror> formal = et.getParameterTypes().iterator();
                Iterator<? extends TypeMirror> actual = foundTypes.iterator();
                boolean mismatchFound = false;
                int i = 0;

                while (formal.hasNext() && actual.hasNext()) {
                    TypeMirror currentFormal = formal.next();
                    TypeMirror currentActual = actual.next();

                    if (!info.getTypes().isAssignable(currentActual, currentFormal) || currentActual.getKind() == TypeKind.ERROR) {
                        if (mismatchFound) {
                            //only one mismatch supported:
                            continue OUTER;
                        }
                        mismatchFound = true;
                        innerCandidate = currentFormal;
                        innerIndex = i;
                    }

                    i++;
                }

                if (mismatchFound) {
                    if (candidateTypes.isEmpty()) {
                        index[0] = innerIndex;
                        candidateTypes.add(innerCandidate);
                        found.add(ee);
                    } else {
                        //see testFuzzyResolveConstructor2:
                        if (index[0] == innerIndex) {
                            boolean add = true;
                            for (TypeMirror tm : candidateTypes) {
                                if (info.getTypes().isSameType(tm, innerCandidate)) {
                                    add = false;
                                    break;
                                }
                            }
                            if (add) {
                                candidateTypes.add(innerCandidate);
                                found.add(ee);
                            }
                        }
                    }
                }
            }
        }

        return found;
    }


    private Utilities() {
    }
    
    
}
