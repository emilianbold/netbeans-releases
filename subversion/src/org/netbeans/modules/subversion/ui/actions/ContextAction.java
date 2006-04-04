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

package org.netbeans.modules.subversion.ui.actions;

import com.sun.corba.se.impl.oa.poa.RequestProcessingPolicyImpl;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.openide.util.actions.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataShadow;
import org.openide.filesystems.FileObject;
import org.openide.LifecycleManager;
import org.netbeans.api.project.Project;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.FileInformation;
import org.netbeans.modules.subversion.OutputLogger;
import java.text.MessageFormat;
import java.text.DateFormat;
import java.io.File;
import java.util.MissingResourceException;
import java.util.Date;
import java.awt.event.ActionEvent;
import org.netbeans.modules.subversion.Subversion;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * Base for all context-sensitive SVN actions.
 * 
 * @author Maros Sandor
 */
public abstract class ContextAction extends NodeAction {

    // it's singleton
    // do not declare any instance data

    protected ContextAction() {
        setIcon(null);
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }
    
    /**
     * @return bundle key base name
     * @see #getName
     */
    protected abstract String getBaseName(Node[] activatedNodes);

    protected boolean enable(Node[] nodes) {
        return getContext(nodes).getRootFiles().length > 0;
    }
    
    /**
     * Synchronizes memory modificatios with disk and calls
     * {@link  #performContextAction}.
     */
    protected void performAction(final Node[] nodes) {
        // TODO try to save files in invocation context only
        LifecycleManager.getDefault().saveAll();
        
        SvnProgressSupport support = createSvnProgressSupport(nodes);
        if(support!=null) {
            support.start(getRunningName(nodes));
        } else {
            performContextAction(nodes, null);   
        }               
    }

    protected SvnProgressSupport createSvnProgressSupport(final Node[] nodes) {
        return new ProgressSupport(this, nodes);
    }

    protected SVNUrl getSvnUrl(Node[] nodes) {
        return getSvnUrl(getContext(nodes)); // XXX maybe as a parameter
    }

    public static SVNUrl getSvnUrl(Context ctx) {
        File[] roots = ctx.getRootFiles();
        return SvnUtils.getRepositoryRootUrl(roots[0]);
    }
    
    protected abstract void performContextAction(Node[] nodes, SvnProgressSupport support);

    /** Be sure nobody overwrites */
    public final boolean isEnabled() {
        return super.isEnabled();
    }

