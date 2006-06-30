/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
