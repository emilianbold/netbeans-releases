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
package org.netbeans.dlight.core.stack.model;

import java.util.Comparator;

/**
 * 
 * @author Maria Tishkova
 */
public final class FunctionMetric {

  private final String metricID;
  private final String metricDisplayedName;
  private final Class metricValueClass;
  public static final FunctionMetric CpuTimeInclusiveMetric = FunctionMetricsFactory.getInstance().create(new FunctionMetric.FunctionMetricConfiguration("time_incl", "CPU Time (Inclusive)", Double.class));
  public static final FunctionMetric CpuTimeExclusiveMetric = FunctionMetricsFactory.getInstance().create(new FunctionMetric.FunctionMetricConfiguration("time_excl", "CPU Time (Exclusive)", Double.class));

  FunctionMetric(FunctionMetricConfiguration metricConfiguration) {
    this.metricID = metricConfiguration.getMetricID();
    this.metricDisplayedName = metricConfiguration.getMetricDisplayedName();
    this.metricValueClass = metricConfiguration.getMetricValueClass();
  }

  public final String getMetricID() {
    return metricID;

  }

  public final String getMetricDisplayedName() {
    return metricDisplayedName;
  }

  public final Class getMetricValueClass() {
    return metricValueClass;
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
}
