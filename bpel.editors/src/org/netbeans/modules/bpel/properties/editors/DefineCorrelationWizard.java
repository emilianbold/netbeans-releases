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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bpel.properties.editors;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.text.MessageFormat;
import java.util.*;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.netbeans.modules.bpel.model.api.Activity;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Invoke;
import org.netbeans.modules.bpel.model.api.OnAlarmPick;
import org.netbeans.modules.bpel.model.api.OnEvent;
import org.netbeans.modules.bpel.model.api.OnMessage;
import org.netbeans.modules.bpel.model.api.Pick;
import org.netbeans.modules.bpel.model.api.PortTypeReference;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.Receive;
import org.netbeans.modules.bpel.model.api.Reply;
import org.netbeans.modules.bpel.model.api.Requester;
import org.netbeans.modules.bpel.model.api.Responder;
import org.netbeans.modules.bpel.model.api.Scope;
import org.netbeans.modules.bpel.model.api.Sequence;
import org.netbeans.modules.bpel.model.impl.BpelBuilderImpl;
import org.netbeans.modules.bpel.model.impl.BpelModelImpl;
import org.netbeans.modules.bpel.model.xam.BpelAttributes;
import org.netbeans.modules.bpel.nodes.BpelNode;
import org.netbeans.modules.soa.mappercore.DefaultMapperContext;
import org.netbeans.modules.soa.mappercore.Mapper;
import org.netbeans.modules.soa.mappercore.model.Graph;
import org.netbeans.modules.soa.mappercore.model.GraphSubset;
import org.netbeans.modules.soa.mappercore.model.Link;
import org.netbeans.modules.soa.mappercore.model.MapperModel;
import org.netbeans.modules.soa.mappercore.model.SourcePin;
import org.netbeans.modules.soa.mappercore.model.TargetPin;
import org.netbeans.modules.soa.mappercore.model.TreeSourcePin;
import org.netbeans.modules.soa.mappercore.model.VertexItem;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.OperationParameter;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.WizardValidationException;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * @author Alex Petrov (27.12.2007)
 */
public class DefineCorrelationWizard implements WizardProperties {
    private static final long serialVersionUID = 1L;
    
    private static final Dimension LEFT_DIMENSION_VALUE = new Dimension(200, 450);
    private static final Dimension PANEL_DIMENSION_VALUE = new Dimension(600, 450);
    private static final String[] WIZARD_STEP_NAMES = new String[] {
        NbBundle.getMessage(DefineCorrelationWizard.class, "LBL_Wizard_Step_Select_Messaging_Activity"),
        NbBundle.getMessage(DefineCorrelationWizard.class, "LBL_Wizard_Step_Define_Correlation"),
        NbBundle.getMessage(DefineCorrelationWizard.class, "LBL_Wizard_Step_Correlation_Configuration")
    };

    private static Graph TMP_FAKE_GRAPH;

    private static final String 
        IMAGE_FOLDER_NAME = "org/netbeans/modules/bpel/editors/api/nodes/images/", // NOI18N
        IMAGE_FILE_EXT = ".png", // NOI18N
        JBUTTON_TEXT_NEXT = "Next", // NOI18N
        JBUTTON_TEXT_FINISH = "Finish"; // NOI18N

    private Map<Class, Icon> mapTreeNodeIcons;
    
    private WizardDescriptor wizardDescriptor;
    private BpelEntity mainBpelEntity;
    private Panel[] wizardPanels;
    private JButton buttonNext, buttonFinish;
    
    public DefineCorrelationWizard(BpelNode mainBpelNode) {
        Object mainBpelNodeRef = mainBpelNode.getReference();
        if (mainBpelNodeRef instanceof BpelEntity) {
            this.mainBpelEntity = (BpelEntity) mainBpelNodeRef;
        }
        mapTreeNodeIcons = createTreeNodeIconsMap();
            
        wizardPanels = getWizardPanelList().toArray(new Panel[] {});
        wizardDescriptor = new WizardDescriptor(wizardPanels);
        
        wizardDescriptor.putProperty(PROPERTY_AUTO_WIZARD_STYLE, true);
        wizardDescriptor.putProperty(PROPERTY_CONTENT_DISPLAYED, true);
        wizardDescriptor.putProperty(PROPERTY_CONTENT_NUMBERED, true);
        wizardDescriptor.putProperty(PROPERTY_HELP_DISPLAYED, false);
        wizardDescriptor.putProperty(PROPERTY_LEFT_DIMENSION, LEFT_DIMENSION_VALUE);
        wizardDescriptor.putProperty(PROPERTY_CONTENT_DATA, WIZARD_STEP_NAMES);
        
        wizardDescriptor.setTitleFormat(new MessageFormat(
            NbBundle.getMessage(DefineCorrelationWizard.class, 
            "LBL_DefineCorrelation_Wizard_Title_Format")));
        wizardDescriptor.setTitle(NbBundle.getMessage(DefineCorrelationWizard.class, 
            "LBL_DefineCorrelation_Wizard_Title"));
    }
    
    public void showWizardDialog() {
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        findNextAndFinishButtons(dialog);
        dialog.setPreferredSize(new Dimension(
            LEFT_DIMENSION_VALUE.width + PANEL_DIMENSION_VALUE.width + 50, 
            PANEL_DIMENSION_VALUE.height));
        dialog.pack();
        wizardPanels[0].isValid();
        dialog.setVisible(true);
    }
    
