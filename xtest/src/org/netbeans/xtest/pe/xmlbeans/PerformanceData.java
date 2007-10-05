/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

/*
 * PerformanceData.java
 *
 * Created on July 25, 2002, 4:14 PM
 */

package org.netbeans.xtest.pe.xmlbeans;

/**
 *
 * @author  <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 */
public class PerformanceData extends XMLBean {

    /** Creates new UnitTestCase */
    public PerformanceData() {
    }

    /** Getter for property name.
     * @return Value of property name.
     */
    public String getName() {
        return xmlat_name;
    }

    /** Setter for property name.
     * @param name New value of property name.
     */
    public void setName(String name) {
        xmlat_name = name;
    }
    
    /** Getter for property value.
     * @return Value 
     */
    public long getValue() {
        return xmlat_value;
    }
    
    /** Setter for property value.
     * @param time New value of property value.
     */
    public void setValue(long value) {
        xmlat_value = value;
    }
    
    /** Getter for property unit.
     * @return Value of property unit.
     */
    public String getUnit() {
        return xmlat_unit;
    }
    
    /** Setter for property unit.
     * @param className New value of property unit.
     */
    public void setUnit(String unit) {
        xmlat_unit = unit;
    }
    
    /** Getter for property unitTestSuite_id.
     * @return Value of property unitTestSuite_id.
     */
    public long getUnitTestSuite_id() {
        return this.unitTestSuite_id;
    }
    
    /** Setter for property unitTestSuite_id.
     * @param unitTestSuite_id New value of property unitTestSuite_id.
     */
    public void setUnitTestSuite_id(long unitTestSuite_id) {
        this.unitTestSuite_id = unitTestSuite_id;
    }
    
    /** Getter for property xmlat_runOrder.
     * @return Value of property xmlat_runOrder.
     *
     */
    public int getRunOrder() {
        return xmlat_runOrder;
    }    
    
    /** Setter for property xmlat_runOrder.
     * @param xmlat_runOrder New value of property xmlat_runOrder.
     *
     */
    public void setRunOrder(int xmlat_runOrder) {
        this.xmlat_runOrder = xmlat_runOrder;
    }    
    
    /** Getter for property xmlat_threshold.
     * @return Value of property xmlat_threshold.
     *
     */
    public long getThreshold() {
        return xmlat_threshold;
    }
    
    /** Setter for property xmlat_threshold.
     * @param xmlat_threshold New value of property xmlat_threshold.
     *
     */
    public void setThreshold(long xmlat_threshold) {
        this.xmlat_threshold = xmlat_threshold;
    }
    
    /** Holds value of property unitTestSuite_id. */
    private long unitTestSuite_id;
    
    // attributes
    public String   xmlat_name;
    public long     xmlat_value;
    public String   xmlat_unit;    
    public int      xmlat_runOrder;
    public long     xmlat_threshold;
}
