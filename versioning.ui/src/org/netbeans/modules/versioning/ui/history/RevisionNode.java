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

import javax.swing.Action;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.util.*;
import org.netbeans.swing.etable.QuickFilter;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Tomas Stupka
 *
 */
public class RevisionNode extends AbstractNode implements Comparable {
    
    static final String PROPERTY_NAME_LABEL = "label";                          // NOI18N        
    static final String PROPERTY_NAME_USER = "user";                            // NOI18N        
    static final String PROPERTY_NAME_VERSION = "version";                      // NOI18N                       

    private List<HistoryEntry> entries;
    private static DateFormat dateFormat = DateFormat.getDateTimeInstance();                      
    private static DateFormat timeFormat = DateFormat.getTimeInstance();

    private RevisionNode(List<HistoryEntry> childrenEntries) { 
        super(createChildren(childrenEntries));                        
        this.entries = childrenEntries;
        initProperties();
    }        

    private RevisionNode(List<HistoryEntry> childrenEntries, Lookup l) {                
        super(Children.LEAF, l);                        
        this.entries = childrenEntries;
        initProperties();
    }        

    static RevisionNode create(List<HistoryEntry> childrenEntries) {
        return create(childrenEntries, false);
    }
         
    static RevisionNode create(List<HistoryEntry> childrenEntries, boolean forceMultifile) {
        
        assert childrenEntries != null && childrenEntries.size() > 0;
        
        if(childrenEntries.size() > 1 || forceMultifile) {
            // set siblings for every entry
//            for (HistoryEntry entry : childrenEntries) {
//                entry.setSiblings(childrenEntries);
//            }
            return new RevisionNode(childrenEntries);
        } else {
            return new RevisionNode(childrenEntries, Lookups.fixed(new Object [] { childrenEntries.get(0) }));
        }
    }
    
    private static Children createChildren(List<HistoryEntry> childrenEntries) {
        FileNode[] nodes = new FileNode[childrenEntries.size()];
        int i = 0;
        for (HistoryEntry se : childrenEntries) {
            nodes[i++] = new FileNode(se);            
        }
        Children.SortedArray children = new Children.SortedArray();            
        children.add(nodes);
        return children;        
    }
        
    private void initProperties() {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set ps = Sheet.createPropertiesSet();
                
        ps.put(new RevisionProperty()); // XXX show only if VCS available
        ps.put(new UserProperty()); 
//        XXX
//        ps.put(entries.get(0).isLocalHistory() ? new EditableMessageProperty() : new MessageProperty());
        ps.put(new MessageProperty());
        
        sheet.put(ps);
        setSheet(sheet);        
    }   
    
    @Override
    public String getDisplayName() {
        return getName();
    }

    @Override
    public String getName() {                
        return getFormatedDate(entries.get(0));
    }    
       
    static String getFormatedDate(HistoryEntry se)  {
        int day = getDay(se.getDateTime().getTime());
        switch(day) {
            case 0:  return NbBundle.getMessage(RevisionNode.class, "LBL_Today", new Object[] {timeFormat.format(se.getDateTime())});   
            case 1:  return NbBundle.getMessage(RevisionNode.class, "LBL_Yesterday", new Object[] {timeFormat.format(se.getDateTime())});
            default: return dateFormat.format(se.getDateTime());
        }
    }
    
    private static int getDay(long ts) {
        Date date = new Date(ts);
                
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());        
        
        // set the cal at today midnight
        int todayMillis = c.get(Calendar.HOUR_OF_DAY) * 60 * 60 * 1000 +
                          c.get(Calendar.MINUTE)      * 60 * 1000 + 
                          c.get(Calendar.SECOND)      * 1000 + 
                          c.get(Calendar.MILLISECOND);                
        c.add(Calendar.MILLISECOND, -1 * todayMillis);                        
        
        if(c.getTime().compareTo(date) < 0) {
            return 0;
        }
        
        return (int) ( (c.getTimeInMillis() - ts) / (24 * 60 * 60 * 1000) ) + 1;
                
    }    
    @Override
    public Action[] getActions(boolean context) {
        return entries.get(0).getActions();          
    }

    @Override
    public int compareTo(Object obj) {
        if( !(obj instanceof RevisionNode) || obj == null) {
            return -1;
        }
        RevisionNode node = (RevisionNode) obj;

        if(node.entries.get(0).getDateTime().getTime() > entries.get(0).getDateTime().getTime()) {
            return 1;
        } else if(node.entries.get(0).getDateTime().getTime() < entries.get(0).getDateTime().getTime()) {
            return -1;
        } else {
            return 0;
        }                            
    }
    
    class MessageProperty extends PropertySupport.ReadOnly<String> {
        public MessageProperty() {
            super(PROPERTY_NAME_LABEL, String.class, NbBundle.getMessage(RevisionNode.class, "LBL_LabelProperty_Name"), NbBundle.getMessage(RevisionNode.class, "LBL_LabelProperty_Desc"));
        }
        @Override
        public String getValue() throws IllegalAccessException, InvocationTargetException {
            return entries.get(0).getMessage();
        }

        @Override
        public String toString() {
            return entries.get(0).getMessage();
        }
        
    }
    
