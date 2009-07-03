/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.repository.queue;

import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.testbench.Stats;

/**
 * A queue for writing down cache
 * @author Vladimir Kvashin
 */
public class RepositoryQueue extends KeyValueQueue<Key, Persistent> {
    
    /** Overrides parent to allow timing by flag */
    @Override
    protected boolean needsTiming() {
        return Stats.queueTiming;
    }
    
    /** Overrides parent to allow timing by flag */
    @Override
    protected boolean needsTrace() {
        return Stats.queueTrace;
    }
    
    /** Returns this queue name; used for tracing/debugging purposes */
    @Override
    protected String getTraceName() {
        return "RepositoryQueue" + '@' + hashCode(); // NOI18N
    }
    
    @Override
    protected void doReplaceAddLast(Key key, Persistent value, Entry<Key, Persistent> existent) {
        super.doReplaceAddLast(key, value, existent);
        queue.remove(existent);
        queue.addLast(existent);
    }
    
    public void onIdle() {
        // do nothing
    }
    
    /**
     * Removes all queue entries that are accepted by the given filter.
     * @param filter a filter that deceides whether the entry should be removed
     * (if its accept returns true, entry should be removed)
     * @return a set of objects that are removed from queue
     */
    public Collection<RepositoryQueue.Entry<Key, Persistent>> clearQueue (Filter filter) {
       synchronized (lock) {
	   Collection<RepositoryQueue.Entry<Key, Persistent>> removed = new ArrayList<Entry<Key, Persistent>>();
	   // collecting entried to remove
	   for( Entry<Key, Persistent> entry : map.values() ) {
	       if( filter.accept(entry.getKey(), entry.getValue()) ) {
		   removed.add(entry);
	       }
	   }
	   // remove entries
	   for( Entry<Key, Persistent> entry : removed ) {
	       remove(entry.getKey());
	   }
	   return removed;
       }
    }

    /**
     * A filter for queue elements.
     * An instances of this interface may be passed to a method 
     * that manipulates entries (e.g. removes them, as the clearQueue method does).
     * In the case accept() returns true, the given action (i.e. removal) is performed,
     * otherwise it isn't done
     */
    public interface Filter {
	/**
	 * Is called for each entry in the queue.
	 * 
	 * @param key the given entry key
	 * @param entry the given entry value
	 * 
	 * @return true if the action (e.g. deletion) should be attempt, 
	 * otherwise false
	 */
        boolean accept(Key key, Persistent value);
    }
    
}


