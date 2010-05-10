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
package org.netbeans.modules.dlight.spi.collector;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.dlight.api.collector.DataCollectorConfiguration;
import org.netbeans.modules.dlight.api.datafilter.DataFilterListener;
import org.netbeans.modules.dlight.api.execution.DLightTarget;
import org.netbeans.modules.dlight.api.execution.DLightTargetListener;
import org.netbeans.modules.dlight.api.execution.Validateable;
import org.netbeans.modules.dlight.api.storage.DataTableMetadata;
import org.netbeans.modules.dlight.spi.storage.DataStorage;
import org.netbeans.modules.dlight.spi.storage.DataStorageType;
import org.netbeans.modules.dlight.spi.storage.ServiceInfoDataStorage;

/**
 * DataCollector collects data from application/system.
 * Puts data into the {@link org.netbeans.modules.dlight.spi.storage.DataStorage}
 * it supports (see {@link #getSupportedDataStorageTypes()}) using the description
 * of data collected: {@link #getDataTablesMetadata()}.
 *
 * @param <G>
 */
public interface DataCollector<G extends DataCollectorConfiguration>
        extends DLightTargetListener, Validateable<DLightTarget>, DataFilterListener {


    /**
     * Add new listener which will be notified about all changes in the collector's state
     * @param listener listener to be added
     */
    void addDataCollectorListener(DataCollectorListener listener);

    /**
     * Removes the listener 
     * @param listener listener to be removed
     */
    void removeDataCollectorListener(DataCollectorListener listener);

    /**
     * The types of storage this collector requires
     * @return returns list of {@link org.netbeans.modules.dlight.spi.storage.DataStorageType}
     * data collector can put data into
     */
    Collection<DataStorageType> getRequiredDataStorageTypes();

    /**
     * The description of tables data collector will put information in.
     * @return list of {@link  org.netbeans.modules.dlight.api.storage.DataTableMetadata}
     * this collector gather information at.
     */
    List<DataTableMetadata> getDataTablesMetadata();

    /**
     * Method init() is called BEFORE target start
     * It can be used to initialize collector database tables, etc...
     * @param storages storage this collector will put data into
     * @param target target this collector serve for
     */
    void init(Map<DataStorageType, DataStorage> storages, DLightTarget target);


    /**
     *  Initialize with service info data storage
     * @param infoStorage service info data storage
     */
    void init(ServiceInfoDataStorage infoStorage);

    /**
     * DataCollector can attach to the {@link org.netbeans.modules.dlight.api.execution.DLightTarget}.
     * @return <code>true</code> if collector is attachable, <code>false</code> otherwise
     */
    boolean isAttachable();

    /**
     * In case {@link #isAttachable()} returns <code>false</code> this method should
     * return command line to run the collector
     * @return command line to run collector
     */
    String getCmd();

    /**
     * In case {@link #isAttachable()} returns <code>false</code> this method should
     * return arguments to run data collector with command line returned by {@link #getCmd()}
     * method
     * @return aguments for running this data collector
     */
    String[] getArgs();

    /**
     * Returns user visible name of DataCollector,
     * for example for collector based on DTrace we will
     * have here DTrace name
     * @return user visible name
     */
    String getName();

/**
     * States collector can be at
     */
    public enum CollectorState {


        /**
         * Initial state
         */
        INIT,
        /**
         * Validate state
         */
        VALIDATING,
        /**
         * Starting state
         */
        STARTING,
        /**
         * Running state
         */
        RUNNING,
        /**
         * Target is done
         */
        DONE,
        /**
         * Target is failed
         */
        FAILED,
        /**
         * Target is Stopped
         */
        STOPPED,
        /**
         * Target is terminated
         */
        TERMINATED,
    }
}