    private List<Panel> getWizardPanelList() {
        List<Panel> panelList = new ArrayList<Panel>(WIZARD_STEP_NAMES.length);
        panelList.add(new WizardSelectMessagingActivityPanel());
        panelList.add(new WizardDefineCorrelationPanel());
        panelList.add(new WizardCorrelationConfigurationPanel());
        for (int i = 0; i < panelList.size(); ++i) {
            ((WizardAbstractPanel) panelList.get(i)).setPanelNameIndex(
                WIZARD_STEP_NAMES[i], i);
        }
        return panelList;
    }
    
    private Map<Class, Icon> createTreeNodeIconsMap() {
        if (mapTreeNodeIcons != null) return mapTreeNodeIcons;
            
        Map<Class, Icon> mapIcons =  new HashMap<Class, Icon>(9);
        
        String iconFileName = IMAGE_FOLDER_NAME + "UNKNOWN_TYPE" + IMAGE_FILE_EXT; // NOI18N
        mapIcons.put(Object.class, new ImageIcon(Utilities.loadImage(iconFileName)));

        iconFileName = IMAGE_FOLDER_NAME + "RECEIVE" + IMAGE_FILE_EXT; // NOI18N
        mapIcons.put(Receive.class, new ImageIcon(Utilities.loadImage(iconFileName)));

        iconFileName = IMAGE_FOLDER_NAME + "REPLY" + IMAGE_FILE_EXT; // NOI18N
        mapIcons.put(Reply.class, new ImageIcon(Utilities.loadImage(iconFileName)));

        iconFileName = IMAGE_FOLDER_NAME + "INVOKE" + IMAGE_FILE_EXT; // NOI18N
        mapIcons.put(Invoke.class, new ImageIcon(Utilities.loadImage(iconFileName)));
        
        iconFileName = IMAGE_FOLDER_NAME + "MESSAGE_TYPE" + IMAGE_FILE_EXT; // NOI18N
        mapIcons.put(Message.class, new ImageIcon(Utilities.loadImage(iconFileName)));
        
        iconFileName = IMAGE_FOLDER_NAME + "MESSAGE_PART" + IMAGE_FILE_EXT; // NOI18N
        mapIcons.put(Part.class, new ImageIcon(Utilities.loadImage(iconFileName)));
        
        iconFileName = IMAGE_FOLDER_NAME + "ON_EVENT" + IMAGE_FILE_EXT; // NOI18N
        mapIcons.put(OnEvent.class, new ImageIcon(Utilities.loadImage(iconFileName)));
        
        iconFileName = IMAGE_FOLDER_NAME + "MESSAGE_HANDLER" + IMAGE_FILE_EXT; // NOI18N
        mapIcons.put(OnMessage.class, new ImageIcon(Utilities.loadImage(iconFileName)));
    
        iconFileName = IMAGE_FOLDER_NAME + "GLOBAL_ELEMENT" + IMAGE_FILE_EXT; // NOI18N
        mapIcons.put(GlobalElement.class, new ImageIcon(Utilities.loadImage(iconFileName)));
        
        return mapIcons;
    }
    
    private void findNextAndFinishButtons(Container container) {
        Component[] components = container.getComponents();
        for (Component component : components) {
            if (component instanceof Container) {
                findNextAndFinishButtons((Container) component);
                if ((buttonNext != null) && (buttonFinish != null)) return;
            }
            if (component instanceof JButton) {
                if (((JButton) component).getText().startsWith(JBUTTON_TEXT_NEXT)) {
                    buttonNext = (JButton) component;
                } else if (((JButton) component).getText().startsWith(JBUTTON_TEXT_FINISH)) {
                    buttonFinish = (JButton) component;
                }
            }
            if ((buttonNext != null) && (buttonFinish != null)) return;
        }
    }
    //========================================================================//
    private interface ActivityChooser {
        List<BpelEntity> getPermittedActivityList(BpelEntity mainBpelEntity);
    }
    
    private abstract class AbstractActivityChooser implements ActivityChooser {
        protected Set<Class> permittedActivityTypeSet = new HashSet<Class>(Arrays.asList(
            new Class[] {Requester.class, Responder.class}));
        
        protected BpelEntity getTopParentEntity(BpelEntity mainBpelEntity) {
            BpelEntity parentEntity = mainBpelEntity.getParent();
            while ((parentEntity != null) && 
                   (!(parentEntity instanceof Scope)) && 
                   (!(parentEntity instanceof Process))) {
                if (parentEntity == null) return null;
                parentEntity = parentEntity.getParent();
            }
            return parentEntity;    
        }
        
        public List<BpelEntity> getPermittedActivityList(BpelEntity mainBpelEntity) {
            List<BpelEntity> bpelEntityList = new ArrayList<BpelEntity>();
            BpelEntity parentEntity = getTopParentEntity(mainBpelEntity);
            if (parentEntity == null) return bpelEntityList;
            
            List<BpelEntity> activities = chooseActivities(parentEntity, new ArrayList<BpelEntity>());
            for (BpelEntity bpelEntity : activities) {
                if (mainBpelEntity.equals(bpelEntity)) {
                    bpelEntityList.add(bpelEntity);
                } else {
                    for (Class permittedActivityType : permittedActivityTypeSet) {
                        try {
                            // check that Class of activity object implements/extends
                            // the appropriate class "permittedActivityType"
                            bpelEntity.getClass().asSubclass(permittedActivityType);
                            bpelEntityList.add(bpelEntity);
                            // break  this cycle for avoiding of adding the same activity 
                            // twice, if the given activity object implements/extends
                            // several classes "permittedActivityType" (for example,
                            // Invoke, which is inherited from Responder and Requester)
                            break; 
                        } catch (ClassCastException exception) {}
                    }
                }
            }
            return bpelEntityList;
        }
    
