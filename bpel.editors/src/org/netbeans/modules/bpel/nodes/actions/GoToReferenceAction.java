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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bpel.nodes.actions;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import org.netbeans.modules.bpel.editors.api.EditorUtil;
import org.netbeans.modules.bpel.editors.api.nodes.actions.ActionType;
import org.netbeans.modules.bpel.model.api.Assign;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Catch;
import org.netbeans.modules.bpel.model.api.CompensatableActivityHolder;
import org.netbeans.modules.bpel.model.api.Compensate;
import org.netbeans.modules.bpel.model.api.CompensateScope;
import org.netbeans.modules.bpel.model.api.CompensationHandler;
import org.netbeans.modules.bpel.model.api.Else;
import org.netbeans.modules.bpel.model.api.ElseIf;
import org.netbeans.modules.bpel.model.api.Empty;
import org.netbeans.modules.bpel.model.api.EventHandlers;
import org.netbeans.modules.bpel.model.api.Exit;
import org.netbeans.modules.bpel.model.api.FaultHandlers;
import org.netbeans.modules.bpel.model.api.Flow;
import org.netbeans.modules.bpel.model.api.ForEach;
import org.netbeans.modules.bpel.model.api.If;
import org.netbeans.modules.bpel.model.api.Invoke;
import org.netbeans.modules.bpel.model.api.OnAlarmEvent;
import org.netbeans.modules.bpel.model.api.OnAlarmPick;
import org.netbeans.modules.bpel.model.api.OnEvent;
import org.netbeans.modules.bpel.model.api.OnMessage;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.Pick;
import org.netbeans.modules.bpel.model.api.ReThrow;
import org.netbeans.modules.bpel.model.api.Receive;
import org.netbeans.modules.bpel.model.api.RepeatUntil;
import org.netbeans.modules.bpel.model.api.Reply;
import org.netbeans.modules.bpel.model.api.Scope;
import org.netbeans.modules.bpel.model.api.Sequence;
import org.netbeans.modules.bpel.model.api.TerminationHandler;
import org.netbeans.modules.bpel.model.api.Throw;
import org.netbeans.modules.bpel.model.api.Variable;
import org.netbeans.modules.bpel.model.api.Wait;
import org.netbeans.modules.bpel.model.api.While;
import org.netbeans.modules.bpel.model.api.references.ReferenceCollection;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;
import org.netbeans.modules.xml.xam.Component;
import org.openide.awt.Actions;
import org.openide.util.HelpCtx;
import org.openide.util.actions.SystemAction;
import org.openide.windows.WindowManager;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xml.xam.Referenceable;
import org.openide.util.NbBundle;

/**
 *
 * @author anjeleevich
 */
public class GoToReferenceAction extends BpelNodeAction {
    
    private GoToReferenceSubMenuModel subMenuModel;
    
    public GoToReferenceAction() {
        subMenuModel = new GoToReferenceSubMenuModel();
    }

    public final String getBundleName() {
        return NbBundle.getMessage(BpelNodeAction.class, 
                "CTL_GoToReferenceAction"); // NOI18N    
    }


    public ActionType getType() {
        return ActionType.GO_TO_REFERENCE;
    }

    @Override
    protected boolean enable(BpelEntity[] bpelEntities) {
        subMenuModel.updateSubMenuModel(bpelEntities);
        return subMenuModel.getCount() > 0;
    }

    protected void performAction(BpelEntity[] bpelEntities) {}
    
    private static final void performAction(BpelEntity[] bpelEntities, 
            int index) {}
    
    private static final void performAction(BpelEntity[] bpelEntities, 
            SystemAction wrapAction) {}

    protected boolean asynchronous() {
        return false;
    }

    @Override
    public JMenuItem getPopupPresenter() {
        return new SubMenuFix(this, subMenuModel, true);
    }

    @Override
    public JMenuItem getMenuPresenter() {
        return new SubMenuFix(this, subMenuModel, false);
    }

