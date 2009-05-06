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

package org.netbeans.modules.ide.ergonomics.fod;

import java.util.logging.Level;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;

/** Special ergonomics warm up extension that listens on changes in set of
 * enabled features and re-runs the warm up to make newly added features
 * ready for use.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@ServiceProvider(service=Runnable.class, path="WarmUp")
public final class WarmUp implements Runnable, ChangeListener {
    private static RequestProcessor RP = new RequestProcessor("FoD Warm Up"); // NOI18N
    private RequestProcessor.Task task;

    public void run() {
        FoDFileSystem.LOG.log(Level.FINE, "FoD Warmup Init {0}", task); // NOI18N
        if (task == null) {
            task = RP.create(this);
            FeatureManager.getInstance().addChangeListener(this);
        }
        if (RP.isRequestProcessorThread()) {
            FoDFileSystem.LOG.fine("Warmup starting..."); // NOI18N
            for (Runnable r : Lookups.forPath("WarmUp").lookupAll(Runnable.class)) { // NOI18N
                if (r == this) {
                    continue;
                }
                r.run();
            }
            FoDFileSystem.LOG.fine("Warmup done."); // NOI18N
        }
    }

    public void stateChanged(ChangeEvent e) {
        // schedule warm up
        task.schedule(5000);
    }
}
