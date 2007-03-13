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

package org.netbeans.modules.java.navigation;

import java.awt.Rectangle;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.lang.model.element.Element;
import javax.swing.JEditorPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import static javax.lang.model.element.ElementKind.*;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import static javax.lang.model.type.TypeKind.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;

/**
 *
 * @author Sandip Chitale (Sandip.Chitale@Sun.Com)
 */
class Utils {
    static String format(Element element) {
        return format(element, false);
    }

    static String format(Element element, boolean forSignature) {
        StringBuilder stringBuilder = new StringBuilder();
        format(element, stringBuilder, forSignature);

        return stringBuilder.toString();
    }

    static void format(Element element, StringBuilder stringBuilder, boolean forSignature) {
        if (element == null) {
            return;
        }

        boolean first = true;
        Set<Modifier> modifiers = element.getModifiers();

        switch (element.getKind()) {
        case PACKAGE:

            PackageElement packageElement = (PackageElement) element;
            if (forSignature) {
                stringBuilder.append("package "); // NOI18N
            }
            stringBuilder.append(packageElement.getQualifiedName());
            break;

        case CLASS:
        case INTERFACE:
        case ENUM:
            if (forSignature) {
                stringBuilder.append(toString(modifiers));
                if (modifiers.size() > 0) {
                    if (stringBuilder.length() > 0) {
                        stringBuilder.append(" ");
                    }
                }
            }
            
            if (forSignature) {
                switch (element.getKind()) {
                    case CLASS:
                        stringBuilder.append("class "); // NOI18N
                        break;
                    case INTERFACE:
                        stringBuilder.append("interface "); // NOI18N
                        break;
                    case ENUM:
                        stringBuilder.append("enum "); // NOI18N
                        break;
                }
            }
            
            TypeElement typeElement = (TypeElement) element;
            stringBuilder.append(JavaMembersAndHierarchyOptions.isShowFQN()
                ? typeElement.getQualifiedName().toString()
                : typeElement.getSimpleName().toString());

            formatTypeParameters(typeElement.getTypeParameters(), stringBuilder);

            if (!forSignature) {
                if (modifiers.size() > 0) {
                    stringBuilder.append(" : ");
                    stringBuilder.append(toString(modifiers));
                }
            }

            break;

        case CONSTRUCTOR:
            if (forSignature) {
                stringBuilder.append(toString(modifiers));
                if (modifiers.size() > 0) {
                    if (stringBuilder.length() > 0) {
                        stringBuilder.append(" ");
                    }
                }
            }

            ExecutableElement constructorElement = (ExecutableElement) element;
            stringBuilder.append(constructorElement.getEnclosingElement()
                                                   .getSimpleName().toString());
            stringBuilder.append("(");
            formatVariableElements(constructorElement.getParameters(),
                constructorElement.isVarArgs(), stringBuilder);
            stringBuilder.append(")");

            List<? extends TypeMirror> thrownTypesMirrors = constructorElement.getThrownTypes();
            if (!thrownTypesMirrors.isEmpty()) {
                stringBuilder.append(" throws "); // NOI18N
                formatTypeMirrors(thrownTypesMirrors, stringBuilder);
            }
            if (!forSignature) {
                if (modifiers.size() > 0) {
                    stringBuilder.append(":");
                    stringBuilder.append(toString(modifiers));
                }
            }

            break;

        case METHOD:
            ExecutableElement methodElement = (ExecutableElement) element;
            TypeMirror returnTypeMirror = methodElement.getReturnType();
            List<?extends TypeParameterElement> typeParameters = methodElement.getTypeParameters();

            if (forSignature) {
                stringBuilder.append(toString(modifiers));
                
                if (modifiers.size() > 0) {
                    if (stringBuilder.length() > 0) {
                        stringBuilder.append(" ");
                    }
                }

                if ((typeParameters != null) && (typeParameters.size() > 0)) {
                    formatTypeParameters(typeParameters, stringBuilder);
                    if (stringBuilder.length() > 0) {
                        stringBuilder.append(" ");
                    }
                }



                formatTypeMirror(returnTypeMirror, stringBuilder);
            }

            if (stringBuilder.length() > 0) {
                    stringBuilder.append(" ");
            }

            stringBuilder.append(methodElement.getSimpleName().toString());
            stringBuilder.append("(");
            formatVariableElements(methodElement.getParameters(),
                methodElement.isVarArgs(), stringBuilder);
            stringBuilder.append(")");

            List<? extends TypeMirror> thrownTypesMirrorsByMethod = methodElement.getThrownTypes();
            if (!thrownTypesMirrorsByMethod.isEmpty()) {
                stringBuilder.append(" throws "); // NOI18N
                formatTypeMirrors(thrownTypesMirrorsByMethod, stringBuilder);
            }

            if (!forSignature) {
                stringBuilder.append(":");

                if (modifiers.size() > 0) {
                    stringBuilder.append(toString(modifiers));
                    if (modifiers.size() > 0) {
                        stringBuilder.append(" ");
                    }
                }

                formatTypeMirror(returnTypeMirror, stringBuilder);

                if ((typeParameters != null) && (typeParameters.size() > 0)) {
                    stringBuilder.append(":");
                    formatTypeParameters(typeParameters, stringBuilder);
                }

                if (JavaMembersAndHierarchyOptions.isShowInherited()) {
                    stringBuilder.append(" [");
                    stringBuilder.append(element.getEnclosingElement());
                    stringBuilder.append("]");
                }
            }

            break;

        case TYPE_PARAMETER:
            TypeParameterElement typeParameterElement = (TypeParameterElement) element;
            stringBuilder.append(typeParameterElement.getSimpleName());

            List<?extends TypeMirror> bounds = null;
            try {
                bounds = typeParameterElement.getBounds();
                if ((bounds != null) && (bounds.size() > 0)) {
                    if (bounds.size() == 1 && "java.lang.Object".equals( bounds.get(0).toString())) { // NOI18N
                    } else {
                        stringBuilder.append(" extends "); // NOI18N
                        first = true;
                        for (TypeMirror typeMirror : bounds) {
                            if (first) {
                                first = false;
                            } else {
                                stringBuilder.append(" & "); // NOI18N
                            }
                            formatTypeMirror(typeMirror, stringBuilder);
                        }
                    }
                }
            } catch (NullPointerException npe) {
                // Bug?
            }

            break;

        case FIELD:
            VariableElement fieldElement = (VariableElement) element;
            if (forSignature) {
                stringBuilder.append(toString(modifiers));

                if (stringBuilder.length() > 0) {
                    stringBuilder.append(" ");
                }

                formatTypeMirror(fieldElement.asType(), stringBuilder);
            }

            if (stringBuilder.length() > 0) {
                stringBuilder.append(" ");
            }

            stringBuilder.append(fieldElement.getSimpleName().toString());

            if (!forSignature) {
                stringBuilder.append(":");
                if (modifiers.size() > 0) {
                    stringBuilder.append(toString(modifiers));
                }

                if (stringBuilder.length() > 0) {
                    stringBuilder.append(" ");
                }

                formatTypeMirror(fieldElement.asType(), stringBuilder);

                if (JavaMembersAndHierarchyOptions.isShowInherited()) {
                    stringBuilder.append(" [");
                    stringBuilder.append(element.getEnclosingElement());
                    stringBuilder.append("]");
                }
            }

            break;

        case ENUM_CONSTANT:
            stringBuilder.append(element.toString());

            if (modifiers.size() > 0) {
                stringBuilder.append(":");
                stringBuilder.append(toString(modifiers));
            }

            if (JavaMembersAndHierarchyOptions.isShowInherited()) {
                stringBuilder.append(" [");
                stringBuilder.append(element.getEnclosingElement());
                stringBuilder.append("]");
            }

            break;

        case PARAMETER:
            VariableElement variableElement = (VariableElement) element;
            formatTypeMirror(variableElement.asType(), stringBuilder);
            stringBuilder.append(" ");
            stringBuilder.append(element.getSimpleName().toString());

            break;
        }
    }

