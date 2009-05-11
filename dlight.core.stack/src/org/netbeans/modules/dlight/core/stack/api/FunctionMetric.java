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
package org.netbeans.modules.dlight.core.stack.api;

import org.netbeans.modules.dlight.core.stack.api.support.FunctionMetricsFactory;
import org.netbeans.modules.dlight.api.storage.types.Time;
import org.netbeans.modules.dlight.core.stack.api.impl.FunctionMetricAccessor;
import org.openide.util.NbBundle;

/**
 * Represents function metric, as an example "Exclusive Time" is metric, "Inclusive Time",
 * "Syncronization Wait Time", etc.
 */
public final class FunctionMetric {

    static{
        FunctionMetricAccessor.setDefault(new FunctionMetricAccessorImpl());
    }

    private final String metricID;
    private final String metricDisplayedName;
    private final Class metricValueClass;
    public static final FunctionMetric CpuTimeInclusiveMetric = 
            FunctionMetricsFactory.getInstance().getFunctionMetric(
            new FunctionMetric.FunctionMetricConfiguration("time_incl", getMessage("Metric.CpuTimeInclusive"), Time.class)); //NOI18N
    public static final FunctionMetric CpuTimeExclusiveMetric = 
            FunctionMetricsFactory.getInstance().getFunctionMetric(
            new FunctionMetric.FunctionMetricConfiguration("time_excl", getMessage("Metric.CpuTimeExclusive"), Time.class)); //NOI18N

    FunctionMetric(FunctionMetricConfiguration metricConfiguration) {
        this.metricID = metricConfiguration.getMetricID();
        this.metricDisplayedName = metricConfiguration.getMetricDisplayedName();
        this.metricValueClass = metricConfiguration.getMetricValueClass();
    }

    /**
     * Returns metric unique id
     * @return unique id
     */
    public final String getMetricID() {
        return metricID;

    }

    /**
     * Returns metric displayed name
     * @return
     */
    public final String getMetricDisplayedName() {
        return metricDisplayedName;
    }

    public final Class getMetricValueClass() {
        return metricValueClass;
    }

    @Override
    public String toString() {
        return metricDisplayedName;
    }

    public static final class FunctionMetricConfiguration {

        private final String metricID;
        private final String metricDisplayedName;
        private final Class metricValueClass;

        public FunctionMetricConfiguration(String metricID, String displayedName, Class metricValueClass) {
            this.metricID = metricID;
            this.metricDisplayedName = displayedName;
            this.metricValueClass = metricValueClass;
        }

        final String getMetricID() {
            return metricID;

        }

        final String getMetricDisplayedName() {
            return metricDisplayedName;
        }

        final Class getMetricValueClass() {
            return metricValueClass;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 41 * hash + (this.metricID != null ? this.metricID.hashCode() : 0);
            hash = 41 * hash + (this.metricDisplayedName != null ? this.metricDisplayedName.hashCode() : 0);
            hash = 41 * hash + (this.metricValueClass != null ? this.metricValueClass.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final FunctionMetricConfiguration other = (FunctionMetricConfiguration) obj;
            if ((this.metricID == null) ? (other.metricID != null) : !this.metricID.equals(other.metricID)) {
                return false;
            }
            if ((this.metricDisplayedName == null) ? (other.metricDisplayedName != null) : !this.metricDisplayedName.equals(other.metricDisplayedName)) {
                return false;
            }
            if (this.metricValueClass != other.metricValueClass && (this.metricValueClass == null || !this.metricValueClass.equals(other.metricValueClass))) {
                return false;
            }
            return true;
        }
    }

    private static final class FunctionMetricAccessorImpl extends FunctionMetricAccessor{

        @Override
        public FunctionMetric createNew(FunctionMetricConfiguration functionMetricConfiguration) {
            return new FunctionMetric(functionMetricConfiguration);
        }
        
    }

    private static final String getMessage(String name) {
        return NbBundle.getMessage(FunctionMetric.class, name);
    }
}