        private List<BpelEntity> chooseActivities(BpelEntity bpelEntity, 
            List<BpelEntity> bpelEntityList) {
            if (bpelEntityList == null) return (new ArrayList<BpelEntity>());
            
            if (bpelEntity instanceof Sequence) {
                for (Activity activity : bpelEntity.getChildren(Activity.class)) {
                    bpelEntityList.addAll(chooseActivities(activity, new ArrayList<BpelEntity>()));
                }
            } else if ((bpelEntity instanceof Requester) || (bpelEntity instanceof Responder)) {
                bpelEntityList.add(bpelEntity);
            } else if (bpelEntity instanceof Pick) {
                for (OnMessage onMessage : ((Pick) bpelEntity).getOnMessages()) {
                    bpelEntityList.add(onMessage);
                    for (Sequence sequence : onMessage.getChildren(Sequence.class)) {
                        bpelEntityList.addAll(chooseActivities(sequence, new ArrayList<BpelEntity>()));
                    }
                }
                for (OnAlarmPick onAlarmPick : ((Pick) bpelEntity).getOnAlarms()) {
                    for (Sequence sequence : onAlarmPick.getChildren(Sequence.class)) {
                        bpelEntityList.addAll(chooseActivities(sequence, new ArrayList<BpelEntity>()));
                    }
                }
            } else {
                for (Sequence sequence : bpelEntity.getChildren(Sequence.class)) {
                    bpelEntityList.addAll(chooseActivities(sequence, new ArrayList<BpelEntity>()));
                }
            }
            return bpelEntityList;
        }
        
        protected  List<BpelEntity> removeResponderActivityAbove(List<BpelEntity> activityList, 
            BpelEntity mainBpelEntity) {
            // remove all Responder-activities above mainBpelEntity
            BpelEntity activity = null;
            int index = 0;
            while (true) {
                activity = activityList.get(index);
                if (activity.equals(mainBpelEntity)) break;

                if ((activity instanceof Responder) && (! (activity instanceof Requester))) {
                    activityList.remove(index);
                } else {
                    ++index;
                }
            }
            return activityList;
        }
        
        protected  List<BpelEntity> removeRequesterActivityBelow(List<BpelEntity> activityList, 
            BpelEntity mainBpelEntity) {
            // remove all Requester-activities below mainBpelEntity
            BpelEntity activity = null;
            int index = activityList.size() - 1;
            while (true) {
                activity = activityList.get(index);
                if (activity.equals(mainBpelEntity)) break;    
                    
                if ((activity instanceof Requester) && (! (activity instanceof Responder))) {
                    activityList.remove(index);
                }
                --index;
            }
            return  activityList;
        }
    }
    
    private class RequesterActivityChooser extends AbstractActivityChooser {
        public RequesterActivityChooser() {
            permittedActivityTypeSet = new HashSet<Class>(Arrays.asList(new Class[] {
                Responder.class}));
        }
        
        @Override
        public List<BpelEntity> getPermittedActivityList(BpelEntity mainBpelEntity) {
            List<BpelEntity> activityList = super.getPermittedActivityList(mainBpelEntity);
            if (activityList.isEmpty()) return activityList;
            
            // remove all Responder-activities above mainBpelEntity and mainBpelEntity itself
            activityList = removeResponderActivityAbove(activityList, mainBpelEntity);
            activityList.remove(mainBpelEntity);
            return activityList;
        }
    }
    
    private class ResponderActivityChooser extends AbstractActivityChooser  {
        public ResponderActivityChooser() {
            permittedActivityTypeSet = new HashSet<Class>(Arrays.asList(new Class[] {
                Requester.class}));
        }
        
        @Override
        public List<BpelEntity> getPermittedActivityList(BpelEntity mainBpelEntity) {
            List<BpelEntity> activityList = super.getPermittedActivityList(mainBpelEntity);
            if (activityList.isEmpty()) return activityList;

            // remove all Requester-activities below mainBpelEntity and mainBpelEntity itself
            activityList = removeRequesterActivityBelow(activityList, mainBpelEntity);
            activityList.remove(mainBpelEntity);
            return activityList;
        }
    }
    
    private class RequesterResponderActivityChooser extends AbstractActivityChooser  {
        @Override
        public List<BpelEntity> getPermittedActivityList(BpelEntity mainBpelEntity) {
            List<BpelEntity> activityList = super.getPermittedActivityList(mainBpelEntity);
            if (activityList.isEmpty()) return activityList;
            
            // remove all Responder-activities above mainBpelEntity
            activityList = removeResponderActivityAbove(activityList, mainBpelEntity);
            
            // remove all Requester-activities below mainBpelEntity
            activityList = removeRequesterActivityBelow(activityList, mainBpelEntity);
            
            // remove mainBpelEntity from the list
            activityList.remove(mainBpelEntity);
            
            return  activityList;
        }
    }
    //========================================================================//
    public abstract class WizardAbstractPanel implements WizardDescriptor.ValidatingPanel {
        protected JPanel wizardPanel = createWizardPanel();
        protected ChangeSupport changeSupport = new ChangeSupport(this);
        protected GridBagConstraints gbc = new GridBagConstraints();
        protected int insetX = 5, insetY = 5;
        