    static void formatTypeMirror(TypeMirror typeMirror,
        StringBuilder stringBuilder) {
        if (typeMirror == null) {
            return;
        }

        boolean first = true;

        switch (typeMirror.getKind()) {
        case BOOLEAN:
        case BYTE:
        case CHAR:
        case DOUBLE:
        case FLOAT:
        case INT:
        case LONG:
        case NONE:
        case NULL:
        case SHORT:
        case VOID:
            stringBuilder.append(typeMirror);

            break;

        case TYPEVAR:
            TypeVariable typeVariable = (TypeVariable)typeMirror;
            stringBuilder.append(typeVariable.asElement().getSimpleName().toString());
            break;

        case WILDCARD:
            WildcardType wildcardType = (WildcardType)typeMirror;
            stringBuilder.append("?");
            if ( wildcardType.getExtendsBound() != null ) {
                stringBuilder.append(" extends "); // NOI18N
                formatTypeMirror(wildcardType.getExtendsBound(), stringBuilder);
            }
            if ( wildcardType.getSuperBound() != null ) {
                stringBuilder.append(" super "); // NOI18N
                formatTypeMirror(wildcardType.getSuperBound(), stringBuilder);
            }

            break;

        case DECLARED:
            DeclaredType declaredType = (DeclaredType) typeMirror;
            Element element = declaredType.asElement();
            if (element instanceof TypeElement) {
                stringBuilder.append(
                    JavaMembersAndHierarchyOptions.isShowFQN() ?
                    ((TypeElement)element).getQualifiedName().toString() :
                    element.getSimpleName().toString());
            } else {
                stringBuilder.append(element.getSimpleName().toString());
            }
            List<? extends TypeMirror> typeArgs = declaredType.getTypeArguments();
            if ( !typeArgs.isEmpty() ) {
                stringBuilder.append("<");
                formatTypeMirrors(typeArgs, stringBuilder);
                stringBuilder.append(">");
            }

            break;

        case ARRAY:

            int dims = 0;

            while (typeMirror.getKind() == ARRAY) {
                dims++;
                typeMirror = ((ArrayType) typeMirror).getComponentType();
            }

            formatTypeMirror(typeMirror, stringBuilder);

            for (int i = 0; i < dims; i++) {
                stringBuilder.append("[]");
            }

            break;
        }
    }

