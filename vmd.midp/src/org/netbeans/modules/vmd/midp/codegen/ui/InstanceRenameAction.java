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
package org.netbeans.modules.vmd.midp.codegen.ui;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import com.sun.source.util.TreePath;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.text.StyledDocument;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.ProgressEvent;
import org.netbeans.modules.refactoring.api.ProgressListener;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.netbeans.modules.vmd.api.codegen.CodeReferencePresenter;
import org.netbeans.modules.vmd.api.codegen.ModelUpdatePresenter;
import org.netbeans.modules.vmd.api.inspector.common.RenameAction;
import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.netbeans.modules.vmd.api.io.ProjectUtils;
import org.netbeans.modules.vmd.api.io.providers.DataObjectInterface;
import org.netbeans.modules.vmd.api.io.providers.IOSupport;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.common.DocumentSupport;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.netbeans.modules.vmd.api.model.presenters.actions.ActionContext;
import org.netbeans.modules.vmd.midp.codegen.InstaceRenameRefactoring;
import org.netbeans.modules.vmd.midp.components.general.ClassCD;
import org.netbeans.modules.vmd.midp.components.general.ClassCode.GeneratedCodePresenter;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.LifecycleManager;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;

/**
 *
 * @author ads
 */
public class InstanceRenameAction extends SystemAction implements ActionContext {

    public InstanceRenameAction() {
    }

    public static final String DISPLAY_NAME = NbBundle.getMessage(
            InstanceRenameAction.class, "NAME_RenameAction"); //NOI18N
    private WeakReference<DesignComponent> myComponent;

