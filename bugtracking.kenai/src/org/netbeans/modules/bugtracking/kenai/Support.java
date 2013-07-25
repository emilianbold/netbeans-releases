/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.kenai;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.ui.api.KenaiServer;
import org.netbeans.modules.team.server.ui.spi.ProjectHandle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Stupka
 */
class Support {
    public static final Logger LOG = Logger.getLogger("org.netbeans.modules.bugtracking.kenai"); // NOI18N

    private RequestProcessor rp;
    private static Support instance;
    private Map<String, KenaiHandler> handlers = new HashMap<String, KenaiHandler>();

    final HashMap<String, WeakReference<TeamProjectImpl>> projectsCache = new HashMap<String, WeakReference<TeamProjectImpl>>();

    private Support() {
    }

    static Support getInstance() {
        if(instance == null) {
            instance = new Support();
        }
        return instance;
    }

    private RequestProcessor getRequestProcessor() {
        if(rp == null) {
            rp = new RequestProcessor("org.netbeans.modules.bugtracking.kenai", 10); // NOI18N
        }
        return rp;
    }

    void post(Runnable r) {
        getRequestProcessor().post(r);
    }
    KenaiHandler getKenaiHandler(ProjectHandle<KenaiProject> ph, QueryAccessorImpl accessor) {
        return getKenaiHandler(ph.getTeamProject().getKenai(), accessor);
    }
    KenaiHandler getKenaiHandler(Kenai kenai) {
        return getKenaiHandler(kenai, null);
    }
    KenaiHandler getKenaiHandler(Kenai kenai, QueryAccessorImpl accessor) {
        assert kenai != null;
        String url = kenai.getUrl().toString();
        KenaiHandler ret = handlers.get(url);
        if(ret == null && accessor != null) {
            ret = new KenaiHandler(accessor, kenai);
            handlers.put(url, ret);
        }
        return ret;
    }

}
