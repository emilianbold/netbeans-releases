/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.management.api.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.dlight.api.datafilter.DataFilterListener;
import org.netbeans.modules.dlight.api.datafilter.DataFilter;

public class SessionDataFiltersSupport {

    private final Object lock = new String(SessionDataFiltersSupport.class.getName());
    private final List<DataFilter> filters = new ArrayList<DataFilter>();
    private final Collection<DataFilterListener> listeners = new ArrayList<DataFilterListener>();

    public void addFilter(DataFilter filter, boolean isAdjusting) {
        synchronized (lock) {
            if (filters.add(filter)) {
                notifyListeners(isAdjusting);
            }
        }
    }

    public boolean removeFilter(DataFilter filter) {
        synchronized (lock) {
            boolean result = filters.remove(filter);
            if (result) {
                notifyListeners(false);
            }
            return result;
        }
    }

    public <T extends DataFilter> Collection<T> getDataFilter(Class<T> clazz){
        synchronized (lock) {
            Collection<T> result = new ArrayList<T>();
            for (DataFilter f : filters) {
                if (f.getClass() == clazz) {
                    result.add(clazz.cast(f));
                }
            }
            return result;
        }
    }

    public void cleanAll(){
        synchronized (lock) {
            filters.clear();
            notifyListeners(false);
        }
    }

    public void cleanAll(Class clazz){
        cleanAll(clazz, true);
    }

    public void cleanAll(Class clazz, boolean notify){
        synchronized (lock) {
            Collection<DataFilter> toRemove = new ArrayList<DataFilter>();
            for (DataFilter f : filters){
                if (f.getClass() == clazz){
                    toRemove.add(f);
                }
            }
            filters.removeAll(toRemove);
            if (notify){
                notifyListeners(false);
            }
        }
    }

    public void addDataFilterListener(DataFilterListener listener) {
        synchronized (lock) {
            listeners.add(listener);

           // And immediately notify it...
            listener.dataFiltersChanged(filters, false);
        }
    }

    public void removeDataFilterListener(DataFilterListener listener) {
        synchronized (lock) {
            listeners.remove(listener);
        }
    }

    public void removeAllListeners() {
        synchronized (lock) {
            listeners.clear();
        }
    }

    private void notifyListeners(boolean isAdjusting) {
        for (DataFilterListener listener : listeners) {
            listener.dataFiltersChanged(filters, isAdjusting);
        }
    }
}
