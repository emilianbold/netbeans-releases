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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action;

import java.io.IOException;
import javax.lang.model.element.TypeElement;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.openide.nodes.Node;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.common.source.AbstractTask;
import org.netbeans.modules.j2ee.common.source.SourceUtils;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.EjbMethodController;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.ContextAwareAction;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.SystemAction;

/**
 * Action which just holds a few other SystemAction's for grouping purposes.
 * @author cwebster
 */
public class EJBActionGroup extends NodeAction implements Presenter.Popup {
    
    Lookup actionContext;
    
    public String getName() {
        return NbBundle.getMessage(EJBActionGroup.class, "LBL_EJBActionGroup");
    }
    
    /** List of system actions to be displayed within this one's toolbar or submenu. */
    protected Action[] grouped() {
        return new Action[] {
            SystemAction.get(ExposeInLocalAction.class),
            SystemAction.get(ExposeInRemoteAction.class),
            null,
            new AddBusinessMethodAction(),
            new AddCreateMethodAction(),
            new AddFinderMethodAction(),
            new AddHomeMethodAction(),
            new AddSelectMethodAction(),
            SystemAction.get(AddCmpFieldAction.class)
        };
    }
    
    public JMenuItem getPopupPresenter() {
        if (isEnabled() && isEjbProject(getActivatedNodes())) {
            return getMenu();
        }
        JMenuItem jMenuItem = super.getPopupPresenter();
        jMenuItem.setVisible(false);
        return jMenuItem;
    }
    
    protected JMenu getMenu() {
        return new LazyMenu(actionContext);
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
        // If you will provide context help then use:
        // return new HelpCtx(PromoteBusinessMethodAction.class);
    }
    
    protected boolean enable(org.openide.nodes.Node[] activatedNodes) {
        if (activatedNodes.length != 1) {
            return false;
        }
        FileObject fileObject = activatedNodes[0].getLookup().lookup(FileObject.class);
        if (fileObject == null) {
            return false;
        }
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        final String[] className = new String[1];
        try {
            if (javaSource == null) {
                return false;
            }
            javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
                public void run(WorkingCopy workingCopy) throws IOException {
                    workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    //TODO: RETOUCHE get selected class from Node
                    SourceUtils sourceUtils = SourceUtils.newInstance(workingCopy);
                    if (sourceUtils != null) {
                        TypeElement typeElement = sourceUtils.getTypeElement();
                        className[0] = typeElement.getQualifiedName().toString();
                    }
                }
            });
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }
        EjbMethodController ejbMethodController = null;
        if (className[0] != null) {
             ejbMethodController = EjbMethodController.createFromClass(fileObject, className[0]);
        }
        return ejbMethodController != null;
    }
    
    protected void performAction(org.openide.nodes.Node[] activatedNodes) {
        // do nothing -- should never be called
    }
    
    public boolean isEjbProject(Node[] activatedNodes) { 
        return activatedNodes.length == 1 &&
               isContainingProjectEjb(activatedNodes[0].getLookup().lookup(FileObject.class));
    }
    
    private static boolean isContainingProjectEjb(FileObject fileObject) {
        if (fileObject == null) {
            return false;
        }
        Project project = FileOwnerQuery.getOwner(fileObject);
        if (project == null) {
            return false;
        }
        return EjbJar.getEjbJars(project).length > 0;
    }
    
    /** Implements <code>ContextAwareAction</code> interface method. */
    public Action createContextAwareInstance(Lookup actionContext) {
        this.actionContext = actionContext;
        return super.createContextAwareInstance(actionContext);
    }

    /**
     * Avoids constructing submenu until it will be needed.
     */
    private final class LazyMenu extends JMenu {
        
        private final Lookup lookup;
        
        public LazyMenu(Lookup lookup) {
            super(EJBActionGroup.this.getName());
            this.lookup = lookup;
        }
        
        public JPopupMenu getPopupMenu() {
            if (getItemCount() == 0) {
                Action[] grouped = grouped();
                for (int i = 0; i < grouped.length; i++) {
                    Action action = grouped[i];
                    if (action == null && getItemCount() != 0) {
                        addSeparator();
                    } else {
                        if (action instanceof ContextAwareAction) {
                            action = ((ContextAwareAction)action).createContextAwareInstance(lookup);
                        }
                        if (action instanceof Presenter.Popup) {
                            add(((Presenter.Popup)action).getPopupPresenter());
                        }
                    }
                }
            }
            return super.getPopupMenu();
        }
    }
    
}
