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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.procfs.reader.impl;

import org.netbeans.modules.dlight.procfs.api.PUsage;

/**
 *
 * @author ak119685
 */
public class MSAInfoImpl extends PUsage.MSAInfo {

    public static final MSAInfoImpl nullInfo = new MSAInfoImpl();
    private final long abs_utime;
    private final long abs_stime;
    private final long abs_ttime;
    private final long abs_tftime;
    private final long abs_dftime;
    private final long abs_kftime;
    private final long abs_ltime;
    private final long abs_slptime;
    private final long abs_wtime;
    private final long abs_stoptime;

    private MSAInfoImpl() {
        super(0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        this.abs_utime = 0;
        this.abs_stime = 0;
        this.abs_ttime = 0;
        this.abs_tftime = 0;
        this.abs_dftime = 0;
        this.abs_kftime = 0;
        this.abs_ltime = 0;
        this.abs_slptime = 0;
        this.abs_wtime = 0;
        this.abs_stoptime = 0;
    }

    public MSAInfoImpl(MSAInfoImpl prevInfo,
            long pr_utime,
            long pr_stime,
            long pr_ttime,
            long pr_tftime,
            long pr_dftime,
            long pr_kftime,
            long pr_ltime,
            long pr_slptime,
            long pr_wtime,
            long pr_stoptime) {
        super(pr_utime - prevInfo.abs_utime,
                pr_stime - prevInfo.abs_stime,
                pr_ttime - prevInfo.abs_ttime,
                pr_tftime - prevInfo.abs_tftime,
                pr_dftime - prevInfo.abs_dftime,
                pr_kftime - prevInfo.abs_kftime,
                pr_ltime - prevInfo.abs_ltime,
                pr_slptime - prevInfo.abs_slptime,
                pr_wtime - prevInfo.abs_wtime,
                pr_stoptime - prevInfo.abs_stoptime);
        this.abs_utime = pr_utime;
        this.abs_stime = pr_stime;
        this.abs_ttime = pr_ttime;
        this.abs_tftime = pr_tftime;
        this.abs_dftime = pr_dftime;
        this.abs_kftime = pr_kftime;
        this.abs_ltime = pr_ltime;
        this.abs_slptime = pr_slptime;
        this.abs_wtime = pr_wtime;
        this.abs_stoptime = pr_stoptime;
    }
}
