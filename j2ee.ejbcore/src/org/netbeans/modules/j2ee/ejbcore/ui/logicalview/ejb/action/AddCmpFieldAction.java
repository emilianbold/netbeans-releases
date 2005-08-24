/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action;


import java.io.IOException;
import org.netbeans.jmi.javamodel.Element;
import org.netbeans.jmi.javamodel.Field;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.common.JMIUtils;
import org.netbeans.modules.j2ee.common.ui.nodes.FieldCustomizer;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.EntityMethodController;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.EjbMethodController;
import org.netbeans.modules.j2ee.ejbcore.Utils;
import org.netbeans.modules.javacore.internalapi.JavaMetamodel;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * Action that can always be invoked and work procedurally.
 * @author Chris Webster
 */
public class AddCmpFieldAction extends NodeAction {

    private static final String NAME = NbBundle.getMessage(AddCmpFieldAction.class, "LBL_AddCmpFieldAction");
    
    public String getName() {
        return NAME;
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    protected boolean asynchronous() {
        return false;
    }

    protected boolean enable(org.openide.nodes.Node[] activatedNodes) {
        EjbMethodController c;
        return activatedNodes.length == 1 &&
               isCallable(activatedNodes[0]) &&
               (c = EjbMethodController.create(JMIUtils.getJavaClassFromNode(activatedNodes[0]))) != null &&
               c instanceof EntityMethodController &&
               ((EntityMethodController) c).isCMP();
    }

    protected void performAction(org.openide.nodes.Node[] activatedNodes) {
        JavaClass jc = JMIUtils.getJavaClassFromNode(activatedNodes[0]);
        Field fe = createField(jc);
        FileObject ddFile = getDDFile(jc);
        EntityMethodController emc = (EntityMethodController) EjbMethodController.create(jc);
        addCmpField(emc, ddFile, fe);
    }

    public static boolean addCmpField(JavaClass beanClass, FileObject ddFile) {
        assert beanClass != null;
        JMIUtils.createField(beanClass, "cmpField", "String"); //NOI18N
        Field fe = createField(beanClass);
        EntityMethodController emc = (EntityMethodController) EntityMethodController.createFromClass(beanClass);
        return addCmpField(emc, ddFile, fe);
    }

    /** 
     * Creates a new FieldElement and initializes it with the default values.
     */
    private static Field createField(JavaClass jc) {
        return JMIUtils.createField(jc, "cmpField", "String"); //NOI18N
    }
    
    private static boolean addCmpField(EntityMethodController emc, FileObject ddFile, Field fe) {
        FieldCustomizer customizer = new FieldCustomizer(fe, "", emc.getLocal() != null, emc.getRemote() != null,
                true, true, false, false);
        while (openAddCmpFieldDialog(customizer)) {
            try {
                emc.validateNewCmpFieldName(fe.getName());
                emc.addField(fe, ddFile, customizer.isLocalGetter(), customizer.isLocalSetter(),
                        customizer.isRemoteGetter(), customizer.isRemoteSetter(), customizer.getDescription());
                return true;
            } catch (IllegalArgumentException ex) {
                Utils.notifyError(ex);
            } catch (IOException ioe) {
                Utils.notifyError(ioe);
                return false;
            }
        }
        return false;
    }

    private static boolean openAddCmpFieldDialog(FieldCustomizer fc) {
        NotifyDescriptor nd = new NotifyDescriptor(fc, NAME, NotifyDescriptor.OK_CANCEL_OPTION,
                NotifyDescriptor.PLAIN_MESSAGE, null, null);
        if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.OK_OPTION) {
            fc.isOK();  // apply possible changes in dialog fields;
            return true;
        } else {
            return false;
        }
    }

    protected FileObject getDDFile(Element me) {
        DataObject dobj = JavaMetamodel.getManager().getDataObject(me.getResource());
        assert dobj != null;
        FileObject fo = dobj.getPrimaryFile();
        return EjbJar.getEjbJar(fo).getDeploymentDescriptor();
    }

    private boolean isCallable(Node node) {
        return JMIUtils.getJavaClassFromNode(node) != null;
    }
}
