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
package org.netbeans.modules.localhistory.ui.view;

import java.awt.Image;
import java.beans.BeanInfo;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
            for(StoreEntry se : ses) {
                List<StoreEntry> storeEntries = entriesMap.get(se.getTimestamp());
                if(storeEntries == null) {
                    storeEntries = new ArrayList<StoreEntry>();
                    entriesMap.put(se.getTimestamp(), storeEntries);
                }
                storeEntries.add(se);
            }
        }
                    
        Map<Integer, List<Node>> storeEntryNodesToDays = new HashMap<Integer, List<Node>>();        
        
        // segment the StoreEntries in day groups
        for (Long ts  : entriesMap.keySet()) {                      
            int day = getDay(ts);                                                        
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

