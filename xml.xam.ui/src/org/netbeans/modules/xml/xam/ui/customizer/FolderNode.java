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

package org.netbeans.modules.xml.xam.ui.customizer;

import java.awt.Image;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * An abstract node that uses a file folder icon. Ideally the icon is
 * taken from the Node delegate of DataFolder, if it is available.
 * Otherwise a default icon is used.
 *
 * @author  Nathan Fiedler
 */
public class FolderNode extends AbstractNode {
    /** The source for our folder icons. */
    private static Node iconSource;

    static {
        FileObject fobj = Repository.getDefault().getDefaultFileSystem().getRoot();
        try {
            DataObject dobj = DataObject.find(fobj);
            iconSource = dobj.getNodeDelegate();
        } catch (DataObjectNotFoundException donfe) {
            // In this case, we have our default icons, which are not
            // platform-conformant, but they are better than nothing.
        }
    }

    public FolderNode(Children children) {
        super(children);
    }

    public Image getIcon(int type) {
        if (iconSource != null) {
            return iconSource.getIcon(type);
        } else {
            String url = NbBundle.getMessage(FolderNode.class,
                    "IMG_FolderNode_Closed");
            return org.openide.util.Utilities.loadImage(url);
        }
    }

    public Image getOpenedIcon(int type) {
        if (iconSource != null) {
            return iconSource.getOpenedIcon(type);
        } else {
            String url = NbBundle.getMessage(FolderNode.class,
                    "IMG_FolderNode_Opened");
            return org.openide.util.Utilities.loadImage(url);
        }
    }
}