    static void formatTypeParameters(
        List<? extends TypeParameterElement> typeParameters,
        StringBuilder stringBuilder) {
        if ((typeParameters == null) || (typeParameters.size() == 0)) {
            return;
        }

        boolean first = true;

        if (typeParameters.size() > 0) {
            stringBuilder.append("<");
            first = true;

            for (TypeParameterElement typeParameterElement : typeParameters) {
                if (first) {
                    first = false;
                } else {
                    stringBuilder.append(", ");
                }

                format(typeParameterElement, stringBuilder, false);
            }

            stringBuilder.append(">");
        }
    }

    static void formatVariableElements(
        List<? extends VariableElement> variableElements, boolean varArgs,
        StringBuilder stringBuilder) {
        if ((variableElements == null) || (variableElements.size() == 0)) {
            return;
        }

        boolean first = true;

        for (VariableElement variableElement : variableElements) {
            if (first) {
                first = false;
            } else {
                stringBuilder.append(", ");
            }

           format(variableElement, stringBuilder, false);
        }

        if (varArgs) {
            stringBuilder.append("...");
        }
    }

    static void formatTypeMirrors(List<?extends TypeMirror> thrownTypeMirros,
        StringBuilder stringBuilder) {
        if ((thrownTypeMirros == null) || (thrownTypeMirros.size() == 0)) {
            return;
        }

        boolean first = true;

        for (TypeMirror typeMirror : thrownTypeMirros) {
            if (first) {
                first = false;
            } else {
                stringBuilder.append(", ");
            }

            formatTypeMirror(typeMirror, stringBuilder);
        }
    }

