/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.php.editor.codegen.ui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/**
 *
 * @author kuba
 */
class NodeSelectionListener implements MouseListener, KeyListener {

    JTree tree;

    NodeSelectionListener(JTree tree) {
        this.tree = tree;
    }

    
    public void mouseClicked(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        int row = tree.getRowForLocation(x, y);
        TreePath path = tree.getPathForRow(row);
        //TreePath  path = tree.getSelectionPath();
        if (path != null) {
            CheckNode node = (CheckNode) path.getLastPathComponent();
            boolean isSelected = !(node.isSelected());
            node.setSelected(isSelected);
            ((DefaultTreeModel) tree.getModel()).nodeChanged(node);
            tree.revalidate();
            tree.repaint();
        }
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }

    // Key Listener --------------------------------------------------------
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {

            if (e.getSource() instanceof JTree) {
                JTree t = (JTree) e.getSource();
                TreePath path = t.getSelectionPath();
                if (path != null) {
                    CheckNode node = (CheckNode) path.getLastPathComponent();
                    boolean isSelected = !(node.isSelected());
                    node.setSelected(isSelected);
                    ((DefaultTreeModel) tree.getModel()).nodeChanged(node);
                    tree.revalidate();
                    tree.repaint();
                }
                e.consume();
            }
        }
    }

    public void keyReleased(KeyEvent e) {
    }
}