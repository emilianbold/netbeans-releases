/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.versioning.ui.history;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.util.*;
import javax.swing.Action;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.*;
import org.openide.util.NbBundle;

/**
 *
 * The toplevel Node in the LocalHistoryView
 * 
 * @author Tomas Stupka
 *
 */
public class HistoryRootNode extends AbstractNode {
    
    static final String NODE_GET_MORE   = "getmoreno"; // NOI18N
    static final String NODE_WAIT       = "waitnode";  // NOI18N
    static final String NODE_ROOT       = "rootnode";  // NOI18N
    
    static final Action[] NO_ACTION = new Action[0];
    
    private static DateFormat dateFormat = DateFormat.getDateInstance();
    
    private Map<Long, HistoryEntry> revisionEntries = new HashMap<Long, HistoryEntry>();
    private final File[] files;
    
    private LoadNextNode loadNextNode;
    private WaitNode waitNode;
        
    private final String vcsName;
    private int vcsCount = 0;
    private final Action loadNextAction;
    private final Action[] actions;
        
    HistoryRootNode(File[] files, String vcsName, Action loadNextAction, Action... actions) {
        super(new Children.SortedArray());
        this.files = files;
        this.vcsName = vcsName;
        this.loadNextAction = loadNextAction;
        this.actions = actions;
        if(vcsName != null) {
            waitNode = new WaitNode(vcsName);
            getChildren().add(new Node[] {waitNode}); 
        }
    }
    
    static boolean isLoadNext(Object n) {
        return n instanceof HistoryRootNode.LoadNextNode;
        }
                    
    synchronized void addLHEntries(HistoryEntry[] entries) {
        addEntries(entries, false);
    }
        
    synchronized void addVCSEntries(HistoryEntry[] entries) {
        addEntries(entries, true);
    }
        
    private void addEntries(HistoryEntry[] entries, boolean vcs) {
        // remove previous
        Children children = getChildren();
        List<Node> toRemove = new LinkedList<Node>();
        Node[] nodes = children.getNodes();
        for (Node node : nodes) {
            HistoryEntry he = node.getLookup().lookup(HistoryEntry.class);
            if(he != null && he.isLocalHistory() == !vcs) {
                toRemove.add(node);
            } 
        }
        children.remove(toRemove.toArray(new Node[toRemove.size()]));
        
        // add new
        for (HistoryEntry e : entries) {
            if(!revisionEntries.containsKey(e.getDateTime().getTime())) {
                revisionEntries.put(e.getDateTime().getTime(), e);
                if(vcs) {
                    vcsCount++;
                }
            } 
        }
        if(loadNextNode != null) {
            loadNextNode.refreshMessage();
        }
        nodes = new Node[entries.length];
        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = RevisionNode.create(entries[i]);
        }
        getChildren().add(nodes);
    }

    synchronized void loadingStarted() {
        Children children = getChildren();
        if(loadNextNode != null) {
            children.remove(new Node[] { loadNextNode });
        }
        if(waitNode != null) {
            children.remove(new Node[] { waitNode });
        }
        waitNode = new WaitNode();
        children.add(new Node[] { waitNode });
    }

    synchronized void loadingFinished(Date dateFrom) {
        Children children = getChildren();
        if(waitNode != null) {
            children.remove(new Node[] { waitNode });
    }                
        if(loadNextNode != null) {
            children.remove(new Node[] { loadNextNode });
        }
        if(!HistorySettings.getInstance().getLoadAll()) {
            loadNextNode = new LoadNextNode(dateFrom);
            children.add(new Node[] {loadNextNode});
        }
    }
    
    public String getName() {
        return NODE_ROOT; 
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage(HistoryRootNode.class, "LBL_LocalHistory_Column_Version"); // NOI18N
    }            
        
    public Action[] getActions(boolean context) {
        return NO_ACTION;
    }
        
    synchronized void refreshLoadNextName() {
        if(loadNextNode != null) {
            loadNextNode.nameChanged();
        }
    }

    class LoadNextNode extends AbstractNode implements Comparable<Node> {

        LoadNextNode(Date dateFrom) {
            super(new Children.SortedArray());
            
            Sheet sheet = Sheet.createDefault();
            Sheet.Set ps = Sheet.createPropertiesSet();
            ps.put(new MessageProperty(dateFrom)); 
            sheet.put(ps);
            setSheet(sheet);        
        }

        @Override
        public Action getPreferredAction() {
            return loadNextAction;
        }

        @Override
        public Action[] getActions(boolean context) {
            if(!HistorySettings.getInstance().getLoadAll()) {
                return actions;
            }
            return new Action[0];
            }
        
        @Override
        public String getDisplayName() {
            return (String) loadNextAction.getValue(Action.NAME);  
        }

        @Override
        public int compareTo(Node n) {
            return 1;
            }

        @Override
        public String getName() {
            return NODE_GET_MORE;
            }

        private void refreshMessage() {
            firePropertyChange(RevisionNode.PROPERTY_NAME_LABEL, null, null);
        }      

        private void nameChanged() {
            fireDisplayNameChange(null, null);
        }

        class MessageProperty extends PropertySupport.ReadOnly<String> {
            private final Date dateFrom;
            public MessageProperty(Date dateFrom) {
                super(RevisionNode.PROPERTY_NAME_LABEL, String.class, NbBundle.getMessage(RevisionNode.class, "LBL_LabelProperty_Name"), NbBundle.getMessage(RevisionNode.class, "LBL_LabelProperty_Desc"));
                this.dateFrom = dateFrom;
            }
            @Override
            public String getValue() throws IllegalAccessException, InvocationTargetException {
                if(dateFrom != null) {
                    return "Shoving " + vcsName + " revisions from " + dateFormat.format(dateFrom) + " (" + vcsCount + " entries)."; 
                } else {
                    return "Shoving all " + vcsName + " revisions."; 
        }
            }    
            @Override
            public PropertyEditor getPropertyEditor() {
                return new PropertyEditorSupport();
            }                             
        }
    }

    static class WaitNode extends AbstractNode implements Comparable<Node> {
        public WaitNode() {
            this(null);
            }
        public WaitNode(String vcsName) {
            super(Children.LEAF);
            setDisplayName("Loading" + (vcsName != null ? " from " + vcsName : "") + ". Please wait...");
            setIconBaseWithExtension("org/netbeans/modules/versioning/ui/resources/icons/wait.gif");  // NOI18N
        }

        @Override
        public int compareTo(Node n) {
                return 1;
            }                    

        @Override
        public String getName() {
            return NODE_WAIT;
        }
    }    
}

