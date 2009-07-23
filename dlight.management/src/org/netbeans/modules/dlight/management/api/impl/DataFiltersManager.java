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
package org.netbeans.modules.dlight.management.api.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.dlight.api.datafilter.DataFilter;
import org.netbeans.modules.dlight.spi.datafilter.DataFilterFactory;
import org.netbeans.modules.dlight.util.DLightLogger;
import org.openide.util.Lookup;

public class DataFiltersManager {

    private static final Logger log = DLightLogger.getLogger(DataFiltersManager.class);
    private static DataFiltersManager instance = new DataFiltersManager();
    private final Collection<DataFilterFactory> allDataFilterFactories;

    private DataFiltersManager() {
        allDataFilterFactories = new HashSet<DataFilterFactory>();
    }

    public static DataFiltersManager getInstance() {
        return instance;
    }

    public DataFilter createFilter(String filterID, String filterSpec) {
        Collection<? extends DataFilterFactory> factories = Lookup.getDefault().lookupAll(DataFilterFactory.class);

        for (DataFilterFactory dff : factories) {
            if (allDataFilterFactories.add(dff)) {
                for (String id : dff.getSupportedFilterIDs()) {
                    if (log.isLoggable(Level.FINE)) {
                        log.fine("DataFilterFactory for " + id + " registered..."); // NOI18N
                    }
                }
            }
        }

        for (DataFilterFactory dff : allDataFilterFactories) {
            DataFilter filter = dff.createFilter(filterID, filterSpec);
            if (filter != null) {
                return filter;
            }
        }

        return null;
    }
}