    private static final BpelEntity[] getCurrentEntities() {
        return getBpelEntities(WindowManager.getDefault().getRegistry()
                .getCurrentNodes());
    }
    
    private static class SubMenuFix extends Actions.SubMenu {
        private GoToReferenceSubMenuModel subMenuModel;
                
        public SubMenuFix(Action action, GoToReferenceSubMenuModel model, 
                boolean popup) 
        {
            super(action, model, popup);
            this.subMenuModel = model;
        }
        
        @Override
        public JComponent[] getMenuPresenters() {
            subMenuModel.arm();
            return super.getMenuPresenters();
        }
        
        @Override
        public JComponent[] synchMenuPresenters(JComponent[] items) {
            subMenuModel.arm();
            return super.synchMenuPresenters(items);
        }
    }
    
    private static class GoToReferenceSubMenuModel implements 
            Actions.SubMenuModel 
    {
        private List<String> labels = new ArrayList<String>();
        
        private EventListenerList listeners = new EventListenerList();
        private boolean armed = false;
        
        private List<RPair> rPairList = new ArrayList<RPair>();
        
        void updateSubMenuModel(BpelEntity[] entities) {
            fillReferenceableList(entities);
            
            labels.clear();
            for (RPair rPair : rPairList) {
                if (rPair != null) {
                    labels.add(rPair.getLabel());
                } else {
                    labels.add(null);
                }
            }
            rPairList.clear();
            
            ChangeListener[] changeListeners = listeners
                    .getListeners(ChangeListener.class);
            if (changeListeners != null && changeListeners.length > 0) {
                ChangeEvent event = new ChangeEvent(this);
                for (ChangeListener listener : changeListeners) {
                    listener.stateChanged(event);
                }
            }
        }
        
        public void arm() {
            armed = true;
        }
        
        public int getCount() {
            int count = labels.size();
            if (count == 1 && armed) {
                count = 2;
            }
            armed = false;
            return count;
        }

        public String getLabel(int index) {
            String label = labels.get(index);
            return label;
        }

        public HelpCtx getHelpCtx(int index) {
            return null;
        }

        public void performActionAt(int index) {
            fillReferenceableList(getCurrentEntities());
            
            RPair rPair = null;
            Referenceable r = null;
            
            if (0 <= index && index < rPairList.size()) {
                rPair = rPairList.get(index);
                if (rPair != null) {
                    r = rPair.getReferenceable();
                }
            } 
            
            rPairList.clear();
            
            if (r != null) {
                if (r instanceof Component) {
                    if (canGoToDesign(r)) {
                        EditorUtil.goToDesign((Component) r);
                    } else {
                        EditorUtil.goToSource((Component) r);
                    }
                }
            }
        }

        public void addChangeListener(ChangeListener l) {
            listeners.add(ChangeListener.class, l);
        }

        public void removeChangeListener(ChangeListener l) {
            listeners.remove(ChangeListener.class, l);
        }
        
        private void fillReferenceableList(BpelEntity[] entities) {
            Map<Integer, Set<RPair>> groups 
                    = new TreeMap<Integer, Set<RPair>>();
        
            rPairList.clear();
            
            if (entities != null) {
                for (BpelEntity entity : entities) {
                    if (entity instanceof ReferenceCollection) {
                        Reference[] references = ((ReferenceCollection) entity)
                                .getReferences();
                        
                        if (references != null) {
                            for (Reference ref : references) {
                                if (ref == null) continue;

                                RPair pair = new RPair(ref);

                                int group = pair.getGroup();
                                
                                if (group != 0) {
                                    Set<RPair> groupSet = groups.get(group);
                                    if (groupSet == null) {
                                        groupSet = new TreeSet<RPair>(
                                                R_PAIR_COMPARATOR);
                                        groups.put(group, groupSet);
                                    }
                                    groupSet.add(pair);
                                }
                            }
                        }
                    }
                }
            }
            
            boolean firstGroup = true;
            
            for (Set<RPair> groupSet : groups.values()) {
                if (!firstGroup) {
                    rPairList.add(null);
                }
                firstGroup = false;
                
                for (RPair rPair : groupSet) {
                    rPairList.add(rPair);
                }
            }
        }
    }
    
