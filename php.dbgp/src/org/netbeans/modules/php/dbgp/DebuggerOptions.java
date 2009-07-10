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
package org.netbeans.modules.php.dbgp;

import java.util.List;
import org.netbeans.modules.php.api.util.Pair;
import org.netbeans.modules.php.project.api.PhpOptions;

/**
 * @author Radek Matous
 */
public class DebuggerOptions {
    private static final DebuggerOptions GLOBAL_INSTANCE = new DefaultGlobal();
    int port = -1;
    Boolean debugForFirstPageOnly;
    Boolean debuggerStoppedAtTheFirstLine;
    String phpInterpreter;
    List<Pair<String, String>> pathMapping;
    Pair<String, Integer> debugProxy;

    public static DebuggerOptions getGlobalInstance() {
        return GLOBAL_INSTANCE;
    }

    public List<Pair<String, String>> getPathMapping() {
        return pathMapping;
    }

    /**
     *
     * @return debug proxy <host, port> or <code>null</code> if not used
     */
    public Pair<String, Integer> getDebugProxy() {
        return debugProxy;
    }

    public int getPort() {
        return (port != -1) ? port :  getGlobalInstance().getPort();
    }


    public boolean isDebugForFirstPageOnly() {
        return (debugForFirstPageOnly != null) ? debugForFirstPageOnly :
            getGlobalInstance().isDebugForFirstPageOnly();
    }

    public boolean isDebuggerStoppedAtTheFirstLine() {
        return (debuggerStoppedAtTheFirstLine != null) ? debuggerStoppedAtTheFirstLine :
            getGlobalInstance().isDebuggerStoppedAtTheFirstLine();
    }

    public String getPhpInterpreter() {
        return (phpInterpreter != null) ? phpInterpreter :
            getGlobalInstance().getPhpInterpreter();
    }

    private static class DefaultGlobal extends DebuggerOptions {
        public DefaultGlobal() {
        }

        @Override
        public int getPort() {
            return PhpOptions.getInstance().getDebuggerPort();
        }

        @Override
        public boolean isDebugForFirstPageOnly() {
            return false;
        }


        @Override
        public boolean isDebuggerStoppedAtTheFirstLine() {
            return PhpOptions.getInstance().isDebuggerStoppedAtTheFirstLine();
        }

        @Override
        public String getPhpInterpreter() {
            return PhpOptions.getInstance().getPhpInterpreter();
        }
    }
}