        protected JPanel createWizardPanel() {
            JPanel panel = new JPanel();
            panel.setPreferredSize(PANEL_DIMENSION_VALUE);
            return panel;
        }
        
        protected void setPanelNameIndex(String name, int index) {
            wizardPanel.setName(name);
            wizardPanel.putClientProperty(PROPERTY_CONTENT_SELECTED_INDEX, index);
        }
        
        public Component getComponent() {
            return wizardPanel;
        }
        
        public HelpCtx getHelp() {
            return null;
        }
        public void addChangeListener(ChangeListener listener) {
            changeSupport.addChangeListener(listener);
        }
        public void removeChangeListener(ChangeListener listener) {
            changeSupport.removeChangeListener(listener);
        }
        
        public void readSettings(Object settings) {}
        public void storeSettings(Object settings) {}

        public void validate() throws WizardValidationException {}

        public boolean isValid() {
            return true;
        }
        
        protected void initializeGridBagConstraints() {
            gbc.gridx = 0; gbc.gridy = 0;
            gbc.gridwidth = 1; gbc.gridheight = 1;
            gbc.ipadx = 0; gbc.ipady = 0;
            gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.insets = new Insets(insetY, insetX, insetY, insetX);
            gbc.weightx = 1.0; gbc.weighty = 1.0;
        }
    }
    //========================================================================//
    public class WizardSelectMessagingActivityPanel extends WizardAbstractPanel {
        private final Dimension COMBOBOX_DIMENSION = new Dimension(350, 20);
        private final int COMBOBOX_MAX_ROW_COUNT = 16;
        private final JComboBox activityComboBox = new JComboBox();
        private BpelEntity previousSelectedActivity, currentSelectedActivity;
            
        public WizardSelectMessagingActivityPanel() {
            super();
            wizardPanel.setLayout(new FlowLayout(FlowLayout.LEFT, insetX, insetY));
            wizardPanel.add(new JLabel(NbBundle.getMessage(
                WizardSelectMessagingActivityPanel.class, "LBL_Initiated_Messaging_Activities")));

            fillActivityComboBox();
            activityComboBox.setRenderer(new ComboBoxRenderer());
            activityComboBox.setMaximumRowCount(COMBOBOX_MAX_ROW_COUNT);
            activityComboBox.setEditable(false);
            activityComboBox.setMinimumSize(COMBOBOX_DIMENSION);
            activityComboBox.setPreferredSize(activityComboBox.getMinimumSize());
            wizardPanel.add(activityComboBox);
        }

        private void fillActivityComboBox() {
            ActivityChooser activityChooser = null;
            if ((mainBpelEntity instanceof Requester) && 
                (mainBpelEntity instanceof Responder)) { // Invoke
                activityChooser = new RequesterResponderActivityChooser();
            } else if (mainBpelEntity instanceof Requester) {
                activityChooser = new RequesterActivityChooser();
            }  else if (mainBpelEntity instanceof Responder) {
                activityChooser = new ResponderActivityChooser();
            }
            if (activityChooser == null) {
                String errMsg = "Activity Chooser isn't defined for Bpel Entity of type [" +
                    mainBpelEntity.getElementType().getName() + "]";
                System.err.println(errMsg);
                System.out.println(errMsg);
                return;
            }
            List<BpelEntity> activityEntityList = activityChooser.getPermittedActivityList(mainBpelEntity);
            if (activityEntityList != null) {
                ((DefaultComboBoxModel) activityComboBox.getModel()).removeAllElements();
                for (BpelEntity activityEntity : activityEntityList) {
                    activityComboBox.addItem(activityEntity);
                }
            }
        }
        
        @Override
        public boolean isValid() {
            boolean isOK = ((activityComboBox.getItemCount() > 0) &&
                            (activityComboBox.getSelectedItem() != null));
            
            wizardDescriptor.putProperty(PROPERTY_ERROR_MESSAGE, isOK ? null :
                NbBundle.getMessage(WizardSelectMessagingActivityPanel.class, "LBL_ErrMsg_No_Activity_For_Correlation"));                              
            if (buttonNext != null) buttonNext.setEnabled(isOK);
            return isOK;
        }

