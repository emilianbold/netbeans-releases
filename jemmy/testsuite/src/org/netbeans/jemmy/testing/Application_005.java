package org.netbeans.jemmy.testing;

import java.awt.*;

import java.awt.event.*;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;

public class Application_005 extends TestFrame {

    JPopupMenu popup;
    JList list;
    JTree tree;
    
    public Application_005() {
	super("Application_005");
	
	DefaultMutableTreeNode node000 = new DefaultMutableTreeNode();
	node000.setUserObject("node000");
	DefaultMutableTreeNode node001 = new DefaultMutableTreeNode();
	node001.setUserObject("node001");
	DefaultMutableTreeNode node00 = new DefaultMutableTreeNode();
	node00.setUserObject("node00");
	node00.insert(node000, 0);
	node00.insert(node001, 1);
	DefaultMutableTreeNode node01 = new DefaultMutableTreeNode();
	node01.setUserObject("node01");
	DefaultMutableTreeNode node0 = new DefaultMutableTreeNode();
	node0.setUserObject("node0");
	node0.insert(node00, 0);
	node0.insert(node01, 1);
	
	tree = new JTree(node0);
	tree.setEditable(true);

	list = new JList();

	popup = new JPopupMenu();
	JMenuItem pb = new JMenuItem("XXX");
	pb.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    list.setModel(new MyModel(tree.getSelectionPaths()));
		    popup.setVisible(false);
		}
	    });
	popup.add(pb);
	tree.add(popup);
	
	tree.addMouseListener(new PopupListener());

	getContentPane().setLayout(new BorderLayout());
	JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
					  new JScrollPane(tree),
					  new JScrollPane(list));
	getContentPane().add(split, BorderLayout.CENTER);

	setSize(400, 200);
    }

    public static void main(String[] argv) {
	(new Application_005()).show();
    }

    class MyModel implements ListModel {
	private TreePath[] store;
	public MyModel(TreePath[] st) {
	    if(st == null) {
		store = new TreePath[0];
	    } else {		
		store = st;
	    }
	}
	public void addListDataListener(ListDataListener l){}
	public Object getElementAt(int index) {
	    return(store[index]);
	}
	public int getSize() {
	    return(store.length);
	}
	public void removeListDataListener(ListDataListener l){}
    }

    class PopupListener extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }
	
        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                popup.show(e.getComponent(),
                           e.getX(), e.getY());
            }
        }
    }
}
