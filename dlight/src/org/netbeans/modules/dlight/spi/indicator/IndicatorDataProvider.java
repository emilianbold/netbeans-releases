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
package org.netbeans.modules.dlight.spi.indicator;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.netbeans.modules.dlight.api.datafilter.DataFilterListener;
import org.netbeans.modules.dlight.api.execution.DLightTarget;
import org.netbeans.modules.dlight.api.execution.DLightTargetChangeEvent;
import org.netbeans.modules.dlight.api.execution.DLightTargetListener;
import org.netbeans.modules.dlight.api.execution.Validateable;
import org.netbeans.modules.dlight.api.execution.ValidationListener;
import org.netbeans.modules.dlight.api.execution.ValidationStatus;
import org.netbeans.modules.dlight.api.indicator.IndicatorDataProviderConfiguration;
import org.netbeans.modules.dlight.spi.impl.IndicatorAccessor;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.spi.impl.IndicatorDataProviderAccessor;
import org.netbeans.modules.dlight.spi.storage.ServiceInfoDataStorage;
import org.netbeans.modules.dlight.util.DLightLogger;

/**
 * Provided information for {@link org.netbeans.modules.dlight.spi.indicator.Indicator}.
 * As indicators are supposed to be small and really fast real time UI,
 * data provider for indicators doesn't have to place any data to storage it
 * can use {@link #notifyIndicators(java.util.List)} method with newly real-time data
 * to notify all indicators subscribed to it using {@link #subscribe(org.netbeans.modules.dlight.spi.indicator.Indicator) }
 * @param <T> indicator data provider configuration implementation that can be used to create {@link org.netbeans.modules.dlight.spi.indicator.IndicatorDataProvider} instance
 */
