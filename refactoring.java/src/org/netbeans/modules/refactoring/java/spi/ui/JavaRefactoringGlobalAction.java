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
package org.netbeans.modules.refactoring.java.spi.ui;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.List;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Actions;
import org.openide.cookies.EditorCookie;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditorSupport.Pane;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.TopComponent;

/**
 * @author Jan Becicka
 * @author Tim Boudreau
 */
final class JavaRefactoringGlobalAction extends NodeAction {
    private final JavaRefactoringActionDelegate delegate;
    public JavaRefactoringGlobalAction(String name, JavaRefactoringActionDelegate delegate) {
        setName(name);
        this.delegate = delegate;
    }

    public final String getName() {
        return (String) getValue(Action.NAME);
    }

    protected void setName(String name) {
        putValue(Action.NAME, name);
    }

    protected void setMnemonic(char m) {
        putValue(Action.MNEMONIC_KEY, new Integer(m));
    }

    public org.openide.util.HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    protected boolean enable(Lookup context) {
        return delegate.isEnabled(context);
    }

    public void performAction (Lookup context) {
        EditorCookie ec = getEditorCookie(context);
        if (ec != null) {
            new TextComponentRunnable(ec, delegate).run();
        }
    }


    protected Lookup getLookup(Node[] n) {
        InstanceContent ic = new InstanceContent();
        for (Node node:n) {
            ic.add(node);
        }
        Lookup result = new AbstractLookup (ic);
        EditorCookie tc = getEditorCookie(result);
        if (tc != null) {
            ic.add(tc);
        }
        ic.add(new Hashtable(0));
        return result;
    }


    static EditorCookie getEditorCookie(Lookup context) {
        EditorCookie ck = context.lookup (EditorCookie.class);
        if (ck == null) {
            Node n  = context.lookup (Node.class);
            if (n != null) {
                ck = n.getLookup().lookup (EditorCookie.class);
                if (ck != null) {
                    TopComponent activetc = TopComponent.getRegistry().getActivated();
                    if (!(activetc instanceof Pane)) {
                        ck = null;
                    }
                }
            }
        }
        return ck;
    }

    public final void performAction(final Node[] activatedNodes) {
        performAction(getLookup(activatedNodes));
    }

    protected boolean enable(Node[] activatedNodes) {
        return enable(getLookup(activatedNodes));
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        return new ContextAction(actionContext);
    }

    protected class ContextAction implements Action, Presenter.Menu, Presenter.Popup, Presenter.Toolbar {

        Lookup context;

        public ContextAction(Lookup context) {
            this.context=context;
        }

        public Object getValue(String arg0) {
            return JavaRefactoringGlobalAction.this.getValue(arg0);
        }

        public void putValue(String arg0, Object arg1) {
            JavaRefactoringGlobalAction.this.putValue(arg0, arg1);
        }

        public void setEnabled(boolean arg0) {
            JavaRefactoringGlobalAction.this.setEnabled(arg0);
        }

        public boolean isEnabled() {
            return enable(context);
        }

        public void addPropertyChangeListener(PropertyChangeListener arg0) {
            JavaRefactoringGlobalAction.this.addPropertyChangeListener(arg0);
        }

        public void removePropertyChangeListener(PropertyChangeListener arg0) {
            JavaRefactoringGlobalAction.this.removePropertyChangeListener(arg0);
        }

        public void actionPerformed(ActionEvent arg0) {
            JavaRefactoringGlobalAction.this.performAction(context);
        }
        public JMenuItem getMenuPresenter() {
            if (isMethodOverridden(JavaRefactoringGlobalAction.this, "getMenuPresenter")) { // NOI18N

                return JavaRefactoringGlobalAction.this.getMenuPresenter();
            } else {
                return new Actions.MenuItem(this, true);
            }
        }

        public JMenuItem getPopupPresenter() {
            if (isMethodOverridden(JavaRefactoringGlobalAction.this, "getPopupPresenter")) { // NOI18N

                return JavaRefactoringGlobalAction.this.getPopupPresenter();
            } else {
                return new Actions.MenuItem(this, false);
            }
        }

        public Component getToolbarPresenter() {
            if (isMethodOverridden(JavaRefactoringGlobalAction.this, "getToolbarPresenter")) { // NOI18N

                return JavaRefactoringGlobalAction.this.getToolbarPresenter();
            } else {
                return new Actions.ToolbarButton(this);
            }
        }

        private boolean isMethodOverridden(NodeAction d, String name) {
            try {
                Method m = d.getClass().getMethod(name, new Class[0]);

                return m.getDeclaringClass() != CallableSystemAction.class;
            } catch (java.lang.NoSuchMethodException ex) {
                ex.printStackTrace();
                throw new IllegalStateException("Error searching for method " + name + " in " + d); // NOI18N
            }
        }
    }

   private static final class TextComponentRunnable implements Runnable, CancellableTask <CompilationController> {
        private final JTextComponent textC;
        private final int caret;
        private final int start;
        private final int end;
        private RefactoringUI ui;
        private final JavaRefactoringActionDelegate delegate;

        public TextComponentRunnable(EditorCookie ec, JavaRefactoringActionDelegate delegate) {
            this.delegate = delegate;
            this.textC = ec.getOpenedPanes()[0];
            this.caret = textC.getCaretPosition();
            this.start = textC.getSelectionStart();
            this.end = textC.getSelectionEnd();
            assert caret != -1;
            assert start != -1;
            assert end != -1;
        }

        public final void run() {
            try {
                JavaSource source = JavaSource.forDocument(textC.getDocument());
                source.runUserActionTask(this, false);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
                return;
            }
            TopComponent activetc = TopComponent.getRegistry().getActivated();
            if (ui!=null) {
                UI.openRefactoringUI(ui, activetc);
            } else {
                DialogDescriptor.Message msg = new DialogDescriptor.Message (delegate.getErrorMessage(),
                        DialogDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(msg);
            }
        }

        public void cancel() {
            //do nothing
        }

        public void run(CompilationController cc) throws Exception {
            TreePath selectedElement = null;
            cc.toPhase(Phase.RESOLVED);
            selectedElement = cc.getTreeUtilities().pathFor(caret);
            //workaround for issue 89064
            if (selectedElement.getLeaf().getKind() == Tree.Kind.COMPILATION_UNIT) {
                List<? extends Tree> decls = cc.getCompilationUnit().getTypeDecls();
                if (!decls.isEmpty()) {
                    selectedElement = TreePath.getPath(cc.getCompilationUnit(), decls.get(0));
                }
            }
            ui = delegate.createRefactoringUI(TreePathHandle.create(selectedElement, cc), start, end, cc);
        }
    }
}
