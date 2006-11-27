/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.java;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;

import java.util.*;

import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.*;
import javax.swing.text.JTextComponent;

import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.*;
import org.netbeans.editor.ext.ExtSettingsDefaults;
import org.netbeans.editor.ext.ExtSettingsNames;
import org.netbeans.editor.ext.java.JavaTokenContext;

/**
 *
 * @author Dusan Balek
 */
public class Utilities {
    
    private static final String CAPTURED_WILDCARD = "<captured wildcard>"; //NOI18N
    private static final String ERROR = "<error>"; //NOI18N

    private static boolean caseSensitive = true;
    private static SettingsChangeListener settingsListener = new SettingsListener();

    static {
        Settings.addSettingsChangeListener(settingsListener);
        setCaseSensitive(SettingsUtil.getBoolean(JavaKit.class,
                ExtSettingsNames.COMPLETION_CASE_SENSITIVE,
                ExtSettingsDefaults.defaultCompletionCaseSensitive));
    }
    
    public static boolean startsWith(String theString, String prefix) {
        if (theString == null || theString.length() == 0 || ERROR.equals(theString))
            return false;
        if (prefix == null || prefix.length() == 0)
            return true;
        return caseSensitive ? theString.startsWith(prefix) :
            theString.toLowerCase().startsWith(prefix.toLowerCase());
    }
    
    public static boolean isCaseSensitive() {
        return caseSensitive;
    }

    public static void setCaseSensitive(boolean b) {
        caseSensitive = b;
    }

    public static TreePath getPathElementOfKind(Tree.Kind kind, TreePath path) {
        return getPathElementOfKind(EnumSet.of(kind), path);
    }
    
    public static TreePath getPathElementOfKind(EnumSet<Tree.Kind> kinds, TreePath path) {
        while (path != null) {
            if (kinds.contains(path.getLeaf().getKind()))
                return path;
            path = path.getParentPath();
        }
        return null;        
    }        
    
    public static boolean isJavaContext(final JTextComponent component, final int offset) {
        TokenSequence<JavaTokenId> ts = getJavaTokenSequence(component, offset);
        if (ts == null)
            return false;
        if (!ts.moveNext() || ts.move(offset) == 0)
            return true;
        switch(ts.token().id()) {
            case DOUBLE_LITERAL:
                if (ts.token().text().charAt(0) == '.')
                    break;
            case CHAR_LITERAL:
            case CHAR_LITERAL_INCOMPLETE:
            case FLOAT_LITERAL:
            case FLOAT_LITERAL_INVALID:
            case INT_LITERAL:
            case INVALID_COMMENT_END:
            case JAVADOC_COMMENT:
            case JAVADOC_COMMENT_INCOMPLETE:
            case LONG_LITERAL:
            case STRING_LITERAL:
            case STRING_LITERAL_INCOMPLETE:
            case LINE_COMMENT:
            case BLOCK_COMMENT:
            case BLOCK_COMMENT_INCOMPLETE:
                return false;
        }
        return true;
    }
    
    public static TokenSequence<JavaTokenId> getJavaTokenSequence(final JTextComponent component, final int offset) {
        TokenHierarchy hierarchy = TokenHierarchy.get(component.getDocument());
        if (hierarchy != null) {
            TokenSequence<? extends TokenId> ts = hierarchy.tokenSequence();
            while(ts != null && ts.moveNext()) {
                ts.move(offset);
                if (ts.language() == JavaTokenId.language())
                    return (TokenSequence<JavaTokenId>)ts;
                ts = ts.embedded();
            }
        }
        return null;
    }
    
    public static String getTypeName(TypeMirror type, boolean fqn) {
        return getTypeName(type, fqn, false);
    }
    
    public static String getTypeName(TypeMirror type, boolean fqn, boolean varArg) {
	if (type == null)
            return null;
        return type.accept(new TypeNameVisitor(varArg),fqn);
    }
    
    public static String getElementName(Element el, boolean fqn) {
        if (el == null)
            return null;
        return el.accept(new ElementNameVisitor(), fqn);
    }
    
    public static Collection<? extends Element> getForwardReferences(TreePath path, int pos, SourcePositions sourcePositions, Trees trees) {
        HashSet<Element> refs = new HashSet<Element>();
        while(path != null) {
            switch(path.getLeaf().getKind()) {
                case BLOCK:
                case CLASS:
                    return refs;
                case VARIABLE:
                    refs.add(trees.getElement(path));
                    TreePath parent = path.getParentPath();
                    if (parent.getLeaf().getKind() == Tree.Kind.CLASS) {
                        boolean isStatic = ((VariableTree)path.getLeaf()).getModifiers().getFlags().contains(Modifier.STATIC);
                        for(Tree member : ((ClassTree)parent.getLeaf()).getMembers()) {
                            if (member.getKind() == Tree.Kind.VARIABLE && sourcePositions.getStartPosition(path.getCompilationUnit(), member) >= pos &&
                                    (isStatic || !((VariableTree)member).getModifiers().getFlags().contains(Modifier.STATIC)))
                                refs.add(trees.getElement(new TreePath(parent, member)));
                        }
                    }
                    return refs;
            }
            path = path.getParentPath();
        }
        return refs;
    }
    
