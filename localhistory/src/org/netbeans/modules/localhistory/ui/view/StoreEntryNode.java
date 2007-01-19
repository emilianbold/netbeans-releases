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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.localhistory.ui.view;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport; 
import java.util.ArrayList;
import javax.swing.Action;
import org.netbeans.modules.localhistory.ui.actions.DeleteAction;
import org.netbeans.modules.localhistory.ui.actions.RollbackAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.util.List;
import org.netbeans.modules.localhistory.LocalHistory;
import org.netbeans.modules.localhistory.store.StoreEntry;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Tomas Stupka
 *
 */
public class StoreEntryNode extends AbstractNode implements Comparable {
    
    static final String PROPERTY_NAME_LABEL = "label";     // NOI18N        
    private List<StoreEntry> entries;
    private static DateFormat defaultFormat = DateFormat.getDateTimeInstance();                      

    private StoreEntryNode(List<StoreEntry> childrenEntries) {        
        super(createChildren(childrenEntries));                        
        this.entries = childrenEntries;
        initProperties();
    }        

    private StoreEntryNode(List<StoreEntry> childrenEntries, Lookup l) {        
        super(Children.LEAF, l);                        
        this.entries = childrenEntries;
        initProperties();
    }        

    static StoreEntryNode create(List<StoreEntry> childrenEntries) {
        
        assert childrenEntries != null && childrenEntries.size() > 0;
        
        if(childrenEntries.size() > 1) {
            return new StoreEntryNode(childrenEntries);
        } else {
            return new StoreEntryNode(childrenEntries, Lookups.fixed(new Object [] { childrenEntries.get(0) }));
        }
    }
    
    private static Children createChildren(List<StoreEntry> childrenEntries) {
        FileNode[] nodes = new FileNode[childrenEntries.size()];
        int i = 0;
        for (StoreEntry se : childrenEntries) {
            nodes[i++] = new FileNode(se);            
        }
        Children.SortedArray children = new Children.SortedArray();            
        children.add(nodes);
        return children;        
    }
        
    private void initProperties() {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set ps = Sheet.createPropertiesSet();
                
        ps.put(new LabelProperty());
        
        sheet.put(ps);
        setSheet(sheet);        
    }   
    
    public String getDisplayName() {
        return getName();
    }

    public String getName() {                
        return getFormatedDate(entries.get(0));
    }    
       
    static String getFormatedDate(StoreEntry se)  {
        return defaultFormat.format(se.getDate());
    }
    
    public Action[] getActions(boolean context) {
        return new Action[] {
            SystemAction.get(RollbackAction.class),
            SystemAction.get(DeleteAction.class)     
        };            
    }

    public int compareTo(Object obj) {
        if( !(obj instanceof StoreEntryNode) || obj == null) {
            return 1;
        }
        StoreEntryNode node = (StoreEntryNode) obj;

        if(node.entries.get(0).getTimestamp() > entries.get(0).getTimestamp()) {
            return 1;
        } else if(node.entries.get(0).getTimestamp() < entries.get(0).getTimestamp()) {
            return -1;
        } else {
            return 0;
        }                            
    }
                          
    private class LabelProperty extends PropertySupport.ReadWrite<String> {
        public LabelProperty() {
            super(PROPERTY_NAME_LABEL, String.class, NbBundle.getMessage(StoreEntryNode.class, "LBL_LabelProperty_Name"), NbBundle.getMessage(StoreEntryNode.class, "LBL_LabelProperty_Desc"));
        }
        public String getValue() throws IllegalAccessException, InvocationTargetException {
            return entries.get(0).getLabel();
        }    
        public void setValue(String value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException 
        {        
            List<StoreEntry> newEntries = new ArrayList<StoreEntry>(entries.size());
            for(StoreEntry se : entries) {
                LocalHistory.getInstance().getLocalHistoryStore().setLabel(se.getFile(), se.getTimestamp(), value);    
                newEntries.add(new StoreEntry(se.getFile(), se.getStoreFile(), se.getTimestamp(), value));
            }            
            entries = newEntries;
        }        
        public PropertyEditor getPropertyEditor() {
            return new PropertyEditorSupport();
        }                             
    }                      
    
    private static class FileNode extends AbstractNode implements Comparable {        

        private final StoreEntry entry;
        
        FileNode(StoreEntry entry) {
            super(Children.LEAF, Lookups.fixed(new Object [] { entry }));                        
            this.entry = entry;
        }
    
        public Action[] getActions(boolean context) {
            List<StoreEntry> entries = new ArrayList<StoreEntry>(1);
            entries.add(entry);
            return new Action[] {
                SystemAction.get(RollbackAction.class),
                SystemAction.get(DeleteAction.class)
            };            
        }

        public String getName() {
            return entry.getFile().getName();
        }  
        
        public int compareTo(Object obj) {
            if( !(obj instanceof FileNode) || obj == null) {
                return -1;
            }
            FileNode node = (FileNode) obj;        
            return getName().compareTo(node.getName());            
        }        
    }    
    
}

