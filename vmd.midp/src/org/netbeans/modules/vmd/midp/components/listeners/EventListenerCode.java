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
package org.netbeans.modules.vmd.midp.components.listeners;

import com.sun.source.tree.*;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import org.netbeans.api.java.source.*;
import org.netbeans.modules.vmd.api.codegen.*;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;
import org.netbeans.modules.vmd.midp.components.sources.CommandEventSourceCD;
import org.netbeans.modules.vmd.midp.components.sources.ItemCommandEventSourceCD;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.swing.text.StyledDocument;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * @author David Kaspar
 */
class EventListenerCode {

    static class CodeImplementsPresenter extends CodeGlobalLevelPresenter {

        private String className;
        private String methodName;
        private String[] parameters;

        public CodeImplementsPresenter (String className, String methodName, String... parameters) {
            this.className = className;
            this.methodName = methodName;
            this.parameters = parameters;
        }

        protected void performGlobalGeneration (StyledDocument styledDocument) {
            JavaSource source = JavaSource.forDocument (styledDocument);
            try {
                ModificationResult result = source.runModificationTask (new CancellableTask<WorkingCopy>() {
                    public void cancel () {
                    }

                    public void run (WorkingCopy workingCopy) throws Exception {
                        workingCopy.toPhase (JavaSource.Phase.ELEMENTS_RESOLVED);
                        CompilationUnitTree compilationUnit = workingCopy.getCompilationUnit ();
                        TreeMaker treeMaker = workingCopy.getTreeMaker ();
                        Trees trees = workingCopy.getTrees ();

                        ContainsMethodTreeVisitor visitor = new ContainsMethodTreeVisitor (trees, methodName, parameters);
                        visitor.scan (compilationUnit, null);
                        TreePath classTreePath = visitor.getClassTreePath ();
                        if (classTreePath != null) {
                            ExpressionTree expressionTree = findImplementIdentifier (trees, classTreePath, className);
                            if (visitor.isMethodExists ()) {
                                if (expressionTree == null) {
                                    TypeElement typeElement = workingCopy.getElements ().getTypeElement (className);
                                    ExpressionTree implementsClause = typeElement != null ? treeMaker.QualIdent (typeElement) : treeMaker.Identifier (className);
                                    ClassTree oldClassTree = (ClassTree) classTreePath.getLeaf ();
                                    ClassTree newClassTree = treeMaker.addClassImplementsClause (oldClassTree, implementsClause);
                                    workingCopy.rewrite (oldClassTree, newClassTree);
                                }
                            } else {
                                if (expressionTree != null) {
                                    ClassTree oldClassTree = (ClassTree) classTreePath.getLeaf ();
                                    ClassTree newClassTree = treeMaker.removeClassImplementsClause (oldClassTree, expressionTree);
                                    workingCopy.rewrite (oldClassTree, newClassTree);
                                }
                            }
                        }
                    }
                });
                result.commit ();
            } catch (IOException e) {
                throw Debug.error (e);
            }
        }

    }

    private static ExpressionTree findImplementIdentifier (Trees trees, TreePath classTreePath, String fullyQualifiedName) {
        ClassTree clazz = (ClassTree) classTreePath.getLeaf ();

        for (Tree tree : clazz.getImplementsClause ()) {
            Element element = trees.getElement (new TreePath (classTreePath, tree));
            if (equalsElementWithFQN (element, fullyQualifiedName))
                return (ExpressionTree) tree;
        }

        return null;
    }

    private static boolean equalsElementWithFQN (Element element, String fullyQualifiedName) {
        if (element.getKind ().isInterface () || element.getKind ().isClass ()) {
            TypeElement type = (TypeElement) element;
            if (type.getQualifiedName ().contentEquals (fullyQualifiedName))
                return true;
        }
        return false;
    }

    private static class ContainsMethodTreeVisitor extends TreePathScanner<Void, Void> {

        private Trees trees;
        private String methodName;
        private String[] parameters;

        private boolean isFirstLevel;
        private TreePath classTreePath;
        private boolean methodExists;

        public ContainsMethodTreeVisitor (Trees trees, String methodName, String... parameters) {
            this.trees = trees;
            this.methodName = methodName;
            this.parameters = parameters;

            methodExists = false;
            isFirstLevel = false;
        }

        public boolean isMethodExists () {
            return methodExists;
        }

        public TreePath getClassTreePath () {
            return classTreePath;
        }

        @Override
        public Void visitClass (ClassTree node, Void p) {
            if (! isFirstLevel) {
                isFirstLevel = true;
                if (node.getModifiers ().getFlags ().contains (Modifier.PUBLIC)) {
                    classTreePath = getCurrentPath ();
                    return super.visitClass (node, p);
                }
                isFirstLevel = false;
            }
            return null;
        }

