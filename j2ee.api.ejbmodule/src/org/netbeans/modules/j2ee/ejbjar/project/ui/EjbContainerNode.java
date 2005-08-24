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

package org.netbeans.modules.j2ee.ejbjar.project.ui;

import java.awt.Image;
import javax.swing.Action;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbNodesFactory;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

/**
 * @author Chris Webster
 */
public class EjbContainerNode extends AbstractNode {
    public static final String NAME = "EJBS"; // NOI18N
    
    private static final String EJB_BADGE = "org/netbeans/modules/j2ee/ejbjar/project/ui/enterpriseBeansBadge.png"; // NOI18N
    
    public EjbContainerNode(EjbJar model, ClassPath srcPath, FileObject ddFile, Project p, EjbNodesFactory nodesFactory) {
        super(new EjbContainerChildren(model, srcPath, ddFile, nodesFactory), Lookups.singleton(p));
        setName(EjbNodesFactory.CONTAINER_NODE_NAME);
        setDisplayName(NbBundle.getMessage(EjbContainerNode.class, "LBL_node"));
        setShortDescription(NbBundle.getMessage(EjbContainerNode.class, "HINT_node"));
    }
    
    public Action[] getActions(boolean context) {
        return new Action[] {
            CommonProjectActions.newFileAction()
        };
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
        // When you have help, change to:
        // return new HelpCtx(EjbContainerNode.class);
    }
    
    public Image getIcon(int type) {
        return computeIcon(false, type);
    }
    
    public Image getOpenedIcon(int type) {
        return computeIcon(true, type);
    }
    
    private Image computeIcon(boolean opened, int type) {
        Image image;
        Node iconDelegate = getIconDelegate();
        if (opened) {
            image = iconDelegate != null ? iconDelegate.getOpenedIcon(type) : super.getOpenedIcon(type);
        } else {
            image = iconDelegate != null ? iconDelegate.getIcon(type) : super.getIcon(type);
        }
        Image badge = Utilities.loadImage(EJB_BADGE);
        return Utilities.mergeImages(image, badge, 7, 7);
    }
    
    private Node getIconDelegate() {
        try {
            return DataFolder.find(Repository.getDefault().getDefaultFileSystem().getRoot()).getNodeDelegate();
        }
        catch (DataObjectNotFoundException donfe) {
            return null;
        }
    }
}
