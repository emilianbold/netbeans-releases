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
    "requestTimeout",
    "guardianTimeout",
    "serviceFailurePolicy",
    "memberListener",
    "backingMapScheme",
    "listener",
    "autostart"
})
@XmlRootElement(name = "optimistic-scheme")
public class OptimisticScheme {

    @XmlElement(name = "scheme-name")
    protected SchemeName schemeName;
    @XmlElement(name = "scheme-ref")
    protected String schemeRef;
    @XmlElement(name = "service-name")
    protected String serviceName;
    protected Serializer serializer;
    @XmlElement(name = "request-timeout")
    protected String requestTimeout;
    @XmlElement(name = "guardian-timeout")
    protected String guardianTimeout;
    @XmlElement(name = "service-failure-policy")
    protected String serviceFailurePolicy;
    @XmlElement(name = "member-listener")
    protected MemberListener memberListener;
    @XmlElement(name = "backing-map-scheme")
    protected BackingMapScheme backingMapScheme;
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
