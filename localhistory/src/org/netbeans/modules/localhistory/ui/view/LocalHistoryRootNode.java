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

import java.awt.Image;
import java.beans.BeanInfo;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.Action;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.UIManager;
import org.netbeans.modules.localhistory.LocalHistory;
import org.netbeans.modules.localhistory.store.StoreEntry;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * The toplevel Node in the LocalHistoryView
 *
 * The node hierarchy looks like 
 * <ul> 
 * <li>
 * In case the LocalHistoryView was invoked for a 1 file Node
 * 
 * LocalHistoryViewRootNode ( uvisible root node )
 *  |
 *  +-------- DateFolderNode ( Today ) 
 *              |
 *              +---------- StoreEntryNode ( 29.01.2007 02:15:07 PM ) 
 *              +---------- StoreEntryNode ( 29.01.2007 02:14:59 PM )
 *  
 *  +-------- DateFolderNode ( One day ago )
 *              |
 *              +---------- StoreEntryNode ( 28.01.2007 07:50:59 PM ) 
 *              +---------- StoreEntryNode ( 28.01.2007 03:11:50 PM ) 
 *              +---------- StoreEntryNode ( 28.01.2007 01:15:04 PM )
 *
 * </li>
 * 
 * <li>
 * In case the LocalHistoryView was invoked for a multifile Node
 * 
 * LocalHistoryViewRootNode ( uvisible root node )
 *  |
 *  +-------- DateFolderNode ( Today ) 
 *              |
 *              +---------- StoreEntryNode ( 29.01.2007 02:15:07 PM ) 
 *                              |
 *                              +--------- FileNode ( MyPanel.java )    
 *                              +--------- FileNode ( MyPanel.form )    
 * 
 *              +---------- StoreEntryNode ( 29.01.2007 01:11:23 PM ) 
 *                              |
 *                              +--------- FileNode ( MyPanel.java )    
 *                              +--------- FileNode ( MyPanel.form )     
 *  
 *  +-------- One day ago ( DateFolderNode )
 *  |
 *  +-------- Two days ago ( DateFolderNode )
 * 
 * </li>
 * </ul>
 * 
 * 
 * @author Tomas Stupka
 *
 */
public class LocalHistoryRootNode extends AbstractNode {
    
    static final Action[] NO_ACTION = new Action[0];
    
    private LocalHistoryRootNode(Children children) {
        super(children);
    }
    
    /**
     * 
     * Creates the LocalHistoryViewRootNode with the whole node hierarchy  
     * 
     * @param files files represented by the node on which the LocalHistoryView was invoked - e.g. Main.java, or MyForm.form, MyForm.java, ...
     * @return a node to be applyied as a roo tnode in the LocalHistoryView 
     */
    static Node createRootNode(File[] files) {        
        Children.SortedArray children = new Children.SortedArray();                
        children.add(createDateFolders(files));        
        return new LocalHistoryRootNode(children);        
    }
    
    /**
     * 
     * Creates the DateFolderNodes
     *
     * @param files files represented by the node on which the LocalHistoryView was invoked - e.g. Main.java, or MyForm.form, MyForm.java, ...
     * @return an array of nodes segmenting all entries for the invoked output in separate folders - one for each day
     */
    private static DateFolderNode[] createDateFolders(File[] files) {
        
        // get all StoreEntries for all files and keep them in entriesMap, where 
        // for each timestamp retrieved from the storage there is an array of StoreEntries
        Map<Long, List<StoreEntry>> entriesMap = new HashMap<Long, List<StoreEntry>>();                        
        for (File f : files) {
            StoreEntry[] ses = LocalHistory.getInstance().getLocalHistoryStore().getStoreEntries(f);
            for(StoreEntry e : ses) {
                List<StoreEntry> storeEntries = entriesMap.get(e.getTimestamp());
                if(storeEntries == null) {
                    storeEntries = new ArrayList<StoreEntry>();
                    entriesMap.put(e.getTimestamp(), storeEntries);
                }
                storeEntries.add(e);
            }
        }
                    
        long now = System.currentTimeMillis();
        
        Map<Integer, List<Node>> storeEntryNodesToDays = new HashMap<Integer, List<Node>>();        
        
        // segment the StoreEntries in day groups
        for (Long ts  : entriesMap.keySet()) {          
            int day = (int) ( (now - ts) / (24 * 60 * 60 * 1000) );
                                
            List<Node> nodesFromADay =  storeEntryNodesToDays.get(day);
            if(nodesFromADay == null) {
                nodesFromADay = new ArrayList<Node>();
                 storeEntryNodesToDays.put(day, nodesFromADay);
            }
            List<StoreEntry> storeEntries = entriesMap.get(ts);
            nodesFromADay.add(createStoreEntryNode(storeEntries, files));
        }

        // get a DateFolderNode for each day and the associated group of StoreEntryNode-s
        List<DateFolderNode> dateFolderNodes = new ArrayList<DateFolderNode>( storeEntryNodesToDays.keySet().size());
        for (Iterator<Integer> it =  storeEntryNodesToDays.keySet().iterator(); it.hasNext();) {
            int key = it.next();            
            List<Node> l =  storeEntryNodesToDays.get(key);
            Children.SortedArray children = new Children.SortedArray();            
            children.add(l.toArray(new Node[l.size()]));
            
            dateFolderNodes.add(new DateFolderNode(key, children));
        }

        return dateFolderNodes.toArray(new DateFolderNode[dateFolderNodes.size()]);
    }                
    
