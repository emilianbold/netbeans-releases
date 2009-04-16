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
package org.netbeans.modules.dlight.perfan.storage.impl;

import java.text.DecimalFormat;
import java.text.ParseException;
import org.openide.util.Exceptions;

/**
 *
 * @author ak119685
 */
public final class ExperimentStatistics {

    static private final DecimalFormat nf;
    private final Double startTime;
    private final Double endTime;
    private final Double duration;
    private final Double totalThreadTime;
    private final Double avrgThreadsNumber;
    private final Double t_userCPU;
    private final Double t_userCPU_p;
    private final Double t_sysCPU;
    private final Double t_sysCPU_p;
    private final Double t_waitCPU;
    private final Double t_waitCPU_p;
    private final Double t_usrLock;
    private final Double t_usrLock_p;


    static {
        nf = new DecimalFormat();
        nf.getDecimalFormatSymbols().setDecimalSeparator(',');
    }

    ExperimentStatistics(String[] toParse) {
        Double _startTime = null;
        Double _endTime = null;
        Double _duration = null;
        Double _totalThreadTime = null;
        Double _avrgThreadsNumber = null;
        Double _t_userCPU = null;
        Double _t_userCPU_p = null;
        Double _t_sysCPU = null;
        Double _t_sysCPU_p = null;
        Double _t_waitCPU = null;
        Double _t_waitCPU_p = null;
        Double _t_usrLock = null;
        Double _t_usrLock_p = null;

        for (String s : toParse) {
            int scidx = s.indexOf(':');
            if (scidx < 0) {
                continue;
            }

            String id = s.substring(0, scidx).trim();
            if (id.startsWith("User Lock")) { // NOI18N
                int bidx = s.lastIndexOf('(');
                try {
                    _t_usrLock = nf.parse(s.substring(scidx + 1, bidx).trim()).doubleValue();
                    _t_usrLock_p = nf.parse(s.substring(bidx + 1).trim()).doubleValue();
                } catch (ParseException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        startTime = _startTime;
        endTime = _endTime;
        duration = _duration;
        totalThreadTime = _totalThreadTime;
        avrgThreadsNumber = _avrgThreadsNumber;
        t_userCPU = _t_userCPU;
        t_userCPU_p = _t_userCPU_p;
        t_sysCPU = _t_sysCPU;
        t_sysCPU_p = _t_sysCPU_p;
        t_waitCPU = _t_waitCPU;
        t_waitCPU_p = _t_waitCPU_p;
        t_usrLock = _t_usrLock;
        t_usrLock_p = _t_usrLock_p;
    }

    public Double getULock_p() {
        return t_usrLock_p;
    }
}
