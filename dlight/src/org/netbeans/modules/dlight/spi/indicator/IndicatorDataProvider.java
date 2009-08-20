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
package org.netbeans.modules.dlight.spi.indicator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.dlight.api.datafilter.DataFilterListener;
import org.netbeans.modules.dlight.api.execution.DLightTarget;
import org.netbeans.modules.dlight.api.execution.DLightTargetListener;
import org.netbeans.modules.dlight.api.execution.Validateable;
import org.netbeans.modules.dlight.api.indicator.IndicatorDataProviderConfiguration;
import org.netbeans.modules.dlight.spi.impl.IndicatorAccessor;
import org.netbeans.modules.dlight.api.storage.DataRow;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.spi.impl.IndicatorDataProviderAccessor;
import org.netbeans.modules.dlight.spi.storage.ServiceInfoDataStorage;

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
    private final Collection<IndicatorNotificationsListener> notificationListeners = new ArrayList<IndicatorNotificationsListener>();
    private ServiceInfoDataStorage serviceInfoDataStorage;

    private final void addIndicatorDataProviderListener(IndicatorNotificationsListener l) {
        if (!notificationListeners.contains(l)) {
            notificationListeners.add(l);
        }
    }

    private final boolean removeIndicatorDataProviderListener(IndicatorNotificationsListener l) {
        return notificationListeners.remove(l);
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
    public final boolean subscribe(Indicator indicator) {
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
    public final void unsubscribe(Indicator indicator) {
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

    /**
     * Returns the list of {@link org.netbeans.modules.dlight.api.storage.DataTableMetadata}
     * this data provider can return information about
     * @return list of {@link org.netbeans.modules.dlight.api.storage.DataTableMetadata}
     * this data provider can return information about
     */
    public abstract Collection<DataTableMetadata> getDataTablesMetadata();

    /**
     * Returns name which will be used to filter indicator data
     * provider which will be currently used
     * @return data provider name
     */
    public abstract String getName();

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

    private static final class IndicatorDataProviderAccessorImpl extends IndicatorDataProviderAccessor{

        @Override
        public void addIndicatorDataProviderListener(IndicatorDataProvider provider, IndicatorNotificationsListener l) {
            provider.addIndicatorDataProviderListener(l);
        }

        @Override
        public boolean removeIndicatorDataProviderListener(IndicatorDataProvider provider, IndicatorNotificationsListener l) {
            return provider.removeIndicatorDataProviderListener(l);
        }

    }
}
