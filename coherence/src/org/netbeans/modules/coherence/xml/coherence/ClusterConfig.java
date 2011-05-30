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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "memberIdentity",
    "unicastListener",
    "multicastListener",
    "tcpRingListener",
    "shutdownListener",
    "serviceGuardian",
    "packetSpeaker",
    "packetPublisher",
    "incomingMessageHandler",
    "outgoingMessageHandler",
    "authorizedHosts",
    "services",
    "filters",
    "serializers",
    "socketProviders",
    "clusterQuorumPolicy"
})
@XmlRootElement(name = "cluster-config")
public class ClusterConfig {

    @XmlAttribute(name = "xml-override")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String xmlOverride;
    @XmlElement(name = "member-identity")
    protected MemberIdentity memberIdentity;
    @XmlElement(name = "unicast-listener", required = true)
    protected UnicastListener unicastListener;
    @XmlElement(name = "multicast-listener", required = true)
    protected MulticastListener multicastListener;
    @XmlElement(name = "tcp-ring-listener", required = true)
    protected TcpRingListener tcpRingListener;
    @XmlElement(name = "shutdown-listener", required = true)
    protected ShutdownListener shutdownListener;
    @XmlElement(name = "service-guardian", required = true)
    protected ServiceGuardian serviceGuardian;
    @XmlElement(name = "packet-speaker", required = true)
    protected PacketSpeaker packetSpeaker;
    @XmlElement(name = "packet-publisher", required = true)
    protected PacketPublisher packetPublisher;
    @XmlElement(name = "incoming-message-handler", required = true)
    protected IncomingMessageHandler incomingMessageHandler;
    @XmlElement(name = "outgoing-message-handler", required = true)
    protected OutgoingMessageHandler outgoingMessageHandler;
    @XmlElement(name = "authorized-hosts", required = true)
    protected AuthorizedHosts authorizedHosts;
    @XmlElement(required = true)
    protected Services services;
    @XmlElement(required = true)
    protected Filters filters;
    @XmlElement(required = true)
    protected Serializers serializers;
    @XmlElement(name = "socket-providers", required = true)
    protected SocketProviders socketProviders;
    @XmlElement(name = "cluster-quorum-policy")
    protected ClusterQuorumPolicy clusterQuorumPolicy;

    /**
     * Gets the value of the xmlOverride property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getXmlOverride() {
        return xmlOverride;
    }

    /**
     * Sets the value of the xmlOverride property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setXmlOverride(String value) {
        this.xmlOverride = value;
    }

    /**
     * Gets the value of the memberIdentity property.
     * 
     * @return
     *     possible object is
     *     {@link MemberIdentity }
     *     
     */
    public MemberIdentity getMemberIdentity() {
        return memberIdentity;
    }

    /**
     * Sets the value of the memberIdentity property.
     * 
     * @param value
     *     allowed object is
     *     {@link MemberIdentity }
     *     
     */
    public void setMemberIdentity(MemberIdentity value) {
        this.memberIdentity = value;
    }

    /**
     * Gets the value of the unicastListener property.
     * 
     * @return
     *     possible object is
     *     {@link UnicastListener }
     *     
     */
    public UnicastListener getUnicastListener() {
        return unicastListener;
    }

    /**
     * Sets the value of the unicastListener property.
     * 
     * @param value
     *     allowed object is
     *     {@link UnicastListener }
     *     
     */
    public void setUnicastListener(UnicastListener value) {
        this.unicastListener = value;
    }

    /**
     * Gets the value of the multicastListener property.
     * 
     * @return
     *     possible object is
     *     {@link MulticastListener }
     *     
     */
    public MulticastListener getMulticastListener() {
        return multicastListener;
    }

    /**
     * Sets the value of the multicastListener property.
     * 
     * @param value
     *     allowed object is
     *     {@link MulticastListener }
     *     
     */
    public void setMulticastListener(MulticastListener value) {
        this.multicastListener = value;
    }

    /**
     * Gets the value of the tcpRingListener property.
     * 
     * @return
     *     possible object is
     *     {@link TcpRingListener }
     *     
     */
    public TcpRingListener getTcpRingListener() {
        return tcpRingListener;
    }

    /**
     * Sets the value of the tcpRingListener property.
     * 
     * @param value
     *     allowed object is
     *     {@link TcpRingListener }
     *     
     */
    public void setTcpRingListener(TcpRingListener value) {
        this.tcpRingListener = value;
    }

    /**
     * Gets the value of the shutdownListener property.
     * 
     * @return
     *     possible object is
     *     {@link ShutdownListener }
     *     
     */
    public ShutdownListener getShutdownListener() {
        return shutdownListener;
    }

    /**
     * Sets the value of the shutdownListener property.
     * 
     * @param value
     *     allowed object is
     *     {@link ShutdownListener }
     *     
     */
    public void setShutdownListener(ShutdownListener value) {
        this.shutdownListener = value;
    }

