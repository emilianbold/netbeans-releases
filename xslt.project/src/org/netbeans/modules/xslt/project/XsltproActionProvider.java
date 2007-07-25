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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xslt.project;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.compapp.projects.base.ui.customizer.IcanproProjectProperties;
//import org.netbeans.modules.xslt.project.ui.customizer.VisualClassPathItem;
import org.netbeans.modules.compapp.projects.base.ui.customizer.VisualClassPathItem;
import org.netbeans.modules.xslt.project.ui.customizer.XsltproProjectProperties;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.openide.ErrorManager;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.openide.windows.IOProvider;
import org.openide.windows.OutputWriter;

/**
 * Action provider of the Web project. This is the place where to do
 * strange things to Web actions. E.g. compile-single.
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class XsltproActionProvider implements ActionProvider{
    
    // Commands available from Web project
    private static final String[] supportedActions = {
        COMMAND_BUILD,
        COMMAND_CLEAN,
        COMMAND_REBUILD,
        COMMAND_DELETE, 
        XsltproConstants.POPULATE_CATALOG,
        COMMAND_DELETE,
        COMMAND_COPY,
        COMMAND_MOVE,
        COMMAND_RENAME
    };

    XsltproProject project;

    // Ant project helper of the project
    private AntProjectHelper antProjectHelper;
    private ReferenceHelper refHelper;

    /** Map from commands to ant targets */
    Map<String,String[]> commands;

    public XsltproActionProvider(XsltproProject project, AntProjectHelper antProjectHelper, ReferenceHelper refHelper) {
        commands = new HashMap<String,String[]>();
        commands.put(COMMAND_BUILD, new String[] {"dist"}); // NOI18N
        commands.put(COMMAND_CLEAN, new String[] {"clean"}); // NOI18N
        commands.put(COMMAND_REBUILD, new String[] {"clean", "dist"}); // NOI18N
        commands.put(XsltproConstants.POPULATE_CATALOG, new String[] {"populate"});
        //commands.put(XsltproConstants.COMMAND_REDEPLOY, new String[] {"run"}); // NOI18N
        //commands.put(XsltproConstants.COMMAND_DEPLOY, new String[] {"run"}); // NOI18N

        this.antProjectHelper = antProjectHelper;
        this.project = project;
        this.refHelper = refHelper;
    }

    public String[] getSupportedActions() {
        return supportedActions;
    }

    public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
        if (COMMAND_COPY.equals(command)) {
            DefaultProjectOperations.performDefaultCopyOperation(project);
            return ;
        }
        
        if (COMMAND_MOVE.equals(command)) {
            DefaultProjectOperations.performDefaultMoveOperation(project);
            return ;
        }
        
        if (COMMAND_RENAME.equals(command)) {
            DefaultProjectOperations.performDefaultRenameOperation(project, null);
            return ;
        }
        if (COMMAND_DELETE.equals(command)) {
            DefaultProjectOperations.performDefaultDeleteOperation(project);
            return ;
        }
        if (command.equals(XsltproConstants.POPULATE_CATALOG)) {
            XsltProjectRetriever bpRetriever = new XsltProjectRetriever(project.getProjectDirectory());
            bpRetriever.execute();
            return;
        }
        Properties p = null;
        String[] targetNames = commands.get(command);
////        //EXECUTION PART    
////        if (command.equals (XsltproConstants.COMMAND_DEPLOY) || command.equals (XsltproConstants.COMMAND_REDEPLOY)) {
////            if (!isSelectedServer ()) {
////                return;
////            }
////            if (isDebugged()) {
////                NotifyDescriptor nd;
////                ProjectInformation pi = (ProjectInformation)project.getLookup().lookup(ProjectInformation.class);
////                String text = pi.getDisplayName();
////                nd = new NotifyDescriptor.Confirmation(
////                            NbBundle.getMessage(XsltproActionProvider.class, "MSG_SessionRunning", text),
////                            NotifyDescriptor.OK_CANCEL_OPTION);
////                Object o = DialogDisplayer.getDefault().notify(nd);
////                if (o.equals(NotifyDescriptor.OK_OPTION)) {
////                    DebuggerManager.getDebuggerManager().getCurrentSession().kill();
////                } else {
////                    return;
////                }
////            }
////        } else {
////            p = null;
////            if (targetNames == null) {
////                throw new IllegalArgumentException(command);
////            }
////        }