        @Override
        public Void visitMethod (MethodTree node, Void p) {
            if (node.getName ().contentEquals (methodName)) {
                List<? extends VariableTree> parameters = node.getParameters ();
                if (this.parameters.length == parameters.size ()) {
                    boolean corrent = true;
                    for (int i = 0; i < this.parameters.length; i ++) {
                        Tree type = parameters.get (i).getType ();
                        TreePath treePath = new TreePath (getCurrentPath (), type);
                        Element element = trees.getElement (treePath);
                        if (! equalsElementWithFQN (element, this.parameters[i])) {
                            corrent = false;
                            break;
                        }
                    }
                    if (corrent)
                        methodExists = true;
                }
            }
            return null;
        }

        @Override
        public Void visitVariable (VariableTree node, Void p) {
            return null;
        }

    }

    public static class CodeCommandListenerPresenter extends CodeClassLevelPresenter.Adapter {

        @Override
        protected void generateClassBodyCode (StyledDocument document) {
            List<DesignComponent> sources = DocumentSupport.gatherAllComponentsOfTypeID (getComponent ().getDocument (), CommandEventSourceCD.TYPEID);
            if (sources.size () == 0)
                return;

            MultiGuardedSection section = MultiGuardedSection.create (document, getComponent ().getComponentID () + "-commandAction"); // NOI18N
            section.getWriter ().write ("//<editor-fold defaultstate=\"collapsed\" desc=\" Generated Method: commandAction for Displayables \">\n"); // NOI18N
            section.getWriter ().write ("/**\n * Called by a system to indicated that a command has been invoked on a particular displayable.\n * @param command the Command that was invoked\n * @param displayable the Displayable where the command was invoked\n */\n"); // NOI18N
            section.getWriter ().write ("public void commandAction (Command command, Displayable displayable) {\n").commit (); // NOI18N

            section.switchToEditable (getComponent ().getComponentID () + "-preCommandAction"); // NOI18N
            section.getWriter ().write (" // write pre-action user code here\n").commit (); // NOI18N
            section.switchToGuarded ();

            resolveFirstLevel (section, sources);

            section.switchToEditable (getComponent ().getComponentID () + "-postCommandAction"); // NOI18N
            section.getWriter ().write (" // write post-action user code here\n").commit (); // NOI18N
            section.switchToGuarded ();

            section.getWriter ().write ("}\n"); // NOI18N
            section.getWriter ().write ("//</editor-fold>\n").commit (); // NOI18N
            section.close ();
        }

        private void resolveFirstLevel (MultiGuardedSection section, List<DesignComponent> sources) {
            HashMap<String, ArrayList<DesignComponent>> displayables2sources = gatherDisplayables (sources);
            ArrayList<String> displayables = new ArrayList<String> (displayables2sources.keySet ());
            Collections.sort (displayables);

            for (int i = 0; i < displayables.size (); i ++) {
                String displayable = displayables.get (i);
                if (i > 0)
                    section.getWriter ().write ("else "); // NOI18N
                section.getWriter ().write ("if (displayable == " + displayable + ") {\n"); // NOI18N

                resolveSecondLevel (section, displayables2sources.get (displayable));

                assert section.isGuarded ();
                if (i < displayables.size () - 1)
                    section.getWriter ().write ("} "); // NOI18N
                else
                    section.getWriter ().write ("}\n"); // NOI18N
            }

            section.getWriter ().commit ();
        }

        private void resolveSecondLevel (MultiGuardedSection section, List<DesignComponent> sources) {
            HashMap<String, ArrayList<DesignComponent>> commands2sources = gatherCommands (sources);
            ArrayList<String> commands = new ArrayList<String> (commands2sources.keySet ());
            Collections.sort (commands);

            for (int i = 0; i < commands.size (); i ++) {
                String command = commands.get (i);
                if (i > 0)
                    section.getWriter ().write ("else "); // NOI18N
                section.getWriter ().write ("if (command == " + command + ") {\n"); // NOI18N

                for (DesignComponent source : commands2sources.get (command))
                    CodeMultiGuardedLevelPresenter.generateMultiGuardedSectionCode (section, source);

                assert section.isGuarded ();
                if (i < commands.size () - 1)
                    section.getWriter ().write ("} "); // NOI18N
                else
                    section.getWriter ().write ("}\n"); // NOI18N
            }
        }

        private HashMap<String, ArrayList<DesignComponent>> gatherDisplayables (List<DesignComponent> sources) {
            HashMap<String, ArrayList<DesignComponent>> d2s = new HashMap<String, ArrayList<DesignComponent>> ();
            for (DesignComponent source : sources) {
                String displayable = CodeReferencePresenter.generateDirectAccessCode (source.readProperty (CommandEventSourceCD.PROP_DISPLAYABLE).getComponent ());
                ArrayList<DesignComponent> s = d2s.get (displayable);
                if (s == null) {
                    s = new ArrayList<DesignComponent> ();
                    d2s.put (displayable, s);
                }
                s.add (source);
            }
            return d2s;
        }

        private HashMap<String, ArrayList<DesignComponent>> gatherCommands (List<DesignComponent> sources) {
            HashMap<String, ArrayList<DesignComponent>> c2s = new HashMap<String, ArrayList<DesignComponent>> ();
            for (DesignComponent source : sources) {
                String command = CodeReferencePresenter.generateDirectAccessCode (source.readProperty (CommandEventSourceCD.PROP_COMMAND).getComponent ());
                ArrayList<DesignComponent> s = c2s.get (command);
                if (s == null) {
                    s = new ArrayList<DesignComponent> ();
                    c2s.put (command, s);
                }
                s.add (source);
            }
            return c2s;
        }

    }

