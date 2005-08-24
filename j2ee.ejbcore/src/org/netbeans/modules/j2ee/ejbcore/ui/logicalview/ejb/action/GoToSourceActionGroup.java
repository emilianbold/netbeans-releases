/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.JMenuItem;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.Resource;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EntityAndSession;
import org.netbeans.modules.javacore.api.JavaModel;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Group holding source navigation actions for Session and Entity EJBs
 *
 * @author Martin Adamek
 */
public class GoToSourceActionGroup extends EJBActionGroup {
    
    public String getName() {
        return NbBundle.getMessage(GoToSourceActionGroup.class, "LBL_GoToSourceGroup");
    }

    protected Action[] grouped() {
        EntityAndSession ejb = getEjb();
        ClassPath cp = getClassPath();
        List actions = new ArrayList();
        actions.add(new GoToSourceAction(cp, ejb.getEjbClass(),
                NbBundle.getMessage(GoToSourceAction.class, "LBL_GoTo_BeanImplementation")));
        if (ejb.getRemote() != null) {
            actions.add(new GoToSourceAction(cp, ejb.getRemote(),
                NbBundle.getMessage(GoToSourceAction.class, "LBL_GoTo_RemoteInterface")));
        }
        if (ejb.getLocal() != null) {
            actions.add(new GoToSourceAction(cp, ejb.getLocal(),
                NbBundle.getMessage(GoToSourceAction.class, "LBL_GoTo_LocalInterface")));
        }
        if (ejb.getHome() != null) {
            actions.add(new GoToSourceAction(cp, ejb.getHome(),
                NbBundle.getMessage(GoToSourceAction.class, "LBL_GoTo_RemoteHomeInterface")));
        }
        if (ejb.getLocalHome() != null) {
            actions.add(new GoToSourceAction(cp, ejb.getLocalHome(),
                NbBundle.getMessage(GoToSourceAction.class, "LBL_GoTo_LocalHomeInterface")));
        }
        return (Action[]) actions.toArray(new Action[actions.size()]);
    }

    public JMenuItem getPopupPresenter() {
        return getMenu();
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    // private helpers =========================================================
    
    /** 
     * Checks if the node represents Java source file.
     */
    private JavaClass javaClassForNode(Node n) {
        JavaClass jc = null;
        DataObject dobj = (DataObject) n.getCookie(DataObject.class);
        if (dobj != null) {
            Resource res = JavaModel.getResource(dobj.getPrimaryFile());
            if (res != null) {
                List/*<JavaClass>*/ classes = res.getClassifiers();
                if (classes.size() == 1) {
                    jc = (JavaClass)classes.get(0);
                }
            }
        }
        return jc;
    }

    private ClassPath getClassPath() {
        JavaClass jc = javaClassForNode(getActivatedNodes()[0]);
        FileObject fo = JavaModel.getFileObject(jc.getResource());
        return ClassPath.getClassPath(fo, ClassPath.SOURCE);
    }
    
    private EntityAndSession getEjb() {
        try {
            JavaClass jc = javaClassForNode(getActivatedNodes()[0]);
            EjbJar ejbJar = DDProvider.getDefault().getDDRoot(getDDFileObject(jc));
            Ejb[] ejbs = ejbJar.getEnterpriseBeans().getEjbs();
            for (int i = 0; i < ejbs.length; i++) {
                if (jc.getName().equals(ejbs[i].getEjbClass())) {
                    return (EntityAndSession) ejbs[i];
                }
            }
        } catch (IOException ioe) {
            // do nothing
        }
        return null;
    }
    
    private FileObject getDDFileObject(JavaClass jc) {
        FileObject fo = JavaModel.getFileObject(jc.getResource());
        return org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJar(fo).getDeploymentDescriptor();
    }

}
