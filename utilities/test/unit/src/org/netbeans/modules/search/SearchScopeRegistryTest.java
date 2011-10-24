/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of the
 * License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include the
 * License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by Oracle
 * in the GPL Version 2 section of the License file that accompanied this code.
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or only
 * the GPL Version 2, indicate your decision by adding "[Contributor] elects to
 * include this software in this distribution under the [CDDL or GPL Version 2]
 * license." If you do not indicate a single choice of license, a recipient has
 * the option to distribute your version of this file under either the CDDL, the
 * GPL Version 2 or to extend the choice of license to its licensees as provided
 * above. However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is made
 * subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.search;

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Exceptions;
import org.openidex.search.SearchInfo;

/**
 *
 * @author jhavlin
 */
public class SearchScopeRegistryTest extends NbTestCase {

    public SearchScopeRegistryTest(String name) {
        super(name);
    }

    /**
     * Test for bug 204118 - [71cat] AssertionError at
     * org.netbeans.modules.search.SearchScopeRegistry.addChangeListener.
     */
    public void testAddChangeListener() throws InterruptedException,
            InvocationTargetException {

        final ChangeListener cl = new CustomChangeListener();
        final ChangeListener cl2 = new CustomChangeListener();
        final CustomSearchScope css = new CustomSearchScope(true);
        final CustomSearchScope css2 = new CustomSearchScope(true);

        EventQueue.invokeAndWait(new Runnable() {

            @Override
            public void run() {
                try {
                    final SearchScopeRegistry ssr;
                    ssr = SearchScopeRegistry.getDefault();
                    assertEquals(0, ssr.getSearchScopes().size());
                    ssr.addChangeListener(cl);
                    ssr.registerSearchScope(css);
                    ssr.registerSearchScope(css2);
                    Thread t1 = new Thread() {

                        @Override
                        public void run() {
                            // remove listeners...
                            ssr.removeChangeListener(cl);
                            ssr.removeChangeListener(cl2);
                        }
                    };
                    t1.start();
                    Thread t2 = new Thread() {

                        @Override
                        public void run() {
                            // ... and fire listener event at the same time
                            css.setApplicable(!css.isApplicable());
                            css2.setApplicable(!css2.isApplicable());
                        }
                    };
                    t2.start();
                    t1.join();
                    t2.join();

                    ssr.unregisterSearchScope(css);
                    ssr.unregisterSearchScope(css2);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
    }

    /**
     * Call test for bug 20411 many times.
     *
     * It is needed to be able to detect the problem with synchronization.
     */
    public void testAddChangeListenerManyTimes() throws Exception {
        for (int i = 0; i < 500; i++) {
            testAddChangeListener();
        }
    }

    /**
     * Change listener implementation for the tests above.
     */
    private class CustomChangeListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent e) {
            assert e.getSource() instanceof SearchScopeRegistry;
        }
    }

    /**
     * Search scope implementation for the tests above.
     */
    private class CustomSearchScope extends SearchScope {

        private boolean applicable = true;
        private Set<ChangeListener> listeners = new HashSet<ChangeListener>();

        public CustomSearchScope(boolean applicable) {
            this.applicable = applicable;
        }

        @Override
        public String getTypeId() {
            return "TEST";
        }

        @Override
        protected String getDisplayName() {
            return "Test Search Scope";
        }

        @Override
        protected synchronized boolean isApplicable() {
            return applicable;
        }

        @Override
        protected synchronized void addChangeListener(ChangeListener l) {
            listeners.add(l);
        }

        @Override
        protected synchronized void removeChangeListener(ChangeListener l) {
            listeners.remove(l);
        }

        @Override
        protected SearchInfo getSearchInfo() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void setApplicable(boolean applicable) {

            Set<ChangeListener> listenersCopy = null;

            synchronized (this) {
                boolean oldVal = this.applicable;
                this.applicable = applicable;
                if (applicable != oldVal) {

                    listenersCopy = new HashSet(listeners);
                }
            }
            for (ChangeListener l : listenersCopy) {
                l.stateChanged(new ChangeEvent(this));
            }
        }
    }
}
