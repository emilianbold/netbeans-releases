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
package org.netbeans.modules.coherence.xml.coherence;

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
    "managedNodes",
    "allowRemoteManagement",
    "refreshPolicy",
    "refreshExpiry",
    "refreshTimeout",
    "readOnly",
    "defaultDomainName",
    "serviceName",
    "serverFactory",
    "mbeans",
    "mbeanFilter",
    "reporter"
})
@XmlRootElement(name = "management-config")
public class ManagementConfig {

    @XmlElement(name = "managed-nodes", required = true)
    protected ManagedNodes managedNodes;
    @XmlElement(name = "allow-remote-management", required = true)
    protected AllowRemoteManagement allowRemoteManagement;
    @XmlElement(name = "refresh-policy")
    protected RefreshPolicy refreshPolicy;
    @XmlElement(name = "refresh-expiry")
    protected RefreshExpiry refreshExpiry;
    @XmlElement(name = "refresh-timeout")
    protected RefreshTimeout refreshTimeout;
    @XmlElement(name = "read-only")
    protected ReadOnly readOnly;
    @XmlElement(name = "default-domain-name")
    protected DefaultDomainName defaultDomainName;
    @XmlElement(name = "service-name")
    protected ServiceName serviceName;
    @XmlElement(name = "server-factory")
    protected ServerFactory serverFactory;
    protected Mbeans mbeans;
    @XmlElement(name = "mbean-filter")
    protected MbeanFilter mbeanFilter;
    protected Reporter reporter;

    /**
     * Gets the value of the managedNodes property.
     * 
     * @return
     *     possible object is
     *     {@link ManagedNodes }
     *     
     */
    public ManagedNodes getManagedNodes() {
        return managedNodes;
    }

    /**
     * Sets the value of the managedNodes property.
     * 
     * @param value
     *     allowed object is
     *     {@link ManagedNodes }
     *     
     */
    public void setManagedNodes(ManagedNodes value) {
        this.managedNodes = value;
    }

    /**
     * Gets the value of the allowRemoteManagement property.
     * 
     * @return
     *     possible object is
     *     {@link AllowRemoteManagement }
     *     
     */
    public AllowRemoteManagement getAllowRemoteManagement() {
        return allowRemoteManagement;
    }

    /**
     * Sets the value of the allowRemoteManagement property.
     * 
     * @param value
     *     allowed object is
     *     {@link AllowRemoteManagement }
     *     
     */
    public void setAllowRemoteManagement(AllowRemoteManagement value) {
        this.allowRemoteManagement = value;
    }

    /**
     * Gets the value of the refreshPolicy property.
     * 
     * @return
     *     possible object is
     *     {@link RefreshPolicy }
     *     
     */
    public RefreshPolicy getRefreshPolicy() {
        return refreshPolicy;
    }

    /**
     * Sets the value of the refreshPolicy property.
     * 
     * @param value
     *     allowed object is
     *     {@link RefreshPolicy }
     *     
     */
    public void setRefreshPolicy(RefreshPolicy value) {
        this.refreshPolicy = value;
    }

    /**
     * Gets the value of the refreshExpiry property.
     * 
     * @return
     *     possible object is
     *     {@link RefreshExpiry }
     *     
     */
    public RefreshExpiry getRefreshExpiry() {
        return refreshExpiry;
    }

    /**
     * Sets the value of the refreshExpiry property.
     * 
     * @param value
     *     allowed object is
     *     {@link RefreshExpiry }
     *     
     */
    public void setRefreshExpiry(RefreshExpiry value) {
        this.refreshExpiry = value;
    }

    /**
     * Gets the value of the refreshTimeout property.
     * 
     * @return
     *     possible object is
     *     {@link RefreshTimeout }
     *     
     */
    public RefreshTimeout getRefreshTimeout() {
        return refreshTimeout;
    }

    /**
     * Sets the value of the refreshTimeout property.
     * 
     * @param value
     *     allowed object is
     *     {@link RefreshTimeout }
     *     
     */
    public void setRefreshTimeout(RefreshTimeout value) {
        this.refreshTimeout = value;
    }

    /**
     * Gets the value of the readOnly property.
     * 
     * @return
     *     possible object is
     *     {@link ReadOnly }
     *     
     */
    public ReadOnly getReadOnly() {
        return readOnly;
    }

    /**
     * Sets the value of the readOnly property.
     * 
     * @param value
     *     allowed object is
     *     {@link ReadOnly }
     *     
     */
    public void setReadOnly(ReadOnly value) {
        this.readOnly = value;
    }

    /**
     * Gets the value of the defaultDomainName property.
     * 
     * @return
     *     possible object is
     *     {@link DefaultDomainName }
     *     
     */
    public DefaultDomainName getDefaultDomainName() {
        return defaultDomainName;
    }

    /**
     * Sets the value of the defaultDomainName property.
     * 
     * @param value
     *     allowed object is
     *     {@link DefaultDomainName }
     *     
     */
    public void setDefaultDomainName(DefaultDomainName value) {
        this.defaultDomainName = value;
    }

    /**
     * Gets the value of the serviceName property.
     * 
     * @return
     *     possible object is
     *     {@link ServiceName }
     *     
     */
    public ServiceName getServiceName() {
        return serviceName;
    }

    /**
     * Sets the value of the serviceName property.
     * 
     * @param value
     *     allowed object is
     *     {@link ServiceName }
     *     
     */
    public void setServiceName(ServiceName value) {
        this.serviceName = value;
    }

    /**
     * Gets the value of the serverFactory property.
     * 
     * @return
     *     possible object is
     *     {@link ServerFactory }
     *     
     */
    public ServerFactory getServerFactory() {
        return serverFactory;
    }

    /**
     * Sets the value of the serverFactory property.
     * 
     * @param value
     *     allowed object is
     *     {@link ServerFactory }
     *     
     */
    public void setServerFactory(ServerFactory value) {
        this.serverFactory = value;
    }

    /**
     * Gets the value of the mbeans property.
     * 
     * @return
     *     possible object is
     *     {@link Mbeans }
     *     
     */
    public Mbeans getMbeans() {
        return mbeans;
    }

    /**
     * Sets the value of the mbeans property.
     * 
     * @param value
     *     allowed object is
     *     {@link Mbeans }
     *     
     */
    public void setMbeans(Mbeans value) {
        this.mbeans = value;
    }

    /**
     * Gets the value of the mbeanFilter property.
     * 
     * @return
     *     possible object is
     *     {@link MbeanFilter }
     *     
     */
    public MbeanFilter getMbeanFilter() {
        return mbeanFilter;
    }

    /**
     * Sets the value of the mbeanFilter property.
     * 
     * @param value
     *     allowed object is
     *     {@link MbeanFilter }
     *     
     */
    public void setMbeanFilter(MbeanFilter value) {
        this.mbeanFilter = value;
    }

    /**
     * Gets the value of the reporter property.
     * 
     * @return
     *     possible object is
     *     {@link Reporter }
     *     
     */
    public Reporter getReporter() {
        return reporter;
    }

    /**
     * Sets the value of the reporter property.
     * 
     * @param value
     *     allowed object is
     *     {@link Reporter }
     *     
     */
    public void setReporter(Reporter value) {
        this.reporter = value;
    }

}
