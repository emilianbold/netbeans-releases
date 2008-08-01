/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.iep.editor.xsd.nodes;

import java.awt.Image;
import java.beans.BeanInfo;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.nodes.Node;
import org.openide.util.Utilities;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 *
 * @author radval
 */
public class FolderNode extends AbstractSchemaArtifactNode {

    private String badge;
    
    public FolderNode(String folderName) {
        super(folderName);
    }
    
    @Override
    public Icon getIcon() {
        if(mIcon == null) {
            Node n = getFolderNode();
            Image i = null;
            if (n != null) {
                i = n.getIcon(BeanInfo.ICON_COLOR_16x16);
            }
            Image image = badgeImage(i);
            if(image != null) {
                mIcon = new ImageIcon(image);
            }
    
        }
        return mIcon;
    }
    
    public void setBadge(String badge) {
        this.badge = badge;
    }
    
    protected Image badgeImage(Image main) {
        Image rv = main;
        if (badge != null) {
            Image badgeImage = Utilities.loadImage(badge);
            rv = Utilities.mergeImages(main, badgeImage, 8, 8);
        }
        return rv;
    }
    
    private Node getFolderNode() {
        FileObject fo =
            Repository.getDefault().getDefaultFileSystem().getRoot();
        Node n = null;
        try {
            DataObject dobj = DataObject.find(fo);
            n = dobj.getNodeDelegate();
        } catch (DataObjectNotFoundException ex) {
            // cannot get the node for this, this shouldn't happen
            // so just ignore
        }

        return n;
    }
}
