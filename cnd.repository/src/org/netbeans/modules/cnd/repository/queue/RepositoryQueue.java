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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
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


