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


package org.netbeans.modules.compapp.projects.jbi.ui;

import org.netbeans.modules.compapp.projects.jbi.JbiProject;
import org.netbeans.modules.compapp.projects.jbi.ui.actions.JbiProjectCookie;
import org.netbeans.modules.compapp.projects.jbi.ui.customizer.JbiProjectProperties;

import org.openide.filesystems.*;

import org.openide.loaders.DataFolder;

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

import org.openide.windows.IOProvider;
import org.openide.windows.OutputWriter;

import java.awt.Image;
import java.awt.event.ActionEvent;

import java.io.File;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;

import javax.swing.Action;
import org.netbeans.modules.compapp.projects.jbi.ui.actions.AddProjectAction;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;


/**
 * DOCUMENT ME!
 *
 * @author 
 * @version 
 */
public class JbiModuleViewNode extends AbstractNode {
    private static Image JBI_MODULES_BADGE = Utilities.loadImage(
            "org/netbeans/modules/compapp/projects/jbi/ui/resources/compositeApplicationBadge.png", true ); // NOI18N
    private final DataFolder aFolder;
    private final JbiProject project;
    private JbiModuleViewChildren children;

    /**
     * Creates a new JbiModuleViewNode object.
     *
     * @param epp DOCUMENT ME!
     * @param project DOCUMENT ME!
     */
    public JbiModuleViewNode(JbiProjectProperties epp, JbiProject project) {
        super(new JbiModuleViewChildren(project));
        this.project = project;

        DataFolder projectFolder = null;
        FileObject projectDir = project.getProjectDirectory();

        if (projectDir.isFolder()) {
            projectFolder = DataFolder.findFolder(projectDir);
        }

        this.aFolder = projectFolder;

        // Set FeatureDescriptor stuff:
        setName("preferablyUniqueNameForThisNodeAmongSiblings"); // NOI18N or, super.setName if needed
        setDisplayName(NbBundle.getMessage(JbiModuleViewNode.class, "LBL_ModuleViewNode")); // NOI18N

        //setShortDescription(NbBundle.getMessage(JbiModuleViewNode.class, "HINT_LogicalViewNode"));
        getCookieSet().add(new JbiProjectCookie(project));

        // set the model listener
        FileChangeAdapter fca = new FileChangeAdapter() {
                public void fileChanged(FileEvent ev) {
                    //log("ModView: Contents changed.");
                    RequestProcessor.getDefault().post(
                        new Runnable() {
                            public void run() {
                                updateChildren();
                            }
                        }
                    );
                }

                /**
                 * DOCUMENT ME!
                 *
                 * @param ev DOCUMENT ME!
                 */
                public void fileAttributeChanged(FileAttributeEvent ev) {
                    //log("ModView: "+ev.getName() + ": " + ev.getOldValue() + " -> " + ev.getNewValue());
                }
            };

        File pf = FileUtil.toFile(project.getProjectDirectory());
        File file = new File(pf.getPath() + "/nbproject/project.xml"); // NOI18N
        FileObject modelFile = FileUtil.toFileObject(file);
        modelFile.addFileChangeListener(fca);

        children = (JbiModuleViewChildren) this.getChildren();
    }

    private void updateChildren() {
        if (children != null) {
            children.addNotify();
        }

        //JbiModuleViewChildren ch = new JbiModuleViewChildren(project.getProjectProperties());
        //this.setChildren(ch);
        // ch.addNotify();
    }

    /**
     * DOCUMENT ME!
     *
     * @param type DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Image getIcon(int type) {
        return computeIcon(false, type);
    }

    /**
     * DOCUMENT ME!
     *
     * @param type DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Image getOpenedIcon(int type) {
        return computeIcon(true, type);
    }

    private Image computeIcon(boolean opened, int type) {
        if (aFolder != null) {
            Node folderNode = aFolder.getNodeDelegate();
            Image image = opened ? folderNode.getOpenedIcon(type) : folderNode.getIcon(type);

            return Utilities.mergeImages(image, JBI_MODULES_BADGE, 7, 7);
        } else {
            return JBI_MODULES_BADGE;
        }
    }

    // Create the popup menu:
    public Action[] getActions(boolean context) {
        ResourceBundle bundle = NbBundle.getBundle(JbiModuleViewNode.class);
        return new Action[] {
            new AbstractAction(bundle.getString("LBL_AddProjectAction_Name"), null) {
                public void actionPerformed(ActionEvent e) {
                    new AddProjectAction().perform(project);
                }
            }
            /*
            SystemAction.get(AddModuleAction.class), null,
            //SystemAction.get(ToolsAction.class),
            //null,
            SystemAction.get(PropertiesAction.class),
            */
        };
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;

        // When you have help, change to:
        // return new HelpCtx(LogicalViewNode.class);
    }

    private void log(String str) {
        OutputWriter out = IOProvider.getDefault().getStdOut();
        out.println(str);
        out.flush();
    }
}
