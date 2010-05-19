/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.worklist.editor.designview;

import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.modules.wlm.model.api.TEmail;
import org.netbeans.modules.wlm.model.api.TNotification;
import org.netbeans.modules.wlm.model.api.TTask;
import org.netbeans.modules.wlm.model.api.WLMComponent;
import org.netbeans.modules.wlm.model.api.WLMModel;
import org.netbeans.modules.worklist.editor.designview.components.ExTabbedPane;
import org.netbeans.modules.worklist.editor.designview.components.ExUtils;
import org.netbeans.modules.worklist.editor.designview.components.LinkButton;
import org.netbeans.modules.worklist.editor.designview.components.StyledLabel;
import org.netbeans.modules.worklist.editor.designview.components.TitledPanel;
import org.netbeans.modules.worklist.editor.nodes.NotificationsNode;
import org.netbeans.modules.worklist.editor.nodes.WLMNodeType;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author anjeleevich
 */
public class NotificationsPanel extends DesignViewPanel implements Widget {
    
    private AddNotificationAction addNotificationAction;
    private AddNotification addNotification;
    
    public NotificationsPanel(DesignView designView) {
        super(designView);

        ExUtils.setA11Y(this, "NotificationsPanel"); // NOI18N

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        addNotificationAction = new AddNotificationAction();
        addNotification = new AddNotification();
        
        add(addNotification);
        
        processWLMModelChanged(false);
    }
    
    void addToTabbedPane(ExTabbedPane tabbedPane) {
        ExTabbedPane.Tab tab = tabbedPane.addTab(NAME, this, true,
                null, null);
        
        tab.addHeaderRow(getMessage("LBL_NOTIFICATIONS"), // NOI18N
                null, // NOI18N
                StyledLabel.PLAIN_STYLE);      
        
        processWLMModelChanged(false);
    }
    
    public void processWLMModelChanged(boolean processChildren) {
        TTask task = getTask();
        List<TNotification> notificationsList = (task == null) ? null 
                : task.getNotifications();
        
        if (notificationsList == null || notificationsList.size() == 0) {
            for (int i = getComponentCount() - 2; i >= 0; i--) {
                remove(i);
            }
        } else {
            Map<TNotification, NotificationPanel> modelToViewMap 
                    = new HashMap<TNotification, NotificationPanel>();
            
            for (int i = getComponentCount() - 3; i >= 0; i -= 2) {
                Component component = getComponent(i);
                NotificationPanel notificationPanel 
                        = ((NotificationPanel) ((TitledPanel) component)
                        .getContent());
                
                TNotification notification = notificationPanel
                        .getNotification();
                if (notificationsList.contains(notification)) {
                    modelToViewMap.put(notificationPanel.getNotification(), 
                            notificationPanel);
                } else {
                    remove(i + 1);
                    remove(i);
                }
            }
            
            for (int i = 0; i < notificationsList.size(); i++) {
                TNotification notification = notificationsList.get(i);

                NotificationPanel notificationPanel = modelToViewMap
                        .get(notification);
                
                if (notificationPanel == null) {
                    notificationPanel = new NotificationPanel(this, 
                            getDesignView(), notification);
                    add(notificationPanel.getView(), i * 2);
                    add(Box.createVerticalStrut(12), i * 2 + 1)
                            .setFocusable(false);
                } 
           }

            if (processChildren) {
                for (NotificationPanel notificationPanel 
                        : modelToViewMap.values()) 
                {
                    notificationPanel.processWLMModelChanged();
                }
            }
        }
        
        revalidate();
        repaint();
    }

    public Widget getWidgetParent() {
        return getDesignView();
    }

    public Widget getWidget(int index) {
        int componentCount = getComponentCount();

        for (int i = 0; i <= componentCount; i++) {
            Component component = getComponent(i);
            if (component instanceof TitledPanel) {
                JComponent content = ((TitledPanel) component).getContent();
                if (content instanceof NotificationPanel) {
                    if (index == 0) {
                        return (NotificationPanel) content;
                    } else {
                        index--;
                    }
                }
            }
        }

        throw new IndexOutOfBoundsException();
    }

    public int getWidgetCount() {
        int count = 0;
        for (int i = getComponentCount() - 1; i >= 0; i--) {
            Component component = getComponent(i);
            if ((component instanceof TitledPanel) && (((TitledPanel)
                    component).getContent() instanceof NotificationPanel))
            {
                count++;
            }
        }
        return count;
    }

    public Node getWidgetNode() {
        return new NotificationsNode(getTask(), Children.LEAF, getNodeLookup());
    }

    public void requestFocusToWidget() {
        getDesignView().showNotificationsTab();
    }

    public WLMComponent getWidgetWLMComponent() {
        return getTask();
    }

    public WLMNodeType getWidgetNodeType() {
        return WLMNodeType.NOTIFICATIONS;
    }
    
    private class AddNotification extends JPanel implements FocusListener {
        private LinkButton addNotificationButton;

        AddNotification() {
            setOpaque(false);

            ExUtils.setA11Y(this, NotificationsPanel.class,
                    "AddNotificationsPanel"); // NOI18N

            addNotificationButton = new LinkButton(getMessage(
                    "LBL_ADD_NOTIFICATION")); // NOI18N
            addNotificationButton.addActionListener(addNotificationAction);
            addNotificationButton.addFocusListener(this);
            ExUtils.setA11Y(addNotificationButton, NotificationsPanel.class,
                    "AddNotificationsButton"); // NOI18N

            add(addNotificationButton);
        }
        
