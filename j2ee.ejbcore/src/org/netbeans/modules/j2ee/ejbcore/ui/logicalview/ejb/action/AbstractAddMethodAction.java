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
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.modules.j2ee.common.JMIUtils;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.EjbMethodController;
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
    private String name;
    private AbstractAddMethodStrategy strategy;

    public AbstractAddMethodAction(AbstractAddMethodStrategy strategy) {
        super(/*strategy.getTitle()*/);
        this.strategy = strategy;
        this.name = strategy.getTitle();
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
        JMIUtils.beginJmiTransaction();
        try {
            JavaClass jc = JMIUtils.getJavaClassFromNode(activatedNodes[0]);
            boolean result = false;
            if (jc != null) {
                EjbMethodController c = EjbMethodController.createFromClass(jc);
                result = (c != null) && c.supportsMethodType(strategy.prototypeMethod());
            }
            return result;
        }
        finally {
            JMIUtils.endJmiTransaction();
        }
    }
    
    public void actionPerformed(java.awt.event.ActionEvent e) {
        performAction((Node[])context.lookup (new org.openide.util.Lookup.Template (
                Node.class
            )).allInstances().toArray(new Node[0]));
    }
    
    protected void performAction(org.openide.nodes.Node[] activatedNodes) {
        JavaClass jc = JMIUtils.getJavaClassFromNode(activatedNodes[0]);
        if (jc != null) {
            strategy.addMethod(jc);
        }
    }

    public Object getValue(String key) {
        if (NAME.equals(key)) {
            return getName();
        }
        else {
            return super.getValue(key);
        }
    }

    public JMenuItem getPopupPresenter() {
        return new JMenuItem (this);
    }

}
