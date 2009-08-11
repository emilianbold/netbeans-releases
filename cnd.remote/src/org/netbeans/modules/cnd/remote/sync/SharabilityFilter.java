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
package org.netbeans.modules.cnd.remote.sync;

import java.io.File;
import java.io.FileFilter;
import java.util.logging.Logger;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.modules.cnd.utils.CndUtils;

/**
 * FileFilter implementation that is based on file sharability
 * @author Vladimir Kvashin
 */
public class SharabilityFilter implements FileFilter {

    public interface StatisticsCallback {
        void onAccept(File file, boolean accepted);
    }

    private Logger logger = Logger.getLogger("cnd.remote.logger"); // NOI18N
    private static final boolean TRACE_SHARABILITY = Boolean.getBoolean("cnd.remote.trace.sharability"); //NOI18N
    private StatisticsCallback statisticsCallback;

    public final boolean accept(File file) {
        boolean accepted = acceptImpl(file);
        if (statisticsCallback != null) {
            statisticsCallback.onAccept(file, accepted);
        }
        return accepted;
    }

    public void setStatisticsCallback(StatisticsCallback statisticsCallback) {
        this.statisticsCallback = statisticsCallback;
    }

    protected boolean acceptImpl(File file) {
        final int sharability = SharabilityQuery.getSharability(file);
        if(TRACE_SHARABILITY) {
            logger.info(file.getAbsolutePath() + " sharability is " + sharabilityToString(sharability));
        }
        switch (sharability) {
            case SharabilityQuery.NOT_SHARABLE:
                return false;
            case SharabilityQuery.MIXED:
            case SharabilityQuery.SHARABLE:
            case SharabilityQuery.UNKNOWN:
                return true;
            default:
                CndUtils.assertTrueInConsole(false, "Unexpected sharability value: " + sharability); //NOI18N
                return true;
        }
    }

    private static String sharabilityToString(int sharability) {
        switch (sharability) {
            case SharabilityQuery.NOT_SHARABLE: return "NOT_SHARABLE"; //NOI18N
            case SharabilityQuery.MIXED:        return "MIXED"; //NOI18N
            case SharabilityQuery.SHARABLE:     return "SHARABLE"; //NOI18N
            case SharabilityQuery.UNKNOWN:      return "UNKNOWN"; //NOI18N
            default:                            return "UNEXPECTED: " + sharability; //NOI18N
        }
    }
}