//        	if build command then build any depedent project
            if(command.equals(COMMAND_BUILD)) {
            	try {
            		buildDependentProjectsAndRunTask(targetNames, p);
            	} catch (IOException e) {
                    ErrorManager.getDefault().notify(e);
                }
            } else {
            	runTask(targetNames, p);
            }
    }

    public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
        if ( findBuildXml() == null ) {
            return false;
        }
        return true;
    }
    
    // private methods
    
    /**
     * @return array of targets or null to stop execution; can return empty array
     */
    String[] getTargetNames(String command, Lookup context, Properties p) throws IllegalArgumentException {
        String[] targetNames = commands.get(command);
        return targetNames;
    }

    private FileObject findBuildXml() {
        return project.getProjectDirectory().getFileObject(project.getBuildXmlName ());
    }
    private void runTask(String[] targetNames, Properties p)  {
        try {
            ActionUtils.runTarget(findBuildXml(), targetNames, p);
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
    }
    
    private void buildDependentProjectsAndRunTask(String[] targetNames, Properties p) throws IOException  {
   	XsltproProjectProperties app = this.project.getProjectProperties();
        List items = (List) app.get(IcanproProjectProperties.JAVAC_CLASSPATH);
        ArrayList artifacts = new ArrayList();

        for (int i = 0, size = items.size(); i < size; i++) {
            VisualClassPathItem vi = (VisualClassPathItem) items.get(i);
            AntArtifact aa = (AntArtifact) vi.getObject();
            String loc =  aa.getProject().getProjectDirectory().getPath() + "/" +  aa.getArtifactLocation().getPath();
            File asa = new File(loc);
            log(" Dependent Project artifact jar: "+ loc + ", [" + (asa.exists()?"exist":"missing") + "]");
            if (! asa.exists()) {
            	artifacts.add(aa);
            }
        }

        if(artifacts.size() != 0) {
	        //use AntTaskListener which invokes the target on
        	//current project build script after all the depedent projects
        	//are build
        	AntTaskListener antTaskListener = new AntTaskListener(targetNames, p);
	        antTaskListener.setTotalTasks(artifacts.size());
	        Iterator it = artifacts.iterator();
	        while(it.hasNext()) {
	        	AntArtifact aa = (AntArtifact) it.next();
	        	String loc =  aa.getProject().getProjectDirectory().getPath() + "/" +  aa.getArtifactLocation().getPath();
	        	log(" Building dependent project "+ loc + "...");
	        	ExecutorTask task = ActionUtils.runTarget(aa.getScriptFile(), new String[] { aa.getTargetName() }, null);
	        	task.addTaskListener(antTaskListener);
	        }
        } else {
        	//no need to build depedent projects
        	//directly invoke target on current project build script;
        	runTask(targetNames, p);
        }
    }

    private boolean isDebugged() {
        return false;
    }
    
//    private boolean isSelectedServer () {
//        String instance = antProjectHelper.getStandardPropertyEvaluator ().getProperty (XsltproConstants.J2EE_SERVER_INSTANCE);
//        boolean selected;
//        if (instance != null) {
//            selected = true;
//        } else {
//            // no selected server => warning
//            String server = antProjectHelper.getStandardPropertyEvaluator ().getProperty (XsltproConstants.J2EE_SERVER_TYPE);
//            NoSelectedServerWarning panel = new NoSelectedServerWarning (server);
//
//            Object[] options = new Object[] {
//                DialogDescriptor.OK_OPTION,
//                DialogDescriptor.CANCEL_OPTION
//            };
//            DialogDescriptor desc = new DialogDescriptor (panel,
//                    NbBundle.getMessage (NoSelectedServerWarning.class, "CTL_NoSelectedServerWarning_Title"), // NOI18N
//                true, options, options[0], DialogDescriptor.DEFAULT_ALIGN, null, null);
//            Dialog dlg = DialogDisplayer.getDefault().createDialog (desc);
//            dlg.setVisible (true);
//            if (desc.getValue() != options[0]) {
//                selected = false;
//            } else {
//                instance = panel.getSelectedInstance ();
//                selected = instance != null;
//                if (selected) {
//                    XsltproProjectProperties wpp = new XsltproProjectProperties (project, antProjectHelper, refHelper);
//                    wpp.put (XsltproConstants.J2EE_SERVER_INSTANCE, instance);
//                    wpp.store ();
//                }
//            }
//            dlg.dispose();
//        }
//        return selected;
//    }

    private void log(String str) {
        OutputWriter out = IOProvider.getDefault().getStdOut();
        out.println(str);
        out.flush();
    }

    private class AntTaskListener implements TaskListener {
        int totalTaskCount;
        int finishedTaskCount = 0;
        private String[] mTargetNames;
        private Properties mProperties;
        
        public AntTaskListener(String[] targetNames, Properties p) {
            this.mTargetNames = targetNames;
            this.mProperties = p;
        }
        
        public void setTotalTasks(int total) {
            this.totalTaskCount = total;
        }
        
        public void taskFinished(Task task) {
            finishedTaskCount++;
            if(finishedTaskCount == totalTaskCount) {
                runTask(this.mTargetNames, this.mProperties);
            }
        }
    }

}