        @Override
        public void validate() throws WizardValidationException {
            previousSelectedActivity = currentSelectedActivity;
            currentSelectedActivity = (BpelEntity) activityComboBox.getSelectedItem();
            WizardDefineCorrelationPanel wizardDefineCorrelationPanel = 
                ((WizardDefineCorrelationPanel) wizardPanels[1]);
            if (previousSelectedActivity == null) { // this panel is shown for the 1st time
                wizardDefineCorrelationPanel.buildCorrelationMapper(mainBpelEntity, currentSelectedActivity);
            } else { // this panel is shown after clicking of the button "Back"
                if (! previousSelectedActivity.equals(currentSelectedActivity)) {
                    wizardDefineCorrelationPanel.buildCorrelationMapper(null, currentSelectedActivity);
                }
            }
        }
        //====================================================================//
        private class ComboBoxRenderer extends BasicComboBoxRenderer.UIResource {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if ((value != null) && (value instanceof BpelEntity) &&
                    (component != null) && (component instanceof JLabel)) {
                    String itemText = null;
                    try {
                        itemText = ((BpelEntity) value).getAttribute(BpelAttributes.NAME);
                    } catch (Exception e) {
                        itemText = value.toString();
                    }
                    String messagePattern = NbBundle.getMessage(ComboBoxRenderer.class, "LBL_ComboBox_Item_Name_Pattern");
                    Object[] messageValues = new Object[] {itemText, ((BpelEntity) value).getElementType().getSimpleName()};
                    if (itemText == null) {
                        if (value instanceof OnMessage) {
                            String pickEntityName = ((BpelEntity) value).getParent().getAttribute(BpelAttributes.NAME),
                                   operationName = ((BpelEntity) value).getAttribute(BpelAttributes.OPERATION);
                            itemText = ((BpelEntity) value).getElementType().getSimpleName();
                            messagePattern = NbBundle.getMessage(ComboBoxRenderer.class, "LBL_ComboBox_OnMessage_Name_Pattern");
                            messageValues = new Object[] {itemText, pickEntityName, operationName};
                        }
                    }
                    ((JLabel) component).setText(MessageFormat.format(messagePattern, messageValues));
                }
                return component;
            }
        }
    }
    //========================================================================//
    public class WizardDefineCorrelationPanel extends WizardAbstractPanel {
        private final String ACTION_KEY_DELETE = "ACTION_KEY_DELETE";
            
        private Mapper correlationMapper;
        
        public WizardDefineCorrelationPanel() {
            super();
        }
        
        public void buildCorrelationMapper(BpelEntity leftBpelEntity, BpelEntity rightBpelEntity) {
            boolean isMapperChanged = (correlationMapper == null);
            CorrelationMapperTreeModel 
                leftTreeModel  = (CorrelationMapperTreeModel) (correlationMapper == null ? null : 
                    correlationMapper.getModel().getLeftTreeModel()), 
                rightTreeModel = (CorrelationMapperTreeModel) (correlationMapper == null ? null : 
                    ((CorrelationMapperModel) correlationMapper.getModel()).getRightTreeModel());
            if (leftBpelEntity != null) {
                leftTreeModel = new CorrelationMapperTreeModel();
                CorrelationMapperTreeNode topLeftTreeNode = buildCorrelationTree(leftBpelEntity,
                    NbBundle.getMessage(WizardDefineCorrelationPanel.class, 
                    leftBpelEntity instanceof OnMessage ? "LBL_Mapper_Tree_OnMessage_Name_Pattern" : 
                                                          "LBL_Mapper_Tree_Top_Node_Name_Pattern"));
                leftTreeModel.buildCorrelationMapperTree(topLeftTreeNode);
                isMapperChanged = true;
            }
            if (rightBpelEntity != null) {
                rightTreeModel = new CorrelationMapperTreeModel();
                CorrelationMapperTreeNode topRightTreeNode = buildCorrelationTree(rightBpelEntity,
                    NbBundle.getMessage(WizardDefineCorrelationPanel.class, 
                    rightBpelEntity instanceof OnMessage ? "LBL_Mapper_Tree_OnMessage_Name_Pattern" : 
                                                           "LBL_Mapper_Tree_Top_Node_Name_Pattern"));
                rightTreeModel.buildCorrelationMapperTree(topRightTreeNode);
                isMapperChanged = true;
            }
            MapperModel mapperModel = null;
            if (isMapperChanged) {
                mapperModel = new CorrelationMapperModel(leftTreeModel, rightTreeModel);
            }
            if (correlationMapper == null) {
                wizardPanel.setLayout(new BorderLayout());
                EtchedBorder panelBorder = (EtchedBorder) BorderFactory.createEtchedBorder(EtchedBorder.LOWERED, 
                    Color.BLACK, Color.WHITE);
                wizardPanel.setBorder(panelBorder);
                
                correlationMapper = new Mapper(mapperModel);
                correlationMapper.setContext(new CorrelationMapperContext());                
                defineCorrelationMapperKeyBindings();
                
                wizardPanel.add(correlationMapper);
                wizardPanel.revalidate();
            } else if (isMapperChanged) {
                correlationMapper.setModel(mapperModel);
            }
        }

        private CorrelationMapperTreeNode buildCorrelationTree(BpelEntity topBpelEntity, String nodeNamePattern) {
            CorrelationMapperTreeNode topTreeNode = new CorrelationMapperTreeNode(
                topBpelEntity, nodeNamePattern);
            topTreeNode = buildCorrelationTree(topBpelEntity, topTreeNode);
            return topTreeNode;
        }
        
        private CorrelationMapperTreeNode buildCorrelationTree(BpelEntity topBpelEntity, 
            CorrelationMapperTreeNode topTreeNode) {
            PortType portType = ((PortTypeReference) topBpelEntity).getPortType().get();
            Collection<Operation> operations = portType.getOperations();
            String requiredOperationName = topBpelEntity.getAttribute(BpelAttributes.OPERATION);
            Operation requiredOperation = null;
            for (Operation operation : operations) {
                if (operation.getName().equals(requiredOperationName)) {
                    requiredOperation = operation;
                    break;
                }
            }
            handleOperations(topBpelEntity, topTreeNode, requiredOperation);
            return topTreeNode;
        }
        
        private void handleOperations(BpelEntity topBpelEntity, 
            CorrelationMapperTreeNode topTreeNode, Operation operation) {
            List<Message> messages = new ArrayList<Message>();
            if (topBpelEntity instanceof Requester) {
                try {
                    OperationParameter output = operation.getOutput();
                    messages.add(output.getMessage().get());
                } catch (Exception exception) {}
            }
            if (topBpelEntity instanceof Responder) {
                try {
                    OperationParameter input = operation.getInput();
                    messages.add(input.getMessage().get());
                } catch (Exception exception) {}
            }
            handleMessages(topBpelEntity, topTreeNode, messages);
        }
        
        private void handleMessages(BpelEntity topBpelEntity, 
            CorrelationMapperTreeNode topTreeNode, Collection<Message> messages) {
            for (Message message : messages) {
                Collection<Part> parts = message.getParts();
                if (! parts.isEmpty()) {
                    CorrelationMapperTreeNode messageNode = new CorrelationMapperTreeNode(message, null);
                    for (Part part : parts) {
                        messageNode.add(new CorrelationMapperTreeNode(part, null));
                    }
                    topTreeNode.add(messageNode);
                }
            }
        }
        
        private void defineCorrelationMapperKeyBindings() {
            if (correlationMapper == null) return;
            InputMap inputMap = correlationMapper.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), ACTION_KEY_DELETE);
            ActionMap actionMap = correlationMapper.getActionMap();
            actionMap.put(ACTION_KEY_DELETE, new ActionDeleteKey());
        }
        
        @Override
        public boolean isValid() {
            boolean isOK = false;
            // allows moving to the next panel if the right tree 
            // in mapper contains at least 1 link
            Map<TreePath, Graph> mapTreePathGraphs = ((CorrelationMapperModel) correlationMapper.getModel()).getMapTreePathGraphs();
            for (Graph graph : mapTreePathGraphs.values()) {
                isOK = graph.hasIngoingLinks();
                if (isOK) break;
            }
            wizardDescriptor.putProperty(PROPERTY_ERROR_MESSAGE, isOK ? null :
                NbBundle.getMessage(WizardDefineCorrelationPanel.class, "LBL_ErrMsg_No_Links_For_Correlation"));                              
            if (buttonNext != null) buttonNext.setEnabled(isOK);
            return isOK;
        }

        @Override
        public void validate() throws WizardValidationException {
        }
        //====================================================================//
        private class ActionDeleteKey extends AbstractAction {
            public void actionPerformed(ActionEvent e) {
                List<Link> selectedLinks =  correlationMapper.getSelectionModel().getSelectedLinks();
                if ((selectedLinks != null) && (! selectedLinks.isEmpty())) {
                    for (Link link : selectedLinks) {
                        SourcePin sourcePin = link.getSource();
                        if ((sourcePin == null) || (! (sourcePin instanceof TreeSourcePin))) break;

                        Graph targetGraph = link.getGraph();
                        if (targetGraph == null) break;
                        targetGraph.removeLink(link);

                        CorrelationMapperModel mapperModel = (CorrelationMapperModel) correlationMapper.getModel();
                        TreePath targetTreePath = mapperModel.getTreePathByGraph(targetGraph);                        
                        if (targetTreePath == null) break;
                        CorrelationMapperTreeModel rightTreeModel = 
                            (CorrelationMapperTreeModel) mapperModel.getRightTreeModel();
                        rightTreeModel.fireTreeChanged(this, targetTreePath);
                    }
                }
            }
        }
        //====================================================================//
        private class CorrelationMapperTreeModel extends DefaultTreeModel {
            public CorrelationMapperTreeModel() {
                super(null);
            }
        
            public void buildCorrelationMapperTree(CorrelationMapperTreeNode topTreeNode) {
                BpelEntity topBpelEntity = (BpelEntity) (topTreeNode).getUserObject();
                CorrelationMapperTreeNode fakeRootTreeNode = new CorrelationMapperTreeNode(
                    new BpelBuilderImpl((BpelModelImpl) topBpelEntity.getBpelModel()).createEmpty());
                fakeRootTreeNode.add(topTreeNode);
                setRoot(fakeRootTreeNode);
            }
            
            public BpelEntity getTopBpelEntity() {
                CorrelationMapperTreeNode topTreeNode = (CorrelationMapperTreeNode) ((CorrelationMapperTreeNode) getRoot()).getChildAt(0);
                return (BpelEntity) topTreeNode.getUserObject();
            }
            
            public void fireTreeChanged(Object source, TreePath treePath) {
                Object[] listeners = listenerList.getListenerList(); // guaranteed to return a non-null array
                TreeModelEvent treeModelEvent = null;
                // go through the listener list from the last to the first, 
                // notifying those ones which are interested in this event
                for (int i = listeners.length - 2; i >= 0; i -= 2) {
                    if (listeners[i] == TreeModelListener.class) {
                        if (treeModelEvent == null) { // lazily create the event:
                            TreePath parentTreePath = treePath.getParentPath();
                            if (parentTreePath != null) {
                                Object treeNode = treePath.getLastPathComponent();
                                // reload cached user object
                                ((CorrelationMapperTreeNode) treeNode).setUserObject(
                                    ((CorrelationMapperTreeNode) treeNode).getUserObject());

                                int childIndex = getIndexOfChild(
                                    parentTreePath.getLastPathComponent(), treeNode);

                                treeModelEvent = new TreeModelEvent(source, parentTreePath, 
                                    new int[] {childIndex}, new Object[] {treeNode});
                            } else {
                                treeModelEvent = new TreeModelEvent(source, treePath, 
                                    new int[] {}, new Object[] {});
                            }
                        }
                        ((TreeModelListener) listeners[i+1]).treeNodesChanged(treeModelEvent);
                    }          
                }
                // invoke the following method of WizardDefineCorrelationPanel to hide/show warning message
                isValid(); 
            }
        }
        //====================================================================//
        private class CorrelationMapperTreeNode extends DefaultMutableTreeNode {
            private String nodeNamePattern;
            
            public CorrelationMapperTreeNode(Object userObject) {
                this(userObject, null);
            }
            public CorrelationMapperTreeNode(Object userObject, String nodeNamePattern) {
                super(userObject);
                this.nodeNamePattern = nodeNamePattern;
            }

            @Override
            public String toString() {
                String userObjectName = null;
                Object userObj = getUserObject();
                Object[] patternValues = new Object[0]; 
                try {
                    if (userObj instanceof BpelEntity) {
                        if (userObj instanceof OnMessage) {
                            String pickEntityName = ((BpelEntity) userObj).getParent().getAttribute(BpelAttributes.NAME),
                                   operationName = ((BpelEntity) userObj).getAttribute(BpelAttributes.OPERATION);
                            userObjectName = ((BpelEntity) userObj).getElementType().getSimpleName();
                            patternValues = new Object[] {userObjectName, pickEntityName, operationName};
                        } else {
                            userObjectName = ((BpelEntity) userObj).getAttribute(BpelAttributes.NAME);
                            patternValues = new Object[] {userObjectName, ((BpelEntity) userObj).getElementType().getSimpleName()};
                        }
                    } else if (userObj instanceof Message) {
                        userObjectName = ((Message) userObj).getName();
                        patternValues = new Object[] {userObjectName, "Message"};
                    } else if (userObj instanceof Part) {
                        userObjectName = ((Part) userObj).getName();
                        patternValues = new Object[] {userObjectName, "Part"};
                    } else if (userObj instanceof GlobalElement) {
                        userObjectName = ((GlobalElement) userObj).getName();
                        patternValues = new Object[] {userObjectName, "GlobalElement"};
                    } else {
                        userObjectName = userObj.toString();
                    }
                } catch (Exception e) {
                    userObjectName = userObj.toString();
                }
                String nodeName = (nodeNamePattern == null ? userObjectName : 
                    MessageFormat.format(nodeNamePattern, patternValues));
                return nodeName;
            }

            public Icon getIcon() {
                Object userObj = getUserObject();
                Class userObjClass = Object.class;
                if (userObj instanceof BpelEntity) {
                    userObjClass = ((BpelEntity) userObj).getElementType();
                } else {
                    if (userObj instanceof Message) userObjClass = Message.class;
                    if (userObj instanceof Part) userObjClass = Part.class;
                    if (userObj instanceof GlobalElement) userObjClass = GlobalElement.class;
                }
                Icon icon = mapTreeNodeIcons.get(userObjClass);
                if (icon == null) {
                    icon = mapTreeNodeIcons.get(Object.class);
                }
                return icon;
            }
        }
        //====================================================================//
        private class CorrelationMapperContext extends DefaultMapperContext {
            @Override
            public Icon getLeftIcon(MapperModel model, Object value, Icon defaultIcon) {
                return ((CorrelationMapperTreeNode) value).getIcon();
            }
            
            @Override
            public Icon getRightIcon(MapperModel model, Object value, Icon defaultIcon) {
                return ((CorrelationMapperTreeNode) value).getIcon();
            }
        }
        //====================================================================//
        private class CorrelationMapperModel implements MapperModel {
            private TreeModel leftTreeModel, rightTreeModel;
            private Map<TreePath, Graph> mapTreePathGraphs = new HashMap<TreePath, Graph>();

            public CorrelationMapperModel(TreeModel leftTreeModel, TreeModel rightTreeModel) {
                this.leftTreeModel = leftTreeModel;
                this.rightTreeModel = rightTreeModel;
                TMP_FAKE_GRAPH = new Graph(this);
            }

            public boolean canConnect(TreePath treePath, SourcePin source, TargetPin target, 
                    TreePath oldTreePath, Link oldLink) {
                if (oldLink != null) return false;
                boolean result = false;
                CorrelationMapperTreeNode treeNode = null;
                if ((source != null) && (source instanceof TreeSourcePin)) {
                    TreePath sourceTreePath = ((TreeSourcePin) source).getTreePath();
                    treeNode = (CorrelationMapperTreeNode) sourceTreePath.getLastPathComponent();
                    result = treeNode.isLeaf();
                    result &= ! treeNode.getUserObject().equals(
                        ((CorrelationMapperTreeModel) leftTreeModel).getTopBpelEntity());
                    result &= ! isLeftTreePathLinked(sourceTreePath);
                }
                treeNode = (CorrelationMapperTreeNode) treePath.getLastPathComponent();
                result &= treeNode.isLeaf();
                result &= ! treeNode.getUserObject().equals(
                    ((CorrelationMapperTreeModel) rightTreeModel).getTopBpelEntity());

                if (target instanceof Graph) {
                    Graph targetGraph = (Graph) target;
                    if (targetGraph.hasIngoingLinks() || targetGraph.hasOutgoingLinks()) {
                        result = false; // the target tree node already has a connected link
                    }
                }
                return result;
            }

            public void connect(TreePath treePath, SourcePin source, TargetPin target, 
                TreePath oldTreePath, Link oldLink) {
                if (oldLink != null) return;
                Graph graph = getGraph(treePath);
                if ((graph == null) || (graph == TMP_FAKE_GRAPH)) {
                    graph = createNewGraph(treePath);
                }
                Link newLink = new Link(source, target);
                graph.addLink(newLink);
                ((CorrelationMapperTreeModel) rightTreeModel).fireTreeChanged(this, treePath);
            }

            public Map<TreePath, Graph> getMapTreePathGraphs() {
                return mapTreePathGraphs;
            }

            protected Graph createNewGraph(TreePath treePath) {
                Graph treePathGraph = new Graph(this);
                mapTreePathGraphs.put(treePath, treePathGraph);
                ((CorrelationMapperTreeModel) rightTreeModel).fireTreeChanged(this, treePath);
                return treePathGraph;
            }
            
            public TreePath getTreePathByGraph(Graph graph) {
                if (graph == null) return null;
                TreePath treePath = null;
                for (Map.Entry<TreePath, Graph> mapEntry : mapTreePathGraphs.entrySet()) {
                    if (mapEntry.getValue().equals(graph)) {
                        return mapEntry.getKey();
                    }
                }
                return treePath;
            }
            
            public boolean isLeftTreePathLinked(TreePath leftTreePath) {
                for (Graph graph : mapTreePathGraphs.values()) {
                    for (Link link : graph.getLinks()) {
                        TreePath sourceTreePath = ((TreeSourcePin) link.getSource()).getTreePath();
                        if (sourceTreePath.equals(leftTreePath)) {
                            return true;
                        }
                    }
                }
                return false;
            }
            
            public Graph getGraph(TreePath treePath) {
                Graph treePathGraph = mapTreePathGraphs.get(treePath);
                return (treePathGraph != null ? treePathGraph : TMP_FAKE_GRAPH);
            }

            public GraphSubset getGraphSubset(Transferable transferable) {
                return null;
            }

            public TreeModel getLeftTreeModel() {
                return leftTreeModel;
            }

            public TreeModel getRightTreeModel() {
                return rightTreeModel;
            }

            public TreeSourcePin getTreeSourcePin(TreePath treePath) {
                return new TreeSourcePin(treePath);
            }

            public boolean searchGraphsInside(TreePath treePath) {
                Object treeNode = treePath.getLastPathComponent();
                for (TreePath graphTreePath : mapTreePathGraphs.keySet()) {
                    while (true) {
                        graphTreePath = graphTreePath.getParentPath();
                        if (graphTreePath == null) {
                            break;
                        }
                        Object parentTreeNode = graphTreePath.getLastPathComponent();
                        if (parentTreeNode == treeNode) {
                            return true;
                        }
                    }
                }
                return false;
            }

            public void valueChanged(TreePath treePath, VertexItem vertexItem, Object newValue) {}

            public boolean canCopy(TreePath treePath, GraphSubset graphSubset) {
                return false;
            }

            public boolean canMove(TreePath treePath, GraphSubset graphSubset) {
                return false;
            }

            public void copy(TreePath treePath, GraphSubset graphGroup, int x, int y) {}

            public void move(TreePath treePath, GraphSubset graphGroup, int x, int y) {}
            
            public void addTreeModelListener(TreeModelListener l) {
                rightTreeModel.addTreeModelListener(l);
            }

            public Object getChild(Object parent, int index) {
                return rightTreeModel.getChild(parent, index);
            }

            public int getChildCount(Object parent) {
                return rightTreeModel.getChildCount(parent);
            }

            public int getIndexOfChild(Object parent, Object child) {
                return rightTreeModel.getIndexOfChild(parent, child);
            }

            public Object getRoot() {
                return rightTreeModel.getRoot();
            }

            public boolean isLeaf(Object node) {
                return rightTreeModel.isLeaf(node);
            }

            public void removeTreeModelListener(TreeModelListener l) {
                rightTreeModel.removeTreeModelListener(l);
            }

            public void valueForPathChanged(TreePath path, Object newValue) {
                rightTreeModel.valueForPathChanged(path, newValue);
            }
        }
    }
    //========================================================================//
    public class WizardCorrelationConfigurationPanel extends WizardAbstractPanel {
        public WizardCorrelationConfigurationPanel() {
            super();
        }
        
        @Override
        public boolean isValid() {
            boolean isOK = false;
            if (buttonFinish != null) buttonFinish.setEnabled(isOK);
            return isOK;
        }

        @Override
        public void validate() throws WizardValidationException {
        }
    }
}

