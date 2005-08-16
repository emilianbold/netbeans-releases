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

package org.netbeans.modules.versioning.system.cvss.ui.actions;

import org.openide.util.actions.SystemAction;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;
import org.openide.loaders.DataObject;
import org.openide.filesystems.FileObject;
import org.openide.awt.DynamicMenuContent;
import org.openide.awt.Actions;
import org.netbeans.modules.versioning.system.cvss.FileInformation;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.api.project.Project;

import javax.swing.*;
import java.text.MessageFormat;
import java.io.File;
import java.util.MissingResourceException;

/**
 * Base for all context-sensitive CVS actions.
 * 
 * @author Maros Sandor
 */
public abstract class AbstractSystemAction extends SystemAction implements DynamicMenuContent {

    /**
     * @return bundle key base name
     * @see #getName
     */
    protected abstract String getBaseName();

    protected AbstractSystemAction() {
        setIcon(null);
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

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

        File [] nodes = Utils.getActivatedFiles(getFileEnabledStatus(), getDirectoryEnabledStatus());
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
        File [] nodes = Utils.getActivatedFiles(getFileEnabledStatus(), getDirectoryEnabledStatus());
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
                if (dao != null) {
                    return dao.getPrimaryFile().getNameExt();
                } else {
                    return activatedNodes[0].getDisplayName();
                }
            }
        } else {
            if (projectsOnly) {
                try {
                    return MessageFormat.format(NbBundle.getBundle(AbstractSystemAction.class).getString("MSG_ActionContext_MultipleProjects"),  // NOI18N
                                                new Object [] { new Integer(objectCount) });
                } catch (MissingResourceException ex) {
                    // ignore use files alternative bellow
                }
            }
            return MessageFormat.format(NbBundle.getBundle(AbstractSystemAction.class).getString("MSG_ActionContext_MultipleFiles"),  // NOI18N
                                        new Object [] { new Integer(objectCount) });
        }
    }    
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(this.getClass());
    }

    /**
     * Determines action working files and its enablement logic
     * (most subclasses just define {@link #getFileEnabledStatus)
     * and {@link #getDirectoryEnabledStatus}.
     * .
     * @return files to act on or empty array if this action should be disabled.
     */
    protected File [] getFilesToProcess() {
        return Utils.getActivatedFiles(getFileEnabledStatus(), getDirectoryEnabledStatus());
    }

    public boolean isEnabled() {
        return getFilesToProcess().length > 0;
    }

    protected int getFileEnabledStatus() {
        return ~0;
    }

    protected int getDirectoryEnabledStatus() {
        return FileInformation.STATUS_MANAGED & ~FileInformation.STATUS_NOTVERSIONED_EXCLUDED;
    }
}
