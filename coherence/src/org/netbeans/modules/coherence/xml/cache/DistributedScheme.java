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
    "serviceName",
    "serializer",
    "threadCount",
    "leaseGranularity",
    "localStorage",
    "partitionCount",
    "transferThreshold",
    "backupCount",
    "backupCountAfterWritebehind",
    "backupStorage",
    "keyAssociator",
    "keyPartitioning",
    "partitionListener",
    "taskHungThreshold",
    "taskTimeout",
    "requestTimeout",
    "guardianTimeout",
    "serviceFailurePolicy",
    "memberListener",
    "operationBundling",
    "backingMapScheme",
    "partitionedQuorumPolicyScheme",
    "listener",
    "autostart"
})
@XmlRootElement(name = "distributed-scheme")
public class DistributedScheme {

    @XmlElement(name = "scheme-name")
    protected SchemeName schemeName;
    @XmlElement(name = "scheme-ref")
    protected String schemeRef;
    @XmlElement(name = "service-name")
    protected String serviceName;
    protected Serializer serializer;
    @XmlElement(name = "thread-count")
    protected ThreadCount threadCount;
    @XmlElement(name = "lease-granularity")
    protected String leaseGranularity;
    @XmlElement(name = "local-storage")
    protected LocalStorage localStorage;
    @XmlElement(name = "partition-count")
    protected String partitionCount;
    @XmlElement(name = "transfer-threshold")
    protected String transferThreshold;
    @XmlElement(name = "backup-count")
    protected String backupCount;
    @XmlElement(name = "backup-count-after-writebehind")
    protected String backupCountAfterWritebehind;
    @XmlElement(name = "backup-storage")
    protected BackupStorage backupStorage;
    @XmlElement(name = "key-associator")
    protected KeyAssociator keyAssociator;
    @XmlElement(name = "key-partitioning")
    protected KeyPartitioning keyPartitioning;
    @XmlElement(name = "partition-listener")
    protected PartitionListener partitionListener;
    @XmlElement(name = "task-hung-threshold")
    protected String taskHungThreshold;
    @XmlElement(name = "task-timeout")
    protected String taskTimeout;
    @XmlElement(name = "request-timeout")
    protected String requestTimeout;
    @XmlElement(name = "guardian-timeout")
    protected String guardianTimeout;
    @XmlElement(name = "service-failure-policy")
    protected String serviceFailurePolicy;
    @XmlElement(name = "member-listener")
    protected MemberListener memberListener;
    @XmlElement(name = "operation-bundling")
    protected OperationBundling operationBundling;
    @XmlElement(name = "backing-map-scheme")
    protected BackingMapScheme backingMapScheme;
    @XmlElement(name = "partitioned-quorum-policy-scheme")
    protected PartitionedQuorumPolicyScheme partitionedQuorumPolicyScheme;
    protected Listener listener;
    protected Autostart autostart;

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
     * Gets the value of the serializer property.
     * 
     * @return
     *     possible object is
     *     {@link Serializer }
     *     
     */
    public Serializer getSerializer() {
        return serializer;
    }

    /**
     * Sets the value of the serializer property.
     * 
     * @param value
     *     allowed object is
     *     {@link Serializer }
     *     
     */
    public void setSerializer(Serializer value) {
        this.serializer = value;
    }

    /**
     * Gets the value of the threadCount property.
     * 
     * @return
     *     possible object is
     *     {@link ThreadCount }
     *     
     */
    public ThreadCount getThreadCount() {
        return threadCount;
    }

    /**
     * Sets the value of the threadCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link ThreadCount }
     *     
     */
    public void setThreadCount(ThreadCount value) {
        this.threadCount = value;
    }

    /**
     * Gets the value of the leaseGranularity property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLeaseGranularity() {
        return leaseGranularity;
    }

    /**
     * Sets the value of the leaseGranularity property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLeaseGranularity(String value) {
        this.leaseGranularity = value;
    }

    /**
     * Gets the value of the localStorage property.
     * 
     * @return
     *     possible object is
     *     {@link LocalStorage }
     *     
     */
    public LocalStorage getLocalStorage() {
        return localStorage;
    }

    /**
     * Sets the value of the localStorage property.
     * 
     * @param value
     *     allowed object is
     *     {@link LocalStorage }
     *     
     */
    public void setLocalStorage(LocalStorage value) {
        this.localStorage = value;
    }

    /**
     * Gets the value of the partitionCount property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPartitionCount() {
        return partitionCount;
    }

    /**
     * Sets the value of the partitionCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPartitionCount(String value) {
        this.partitionCount = value;
    }

    /**
     * Gets the value of the transferThreshold property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTransferThreshold() {
        return transferThreshold;
    }

    /**
     * Sets the value of the transferThreshold property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTransferThreshold(String value) {
        this.transferThreshold = value;
    }

    /**
     * Gets the value of the backupCount property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBackupCount() {
        return backupCount;
    }

    /**
     * Sets the value of the backupCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBackupCount(String value) {
        this.backupCount = value;
    }

    /**
     * Gets the value of the backupCountAfterWritebehind property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBackupCountAfterWritebehind() {
        return backupCountAfterWritebehind;
    }

    /**
     * Sets the value of the backupCountAfterWritebehind property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBackupCountAfterWritebehind(String value) {
        this.backupCountAfterWritebehind = value;
    }

    /**
     * Gets the value of the backupStorage property.
     * 
     * @return
     *     possible object is
     *     {@link BackupStorage }
     *     
     */
    public BackupStorage getBackupStorage() {
        return backupStorage;
    }