    /**
     * Gets the value of the serviceGuardian property.
     * 
     * @return
     *     possible object is
     *     {@link ServiceGuardian }
     *     
     */
    public ServiceGuardian getServiceGuardian() {
        return serviceGuardian;
    }

    /**
     * Sets the value of the serviceGuardian property.
     * 
     * @param value
     *     allowed object is
     *     {@link ServiceGuardian }
     *     
     */
    public void setServiceGuardian(ServiceGuardian value) {
        this.serviceGuardian = value;
    }

    /**
     * Gets the value of the packetSpeaker property.
     * 
     * @return
     *     possible object is
     *     {@link PacketSpeaker }
     *     
     */
    public PacketSpeaker getPacketSpeaker() {
        return packetSpeaker;
    }

    /**
     * Sets the value of the packetSpeaker property.
     * 
     * @param value
     *     allowed object is
     *     {@link PacketSpeaker }
     *     
     */
    public void setPacketSpeaker(PacketSpeaker value) {
        this.packetSpeaker = value;
    }

    /**
     * Gets the value of the packetPublisher property.
     * 
     * @return
     *     possible object is
     *     {@link PacketPublisher }
     *     
     */
    public PacketPublisher getPacketPublisher() {
        return packetPublisher;
    }

    /**
     * Sets the value of the packetPublisher property.
     * 
     * @param value
     *     allowed object is
     *     {@link PacketPublisher }
     *     
     */
    public void setPacketPublisher(PacketPublisher value) {
        this.packetPublisher = value;
    }

    /**
     * Gets the value of the incomingMessageHandler property.
     * 
     * @return
     *     possible object is
     *     {@link IncomingMessageHandler }
     *     
     */
    public IncomingMessageHandler getIncomingMessageHandler() {
        return incomingMessageHandler;
    }

    /**
     * Sets the value of the incomingMessageHandler property.
     * 
     * @param value
     *     allowed object is
     *     {@link IncomingMessageHandler }
     *     
     */
    public void setIncomingMessageHandler(IncomingMessageHandler value) {
        this.incomingMessageHandler = value;
    }

    /**
     * Gets the value of the outgoingMessageHandler property.
     * 
     * @return
     *     possible object is
     *     {@link OutgoingMessageHandler }
     *     
     */
    public OutgoingMessageHandler getOutgoingMessageHandler() {
        return outgoingMessageHandler;
    }

    /**
     * Sets the value of the outgoingMessageHandler property.
     * 
     * @param value
     *     allowed object is
     *     {@link OutgoingMessageHandler }
     *     
     */
    public void setOutgoingMessageHandler(OutgoingMessageHandler value) {
        this.outgoingMessageHandler = value;
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
     * Gets the value of the services property.
     * 
     * @return
     *     possible object is
     *     {@link Services }
     *     
     */
    public Services getServices() {
        return services;
    }

    /**
     * Sets the value of the services property.
     * 
     * @param value
     *     allowed object is
     *     {@link Services }
     *     
     */
    public void setServices(Services value) {
        this.services = value;
    }

    /**
     * Gets the value of the filters property.
     * 
     * @return
     *     possible object is
     *     {@link Filters }
     *     
     */
    public Filters getFilters() {
        return filters;
    }

    /**
     * Sets the value of the filters property.
     * 
     * @param value
     *     allowed object is
     *     {@link Filters }
     *     
     */
    public void setFilters(Filters value) {
        this.filters = value;
    }

    /**
     * Gets the value of the serializers property.
     * 
     * @return
     *     possible object is
     *     {@link Serializers }
     *     
     */
    public Serializers getSerializers() {
        return serializers;
    }

    /**
     * Sets the value of the serializers property.
     * 
     * @param value
     *     allowed object is
     *     {@link Serializers }
     *     
     */
    public void setSerializers(Serializers value) {
        this.serializers = value;
    }

    /**
     * Gets the value of the socketProviders property.
     * 
     * @return
     *     possible object is
     *     {@link SocketProviders }
     *     
     */
    public SocketProviders getSocketProviders() {
        return socketProviders;
    }

    /**
     * Sets the value of the socketProviders property.
     * 
     * @param value
     *     allowed object is
     *     {@link SocketProviders }
     *     
     */
    public void setSocketProviders(SocketProviders value) {
        this.socketProviders = value;
    }

    /**
     * Gets the value of the clusterQuorumPolicy property.
     * 
     * @return
     *     possible object is
     *     {@link ClusterQuorumPolicy }
     *     
     */
    public ClusterQuorumPolicy getClusterQuorumPolicy() {
        return clusterQuorumPolicy;
    }

    /**
     * Sets the value of the clusterQuorumPolicy property.
     * 
     * @param value
     *     allowed object is
     *     {@link ClusterQuorumPolicy }
     *     
     */
    public void setClusterQuorumPolicy(ClusterQuorumPolicy value) {
        this.clusterQuorumPolicy = value;
    }

}
