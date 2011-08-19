/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.debugger.jpda.visual.models;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JList;
import javax.swing.JScrollPane;
import org.netbeans.modules.debugger.jpda.visual.RemoteAWTScreenshot.AWTComponentInfo;
import org.netbeans.modules.debugger.jpda.visual.RemoteServices;
import org.netbeans.modules.debugger.jpda.visual.RemoteServices.RemoteListener;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.visual.ComponentInfo;
import org.netbeans.spi.debugger.visual.ScreenshotUIManager;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;

/**
 *
 * @author Martin Entlicher
 */
@DebuggerServiceRegistration(path="netbeans-JPDASession/EventsView", types={ TreeModel.class, NodeModel.class, NodeActionsProvider.class })
public class EventsModel implements TreeModel, NodeModel, NodeActionsProvider {
    
    private static final String attachedListeners = "attachedListeners"; // NOI18N
    private static final String eventsLog = "eventsLog"; // NOI18N
    
    private Set<ModelListener> listeners = new CopyOnWriteArraySet<ModelListener>();
    
    private AWTComponentInfo selectedCI = null;
    private final List<RemoteEvent> events = new ArrayList<RemoteEvent>();
    
    public EventsModel() {
        ScreenshotUIManager uiManager = ScreenshotUIManager.getActive();
        if (uiManager != null) {
            ComponentInfo ci = uiManager.getSelectedComponent();
            if (ci instanceof AWTComponentInfo) {
                selectedCI = (AWTComponentInfo) ci;
            }
        }
        /*Node[] nodes = ComponentHierarchy.getInstance().getExplorerManager().getSelectedNodes();
        if (nodes.length > 0) {
            selectedCI = nodes[0].getLookup().lookup(AWTComponentInfo.class);
        }*/
        final Result<Node> nodeLookupResult = Utilities.actionsGlobalContext().lookupResult(Node.class);
        LookupListener ll = new LookupListener() {
            @Override
            public void resultChanged(LookupEvent ev) {
                Collection<? extends Node> nodeInstances = nodeLookupResult.allInstances();
                for (Node n : nodeInstances) {
                    AWTComponentInfo ci = n.getLookup().lookup(AWTComponentInfo.class);
                    if (ci != null) {
                        if (!ci.equals(selectedCI)) {
                            selectedCI = ci;
                            if (ev != null) {
                                fireModelChanged();
                            }
                        }
                        break;
                    }
                }
            }
        };
        nodeLookupResult.addLookupListener(ll);
        ll.resultChanged(null); // To initialize
    }
    
    @Override
    public Object getRoot() {
        return ROOT;
    }

    @Override
    public Object[] getChildren(Object parent, int from, int to) throws UnknownTypeException {
        if (parent == ROOT) {
            return new Object[] { attachedListeners, eventsLog };
        }
        if (parent == attachedListeners) {
            AWTComponentInfo ci = selectedCI;
            if (ci != null) {
                //ObjectReference component = ci.getComponent();
                List<RemoteListener> componentListeners;
                try {
                    componentListeners = RemoteServices.getAttachedListeners(ci);
                } catch (PropertyVetoException pvex) {
                    Exceptions.printStackTrace(pvex);
                    return new Object[] {};
                }
                Map<String, ListenerCategory> listenerCategories = new TreeMap<String, ListenerCategory>();
                for (RemoteListener rl : componentListeners) {
                    String type = rl.getType();
                    ListenerCategory lc = listenerCategories.get(type);
                    if (lc == null) {
                        lc = new ListenerCategory(type);
                        listenerCategories.put(type, lc);
                    }
                    lc.addListener(rl);
                }
                return listenerCategories.values().toArray();
            }
        }
        if (parent instanceof ListenerCategory) {
            return ((ListenerCategory) parent).getListeners().toArray();
        }
        if (parent == eventsLog) {
            synchronized (events) {
                return events.toArray();
            }
        }
        if (parent instanceof RemoteEvent) {
            return ((RemoteEvent) parent).getProperties();
        }
        return new Object[] {};
    }

    @Override
    public boolean isLeaf(Object node) throws UnknownTypeException {
        if (node == ROOT || node == attachedListeners || node == eventsLog) {
            return false;
        }
        if (node instanceof RemoteListener) {
            return true;
        }
        if (node instanceof String) {
            return true;
        }
        return false;
    }

    @Override
    public int getChildrenCount(Object node) throws UnknownTypeException {
        return Integer.MAX_VALUE;
    }

    @Override
    public void addModelListener(ModelListener l) {
        listeners.add(l);
    }

    @Override
    public void removeModelListener(ModelListener l) {
        listeners.remove(l);
    }
    
    private void fireModelChanged() {
        ModelEvent me = new ModelEvent.TreeChanged(this);
        for (ModelListener l : listeners) {
            l.modelChanged(me);
        }
    }
    
    private void fireNodeChanged(Object node) {
        ModelEvent me = new ModelEvent.NodeChanged(this, node);
        for (ModelListener l : listeners) {
            l.modelChanged(me);
        }
    }

    @Override
    public String getDisplayName(Object node) throws UnknownTypeException {
        if (node == ROOT) {
            return "Events";
        }
        if (node == attachedListeners) {
            return "Listeners";
        }
        if (node == eventsLog) {
            return "Event Log";
        }
        if (node instanceof ListenerCategory) {
            return ((ListenerCategory) node).getType();
        }
        if (node instanceof RemoteListener) {
            return ((RemoteListener) node).getListener().referenceType().name();
        }
        if (node instanceof RemoteEvent) {
            RemoteEvent re = (RemoteEvent) node;
            String toString = re.getEventToString();
            int end = toString.indexOf('[');
            if (end < 0) end = toString.length();
            return re.getListenerMethod()+" ("+toString.substring(0, end)+')';
        }
        return String.valueOf(node);
    }

