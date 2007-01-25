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
    
    public String PROPERTY_CHANGED = "LocalHistoryStore.changed";    
    
    public void fileCreate(File file, long ts);        
    
    public void fileDelete(File file, long ts);
    
    public void fileCreateFromMove(File from, File to, long ts);
    
    public void fileDeleteFromMove(File from, File to, long ts);
 
    public void fileChange(File file, long ts);        
    
    public StoreEntry[] getFiles(File file);
    
    public StoreEntry getFile(File file, long ts);
    
    public StoreEntry[] getDeletedFiles(File file);    
    
    public void deleteEntry(File file, long ts); 
    
    public void setLabel(File file, long ts, String label);

    public void cleanUp(long ttl);
        
    public void addPropertyChangeListener(PropertyChangeListener l);
    
    public void removePropertyChangeListener(PropertyChangeListener l);
    
}
