/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.spi.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.netbeans.modules.dlight.api.execution.DLightTarget;
import org.netbeans.modules.dlight.api.execution.DLightTargetChangeEvent;
import org.netbeans.modules.dlight.api.execution.ValidationListener;
import org.netbeans.modules.dlight.api.execution.ValidationStatus;
import org.netbeans.modules.dlight.api.indicator.IndicatorDataProviderConfiguration;
import org.netbeans.modules.dlight.spi.support.TimerIDPConfiguration;
import org.netbeans.modules.dlight.spi.indicator.IndicatorDataProvider;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.api.datafilter.DataFilter;
import org.netbeans.modules.dlight.util.DLightExecutorService;

public final class TimerTicker
    extends IndicatorDataProvider<TimerIDPConfiguration>
    implements Runnable {

    private static final DataTableMetadata tableMetadata;
    private final Object lock = new String(TimerTicker.class.getName());
    private final IndicatorDataProviderConfiguration configuration;
    private long startTime = 0;
    private Future tickerService;


    static {
        tableMetadata = new DataTableMetadata(TimerIDPConfiguration.TIME_ID,
            Arrays.asList(TimerIDPConfiguration.TIME_INFO), null);
    }

    TimerTicker(TimerIDPConfiguration configuration) {
        this.configuration = configuration;
    }

    private void targetStarted(DLightTarget target) {
        synchronized (lock) {
            resetIndicators();
            tickerService = DLightExecutorService.scheduleAtFixedRate(
                this, 1, TimeUnit.SECONDS, "TimerTicker"); // NOI18N
            startTime = System.currentTimeMillis();
        }
    }

    private void targetFinished(DLightTarget target) {
        synchronized (lock) {
            tickerService.cancel(true);
            tickerService = null;
        }
    }

    public void run() {
        DataRow data = new DataRow(Arrays.asList(TimerIDPConfiguration.TIME_ID),
            Arrays.<Object>asList(System.currentTimeMillis() - startTime));

        notifyIndicators(Arrays.asList(data));
    }

    @Override
    public Collection<DataTableMetadata> getDataTablesMetadata() {
        return Arrays.asList(tableMetadata);
    }

    public void targetStateChanged(DLightTargetChangeEvent event) {
        switch (event.state) {
            case RUNNING:
                targetStarted(event.target);
                return;
            case FAILED:
                targetFinished(event.target);
                return;
            case TERMINATED:
                targetFinished(event.target);
                return;
            case DONE:
                targetFinished(event.target);
                return;
            case STOPPED:
                targetFinished(event.target);
                return;
        }
    }

    public ValidationStatus validate(DLightTarget objectToValidate) {
        return ValidationStatus.validStatus();
    }

    public void invalidate() {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public ValidationStatus getValidationStatus() {
        // throw new UnsupportedOperationException("Not supported yet.");
        return ValidationStatus.validStatus();
    }

    public void addValidationListener(ValidationListener listener) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeValidationListener(ValidationListener listener) {
        // throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getName() {
        return "Timer"; //NOI18N
    }

    public void dataFiltersChanged(List<DataFilter> newSet, boolean isAdjusting) {

    }
}