interface WizardProperties {
    String
        PROPERTY_AUTO_WIZARD_STYLE = "WizardPanel_autoWizardStyle", // NOI18N
        PROPERTY_CONTENT_DISPLAYED = "WizardPanel_contentDisplayed", // NOI18N
        PROPERTY_CONTENT_NUMBERED = "WizardPanel_contentNumbered", // NOI18N
        PROPERTY_LEFT_DIMENSION = "WizardPanel_leftDimension", // NOI18N

        PROPERTY_CONTENT_SELECTED_INDEX = "WizardPanel_contentSelectedIndex", // NOI18N
        PROPERTY_CONTENT_DATA = "WizardPanel_contentData", // NOI18N
        PROPERTY_ERROR_MESSAGE = "WizardPanel_errorMessage", // NOI18N
        PROPERTY_CONTENT_BACK_COLOR = "WizardPanel_contentBackColor", // NOI18N
        PROPERTY_CONTENT_FOREGROUND_COLOR = "WizardPanel_contentForegroundColor", // NOI18N
        PROPERTY_IMAGE = "WizardPanel_image", // NOI18N
        PROPERTY_IMAGE_ALIGNMENT = "WizardPanel_imageAlignment", // NOI18N

        PROPERTY_HELP_DISPLAYED = "WizardPanel_helpDisplayed", // NOI18N
        PROPERTY_HELP_URL = "WizardPanel_helpURL"; // NOI18N
}