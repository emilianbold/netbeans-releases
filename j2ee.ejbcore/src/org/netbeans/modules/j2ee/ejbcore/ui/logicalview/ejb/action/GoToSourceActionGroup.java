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
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.modules.j2ee.common.JMIUtils;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EntityAndSession;
import org.netbeans.modules.javacore.api.JavaModel;
import org.openide.filesystems.FileObject;
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

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    // private helpers =========================================================
    
    private ClassPath getClassPath() {
        JavaClass jc = JMIUtils.getJavaClassFromNode(getActivatedNodes()[0]);
        FileObject fo = JavaModel.getFileObject(jc.getResource());
        return ClassPath.getClassPath(fo, ClassPath.SOURCE);
    }
    
    private EntityAndSession getEjb() {
        try {
            JavaClass jc = JMIUtils.getJavaClassFromNode(getActivatedNodes()[0]);
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
