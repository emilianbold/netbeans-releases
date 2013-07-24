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
package org.netbeans.modules.team.server.ui.spi;

import java.awt.Color;
import javax.swing.Action;
import org.netbeans.modules.team.server.ui.common.ColorManager;
import org.openide.util.NbBundle;

/**
 * Handle for a build of a builder job.
 *
 * Instances of this class are immutable. If status of a build changes, the
 * parent job is notified and its list of builds is updated. Use
 * {@link JobHandle#addPropertyChangeListener(PropertyChangeListener)} to listen
 * for chages.
 *
 * @author S. Aubrecht
 */
@NbBundle.Messages({
    "LBL_Running=running",
    "LBL_Failed=failed",
    "LBL_Stable=stable",
    "LBL_Unstable=unstable",
    "LBL_Unknown=unknown"
})
public abstract class BuildHandle {

    public enum Status {
        
        RUNNING(Bundle.LBL_Running(), ColorManager.getDefault().getDefaultForeground() ),
        FAILED( Bundle.LBL_Failed(), ColorManager.getDefault().getErrorColor() ),
        STABLE( Bundle.LBL_Stable(), ColorManager.getDefault().getStableBuildColor() ),
        UNSTABLE( Bundle.LBL_Unstable(), ColorManager.getDefault().getUnstableBuildColor() ),
        UNKNOWN( Bundle.LBL_Unknown(), ColorManager.getDefault().getDisabledColor() );

        private final Color c;
        private final String displayName;

        private Status( String displayName, Color c ) {
            this.displayName = displayName;
            this.c = c;
        }

        public Color getColor() {
            return c;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    /**
     *
     * @return Display name
     */
    public abstract String getDisplayName();

    /**
     *
     * @return Build status
     */
    public abstract Status getStatus();

    /**
     *
     * @return Action to invoke when user pressed Enter key on given build line.
     */
    public abstract Action getDefaultAction();
}
