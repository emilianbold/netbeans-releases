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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package com.strikeiron.search;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SubscriptionInfo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SubscriptionInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="LicenseStatusCode" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="LicenseStatus" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="LicenseActionCode" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="LicenseAction" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="RemainingHits" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="Amount" type="{http://www.w3.org/2001/XMLSchema}decimal"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SubscriptionInfo", namespace = "http://ws.strikeiron.com", propOrder = {
    "licenseStatusCode",
    "licenseStatus",
    "licenseActionCode",
    "licenseAction",
    "remainingHits",
    "amount"
})
public class SubscriptionInfo {

    @XmlElement(name = "LicenseStatusCode")
    protected int licenseStatusCode;
    @XmlElement(name = "LicenseStatus")
    protected String licenseStatus;
    @XmlElement(name = "LicenseActionCode")
    protected int licenseActionCode;
    @XmlElement(name = "LicenseAction")
    protected String licenseAction;
    @XmlElement(name = "RemainingHits")
    protected int remainingHits;
    @XmlElement(name = "Amount", required = true)
    protected BigDecimal amount;

    /**
     * Gets the value of the licenseStatusCode property.
     * 
     */
    public int getLicenseStatusCode() {
        return licenseStatusCode;
    }

    /**
     * Sets the value of the licenseStatusCode property.
     * 
     */
    public void setLicenseStatusCode(int value) {
        this.licenseStatusCode = value;
    }

    /**
     * Gets the value of the licenseStatus property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLicenseStatus() {
        return licenseStatus;
    }

    /**
     * Sets the value of the licenseStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLicenseStatus(String value) {
        this.licenseStatus = value;
    }

    /**
     * Gets the value of the licenseActionCode property.
     * 
     */
    public int getLicenseActionCode() {
        return licenseActionCode;
    }

    /**
     * Sets the value of the licenseActionCode property.
     * 
     */
    public void setLicenseActionCode(int value) {
        this.licenseActionCode = value;
    }

    /**
     * Gets the value of the licenseAction property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLicenseAction() {
        return licenseAction;
    }

    /**
     * Sets the value of the licenseAction property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLicenseAction(String value) {
        this.licenseAction = value;
    }

    /**
     * Gets the value of the remainingHits property.
     * 
     */
    public int getRemainingHits() {
        return remainingHits;
    }

    /**
     * Sets the value of the remainingHits property.
     * 
     */
    public void setRemainingHits(int value) {
        this.remainingHits = value;
    }

    /**
     * Gets the value of the amount property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * Sets the value of the amount property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setAmount(BigDecimal value) {
        this.amount = value;
    }

}