    /**
     * 
     * Creates a StoreEntryNode for a list of files, where the files are related to one DataObject - e.g. MyForm.java, MyForm.form
     * 
     */
    private static Node createStoreEntryNode(List<StoreEntry> entries, File[] files) {
        if(files.length == 1) {
            
            // it's only 1 file, so we also already have the 1 StoreEntry
            return StoreEntryNode.create(entries);
            
        } else {            
            // it's a multifile node ...             
            
            // the timestamp must be the same for all StoreEntries
            long ts = entries.get(0).getTimestamp();           
            
            // get the entries for every file - 
            // if there is no entry in the Storage then create a structural (fake) one 
            List<StoreEntry> entriesList = new ArrayList<StoreEntry>();            
            for(File f : files) {                
                boolean fileInEntries = false;
                // check if we already have an entry for the file
                for(StoreEntry se : entries) {
                    if(f.equals(se.getFile())) {
                        entriesList.add(se);
                        fileInEntries = true;
                        break;
                    }
                }
                if(fileInEntries) {
                    // continue if we already have an entry for the file 
                    continue;
                }
                                
                // if there was no entry for the the file then try to get it ...
                StoreEntry e = LocalHistory.getInstance().getLocalHistoryStore().getStoreEntry(f, ts);
                if(e != null) {
                    
                    // XXX we probably don't have to do this anymore - see in createDateFolders( ... )
                    
                    // ... either by retrieving them from the storage
                    entriesList.add(e);
                } else {
                    // ... or by creating a structural (fake) one
                    entriesList.add(StoreEntry.createFakeStoreEntry(f, ts));
                }                
            }            
            return StoreEntryNode.create(entriesList);            
        }
    }                                     

    public String getName() {
        return "rootnode"; // NOI18N
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage(LocalHistoryRootNode.class, "LBL_LocalHistory_Column_Version"); // NOI18N
    }            
        
    public Action[] getActions(boolean context) {
        return NO_ACTION;
    }
        
    static class DateFolderNode extends AbstractNode implements Comparable {
        private final int day;

        DateFolderNode(int day, Children children) {
            super(children);                        
            this.day = day;
        }

        int getDay() {
            return day;
        }

        public Image getIcon(int type) {
            Image img = null;
            if (type == BeanInfo.ICON_COLOR_16x16) {
                img = (Image) UIManager.get("Nb.Explorer.Folder.icon");  // NOI18N
            }
            if (img == null) {
                img = super.getIcon(type);
            }
            return img;
        }

        public Image getOpenedIcon(int type) {
            Image img = null;
            if (type == BeanInfo.ICON_COLOR_16x16) {
                img = (Image) UIManager.get("Nb.Explorer.Folder.openedIcon");  // NOI18N
            }
            if (img == null) {
                img = super.getIcon(type);
            }
            return img;
        }      

        public Action[] getActions(boolean context) {
            return NO_ACTION;
        }

        public String getName() {
            switch (day) {
                case 0: return NbBundle.getMessage(LocalHistoryRootNode.class, "DateFolderName_0");                
                case 1: return NbBundle.getMessage(LocalHistoryRootNode.class, "DateFolderName_1");                
                case 2: return NbBundle.getMessage(LocalHistoryRootNode.class, "DateFolderName_2");                
                case 3: return NbBundle.getMessage(LocalHistoryRootNode.class, "DateFolderName_3");                
                case 4: return NbBundle.getMessage(LocalHistoryRootNode.class, "DateFolderName_4");                
                case 5: return NbBundle.getMessage(LocalHistoryRootNode.class, "DateFolderName_5");                
                case 6: return NbBundle.getMessage(LocalHistoryRootNode.class, "DateFolderName_6");                                             
            }
            return NbBundle.getMessage(LocalHistoryRootNode.class, "DateFolderName_other");                
        }

        public int compareTo(Object obj) {
            if( !(obj instanceof DateFolderNode) || obj == null) {
                return -1;
            }
            DateFolderNode lhNode = (DateFolderNode) obj;        

            if(lhNode.getDay() > getDay()) {
                return -1;
            } else if(lhNode.getDay() < getDay()) {
                return 1;
            } else {
                return 0;
            }                    
        }
    }    
}