    public static class CodeItemCommandListenerPresenter extends CodeClassLevelPresenter.Adapter {

        @Override
        protected void generateClassBodyCode (StyledDocument document) {
            List<DesignComponent> sources = DocumentSupport.gatherAllComponentsOfTypeID (getComponent ().getDocument (), ItemCommandEventSourceCD.TYPEID);
            if (sources.size () == 0)
                return;

            MultiGuardedSection section = MultiGuardedSection.create (document, getComponent ().getComponentID () + "-itemCommandAction"); // NOI18N
            section.getWriter ().write ("//<editor-fold defaultstate=\"collapsed\" desc=\" Generated Method: commandAction for Items \">\n"); // NOI18N
            section.getWriter ().write ("/**\n * Called by a system to indicated that a command has been invoked on a particular item.\n * @param command the Command that was invoked\n * @param displayable the Item where the command was invoked\n */\n"); // NOI18N
            section.getWriter ().write ("public void commandAction (Command command, Item item) {\n").commit (); // NOI18N

            section.switchToEditable (getComponent ().getComponentID () + "-preItemCommandAction"); // NOI18N
            section.getWriter ().write (" // write pre-action user code here\n").commit (); // NOI18N
            section.switchToGuarded ();

            resolveFirstLevel (section, sources);

            section.switchToEditable (getComponent ().getComponentID () + "-postItemCommandAction"); // NOI18N
            section.getWriter ().write (" // write post-action user code here\n").commit (); // NOI18N
            section.switchToGuarded ();

            section.getWriter ().write ("}\n"); // NOI18N
            section.getWriter ().write ("//</editor-fold>\n").commit (); // NOI18N
            section.close ();
        }

        private void resolveFirstLevel (MultiGuardedSection section, List<DesignComponent> sources) {
            HashMap<String, ArrayList<DesignComponent>> items2sources = gatherItems (sources);
            ArrayList<String> items = new ArrayList<String> (items2sources.keySet ());
            Collections.sort (items);

            for (int i = 0; i < items.size (); i ++) {
                String item = items.get (i);
                if (i > 0)
                    section.getWriter ().write ("else "); // NOI18N
                section.getWriter ().write ("if (item == " + item + ") {\n"); // NOI18N

                resolveSecondLevel (section, items2sources.get (item));

                assert section.isGuarded ();
                if (i < items.size () - 1)
                    section.getWriter ().write ("} "); // NOI18N
                else
                    section.getWriter ().write ("}\n"); // NOI18N
            }

            section.getWriter ().commit ();
        }

        private void resolveSecondLevel (MultiGuardedSection section, List<DesignComponent> sources) {
            HashMap<String, ArrayList<DesignComponent>> commands2sources = gatherCommands (sources);
            ArrayList<String> commands = new ArrayList<String> (commands2sources.keySet ());
            Collections.sort (commands);

            for (int i = 0; i < commands.size (); i ++) {
                String command = commands.get (i);
                if (i > 0)
                    section.getWriter ().write ("else "); // NOI18N
                section.getWriter ().write ("if (command == " + command + ") {\n"); // NOI18N

                for (DesignComponent source : commands2sources.get (command))
                    CodeMultiGuardedLevelPresenter.generateMultiGuardedSectionCode (section, source);

                assert section.isGuarded ();
                if (i < commands.size () - 1)
                    section.getWriter ().write ("} "); // NOI18N
                else
                    section.getWriter ().write ("}\n"); // NOI18N
            }
        }

        private HashMap<String, ArrayList<DesignComponent>> gatherItems (List<DesignComponent> sources) {
            HashMap<String, ArrayList<DesignComponent>> i2s = new HashMap<String, ArrayList<DesignComponent>> ();
            for (DesignComponent source : sources) {
                String item = CodeReferencePresenter.generateDirectAccessCode (ItemCommandEventSourceCD.getItemComponent (source));
                ArrayList<DesignComponent> s = i2s.get (item);
                if (s == null) {
                    s = new ArrayList<DesignComponent> ();
                    i2s.put (item, s);
                }
                s.add (source);
            }
            return i2s;
        }

        private HashMap<String, ArrayList<DesignComponent>> gatherCommands (List<DesignComponent> sources) {
            HashMap<String, ArrayList<DesignComponent>> c2s = new HashMap<String, ArrayList<DesignComponent>> ();
            for (DesignComponent source : sources) {
                String command = CodeReferencePresenter.generateDirectAccessCode (source.readProperty (CommandEventSourceCD.PROP_COMMAND).getComponent ());
                ArrayList<DesignComponent> s = c2s.get (command);
                if (s == null) {
                    s = new ArrayList<DesignComponent> ();
                    c2s.put (command, s);
                }
                s.add (source);
            }
            return c2s;
        }

    }

}