    @Override
    public String getIconBase(Object node) throws UnknownTypeException {
        return null;
    }

    @Override
    public String getShortDescription(Object node) throws UnknownTypeException {
        if (node instanceof RemoteListener) {
            try {
                return ((RemoteListener) node).getListener().referenceType().sourceName();
            } catch (AbsentInformationException ex) {
                return "";
            }
        }
        if (node instanceof RemoteEvent) {
            RemoteEvent re = (RemoteEvent) node;
            return re.getEventToString();
        }
        return "";
    }

    @Override
    public void performDefaultAction(Object node) throws UnknownTypeException {
    }

    @Override
    public Action[] getActions(Object node) throws UnknownTypeException {
        if (selectedCI != null) {
            if (node == attachedListeners) {
                return new Action[] { new AddLoggingListenerAction(null) };
            }
            if (node instanceof ListenerCategory) {
                return new Action[] { new AddLoggingListenerAction((ListenerCategory) node) };
            }
        }
        return new Action[] {};
    }
    
    private static class ListenerCategory {
        
        private String type;
        private List<RemoteListener> listeners = new ArrayList<RemoteListener>();
        
        public ListenerCategory(String type) {
            this.type = type;
        }
        
        public String getType() {
            return type;
        }
        
        public void addListener(RemoteListener l) {
            listeners.add(l);
        }
        
        public List<RemoteListener> getListeners() {
            return listeners;
        }
    }
    
    private class AddLoggingListenerAction extends AbstractAction {
        
        private ListenerCategory lc;
        
        public AddLoggingListenerAction(ListenerCategory lc) {
            this.lc = lc;
        }

        @Override
        public Object getValue(String key) {
            if (Action.NAME.equals(key)) {
                return "Add Logging Listener" + ((lc == null) ? "..." : "" );
            }
            return super.getValue(key);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            final AWTComponentInfo ci = selectedCI;
            if (ci == null) return ;
            String listenerClass;
            if (lc != null) {
                listenerClass = lc.getType();
            } else {
                listenerClass = selectListenerClass(ci);
                if (listenerClass == null) {
                    return;
                }
            }
            final ReferenceType rt = getReferenceType(ci.getComponent().virtualMachine(), listenerClass);
            if (rt == null) {
                System.err.println("No class "+listenerClass);
                return ;
            }
            ci.getThread().getDebugger().getRequestProcessor().post(new Runnable() {
                @Override
                public void run() {
                    ObjectReference l;
                    try {
                        l = RemoteServices.attachLoggingListener(ci, rt.classObject(), new LoggingEventListener());
                    } catch (PropertyVetoException pvex) {
                        Exceptions.printStackTrace(pvex);
                        return ;
                    }
                    if (l != null) {
                        if (lc != null) {
                            lc.addListener(new RemoteListener(l.referenceType().name(), l));
                            fireNodeChanged(lc);
                        } else {
                            fireNodeChanged(attachedListeners);
                        }
                    }
                }
            });
            
        }
        
        private ReferenceType getReferenceType(VirtualMachine vm, String name) {
            List<ReferenceType> classList = vm.classesByName(name);
            ReferenceType clazz = null;
            for (ReferenceType c : classList) {
                clazz = c;
                if (c.classLoader() == null) {
                    break;
                }
            }
            return clazz;
        }
        
        private String selectListenerClass(AWTComponentInfo ci) {
            List<ReferenceType> attachableListeners = RemoteServices.getAttachableListeners(ci);
            System.err.println("Attachable Listeners = "+attachableListeners);
            String[] listData = new String[attachableListeners.size()];
            for (int i = 0; i < listData.length; i++) {
                listData[i] = attachableListeners.get(i).name();
            }
            JList jl = new JList(listData);
            JScrollPane jsp = new JScrollPane(jl);
            NotifyDescriptor nd = new DialogDescriptor(jsp, "Select Listener", true, null);
            Object res = DialogDisplayer.getDefault().notify(nd);
            if (DialogDescriptor.OK_OPTION.equals(res)) {
                String clazz = (String) jl.getSelectedValue();
                return clazz;
            } else {
                return null;
            }
        }

    }
    
    private class LoggingEventListener implements RemoteServices.LoggingListenerCallBack {

        @Override
        public void eventsData(AWTComponentInfo ci, String[] data) {
            RemoteEvent re = new RemoteEvent(data);
            /*
            System.err.println("Have data about "+ci.getType()+":");//\n  "+Arrays.toString(data));
            System.err.println("  Method: "+data[0]+", event toString() = "+data[1]);
            for (int j = 2; j < data.length; j += 2) {
                System.err.println("    "+data[j]+" = "+data[j+1]);
            }
             */
            synchronized (events) {
                events.add(re);
            }
            fireNodeChanged(eventsLog);
        }
        
    }
    
    private class RemoteEvent {
        
        private String[] data;
        
        public RemoteEvent(String[] data) {
            this.data = data;
        }
        
        public String getListenerMethod() {
            return data[0];
        }
        
        public String getEventToString() {
            return data[1];
        }
        
        public String[] getProperties() {
            String[] properties = new String[data.length/2 - 1];
            for (int i = 0; i < properties.length; i++) {
                properties[i] = data[2 + 2*i] + " = "+data[3 + 2*i];
            }
            return properties;
        }
        
    }
    
}
