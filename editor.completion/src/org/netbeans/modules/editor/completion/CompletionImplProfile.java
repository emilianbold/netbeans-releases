/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.editor.completion;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

final class CompletionImplProfile {
    private static final Logger LOG = Logger.getLogger(CompletionImplProfile.class.getName());

    private final Object profiler;
    private boolean profiling;
    private final long time;

    CompletionImplProfile(long when) {
        time = when;

        Object p = null;
        FileObject fo = FileUtil.getConfigFile("Actions/Profile/org-netbeans-modules-profiler-actions-SelfSamplerAction.instance"); // NOI18N
        if (fo != null) {
            Action a = (Action) fo.getAttribute("delegate"); // NOI18N
            if (a != null) {
                p = a.getValue("logger-completion"); // NOI18N
            }
        }
        this.profiler = p;
        this.profiling = true;
        
        if (profiler instanceof Runnable) {
            Runnable r = (Runnable) profiler;
            r.run();
            LOG.log(Level.FINE, "Profiling started {0} at {1}", new Object[] { profiler, time });
        }
    }

    final synchronized void stop() {
        if (!profiling) {
            return;
        }
        try {
            stopImpl();
        } catch (Exception ex) {
            LOG.log(Level.INFO, "Cannot stop profiling", ex);
        } finally {
            profiling = false;
        }
    }

    private void stopImpl() throws Exception {
        final long now = System.currentTimeMillis();
        long delta = now - time;
        LOG.log(Level.FINE, "Profiling stopped at {0}", now);
        ActionListener ss = (ActionListener) profiler;
        if (delta < 2000) {
            LOG.log(Level.FINE, "Cancel profiling of {0}. Profiling {1}. Time {2} ms.", new Object[] { ss, profiling, delta });
            if (ss != null) {
                ss.actionPerformed(new ActionEvent(this, 0, "cancel"));
            }
            return;
        }
        try {
            LOG.log(Level.FINE, "Obtaining snapshot for {0} ms.", delta);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(out);
            if (ss != null) {
                ss.actionPerformed(new ActionEvent(dos, 0, "write")); // NOI18N
            }
            dos.close();
            if (dos.size() > 0) {
                Object[] params = new Object[]{out.toByteArray(), delta, "CodeCompletion"};
                Logger.getLogger("org.netbeans.ui.performance").log(Level.CONFIG, "Slowness detected", params);
                LOG.log(Level.FINE, "Snapshot sent to UI logger. Size {0} bytes.", dos.size());
            } else {
                LOG.log(Level.WARNING, "No snapshot taken"); // NOI18N
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
