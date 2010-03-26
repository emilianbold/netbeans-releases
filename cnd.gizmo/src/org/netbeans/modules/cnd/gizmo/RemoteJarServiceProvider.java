/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.gizmo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.cnd.api.remote.HostInfoProvider;
import org.netbeans.modules.cnd.api.remote.SetupProvider;
import org.netbeans.modules.cnd.dwarfdump.Offset2LineService;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcess;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;

/**
 *
 * @author Alexander Simon
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.api.remote.SetupProvider.class)
public class RemoteJarServiceProvider implements SetupProvider {
    private static final Class<?> service = Offset2LineService.class;
    private static final String relativePath;
    static {
        String path = service.getProtectionDomain().getCodeSource().getLocation().getPath();
        path = path.replace('\\', '/');
        path = path.substring(path.lastIndexOf("cnd/")+4);
        if (path.indexOf('!') > 0) {
            path = path.substring(0, path.indexOf('!'));
        }
        relativePath = path;
    }

    @Override
    public Map<String, String> getBinaryFiles(ExecutionEnvironment env) {
        Map<String, String> result = new HashMap<String, String>();
        result.put(relativePath, relativePath); // NOI18N
        return result;
    }

    public static NativeProcess getJavaProcess(Class<?> clazz, ExecutionEnvironment env, String[] arguments) throws IOException{
        NativeProcessBuilder npb = NativeProcessBuilder.newProcessBuilder(env);
        npb.setExecutable("java"); //NOI18N
        List<String> args = new ArrayList<String>();
        args.add("-cp"); //NOI18N
        if (env.isLocal()) {
            args.add(clazz.getProtectionDomain().getCodeSource().getLocation().getPath());
        } else {
            String libDir = HostInfoProvider.getLibDir(env); //NB: should contain trailing '/'
            if (!libDir.endsWith("/")) { // NOI18N
                libDir += "/"; // NOI18N
            }
            String resource = libDir+relativePath;
            args.add(resource);
        }
        args.add(clazz.getName());
        args.addAll(Arrays.asList(arguments));
        npb.setArguments(args.toArray(new String[args.size()]));
        return npb.call();
    }
}