    static int getIntModifiers(Set<Modifier> modifiers) {
        int intModifiers = 0;

        if (modifiers.contains(Modifier.ABSTRACT)) {
            intModifiers |= java.lang.reflect.Modifier.ABSTRACT;
        }

        if (modifiers.contains(Modifier.FINAL)) {
            intModifiers |= java.lang.reflect.Modifier.FINAL;
        }

        if (modifiers.contains(Modifier.NATIVE)) {
            intModifiers |= java.lang.reflect.Modifier.NATIVE;
        }

        if (modifiers.contains(Modifier.PRIVATE)) {
            intModifiers |= java.lang.reflect.Modifier.PRIVATE;
        }

        if (modifiers.contains(Modifier.PROTECTED)) {
            intModifiers |= java.lang.reflect.Modifier.PROTECTED;
        }

        if (modifiers.contains(Modifier.PUBLIC)) {
            intModifiers |= java.lang.reflect.Modifier.PUBLIC;
        }

        if (modifiers.contains(Modifier.STATIC)) {
            intModifiers |= java.lang.reflect.Modifier.STATIC;
        }

        if (modifiers.contains(Modifier.STRICTFP)) {
            intModifiers |= java.lang.reflect.Modifier.STRICT;
        }

        if (modifiers.contains(Modifier.SYNCHRONIZED)) {
            intModifiers |= java.lang.reflect.Modifier.SYNCHRONIZED;
        }

        if (modifiers.contains(Modifier.TRANSIENT)) {
            intModifiers |= java.lang.reflect.Modifier.TRANSIENT;
        }

        if (modifiers.contains(Modifier.VOLATILE)) {
            intModifiers |= java.lang.reflect.Modifier.VOLATILE;
        }

        return intModifiers;
    }

    static String toString(Set<Modifier> modifiers) {
        return java.lang.reflect.Modifier.toString(getIntModifiers(modifiers));
    }

    static Set<Modifier> getModifiers(int intModifiers) {
        EnumSet<Modifier> modifiers = EnumSet.noneOf(Modifier.class);

        if ((intModifiers & java.lang.reflect.Modifier.ABSTRACT) != 0) {
            modifiers.add(Modifier.ABSTRACT);
        }

        if ((intModifiers & java.lang.reflect.Modifier.FINAL) != 0) {
            modifiers.add(Modifier.FINAL);
        }

        if ((intModifiers & java.lang.reflect.Modifier.NATIVE) != 0) {
            modifiers.add(Modifier.NATIVE);
        }

        if ((intModifiers & java.lang.reflect.Modifier.PRIVATE) != 0) {
            modifiers.add(Modifier.PRIVATE);
        }

        if ((intModifiers & java.lang.reflect.Modifier.PROTECTED) != 0) {
            modifiers.add(Modifier.PROTECTED);
        }

        if ((intModifiers & java.lang.reflect.Modifier.PUBLIC) != 0) {
            modifiers.add(Modifier.PUBLIC);
        }

        if ((intModifiers & java.lang.reflect.Modifier.STATIC) != 0) {
            modifiers.add(Modifier.STATIC);
        }

        if ((intModifiers & java.lang.reflect.Modifier.STRICT) != 0) {
            modifiers.add(Modifier.STRICTFP);
        }

        if ((intModifiers & java.lang.reflect.Modifier.SYNCHRONIZED) != 0) {
            modifiers.add(Modifier.SYNCHRONIZED);
        }

        if ((intModifiers & java.lang.reflect.Modifier.TRANSIENT) != 0) {
            modifiers.add(Modifier.TRANSIENT);
        }

        if ((intModifiers & java.lang.reflect.Modifier.VOLATILE) != 0) {
            modifiers.add(Modifier.VOLATILE);
        }

        return modifiers;
    }
    
    static boolean patternMatch(JavaElement javaToolsJavaElement, String pattern, String patternLowerCase) {

        if (pattern == null) {
            return true;
        }

        String patternRegexpString = pattern;

        if (pattern.trim().length() == 0) {
            patternRegexpString = pattern + ".*";
        } else {
            // TODO FIx the replacement of * and ? 
            patternRegexpString = pattern.
                    replaceAll(Pattern.quote("*"), Matcher.quoteReplacement(".*")).
                    replaceAll(Pattern.quote("?"), Matcher.quoteReplacement(".")) +
                    (pattern.endsWith("$") ? "" : ".*");
        }

        String name = javaToolsJavaElement.getName();

        try {
            Pattern compiledPattern = Pattern.compile(patternRegexpString,
                    JavaMembersAndHierarchyOptions.isCaseSensitive() ? 0
                                                           : Pattern.CASE_INSENSITIVE);
            Matcher m = compiledPattern.matcher(name);

            return m.matches();
        } catch (PatternSyntaxException pse) {
            if (JavaMembersAndHierarchyOptions.isCaseSensitive()) {
                return name.startsWith(pattern);
            }

            return name.toLowerCase().startsWith(patternLowerCase);
        }
    }
    
