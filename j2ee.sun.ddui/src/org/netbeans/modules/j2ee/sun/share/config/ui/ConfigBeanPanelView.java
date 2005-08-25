/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * ConfigBeanPanelView.java
 *
 * Created on March 6, 2003, 2:13 PM
 */

package org.netbeans.modules.j2ee.sun.share.config.ui;

import java.awt.Component;
import java.awt.CardLayout;
import java.util.HashMap;
import java.util.Map;
import java.beans.Customizer;

import javax.swing.JPanel;

import org.openide.explorer.propertysheet.PropertySheetView;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAdapter;
import org.openide.nodes.NodeMemberEvent;

import org.netbeans.modules.j2ee.sun.share.config.Utils;

import com.sun.tools.j2ee.editor.PanelView;


/**
 *
 * @author  Jeri Lockhart
 */
public class ConfigBeanPanelView extends PanelView {


    private CardLayout deck = new CardLayout();
    private Map map = new HashMap();
    private JPanel blankPanel = new JPanel();
    private static String BLANK_PANEL = "ConfigBeanPanelView_blank_panel";

    /** Creates a new instance of ConfigBeanPanelView */
    public ConfigBeanPanelView(Node root) {
        setRoot(root);
        setActivatedNodes(new Node[] { root });
        initComponents();
    }


    protected void initComponents() {
        setLayout(deck);
        setPopupAllowed(true);
        blankPanel.setName(BLANK_PANEL);
        add(BLANK_PANEL, blankPanel);
    }

    /** Called when explorer manager has changed the current selection.
     * The view should display the panel corresponding to the selected nodes
     *
     * @param nodes the nodes used to update the view
     *
     */
    public void showSelection(Node[] nodes) {
        // Sanity check
        if (nodes == null || nodes.length==0)
            return;

        //  lookup panel to show
        setActivatedNodes(nodes);
        Node selNode = (Node) nodes[0];
        Component nodePanel = getPanel(selNode);
        if (nodePanel != null) {
		if (nodePanel == blankPanel) {
				deck.show(this,BLANK_PANEL);
			}
			else {
            	deck.show(this,String.valueOf(nodePanel.hashCode()));
            }
        }
    }

    public Component getPanel(final Node selNode){
        // Lookup panel from node
        Component panel = null;
        ConfigBeanNode node = null;
        if (selNode instanceof ConfigBeanNode) {
            node = (ConfigBeanNode) selNode;
        }
        else {
            return blankPanel; // Show blank panel
        }
        panel = (Component)map.get(selNode);
        // found a panel, return it
        if (panel!=null) {
            if(panel instanceof Customizer)
                ((Customizer)panel).setObject(node.getBean());
            return panel;
        }
        
        // nodeListener will clean up when the node goes away
        node.addNodeListener(new NodeAdapter() {
            public void childrenRemoved(final NodeMemberEvent ev) {
                Utils.runInEventDispatchThread(new Runnable() {
                    public void run() {
                        Children children = selNode.getChildren();
                        Node[] nodes = children.getNodes();
                        // display a sibling node if possible, parent node otherwise
                        showSelection(new Node[] {nodes.length > 0 ? nodes[0] : selNode});
                    }
                });
            }
        });
        
        // no current panel, use the nodes customizer instead and cache it
        // after milestone 2, this should change to reuse the same panel instead of 1 customizer per method
        //
        panel = node.getCustomizer();
        if (panel!=null) {
            map.put(node,panel);
            add(String.valueOf(panel.hashCode()),panel);
            ((Customizer)panel).setObject(node.getBean());
            return panel;
        }
        else {
            PropertySheetView propSheetView = new PropertySheetView();
            propSheetView.setNodes(new Node[] {node});
            map.put(node, propSheetView);
            propSheetView.setName(node.getDisplayName());
            add(String.valueOf(propSheetView.hashCode()), propSheetView);
            return propSheetView;
        }
    }
}
