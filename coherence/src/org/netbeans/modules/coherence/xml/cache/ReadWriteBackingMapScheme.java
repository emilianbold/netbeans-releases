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
    "internalCacheScheme",
    "writeMaxBatchSize",
    "missCacheScheme",
    "cachestoreScheme",
    "readOnly",
    "writeDelayOrWriteDelaySeconds",
    "writeBatchFactor",
    "writeRequeueThreshold",
    "refreshAheadFactor",
    "cachestoreTimeout",
    "rollbackCachestoreFailures",
    "listener"
})
@XmlRootElement(name = "read-write-backing-map-scheme")
public class ReadWriteBackingMapScheme {

    @XmlElement(name = "scheme-name")
    protected SchemeName schemeName;
    @XmlElement(name = "scheme-ref")
    protected String schemeRef;
    @XmlElement(name = "class-name")
    protected ClassName className;
    @XmlElement(name = "init-params")
    protected InitParams initParams;
    @XmlElement(name = "internal-cache-scheme")
    protected InternalCacheScheme internalCacheScheme;
    @XmlElement(name = "write-max-batch-size")
    protected String writeMaxBatchSize;
    @XmlElement(name = "miss-cache-scheme")
    protected MissCacheScheme missCacheScheme;
    @XmlElement(name = "cachestore-scheme")
    protected CachestoreScheme cachestoreScheme;
    @XmlElement(name = "read-only")
    protected String readOnly;
    @XmlElements({
        @XmlElement(name = "write-delay", type = WriteDelay.class),
        @XmlElement(name = "write-delay-seconds", type = WriteDelaySeconds.class)
    })
    protected List<Object> writeDelayOrWriteDelaySeconds;
    @XmlElement(name = "write-batch-factor")
    protected String writeBatchFactor;
    @XmlElement(name = "write-requeue-threshold")
    protected String writeRequeueThreshold;
    @XmlElement(name = "refresh-ahead-factor")
    protected String refreshAheadFactor;
    @XmlElement(name = "cachestore-timeout")
    protected String cachestoreTimeout;
    @XmlElement(name = "rollback-cachestore-failures")
    protected String rollbackCachestoreFailures;
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
     * Gets the value of the internalCacheScheme property.
     * 
     * @return
     *     possible object is
     *     {@link InternalCacheScheme }
     *     
     */
    public InternalCacheScheme getInternalCacheScheme() {
        return internalCacheScheme;
    }

    /**
     * Sets the value of the internalCacheScheme property.
     * 
     * @param value
     *     allowed object is
     *     {@link InternalCacheScheme }
     *     
     */
    public void setInternalCacheScheme(InternalCacheScheme value) {
        this.internalCacheScheme = value;
    }

    /**
     * Gets the value of the writeMaxBatchSize property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWriteMaxBatchSize() {
        return writeMaxBatchSize;
    }

    /**
     * Sets the value of the writeMaxBatchSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWriteMaxBatchSize(String value) {
        this.writeMaxBatchSize = value;
    }

    /**
     * Gets the value of the missCacheScheme property.
     * 
     * @return
     *     possible object is
     *     {@link MissCacheScheme }
     *     
     */
    public MissCacheScheme getMissCacheScheme() {
        return missCacheScheme;
    }

    /**
     * Sets the value of the missCacheScheme property.
     * 
     * @param value
     *     allowed object is
     *     {@link MissCacheScheme }
     *     
     */
    public void setMissCacheScheme(MissCacheScheme value) {
        this.missCacheScheme = value;
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
     * Gets the value of the readOnly property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReadOnly() {
        return readOnly;
    }

    /**
     * Sets the value of the readOnly property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReadOnly(String value) {
        this.readOnly = value;
    }

    /**
     * Gets the value of the writeDelayOrWriteDelaySeconds property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the writeDelayOrWriteDelaySeconds property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getWriteDelayOrWriteDelaySeconds().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link WriteDelay }
     * {@link WriteDelaySeconds }
     * 
     * 
     */
    public List<Object> getWriteDelayOrWriteDelaySeconds() {
        if (writeDelayOrWriteDelaySeconds == null) {
            writeDelayOrWriteDelaySeconds = new ArrayList<Object>();
        }
        return this.writeDelayOrWriteDelaySeconds;
    }

    /**
     * Gets the value of the writeBatchFactor property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWriteBatchFactor() {
        return writeBatchFactor;
    }

    /**
     * Sets the value of the writeBatchFactor property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWriteBatchFactor(String value) {
        this.writeBatchFactor = value;
    }

    /**
     * Gets the value of the writeRequeueThreshold property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWriteRequeueThreshold() {
        return writeRequeueThreshold;
    }

    /**
     * Sets the value of the writeRequeueThreshold property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWriteRequeueThreshold(String value) {
        this.writeRequeueThreshold = value;
    }

    /**
     * Gets the value of the refreshAheadFactor property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRefreshAheadFactor() {
        return refreshAheadFactor;
    }

    /**
     * Sets the value of the refreshAheadFactor property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRefreshAheadFactor(String value) {
        this.refreshAheadFactor = value;
    }

    /**
     * Gets the value of the cachestoreTimeout property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCachestoreTimeout() {
        return cachestoreTimeout;
    }

    /**
     * Sets the value of the cachestoreTimeout property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCachestoreTimeout(String value) {
        this.cachestoreTimeout = value;
    }

    /**
     * Gets the value of the rollbackCachestoreFailures property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRollbackCachestoreFailures() {
        return rollbackCachestoreFailures;
    }

    /**
     * Sets the value of the rollbackCachestoreFailures property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRollbackCachestoreFailures(String value) {
        this.rollbackCachestoreFailures = value;
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