    public static List<String> varNamesSuggestions(TypeMirror type, String prefix, Types types, Elements elements, Iterable<? extends Element> locals, boolean isConst) {
        List<String> result = new ArrayList<String>();
        List<String> vnct = varNamesForType(type, types, elements);
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
            boolean isPrimitive = type.getKind().isPrimitive();
            if (prefix != null && prefix.length() > 0) {
                if (isConst) {
                    name = prefix.toUpperCase() + '_' + name;
                } else {
                    name = prefix + Character.toUpperCase(name.charAt(0)) + name.substring(1);
                }
            }
            int cnt = 1;
            while (isClashing(name, locals)) {
                if (isPrimitive) {
                    char c = name.charAt(0);
                    name = Character.toString(++c);
                    if (c == 'z') //NOI18N
                        isPrimitive = false;
                } else {
                    name += cnt++;
                }
            }
            result.add(name);
        }
        return result;
    }

    public static boolean isInMethod(TreePath tp) {
        while (tp != null) {
            if (tp.getLeaf().getKind() == Tree.Kind.METHOD) {
                return true;
            }
            
            tp = tp.getParentPath();
        }
        
        return false;
    }
        
    private static List<String> varNamesForType(TypeMirror type, Types types, Elements elements) {
        switch (type.getKind()) {
            case ARRAY:
                TypeMirror iterable = types.getDeclaredType(elements.getTypeElement("java.lang.Iterable")); //NOI18N
                TypeMirror ct = ((ArrayType)type).getComponentType();
                if (ct.getKind() == TypeKind.ARRAY && types.isSubtype(ct, iterable))
                    return varNamesForType(ct, types, elements);
                List<String> vnct = new ArrayList<String>();
                for (String name : varNamesForType(ct, types, elements))
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
                return Collections.<String>singletonList(type.toString().toLowerCase());
            case ERROR:
                String tn = ((ErrorType)type).asElement().getSimpleName().toString();
                if (tn.toUpperCase().contentEquals(tn))
                    return Collections.<String>singletonList(tn.toLowerCase());
                StringBuilder sb = new StringBuilder();
                ArrayList<String> al = new ArrayList<String>();
                if ("Iterator".equals(tn)) //NOI18N
                    al.add("it"); //NOI18N
                while((tn = nextName(tn)).length() > 0) {
                    al.add(tn);
                    sb.append(tn.charAt(0));
                }
                if (sb.length() > 0)
                    al.add(sb.toString());
                return al;
            case DECLARED:
                iterable = types.getDeclaredType(elements.getTypeElement("java.lang.Iterable")); //NOI18N
                tn = ((DeclaredType)type).asElement().getSimpleName().toString();
                if (tn.toUpperCase().contentEquals(tn))
                    return Collections.<String>singletonList(tn.toLowerCase());
                sb = new StringBuilder();
                al = new ArrayList<String>();
                if ("Iterator".equals(tn)) //NOI18N
                    al.add("it"); //NOI18N
                while((tn = nextName(tn)).length() > 0) {
                    al.add(tn);
                    sb.append(tn.charAt(0));
                }
                if (types.isSubtype(type, iterable)) {
                    List<? extends TypeMirror> tas = ((DeclaredType)type).getTypeArguments();
                    if (tas.size() > 0) {
                        TypeMirror et = tas.get(0);
                        if (et.getKind() == TypeKind.ARRAY || (et.getKind() != TypeKind.WILDCARD && types.isSubtype(et, iterable))) {
                            al.addAll(varNamesForType(et, types, elements));
                        } else {
                            for (String name : varNamesForType(et, types, elements))
                                al.add(name.endsWith("s") ? name + "es" : name + "s"); //NOI18N
                        }
                    }
                }
                if (sb.length() > 0)
                    al.add(sb.toString());
                return al;
            case WILDCARD:
                TypeMirror bound = ((WildcardType)type).getExtendsBound();
                if (bound == null)
                    bound = ((WildcardType)type).getSuperBound();
                if (bound != null)
                    return varNamesForType(bound, types, elements);
        }
        return Collections.<String>emptyList();
    }
    
    private static String getConstName(String s) {
        StringBuilder sb = new StringBuilder();
        boolean prevUpper = false;
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
            } else if (sb.length() > 0) {
                sb.append(name.subSequence(i, name.length()));
                break;
            }
        }
        return sb.toString();
    }
    
    private static boolean isClashing(String varName, Iterable<? extends Element> locals) {
        if (JavaTokenContext.getKeyword(varName) != null)
            return true;
        for (Element e : locals) {
            if ((e.getKind() == ElementKind.LOCAL_VARIABLE || e.getKind() == ElementKind.PARAMETER || e.getKind() == ElementKind.EXCEPTION_PARAMETER) && varName.contentEquals(e.getSimpleName()))
                return true;
        }
        return false;
    }
    
    private static class SettingsListener implements SettingsChangeListener {

        public void settingsChange(SettingsChangeEvent evt) {
            setCaseSensitive(SettingsUtil.getBoolean(JavaKit.class,
                    ExtSettingsNames.COMPLETION_CASE_SENSITIVE,
                    ExtSettingsDefaults.defaultCompletionCaseSensitive));
        }
    }

    private static class TypeNameVisitor extends SimpleTypeVisitor6<String,Boolean> {
        
        private boolean varArg;
        
        private TypeNameVisitor(boolean varArg) {
            this.varArg = varArg;
        }
        
        @Override
        public String defaultAction(TypeMirror t, Boolean p) {
            return t.toString();
        }
        
        @Override
        public String visitDeclared(DeclaredType t, Boolean p) {
            TypeElement el = (TypeElement)t.asElement();
            if (el == null)
                return "<unknown>"; //NOI18N
            StringBuffer sb = new StringBuffer();
            sb.append(p ? el.getQualifiedName() : el.getSimpleName());
            Iterator<? extends TypeMirror> it = t.getTypeArguments().iterator();
            if (it.hasNext()) {
                sb.append("<"); //NOI18N
                while(it.hasNext()) {
                    sb.append(it.next().accept(this, p));
                    if (it.hasNext())
                        sb.append(", "); //NOI18N
                }
                sb.append(">"); //NOI18N
            }
            return sb.toString();
        }
                        
        @Override
        public String visitArray(ArrayType t, Boolean p) {
            String s = varArg ? "..." : "[]"; //NOI18N
            varArg = false;
            return t.getComponentType().accept(this, p) + s; //NOI18N;
        }

        @Override
        public String visitTypeVariable(TypeVariable t, Boolean p) {
            Element e = t.asElement();
            if (e != null) {
                String name = e.getSimpleName().toString();
                if (!CAPTURED_WILDCARD.equals(name))
                    return name;
            }
            StringBuffer sb = new StringBuffer();
            sb.append("?"); //NOI18N
            TypeMirror bound = t.getLowerBound();
            if (bound != null && bound.getKind() != TypeKind.NULL) {
                sb.append(" super "); //NOI18N
                sb.append(bound.accept(this, p));
            } else {
                bound = t.getUpperBound();
                if (bound != null && bound.getKind() != TypeKind.NULL) {
                    sb.append(" extends "); //NOI18N
                    if (bound.getKind() == TypeKind.TYPEVAR)
                        bound = ((TypeVariable)bound).getLowerBound();
                    sb.append(bound.accept(this, p));
                }
            }
            return sb.toString();
        }

        @Override
        public String visitWildcard(WildcardType t, Boolean p) {
            StringBuffer sb = new StringBuffer();
            sb.append("?"); //NOI18N
            TypeMirror bound = t.getSuperBound();
            if (bound == null) {
                bound = t.getExtendsBound();
                if (bound != null) {
                    sb.append(" extends "); //NOI18N
                    if (bound.getKind() == TypeKind.WILDCARD)
                        bound = ((WildcardType)bound).getSuperBound();
                    sb.append(bound.accept(this, p));
                }
            } else {
                sb.append(" super "); //NOI18N
                sb.append(bound.accept(this, p));
            }
            return sb.toString();
        }

        public String visitError(ErrorType t, Boolean p) {
            TypeElement te = (TypeElement) t.asElement();
            if (te == null)
                return null;
            return (p ? te.getQualifiedName() : te.getSimpleName()).toString();
        }        
    }
    
    private static class ElementNameVisitor extends SimpleElementVisitor6<String,Boolean> {
        
	@Override
        public String visitPackage(PackageElement e, Boolean p) {
            return p ? e.getQualifiedName().toString() : e.getSimpleName().toString();
        }

	@Override
        public String visitType(TypeElement e, Boolean p) {
            return p ? e.getQualifiedName().toString() : e.getSimpleName().toString();
        }        
    }    
}