public abstract class IndicatorDataProvider<T extends IndicatorDataProviderConfiguration>
        implements DLightTargetListener, Validateable<DLightTarget>, DataFilterListener {

    static {
        IndicatorDataProviderAccessor.setDefault(new IndicatorDataProviderAccessorImpl());
    }
    private final CopyOnWriteArrayList<IndicatorNotificationsListener> notificationListeners =
            new CopyOnWriteArrayList<IndicatorNotificationsListener>();
    private final CopyOnWriteArrayList<ValidationListener> validationListeners =
            new CopyOnWriteArrayList<ValidationListener>();
    private ServiceInfoDataStorage serviceInfoDataStorage;
    private DLightTarget validatedTarget = null;
    private ValidationStatus validationStatus;
    private final String NAME;

    public IndicatorDataProvider(String name) {
        this.NAME = name;
        validationStatus = ValidationStatus.initialStatus();
    }

    private void addIndicatorDataProviderListener(IndicatorNotificationsListener l) {
        notificationListeners.addIfAbsent(l);
    }

    private boolean removeIndicatorDataProviderListener(IndicatorNotificationsListener l) {
        return notificationListeners.remove(l);
    }

    @Override
    public final synchronized ValidationStatus validate(DLightTarget target) {
        if (validationStatus.isValid()) {
            return validationStatus;
        }

        ValidationStatus oldStatus = validationStatus;
        ValidationStatus newStatus = doValidation(target);

        notifyStatusChanged(oldStatus, newStatus);

        validatedTarget = target;
        validationStatus = newStatus;
        return newStatus;
    }

    protected abstract ValidationStatus doValidation(DLightTarget target);

    @Override
    public final synchronized void invalidate() {
        ValidationStatus oldStatus = validationStatus;
        validationStatus = ValidationStatus.initialStatus();
        validatedTarget = null;
        notifyStatusChanged(oldStatus, validationStatus);
    }

    @Override
    public final synchronized ValidationStatus getValidationStatus() {
        return validationStatus;
    }

    @Override
    public final void addValidationListener(ValidationListener listener) {
        validationListeners.addIfAbsent(listener);
    }

    @Override
    public final void removeValidationListener(ValidationListener listener) {
        validationListeners.remove(listener);
    }

    private void notifyStatusChanged(
            final ValidationStatus oldStatus,
            final ValidationStatus newStatus) {

        if (oldStatus.equals(newStatus)) {
            return;
        }

        for (ValidationListener validationListener : validationListeners) {
            validationListener.validationStateChanged(this, oldStatus, newStatus);
        }
    }

    @Override
    public void targetStateChanged(DLightTargetChangeEvent event) {
        DLightLogger.assertTrue(validatedTarget == event.target,
                "Validation was performed against another target"); // NOI18N

        switch (event.state) {
            case RUNNING:
                targetStarted(event.target);
                break;
            case DONE:
            case FAILED:
            case STOPPED:
            case TERMINATED:
                targetFinished(event.target);
                break;
        }
    }

    protected void targetStarted(DLightTarget target) {
    }

    protected void targetFinished(DLightTarget target) {
    }

    /**
     * Try to subscibe indicator to this Indicator DataProvider.
     * To successfuly subscribe indicator {@link #getDataTablesMetadata()} should contain
     * columns which are required by indicator(the result of
     * {@link Indicator#getMetadataColumns()} method)
     * @param indicator indicator to subscribe
     * @return <code>true</code> if indicator was successfuly subscribed,
     * <code>false</code> otherwise
     */
    public final boolean subscribe(Indicator<?> indicator) {
        List<DataTableMetadata.Column> indicatorColumns = IndicatorAccessor.getDefault().getMetadataColumns(indicator);

        // if this provider provides at least one column of information
        // that indicator can display - subscribe it.
        // TODO: ???

        for (DataTableMetadata tdm : getDataTablesMetadata()) {
            if (tdm == null) {
                continue;
            }

            List<DataTableMetadata.Column> providedColumns = tdm.getColumns();
            for (DataTableMetadata.Column pcol : providedColumns) {
                for (DataTableMetadata.Column icol : indicatorColumns) {
                    if (icol.equals(pcol)) {
                        indicator.columnProvided(icol);
                        addIndicatorDataProviderListener(indicator);
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Use this method to unsubscribe from this data provider
     * @param indicator indicator to unsubscribe
     */
    public final void unsubscribe(Indicator<?> indicator) {
        removeIndicatorDataProviderListener(indicator);
    }

    protected final void resetIndicators() {
        for (IndicatorNotificationsListener l : notificationListeners) {
            l.reset();
        }
    }

    protected final void notifyIndicators(List<DataRow> data) {
        for (IndicatorNotificationsListener l : notificationListeners) {
            l.updated(data);
        }
    }

    protected final void suggestIndicatorsRepaint() {
        for (IndicatorNotificationsListener l : notificationListeners) {
            l.suggestRepaint();
        }
    }

    /**
     * Returns the list of {@link org.netbeans.modules.dlight.api.storage.DataTableMetadata}
     * this data provider can return information about
     * @return list of {@link org.netbeans.modules.dlight.api.storage.DataTableMetadata}
     * this data provider can return information about
     */
    public abstract List<DataTableMetadata> getDataTablesMetadata();

    /**
     * Returns name which will be used to filter indicator data
     * provider which will be currently used
     * @return data provider name
     */
    public final String getName() {
        return NAME;
    }

    /**
     *  Initialize with service info data storage
     * @param infoStorage service infor data storage
     */
    public final void init(ServiceInfoDataStorage infoStorage) {
        this.serviceInfoDataStorage = infoStorage;
    }

    /**
     * Returns service info storage
     * @return service info storage
     */
    protected final ServiceInfoDataStorage getServiceInfoDataStorage() {
        return serviceInfoDataStorage;
    }

    private static final class IndicatorDataProviderAccessorImpl extends IndicatorDataProviderAccessor {

        @Override
        public void addIndicatorDataProviderListener(IndicatorDataProvider<?> provider, IndicatorNotificationsListener l) {
            provider.addIndicatorDataProviderListener(l);
        }

        @Override
        public boolean removeIndicatorDataProviderListener(IndicatorDataProvider<?> provider, IndicatorNotificationsListener l) {
            return provider.removeIndicatorDataProviderListener(l);
        }
    }
}
