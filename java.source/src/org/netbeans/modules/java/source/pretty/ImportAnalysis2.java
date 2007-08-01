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

package org.netbeans.modules.java.source.pretty;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.util.Context;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import org.netbeans.modules.java.source.builder.TreeFactory;

/**
 *
 * @author Jan Lahoda
 */
public class ImportAnalysis2 {

    private Elements elements;
    private TreeFactory make;
    private List<ImportTree> imports;
    private Set<Element> imported;
    private Stack<Set<Element>> visibleThroughClasses;
    private Map<String, Element> simpleNames2Elements;
    private PackageElement unnamedPackage;
    private PackageElement pack;

    public ImportAnalysis2(Context env) {
        elements = JavacElements.instance(env);
        make = TreeFactory.instance(env);
        unnamedPackage = Symtab.instance(env).unnamedPackage;
    }

    public void setPackage(ExpressionTree packageNameTree) {
        if (packageNameTree == null) {
            //if there is no package declaration in the code, unnamedPackage should be used:
            this.pack = unnamedPackage;
            return;
        }

        String packageName = getFQN(packageNameTree);

        this.pack = elements.getPackageElement(packageName);
    }

    public void setImports(List<? extends ImportTree> importsToAdd) {
        imports = new ArrayList<ImportTree>();
        imported = new HashSet<Element>();
        simpleNames2Elements = new HashMap<String, Element>();
        visibleThroughClasses = new Stack<Set<Element>>();

        for (ImportTree imp : importsToAdd) {
            addImport(imp, false);
        }
    }

    public List<? extends ImportTree> getImports() {
        return imports;
    }

    public void classEntered(ClassTree clazz) {
        Set<Element> visible = new HashSet<Element>();

        addAll(clazz.getExtendsClause(), visible);

        for (Tree t : clazz.getImplementsClause()) {
            addAll(t, visible);
        }

        visibleThroughClasses.push(visible);
    }

    public void classLeft() {
        visibleThroughClasses.pop();
    }

    private String getFQN(ImportTree imp) {
        return getFQN(imp.getQualifiedIdentifier());
    }

    private String getFQN(Tree expression) {
        final StringBuffer result = new StringBuffer();

        new TreeScanner<Void, Void>() {

            @Override
            public Void visitMemberSelect(MemberSelectTree tree, Void p) {
                super.visitMemberSelect(tree, p);
                result.append('.');
                result.append(tree.getIdentifier().toString());
                return null;
            }

            @Override
            public Void visitIdentifier(IdentifierTree tree, Void p) {
                result.append(tree.getName().toString());
                return null;
            }
        }.scan(expression, null);

        return result.toString();
    }

    private void addImport(ImportTree imp, boolean sort) {
        String fqn = getFQN(imp);

        if (!imp.isStatic()) {
            TypeElement resolve = elements.getTypeElement(fqn);

            if (resolve != null) {
                imported.add(resolve);
                simpleNames2Elements.put(resolve.getSimpleName().toString(), resolve);
            } else {
                //.*?:
                if (fqn.endsWith(".*")) {
                    fqn = fqn.substring(0, fqn.length() - 2);

                    List<TypeElement> classes = Collections.<TypeElement>emptyList();
                    TypeElement clazz = elements.getTypeElement(fqn);

                    if (clazz != null) {
                        classes = ElementFilter.typesIn(clazz.getEnclosedElements());
                    } else {
                        PackageElement pack = elements.getPackageElement(fqn);

                        if (pack != null) {
                            classes = ElementFilter.typesIn(pack.getEnclosedElements());
                        } else {
                            //cannot resolve - the imports will probably not work correctly...
                        }
                    }

                    for (TypeElement te : classes) {
                        imported.add(te);
                        simpleNames2Elements.put(te.getSimpleName().toString(), te);
                    }
                } else {
                    //cannot resolve - the imports will probably not work correctly...
                }
            }
        } else {
            int dot = fqn.lastIndexOf('.');

            if (dot != (-1)) {
                String className = fqn.substring(0, dot);
                String memberName = fqn.substring(dot + 1);
                boolean isStarred = "*".equals(memberName);
                TypeElement resolved = elements.getTypeElement(className);

                if (resolved != null) {
                    for (Element e : resolved.getEnclosedElements()) {
                        if (!e.getModifiers().contains(Modifier.STATIC)) {
                            continue;
                        }
                        if (isStarred || memberName.contains(e.getSimpleName().toString())) {
                            imported.add(e);
                            simpleNames2Elements.put(e.getSimpleName().toString(), e);
                        }
                    }
                } else {
                    //cannot resolve - the imports will probably not work correctly...
                }
            } else {
                //no dot?
            }
        }

        if (!sort) {
            imports.add(imp);
        } else {
            //not very efficient to compute FQNs again and again:
            int point = -1;

            for (int cntr = 0; cntr < imports.size(); cntr++) {
                String currentFQN = getFQN(imports.get(cntr));

                if (currentFQN.compareTo(fqn) < 0) {
                    point = cntr;
                } else {
                    //== 0 should never happen
                    break;
                }
            }

            imports.add(point + 1, imp);
        }
    }

