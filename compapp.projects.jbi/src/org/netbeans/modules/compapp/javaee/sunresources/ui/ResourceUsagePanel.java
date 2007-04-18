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

package org.netbeans.modules.compapp.javaee.sunresources.ui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import javax.swing.AbstractListModel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import org.netbeans.api.project.Project;
import org.netbeans.modules.compapp.javaee.sunresources.ResourceAggregator;
import org.netbeans.modules.compapp.javaee.sunresources.SunResourcesUtil;


/**
 * @author echou
 *
 */
@SuppressWarnings("serial")
public class ResourceUsagePanel extends JPanel {

    private Project p;
    private JDialog root;
    private Dialog parent;
    private ResourceUsageListModel listModel;
    private JList list;
    
    public ResourceUsagePanel(final Project p, List<ResourceAggregator.ResourceUsage> usages) {
        super(new BorderLayout());
        this.p = p;
        this.listModel = new ResourceUsageListModel(usages);
        initComponents();
    }

    private void initComponents() {
        list = new JList(listModel);
        list.setPreferredSize(new Dimension(400, 300));
        list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        list.addMouseListener(new UsageMouseListener());
        JScrollPane scrollPane = new JScrollPane(list);
        
        add(scrollPane, BorderLayout.NORTH);
    }
    
    public void setRootDialog(JDialog root) {
        this.root = root;
    }
    
    public void setParentDialog(Dialog parent) {
        this.parent = parent;
    }
    
    private void select_actionPerformed() {
        parent.setVisible(false);
        parent.dispose();
        root.setVisible(false);
        root.dispose();
        
        SunResourcesUtil.openSourceFile(this.p, 
                listModel.getUsageAt(list.getSelectedIndex()).sourceName);
    }
    
    public class ResourceUsageListModel extends AbstractListModel {
        
        private List<ResourceAggregator.ResourceUsage> usages;
        
        public ResourceUsageListModel(List<ResourceAggregator.ResourceUsage> usages) {
            this.usages = usages;
        }
        
        public int getSize() {
            return usages.size();
        }

        public Object getElementAt(int index) {
            ResourceAggregator.ResourceUsage usage = usages.get(index);
            return usage.sourceName;
        }
        
        public ResourceAggregator.ResourceUsage getUsageAt(int index) {
            return usages.get(index);
        }
    }
    
    public class UsageMouseListener implements MouseListener {
        public void mouseClicked(MouseEvent e) {
            if (list.getSelectedIndex() == -1) {
                return;
            }
            if (e.getClickCount() == 2) {
                select_actionPerformed();
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
    }
    
    /*
    public static void main(String[] args) {
        try {
            
            ResourceUsagePanel panel = new ResourceUsagePanel();
            
            JFrame frame = new JFrame("Resource Usage");
            frame.add(panel);
            
            int width = 600;
            int height = 300;
            frame.add (panel, BorderLayout.CENTER);
            frame.setDefaultCloseOperation (JFrame.DISPOSE_ON_CLOSE);
            java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
            frame.setBounds((screenSize.width-width)/2, (screenSize.height-height)/2, width, height);
            frame.setVisible (true);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
     */
}
