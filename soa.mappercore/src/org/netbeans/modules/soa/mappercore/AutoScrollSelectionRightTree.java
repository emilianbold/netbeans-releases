/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.soa.mappercore;

import java.awt.Rectangle;
import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.mappercore.event.MapperSelectionEvent;
import org.netbeans.modules.soa.mappercore.event.MapperSelectionListener;
import org.netbeans.modules.soa.mappercore.utils.Utils;

/**
 *
 * @author AlexanderPermyakov
 */
public class AutoScrollSelectionRightTree implements MapperSelectionListener {
    private RightTree rightTree;
    private TreePath currentPath;

    public AutoScrollSelectionRightTree(RightTree rightTree) {
        this.rightTree = rightTree;
        rightTree.getSelectionModel().addSelectionListener(this);
        currentPath = null;
    }

    public void mapperSelectionChanged(MapperSelectionEvent event) {
        TreePath treePath = rightTree.getSelectionModel().getSelectedPath();
        if (Utils.equal(treePath, currentPath)) { return; }
        
        currentPath = treePath;
        if (treePath == null) { return; }
        
        MapperNode node = rightTree.getMapper().getNode(treePath, true);
        int h = node.getContentHeight();
        int y = node.getContentCenterY();
        y = node.yToView(y);
        Rectangle rect = rightTree.getScrollPane().getViewport().getViewRect();
        Rectangle r;
        if (y + h / 2 > rect.y + rect.height && h < rect.height) {
            r = new Rectangle(rect.x, y + h / 2, 2, 2);
            rightTree.scrollRectToVisible(r);
        }
        if (y - h / 2 < rect.y && h < rect.height) {
            r = new Rectangle(rect.x, y - h / 2, 2, 2);
            rightTree.scrollRectToVisible(r);
        }
        if (y + h / 2 > rect.y + rect.height && h > rect.height) {
            r = new Rectangle(rect.x, y - h / 2, 2, rect.height);
            rightTree.scrollRectToVisible(r);
            return;
        }
        if (y - h / 2 < rect.y && h > rect.height) {
            r = new Rectangle(rect.x, y + h / 2 - rect.height, 2, rect.height);
            rightTree.scrollRectToVisible(r);
            return;
        }
    }
}