//    class EditableMessageProperty extends PropertySupport.ReadWrite<String> {
//        public EditableMessageProperty() {
//            super(PROPERTY_NAME_LABEL, String.class, NbBundle.getMessage(RevisionNode.class, "LBL_LabelProperty_Name"), NbBundle.getMessage(RevisionNode.class, "LBL_LabelProperty_Desc"));
//        }
//        @Override
//        public String getValue() throws IllegalAccessException, InvocationTargetException {
//            return entries.get(0).getMessage();
//        }    
//        @Override        
//        public void setValue(String value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException 
//        {        
//            value = value.trim();
//            List<HistoryEntry> newEntries = new ArrayList<HistoryEntry>(entries.size());
//            for(HistoryEntry se : entries) {
//                newEntries.add(HistoryEntry.createHistoryEntry(History.getInstance().getLocalHistoryStore().setLabel(se.getFile(), se.getTimestamp(), !value.equals("") ? value : null)));
//            }            
//            entries = newEntries;
//        }        
//        @Override
//        public PropertyEditor getPropertyEditor() {
//            return new PropertyEditorSupport();
//        }           
//        
//        @Override
//        public String toString() {
//            return entries.get(0).getMessage();
//        }        
//    }                      
    
    class UserProperty extends PropertySupport.ReadOnly<String> {
        public UserProperty() {
            super(PROPERTY_NAME_USER, String.class, NbBundle.getMessage(RevisionNode.class, "LBL_UserProperty_Name"), NbBundle.getMessage(RevisionNode.class, "LBL_UserProperty_Desc"));
        }
        @Override
        public String getValue() throws IllegalAccessException, InvocationTargetException {
            return entries.get(0).getUsernameShort();
        }

        @Override
        public String toString() {
            return entries.get(0).getUsername();
        }
    }        
    
    class RevisionProperty extends PropertySupport.ReadOnly<String> {
        public RevisionProperty() {
            super(PROPERTY_NAME_VERSION, String.class, NbBundle.getMessage(RevisionNode.class, "LBL_VersionProperty_Name"), NbBundle.getMessage(RevisionNode.class, "LBL_VersionProperty_Desc"));
        }
        @Override
        public String getValue() throws IllegalAccessException, InvocationTargetException {
            return entries.get(0).getRevisionShort();
        }

        @Override
        public String toString() {
            return entries.get(0).getRevision();
        }
    } 

    private static class FileNode extends AbstractNode implements Comparable {        

        private final HistoryEntry entry;
        
        FileNode(HistoryEntry entry) {
            super(Children.LEAF, Lookups.fixed(new Object [] { entry }));                        
            this.entry = entry;
        }
    
        @Override
        public Action[] getActions(boolean context) {
            return entry.getActions();       
        }

        @Override
        public String getName() {
            return entry.getFiles()[0].getName(); // XXX 
        }  
        
        @Override
        public int compareTo(Object obj) {
            if( !(obj instanceof FileNode) || obj == null) {
                return -1;
            }
            FileNode node = (FileNode) obj;        
            return getName().compareTo(node.getName());            
        }        
    }    
    
    public static abstract class Filter implements QuickFilter {
        public abstract String getDisplayName();
        protected Collection<HistoryEntry> getEntries(Object value) {
            if(value instanceof Node) {
                return getEntries((Node)value);
        }
            return null;
        }
 
        private Collection<HistoryEntry> getEntries(Node node) {
            if(node instanceof RevisionNode) {
                return ((RevisionNode)node).entries;
            } else if (node instanceof FileNode) {
                return Arrays.asList(new HistoryEntry[] {((FileNode)node).entry});
            } else {
                Node[] nodes = node.getChildren().getNodes();
                List<HistoryEntry> ret = new LinkedList<HistoryEntry>();
                for (Node n : nodes) {
                    ret.addAll(getEntries(n));
                }
                return ret;
            }
        }
        
        public String getRendererValue(String value) {
            return value;
        }
    }

}

