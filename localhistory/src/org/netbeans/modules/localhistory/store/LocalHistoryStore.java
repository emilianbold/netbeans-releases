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
package org.netbeans.modules.localhistory.store;

import java.beans.PropertyChangeListener;
import java.io.File;

/**
 *
 * @author Tomas Stupka
 */
// XXX what about multifile dataobjects ?
public interface LocalHistoryStore {
    
    
    /**
     * 
     */
    public String PROPERTY_CHANGED = "LocalHistoryStore.changed";               // NOI18N  
    
    
    // events
    
    /**
     * 
     * @param file 
     * @param ts 
     */
    public void fileCreate(File file, long ts);        
    
    /**
     * 
     * @param file 
     * @param ts 
     */
    public void fileDelete(File file, long ts);
    
    // XXX merge fileCreateFromMove and fileDeleteFromMove into one call
    /**
     * 
     * @param from 
     * @param to 
     * @param ts 
     */
    public void fileCreateFromMove(File from, File to, long ts);
    
    /**
     * 
     * @param from 
     * @param to 
     * @param ts 
     */
    public void fileDeleteFromMove(File from, File to, long ts);
 
    /**
     * 
     * @param file 
     * @param ts 
     */
    public void fileChange(File file, long ts);                   
    
    
    // edit 
    
    /**
     * 
     * 
     * @param file 
     * @param ts 
     * @param label 
     */ 
    public void setLabel(File file, long ts, String label);    
    
    // listeners 
    
    /**
     * 
     * Adds a property change listener
     * 
     * @param l the property change listener
     */
    public void addPropertyChangeListener(PropertyChangeListener l);
    
    /**
     * 
     * Removes a property change listener
     * 
     * @param l the property change listener
     */
    public void removePropertyChangeListener(PropertyChangeListener l);
    
    
    // data
    
    /**
     * Returns all entries for a file
     * 
     * @param file the file for which the entries are to be retrieved
     * @return StoreEntry[] all entries present in the storage
     */ 
    public StoreEntry[] getStoreEntries(File file);
    
    /**
     * Returns an entry representig the given files state in time ts
     * 
     * @param file the file for which the entries are to be retrieved
     * @param ts the time for which the StoreEntry has to retrieved
     * @return StoreEntry a StoreEntry representing the given file in time ts. 
     *         <tt>null</tt> if file is a directory or there is no entry with a timestamp &lt; <tt>ts</tt>
     */ 
    public StoreEntry getStoreEntry(File file, long ts);
    
    /**
     * 
     * 
     * @param root 
     * @param files 
     * @param ts 
     * @return 
     */ 
    public StoreEntry[] getFolderState(File root, File[] files, long ts);        
    
    /**
     * 
     * 
     * @param root 
     * @return 
     */ 
    public StoreEntry[] getDeletedFiles(File root);    
    
    /**
     * 
     * Deletes a StoreEntry from the storage represented by the given file and timestamp
     * 
     * @param file the file for which a StoreEntry has to be deleted
     * @param ts the timestamp for which a StoreEntry has to be deleted
     * 
     */ 
    public void deleteEntry(File file, long ts); 
        
    /**
     * 
     * Removes all entries, their labels and all tags from the storage 
     * which are related to a time &lt; System.currentTimeMillis() - ttl;
     * 
     * @param ttl 
     */ 
    public void cleanUp(long ttl);    
}