    private static class RPair {
        private Referenceable referenceable;
        private Reference reference;
        
        private long uid;
        private String label = null;
        
        private int group = 0;
        
        RPair(Reference reference) {
            this.reference = reference;
            this.referenceable = reference.get();
            
            synchronized (UID_LOCK) {
                this.uid = uidCounter++;
            }
            
            if (referenceable instanceof ReferenceableWSDLComponent
                    || referenceable instanceof WSDLComponent) 
            {
                group = WSDL_GROUP;
            } else if (referenceable instanceof ReferenceableSchemaComponent
                    || referenceable instanceof SchemaComponent) 
            {
                group = SCHEMA_GROUP;
            } else if (referenceable instanceof BpelEntity) {
                group = BPEL_GROUP;
            }
            
            if (group != 0) {
                this.label = createLabel();
            }
            
            if (this.label == null || this.label.length() == 0) {
                group = 0;
            }
        }
        
        Referenceable getReferenceable() {
            return referenceable;
        }
        
        Reference getReference() {
            return reference;
        }
        
        String getLabel() {
            return label;
        }
        
        long getUID() {
            return uid;
        }
        
        int getGroup() {
            return group;
        }
        
        String createLabel() {
            String type = null;
            String name = reference.toString();
            
            if (group == BPEL_GROUP) {
                type = createBPELTypeLabel();
            } else if (group == WSDL_GROUP) {
                type = createWSDLTypeLabel();
            } else if (group == SCHEMA_GROUP) {
                type = createSchemaTypeLabel();
            }
            
            if (type == null) {
                type = referenceable.getClass().getSimpleName();
                if (type != null) {
                    if (type.endsWith("Impl")) { // NOI18N
                        type = type.substring(0, type.length() - 4);
                    }
                    
                    for (int i = type.length() - 1; i > 0; i--) {
                        if (Character.isUpperCase(type.charAt(i))) {
                            type = type.substring(0, i) + " " // NOI18N
                                    + type.substring(i);
                        }
                    }
                }
            }
            
            if (name != null && name.length() > 0) {
                label = type + " \"" + name + "\""; // NOI18N
            } else {
                label = type;
            }
            
            return label;
        }
        
        private String createBPELTypeLabel() {
            if (referenceable instanceof PartnerLink) {
                return NbBundle.getMessage(GoToReferenceAction.class, 
                        "GTT_PartnerLink"); // NOI18N
            } else if (referenceable instanceof Variable) {
                return NbBundle.getMessage(GoToReferenceAction.class, 
                        "GTT_Variable"); // NOI18N
            } 
            return null;
        }
        
        private String createWSDLTypeLabel() {
            if (referenceable instanceof PortType) {
                return NbBundle.getMessage(GoToReferenceAction.class, 
                        "GTT_PortType"); // NOI18N
            } else if (referenceable instanceof Operation) {
                return NbBundle.getMessage(GoToReferenceAction.class, 
                        "GTT_Operation"); // NOI18N
            } else if (referenceable instanceof Message) {
                return NbBundle.getMessage(GoToReferenceAction.class, 
                        "GTT_Message"); // NOI18N
            } else if (referenceable instanceof Part) {
                return NbBundle.getMessage(GoToReferenceAction.class, 
                        "GTT_Part"); // NOI18N
            } else if (referenceable instanceof PartnerLinkType) {
                return NbBundle.getMessage(GoToReferenceAction.class, 
                        "GTT_PartnerLinkType"); // NOI18N
            } else if (referenceable instanceof Role) {
                return NbBundle.getMessage(GoToReferenceAction.class, 
                        "GTT_PartnerRole"); // NOI18N
            }
            return null;
        }
        
