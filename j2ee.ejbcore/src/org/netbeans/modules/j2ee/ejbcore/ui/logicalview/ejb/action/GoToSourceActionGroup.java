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
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.j2ee.common.source.AbstractTask;
import org.netbeans.modules.j2ee.common.source.SourceUtils;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.ejb.EntityAndSession;
import org.netbeans.modules.j2ee.ejbcore.Utils;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Group holding source navigation actions for Session and Entity EJBs
 *
 * @author Martin Adamek
 */
public class GoToSourceActionGroup extends EJBActionGroup {

    private final static int EJB_CLASS = 0;
    private final static int REMOTE = 1;
    private final static int LOCAL = 2;
    private final static int HOME = 3;
    private final static int LOCAL_HOME = 4;
    
    public String getName() {
        return NbBundle.getMessage(GoToSourceActionGroup.class, "LBL_GoToSourceGroup");
    }

    protected Action[] grouped() {

        final FileObject[] results = new FileObject[5];
        
        Node node = getActivatedNodes()[0];
        if (node == null) {
            return new Action[0];
        }
        
        FileObject fileObject = node.getLookup().lookup(FileObject.class);
        final String[] ejbClass = new String[1];
        
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        MetadataModel<EjbJarMetadata> model = org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJar(fileObject).getMetadataModel();

        try {
            javaSource.runUserActionTask(new AbstractTask<CompilationController>() {
                public void run(CompilationController controller) throws IOException {
                    controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    ejbClass[0] = SourceUtils.newInstance(controller).getTypeElement().getQualifiedName().toString();
                }
            }, true);

            model.runReadAction(new MetadataModelAction<EjbJarMetadata, Void>() {
                public Void run(EjbJarMetadata metadata) {
                    EntityAndSession ejb = (EntityAndSession) metadata.findByEjbClass(ejbClass[0]);
                    results[EJB_CLASS] = ejb.getEjbClass() ==null ? null : metadata.findResource(Utils.toResourceName(ejb.getEjbClass()));
                    results[REMOTE] = ejb.getRemote() ==null ? null : metadata.findResource(Utils.toResourceName(ejb.getRemote()));
                    results[LOCAL] = ejb.getLocal() ==null ? null : metadata.findResource(Utils.toResourceName(ejb.getLocal()));
                    results[HOME] = ejb.getHome() ==null ? null : metadata.findResource(Utils.toResourceName(ejb.getHome()));
                    results[LOCAL_HOME] = ejb.getLocalHome() ==null ? null : metadata.findResource(Utils.toResourceName(ejb.getLocalHome()));
                    return null;
                }
            });
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }

        List<Action> actions = new ArrayList<Action>();
        actions.add(new GoToSourceAction(results[EJB_CLASS], NbBundle.getMessage(GoToSourceActionGroup.class, "LBL_GoTo_BeanImplementation")));
        if (results[REMOTE] != null) {
            actions.add(new GoToSourceAction(results[REMOTE], NbBundle.getMessage(GoToSourceActionGroup.class, "LBL_GoTo_RemoteInterface")));
        }
        if (results[LOCAL] != null) {
            actions.add(new GoToSourceAction(results[LOCAL], NbBundle.getMessage(GoToSourceActionGroup.class, "LBL_GoTo_LocalInterface")));
        }
        if (results[HOME] != null) {
            actions.add(new GoToSourceAction(results[HOME], NbBundle.getMessage(GoToSourceActionGroup.class, "LBL_GoTo_RemoteHomeInterface")));
        }
        if (results[LOCAL_HOME] != null) {
            actions.add(new GoToSourceAction(results[LOCAL_HOME], NbBundle.getMessage(GoToSourceActionGroup.class, "LBL_GoTo_LocalHomeInterface")));
        }
        
        return actions.toArray(new Action[actions.size()]);
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
}