    public void actionPerformed(final ActionEvent e) {
        if (myComponent == null) {
            return;
        }

        final boolean[] proceed = new boolean [1];
        myComponent.get().getDocument().getTransactionManager().
                        readAccess(new Runnable() {

                    public void run() {
                        GeneratedCodePresenter presenter =
                                myComponent.get().getPresenter(GeneratedCodePresenter.class);
                        if ( presenter == null  ){
                            proceed[0] = true;
                        }
                        else if (presenter.isCodeGenerated())
                        {
                            System.out.println("@@@@@@@@@@@@@ code is generated");
                            proceed[0] = true;
                        }
                    }
        });

        if ( !proceed[0]){
            SystemAction.get(RenameAction.class).actionPerformed(e);
            return;
        }
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                LifecycleManager.getDefault().saveAll();

                if (myComponent == null || myComponent.get() == null) {
                    throw new IllegalArgumentException("No DesignComponent attached to RenameAction"); //NOI18N
                }

                final String[] names = new String[2];
                myComponent.get().getDocument().getTransactionManager().
                        readAccess(new Runnable() {

                    public void run() {
                        names[0] = myComponent.get().readProperty(
                                ClassCD.PROP_INSTANCE_NAME).getPrimitiveValue().
                                toString();
                        names[1] = getGetterName(myComponent.get(), names[0]);

                    }
                });

                final String oldName = names[0];
                final String getterName = names[1];

                Runnable task = new Runnable() {

                    public void run() {
                        try {
                            DataObjectContext context = ProjectUtils.getDataObjectContextForDocument(
                                    myComponent.get().getDocument());
                            DataObject dataObject = context.getDataObject();
                            DataObjectInterface dataObjectInteface = IOSupport.getDataObjectInteface(dataObject);
                            StyledDocument styledDocument =
                                    dataObjectInteface.getEditorDocument();
                            JavaSource javaSource = JavaSource.forDocument(
                                    styledDocument);
                            MemberVisitor visitor = new MemberVisitor(oldName,
                                    getterName);
                            javaSource.runUserActionTask(visitor, true);

                            TopComponent activetc = TopComponent.getRegistry().
                                    getActivated();

                            Lookup lookup = null;
                            if (visitor.getField() == null) {
                                SystemAction.get(RenameAction.class).
                                        actionPerformed(e);
                                return;
                            }
                            if (visitor.getMethod() == null ||
                                    names[1] == null) {
                                lookup = Lookups.singleton(visitor.getField());
                            } else {
                                lookup = Lookups.fixed(visitor.getField(),
                                        visitor.getMethod());
                            }
                            final InstaceRenameRefactoring refactoring =
                                    new InstaceRenameRefactoring(lookup);
                            /*
                             * For some reasons this commented call doesn't work.
                             * I don't know why but at the end of refactoring
                             *  method <code>stop</code> is not called for
                             * listener.
                             *  refactoring.addProgressListener(new ProgressListener() {
                             */
                            refactoring.setProgressListener(new ProgressListener() {

                                public void start(ProgressEvent event) {
                                }

                                public void step(ProgressEvent event) {
                                }

                                public void stop(ProgressEvent event) {
                                    if (refactoring.getResult() == null ||
                                            !refactoring.getResult().isFatal()) {
                                        updateModel(refactoring.getNewFieldName());
                                    }
                                }
                            });
                            RefactoringUI ui = new RenameRefactoringUI(refactoring,
                                    visitor.getInfo(), oldName, myComponent.get(),
                                    visitor.getMethod() != null && names[1] != null);
                            UI.openRefactoringUI(ui, activetc);
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                };
                invokeAfterScanFinished(task);
            }

            private void updateModel(final String newName) {
                myComponent.get().getDocument().getTransactionManager().
                        writeAccess(new Runnable() {

                    public void run() {
                        InfoPresenter presenter = myComponent.get().
                                getPresenter(InfoPresenter.class);
                        if (presenter == null) {
                            Debug.warning("No necessary presenter for " +
                                    "this operation - component: " + myComponent.get()); //NOI18N
                            return;
                        }
                        presenter.setEditableName(newName);
                    }
                });
            }
        });
    }

    public static boolean invokeAfterScanFinished(final Runnable runnable) {
        assert SwingUtilities.isEventDispatchThread();
        if (SourceUtils.isScanInProgress()) {
            final ActionPerformer ap = new ActionPerformer(runnable);
            ActionListener listener = new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    ap.cancel();
                    waitTask.cancel();
                }
            };
            JLabel label = new JLabel(getString("MSG_WaitScan"),
                    javax.swing.UIManager.getIcon("OptionPane.informationIcon"),
                    SwingConstants.LEFT);                           // NOI18N
            label.setBorder(new EmptyBorder(12, 12, 11, 11));
            DialogDescriptor dd = new DialogDescriptor(label, DISPLAY_NAME.replace("&", ""), true,
                    new Object[]{getString("LBL_CancelAction", // NOI18N
                        new Object[]{DISPLAY_NAME})}, null, 0, null, listener);
            waitDialog = DialogDisplayer.getDefault().createDialog(dd);
            waitDialog.pack();
            waitTask = RequestProcessor.getDefault().post(ap, 100);
            waitDialog.setVisible(true);
            waitTask = null;
            waitDialog = null;
            return ap.hasBeenCancelled();
        } else {
            runnable.run();
            return false;
        }
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public boolean isEnabled() {
        return SystemAction.get(RenameAction.class).isEnabled();
    }

    public String getName() {
        return DISPLAY_NAME;
    }

    public void setComponent(DesignComponent component) {
        setActionComponent(component);
        SystemAction.get(RenameAction.class).setComponent(component);
    }

    protected void setActionComponent(DesignComponent component) {
        myComponent = new WeakReference<DesignComponent>(component);
    }

    static String getGetterName(final DesignComponent component,
            final String fieldName) {
        if (fieldName == null || fieldName.length() == 0) {
            return "";
        }
        if (component.getDocument().getTransactionManager().isAccess()) {
            String accessCode = CodeReferencePresenter.generateAccessCode(component,
                    fieldName);
            if (!accessCode.equals(fieldName)) {
                String methodName = accessCode.replace("()", ""). // NOI18N
                        trim();
                if (accessCode.startsWith(methodName)) {
                    return methodName;
                }
            }
            return null;
        } else {
            final String[] result = new String[1];
            component.getDocument().getTransactionManager().readAccess(
                    new Runnable() {

                        public void run() {
                            result[0] = getGetterName(component, fieldName);
                        }
                    });
            return result[0];
        }
    }

    private static String getString(String key) {
        return NbBundle.getMessage(InstanceRenameAction.class, key);
    }

    private static String getString(String key, Object values) {
        return new MessageFormat(getString(key)).format(values);
    }

    private static class ActionPerformer implements Runnable {

        private Runnable action;
        private boolean cancel = false;

        ActionPerformer(Runnable a) {
            this.action = a;
        }

        public boolean hasBeenCancelled() {
            return cancel;
        }

        public void run() {
            try {
                SourceUtils.waitScanFinished();
            } catch (InterruptedException ie) {
                Exceptions.printStackTrace(ie);
            }
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    if (!cancel) {
                        if (waitDialog != null) {
                            waitDialog.setVisible(false);
                            waitDialog.dispose();
                        }
                        action.run();
                    }
                }
            });
        }

        public void cancel() {
            assert SwingUtilities.isEventDispatchThread();
            // check if the scanning did not finish during cancel
            // invocation - in such case do not set cancel to true
            // and do not try to hide waitDialog window
            if (waitDialog != null) {
                cancel = true;
                waitDialog.setVisible(false);
                waitDialog.dispose();
            }
        }
    }

    private class MemberVisitor
            extends TreePathScanner<Void, Void>
            implements CancellableTask<CompilationController> {

        public MemberVisitor(String field, String method) {
            myMember = field;
            myMethod = method;
        }

        @Override
        public Void visitClass(ClassTree t, Void v) {
            List<? extends Tree> members = (List<? extends Tree>) t.getMembers();
            Iterator<? extends Tree> it = members.iterator();
            while (it.hasNext()) {
                Tree tr = it.next();
                if (tr.getKind() == Tree.Kind.VARIABLE) {
                    Trees trees = myInfo.getTrees();
                    TreePath path = new TreePath(getCurrentPath(), tr);
                    Element el = trees.getElement(path);
                    String sname = el.getSimpleName().toString();
                    if (sname.equals(myMember)) {
                        myFieldHandle = TreePathHandle.create(path, myInfo);
                    }
                } else if (tr.getKind() == Tree.Kind.METHOD) {
                    Trees trees = myInfo.getTrees();
                    TreePath path = new TreePath(getCurrentPath(), tr);
                    Element el = trees.getElement(path);
                    String sname = el.getSimpleName().toString();
                    if (sname.equals(myMethod)) {
                        ExecutableElement method = (ExecutableElement) el;
                        List<? extends VariableElement> params =
                                method.getParameters();
                        if (params == null || params.size() == 0) {
                            myMethodHandle = TreePathHandle.create(path, myInfo);
                        }
                    }
                }
            }
            return null;
        }

        public void cancel() {
        }

        public void run(CompilationController parameter) throws IOException {
            myInfo = parameter;
            parameter.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
            scan(parameter.getCompilationUnit(), null);
        }

        TreePathHandle getField() {
            return myFieldHandle;
        }

        TreePathHandle getMethod() {
            return myMethodHandle;
        }

        CompilationInfo getInfo() {
            return myInfo;
        }
        private CompilationInfo myInfo;
        private String myMember;
        private String myMethod;
        private TreePathHandle myFieldHandle;
        private TreePathHandle myMethodHandle;
    }
    private static RequestProcessor.Task waitTask = null;
    private static Dialog waitDialog = null;
}
