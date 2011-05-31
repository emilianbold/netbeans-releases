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
    "_interface",
    "address",
    "port",
    "timeToLive",
    "packetBuffer",
    "priority",
    "joinTimeoutMilliseconds",
    "multicastThresholdPercent"
})
@XmlRootElement(name = "multicast-listener")
public class MulticastListener {

    @XmlAttribute(name = "xml-override")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String xmlOverride;
    @XmlElement(name = "interface")
    protected Interface _interface;
    @XmlElement(required = true)
    protected Address address;
    @XmlElement(required = true)
    protected Port port;
    @XmlElement(name = "time-to-live", required = true)
    protected TimeToLive timeToLive;
    @XmlElement(name = "packet-buffer", required = true)
    protected PacketBuffer packetBuffer;
    @XmlElement(required = true)
    protected Priority priority;
    @XmlElement(name = "join-timeout-milliseconds", required = true)
    protected JoinTimeoutMilliseconds joinTimeoutMilliseconds;
    @XmlElement(name = "multicast-threshold-percent", required = true)
    protected MulticastThresholdPercent multicastThresholdPercent;

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
     * Gets the value of the interface property.
     * 
     * @return
     *     possible object is
     *     {@link Interface }
     *     
     */
    public Interface getInterface() {
        return _interface;
    }

    /**
     * Sets the value of the interface property.
     * 
     * @param value
     *     allowed object is
     *     {@link Interface }
     *     
     */
    public void setInterface(Interface value) {
        this._interface = value;
    }

    /**
     * Gets the value of the address property.
     * 
     * @return
     *     possible object is
     *     {@link Address }
     *     
     */
    public Address getAddress() {
        return address;
    }

    /**
     * Sets the value of the address property.
     * 
     * @param value
     *     allowed object is
     *     {@link Address }
     *     
     */
    public void setAddress(Address value) {
        this.address = value;
    }

    /**
     * Gets the value of the port property.
     * 
     * @return
     *     possible object is
     *     {@link Port }
     *     
     */
    public Port getPort() {
        return port;
    }

    /**
     * Sets the value of the port property.
     * 
     * @param value
     *     allowed object is
     *     {@link Port }
     *     
     */
    public void setPort(Port value) {
        this.port = value;
    }

    /**
     * Gets the value of the timeToLive property.
     * 
     * @return
     *     possible object is
     *     {@link TimeToLive }
     *     
     */
    public TimeToLive getTimeToLive() {
        return timeToLive;
    }

    /**
     * Sets the value of the timeToLive property.
     * 
     * @param value
     *     allowed object is
     *     {@link TimeToLive }
     *     
     */
    public void setTimeToLive(TimeToLive value) {
        this.timeToLive = value;
    }

    /**
     * Gets the value of the packetBuffer property.
     * 
     * @return
     *     possible object is
     *     {@link PacketBuffer }
     *     
     */
    public PacketBuffer getPacketBuffer() {
        return packetBuffer;
    }

    /**
     * Sets the value of the packetBuffer property.
     * 
     * @param value
     *     allowed object is
     *     {@link PacketBuffer }
     *     
     */
    public void setPacketBuffer(PacketBuffer value) {
        this.packetBuffer = value;
    }

    /**
     * Gets the value of the priority property.
     * 
     * @return
     *     possible object is
     *     {@link Priority }
     *     
     */
    public Priority getPriority() {
        return priority;
    }

    /**
     * Sets the value of the priority property.
     * 
     * @param value
     *     allowed object is
     *     {@link Priority }
     *     
     */
    public void setPriority(Priority value) {
        this.priority = value;
    }

    /**
     * Gets the value of the joinTimeoutMilliseconds property.
     * 
     * @return
     *     possible object is
     *     {@link JoinTimeoutMilliseconds }
     *     
     */
    public JoinTimeoutMilliseconds getJoinTimeoutMilliseconds() {
        return joinTimeoutMilliseconds;
    }

    /**
     * Sets the value of the joinTimeoutMilliseconds property.
     * 
     * @param value
     *     allowed object is
     *     {@link JoinTimeoutMilliseconds }
     *     
     */
    public void setJoinTimeoutMilliseconds(JoinTimeoutMilliseconds value) {
        this.joinTimeoutMilliseconds = value;
    }

    /**
     * Gets the value of the multicastThresholdPercent property.
     * 
     * @return
     *     possible object is
     *     {@link MulticastThresholdPercent }
     *     
     */
    public MulticastThresholdPercent getMulticastThresholdPercent() {
        return multicastThresholdPercent;
    }

    /**
     * Sets the value of the multicastThresholdPercent property.
     * 
     * @param value
     *     allowed object is
     *     {@link MulticastThresholdPercent }
     *     
     */
    public void setMulticastThresholdPercent(MulticastThresholdPercent value) {
        this.multicastThresholdPercent = value;
    }

}
