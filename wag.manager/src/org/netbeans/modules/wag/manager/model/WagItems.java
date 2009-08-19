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
package org.netbeans.modules.wag.manager.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;
import org.openide.util.RequestProcessor;

/**
 *
 * @author peterliu
 */
public abstract class WagItems<T> {

    public enum State {

        UNINITIALIZED, LOADING, INITIALIZED
    };
    
    protected PropertyChangeSupport pps;
    protected State state;
    protected SortedSet<T> items;

    public WagItems() {
        pps = new PropertyChangeSupport(this);
        state = State.UNINITIALIZED;
        items = new TreeSet<T>();
    }

    public State getState() {
        return state;
    }

    public abstract String getDisplayName();

    public abstract String getDescription();
    
    public void refresh() {
        RequestProcessor.getDefault().post(new Runnable() {

            public void run() {
                State oldState = state;
                items.clear();
                state = State.LOADING;
                fireChange(oldState, state);

                oldState = state;
                items.addAll(loadItems());
                state = State.INITIALIZED;
                fireChange(oldState, state);
            }
        });
    }

    public Collection<T> getItems() {
        return Collections.unmodifiableSortedSet(items);
    }

    public void addServices(Collection<T> itemsToAdd) {
        SortedSet<T> old = new TreeSet<T>(items);
        items.addAll(itemsToAdd);
        fireChange(old, Collections.unmodifiableSortedSet(items));
    }

    public void removeServices(Collection<T> itemsToRemove) {
        SortedSet<T> old = new TreeSet<T>(items);
        items.removeAll(itemsToRemove);
        fireChange(old, Collections.unmodifiableSortedSet(items));
    }


    public void addPropertyChangeListener(PropertyChangeListener l) {
        pps.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        pps.removePropertyChangeListener(l);
    }

    protected void fireChange(Object old, Object neu) {
        PropertyChangeEvent pce = new PropertyChangeEvent(this, getPropName(), old, neu);
        pps.firePropertyChange(pce);
    }

    protected abstract Collection<T> loadItems();

    protected abstract String getPropName();
}
