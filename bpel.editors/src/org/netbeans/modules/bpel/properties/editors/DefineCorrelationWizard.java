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
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.text.MessageFormat;
import java.util.*;
import java.util.ArrayList;
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
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.netbeans.modules.bpel.model.api.Activity;
import org.netbeans.modules.bpel.model.api.BPELElementsBuilder;
import org.netbeans.modules.bpel.model.api.BaseScope;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Correlation;
import org.netbeans.modules.bpel.model.api.CorrelationContainer;
import org.netbeans.modules.bpel.model.api.CorrelationSet;
import org.netbeans.modules.bpel.model.api.CorrelationSetContainer;
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
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.bpel.model.api.support.Initiate;
import org.netbeans.modules.bpel.model.impl.BpelBuilderImpl;
import org.netbeans.modules.bpel.model.impl.BpelModelImpl;
import org.netbeans.modules.bpel.model.impl.InvokeReceiveReplyCommonImpl;
import org.netbeans.modules.bpel.model.impl.OnMessageCommonImpl;
import org.netbeans.modules.bpel.model.xam.BpelAttributes;
import org.netbeans.modules.bpel.nodes.BpelNode;
import org.netbeans.modules.bpel.properties.Util;
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
import org.netbeans.modules.xml.schema.model.Attribute;
import org.netbeans.modules.xml.schema.model.ComplexType;
import org.netbeans.modules.xml.schema.model.Element;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SimpleType;
import org.netbeans.modules.xml.schema.model.TypeContainer;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.OperationParameter;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.BPELQName;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.CorrelationProperty;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PropertyAlias;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PropertyAlias;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.xpath.ext.schema.FindAllChildrenSchemaVisitor;
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
        NbBundle.getMessage(DefineCorrelationWizard.class, "LBL_Wizard_Step_Define_Correlation")
        //NbBundle.getMessage(DefineCorrelationWizard.class, "LBL_Wizard_Step_Correlation_Configuration")
    };

    private static Graph TMP_FAKE_GRAPH;

    private static final String 
        IMAGE_FOLDER_NAME = "org/netbeans/modules/bpel/editors/api/nodes/images/", // NOI18N
        IMAGE_FILE_EXT = ".png", // NOI18N
        
        SIMPLE_TYPE_NAME_PATTERN = NbBundle.getMessage(DefineCorrelationWizard.class, 
                                   "LBL_Mapper_Tree_SimpleType_Name_Pattern"),

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
        //panelList.add(new WizardCorrelationConfigurationPanel());
        for (int i = 0; i < panelList.size(); ++i) {
            ((WizardAbstractPanel) panelList.get(i)).setPanelNameIndex(
                WIZARD_STEP_NAMES[i], i);
        }
        return panelList;
    }
        
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
        mapIcons.put(Element.class, new ImageIcon(Utilities.loadImage(iconFileName)));
    
        iconFileName = IMAGE_FOLDER_NAME + "GLOBAL_COMPLEX_TYPE" + IMAGE_FILE_EXT; // NOI18N
        mapIcons.put(ComplexType.class, new ImageIcon(Utilities.loadImage(iconFileName)));
        
        iconFileName = IMAGE_FOLDER_NAME + "GLOBAL_SIMPLE_TYPE" + IMAGE_FILE_EXT; // NOI18N
        mapIcons.put(SimpleType.class, new ImageIcon(Utilities.loadImage(iconFileName)));
         
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
        protected Set<Class> forbiddenActivityTypeSet = new HashSet<Class>();
        
        public List<BpelEntity> getPermittedActivityList(BpelEntity mainBpelEntity) {
            List<BpelEntity> bpelEntityList = new ArrayList<BpelEntity>();
            BpelEntity parentEntity = getTopParentEntity(mainBpelEntity);
            if (parentEntity == null) return bpelEntityList;
        
            boolean isScopeEntityIgnored = ! (parentEntity instanceof Scope);
            List<BpelEntity> activities = chooseActivities(parentEntity, 
                new ArrayList<BpelEntity>(), isScopeEntityIgnored);
            for (BpelEntity bpelEntity : activities) {
                if (mainBpelEntity.equals(bpelEntity)) {
                    bpelEntityList.add(bpelEntity);
                } else {
                    // filter all chosen activities by allowable classes (only those 
                    // activities will be selected, which are instances of permitted classes)
                    if (! isInstanceOfClass(forbiddenActivityTypeSet, bpelEntity)) {
                        // check that Class of activity object implements/extends
                        // the appropriate class "permittedActivityType"
                        if (isInstanceOfClass(permittedActivityTypeSet, bpelEntity)) {
                            bpelEntityList.add(bpelEntity);
                        }
                    }
                }
            }
            return bpelEntityList;
        }
    
        private boolean isInstanceOfClass(Set<Class> setCheckedClasses, BpelEntity bpelEntity) {
            for (Class checkedClass : setCheckedClasses) {
                try {
                    bpelEntity.getClass().asSubclass(checkedClass);
                    return true;
                } catch (ClassCastException e) {}
            }
            return false;
        }
        
        private List<BpelEntity> chooseActivities(BpelEntity bpelEntity, 
            List<BpelEntity> bpelEntityList, boolean isScopeEntityIgnored) {
            if (bpelEntityList == null) return (new ArrayList<BpelEntity>());
            
            if (bpelEntity instanceof Sequence) {
                for (Activity activity : bpelEntity.getChildren(Activity.class)) {
                    bpelEntityList.addAll(chooseActivities(activity, 
                        new ArrayList<BpelEntity>(), true));
                }
            } else if ((bpelEntity instanceof Requester) || (bpelEntity instanceof Responder)) {
                bpelEntityList.add(bpelEntity);
            } else if (bpelEntity instanceof Pick) {
                for (OnMessage onMessage : ((Pick) bpelEntity).getOnMessages()) {
                    bpelEntityList.add(onMessage);
                    for (Sequence sequence : onMessage.getChildren(Sequence.class)) {
                        bpelEntityList.addAll(chooseActivities(sequence, 
                            new ArrayList<BpelEntity>(), true));
                    }
                }
                for (OnAlarmPick onAlarmPick : ((Pick) bpelEntity).getOnAlarms()) {
                    for (Sequence sequence : onAlarmPick.getChildren(Sequence.class)) {
                        bpelEntityList.addAll(chooseActivities(sequence, 
                            new ArrayList<BpelEntity>(), true));
                    }
                }
            } else {
                // collect activity from the global scope (whole Process scope),
                // ignoring all sub-scope, or from the current selected scope only
                if (! ((bpelEntity instanceof Scope) && (isScopeEntityIgnored))) {
                    for (Sequence sequence : bpelEntity.getChildren(Sequence.class)) {
                        bpelEntityList.addAll(chooseActivities(sequence, 
                            new ArrayList<BpelEntity>(), true));
                    }
                }
            }
            return bpelEntityList;
        }
        /*
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
        */
    }
    
    private class RequesterActivityChooser extends AbstractActivityChooser {
        public RequesterActivityChooser() {
            permittedActivityTypeSet = new HashSet<Class>(Arrays.asList(new Class[] {
                Responder.class}));
            forbiddenActivityTypeSet = new HashSet<Class>(Arrays.asList(new Class[] {
                Requester.class}));
        }
        @Override
        public List<BpelEntity> getPermittedActivityList(BpelEntity mainBpelEntity) {
            List<BpelEntity> activityList = super.getPermittedActivityList(mainBpelEntity);
            if (activityList.isEmpty()) return activityList;
            
            // remove all Responder-activities above mainBpelEntity and mainBpelEntity itself
            //activityList = removeResponderActivityAbove(activityList, mainBpelEntity);
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
            //activityList = removeRequesterActivityBelow(activityList, mainBpelEntity);
            activityList.remove(mainBpelEntity);
            return activityList;
        }
    }
    
    private class RequesterResponderActivityChooser extends AbstractActivityChooser  {
        // it's assumed that Invoke (Requester-Responder) is used as Requester only
        public RequesterResponderActivityChooser() {
            permittedActivityTypeSet = new HashSet<Class>(Arrays.asList(new Class[] {
                Responder.class}));
            forbiddenActivityTypeSet = new HashSet<Class>(Arrays.asList(new Class[] {
                Requester.class}));
        }
        @Override
        public List<BpelEntity> getPermittedActivityList(BpelEntity mainBpelEntity) {
            List<BpelEntity> activityList = super.getPermittedActivityList(mainBpelEntity);
            if (activityList.isEmpty()) return activityList;
            
            // remove all Responder-activities above mainBpelEntity
            //activityList = removeResponderActivityAbove(activityList, mainBpelEntity);
            
            // remove all Requester-activities below mainBpelEntity
            //activityList = removeRequesterActivityBelow(activityList, mainBpelEntity);
            
            // remove mainBpelEntity from the list
            activityList.remove(mainBpelEntity);
            
            return  activityList;
        }
    }
    //========================================================================//
    public abstract class WizardAbstractPanel implements WizardDescriptor.ValidatingPanel {
        protected JPanel wizardPanel = createWizardPanel();
        protected ChangeSupport changeSupport = new ChangeSupport(this);
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
        private final String CORRELATION_PROPERTY_NAME_PREFIX = "wizard_";
        private final String CORRELATION_SET_NAME_PREFIX = "wizard_set_";
            
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
            ((CorrelationMapperModel) mapperModel).expandTree(((CorrelationMapperModel) mapperModel).getLeftTreeModel());
            ((CorrelationMapperModel) mapperModel).expandTree(((CorrelationMapperModel) mapperModel).getRightTreeModel());
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
                        messageNode.add(handlePart(part));
                    }
                    topTreeNode.add(messageNode);
                }
            }
        }

        private CorrelationMapperTreeNode handlePart(Part part) {
            CorrelationMapperTreeNode partNode = new CorrelationMapperTreeNode(part, null);
            NamedComponentReference<GlobalElement> partElementRef = part.getElement();
            if (partElementRef != null) {
                GlobalElement partElement = partElementRef.get();
                addSchemaComponentNode(partNode, partElement, new FindAllChildrenSchemaVisitor(true, true));                    
            } else {
                NamedComponentReference<GlobalType> partTypeRef = part.getType();
                if (partTypeRef != null) {
                    GlobalType partType = partTypeRef.get();
                    addSchemaComponentNode(partNode, partType, new FindAllChildrenSchemaVisitor(true, true)); 
                }
            }
            return partNode;
        }        
        
        private void addSchemaComponentNode(CorrelationMapperTreeNode parentNode, 
            SchemaComponent schemaComponent, FindAllChildrenSchemaVisitor schemaTypeFinder) {
            String nodeNamePattern = schemaComponent instanceof SimpleType ?
                SIMPLE_TYPE_NAME_PATTERN : null;
            CorrelationMapperTreeNode schemaComponentNode = new CorrelationMapperTreeNode(
                schemaComponent, nodeNamePattern);

            List<SchemaComponent> childSchemaTypeComponentList = null;
            if (! (schemaComponent instanceof SimpleType)) {
                schemaTypeFinder.lookForSubcomponents(schemaComponent);
                childSchemaTypeComponentList = schemaTypeFinder.getFound();
                for (SchemaComponent childSchemaTypeComponent : childSchemaTypeComponentList) {
                    addSchemaComponentNode(schemaComponentNode, childSchemaTypeComponent, new FindAllChildrenSchemaVisitor(true, true));
                }
            }
            if ((schemaComponent instanceof Element) && 
                ((childSchemaTypeComponentList == null) || (childSchemaTypeComponentList.isEmpty())) &&
                isElementComplexType(schemaComponent)) {
                return;
            }
            if ((schemaComponent instanceof Attribute) && 
                ((childSchemaTypeComponentList == null) || (childSchemaTypeComponentList.isEmpty())) &&
                isAttributeUnknownType(schemaComponent)) {
                return;
            }
            parentNode.add(schemaComponentNode);
        }
    
        private boolean isElementComplexType(SchemaComponent schemaComponent) {
            if (! (schemaComponent instanceof Element)) {
                return false;
            }
            NamedComponentReference<? extends GlobalType> typeRef = 
                getSchemaComponentTypeRef(schemaComponent);
            return ((typeRef != null) && (typeRef.get() instanceof ComplexType));
        }
        
        private boolean isAttributeUnknownType(SchemaComponent schemaComponent) {
            if (! (schemaComponent instanceof Attribute)) {
                return false;
            }
            return (getSchemaComponentTypeName(schemaComponent) == null);
        }

        private NamedComponentReference<? extends GlobalType> getSchemaComponentTypeRef(SchemaComponent schemaComponent) {
            NamedComponentReference<? extends GlobalType> typeRef = null;
            try {
                typeRef = ((TypeContainer) schemaComponent).getType();
            } catch (Exception exception) {}
            return typeRef;
        }

        private String getSchemaComponentTypeName(SchemaComponent schemaComponent) {
            String typeName = null;
            if ((schemaComponent instanceof SimpleType) || (schemaComponent instanceof ComplexType)) {
                typeName = schemaComponent.getAttribute(BpelAttributes.NAME);
            } else {
                NamedComponentReference<? extends GlobalType> typeRef = getSchemaComponentTypeRef(schemaComponent);
                if (typeRef != null) {
                    typeName = typeRef.get().getName();
                } else {
                    typeName = ((SchemaComponent) schemaComponent).getAttribute(BpelAttributes.TYPE);
                }
            }
            return typeName;
        }

        public String getSchemaComponentName(SchemaComponent schemaComponent) {
            String name = null;
            if (schemaComponent instanceof SimpleType) {
                name = getSchemaComponentTypeName(schemaComponent);
            } else  {
                name = schemaComponent.toString();
            }
            return name;
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
            // switch to the next panel or close the wizard if the correlation mapper contains at least 1 link
            Map<TreePath, Graph> mapTreePathGraphs = ((CorrelationMapperModel) correlationMapper.getModel()).getMapTreePathGraphs();
            for (Graph graph : mapTreePathGraphs.values()) {
                isOK = graph.hasIngoingLinks();
                if (isOK) break;
            }
            wizardDescriptor.putProperty(PROPERTY_ERROR_MESSAGE, isOK ? null :
                NbBundle.getMessage(WizardDefineCorrelationPanel.class, "LBL_ErrMsg_No_Links_For_Correlation"));                              
            //if (buttonNext != null) buttonNext.setEnabled(isOK);
            if (buttonFinish != null) buttonFinish.setEnabled(isOK);
            return isOK;
        }

        @Override
        public void validate() throws WizardValidationException {
            makeCorrelations();
        }
        
        private void makeCorrelations() throws WizardValidationException {
            CorrelationMapperModel mapperModel = (CorrelationMapperModel) correlationMapper.getModel();
            CorrelationMapperTreeModel 
                leftTreeModel = (CorrelationMapperTreeModel) mapperModel.getLeftTreeModel(), 
                rightTreeModel = (CorrelationMapperTreeModel) mapperModel.getRightTreeModel();
            List<CorrelationLinker> correlationLinkers = getCorrelationLinkers(mapperModel, leftTreeModel, rightTreeModel);
            // group linkers by equivalence of [activity-message-part]
            // to combine appropriate properties in one correlation set
            while (! correlationLinkers.isEmpty()) {
                List<CorrelationLinker> equalLinkerSublist = getSublistEqualCorrelationLinkers(correlationLinkers);
                if (equalLinkerSublist.isEmpty()) break;
                correlationLinkers.removeAll(equalLinkerSublist);
                
                createCorrelationPropertiesAndPropertyAliases(equalLinkerSublist);
                CorrelationSet correlationSet = createCorrelationSet(equalLinkerSublist);
                equalLinkerSublist.get(0).createActivityCorrelation(correlationSet);
            }
        }

        private CorrelationSet createCorrelationSet(List<CorrelationLinker> linkerList) {
            BaseScope scopeEntity = (BaseScope) getTopParentEntity(mainBpelEntity);
            if (scopeEntity == null) {
                return null;
            }
            String correlationSetName = getUniqueCorrelationSetName(scopeEntity);

            BpelModel bpelModel = mainBpelEntity.getBpelModel();
            BPELElementsBuilder elementBuilder = bpelModel.getBuilder();
            CorrelationSet correlationSet = elementBuilder.createCorrelationSet();
            
            try {
                correlationSet.setName(correlationSetName);
            } catch (Exception e) {
                e.printStackTrace();
            }

            List<WSDLReference<CorrelationProperty>> propertyRefList = new ArrayList<WSDLReference<CorrelationProperty>>();
            for (CorrelationLinker linker : linkerList) {
                WSDLReference<CorrelationProperty> correlationPropertyRef = 
                    correlationSet.createWSDLReference(linker.getCorrelationProperty(), CorrelationProperty.class);
                if (correlationPropertyRef != null) {
                    propertyRefList.add(correlationPropertyRef);
                }
            }
            if (! propertyRefList.isEmpty()) {   
                correlationSet.setProperties(propertyRefList);
            }
            CorrelationSetContainer container = scopeEntity.getCorrelationSetContainer();
            try {
                bpelModel.startTransaction();
                if (container == null) {
                    container = elementBuilder.createCorrelationSetContainer();
                    scopeEntity.setCorrelationSetContainer(container);
                    container = scopeEntity.getCorrelationSetContainer();
                }
                container.insertCorrelationSet(correlationSet, 0);
            } catch(Exception e) {
            } finally {
                bpelModel.endTransaction();
            }
            return correlationSet;
        }

        private String getUniqueCorrelationSetName(BaseScope scopeEntity) {
            String baseCorrelationSetName = CORRELATION_SET_NAME_PREFIX + 
                mainBpelEntity.getAttribute(BpelAttributes.NAME);
            CorrelationSetContainer container = scopeEntity.getCorrelationSetContainer();
            if (container == null) return baseCorrelationSetName;
            CorrelationSet[] correlationSets = container.getCorrelationSets();
            if ((correlationSets != null) && (correlationSets.length > 0)) {
                int index = 0;
                String checkedName = baseCorrelationSetName;
                while (++index < 10000) {
                    if (containsCorrelationSetName(correlationSets, checkedName)) {
                        checkedName = baseCorrelationSetName + "_" + index;
                    } else {
                        return checkedName;
                    }
                }
                if (index >= 10000) return null;
            }
            return baseCorrelationSetName;
        }
        
        private boolean containsCorrelationSetName(CorrelationSet[] correlationSets,
            String checkedName) {
            for (CorrelationSet correlationSet : correlationSets) {
                String correlationSetName = correlationSet.getName();
                if (ignoreNamespace(correlationSetName).equals(checkedName)) {
                    return true;
                }
            }
            return false;
        }
        
        private void createCorrelationPropertiesAndPropertyAliases(List<CorrelationLinker> linkerList) {
            for (CorrelationLinker linker : linkerList) {
                linker.createCorrelationProperty();
                linker.createPropertyAlias();
            }
        }
        
        private List<CorrelationLinker> getSublistEqualCorrelationLinkers(List<CorrelationLinker> correlationLinkers) {
            List<CorrelationLinker> linkerSublist = new ArrayList<CorrelationLinker>();
            CorrelationLinker controlLinker = null;
            for (CorrelationLinker linker : correlationLinkers) {
                if (controlLinker == null) {
                    controlLinker = linker;
                    linkerSublist.add(linker);
                } else {
                    if (linker.equals(controlLinker)) {
                        linkerSublist.add(linker);
                    }
                }
            }
            return linkerSublist;
        }
        
        private List<CorrelationLinker> getCorrelationLinkers(CorrelationMapperModel mapperModel,
            CorrelationMapperTreeModel leftTreeModel, CorrelationMapperTreeModel rightTreeModel) 
            throws WizardValidationException {
            List<CorrelationLinker> correlationLinkers = new ArrayList<CorrelationLinker>();
            BpelEntity leftActivity = leftTreeModel.getTopBpelEntity(),
                       rightActivity = rightTreeModel.getTopBpelEntity();
            
            Map<TreePath, Graph> mapTreePathGraph = mapperModel.getMapTreePathGraphs();
            for (Map.Entry<TreePath, Graph> mapEntry : mapTreePathGraph.entrySet()) {
                CorrelationDataHolder rightDataHolder = new CorrelationDataHolder();
                rightDataHolder.setActivity(rightActivity);
                rightDataHolder.extractDataFromTreePath(mapEntry.getKey());
                
                Graph graph = mapEntry.getValue();
                List<Link> linkList = graph.getLinks();
                for (Link link : linkList) {
                    CorrelationDataHolder leftDataHolder = new CorrelationDataHolder();
                    leftDataHolder.setActivity(leftActivity);
                    leftDataHolder.extractDataFromLink(link);
                    
                    CorrelationLinker correlationLinker = new CorrelationLinker(
                        leftDataHolder, rightDataHolder);
                    correlationLinker.checkTypesEquivalence();
                    correlationLinkers.add(correlationLinker);
                }
            }
            return correlationLinkers;
        }
        
        public String ignoreNamespace(String dataWithNamespace) {
            int index = dataWithNamespace.indexOf(":");
            if ((index > -1) && (index < dataWithNamespace.length() - 1)) {
                return dataWithNamespace.substring(index + 1);
            }
            return dataWithNamespace;
        }
        //====================================================================//
        private class CorrelationLinker {
            private CorrelationDataHolder source, target;
            private CorrelationProperty correlationProperty;
            
            public CorrelationLinker() {}
            public CorrelationLinker(CorrelationDataHolder source, CorrelationDataHolder target) {
                this.source = source;
                this.target = target;
            }
            
            public CorrelationDataHolder getSource() {return source;}
            public CorrelationDataHolder getTarget() {return target;}
            public void setSource(CorrelationDataHolder source) {this.source = source;}
            public void setTarget(CorrelationDataHolder target) {this.target = target;}
            public CorrelationProperty getCorrelationProperty() {return correlationProperty;}

            public void createActivityCorrelation(CorrelationSet correlationSet) {
                source.createActivityCorrelation(correlationSet);
                target.createActivityCorrelation(correlationSet);
            }
            
            public void createPropertyAlias() {
                source.createPropertyAlias(correlationProperty);
                target.createPropertyAlias(correlationProperty);
            }

            public void createCorrelationProperty() {
                String propertyName = getBasePropertyName();
                WSDLModel wsdlModel = source.getWSDLModel();
                
                correlationProperty = (CorrelationProperty) wsdlModel.getFactory().create(
                    wsdlModel.getDefinitions(), BPELQName.PROPERTY.getQName());
                correlationProperty.setName(propertyName);
                
                NamedComponentReference<GlobalType> typeRef = source.getGlobalTypeReference();
                if (typeRef != null) {
                    correlationProperty.setType(typeRef);
                }
                
                if (Util.isUniquePropertyName(wsdlModel, propertyName)) {
                    try {
                        wsdlModel.startTransaction();
                        wsdlModel.addChildComponent(wsdlModel.getRootComponent(), 
                            correlationProperty, 0);
                    } finally {
                        wsdlModel.endTransaction();
                    }
                }
            }
            
            private String getBasePropertyName() {
                return CORRELATION_PROPERTY_NAME_PREFIX + 
                       WizardDefineCorrelationPanel.this.getSchemaComponentName(source.getSchemaComponent()) + 
                       "_" +
                       WizardDefineCorrelationPanel.this.getSchemaComponentName(target.getSchemaComponent()); 
            }
            
            @Override
            public boolean equals(Object obj) {
                if ((obj == null) || (getClass() != obj.getClass())) {
                    return false;
                }
                final CorrelationLinker otherLinker = (CorrelationLinker) obj;
                return (source.equals(otherLinker.source) && target.equals(otherLinker.target));
            }

            @Override
            public int hashCode() {
                int hash = 3;
                hash = 71 * hash + (this.source != null ? this.source.hashCode() : 0);
                hash = 71 * hash + (this.target != null ? this.target.hashCode() : 0);
                return hash;
            }
            
            public void checkTypesEquivalence() throws WizardValidationException {
                SchemaComponent sourceSchemaComponent = source.getSchemaComponent(),
                                targetSchemaComponent = target.getSchemaComponent();
                String sourceType = source.getTypeNameIgnoreNamespace(),
                       targetType = target.getTypeNameIgnoreNamespace();

                if (! sourceType.equals(targetType)) {
                    String sourceComponentName = WizardDefineCorrelationPanel.this.getSchemaComponentName(sourceSchemaComponent),
                           targetComponentName = WizardDefineCorrelationPanel.this.getSchemaComponentName(targetSchemaComponent);
                    String errMsg = MessageFormat.format(NbBundle.getMessage(WizardDefineCorrelationPanel.class, 
                        "LBL_ErrMsg_Different_Schema_Component_Types"),
                        new Object[] {sourceComponentName, targetComponentName});
                    //wizardDescriptor.putProperty(PROPERTY_ERROR_MESSAGE, errMsg);                              
                    throw new WizardValidationException(wizardPanel, errMsg, errMsg);
                }
            }
        }
        //--------------------------------------------------------------------//
        private class CorrelationDataHolder {
            private BpelEntity activity;
            private Message message;
            private Part part;
            private SchemaComponent schemaComponent;

            public CorrelationDataHolder() {}

            public WSDLModel getWSDLModel() {
                PortType portType = ((PortTypeReference) activity).getPortType().get();
                return portType.getModel();
            }
        
            public void createActivityCorrelation(CorrelationSet correlationSet) {
                BpelModel bpelModel = activity.getBpelModel();
                BPELElementsBuilder elementBuilder = bpelModel.getBuilder();
                Correlation correlation = elementBuilder.createCorrelation();
                
                BpelReference<CorrelationSet> correlationSetRef = correlation.createReference(
                    correlationSet, CorrelationSet.class);
                try {
                    correlation.setSet(correlationSetRef);
                    correlation.setInitiate(Initiate.NO);
                    if (activity instanceof Invoke) {
                        //correlation.setAttribute(*****??????, 
                        //    PatternedCorrelation.PATTERN, Pattern.REQUEST_RESPONSE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                CorrelationContainer container = null;
                if (activity instanceof InvokeReceiveReplyCommonImpl) {
                    container = ((InvokeReceiveReplyCommonImpl) activity).getCorrelationContainer();
                } else if (activity instanceof OnMessageCommonImpl) {
                    container = ((OnMessageCommonImpl) activity).getCorrelationContainer();
                }
                try {
                    bpelModel.startTransaction();
                    if (container == null) {
                        container = elementBuilder.createCorrelationContainer();
                        if (activity instanceof InvokeReceiveReplyCommonImpl) {
                            ((InvokeReceiveReplyCommonImpl) activity).setCorrelationContainer(container);
                            container = ((InvokeReceiveReplyCommonImpl) activity).getCorrelationContainer();
                        } else if (activity instanceof OnMessageCommonImpl) {
                            ((OnMessageCommonImpl) activity).setCorrelationContainer(container);
                            container = ((OnMessageCommonImpl) activity).getCorrelationContainer();
                        }
                    }
                    container.insertCorrelation(correlation, 0);
                } catch(Exception e) {
                } finally {
                    bpelModel.endTransaction();
                }
            }
            
            public void createPropertyAlias(CorrelationProperty correlationProperty) {
                WSDLModel wsdlModel = getWSDLModel();
                
                PropertyAlias propertyAlias = (PropertyAlias) wsdlModel.getFactory().create(
                    wsdlModel.getDefinitions(), BPELQName.PROPERTY_ALIAS.getQName());

                NamedComponentReference<CorrelationProperty> correlationPropertyRef =
                    propertyAlias.createReferenceTo(correlationProperty, CorrelationProperty.class);
                propertyAlias.setPropertyName(correlationPropertyRef);

                NamedComponentReference<Message> messageTypeRef =
                    propertyAlias.createReferenceTo(message, Message.class);
                propertyAlias.setMessageType(messageTypeRef);

                propertyAlias.setPart(part.getName());
                
                try {
                    wsdlModel.startTransaction();
                    wsdlModel.addChildComponent(wsdlModel.getRootComponent(), 
                        propertyAlias, 0);
                } finally {
                    wsdlModel.endTransaction();
                }
            }
            
            public NamedComponentReference<GlobalType> getGlobalTypeReference() {
                Collection<GlobalSimpleType> globalSimpleTypes = getSchemaComponent().getModel().getSchema().getSimpleTypes();
                NamedComponentReference<GlobalType> typeRef = null;
                for (GlobalSimpleType globalSimpleType : globalSimpleTypes) {
                    String typeName = getTypeNameIgnoreNamespace();
                    if (globalSimpleType.toString().equals(typeName)) {
                        typeRef = schemaComponent.createReferenceTo(globalSimpleType, GlobalType.class);
                        return typeRef;
                    }
                }
                return typeRef;
            }

            public String getTypeNameIgnoreNamespace() {
                String typeName = WizardDefineCorrelationPanel.this.getSchemaComponentTypeName(schemaComponent);
                return ignoreNamespace(typeName);
            }
            
            @Override
            public boolean equals(Object obj) {
                if ((obj == null) || (getClass() != obj.getClass())){
                    return false;
                }
                final CorrelationDataHolder otherHolder = (CorrelationDataHolder) obj;
                return ((activity.equals(otherHolder.activity)) &&
                        (message.equals(otherHolder.message)) &&
                        (part.equals(otherHolder.part)));
            }

            @Override
            public int hashCode() {
                int hash = 7;
                hash = 97 * hash + (this.activity != null ? this.activity.hashCode() : 0);
                hash = 97 * hash + (this.message != null ? this.message.hashCode() : 0);
                hash = 97 * hash + (this.part != null ? this.part.hashCode() : 0);
                return hash;
            }

            public void extractDataFromTreePath(TreePath treePath) {
                CorrelationMapperTreeNode treeNode = (CorrelationMapperTreeNode) treePath.getLastPathComponent();
                schemaComponent = (SchemaComponent) treeNode.getUserObject();
                getPartAndMessage(treeNode);
            }
            
            public void extractDataFromLink(Link link) {
                TreePath treePath = ((TreeSourcePin) link.getSource()).getTreePath();
                extractDataFromTreePath(treePath);
            }
            
            private void getPartAndMessage(CorrelationMapperTreeNode treeNode) {
                if (treeNode == null) return;
                treeNode = (CorrelationMapperTreeNode) treeNode.getParent();
                while (treeNode != null) {
                    Object obj = treeNode.getUserObject();
                    if (obj instanceof Part) {
                        part = ((Part) obj);
                        try {
                            message = (Message) ((CorrelationMapperTreeNode) treeNode.getParent()).getUserObject();
                        } catch (Exception exception) {}
                        return;
                    }
                    treeNode = (CorrelationMapperTreeNode) treeNode.getParent();
                }
            }
            
            public BpelEntity getActivity() {return activity;}
            public void setActivity(BpelEntity activity) {this.activity = activity;}
            public Message getMessage() {return message;}
            public void setMessage(Message message) {this.message = message;}
            public Part getPart() {return part;}
            public void setPart(Part part) {this.part = part;}
            public SchemaComponent getSchemaComponent() {return schemaComponent;}
            public void setSchemaComponent(SchemaComponent schemaComponent) {this.schemaComponent = schemaComponent;}
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
                    } else if (userObj instanceof Part) {
                        userObjectName = ((Part) userObj).getName();
                    } else if ((userObj instanceof Element) || (userObj instanceof Attribute)) {
                        userObjectName = getSchemaComponentName((SchemaComponent) userObj);
                        if (getChildCount() == 0) { // simple type
                            String typeName = getSchemaComponentTypeName((SchemaComponent) userObj);
                            if (typeName != null) {
                                userObjectName += " " + MessageFormat.format(SIMPLE_TYPE_NAME_PATTERN, 
                                    new Object[] {typeName});
                            }
                        }
                    } else if ((userObj instanceof SimpleType) || (userObj instanceof ComplexType)) {
                        userObjectName = getSchemaComponentName((SchemaComponent) userObj);
                        patternValues = new Object[] {userObjectName};
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
                    if (userObj instanceof Element) userObjClass = Element.class;
                    if (userObj instanceof Attribute) userObjClass = SimpleType.class;
                    if (userObj instanceof SimpleType) userObjClass = SimpleType.class;
                    if (userObj instanceof ComplexType) userObjClass = ComplexType.class;
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
            
            public void expandTree(TreeModel mapperTreeModel) {
                if (mapperTreeModel == null) return;
                TreeNode rootNode = (TreeNode) mapperTreeModel.getRoot();
                expandTreeNode(rootNode, (mapperTreeModel.equals(rightTreeModel)));
            }
            
            private void expandTreeNode(TreeNode treeNode, boolean isRightTreeExpanded) {
                if (treeNode == null) return;
                TreePath treePath = new TreePath(isRightTreeExpanded ?
                    ((CorrelationMapperTreeModel) rightTreeModel).getPathToRoot(treeNode) :
                    ((CorrelationMapperTreeModel) leftTreeModel).getPathToRoot(treeNode));
                if (isRightTreeExpanded) {
                    correlationMapper.expandGraphs(new ArrayList(Arrays.asList(
                        new TreePath[] {treePath})));
                } else {
                    JTree leftTree = (JTree) correlationMapper.getLeftTree();
                    leftTree.expandPath(treePath);
                }
                int childCount = treeNode.getChildCount();
                for (int i = 0; i < childCount; ++i) {
                    TreeNode childNode = treeNode.getChildAt(i);
                    expandTreeNode(childNode, isRightTreeExpanded);
                }
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

            public boolean canEditInplace(VertexItem vItem) {
                return true;
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
        public void validate() throws WizardValidationException {}
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