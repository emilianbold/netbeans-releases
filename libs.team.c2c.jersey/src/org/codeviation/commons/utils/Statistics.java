/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.codeviation.commons.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A utility class that provides some common statistical functions.
 */
public abstract class Statistics {

    
    public static double average(Collection<? extends Number> values) {
        return average(values, false);
    }
        
    public static double average(Collection<? extends Number> values, 
            boolean includeNullAndNaN) {
        
        if (values == null) {
            throw new IllegalArgumentException("Null 'values' argument.");
        }
        int count = 0;
        double total = 0.0;
        
        for(Number number : values) {
            if (number == null) {
                if (includeNullAndNaN) {
                    return Double.NaN;
                }
            }
            else {                
                double value = number.doubleValue();
                if (Double.isNaN(value)) {
                    if (includeNullAndNaN) {
                        return Double.NaN;
                    }
                }
                else {
                    total += number.doubleValue();
                    count++;
                }
            }
        }      
        return total / count;
    }

    
    /**
     * Calculates the median for a list of values (<code>Number</code> objects).
     * If <code>copyAndSort</code> is <code>false</code>, the list is assumed
     * to be presorted in ascending order by value.
     * 
     * @param values  the values (<code>null</code> permitted).
     * @param copyAndSort  a flag that controls whether the list of values is
     *                     copied and sorted.
     * 
     * @return The median.
     */
    
    public static double median(Collection<? extends Number> values) {
        
        double result = Double.NaN;
        if (values != null) {
            
            int itemCount = values.size();
            List<Double> copy = new ArrayList<Double>(itemCount);
            for (Number number : values) {
                copy.add(number.doubleValue());
            }
            Collections.sort(copy);
            
            int count = copy.size();
            if (count > 0) {
                if (count % 2 == 1) {
                    if (count > 1) {
                        Number value = copy.get((count - 1) / 2);
                        result = value.doubleValue();
                    }
                    else {
                        Number value = copy.get(0);
                        result = value.doubleValue();
                    }
                }
                else {
                    Number value1 = copy.get(count / 2 - 1);
                    Number value2 = copy.get(count / 2);
                    result = (value1.doubleValue() + value2.doubleValue()) 
                             / 2.0;
                }
            }
        }
        return result;
    }
    
   
    public static double stdDev(Collection<? extends Number> data) {
        if (data == null) {
            throw new IllegalArgumentException("Null 'data' array.");
        }
        if (data.size() == 0) {
            throw new IllegalArgumentException("Zero length 'data' array.");
        }
        double avg = average(data);
        double sum = 0.0;

        for (Number number : data) {
            double diff = number.doubleValue() - avg;
            sum = sum + diff * diff;
        }
        return Math.sqrt(sum / (data.size() - 1));
    }
    
    
}
