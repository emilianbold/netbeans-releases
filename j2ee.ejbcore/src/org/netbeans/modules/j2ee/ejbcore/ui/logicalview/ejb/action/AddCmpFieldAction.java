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
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.EjbMethodController;
import org.netbeans.modules.j2ee.ejbcore.Utils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
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
        if (activatedNodes == null || activatedNodes.length < 1) {
            return false;
        }
        EjbMethodController ejbMethodController;
        try {
            ElementHandle<TypeElement> elementHandle = Utils.getJavaClassFromNode(activatedNodes[0]);
            if (elementHandle == null) {
                return false;
            }
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
            return false;
        }
        //TODO: RETOUCHE
        return false;
//        return activatedNodes.length == 1 &&
//               isCallable(activatedNodes[0]) &&
//               (ejbMethodController = EjbMethodController.create(jc)) != null &&
//               ejbMethodController instanceof EntityMethodController &&
//               ((EntityMethodController) ejbMethodController).isCMP();
    }

    protected void performAction(org.openide.nodes.Node[] activatedNodes) {
        //TODO: RETOUCHE
//        TypeElement jc = Utils.getJavaClassFromNode(activatedNodes[0]);
//        Field fe = createField(jc);
//        FileObject ddFile = getDDFile(jc);
//        EntityMethodController emc = (EntityMethodController) EjbMethodController.create(jc);
//        addCmpField(emc, ddFile, fe);
    }

    public static boolean addCmpField(TypeElement beanClass, FileObject ddFile) {
        //TODO: RETOUCHE
        return false;
//        assert beanClass != null;
//        Field fe = createField(beanClass);
//        EntityMethodController emc = (EntityMethodController) EntityMethodController.createFromClass(beanClass);
//        return addCmpField(emc, ddFile, fe);
    }

    /** 
     * Creates a new FieldElement and initializes it with the default values.
     */
    //TODO: RETOUCHE
//    private static Field createField(JavaClass jc) {
//        return JMIUtils.createField(jc, "cmpField", "String"); //NOI18N
//    }
    
    //TODO: RETOUCHE
//    private static boolean addCmpField(EntityMethodController emc, FileObject ddFile, Field fe) {
//        FieldCustomizer customizer = new FieldCustomizer(fe, "", emc.getLocal() != null, emc.getRemote() != null,
//                true, true, false, false);
//        while (openAddCmpFieldDialog(customizer)) {
//            try {
//                emc.validateNewCmpFieldName(fe.getName());
//                emc.addField(fe, ddFile, customizer.isLocalGetter(), customizer.isLocalSetter(),
//                        customizer.isRemoteGetter(), customizer.isRemoteSetter(), customizer.getDescription());
//                return true;
//            } catch (IllegalArgumentException ex) {
//                Utils.notifyError(ex);
//            } catch (IOException ioe) {
//                Utils.notifyError(ioe);
//                return false;
//            }
//        }
//        return false;
//    }
//
//    private static boolean openAddCmpFieldDialog(FieldCustomizer fc) {
//        NotifyDescriptor nd = new NotifyDescriptor(fc, NAME, NotifyDescriptor.OK_CANCEL_OPTION,
//                NotifyDescriptor.PLAIN_MESSAGE, null, null);
//        if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.OK_OPTION) {
//            return fc.isOK();  // apply possible changes in dialog field
//        } else {
//            return false;
//        }
//    }
//
//    protected FileObject getDDFile(Element me) {
//        DataObject dobj = JavaMetamodel.getManager().getDataObject(me.getResource());
//        assert dobj != null;
//        FileObject fo = dobj.getPrimaryFile();
//        return EjbJar.getEjbJar(fo).getDeploymentDescriptor();
//    }
//
//    private boolean isCallable(Node node) {
//        return JMIUtils.getJavaClassFromNode(node) != null;
//    }
//
//    public javax.swing.Action createContextAwareInstance(org.openide.util.Lookup actionContext) {
//        boolean enable = enable((Node[])actionContext.lookup (new Lookup.Template(Node.class)).allInstances().toArray(new Node[0]));
//        return enable ? super.createContextAwareInstance(actionContext) : null;
//    }
}
