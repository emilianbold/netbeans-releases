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
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.*;
import java.util.ArrayList;
import java.util.concurrent.Callable;
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
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import org.netbeans.modules.bpel.model.api.Activity;
import org.netbeans.modules.bpel.model.api.BPELElementsBuilder;
import org.netbeans.modules.bpel.model.api.BaseCorrelation;
import org.netbeans.modules.bpel.model.api.BaseScope;
import org.netbeans.modules.bpel.model.api.BpelContainer;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Correlation;
import org.netbeans.modules.bpel.model.api.CorrelationContainer;
import org.netbeans.modules.bpel.model.api.CorrelationSet;
import org.netbeans.modules.bpel.model.api.CorrelationSetContainer;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.model.api.Invoke;
import org.netbeans.modules.bpel.model.api.OnAlarmPick;
import org.netbeans.modules.bpel.model.api.OnEvent;
import org.netbeans.modules.bpel.model.api.OnMessage;
import org.netbeans.modules.bpel.model.api.PatternedCorrelation;
import org.netbeans.modules.bpel.model.api.PatternedCorrelationContainer;
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
import org.netbeans.modules.bpel.model.api.support.Pattern;
import org.netbeans.modules.bpel.model.impl.BpelBuilderImpl;
import org.netbeans.modules.bpel.model.impl.BpelModelImpl;
import org.netbeans.modules.bpel.model.impl.InvokeReceiveReplyCommonImpl;
import org.netbeans.modules.bpel.model.impl.OnMessageCommonImpl;
import org.netbeans.modules.bpel.model.xam.BpelAttributes;
import org.netbeans.modules.bpel.nodes.BpelNode;
import org.netbeans.modules.bpel.properties.ImportRegistrationHelper;
import org.netbeans.modules.bpel.properties.ResolverUtility;
import org.netbeans.modules.bpel.properties.Util;
import org.netbeans.modules.soa.mappercore.Canvas;
import org.netbeans.modules.soa.mappercore.DefaultMapperContext;
import org.netbeans.modules.soa.mappercore.LeftTree;
import org.netbeans.modules.soa.mappercore.Mapper;
import org.netbeans.modules.soa.mappercore.RightTree;
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
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.schema.model.SimpleType;
import org.netbeans.modules.xml.schema.model.TypeContainer;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.OperationParameter;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.RequestResponseOperation;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.BPELQName;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.CorrelationProperty;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PropertyAlias;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Query;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.xpath.ext.XPathLocationPath;
import org.netbeans.modules.xml.xpath.ext.LocationStep;
import org.netbeans.modules.xml.xpath.ext.StepNodeNameTest;
import org.netbeans.modules.xml.xpath.ext.XPathModel;
import org.netbeans.modules.xml.xpath.ext.XPathModelFactory;
import org.netbeans.modules.xml.xpath.ext.XPathModelHelper;
import org.netbeans.modules.xml.xpath.ext.XPathUtils;
import org.netbeans.modules.xml.xpath.ext.schema.FindAllChildrenSchemaVisitor;
import org.netbeans.modules.xml.xpath.ext.spi.ExternalModelResolver;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.WizardValidationException;
import org.openide.filesystems.FileObject;
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
        dialog.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(
            DefineCorrelationWizard.class, "A11_DESCRIPTOR_DefineCorrelationWizardDialog"));
        dialog.getAccessibleContext().setAccessibleName(NbBundle.getMessage(
            DefineCorrelationWizard.class, "A11_NAME_DefineCorrelationWizardDialog"));
        
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
    
        iconFileName = IMAGE_FOLDER_NAME + "ATTRIBUTE" + IMAGE_FILE_EXT; // NOI18N
        mapIcons.put(Attribute.class, new ImageIcon(Utilities.loadImage(iconFileName)));
    
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
                } catch (ClassCastException e) { // ignore
                } catch (Exception ex) {
                    ErrorManager.getDefault().notify(ex);
                }
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
            //permittedActivityTypeSet = new HashSet<Class>(Arrays.asList(new Class[] {
            //    Responder.class}));
            //forbiddenActivityTypeSet = new HashSet<Class>(Arrays.asList(new Class[] {
            //    Requester.class}));
        }
        @Override
        public List<BpelEntity> getPermittedActivityList(BpelEntity mainBpelEntity) {
            List<BpelEntity> activityList = super.getPermittedActivityList(mainBpelEntity);
            if (activityList.isEmpty()) return activityList;
            
            // remove all Responder-activities above mainBpelEntity and mainBpelEntity itself
            // activityList = removeResponderActivityAbove(activityList, mainBpelEntity);
            activityList.remove(mainBpelEntity);
            return activityList;
        }
    }
    
    private class ResponderActivityChooser extends AbstractActivityChooser  {
        public ResponderActivityChooser() {
            //permittedActivityTypeSet = new HashSet<Class>(Arrays.asList(new Class[] {
            //    Requester.class}));
        }
        @Override
        public List<BpelEntity> getPermittedActivityList(BpelEntity mainBpelEntity) {
            List<BpelEntity> activityList = super.getPermittedActivityList(mainBpelEntity);
            if (activityList.isEmpty()) return activityList;

            // remove all Requester-activities below mainBpelEntity and mainBpelEntity itself
            // activityList = removeRequesterActivityBelow(activityList, mainBpelEntity);
            activityList.remove(mainBpelEntity);
            return activityList;
        }
    }
    
    private class RequesterResponderActivityChooser extends AbstractActivityChooser  {
        public RequesterResponderActivityChooser() {
            // it's assumed that Invoke (Requester-Responder) is used as Requester only
            //permittedActivityTypeSet = new HashSet<Class>(Arrays.asList(new Class[] {
            //    Responder.class}));
            //forbiddenActivityTypeSet = new HashSet<Class>(Arrays.asList(new Class[] {
            //    Requester.class}));
        }
        @Override
        public List<BpelEntity> getPermittedActivityList(BpelEntity mainBpelEntity) {
            List<BpelEntity> activityList = super.getPermittedActivityList(mainBpelEntity);
            if (activityList.isEmpty()) return activityList;
            
            // remove all Responder-activities above mainBpelEntity
            // activityList = removeResponderActivityAbove(activityList, mainBpelEntity);
            
            // remove all Requester-activities below mainBpelEntity
            // activityList = removeRequesterActivityBelow(activityList, mainBpelEntity);
            
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
            activityComboBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(
                DefineCorrelationWizard.class, "A11_DESCRIPTOR_ActivityComboBox"));
            activityComboBox.getAccessibleContext().setAccessibleName(NbBundle.getMessage(
                DefineCorrelationWizard.class, "A11_NAME_ActivityComboBox"));
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
                wizardDefineCorrelationPanel.buildCorrelationMapper(currentSelectedActivity, mainBpelEntity);
            } else { // this panel is shown after clicking of the button "Back"
                if (! previousSelectedActivity.equals(currentSelectedActivity)) {
                    wizardDefineCorrelationPanel.buildCorrelationMapper(currentSelectedActivity, null);
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
                    String itemText = "", messagePattern = null;
                    try {
                        if ((value instanceof OnMessage) || (value instanceof OnEvent)) {
                            messagePattern = NbBundle.getMessage(ComboBoxRenderer.class, 
                                (value instanceof OnMessage) ? "LBL_ComboBox_OnMessage_Name_Pattern" : 
                                "LBL_ComboBox_OnEvent_Name_Pattern");
                            itemText = WizardUtils.getBpelEntityName((BpelEntity) value, messagePattern);
                        } else {
                            messagePattern = NbBundle.getMessage(ComboBoxRenderer.class, "LBL_ComboBox_Item_Name_Pattern");
                            itemText = WizardUtils.getBpelEntityName((BpelEntity) value);
                            Object[] messageValues = new Object[] {itemText, ((BpelEntity) value).getElementType().getSimpleName()};
                            itemText = MessageFormat.format(messagePattern, messageValues);
                        }
                    } catch (Exception e) {
                        itemText = value.toString();
                    }
                    ((JLabel) component).setText(itemText);
                }
                return component;
            }
        }
    }
    //========================================================================//
    public class WizardDefineCorrelationPanel extends WizardAbstractPanel 
        implements WizardConstants {
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
                    leftBpelEntity instanceof OnEvent ? "LBL_Mapper_Tree_OnEvent_Name_Pattern" : 
                    "LBL_Mapper_Tree_Top_Node_Name_Pattern"));
                leftTreeModel.buildCorrelationMapperTree(topLeftTreeNode);
                isMapperChanged = true;
            }
            if (rightBpelEntity != null) {
                rightTreeModel = new CorrelationMapperTreeModel();
                CorrelationMapperTreeNode topRightTreeNode = buildCorrelationTree(rightBpelEntity,
                    NbBundle.getMessage(WizardDefineCorrelationPanel.class, 
                    rightBpelEntity instanceof OnMessage ? "LBL_Mapper_Tree_OnMessage_Name_Pattern" : 
                    rightBpelEntity instanceof OnEvent ? "LBL_Mapper_Tree_OnEvent_Name_Pattern" : 
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
            setAccessibilityInfo(correlationMapper);
            ((CorrelationMapperModel) mapperModel).expandTree(((CorrelationMapperModel) mapperModel).getLeftTreeModel());
            ((CorrelationMapperModel) mapperModel).expandTree(((CorrelationMapperModel) mapperModel).getRightTreeModel());
        }

        private void setAccessibilityInfo(Mapper correlationMapper) {
            LeftTree leftTree = correlationMapper.getLeftTree();
            leftTree.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(
                DefineCorrelationWizard.class, "A11_DESCRIPTOR_MapperLeftTree"));
            leftTree.getAccessibleContext().setAccessibleName(NbBundle.getMessage(
                DefineCorrelationWizard.class, "A11_NAME_MapperLeftTree"));
            
            Canvas canvas = correlationMapper.getCanvas();
            canvas.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(
                DefineCorrelationWizard.class, "A11_DESCRIPTOR_MapperCanvas"));
            canvas.getAccessibleContext().setAccessibleName(NbBundle.getMessage(
                DefineCorrelationWizard.class, "A11_NAME_MapperCanvas"));
            
            RightTree rightTree = correlationMapper.getRightTree();
            rightTree.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(
                DefineCorrelationWizard.class, "A11_DESCRIPTOR_MapperRightTree"));
            rightTree.getAccessibleContext().setAccessibleName(NbBundle.getMessage(
                DefineCorrelationWizard.class, "A11_NAME_MapperRightTree"));
        }

        private CorrelationMapperTreeNode buildCorrelationTree(BpelEntity topBpelEntity, 
            String nodeNamePattern) {
            CorrelationMapperTreeNode topTreeNode = new CorrelationMapperTreeNode(
                topBpelEntity, nodeNamePattern);
            topTreeNode = buildCorrelationTree(topBpelEntity, topTreeNode);
            return topTreeNode;
        }
        
        private CorrelationMapperTreeNode buildCorrelationTree(BpelEntity topBpelEntity, 
            CorrelationMapperTreeNode topTreeNode) {
            Operation requiredOperation = WizardUtils.getBpelEntityOperation(topBpelEntity);
            handleOperations(topBpelEntity, topTreeNode, requiredOperation);
            return topTreeNode;
        }
        
        private void handleOperations(BpelEntity topBpelEntity, 
            CorrelationMapperTreeNode topTreeNode, Operation operation) {
            Message outputMessage = null, inputMessage = null;
            List<Message> messages = new ArrayList<Message>();
            if (topBpelEntity instanceof Requester) {
                try {
                    OperationParameter output = operation.getOutput();
                    outputMessage = output.getMessage().get();
                    messages.add(outputMessage);
                } catch (Exception e) {}
            }
            if (topBpelEntity instanceof Responder) {
                try {
                    OperationParameter input = operation.getInput();
                    inputMessage = input.getMessage().get();
                    messages.add(inputMessage);
                } catch (Exception e) {}
            }
            handleMessages(topTreeNode, messages);
        }
        
        private void handleMessages(CorrelationMapperTreeNode topTreeNode, 
            Collection<Message> messages) {
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
                WizardUtils.getSchemaComponentTypeRef(schemaComponent);
            return ((typeRef != null) && (typeRef.get() instanceof ComplexType));
        }
        
        private boolean isAttributeUnknownType(SchemaComponent schemaComponent) {
            if (! (schemaComponent instanceof Attribute)) {
                return false;
            }
            return (WizardUtils.getSchemaComponentTypeName(schemaComponent) == null);
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
            try {
                makeCorrelations();
            } finally {
                CorrelationWizardWSDLWrapper.closeInstance();
            }
        }
        
        private void makeCorrelations() throws WizardValidationException {
            CorrelationMapperModel mapperModel = (CorrelationMapperModel) correlationMapper.getModel();
            CorrelationMapperTreeModel 
                leftTreeModel = (CorrelationMapperTreeModel) mapperModel.getLeftTreeModel(), 
                rightTreeModel = (CorrelationMapperTreeModel) mapperModel.getRightTreeModel();
            List<CorrelationLinker> correlationLinkers = getCorrelationLinkers(mapperModel, leftTreeModel, rightTreeModel);
            // group linkers by equivalence of [activity-message-part]
            // to combine appropriate properties in one correlation set
            WSDLModel wizardWsdlModel = null;
            while (! correlationLinkers.isEmpty()) {
                if (wizardWsdlModel == null) {
                    CorrelationWizardWSDLWrapper wizardWsdlWrapper = 
                        CorrelationWizardWSDLWrapper.getInstance(mainBpelEntity.getBpelModel());
                    wizardWsdlModel = wizardWsdlWrapper.getWsdlModel();
                    wizardWsdlWrapper.importIntoBpelModel();
                }
                List<CorrelationLinker> equalLinkerSublist = getSublistEqualCorrelationLinkers(correlationLinkers);
                if (equalLinkerSublist.isEmpty()) break;
                correlationLinkers.removeAll(equalLinkerSublist);
                
                createCorrelationPropertiesAndPropertyAliases(equalLinkerSublist, wizardWsdlModel);
                CorrelationSet correlationSet = createCorrelationSet(equalLinkerSublist);
                equalLinkerSublist.get(0).createActivityCorrelation(correlationSet);
            }
        }

        private CorrelationSet createCorrelationSet(List<CorrelationLinker> linkerList) {
            if ((linkerList == null) || (linkerList.isEmpty())) return null;
            BpelModel bpelModel = mainBpelEntity.getBpelModel();
            BaseScope scopeEntity = bpelModel.getProcess();
            if (scopeEntity == null) {
                return null;
            }
            String correlationSetName = getUniqueCorrelationSetName(linkerList.get(0), 
                scopeEntity);

            BPELElementsBuilder elementBuilder = bpelModel.getBuilder();
            CorrelationSet correlationSet = elementBuilder.createCorrelationSet();
            try {
                correlationSet.setName(correlationSetName);
            } catch (Exception e) {
                ErrorManager.getDefault().notify(e);
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
            addCorrelationSet(bpelModel, scopeEntity, container, correlationSet);
            return correlationSet;
        }

        private void addCorrelationSet(final BpelModel bpelModel, final BaseScope scopeEntity,
            final CorrelationSetContainer correlationSetContainer, final CorrelationSet correlationSet) {
            try {
                bpelModel.invoke(new Callable<CorrelationSet>() {
                    public CorrelationSet call() throws Exception {
                        BPELElementsBuilder elementBuilder = bpelModel.getBuilder();
                        CorrelationSetContainer container = correlationSetContainer;
                        if (container == null) {
                            container = elementBuilder.createCorrelationSetContainer();
                            scopeEntity.setCorrelationSetContainer(container);
                            container = scopeEntity.getCorrelationSetContainer();
                        }
                        container.addCorrelationSet(correlationSet);
                        return correlationSet;
                    }
                }, correlationSet);
            } catch(Exception e) {
                ErrorManager.getDefault().notify(e);
            }
        }
        
        private String getUniqueCorrelationSetName(CorrelationLinker correlationLinker, 
            BaseScope scopeEntity) {
            BpelEntity leftBpelEntity = correlationLinker.getSource().getActivity(),
                       rightBpelEntity = correlationLinker.getTarget().getActivity();
            
            String baseCorrelationSetName = CORRELATION_SET_NAME_PREFIX + 
                WizardUtils.getCorrelationSetBpelEntityName(leftBpelEntity) + "_" + 
                WizardUtils.getCorrelationSetBpelEntityName(rightBpelEntity);

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
                if (WizardUtils.ignoreNamespace(correlationSetName).equals(checkedName)) {
                    return true;
                }
            }
            return false;
        }
        
        private void createCorrelationPropertiesAndPropertyAliases(
            List<CorrelationLinker> linkerList, WSDLModel wizardWsdlModel) throws WizardValidationException {
            for (CorrelationLinker linker : linkerList) {
                linker.createCorrelationProperty(wizardWsdlModel);
                linker.createPropertyAlias(wizardWsdlModel);
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
        //====================================================================//
        private class CorrelationLinker {
            private CorrelationDataHolder source, target;
            private CorrelationProperty correlationProperty;
            
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
            
            public void createPropertyAlias(WSDLModel wizardWsdlModel) {
                source.createPropertyAlias(wizardWsdlModel, correlationProperty);
                target.createPropertyAlias(wizardWsdlModel, correlationProperty);
            }

            public void createCorrelationProperty(WSDLModel wizardWsdlModel) throws WizardValidationException {
                String propertyName = getBasePropertyName();
                
                correlationProperty = (CorrelationProperty) wizardWsdlModel.getFactory().create(
                    wizardWsdlModel.getDefinitions(), BPELQName.PROPERTY.getQName());
                correlationProperty.setName(propertyName);
                
                NamedComponentReference<GlobalType> typeRef = source.getGlobalTypeReference();
                if (typeRef != null) {
                    correlationProperty.setType(typeRef);
                }                
                assert (typeRef != null);
                WizardUtils.importWsdlIntoWsdl(wizardWsdlModel, source.getWSDLModel());
                WizardUtils.importWsdlIntoWsdl(wizardWsdlModel, target.getWSDLModel());
                if (Util.isUniquePropertyName(wizardWsdlModel, propertyName)) {
                    try {
                        wizardWsdlModel.startTransaction();
                        wizardWsdlModel.addChildComponent(wizardWsdlModel.getRootComponent(), 
                            correlationProperty, 0);
                    } finally {
                        wizardWsdlModel.endTransaction();
                    }
                }
            }
            
            private String getBasePropertyName() {
                return CORRELATION_PROPERTY_NAME_PREFIX + 
                       WizardUtils.getSchemaComponentName(source.getSchemaComponent()) + 
                       "_" +
                       WizardUtils.getSchemaComponentName(target.getSchemaComponent()); 
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
                    String sourceComponentName = WizardUtils.getSchemaComponentName(sourceSchemaComponent),
                           targetComponentName = WizardUtils.getSchemaComponentName(targetSchemaComponent);
                    String errMsg = MessageFormat.format(NbBundle.getMessage(WizardDefineCorrelationPanel.class, 
                        "LBL_ErrMsg_Different_Schema_Component_Types"),
                        new Object[] {sourceComponentName, targetComponentName});
                    //wizardDescriptor.putProperty(PROPERTY_ERROR_MESSAGE, errMsg);                              
                    throw new WizardValidationException(wizardPanel, errMsg, errMsg);
                }
            }
        }
        //--------------------------------------------------------------------//
        private class CorrelationDataHolder implements WizardConstants {
            private BpelEntity activity;
            private Message message;
            private Part part;
            private CorrelationMapperTreeNode mapperTreeNode;

            public WSDLModel getWSDLModel() {
                PortType portType = ((PortTypeReference) activity).getPortType().get();
                return portType.getModel();
            }
        
            public void createActivityCorrelation(CorrelationSet correlationSet) {
                BpelModel bpelModel = activity.getBpelModel();
                BaseCorrelation correlation = createCorrelation(bpelModel, correlationSet);
                    
                BpelContainer container = null;
                if (activity instanceof Invoke) {
                    container = ((Invoke) activity).getPatternedCorrelationContainer();
                } else if ((activity instanceof Receive) || (activity instanceof Reply)) {
                    container = ((InvokeReceiveReplyCommonImpl) activity).getCorrelationContainer();
                } else if ((activity instanceof OnMessage) || (activity instanceof OnEvent)) {
                    container = ((OnMessageCommonImpl) activity).getCorrelationContainer();
                }
                addActivityCorrelation(bpelModel, container, correlation);
            }

            private void addActivityCorrelation(final BpelModel bpelModel, 
                final BpelContainer bpelContainer, final BaseCorrelation correlation) {
                try {
                    bpelModel.invoke(new Callable<Object>() {
                        public Object call() throws Exception {
                            BpelContainer container = bpelContainer;
                            if (container == null) {
                                BPELElementsBuilder elementBuilder = bpelModel.getBuilder();
                                container = activity instanceof Invoke ? 
                                    elementBuilder.createPatternedCorrelationContainer() : 
                                    elementBuilder.createCorrelationContainer();
                                if (activity instanceof Invoke) {
                                    ((Invoke) activity).setPatternedCorrelationContainer((PatternedCorrelationContainer) container);
                                    container = ((Invoke) activity).getPatternedCorrelationContainer();
                                } else if ((activity instanceof Receive) || (activity instanceof Reply)) {
                                    ((InvokeReceiveReplyCommonImpl) activity).setCorrelationContainer((CorrelationContainer) container);
                                    container = ((InvokeReceiveReplyCommonImpl) activity).getCorrelationContainer();
                                } else if ((activity instanceof OnMessage) || (activity instanceof OnEvent)) {
                                    ((OnMessageCommonImpl) activity).setCorrelationContainer((CorrelationContainer) container);
                                    container = ((OnMessageCommonImpl) activity).getCorrelationContainer();
                                }
                            }
                            if (activity instanceof Invoke) {
                                ((PatternedCorrelationContainer) container).addPatternedCorrelation((PatternedCorrelation) correlation);
                            } else {
                                ((CorrelationContainer) container).addCorrelation((Correlation) correlation);
                            }
                            return correlation;
                        }
                    }, correlation);
                } catch(Exception e) {
                    ErrorManager.getDefault().notify(e);
                }
            }
            
            private BaseCorrelation createCorrelation(BpelModel bpelModel, 
                CorrelationSet correlationSet) {
                BPELElementsBuilder elementBuilder = bpelModel.getBuilder();
                BaseCorrelation correlation = null;
                try {
                    correlation = activity instanceof Invoke ?
                        elementBuilder.createPatternedCorrelation() : 
                        elementBuilder.createCorrelation();

                    BpelReference<CorrelationSet> correlationSetRef = correlation.createReference(
                    correlationSet, CorrelationSet.class);
                    correlation.setSet(correlationSetRef);
                    setCorrelationInitiateValue(correlation);
                    if (correlation instanceof PatternedCorrelation) {
                        Pattern pattern = defineInvokeCorrelationPattern(activity);
                        if (pattern != null) {
                            ((PatternedCorrelation) correlation).setPattern(pattern);
                        }
                    }
                } catch (Exception e) {
                    ErrorManager.getDefault().notify(e);
                }
                return correlation;
            }

            /** The Rule:
             *  - for a CORRELATED activity, on which a pop-up menu has been invoked
             *    and which is related to the RIGHT mapper tree, the value "no" is used
             *    <correlation ... initiate="no"/>
             *  - for an INITIATED activity, which is related to the LEFT mapper tree, 
             *    the value "yes" is used
             *    <correlation ... initiate="yes"/>
             */
            private void setCorrelationInitiateValue(BaseCorrelation correlation) {
                correlation.setInitiate(mainBpelEntity.equals(activity) ? // is this activity CORRELATED
                    Initiate.NO :  // for CORRELATED activity
                    Initiate.YES); // for INITIATED activity
            }
            
            private Pattern defineInvokeCorrelationPattern(BpelEntity bpelEntity) {
                assert (bpelEntity != null);
                Operation operation = WizardUtils.getBpelEntityOperation(bpelEntity);
                // Rule: The pattern attribute used in <correlation>  within 
                // <invoke> is required for request-response operations, and 
                // disallowed when a one-way operation (OneWayOperation) is invoked.
                if ((bpelEntity instanceof Invoke) && 
                    (operation instanceof RequestResponseOperation)) {
                    Message outputMessage = null, inputMessage = null;
                    try {
                        OperationParameter output = operation.getOutput();
                        outputMessage = output.getMessage().get();
                    } catch(Exception e) {}
                    try {
                        OperationParameter input = operation.getInput();
                        inputMessage = input.getMessage().get();
                    } catch(Exception e) {}
                    if ((inputMessage != null) && (outputMessage != null) && 
                        (message.equals(inputMessage)) && (message.equals(outputMessage))) {
                        return Pattern.REQUEST_RESPONSE;
                    } else if ((inputMessage != null) && (message.equals(inputMessage))) {
                        return Pattern.REQUEST;
                    } else if ((outputMessage != null) && (message.equals(outputMessage))) {
                        return Pattern.RESPONSE;
                    }
                }
                return null;
            }
            
            public void createPropertyAlias(WSDLModel wizardWsdlModel, CorrelationProperty correlationProperty) {
                if (! WizardUtils.wsdlContainsPropertyAlias(wizardWsdlModel, correlationProperty, message, part)) {
                    PropertyAlias propertyAlias = (PropertyAlias) wizardWsdlModel.getFactory().create(
                        wizardWsdlModel.getDefinitions(), BPELQName.PROPERTY_ALIAS.getQName());

                    NamedComponentReference<CorrelationProperty> correlationPropertyRef =
                        propertyAlias.createReferenceTo(correlationProperty, CorrelationProperty.class);
                    propertyAlias.setPropertyName(correlationPropertyRef);

                    NamedComponentReference<Message> messageTypeRef =
                        propertyAlias.createReferenceTo(message, Message.class);
                    propertyAlias.setMessageType(messageTypeRef);

                    propertyAlias.setPart(part.getName());
                    
                    Query query = getPropertyAliasQuery(wizardWsdlModel,
                        propertyAlias);
                    if (query != null) propertyAlias.setQuery(query);
                    
                    try {
                        wizardWsdlModel.startTransaction();
                        wizardWsdlModel.addChildComponent(wizardWsdlModel.getRootComponent(), 
                            propertyAlias, 0);
                    } finally {
                        wizardWsdlModel.endTransaction();
                    }
                }
            }
            
            private Query getPropertyAliasQuery(WSDLModel wizardWsdlModel,
                PropertyAlias propertyAlias) {
                // Query is used for a property alias only if a part contains an attribute 
                // <element> (not an attribute <type>, either complex type or simple type)
                NamedComponentReference<GlobalElement> partElementRef = part.getElement();
                if ((partElementRef != null) && (getSchemaComponent() != null)) {
                    CorrelationMapperTreeNode parentObj = mapperTreeNode;
                    Object userObj = parentObj.getUserObject();
                    List<SchemaComponent> queryComponents = new ArrayList<SchemaComponent>();
                    do {
                        queryComponents.add(0, (SchemaComponent) userObj);                            
                        parentObj = (CorrelationMapperTreeNode) parentObj.getParent();
                        userObj = parentObj.getUserObject();
                    } while (! (userObj instanceof Part)); 
                    WizardUtils.importRequiredSchemas(wizardWsdlModel, queryComponents);
                    String strQueryAbsPath = WizardUtils.makeLocationPath(wizardWsdlModel, 
                        queryComponents);
                    if (strQueryAbsPath.length() > 0) {
                        Query query = (Query) wizardWsdlModel.getFactory().create(
                            propertyAlias, BPELQName.QUERY.getQName());
                        query.setContent(strQueryAbsPath);
                        return query;
                    }
                }
                return null;
            }
                
            public NamedComponentReference<GlobalType> getGlobalTypeReference() throws WizardValidationException {
                String typeName = getTypeNameIgnoreNamespace();

                Collection<GlobalSimpleType> 
                    primitiveSimpleTypes = SchemaModelFactory.getDefault().getPrimitiveTypesModel().getSchema().getSimpleTypes();
                NamedComponentReference<GlobalType> typeRef = findGlobalSimpleType(typeName, 
                    primitiveSimpleTypes);
                if (typeRef == null) {
                    typeRef = resolveSimpleType(typeName, primitiveSimpleTypes);
                }
                if (typeRef != null) return typeRef;
                
                String errMsg = MessageFormat.format(NbBundle.getMessage(WizardDefineCorrelationPanel.class, 
                    "LBL_ErrMsg_Unknown_Schema_Component_Type"), new Object[] {
                    typeName, WizardUtils.getSchemaComponentName(getSchemaComponent())});
                throw new WizardValidationException(wizardPanel, errMsg, errMsg);
            }

            private NamedComponentReference<GlobalType> resolveSimpleType(String typeName,
                Collection<GlobalSimpleType> primitiveSimpleTypes) {
                Collection<GlobalSimpleType> 
                    globalSimpleTypes = getSchemaComponent().getModel().getSchema().getSimpleTypes();
                GlobalSimpleType simpleType = null;
                for (GlobalSimpleType globalSimpleType : globalSimpleTypes) {
                    if (globalSimpleType.toString().equals(typeName)) {
                        simpleType = globalSimpleType;
                        break;
                    }
                }
                if (simpleType != null) {
                    List<SchemaComponent> children = simpleType.getChildren();
                    typeName = getBaseSimpleTypeName(children, primitiveSimpleTypes, globalSimpleTypes);
                    if (typeName != null) {
                        NamedComponentReference<GlobalType> typeRef = findGlobalSimpleType(typeName, 
                            primitiveSimpleTypes);
                        return typeRef;
                    }
                }
                return null;
            }
            
            private String getBaseSimpleTypeName(List<SchemaComponent> componentList,
                Collection<GlobalSimpleType> primitiveSimpleTypes,
                Collection<GlobalSimpleType> globalSimpleTypes) {
                if ((componentList == null) || (primitiveSimpleTypes == null) || 
                    (globalSimpleTypes == null)) return null;
                
                for (SchemaComponent component : componentList) {
                 String baseTypeName = component.getAnyAttribute(new QName(
                     WizardConstants.SCHEMA_COMPONENT_ATTRIBUTE_BASE));
                    if (baseTypeName != null) {
                        baseTypeName = WizardUtils.ignoreNamespace(baseTypeName);
                        NamedComponentReference<GlobalType> typeRef = findGlobalSimpleType(baseTypeName, 
                            primitiveSimpleTypes);
                        if (typeRef != null) return baseTypeName;
                        typeRef = findGlobalSimpleType(baseTypeName, globalSimpleTypes);
                        if (typeRef != null) {
                            return getBaseSimpleTypeName(component.getChildren(), primitiveSimpleTypes, globalSimpleTypes);
                        } else {
                            return null;
                        }
                    }
                }
                return null;
            }            
            private NamedComponentReference<GlobalType> findGlobalSimpleType(String typeName,
                Collection<GlobalSimpleType> globalSimpleTypes) {
                NamedComponentReference<GlobalType> typeRef = null;
                for (GlobalSimpleType globalSimpleType : globalSimpleTypes) {
                    if (globalSimpleType.toString().equals(typeName)) {
                        typeRef = getSchemaComponent().createReferenceTo(globalSimpleType, GlobalType.class);
                        return typeRef;
                    }
                }
                return null;
            }
            
            public String getTypeNameIgnoreNamespace() {
                String typeName = WizardUtils.getSchemaComponentTypeName(getSchemaComponent());
                return WizardUtils.ignoreNamespace(typeName);
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
                mapperTreeNode = (CorrelationMapperTreeNode) treePath.getLastPathComponent();
                getPartAndMessage(mapperTreeNode);
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
                        } catch (Exception e) {
                            ErrorManager.getDefault().notify(e);
                        }
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
            public CorrelationMapperTreeNode getMapperTreeNode() {return mapperTreeNode;}
            public SchemaComponent getSchemaComponent() {
                try {
                    return ((SchemaComponent) mapperTreeNode.getUserObject());
                } catch(ClassCastException cce) {
                    ErrorManager.getDefault().notify(cce);
                    return null;
                }
            }
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
                        if ((userObj instanceof OnMessage) || (userObj instanceof OnEvent)) {
                            return WizardUtils.getBpelEntityName((BpelEntity) userObj, nodeNamePattern);
                        } else {
                            userObjectName = WizardUtils.getBpelEntityName((BpelEntity) userObj);
                            patternValues = new Object[] {userObjectName, ((BpelEntity) userObj).getElementType().getSimpleName()};
                        }
                    } else if (userObj instanceof Message) {
                        userObjectName = ((Message) userObj).getName();
                    } else if (userObj instanceof Part) {
                        userObjectName = ((Part) userObj).getName();
                    } else if ((userObj instanceof Element) || (userObj instanceof Attribute)) {
                        userObjectName = WizardUtils.getSchemaComponentName((SchemaComponent) userObj);
                        if (getChildCount() == 0) { // simple type
                            String typeName = WizardUtils.getSchemaComponentTypeName((SchemaComponent) userObj);
                            if (typeName != null) {
                                userObjectName += " " + MessageFormat.format(SIMPLE_TYPE_NAME_PATTERN, 
                                    new Object[] {typeName});
                            }
                        }
                    } else if ((userObj instanceof SimpleType) || (userObj instanceof ComplexType)) {
                        userObjectName = WizardUtils.getSchemaComponentName((SchemaComponent) userObj);
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
                    if (userObj instanceof Attribute) userObjClass = Attribute.class;
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
                expandTreeNode(treeNode, (isRightTreeExpanded ? 6 : 5), isRightTreeExpanded);
                // value (-1) for the variable "expandLevel" means that all 
                // tree nodes should be expanded
                // expandTreeNode(treeNode, -1, isRightTreeExpanded);
            }
            
            private void expandTreeNode(TreeNode treeNode, int expandLevel, 
                boolean isRightTreeExpanded) {
                if ((treeNode == null) || (expandLevel == 0)) return;
                
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
                expandLevel = (expandLevel == -1 ? -1 : expandLevel - 1);
                if (expandLevel == 0) return;
                
                int childCount = treeNode.getChildCount();
                for (int i = 0; i < childCount; ++i) {
                    TreeNode childNode = treeNode.getChildAt(i);
                    expandTreeNode(childNode, expandLevel, isRightTreeExpanded);
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

            public GraphSubset copy(TreePath treePath, GraphSubset graphGroup, int x, int y) {
                return null;
            }

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
//============================================================================//
class CorrelationWizardWSDLWrapper {
    private static final String 
        WIZARD_PROPERTIES_WSDL_FILE_NAME = "WizardCorrelationProperties", // NOI18N
        WSDL_FILE_EXTENSION = "wsdl", // NOI18N
        HOST = "http://enterprise.netbeans.org/bpel/"; // NOI18N
    
    private static CorrelationWizardWSDLWrapper wizardWSDLWrapperInstance = null;
    
    private BpelModel bpelModel;
    private WSDLModel wsdlModel;
    
    private CorrelationWizardWSDLWrapper() {}

    public static final CorrelationWizardWSDLWrapper getInstance(BpelModel bpelModel) {
        if (wizardWSDLWrapperInstance == null) {
            wizardWSDLWrapperInstance = new CorrelationWizardWSDLWrapper();
        }
        if (bpelModel != null) {
            wizardWSDLWrapperInstance.bpelModel = bpelModel;
        }
        assert (wizardWSDLWrapperInstance.bpelModel != null);
        return wizardWSDLWrapperInstance;
    }
    
    public static void closeInstance() {
        if (wizardWSDLWrapperInstance != null) {
            wizardWSDLWrapperInstance.wsdlModel = null;
            wizardWSDLWrapperInstance.bpelModel = null;
        }
        wizardWSDLWrapperInstance = null;
    }
    
    public WSDLModel getWsdlModel() {
        if (wsdlModel == null) createWSDLPropertiesFile();
        return wsdlModel;
    }
    
    private FileObject createWSDLPropertiesFile() {
        try {
            FileObject folderBpelProcess = ResolverUtility.getBpelProcessFolder(bpelModel);
            WsdlWrapper wsdlWrapper = new WsdlWrapper(folderBpelProcess, 
                WIZARD_PROPERTIES_WSDL_FILE_NAME, true);
            wsdlModel = wsdlWrapper.getModel();
            Definitions definitions = wsdlModel.getDefinitions();

            wsdlModel.startTransaction();
            definitions.setName(getShortWsdlFileName());
            definitions.setTargetNamespace(HOST + WIZARD_PROPERTIES_WSDL_FILE_NAME);
            
            return wsdlWrapper.getFile();
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
        } finally {
            wsdlModel.endTransaction();
        }
        return null;
    }
    
    public String getShortWsdlFileName() {
        return (WIZARD_PROPERTIES_WSDL_FILE_NAME + "." + WSDL_FILE_EXTENSION);
    }
    
    public void importIntoBpelModel() {
        if (bpelModel == null) return;
        try {
            Import wsdlBpelImport = bpelModel.getBuilder().createImport();
            wsdlBpelImport.setNamespace(wsdlModel.getDefinitions().getTargetNamespace());
            wsdlBpelImport.setLocation(getShortWsdlFileName());
            wsdlBpelImport.setImportType(Import.WSDL_IMPORT_TYPE);
            new ImportRegistrationHelper(bpelModel).addImport(wsdlBpelImport);
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
        }
    }
}
//============================================================================//
class WizardUtils implements WizardConstants {
    public static String getCorrelationSetBpelEntityName(BpelEntity bpelEntity) {
        String onMessageOnEventNamePattern = null;
        if ((bpelEntity instanceof OnMessage) || (bpelEntity instanceof OnEvent)) {
            onMessageOnEventNamePattern = NbBundle.getMessage(DefineCorrelationWizard.class, 
                (bpelEntity instanceof OnMessage) ? 
                "LBL_Correlation_Set_Name_OnMessage_Pattern" :
                "LBL_Correlation_Set_Name_OnEvent_Pattern");
        }
        return getBpelEntityName(bpelEntity, onMessageOnEventNamePattern);
    }

    public static String getBpelEntityName(BpelEntity bpelEntity) {
        return getBpelEntityName(bpelEntity, null);
    }
    
    public static String getBpelEntityName(BpelEntity bpelEntity, String onMessageOnEventNamePattern) {
        if ((bpelEntity instanceof OnMessage) || (bpelEntity instanceof OnEvent)) {
            assert (onMessageOnEventNamePattern != null);
            BpelEntityComplexName compositeName = (bpelEntity instanceof OnMessage) ?
                new OnMessageComplexName((OnMessage) bpelEntity) :
                new OnEventComplexName((OnEvent) bpelEntity);
            String 
                entityName = compositeName.getFirstName(),
                relatedObjName = compositeName.getMiddleName(),
                operationName = compositeName.getLastName();
            return MessageFormat.format(onMessageOnEventNamePattern, new Object[] {
                entityName, relatedObjName, operationName});
        }
        return bpelEntity.getAttribute(BpelAttributes.NAME);
    }
    
    public static NamedComponentReference<? extends GlobalType> getSchemaComponentTypeRef(SchemaComponent schemaComponent) {
        NamedComponentReference<? extends GlobalType> typeRef = null;
        try {
            typeRef = ((TypeContainer) schemaComponent).getType();
        } catch (Exception e) {}
        return typeRef;
    }

    public static String getSchemaComponentTypeName(SchemaComponent schemaComponent) {
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

    public static String getSchemaComponentName(SchemaComponent schemaComponent) {
        String name = null;
        if (schemaComponent instanceof SimpleType) {
            name = getSchemaComponentTypeName(schemaComponent);
        } else  {
            name = schemaComponent.toString();
        }
        return name;
    }
    
    public static Operation getBpelEntityOperation(BpelEntity bpelEntity) {
        if (bpelEntity == null) return null;
        PortType portType = ((PortTypeReference) bpelEntity).getPortType().get();
        Collection<Operation> operations = portType.getOperations();
        String requiredOperationName = bpelEntity.getAttribute(BpelAttributes.OPERATION);
        for (Operation operation : operations) {
            if (operation.getName().equals(requiredOperationName)) {
                return operation;
            }
        }
        return null;
    }
    
    public static String ignoreNamespace(String dataWithNamespace) {
        int index = dataWithNamespace.indexOf(":");
        if ((index > -1) && (index < dataWithNamespace.length() - 1)) {
            return dataWithNamespace.substring(index + 1);
        }
        return dataWithNamespace;
    }

    public static void importRequiredSchemas(WSDLModel wsdlModel, 
        List<SchemaComponent> schemaComponents) {
        try {
            wsdlModel.startTransaction();
            for (SchemaComponent schemaComponent : schemaComponents) {
                Utility.addSchemaImport(schemaComponent, wsdlModel);
            }
        } catch(Exception e) {
            ErrorManager.getDefault().notify(e);
        } finally {
            wsdlModel.endTransaction();
        }
    }
    
    public static void importWsdlIntoWsdl(WSDLModel baseWsdlModel, WSDLModel importedWsdlModel) {
        try {
            org.netbeans.modules.xml.wsdl.model.Import objImport = 
                baseWsdlModel.getFactory().createImport();
            
            objImport.setNamespace(importedWsdlModel.getDefinitions().getTargetNamespace());

            FileObject importedFileObject = 
                importedWsdlModel.getModelSource().getLookup().lookup(FileObject.class);
            objImport.setLocation(importedFileObject.getNameExt());

            if (! wsdlContainsImport(baseWsdlModel, objImport)) {
                baseWsdlModel.startTransaction();
                baseWsdlModel.getDefinitions().addImport(objImport);
            }
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
        } finally {
            baseWsdlModel.endTransaction();
        }
    }
    
    public static boolean wsdlContainsImport(WSDLModel baseWsdlModel, 
        org.netbeans.modules.xml.wsdl.model.Import checkedImport) {
        Collection<org.netbeans.modules.xml.wsdl.model.Import> imports = 
            baseWsdlModel.getDefinitions().getImports();
        for (org.netbeans.modules.xml.wsdl.model.Import existingImport : imports) {
            if ((existingImport.getNamespace().equals(checkedImport.getNamespace())) &&
                (existingImport.getLocation().equals(checkedImport.getLocation()))) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean wsdlContainsPropertyAlias(WSDLModel baseWsdlModel, 
        CorrelationProperty checkedProperty, Message checkedMessage, Part checkedPart) {
        List<PropertyAlias> propertyAliases = 
            baseWsdlModel.getRootComponent().getChildren(PropertyAlias.class);
        for (PropertyAlias existingPropertyAlias : propertyAliases) {
            CorrelationProperty existingProperty = existingPropertyAlias.getPropertyName().get();
            Message existingMessage = existingPropertyAlias.getMessageType().get();
            String existingPart = existingPropertyAlias.getPart();
            
            if ((existingProperty.getName().equals(checkedProperty.getName())) &&
                (existingMessage.getName().equals(checkedMessage.getName())) &&
                (existingPart.equals(checkedPart.getName()))) {
                return true;
            }
        }
        return false;
    }

    public static String makeLocationPath(final WSDLModel wsdlModel,
        final List<SchemaComponent> schemaComponents) {
        if ((schemaComponents == null) || (schemaComponents.isEmpty())) return null;
        XPathModelHelper xpathHelper = XPathModelHelper.getInstance();
        XPathModel xpathModel = xpathHelper.newXPathModel();

        NamespaceContext namespaceContext = new WsdlNamespaceContext(
            wsdlModel.getDefinitions());
        xpathModel.setNamespaceContext(namespaceContext);
        
        xpathModel.setExternalModelResolver(new ExternalModelResolver() {
            public Collection<SchemaModel> getModels(String schemaNamespaceUri) {
                List<Schema> schemaList = wsdlModel.findSchemas(schemaNamespaceUri);
                ArrayList<SchemaModel> schemaModels = new ArrayList<SchemaModel>(schemaList.size());
                for (Schema schema : schemaList) {
                    SchemaModel schemaModel = schema.getModel();
                    schemaModels.add(schemaModel);
                }
                return schemaModels;
            }

            public Collection<SchemaModel> getVisibleModels() {
                SchemaModel schemaModel = schemaComponents.get(0).getModel();
                return Collections.singleton(schemaModel);
            }

            public boolean isSchemaVisible(String schemaNamespaceUri) {
                List<Schema> schemaList = wsdlModel.findSchemas(schemaNamespaceUri);
                return ((schemaList != null) && (schemaList.size() > 0));
            }
        });
        XPathModelFactory xpathModelFactory = xpathModel.getFactory();
        List<LocationStep> locationSteps = new ArrayList<LocationStep>(
            schemaComponents.size());
        for (SchemaComponent schemaComponent : schemaComponents) {
            String namespacePrefix = getNamespacePrefix(wsdlModel, schemaComponent);
            StepNodeNameTest stepNodeNameTest = null;
            if (namespacePrefix != null) {
                namespacePrefix = XPathUtils.isPrefixRequired(schemaComponent) ? 
                    namespacePrefix : "";
                stepNodeNameTest = new StepNodeNameTest(new QName(null, 
                getSchemaComponentName(schemaComponent), namespacePrefix));
            } else {
                new StepNodeNameTest(xpathModel, schemaComponent);
            }
            LocationStep locationStep = xpathModelFactory.newLocationStep(null, 
                stepNodeNameTest, null);
            locationSteps.add(locationStep);
        }
        XPathLocationPath locationPath = xpathModelFactory.newXPathLocationPath(
            locationSteps.toArray(new LocationStep[locationSteps.size()]));
        locationPath.setAbsolute(true);
        xpathModel.setRootExpression(locationPath);
        return locationPath.getExpressionString();
    }
    
    public static String getNamespacePrefix(WSDLModel wsdlModel, SchemaComponent schemaComponent) {
        assert ((wsdlModel != null) && (schemaComponent != null));
        String uri = schemaComponent.getModel().getSchema().getTargetNamespace();
        if (uri != null) {
            try {
                new URI(uri);
            }
            catch (URISyntaxException e) {
                ErrorManager.getDefault().notify(e);
                return null;
            }
        }
        try {
            String prefix = getNamespacePrefix(wsdlModel, uri);
            if (prefix != null) {
                return prefix;
            }
            prefix = DEFAULT_NS_PREFIX;

            int i = getMaxSuffixNumber(wsdlModel, prefix);
            prefix += (++i);
            
            wsdlModel.startTransaction();
            if (i == 0) { 
                ((AbstractDocumentComponent) wsdlModel.getDefinitions()).addPrefix(
                    prefix, uri);
            }
            return prefix;
        }
        finally {
            wsdlModel.endTransaction();
        }
    }
    
    private static String getNamespacePrefix(WSDLModel wsdlModel, String uri) {
        String prefix = wsdlModel.getDefinitions().getPeer().lookupPrefix(uri);
        if ((prefix != null) && (prefix.length() == 0)) {
            return null;
        }
        return prefix;
    }
    
    private static int getMaxSuffixNumber(WSDLModel wsdlModel, String checkedPrefix) {
        assert (checkedPrefix != null);
        Set<String> prefixes = ((AbstractDocumentComponent) 
            wsdlModel.getDefinitions()).getPrefixes().keySet();
        int maxSuffixNumber = -1;
        for (String registeredPrefix : prefixes) {
            if (registeredPrefix.startsWith(checkedPrefix)) {
                String end = registeredPrefix.substring(checkedPrefix.length());
                try {
                    int suffixNumber = Integer.parseInt(end);
                    if (suffixNumber > maxSuffixNumber) {
                        maxSuffixNumber = suffixNumber;
                    }
                }
                catch (NumberFormatException e) {}
            }
        }
        return maxSuffixNumber;
    }
}
//============================================================================//
interface BpelEntityComplexName {
    String getFirstName();
    String getMiddleName();
    String getLastName();
}
//============================================================================//
class OnMessageComplexName implements BpelEntityComplexName {
    private String simpleName = "", pickName = "", operationName = "";

    public OnMessageComplexName(OnMessage onMessageEntity) {
        simpleName = ((BpelEntity) onMessageEntity).getElementType().getSimpleName();
        pickName = ((BpelEntity) onMessageEntity).getParent().getAttribute(BpelAttributes.NAME);
        operationName = ((BpelEntity) onMessageEntity).getAttribute(BpelAttributes.OPERATION);
    }
    public String getSimpleName() {return simpleName;}
    public String getOperationName() {return operationName;}
    public String getPickName() {return pickName;}

    public String getFirstName() {return getSimpleName();}
    public String getMiddleName() {return getPickName();}
    public String getLastName() {return getOperationName();}
}
//============================================================================//
class OnEventComplexName implements BpelEntityComplexName {
    private String simpleName = "", partnerLinkName = "", operationName = "";

    public OnEventComplexName(OnEvent onEventEntity) {
        simpleName = ((BpelEntity) onEventEntity).getElementType().getSimpleName();
        partnerLinkName = ((BpelEntity) onEventEntity).getAttribute(
            BpelAttributes.PARTNER_LINK);
        operationName = ((BpelEntity) onEventEntity).getAttribute(BpelAttributes.OPERATION);
    }
    public String getSimpleName() {return simpleName;}
    public String getOperationName() {return operationName;}
    public String getPartnerLinkName() {return partnerLinkName;}
    
    public String getFirstName() {return getSimpleName();}
    public String getMiddleName() {return getPartnerLinkName();}
    public String getLastName() {return getOperationName();}
}
//============================================================================//
interface WizardConstants {
    String 
        CORRELATION_PROPERTY_NAME_PREFIX = "wzrd_prop_", // NOI18N
        CORRELATION_SET_NAME_PREFIX = "wzrd_set_", // NOI18N
        SCHEMA_COMPONENT_ATTRIBUTE_BASE = "base", // NOI18N
        DEFAULT_NS_PREFIX = "ns"; // NOI18N
}
//============================================================================//
class WsdlNamespaceContext implements NamespaceContext {
    private WSDLComponent mXPathOwner;
    
    public WsdlNamespaceContext(WSDLComponent xPathOwner) {
        mXPathOwner = xPathOwner;
    }

    public String getNamespaceURI(String prefix) {
        if (prefix == null || prefix.length() == 0) {
            // the default namespace isn't supported by BPEL XPath
            // so, empty prefix corresponds to empty namespace.
            return XMLConstants.NULL_NS_URI;
        }
        assert (mXPathOwner instanceof AbstractDocumentComponent);
        String nsUri = ((AbstractDocumentComponent) mXPathOwner).lookupNamespaceURI(
            prefix, true);
        return nsUri;
    }

    public String getPrefix(String namespaceURI) {
        assert mXPathOwner instanceof AbstractDocumentComponent;
        String nsPrefix = ((AbstractDocumentComponent) mXPathOwner).lookupPrefix(
            namespaceURI);
        return nsPrefix;
    }

    public Iterator getPrefixes(String namespaceURI) {
        String single = getPrefix(namespaceURI);
        return Collections.singletonList(single).iterator();
    }
}
//============================================================================//
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