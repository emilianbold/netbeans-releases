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
    "packetSize",
    "packetPool",
    "packetDelivery",
    "notificationQueueing",
    "trafficJam",
    "packetBuffer",
    "priority",
    "enabled"
})
@XmlRootElement(name = "packet-publisher")
public class PacketPublisher {

    @XmlAttribute(name = "xml-override")
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String xmlOverride;
    @XmlElement(name = "packet-size")
    protected PacketSize packetSize;
    @XmlElement(name = "packet-pool", required = true)
    protected PacketPool packetPool;
    @XmlElement(name = "packet-delivery", required = true)
    protected PacketDelivery packetDelivery;
    @XmlElement(name = "notification-queueing", required = true)
    protected NotificationQueueing notificationQueueing;
    @XmlElement(name = "traffic-jam", required = true)
    protected TrafficJam trafficJam;
    @XmlElement(name = "packet-buffer", required = true)
    protected PacketBuffer packetBuffer;
    @XmlElement(required = true)
    protected Priority priority;
    protected Enabled enabled;

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
     * Gets the value of the packetSize property.
     * 
     * @return
     *     possible object is
     *     {@link PacketSize }
     *     
     */
    public PacketSize getPacketSize() {
        return packetSize;
    }

    /**
     * Sets the value of the packetSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link PacketSize }
     *     
     */
    public void setPacketSize(PacketSize value) {
        this.packetSize = value;
    }

    /**
     * Gets the value of the packetPool property.
     * 
     * @return
     *     possible object is
     *     {@link PacketPool }
     *     
     */
    public PacketPool getPacketPool() {
        return packetPool;
    }

    /**
     * Sets the value of the packetPool property.
     * 
     * @param value
     *     allowed object is
     *     {@link PacketPool }
     *     
     */
    public void setPacketPool(PacketPool value) {
        this.packetPool = value;
    }

    /**
     * Gets the value of the packetDelivery property.
     * 
     * @return
     *     possible object is
     *     {@link PacketDelivery }
     *     
     */
    public PacketDelivery getPacketDelivery() {
        return packetDelivery;
    }

    /**
     * Sets the value of the packetDelivery property.
     * 
     * @param value
     *     allowed object is
     *     {@link PacketDelivery }
     *     
     */
    public void setPacketDelivery(PacketDelivery value) {
        this.packetDelivery = value;
    }

    /**
     * Gets the value of the notificationQueueing property.
     * 
     * @return
     *     possible object is
     *     {@link NotificationQueueing }
     *     
     */
    public NotificationQueueing getNotificationQueueing() {
        return notificationQueueing;
    }

    /**
     * Sets the value of the notificationQueueing property.
     * 
     * @param value
     *     allowed object is
     *     {@link NotificationQueueing }
     *     
     */
    public void setNotificationQueueing(NotificationQueueing value) {
        this.notificationQueueing = value;
    }

    /**
     * Gets the value of the trafficJam property.
     * 
     * @return
     *     possible object is
     *     {@link TrafficJam }
     *     
     */
    public TrafficJam getTrafficJam() {
        return trafficJam;
    }

    /**
     * Sets the value of the trafficJam property.
     * 
     * @param value
     *     allowed object is
     *     {@link TrafficJam }
     *     
     */
    public void setTrafficJam(TrafficJam value) {
        this.trafficJam = value;
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
     * Gets the value of the enabled property.
     * 
     * @return
     *     possible object is
     *     {@link Enabled }
     *     
     */
    public Enabled getEnabled() {
        return enabled;
    }

    /**
     * Sets the value of the enabled property.
     * 
     * @param value
     *     allowed object is
     *     {@link Enabled }
     *     
     */
    public void setEnabled(Enabled value) {
        this.enabled = value;
    }

}
