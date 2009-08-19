/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.gizmo;

import org.netbeans.modules.cnd.api.remote.HostInfoProvider;
import org.netbeans.modules.cnd.api.remote.PathMap;
import org.netbeans.modules.dlight.management.remote.spi.PathMapper;
import org.netbeans.modules.dlight.management.remote.spi.PathMapperProvider;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author mt154047
 */
@ServiceProvider(service = org.netbeans.modules.dlight.management.remote.spi.PathMapperProvider.class)
public class CndPathMapperProvider implements PathMapperProvider{

    public PathMapper getPathMapper(ExecutionEnvironment env) {
         PathMap mapper = HostInfoProvider.getMapper(env);
         return new PathMapperImpl(mapper);
    }

    private class PathMapperImpl implements PathMapper{
        private final PathMap pathMap;

        public PathMapperImpl(PathMap pathMap) {
            this.pathMap = pathMap;
        }

        public String getRemotePath(String localPath) {
            return pathMap.getRemotePath(localPath,true);
        }

        public String getLocalPath(String remotePath) {
            return pathMap.getLocalPath(remotePath,true);
        }

    }

}