        @Override
        public Insets getInsets() {
            return new Insets(8, 8, 8, 8);
        }

        @Override
        public void doLayout() {
            Insets insets = getInsets();
            
            int w = getWidth() - insets.left - insets.right;
            int h = getHeight() - insets.top - insets.bottom;
            
            Dimension size = addNotificationButton.getPreferredSize();
            
            addNotificationButton.setBounds(
                    insets.left + (w - size.width) / 2,
                    insets.top + (h - size.height) / 2, 
                    size.width, 
                    size.height);
        }

        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            Stroke oldStoke = g2.getStroke();
            
            g2.setColor(ExTabbedPane.TAB_BORDER_COLOR);
            g2.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, 
                    BasicStroke.JOIN_ROUND, 1, new float[] { 4, 4 } , 0));
            g2.drawRect(1, 1, getWidth() - 3, getHeight() - 3);
            
            g2.setStroke(oldStoke);
        }
        
        @Override
        public Dimension getPreferredSize() {
            Insets insets = getInsets();
            Dimension size = addNotificationButton.getPreferredSize();
            size.width += insets.left + insets.right;
            size.height += insets.top + insets.bottom;
            return size;
        }
        
        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
        }

        public void focusGained(FocusEvent e) {
            selectWidget(NotificationsPanel.this);
        }

        public void focusLost(FocusEvent e) {
            // do nothing
        }
    }
    
    
    private class AddNotificationAction extends AbstractAction {
        AddNotificationAction() {
            super(getMessage("LBL_ADD_NOTIFICATION")); // NOI18N
        }
        
        public void actionPerformed(ActionEvent event) {
            Operation operation = null;
            
            try {
                operation = getEMailNotificationOperation();
            } catch (Exception ex) {
                // do nothing
            }
            
            WLMModel model = getModel();
            TTask task = model.getTask();

            Set<String> notificationsNames = new HashSet<String>();
            
            List<TNotification> notifications = task.getNotifications();
            if (notifications != null) {
                for (TNotification notification : notifications) {
                    String name = notification.getName();
                    if (name == null) {
                        name = ""; // NOI18N
                    } else {
                        name = name.trim();
                    }
                    
                    if (name.length() > 0) {
                        notificationsNames.add(name);
                    }
                }
            }
            
            String baseNotificationName = "newNotification"; // NOI18N
            String notificationName = baseNotificationName;
            int i = 1;
            
            while (notificationsNames.contains(notificationName)) {
                notificationName = baseNotificationName + i;
                i++;
            }

            if (model.startTransaction()) {
                TNotification notification = null;
                try {
                    notification = model.getFactory().createNotification(model);
                    notification.setName(notificationName);
                    task.addNotification(notification);
                    
                    if (operation != null) {
                        TEmail email = model.getFactory().createEmail(model);
                        notification.setEmail(email);
                        email.setOperation(email
                                .createOperationReference(operation));
                    }
                } finally {
                    model.endTransaction();
                }
            }             
        }
    }
    
    private Operation getEMailNotificationOperation() {
        FileObject folder = getDataObject().getPrimaryFile()
                .getParent();
        
        FileObject emailWSDL = folder.getFileObject(EMAIL_WSDL_NAME, 
                EMAIL_WSDL_EXT);
        
        if (emailWSDL == null) {
            InputStream input = null;
            OutputStream output = null;
            FileLock lock = null;
            
            try {
                emailWSDL = folder.createData(EMAIL_WSDL_NAME, EMAIL_WSDL_EXT);
                lock = emailWSDL.lock();
                
                output = emailWSDL.getOutputStream(lock);
                input = getClass().getResourceAsStream(
                        "/org/netbeans/modules/worklist/editor/" // NOI18N
                        + EMAIL_WSDL_NAME + "." + EMAIL_WSDL_EXT); // NOI18N
                
                int b = input.read();
                while (b >= 0) {
                    output.write(b);
                    b = input.read();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                return null;
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            } finally {
                if (input != null) {
                    try {
                        input.close();
                    } catch (Exception ex) {
                        // do nothing
                    }
                }
                
                if (output != null) {
                    try {
                        output.close();
                    } catch (Exception ex) {
                        // do nothing
                    }
                }
                
                if (lock != null) {
                    try {
                        lock.releaseLock();
                    } catch (Exception ex) {
                        // do nothing
                    }
                }
            }
        }
        
        ModelSource wsdlModelSource = Utilities.getModelSource(emailWSDL, 
                emailWSDL.canWrite());
        WSDLModel wsdlModel = null;
        if (wsdlModelSource != null) {
            wsdlModel = WSDLModelFactory.getDefault()
                    .getModel(wsdlModelSource);
        }
        
        if (wsdlModel == null) {
            return null;
        }
        
        Definitions definitions = wsdlModel.getDefinitions();
        if (definitions == null) {
            return null;
        }
        
        Collection<PortType> portTypes = definitions.getPortTypes();
        if (portTypes == null || portTypes.isEmpty()) {
            return null;
        }
        
        for (PortType portType : portTypes) {
            Collection<Operation> operations = portType.getOperations();
            if (operations != null && !operations.isEmpty()) {
                return operations.iterator().next();
            }
        }
        
        return null;
    }
    
    private static String NAME = "NOTIFICATIONS"; // NOI18N
    
    private static String EMAIL_WSDL_NAME = "EmailNotificationHandler"; // NOI18N
    private static String EMAIL_WSDL_EXT = "wsdl"; // NOI18N
    
    private static String getMessage(String key) {
        return NbBundle.getMessage(NotificationsPanel.class, key);
    }        
}
