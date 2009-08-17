/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