    /**
     * Sets the value of the backupStorage property.
     * 
     * @param value
     *     allowed object is
     *     {@link BackupStorage }
     *     
     */
    public void setBackupStorage(BackupStorage value) {
        this.backupStorage = value;
    }

    /**
     * Gets the value of the keyAssociator property.
     * 
     * @return
     *     possible object is
     *     {@link KeyAssociator }
     *     
     */
    public KeyAssociator getKeyAssociator() {
        return keyAssociator;
    }

    /**
     * Sets the value of the keyAssociator property.
     * 
     * @param value
     *     allowed object is
     *     {@link KeyAssociator }
     *     
     */
    public void setKeyAssociator(KeyAssociator value) {
        this.keyAssociator = value;
    }

    /**
     * Gets the value of the keyPartitioning property.
     * 
     * @return
     *     possible object is
     *     {@link KeyPartitioning }
     *     
     */
    public KeyPartitioning getKeyPartitioning() {
        return keyPartitioning;
    }

    /**
     * Sets the value of the keyPartitioning property.
     * 
     * @param value
     *     allowed object is
     *     {@link KeyPartitioning }
     *     
     */
    public void setKeyPartitioning(KeyPartitioning value) {
        this.keyPartitioning = value;
    }

    /**
     * Gets the value of the partitionListener property.
     * 
     * @return
     *     possible object is
     *     {@link PartitionListener }
     *     
     */
    public PartitionListener getPartitionListener() {
        return partitionListener;
    }

    /**
     * Sets the value of the partitionListener property.
     * 
     * @param value
     *     allowed object is
     *     {@link PartitionListener }
     *     
     */
    public void setPartitionListener(PartitionListener value) {
        this.partitionListener = value;
    }

    /**
     * Gets the value of the taskHungThreshold property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTaskHungThreshold() {
        return taskHungThreshold;
    }

    /**
     * Sets the value of the taskHungThreshold property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTaskHungThreshold(String value) {
        this.taskHungThreshold = value;
    }

    /**
     * Gets the value of the taskTimeout property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTaskTimeout() {
        return taskTimeout;
    }

    /**
     * Sets the value of the taskTimeout property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTaskTimeout(String value) {
        this.taskTimeout = value;
    }

    /**
     * Gets the value of the requestTimeout property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRequestTimeout() {
        return requestTimeout;
    }

    /**
     * Sets the value of the requestTimeout property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRequestTimeout(String value) {
        this.requestTimeout = value;
    }

    /**
     * Gets the value of the guardianTimeout property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGuardianTimeout() {
        return guardianTimeout;
    }

    /**
     * Sets the value of the guardianTimeout property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGuardianTimeout(String value) {
        this.guardianTimeout = value;
    }

    /**
     * Gets the value of the serviceFailurePolicy property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getServiceFailurePolicy() {
        return serviceFailurePolicy;
    }

    /**
     * Sets the value of the serviceFailurePolicy property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setServiceFailurePolicy(String value) {
        this.serviceFailurePolicy = value;
    }

    /**
     * Gets the value of the memberListener property.
     * 
     * @return
     *     possible object is
     *     {@link MemberListener }
     *     
     */
    public MemberListener getMemberListener() {
        return memberListener;
    }

    /**
     * Sets the value of the memberListener property.
     * 
     * @param value
     *     allowed object is
     *     {@link MemberListener }
     *     
     */
    public void setMemberListener(MemberListener value) {
        this.memberListener = value;
    }

    /**
     * Gets the value of the operationBundling property.
     * 
     * @return
     *     possible object is
     *     {@link OperationBundling }
     *     
     */
    public OperationBundling getOperationBundling() {
        return operationBundling;
    }

    /**
     * Sets the value of the operationBundling property.
     * 
     * @param value
     *     allowed object is
     *     {@link OperationBundling }
     *     
     */
    public void setOperationBundling(OperationBundling value) {
        this.operationBundling = value;
    }

    /**
     * Gets the value of the backingMapScheme property.
     * 
     * @return
     *     possible object is
     *     {@link BackingMapScheme }
     *     
     */
    public BackingMapScheme getBackingMapScheme() {
        return backingMapScheme;
    }

    /**
     * Sets the value of the backingMapScheme property.
     * 
     * @param value
     *     allowed object is
     *     {@link BackingMapScheme }
     *     
     */
    public void setBackingMapScheme(BackingMapScheme value) {
        this.backingMapScheme = value;
    }

    /**
     * Gets the value of the partitionedQuorumPolicyScheme property.
     * 
     * @return
     *     possible object is
     *     {@link PartitionedQuorumPolicyScheme }
     *     
     */
    public PartitionedQuorumPolicyScheme getPartitionedQuorumPolicyScheme() {
        return partitionedQuorumPolicyScheme;
    }

    /**
     * Sets the value of the partitionedQuorumPolicyScheme property.
     * 
     * @param value
     *     allowed object is
     *     {@link PartitionedQuorumPolicyScheme }
     *     
     */
    public void setPartitionedQuorumPolicyScheme(PartitionedQuorumPolicyScheme value) {
        this.partitionedQuorumPolicyScheme = value;
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

    /**
     * Gets the value of the autostart property.
     * 
     * @return
     *     possible object is
     *     {@link Autostart }
     *     
     */
    public Autostart getAutostart() {
        return autostart;
    }

    /**
     * Sets the value of the autostart property.
     * 
     * @param value
     *     allowed object is
     *     {@link Autostart }
     *     
     */
    public void setAutostart(Autostart value) {
        this.autostart = value;
    }

}
