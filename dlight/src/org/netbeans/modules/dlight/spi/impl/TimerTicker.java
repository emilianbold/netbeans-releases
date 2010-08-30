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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.spi.impl;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.netbeans.modules.dlight.api.execution.DLightTarget;
import org.netbeans.modules.dlight.api.execution.ValidationStatus;
import org.netbeans.modules.dlight.spi.support.TimerIDPConfiguration;
import org.netbeans.modules.dlight.spi.indicator.IndicatorDataProvider;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.api.datafilter.DataFilter;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.netbeans.modules.dlight.util.DLightExecutorService.DLightScheduledTask;

public final class TimerTicker
        extends IndicatorDataProvider<TimerIDPConfiguration>
        implements Runnable {

    private static final List<DataTableMetadata> metadata;
    private static final List<String> columnNames;
    private long startTime = 0;
    private DLightScheduledTask tickerTask;

    static {
        DataTableMetadata tableMetadata = new DataTableMetadata(TimerIDPConfiguration.TIME_ID,
                Collections.singletonList(TimerIDPConfiguration.TIME_INFO), null);
        metadata = Collections.singletonList(tableMetadata);
        columnNames = tableMetadata.getColumnNames();
    }

    TimerTicker(TimerIDPConfiguration configuration) {
        super("Timer"); // NOI18N
    }

    @Override
    protected synchronized void targetStarted(DLightTarget target) {
        resetIndicators();
        tickerTask = DLightExecutorService.scheduleAtFixedRate(
                this, 1, TimeUnit.SECONDS, "TimerTicker"); // NOI18N
        startTime = System.currentTimeMillis();
    }

    @Override
    protected synchronized void targetFinished(DLightTarget target) {
        tickerTask.cancel(1);
        tickerTask = null;
    }

    @Override
    public void run() {
        DataRow data = new DataRow(columnNames,
                Collections.singletonList(System.currentTimeMillis() - startTime));

        notifyIndicators(Collections.singletonList(data));
    }

    @Override
    protected ValidationStatus doValidation(DLightTarget target) {
        return ValidationStatus.validStatus();
    }

    @Override
    public void dataFiltersChanged(List<DataFilter> newSet, boolean isAdjusting) {
    }

    @Override
    public List<DataTableMetadata> getDataTablesMetadata() {
        return metadata;
    }
}
