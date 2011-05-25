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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
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
    "initParams",
    "asyncStoreManagerOrCustomStoreManagerOrLhFileManagerOrBdbStoreManagerOrNioFileManagerOrNioMemoryManager",
    "highUnits",
    "unitCalculator",
    "unitFactor",
    "expiryDelay",
    "listener"
})
@XmlRootElement(name = "external-scheme")
public class ExternalScheme {

    @XmlElement(name = "scheme-name")
    protected SchemeName schemeName;
    @XmlElement(name = "scheme-ref")
    protected String schemeRef;
    @XmlElement(name = "class-name")
    protected ClassName className;
    @XmlElement(name = "init-params")
    protected InitParams initParams;
    @XmlElements({
        @XmlElement(name = "async-store-manager", type = AsyncStoreManager.class),
        @XmlElement(name = "custom-store-manager", type = CustomStoreManager.class),
        @XmlElement(name = "lh-file-manager", type = LhFileManager.class),
        @XmlElement(name = "bdb-store-manager", type = BdbStoreManager.class),
        @XmlElement(name = "nio-file-manager", type = NioFileManager.class),
        @XmlElement(name = "nio-memory-manager", type = NioMemoryManager.class)
    })
    protected List<Object> asyncStoreManagerOrCustomStoreManagerOrLhFileManagerOrBdbStoreManagerOrNioFileManagerOrNioMemoryManager;
    @XmlElement(name = "high-units")
    protected String highUnits;
    @XmlElement(name = "unit-calculator")
    protected String unitCalculator;
    @XmlElement(name = "unit-factor")
    protected String unitFactor;
    @XmlElement(name = "expiry-delay")
    protected String expiryDelay;
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
     * Gets the value of the asyncStoreManagerOrCustomStoreManagerOrLhFileManagerOrBdbStoreManagerOrNioFileManagerOrNioMemoryManager property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the asyncStoreManagerOrCustomStoreManagerOrLhFileManagerOrBdbStoreManagerOrNioFileManagerOrNioMemoryManager property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAsyncStoreManagerOrCustomStoreManagerOrLhFileManagerOrBdbStoreManagerOrNioFileManagerOrNioMemoryManager().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AsyncStoreManager }
     * {@link CustomStoreManager }
     * {@link LhFileManager }
     * {@link BdbStoreManager }
     * {@link NioFileManager }
     * {@link NioMemoryManager }
     * 
     * 
     */
    public List<Object> getAsyncStoreManagerOrCustomStoreManagerOrLhFileManagerOrBdbStoreManagerOrNioFileManagerOrNioMemoryManager() {
        if (asyncStoreManagerOrCustomStoreManagerOrLhFileManagerOrBdbStoreManagerOrNioFileManagerOrNioMemoryManager == null) {
            asyncStoreManagerOrCustomStoreManagerOrLhFileManagerOrBdbStoreManagerOrNioFileManagerOrNioMemoryManager = new ArrayList<Object>();
        }
        return this.asyncStoreManagerOrCustomStoreManagerOrLhFileManagerOrBdbStoreManagerOrNioFileManagerOrNioMemoryManager;
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
