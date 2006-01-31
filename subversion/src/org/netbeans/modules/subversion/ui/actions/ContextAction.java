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

import org.openide.util.actions.SystemAction;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataShadow;
import org.openide.filesystems.FileObject;
import org.openide.awt.DynamicMenuContent;
import org.openide.awt.Actions;
import org.openide.LifecycleManager;
import org.netbeans.api.project.Project;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.FileInformation;

import javax.swing.*;
import java.text.MessageFormat;
import java.io.File;
import java.util.MissingResourceException;
import java.awt.event.ActionEvent;

/**
 * Base for all context-sensitive SVN actions.
 * 
 * @author Maros Sandor
 */
public abstract class ContextAction extends SystemAction implements DynamicMenuContent {

    /**
     * @return bundle key base name
     * @see #getName
     */
    protected abstract String getBaseName();

    protected ContextAction() {
        setIcon(null);
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    /**
     * Synchronizes memory modificatios with disk and calls
     * {@link  #performContextAction}.
     */
    public final void actionPerformed(ActionEvent ev) {
        // TODO try to save files in invocation context only
        LifecycleManager.getDefault().saveAll();
        performContextAction(ev);
    }

    protected abstract void performContextAction(ActionEvent e);

    public JComponent[] getMenuPresenters() {
        return new JComponent[] { new Actions.MenuItem(this, true) };
    }

    public JComponent[] synchMenuPresenters(JComponent[] items) {
        return new JComponent[] { new Actions.MenuItem(this, true) };
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
    public String getName() {
        String baseName = getBaseName();
        if (!isEnabled()) {
            return NbBundle.getBundle(this.getClass()).getString(baseName);
        }

        File [] nodes = SvnUtils.getCurrentContext(null, getFileEnabledStatus(), getDirectoryEnabledStatus()).getFiles();
        int objectCount = nodes.length;
        // if all nodes represent project node the use plain name
        // It avoids "Show changes 2 files" on project node
        // caused by fact that project contains two source groups.

        boolean projectsOnly = true;
        Node [] activatedNodes = TopComponent.getRegistry().getActivatedNodes();
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
                return NbBundle.getMessage(this.getClass(), baseName + "_Context",  // NOI18N
                                                activatedNodes[0].getDisplayName());
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
    public String getContextDisplayName() {
        // TODO: reuse this code in getName() 
        File [] nodes = SvnUtils.getCurrentContext(null, getFileEnabledStatus(), getDirectoryEnabledStatus()).getFiles();
        int objectCount = nodes.length;
        // if all nodes represent project node the use plain name
        // It avoids "Show changes 2 files" on project node
        // caused by fact that project contains two source groups.

        boolean projectsOnly = true;
        Node [] activatedNodes = TopComponent.getRegistry().getActivatedNodes();
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
                return activatedNodes[0].getDisplayName();
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

    protected Context getContext() {
        return SvnUtils.getCurrentContext(null, getFileEnabledStatus(), getDirectoryEnabledStatus());
    }
    
    public boolean isEnabled() {
        return getContext().getRootFiles().length > 0;
    }

    protected int getFileEnabledStatus() {
        return ~0;
    }

    protected int getDirectoryEnabledStatus() {
        return FileInformation.STATUS_MANAGED & ~FileInformation.STATUS_NOTVERSIONED_EXCLUDED;
    }
}
