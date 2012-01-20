/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.api.extexecution.startup;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.extexecution.startup.StartupArgumentsRegistrationProcessor;
import org.netbeans.spi.extexecution.startup.StartupArgumentsProvider;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.lookup.Lookups;

/**
 * The API class allowing clients, typically server plugins or project,
 * to query additional groups of arguments it may pass to VM.
 *
 * @author Petr Hejl
 * @since 1.30
 * @see StartupArgumentsProvider
 */
public final class StartupArguments {

    private final String description;

    private final List<String> arguments;

    private StartupArguments(String description, List<String> arguments) {
        this.description = description;
        this.arguments = arguments;
    }

    /**
     * Returns the groups of arguments provided by all registered
     * {@link StartupArgumentsProvider} for the given start mode.
     * <p>The contents of the {@code context} parameter will depend on the kind of execution.
     * For a simple Java SE program being run in the Java launcher,
     * a {@code org.netbeans.api.project.Project} can be expected.
     * For a Java EE program being run in an application server, the context may correspond to
     * {@code org.netbeans.api.server.ServerInstance.getLookup()}.
     * Other kinds of API objects may be present according to contracts not specified here.
     * @param context the lookup providing the contract between client
     *             and provider
     * @param mode the VM mode the client is going to use
     * @return the groups of arguments provided by all registered
     *             {@link StartupArgumentsProvider}
     */
    @NonNull
    public static List<StartupArguments> getStartupArguments(
            @NonNull Lookup context, @NonNull StartMode mode) {
        Parameters.notNull("context", context);
        Parameters.notNull("mode", mode);

        Lookup lkp = Lookups.forPath(StartupArgumentsRegistrationProcessor.PATH);

        List<StartupArguments> res = new ArrayList<StartupArguments>();
        for (Lookup.Item<StartupArgumentsProvider> item : lkp.lookupResult(StartupArgumentsProvider.class).allItems()) {
            res.add(new StartupArguments(item.getDisplayName(),
                    item.getInstance().getArguments(context, mode)));
        }
        return res;
    }

    /**
     * Returns the description of group of arguments.
     *
     * @return the description of group of arguments
     */
    @NonNull
    public String getDescription() {
        return description;
    }

    /**
     * The list of the VM arguments.
     *
     * @return list of the VM arguments
     */
    @NonNull
    public List<String> getArguments() {
        return arguments;
    }

    /**
     * Class representing the startup mode of the VM.
     */
    public static enum StartMode {

        /**
         * The normal startup mode.
         */
        @NbBundle.Messages("StartMode_Normal=Normal")
        NORMAL(Bundle.StartMode_Normal()),

        /**
         * The debug startup mode.
         */
        @NbBundle.Messages("StartMode_Debug=Debug")
        DEBUG(Bundle.StartMode_Debug()),

        /**
         * The profile startup mode.
         */
        @NbBundle.Messages("StartMode_Profile=Profile")
        PROFILE(Bundle.StartMode_Profile());

        private final String mode;

        private StartMode(String mode) {
           this.mode = mode;
        }

        @Override
        public String toString() {
            return mode;
        }
    }
}
