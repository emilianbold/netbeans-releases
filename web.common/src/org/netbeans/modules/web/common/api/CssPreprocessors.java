/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.common.api;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.web.common.spi.CssPreprocessor;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Parameters;
import org.openide.util.lookup.Lookups;

/**
 * This class provides access to the list of registered CSS preprocessors. The path
 * for registration is "{@value #PREPROCESSORS_PATH}" on SFS.
 * <p>
 * For typical usage, {@link Support support} class can be used.
 * @since 1.37
 */
public final class CssPreprocessors {

    // XXX path is ok or not?
    /**
     * Path on SFS for CSS preprocessors registrations.
     */
    public static final String PREPROCESSORS_PATH = "CSS/PreProcessors"; // NOI18N

    private static final Lookup.Result<CssPreprocessor> PREPROCESSORS = Lookups.forPath(PREPROCESSORS_PATH).lookupResult(CssPreprocessor.class);


    private CssPreprocessors() {
    }

    /**
     * Get all registered {@link CssPreprocessor}s.
     * @return a list of all registered {@link CssPreprocessor}s; never {@code null}
     * @see Support#getPreprocessors()
     */
    public static List<CssPreprocessor> getPreprocessors() {
        return new ArrayList<CssPreprocessor>(PREPROCESSORS.allInstances());
    }

    /**
     * Add {@link LookupListener listener} to be notified when preprocessors change
     * (new preprocessor added, existing removed).
     * <p>
     * To avoid memory leaks, do not forget to {@link #removePreprocessorsListener(LookupListener) remove} the listener.
     * @param listener {@link LookupListener listener} to be added
     * @see #removePreprocessorsListener(LookupListener)
     */
    public static void addPreprocessorsListener(@NonNull LookupListener listener) {
        Parameters.notNull("listener", listener);
        PREPROCESSORS.addLookupListener(listener);
    }

    /**
     * Remove {@link LookupListener listener}.
     * @param listener {@link LookupListener listener} to be removed
     * @see #addPreprocessorsListener(LookupListener)
     */
    public static void removePreprocessorsListener(@NonNull LookupListener listener) {
        Parameters.notNull("listener", listener);
        PREPROCESSORS.removeLookupListener(listener);
    }

    //~ Inner classes

    /**
     * Support class for {@link CssPreprocessors} which avoids lookup calls. In other words,
     * it {@link #start() starts} and {@link #stop() stops} holding CSS preprocessors and listening to SFS changes.
     */
    public static final class Support implements LookupListener {

        private final List<CssPreprocessor> preprocessors = new CopyOnWriteArrayList<CssPreprocessor>();


        /**
         * Create new support for {@link CssPreprocessors}.
         * @see #start()
         * @see #stop()
         */
        public Support() {
        }

        /**
         * Start holding CSS preprocessors and listening to SFS changes.
         * @see #stop()
         */
        public void start() {
            if (!preprocessors.isEmpty()) {
                throw new IllegalStateException("Preprocessors already held; forgot to call stop() method?");
            }
            CssPreprocessors.addPreprocessorsListener(this);
            preprocessors.addAll(CssPreprocessors.getPreprocessors());
        }

        /**
         * Stop holding CSS preprocessors and listening to SFS changes.
         * @see #start()
         */
        public void stop() {
            CssPreprocessors.removePreprocessorsListener(this);
            preprocessors.clear();
        }

        /**
         * Get all registered {@link CssPreprocessor}s.
         * @return a list of all registered {@link CssPreprocessor}s; never {@code null}
         * @see CssPreprocessors#getPreprocessors()
         */
        public List<CssPreprocessor> getPreprocessors() {
            return new ArrayList<CssPreprocessor>(preprocessors);
        }

        @Override
        public void resultChanged(LookupEvent ev) {
            synchronized (preprocessors) {
                preprocessors.clear();
                preprocessors.addAll(CssPreprocessors.getPreprocessors());
            }
        }

    }

}
