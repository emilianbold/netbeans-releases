/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.coherence.xml.cache;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "schemeName",
    "schemeRef",
    "className",
    "serviceName",
    "initParams",
    "evictionPolicy",
    "highUnits",
    "lowUnits",
    "unitCalculator",
    "unitFactor",
    "expiryDelay",
    "flushDelay",
    "cachestoreScheme",
    "preLoad",
    "listener"
})
@XmlRootElement(name = "local-scheme")
public class LocalScheme {

    @XmlElement(name = "scheme-name")
    protected SchemeName schemeName;
    @XmlElement(name = "scheme-ref")
    protected String schemeRef;
    @XmlElement(name = "class-name")
    protected ClassName className;
    @XmlElement(name = "service-name")
    protected String serviceName;
    @XmlElement(name = "init-params")
    protected InitParams initParams;
    @XmlElement(name = "eviction-policy")
    protected String evictionPolicy;
    @XmlElement(name = "high-units")
    protected String highUnits;
    @XmlElement(name = "low-units")
    protected String lowUnits;
    @XmlElement(name = "unit-calculator")
    protected String unitCalculator;
    @XmlElement(name = "unit-factor")
    protected String unitFactor;
    @XmlElement(name = "expiry-delay")
    protected String expiryDelay;
    @XmlElement(name = "flush-delay")
    protected String flushDelay;
    @XmlElement(name = "cachestore-scheme")
    protected CachestoreScheme cachestoreScheme;
    @XmlElement(name = "pre-load")
    protected String preLoad;
    protected Listener listener;

    /**
     * Gets the value of the schemeName property.
     * 
     * @return
     *     possible object is
     *     {@link SchemeName }
     *     
     */
    public SchemeName getSchemeName() {
        return schemeName;
    }

    /**
     * Sets the value of the schemeName property.
     * 
     * @param value
     *     allowed object is
     *     {@link SchemeName }
     *     
     */
    public void setSchemeName(SchemeName value) {
        this.schemeName = value;
    }

    /**
     * Gets the value of the schemeRef property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSchemeRef() {
        return schemeRef;
    }

    /**
     * Sets the value of the schemeRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSchemeRef(String value) {
        this.schemeRef = value;
    }

    /**
     * Gets the value of the className property.
     * 
     * @return
     *     possible object is
     *     {@link ClassName }
     *     
     */
    public ClassName getClassName() {
        return className;
    }

    /**
     * Sets the value of the className property.
     * 
     * @param value
     *     allowed object is
     *     {@link ClassName }
     *     
     */
    public void setClassName(ClassName value) {
        this.className = value;
    }

    /**
     * Gets the value of the serviceName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * Sets the value of the serviceName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setServiceName(String value) {
        this.serviceName = value;
    }

    /**
     * Gets the value of the initParams property.
     * 
     * @return
     *     possible object is
     *     {@link InitParams }
     *     
     */
    public InitParams getInitParams() {
        return initParams;
    }

    /**
     * Sets the value of the initParams property.
     * 
     * @param value
     *     allowed object is
     *     {@link InitParams }
     *     
     */
    public void setInitParams(InitParams value) {
        this.initParams = value;
    }

    /**
     * Gets the value of the evictionPolicy property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEvictionPolicy() {
        return evictionPolicy;
    }

    /**
     * Sets the value of the evictionPolicy property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEvictionPolicy(String value) {
        this.evictionPolicy = value;
    }

    /**
     * Gets the value of the highUnits property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHighUnits() {
        return highUnits;
    }

    /**
     * Sets the value of the highUnits property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHighUnits(String value) {
        this.highUnits = value;
    }

    /**
     * Gets the value of the lowUnits property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLowUnits() {
        return lowUnits;
    }

    /**
     * Sets the value of the lowUnits property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLowUnits(String value) {
        this.lowUnits = value;
    }

    /**
     * Gets the value of the unitCalculator property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUnitCalculator() {
        return unitCalculator;
    }

    /**
     * Sets the value of the unitCalculator property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUnitCalculator(String value) {
        this.unitCalculator = value;
    }

    /**
     * Gets the value of the unitFactor property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUnitFactor() {
        return unitFactor;
    }

    /**
     * Sets the value of the unitFactor property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUnitFactor(String value) {
        this.unitFactor = value;
    }

    /**
     * Gets the value of the expiryDelay property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExpiryDelay() {
        return expiryDelay;
    }

    /**
     * Sets the value of the expiryDelay property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExpiryDelay(String value) {
        this.expiryDelay = value;
    }

    /**
     * Gets the value of the flushDelay property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFlushDelay() {
        return flushDelay;
    }

    /**
     * Sets the value of the flushDelay property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFlushDelay(String value) {
        this.flushDelay = value;
    }

    /**
     * Gets the value of the cachestoreScheme property.
     * 
     * @return
     *     possible object is
     *     {@link CachestoreScheme }
     *     
     */
    public CachestoreScheme getCachestoreScheme() {
        return cachestoreScheme;
    }

    /**
     * Sets the value of the cachestoreScheme property.
     * 
     * @param value
     *     allowed object is
     *     {@link CachestoreScheme }
     *     
     */
    public void setCachestoreScheme(CachestoreScheme value) {
        this.cachestoreScheme = value;
    }

    /**
     * Gets the value of the preLoad property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPreLoad() {
        return preLoad;
    }

    /**
     * Sets the value of the preLoad property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPreLoad(String value) {
        this.preLoad = value;
    }

    /**
     * Gets the value of the listener property.
     * 
     * @return
     *     possible object is
     *     {@link Listener }
     *     
     */
    public Listener getListener() {
        return listener;
    }

    /**
     * Sets the value of the listener property.
     * 
     * @param value
     *     allowed object is
     *     {@link Listener }
     *     
     */
    public void setListener(Listener value) {
        this.listener = value;
    }

}
