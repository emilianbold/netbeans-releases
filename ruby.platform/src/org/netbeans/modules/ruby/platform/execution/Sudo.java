/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.ruby.platform.execution;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.ruby.platform.Util;

// TODO: allow client to choose among sudo provider (gksu, kdesu, ...), or
//       better, improve selection, on KDE choose kdesu, on GNOME gksu
// TODO: pluggable SPI for other providers

/**
 * Provides <em>sudo</em> tool capabilities. It is a blackbox wrt. to selecting
 * the suitable sudo tool.
 */
public final class Sudo {

    private final Iterable<String> command;
    private final String message;

    /**
     * The same like {@link #Sudo(Iterable, String)} with default
     * message.
     */
    public Sudo(final Iterable<String> command) {
        this(command, null);
    }

    /**
     * @param command command to be run under a sudo tool
     * @param message message to be shown to the user if supported by the tool
     */
    public Sudo(final Iterable<String> command, final String message) {
        this.command = command;
        this.message = message;
    }

    /**
     * @return returns command as a list suitable for the {@link ProcessBuilder}
     */
    public List<String> createCommand() {
        // run through gksu
        String gksu = Util.findOnPath("gksu"); // NOI18N
        if (gksu != null) {
            return compoundGksu(gksu);
        }
        String kdesu = Util.findOnPath("kdesu"); // NOI18N
        if (kdesu != null) {
            return compoundKdesu(kdesu);
        }
        throw new AssertionError("sudo tool cannot be found; be sure you've checked it before using GemRunner");
    }

    private List<String> compoundGksu(String gksu) {
        List<String> argList = new ArrayList<String>();
        argList.add(gksu);
        argList.add("--su-mode"); // NOI18N
        argList.add("--description"); // NOI18N
        argList.add(message);
        StringBuilder asString = new StringBuilder();
        for (String arg : command) {
            asString.append(arg).append(' ');
        }
        argList.add(asString.toString().trim()); // trim the last space from loop above
        return argList;
    }

    private List<String> compoundKdesu(String kdesu) {
        List<String> argList = new ArrayList<String>();
        argList.add(kdesu);
        StringBuilder asString = new StringBuilder();
        for (String arg : command) {
            asString.append(arg).append(' ');
        }
        argList.add("-c"); // NOI18N
        argList.add(asString.toString().trim()); // trim the last space from loop above
        return argList;
    }

}
