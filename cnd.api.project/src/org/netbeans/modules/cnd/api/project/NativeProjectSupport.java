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
package org.netbeans.modules.cnd.api.project;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import org.netbeans.modules.cnd.spi.project.NativeFileSearchProvider;
import org.netbeans.modules.cnd.spi.project.NativeProjectExecutionProvider;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Vladimir Voskresensky
 */
public final class NativeProjectSupport {
    private NativeProjectSupport() {}

    /**
     * Execute a command from user's PATH in the context of the native project
     *
     * @param executable Executable name (not path)
     * @param env Additional environment variables
     * @param args Arguments
     * @return NativeExitStatus
     */
    public static NativeExitStatus execute(NativeProject project, final String executable, final String[] env, final String... args) throws IOException {
        for (NativeProjectExecutionProvider provider : Lookups.forPath(NativeProjectExecutionProvider.PATH).lookupAll(NativeProjectExecutionProvider.class)) {
            NativeExitStatus result = provider.execute(project, executable, env, args);
            if (result != null) {
                return result;
            }            
        }
        return null;
    }

    /**
     * Return the name of the development platform (Solaris-x86, Solaris-sparc,
     * MacOSX, Windows, Linux-x86)
     *
     * @return development platform name
     */
    public static String getPlatformName(NativeProject project) {
        for (NativeProjectExecutionProvider provider : Lookups.forPath(NativeProjectExecutionProvider.PATH).lookupAll(NativeProjectExecutionProvider.class)) {
            String result = provider.getPlatformName(project);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    /**
     * *
     * Searcher for find project file by name
     *
     * @return searcher
     */
    public static NativeFileSearch getNativeFileSearch(final NativeProject project) {
        final List<NativeFileSearch> delegates = new ArrayList<NativeFileSearch>();
        for (NativeFileSearchProvider provider : Lookups.forPath(NativeFileSearchProvider.PATH).lookupAll(NativeFileSearchProvider.class)) {
            NativeFileSearch result = provider.getNativeFileSearch(project);
            if (result != null) {
                delegates.add(result);
            }
        }
        return new NativeFileSearch() {
            @Override
            public Collection<CharSequence> searchFile(NativeProject project, String fileName) {
                Collection<CharSequence> out = new LinkedHashSet<CharSequence>();
                for (NativeFileSearch searcher : delegates) {
                    out.addAll(searcher.searchFile(project, fileName));
                }
                return out;
            }
        };
    }

    public static final class NativeExitStatus {

        public final int exitCode;
        public final String error;
        public final String output;

        public NativeExitStatus(int exitCode, String output, String error) {
            this.exitCode = exitCode;
            this.error = error;
            this.output = output;
        }

        public boolean isOK() {
            return exitCode == 0;
        }
    }
}
