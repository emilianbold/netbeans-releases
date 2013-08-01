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

package org.netbeans.modules.php.api.testing;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.php.spi.testing.PhpTestingProvider;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;
import org.openide.util.Parameters;
import org.openide.util.lookup.Lookups;

/**
 * This class provides access to the list of registered {@link PhpTestingProvider PHP testing providers}.
 * <p>
 * The path is "{@value #TESTING_PATH}" on SFS.
 */
public final class PhpTesting {

    /**
     * Path on SFS where {@link PhpTestingProvider PHP testing providers} need to be registered.
     */
    public static final String TESTING_PATH = "PHP/Testing"; //NOI18N
    /**
     * Identifier of Project Customizer for testing.
     * @since 0.6
     */
    public static final String CUSTOMIZER_IDENT = "Testing"; // NOI18N

    private static final Lookup.Result<PhpTestingProvider> TESTING_PROVIDERS = Lookups.forPath(TESTING_PATH).lookupResult(PhpTestingProvider.class);


    private PhpTesting() {
    }

    /**
     * Get all registered {@link PhpTestingProvider}s.
     * @return a list of all registered {@link PhpTestingProvider}s; never {@code null}.
     */
    public static List<PhpTestingProvider> getTestingProviders() {
        return new ArrayList<>(TESTING_PROVIDERS.allInstances());
    }

    /**
     * Add {@link LookupListener listener} to be notified when testing providers change
     * (new provider added, existing one removed).
     * <p>
     * To avoid memory leaks, do not forget to {@link #removeTestingProvidersListener(LookupListener) remove} the listener.
     * @param listener {@link LookupListener listener} to be added
     * @see #removeTestingProvidersListener(LookupListener)
     */
    public static void addTestingProvidersListener(@NonNull LookupListener listener) {
        Parameters.notNull("listener", listener);
        TESTING_PROVIDERS.addLookupListener(listener);
    }

    /**
     * Remove {@link LookupListener listener}.
     * @param listener {@link LookupListener listener} to be removed
     * @see #addTestingProvidersListener(LookupListener)
     */
    public static void removeTestingProvidersListener(@NonNull LookupListener listener) {
        Parameters.notNull("listener", listener);
        TESTING_PROVIDERS.removeLookupListener(listener);
    }

}
