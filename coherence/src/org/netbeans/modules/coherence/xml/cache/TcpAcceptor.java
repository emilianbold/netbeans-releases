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
    "socketProvider",
    "localAddressOrAddressProvider",
    "reuseAddress",
    "keepAliveEnabled",
    "tcpDelayEnabled",
    "receiveBufferSize",
    "sendBufferSize",
    "listenBacklog",
    "lingerTimeout",
    "authorizedHosts",
    "suspectProtocolEnabled",
    "suspectBufferSize",
    "suspectBufferLength",
    "nominalBufferSize",
    "nominalBufferLength",
    "limitBufferSize",
    "limitBufferLength"
})
@XmlRootElement(name = "tcp-acceptor")
public class TcpAcceptor {

    @XmlElement(name = "socket-provider")
    protected SocketProvider socketProvider;
    @XmlElements({
        @XmlElement(name = "local-address", required = true, type = LocalAddress.class),
        @XmlElement(name = "address-provider", required = true, type = AddressProvider.class)
    })
    protected List<Object> localAddressOrAddressProvider;
    @XmlElement(name = "reuse-address")
    protected String reuseAddress;
    @XmlElement(name = "keep-alive-enabled")
    protected String keepAliveEnabled;
    @XmlElement(name = "tcp-delay-enabled")
    protected String tcpDelayEnabled;
    @XmlElement(name = "receive-buffer-size")
    protected String receiveBufferSize;
    @XmlElement(name = "send-buffer-size")
    protected String sendBufferSize;
    @XmlElement(name = "listen-backlog")
    protected String listenBacklog;
    @XmlElement(name = "linger-timeout")
    protected String lingerTimeout;
    @XmlElement(name = "authorized-hosts")
    protected AuthorizedHosts authorizedHosts;
    @XmlElement(name = "suspect-protocol-enabled")
    protected String suspectProtocolEnabled;
    @XmlElement(name = "suspect-buffer-size")
    protected String suspectBufferSize;
    @XmlElement(name = "suspect-buffer-length")
    protected String suspectBufferLength;
    @XmlElement(name = "nominal-buffer-size")
    protected String nominalBufferSize;
    @XmlElement(name = "nominal-buffer-length")
    protected String nominalBufferLength;
    @XmlElement(name = "limit-buffer-size")
    protected String limitBufferSize;
    @XmlElement(name = "limit-buffer-length")
    protected String limitBufferLength;

    /**
     * Gets the value of the socketProvider property.
     * 
     * @return
     *     possible object is
     *     {@link SocketProvider }
     *     
     */
    public SocketProvider getSocketProvider() {
        return socketProvider;
    }

    /**
     * Sets the value of the socketProvider property.
     * 
     * @param value
     *     allowed object is
     *     {@link SocketProvider }
     *     
     */
    public void setSocketProvider(SocketProvider value) {
        this.socketProvider = value;
    }

    /**
     * Gets the value of the localAddressOrAddressProvider property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the localAddressOrAddressProvider property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLocalAddressOrAddressProvider().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LocalAddress }
     * {@link AddressProvider }
     * 
     * 
     */
    public List<Object> getLocalAddressOrAddressProvider() {
        if (localAddressOrAddressProvider == null) {
            localAddressOrAddressProvider = new ArrayList<Object>();
        }
        return this.localAddressOrAddressProvider;
    }

    /**
     * Gets the value of the reuseAddress property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReuseAddress() {
        return reuseAddress;
    }

    /**
     * Sets the value of the reuseAddress property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReuseAddress(String value) {
        this.reuseAddress = value;
    }

    /**
     * Gets the value of the keepAliveEnabled property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getKeepAliveEnabled() {
        return keepAliveEnabled;
    }

    /**
     * Sets the value of the keepAliveEnabled property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setKeepAliveEnabled(String value) {
        this.keepAliveEnabled = value;
    }

    /**
     * Gets the value of the tcpDelayEnabled property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTcpDelayEnabled() {
        return tcpDelayEnabled;
    }

    /**
     * Sets the value of the tcpDelayEnabled property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTcpDelayEnabled(String value) {
        this.tcpDelayEnabled = value;
    }

    /**
     * Gets the value of the receiveBufferSize property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReceiveBufferSize() {
        return receiveBufferSize;
    }

    /**
     * Sets the value of the receiveBufferSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReceiveBufferSize(String value) {
        this.receiveBufferSize = value;
    }

    /**
     * Gets the value of the sendBufferSize property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSendBufferSize() {
        return sendBufferSize;
    }

    /**
     * Sets the value of the sendBufferSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSendBufferSize(String value) {
        this.sendBufferSize = value;
    }

    /**
     * Gets the value of the listenBacklog property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getListenBacklog() {
        return listenBacklog;
    }

    /**
     * Sets the value of the listenBacklog property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setListenBacklog(String value) {
        this.listenBacklog = value;
    }

    /**
     * Gets the value of the lingerTimeout property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLingerTimeout() {
        return lingerTimeout;
    }

    /**
     * Sets the value of the lingerTimeout property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLingerTimeout(String value) {
        this.lingerTimeout = value;
    }

    /**
     * Gets the value of the authorizedHosts property.
     * 
     * @return
     *     possible object is
     *     {@link AuthorizedHosts }
     *     
     */
    public AuthorizedHosts getAuthorizedHosts() {
        return authorizedHosts;
    }

    /**
     * Sets the value of the authorizedHosts property.
     * 
     * @param value
     *     allowed object is
     *     {@link AuthorizedHosts }
     *     
     */
    public void setAuthorizedHosts(AuthorizedHosts value) {
        this.authorizedHosts = value;
    }

    /**
     * Gets the value of the suspectProtocolEnabled property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSuspectProtocolEnabled() {
        return suspectProtocolEnabled;
    }

    /**
     * Sets the value of the suspectProtocolEnabled property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSuspectProtocolEnabled(String value) {
        this.suspectProtocolEnabled = value;
    }

    /**
     * Gets the value of the suspectBufferSize property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSuspectBufferSize() {
        return suspectBufferSize;
    }

    /**
     * Sets the value of the suspectBufferSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSuspectBufferSize(String value) {
        this.suspectBufferSize = value;
    }

    /**
     * Gets the value of the suspectBufferLength property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSuspectBufferLength() {
        return suspectBufferLength;
    }

    /**
     * Sets the value of the suspectBufferLength property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSuspectBufferLength(String value) {
        this.suspectBufferLength = value;
    }

    /**
     * Gets the value of the nominalBufferSize property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNominalBufferSize() {
        return nominalBufferSize;
    }

    /**
     * Sets the value of the nominalBufferSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNominalBufferSize(String value) {
        this.nominalBufferSize = value;
    }

    /**
     * Gets the value of the nominalBufferLength property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNominalBufferLength() {
        return nominalBufferLength;
    }

    /**
     * Sets the value of the nominalBufferLength property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNominalBufferLength(String value) {
        this.nominalBufferLength = value;
    }

    /**
     * Gets the value of the limitBufferSize property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLimitBufferSize() {
        return limitBufferSize;
    }

    /**
     * Sets the value of the limitBufferSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLimitBufferSize(String value) {
        this.limitBufferSize = value;
    }

    /**
     * Gets the value of the limitBufferLength property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLimitBufferLength() {
        return limitBufferLength;
    }

    /**
     * Sets the value of the limitBufferLength property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLimitBufferLength(String value) {
        this.limitBufferLength = value;
    }

}
