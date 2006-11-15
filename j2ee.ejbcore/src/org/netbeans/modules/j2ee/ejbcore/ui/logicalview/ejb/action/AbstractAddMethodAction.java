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
import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.lang.model.element.TypeElement;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.j2ee.common.source.AbstractTask;
import org.netbeans.modules.j2ee.common.source.SourceUtils;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.EjbMethodController;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.actions.Presenter;

/**
 * Action that can always be invoked and work procedurally.
 * @author Chris Webster
 * @author Martin Adamek
 */
public abstract class AbstractAddMethodAction extends AbstractAction implements Presenter.Popup, ContextAwareAction {
    
    /** Action context. */
    private Lookup context;
    private final AbstractAddMethodStrategy strategy;

    public AbstractAddMethodAction(AbstractAddMethodStrategy strategy) {
        super(/*strategy.getTitle()*/);
        this.strategy = strategy;
    }

    public Action createContextAwareInstance(Lookup actionContext) {
        this.context = actionContext;
        boolean enable = enable((Node[])context.lookup(new Lookup.Template (Node.class)).allInstances().toArray(new Node[0]));
        return enable ? this : null;
    }
    
    public String getName(){
        return strategy.getTitle();
    }
    
    protected boolean enable(org.openide.nodes.Node[] activatedNodes) {
        if (activatedNodes.length != 1) {
            return false;
        }
        FileObject fileObject = activatedNodes[0].getLookup().lookup(FileObject.class);
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        final boolean[] result = new boolean[] {false};
        final MethodType.Kind prototypeMethodType = strategy.getPrototypeMethodKind();
        try {
            javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
                public void run(WorkingCopy workingCopy) throws Exception {
                    //TODO: RETOUCHE get selected class from Node
                    TypeElement typeElement = SourceUtils.newInstance(workingCopy).getTypeElement();
                    EjbMethodController ejbMethodController = EjbMethodController.createFromClass(workingCopy, typeElement);
                    result[0] = (ejbMethodController != null) && ejbMethodController.supportsMethodType(prototypeMethodType);
                }
            });
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }
        return result[0];
    }
    
    public void actionPerformed(ActionEvent actionEvent) {
        performAction(context.lookup(new Lookup.Template<Node>(Node.class)).allInstances().toArray(new Node[0]));
    }
    
    protected void performAction(org.openide.nodes.Node[] activatedNodes) {
        if (activatedNodes.length != 1) {
            return;
        }
        FileObject fileObject = activatedNodes[0].getLookup().lookup(FileObject.class);
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        final ElementHandle<TypeElement>[] classHandle = new ElementHandle[1];
        try {
            javaSource.runUserActionTask(new AbstractTask<CompilationController>() {
                public void run(CompilationController compilationController) throws Exception {
                    //TODO: RETOUCHE get selected class from Node, now main one is taken
                    TypeElement typeElement = SourceUtils.newInstance(compilationController).getTypeElement();
                    classHandle[0] = ElementHandle.create(typeElement);
                }
            }, true);
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }
        if (classHandle[0] != null) {
            try {
                strategy.addMethod(fileObject, classHandle[0]);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
    }

    public Object getValue(String key) {
        if (NAME.equals(key)) {
            return getName();
        } else {
            return super.getValue(key);
        }
    }

    public JMenuItem getPopupPresenter() {
        return new JMenuItem (this);
    }

}