    //Note: this method should return either "orig" or a IdentifierTree or MemberSelectTree
    //no other tree type is not allowed - see ImmutableTreeTranslator.translateStable(Tree)
    public ExpressionTree resolveImport(MemberSelectTree orig, Element element) {
        if (visibleThroughClasses == null) {
            //may happen for package clause
            return orig;
        }
        //if type is already accessible, do not import:
        for (Set<Element> els : visibleThroughClasses) {
            if (els.contains(element)) {
                return make.Identifier(element.getSimpleName());
            }
        }

        //in the same package:
        if (element.getKind().isClass() || element.getKind().isInterface()) {
            Element parent = element.getEnclosingElement();

            if (pack != null && pack.equals(parent)) {
                //in the same package:
                return make.Identifier(element.getSimpleName());
            }
        }

        if (imported.contains(element)) {
            return make.Identifier(element.getSimpleName());
        }

        Element alreadyImported = simpleNames2Elements.get(element.getSimpleName().toString());

        if (alreadyImported != null && !element.equals(alreadyImported)) {
            // clashing import, use FQN - no need to continue with QualIdent,
            // make MemberSelectTree
            // (see issue #111024 for details)
            return make.MemberSelect(orig.getExpression(), orig.getIdentifier());
        }

        //no creation of static imports yet, import class for fields and methods:
        if (!element.getKind().isClass() && !element.getKind().isInterface()) {
            ExpressionTree clazz = orig.getExpression();

            if (clazz.getKind() == Kind.MEMBER_SELECT) {
                clazz = resolveImport((MemberSelectTree) clazz, element.getEnclosingElement());
            }
            return make.MemberSelect(clazz, orig.getIdentifier());
        }

        TypeElement type = (TypeElement) element;

        //check for java.lang:
        Element parent = type.getEnclosingElement();

        if (parent.getKind() == ElementKind.PACKAGE) {
            if ("java.lang".equals(((PackageElement) parent).getQualifiedName().toString())) {
                return make.Identifier(element.getSimpleName());
            }
        }

        Tree imp = make.QualIdentImpl(element);
        addImport(make.Import(imp, false), true);

        return make.Identifier(element.getSimpleName());
    }

    private void addAll(Tree t, Set<Element> visible) {
        if (t == null) {
            return;
        }
        Element e = null;

        if (t.getKind() == Kind.MEMBER_SELECT) {
            e = ((JCFieldAccess) t).sym;
        } else {
            if (t.getKind() == Kind.IDENTIFIER) {
                e = ((JCIdent) t).sym;
            }
        }

        if (e == null) {
            return;
        }
        visible.addAll(e.getEnclosedElements());
    }
}
