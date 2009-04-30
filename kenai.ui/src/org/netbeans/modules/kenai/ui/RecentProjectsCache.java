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

package org.netbeans.modules.kenai.ui;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.HashMap;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.project.ui.api.RecentProjects;
import org.netbeans.modules.project.ui.api.UnloadedProjectInformation;
import org.openide.filesystems.FileStateInvalidException;

/**
 *
 * @author Jan Becicka
 */
public class RecentProjectsCache {

    private static RecentProjectsCache instance;
    private HashMap<URL,WeakReference<NbProjectHandleImpl>> map = new HashMap<URL, WeakReference<NbProjectHandleImpl>>();

    public static synchronized RecentProjectsCache getDefault() {
        if (instance==null) {
            instance = new RecentProjectsCache();
        }
        return instance;
    }

    public synchronized NbProjectHandleImpl getProjectHandle(URL url) throws FileStateInvalidException, IOException {
        WeakReference<NbProjectHandleImpl> ref = map.get(url);
        NbProjectHandleImpl handle = null;
        if (ref==null || ((handle=ref.get())==null)) {
            for (Project p: OpenProjects.getDefault().getOpenProjects()) {
                if (p.getProjectDirectory().getURL().equals(url)) {
                    handle = new NbProjectHandleImpl(p);
                    map.put(url, new WeakReference<NbProjectHandleImpl>(handle));
                    break;
                }
            }
            for (UnloadedProjectInformation i : RecentProjects.getDefault().getRecentProjectInformation()) {
                if (i.getURL().equals(url)) {
                    handle = new NbProjectHandleImpl(i);
                    map.put(url, new WeakReference<NbProjectHandleImpl>(handle));
                    break;
                }

            }
        }
        return handle;
    }

    public synchronized NbProjectHandleImpl getProjectHandle(Project p) throws IOException  {
        WeakReference<NbProjectHandleImpl> nbph = map.get(p.getProjectDirectory().getURL());
        if (nbph!=null && nbph.get()!=null) {
            return nbph.get();
        }
        NbProjectHandleImpl res  = new NbProjectHandleImpl(p);
        map.put(p.getProjectDirectory().getURL(), new WeakReference(res));
        return res;
    }

}
