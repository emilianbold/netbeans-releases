/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.debugger.jpda.truffle;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import org.netbeans.api.debugger.Properties;
import org.openide.util.BaseUtilities;

/**
 *
 * @author martin
 */
public final class TruffleProperties {
    
    private static final Properties truffleProperties = Properties.getDefault().getProperties("debugger.options.Truffle");  // NOI18N
    private static final String PROP_SHOW_INTERNAL = "showInternal";            // NOI18N
    private static TruffleProperties INSTANCE = new TruffleProperties();
    
    private TrufflePropertiesListener trufflePropertiesListener;
    
    private TruffleProperties() {}
    
    public static TruffleProperties getInstance() {
        return INSTANCE;
    }
    
    public boolean isShowInternal() {
        return truffleProperties.getBoolean(PROP_SHOW_INTERNAL, false);
    }
    
    public void setShowInternal(boolean showInternal) {
        truffleProperties.setBoolean(PROP_SHOW_INTERNAL, showInternal);
    }
    
    public synchronized Disposable onShowInternalChange(Consumer<Boolean> onChange) {
        if (trufflePropertiesListener == null) {
            trufflePropertiesListener = new TrufflePropertiesListener();
            truffleProperties.addPropertyChangeListener(trufflePropertiesListener);
        }
        return trufflePropertiesListener.addOnShowInternalChange(onChange);
    }
    
    public final class Disposable {
        
        private final LinkedList<Function> list;
        private final Consumer f;
        private final DisposableReference ref;
        
        Disposable(LinkedList list, Consumer f) {
            this.list = list;
            this.f = f;
            ref = new DisposableReference(this, BaseUtilities.activeReferenceQueue());
        }
        
        public void dispose() {
            ref.dispose();
            ref.clear();
        }
    }
    
    private static class DisposableReference extends WeakReference<Disposable> implements Runnable {
        
        private final LinkedList list;
        private final Consumer f;
        
        DisposableReference(Disposable disposable,  ReferenceQueue queue) {
            super(disposable, queue);
            this.list = disposable.list;
            this.f = disposable.f;
        }
        
        public void dispose() {
            synchronized (list) {
                list.remove(f);
            }
        }

        @Override
        public void run() {
            dispose();
        }
    }
    
    private class TrufflePropertiesListener implements PropertyChangeListener {
        
        private LinkedList<Consumer<Boolean>> onChangeListeners = new LinkedList<>();

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String propertyName = evt.getPropertyName();
            switch (propertyName) {
                case PROP_SHOW_INTERNAL:
                    Boolean isInternal = (Boolean) evt.getNewValue();
                    List<Consumer<Boolean>> listeners;
                    synchronized (onChangeListeners) {
                        listeners = new ArrayList<>(onChangeListeners);
                    }
                    for (Consumer<Boolean> f : listeners) {
                        f.accept(isInternal);
                    }
                    break;
            }
        }

        private Disposable addOnShowInternalChange(Consumer<Boolean> onChange) {
            synchronized (onChangeListeners) {
                onChangeListeners.add(onChange);
            }
            return new Disposable(onChangeListeners, onChange);
        }
        
    }
    
}