    static String getClassName(String className) {
        // Handle generic type names i.e. strip off parameters
        int firstLessThan = className.indexOf('<');

        if (firstLessThan != -1) {
            className = className.substring(0, firstLessThan);
        }

        if (!JavaMembersAndHierarchyOptions.isShowFQN()) {
            int lastDot = className.lastIndexOf('.');

            if (lastDot != -1) {
                className = className.substring(lastDot + 1);
            }
        }

        return className;
    }

    static String getClassNameSansPackage(String className) {
        // Handle generic type names i.e. strip off parameters
        int firstLessThan = className.indexOf('<');

        if (firstLessThan != -1) {
            className = className.substring(0, firstLessThan);
        }

        int lastDot = className.lastIndexOf('.');

        if (lastDot != -1) {
            className = className.substring(lastDot + 1);
        }

        return className;
    }
    
    // Tree movement
    static void firstRow(JTree tree) {
        int rowCount = tree.getRowCount();
        if (rowCount > 0) {
            tree.setSelectionRow(0);
            scrollTreeToSelectedRow(tree);
        }
    }
    
    static void previousRow(JTree tree) {
        int rowCount = tree.getRowCount();
        if (rowCount > 0) {
            int selectedRow = tree.getSelectionModel().getMinSelectionRow();
            if (selectedRow == -1) {
                selectedRow = (rowCount -1);
            } else {
                selectedRow--;
                if (selectedRow < 0) {
                    selectedRow = (rowCount -1);
                }
            }
            tree.setSelectionRow(selectedRow);
            scrollTreeToSelectedRow(tree);
        }
    }
    
    static void nextRow(JTree tree) {
        int rowCount = tree.getRowCount();
        if (rowCount > 0) {
            int selectedRow = tree.getSelectionModel().getMinSelectionRow();
            if (selectedRow == -1) {
                selectedRow = 0;
                tree.setSelectionRow(selectedRow);
            } else {
                selectedRow++;
            }
            tree.setSelectionRow(selectedRow % rowCount);
            scrollTreeToSelectedRow(tree);
        }
    }
    
    static void lastRow(JTree tree) {
        int rowCount = tree.getRowCount();
        if (rowCount > 0) {
            tree.setSelectionRow(rowCount - 1);
            scrollTreeToSelectedRow(tree);
        }
    }
    
    static void scrollTreeToSelectedRow(final JTree tree) {
        final int selectedRow = tree.getLeadSelectionRow();
        if (selectedRow >=0) {
            SwingUtilities.invokeLater(
                    new Runnable() {
                public void run() {
                    tree.scrollRectToVisible(tree.getRowBounds(selectedRow));
                }
            }
            );
        }
    }
    
    // Javadoc Pane
    static void showJavaDoc(JavaElement node, final JEditorPane javaDocPane) {
        javaDocPane.setText("");
        String javaDoc = ((JavaElement) node).getJavaDoc();
        if (javaDoc != null) {
            // TODO Replacements should be (may be) I18Ned.
            javaDoc = javaDoc
                    .replaceAll("@author ",     "<b>Author:</b> ") // NOI18N
                    .replaceAll("@deprecated ", "<b>Deprecated:</b> ")  // NOI18N
                    .replaceAll("@exception ",  "<b>Exception:</b> ") // NOI18N
                    .replaceAll("@param ",      "<b>Parameter:</b> ") // NOI18N
                    .replaceAll("@return ",     "<b>Return:</b> ") // NOI18N
                    .replaceAll("@see ",        "<b>See:</b> ") // NOI18N
                    .replaceAll("@since ",      "<b>Since:</b> ") // NOI18N
                    .replaceAll("@throws ",     "<b>Throws:</b> ") // NOI18N
                    .replaceAll("@version ",    "<b>Version:</b> ") // NOI18N
                    ;
            javaDocPane.setText(
                    "<html>" // NOI18N
                    + "<head>" // NOI18N
                    + "</head>" // NOI18N
                    + "<body>" // NOI18N
                    + javaDoc.replaceAll("\n", "<br>") // NOI18N
                    + "</body>" // NOI18N
                    + "</html>" // NOI18N
                    );
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                javaDocPane.scrollRectToVisible(new Rectangle(0,0,1,1));
            }});
    }
}