        private String createSchemaTypeLabel() {
            if (referenceable instanceof GlobalElement) {
                return NbBundle.getMessage(GoToReferenceAction.class, 
                        "GTT_GlobalElement"); // NOI18N
            } else if (referenceable instanceof GlobalType) {
                return NbBundle.getMessage(GoToReferenceAction.class, 
                        "GTT_GlobalType"); // NOI18N
            }
            return null;
        }
        
        private static long uidCounter = 0;
        private static final int BPEL_GROUP = 1;
        private static final int WSDL_GROUP = 2;
        private static final int SCHEMA_GROUP = 3;
        private static final Object UID_LOCK = new Object();
    }

    private static final Comparator<RPair> R_PAIR_COMPARATOR 
            = new Comparator<RPair>() 
    {
        public int compare(RPair rpair1, RPair rpair2) {
            String label1 = rpair1.getLabel();
            String label2 = rpair2.getLabel();
            
            int c = label1.compareToIgnoreCase(label2);
            if (c != 0) return c;
            
            c = label1.compareTo(label2);
            if (c != 0) return c;
            
            long d = rpair1.getUID() - rpair2.getUID();
            if (d > 0) return 1;
            if (d < 0) return -1;
            return 0;
        }
    };
    
    private static boolean canGoToDesign(Referenceable r) {
        if (!(r instanceof BpelEntity)) return false;
        for (Class clazz : GO_TO_DESIGN_LIST) {
            if (clazz.isInstance(r)) return true;
        }
        return false;
    }
    
    private static final List<Class> GO_TO_DESIGN_LIST = new ArrayList<Class>();
    
    static {
        GO_TO_DESIGN_LIST.add(PartnerLink.class);
        GO_TO_DESIGN_LIST.add(Process.class);
        GO_TO_DESIGN_LIST.add(Sequence.class);
        GO_TO_DESIGN_LIST.add(If.class);
        GO_TO_DESIGN_LIST.add(ElseIf.class);
        GO_TO_DESIGN_LIST.add(Else.class);
        GO_TO_DESIGN_LIST.add(ForEach.class);
        GO_TO_DESIGN_LIST.add(Assign.class);
        GO_TO_DESIGN_LIST.add(Receive.class);
        GO_TO_DESIGN_LIST.add(Reply.class);
        GO_TO_DESIGN_LIST.add(Invoke.class);
        GO_TO_DESIGN_LIST.add(Flow.class);
        GO_TO_DESIGN_LIST.add(While.class);
        GO_TO_DESIGN_LIST.add(RepeatUntil.class);
        GO_TO_DESIGN_LIST.add(Wait.class);
        GO_TO_DESIGN_LIST.add(Exit.class);
        GO_TO_DESIGN_LIST.add(Throw.class);
        GO_TO_DESIGN_LIST.add(ReThrow.class);
        GO_TO_DESIGN_LIST.add(Scope.class);
        GO_TO_DESIGN_LIST.add(Pick.class);
        GO_TO_DESIGN_LIST.add(OnMessage.class);
        GO_TO_DESIGN_LIST.add(OnEvent.class);
        GO_TO_DESIGN_LIST.add(OnAlarmPick.class);
        GO_TO_DESIGN_LIST.add(Empty.class);
        GO_TO_DESIGN_LIST.add(OnAlarmEvent.class);
        GO_TO_DESIGN_LIST.add(CompensationHandler.class);
        GO_TO_DESIGN_LIST.add(TerminationHandler.class);
        GO_TO_DESIGN_LIST.add(Compensate.class);
        GO_TO_DESIGN_LIST.add(CompensateScope.class);
        GO_TO_DESIGN_LIST.add(EventHandlers.class);
        GO_TO_DESIGN_LIST.add(FaultHandlers.class);
        GO_TO_DESIGN_LIST.add(Catch.class);
        GO_TO_DESIGN_LIST.add(CompensatableActivityHolder.class);
    }
}
 