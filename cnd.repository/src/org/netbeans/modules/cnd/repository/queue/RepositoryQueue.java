/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.repository.queue;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.testbench.Stats;

/**
 * A queue for writing down cache
 * @author Vladimir Kvashin
 */
public class RepositoryQueue extends KeyValueQueue<Key, Persistent> {
    
    /** Overrides parent to allow timing by flag */
    protected boolean needsTiming() {
        return Stats.queueTiming;
    }
    
    /** Overrides parent to allow timing by flag */
    protected boolean needsTrace() {
        return Stats.queueTrace;
    }
    
    /** Returns this queue name; used for tracing/debugging purposes */
    protected String getTraceName() {
        return "RepositoryQueue" + '@' + hashCode(); // NOI18N
    }
    
    protected void doReplaceAddLast(Key key, Persistent value, Entry existent) {
        super.doReplaceAddLast(key, value, existent);
        queue.remove(existent);
        queue.addLast(existent);
    }
    
    public void onIdle() {
        // do nothing
    }
    
    public void clearQueue (Validator validator) {
       synchronized (lock) {
            // don't use Iterator.remove here
            Collection<Key> copy = new HashSet<Key>(map.keySet());
            
            for (Iterator<Key> it = copy.iterator(); it.hasNext();) {
                Key key = it.next();
                Persistent value = map.get(key).getValue();

                if (!validator.isValid(key, value)) {
                    remove(key);
                }
            }
        }
    }

    public interface Validator {
        boolean isValid(Key key, Persistent entry);
    }
    
}


