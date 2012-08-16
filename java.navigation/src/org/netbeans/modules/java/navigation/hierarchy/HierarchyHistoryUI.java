/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.navigation.hierarchy;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.lang.model.element.TypeElement;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.java.navigation.base.Pair;
import org.openide.util.Mutex;
import org.openide.util.WeakListeners;

/**
 *
 * @author Tomas Zezula
 */
class HierarchyHistoryUI {

    private HierarchyHistoryUI() {
        throw new IllegalStateException();
    }

    static ComboBoxModel createModel() {
        return new HierarchyHistoryModel();
    }


    static ListCellRenderer createRenderer() {
        return new HierarchyHistoryRenderer();
    }


    private static class HierarchyHistoryRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof Pair && ((Pair)value).second instanceof ElementHandle) {
                final String fqn =  ((ElementHandle)((Pair)value).second).getQualifiedName();
                value = HierarchyHistory.getSimpleName(fqn);
            }
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }

    private static class HierarchyHistoryModel implements ComboBoxModel, PropertyChangeListener {

        private final List<ListDataListener> listeners;
        private final HierarchyHistory history;
        //@GuardedBy("this")
        private List<? extends Pair<URI,ElementHandle<TypeElement>>> cache;
        private Object selectedItem;


        HierarchyHistoryModel() {
            listeners = new CopyOnWriteArrayList<ListDataListener>();
            history = HierarchyHistory.getInstance();
            history.addPropertyChangeListener(WeakListeners.propertyChange(this, history));
        }

        @Override
        public void setSelectedItem(Object anItem) {
            this.selectedItem = anItem;
            fire();
        }

        @Override
        public Object getSelectedItem() {
            return selectedItem;
        }

        @Override
        public int getSize() {
            return getCache().size();
        }

        @Override
        public Object getElementAt(int index) {
            return getCache().get(index);
        }

        @Override
        public void addListDataListener(@NonNull final ListDataListener l) {
            assert l != null;
            listeners.add(l);
        }

        @Override
        public void removeListDataListener(@NonNull final ListDataListener l) {
            assert l != null;
            listeners.remove(l);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (HierarchyHistory.HISTORY.equals(evt.getPropertyName())) {
                synchronized (this) {
                    cache = null;
                }
                Mutex.EVENT.readAccess(new Runnable() {
                    @Override
                    public void run() {
                        fire();
                    }
                });
            }
        }

        private void fire() {
            final ListDataEvent event = new ListDataEvent(
                    this,
                    -1,
                    -1,
                    Integer.MAX_VALUE);
            for (ListDataListener l : listeners) {
                l.contentsChanged(event);
            }
        }


        @NonNull
        private synchronized List<? extends Pair<URI,ElementHandle<TypeElement>>> getCache() {
            if (cache == null) {
                cache = history.getHistory();
            }
            return cache;
        }

    }

}