    /** Be sure nobody overwrites */
    public final void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
    }

    /** Be sure nobody overwrites */
    public final void actionPerformed(ActionEvent event) {
        super.actionPerformed(event);
    }

    /** Be sure nobody overwrites */
    public final void performAction() {
        super.performAction();
    }    

    /**
     * Running action display name, it seeks action class bundle for:
     * <ul>
     *   <li><code>getBaseName() + "Running"</code> key
     *   <li><code>getBaseName() + "Running_Context"</code> key for one selected file
     *   <li><code>getBaseName() + "Running_Context_Multiple"</code> key for multiple selected files
     *   <li><code>getBaseName() + "Running_Project"</code> key for one selected project
     *   <li><code>getBaseName() + "Running_Projects"</code> key for multiple selected projects
     * </ul>
     */    
    public String getRunningName(Node [] activatedNodes) {
        return getName("Running", activatedNodes); // NOI18N
    }

    public String getName() {
        return getName("", TopComponent.getRegistry().getActivatedNodes()); // NOI18N
    }
    
    /**
     * Display name, it seeks action class bundle for:
     * <ul>
     *   <li><code>getBaseName()</code> key
     *   <li><code>getBaseName() + "_Context"</code> key for one selected file
     *   <li><code>getBaseName() + "_Context_Multiple"</code> key for multiple selected files
     *   <li><code>getBaseName() + "_Project"</code> key for one selected project
     *   <li><code>getBaseName() + "_Projects"</code> key for multiple selected projects
     * </ul>
     */
    public String getName(String role, Node[] activatedNodes) {
        String baseName = getBaseName(activatedNodes) + role;
        if (!isEnabled()) {
            return NbBundle.getBundle(this.getClass()).getString(baseName);
        }

        File [] nodes = SvnUtils.getCurrentContext(activatedNodes, getFileEnabledStatus(), getDirectoryEnabledStatus()).getFiles();
        int objectCount = nodes.length;
        // if all nodes represent project node the use plain name
        // It avoids "Show changes 2 files" on project node
        // caused by fact that project contains two source groups.

        boolean projectsOnly = true;
        for (int i = 0; i < activatedNodes.length; i++) {
            Node activatedNode = activatedNodes[i];
            Project project =  (Project) activatedNode.getLookup().lookup(Project.class);
            if (project == null) {
                projectsOnly = false;
                break;
            }
        }
        if (projectsOnly) objectCount = activatedNodes.length; 

        if (objectCount == 0) {
            return NbBundle.getBundle(this.getClass()).getString(baseName);
        } else if (objectCount == 1) {
            if (projectsOnly) {
                String dispName = ProjectUtils.getInformation((Project) activatedNodes[0].getLookup().lookup(Project.class)).getDisplayName();
                return NbBundle.getMessage(this.getClass(), baseName + "_Context",  // NOI18N
                                                dispName);
            }
            String name;
            FileObject fo = (FileObject) activatedNodes[0].getLookup().lookup(FileObject.class);
            if (fo != null) {
                name = fo.getNameExt();
            } else {
                DataObject dao = (DataObject) activatedNodes[0].getLookup().lookup(DataObject.class);
                if (dao instanceof DataShadow) {
                    dao = ((DataShadow) dao).getOriginal();
                }
                if (dao != null) {
                    name = dao.getPrimaryFile().getNameExt();
                } else {
                    name = activatedNodes[0].getDisplayName();
                }
            }
            return MessageFormat.format(NbBundle.getBundle(this.getClass()).getString(baseName + "_Context"),  // NOI18N
                                            new Object [] { name });
        } else {
            if (projectsOnly) {
                try {
                    return MessageFormat.format(NbBundle.getBundle(this.getClass()).getString(baseName + "_Projects"),  // NOI18N
                                                new Object [] { new Integer(objectCount) });
                } catch (MissingResourceException ex) {
                    // ignore use files alternative bellow
                }
            }
            return MessageFormat.format(NbBundle.getBundle(this.getClass()).getString(baseName + "_Context_Multiple"),  // NOI18N
                                        new Object [] { new Integer(objectCount) });
        }
    }
    
    /**
     * Computes display name of the context this action will operate.
     * 
     * @return String name of this action's context, e.g. "3 files", "MyProject", "2 projects", "Foo.java". Returns
     * null if the context is empty
     */ 
    public String getContextDisplayName(Node [] activatedNodes) {
        // TODO: reuse this code in getName() 
        File [] nodes = SvnUtils.getCurrentContext(activatedNodes, getFileEnabledStatus(), getDirectoryEnabledStatus()).getFiles();
        int objectCount = nodes.length;
        // if all nodes represent project node the use plain name
        // It avoids "Show changes 2 files" on project node
        // caused by fact that project contains two source groups.

        boolean projectsOnly = true;
        for (int i = 0; i < activatedNodes.length; i++) {
            Node activatedNode = activatedNodes[i];
            Project project =  (Project) activatedNode.getLookup().lookup(Project.class);
            if (project == null) {
                projectsOnly = false;
                break;
            }
        }
        if (projectsOnly) objectCount = activatedNodes.length; 

        if (objectCount == 0) {
            return null;
        } else if (objectCount == 1) {
            if (projectsOnly) {
                return ProjectUtils.getInformation((Project) activatedNodes[0].getLookup().lookup(Project.class)).getDisplayName();
            }
            FileObject fo = (FileObject) activatedNodes[0].getLookup().lookup(FileObject.class);
            if (fo != null) {
                return fo.getNameExt();
            } else {
                DataObject dao = (DataObject) activatedNodes[0].getLookup().lookup(DataObject.class);
                if (dao instanceof DataShadow) {
                    dao = ((DataShadow) dao).getOriginal();
                }
                if (dao != null) {
                    return dao.getPrimaryFile().getNameExt();
                } else {
                    return activatedNodes[0].getDisplayName();
                }
            }
        } else {
            if (projectsOnly) {
                try {
                    return MessageFormat.format(NbBundle.getBundle(ContextAction.class).getString("MSG_ActionContext_MultipleProjects"),  // NOI18N
                                                new Object [] { new Integer(objectCount) });
                } catch (MissingResourceException ex) {
                    // ignore use files alternative bellow
                }
            }
            return MessageFormat.format(NbBundle.getBundle(ContextAction.class).getString("MSG_ActionContext_MultipleFiles"),  // NOI18N
                                        new Object [] { new Integer(objectCount) });
        }
    }    
        
    public HelpCtx getHelpCtx() {
        return new HelpCtx(this.getClass());
    }

    protected Context getContext(Node[] nodes) {
        return SvnUtils.getCurrentContext(nodes, getFileEnabledStatus(), getDirectoryEnabledStatus());
    }
    
    protected int getFileEnabledStatus() {
        return ~0;
    }

    protected int getDirectoryEnabledStatus() {
        return FileInformation.STATUS_MANAGED & ~FileInformation.STATUS_NOTVERSIONED_EXCLUDED;
    }

    protected boolean asynchronous() {
        return false;
    }

    protected static class ProgressSupport  extends SvnProgressSupport {

        private final ContextAction action;
        private final Node[] nodes;
        private long progressStamp;

        public ProgressSupport(ContextAction action, Node[] nodes) {
            super(createRequestProcessor(action, nodes));
            this.action = action;
            this.nodes = nodes;
        }

        private static RequestProcessor createRequestProcessor(ContextAction action, Node[] nodes) {
            SVNUrl repository = action.getSvnUrl(nodes);
            return Subversion.getInstance().getRequestProccessor(repository);
        }

        public void perform() {
            action.performContextAction(nodes, this);
        }

        protected void startProgress() {
            OutputLogger logger = new OutputLogger();
            logger.logCommandLine("==[IDE]== " + DateFormat.getDateTimeInstance().format(new Date()) + " " + action.getRunningName(nodes));
            ProgressHandle progress = getProgressHandle();
            progress.setInitialDelay(500);
            progressStamp = System.currentTimeMillis() + 500;
            progress.start();            
        }

        protected void finnishProgress() {
            // TODO add failed and restart texts                

            OutputLogger logger = new OutputLogger();
            logger.logCommandLine("==[IDE]== " + DateFormat.getDateTimeInstance().format(new Date()) + " " + action.getName("", nodes) + " finished.");

            final ProgressHandle progress = getProgressHandle();
            progress.switchToDeterminate(100);
            progress.progress("Done!", 100);
            if (System.currentTimeMillis() > progressStamp) {
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        progress.finish();
                    }
                }, 15 * 1000);
            } else {
                progress.finish();
            }
        }
    }
}
