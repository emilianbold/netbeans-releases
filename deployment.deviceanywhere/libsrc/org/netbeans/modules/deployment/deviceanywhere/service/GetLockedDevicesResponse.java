/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.deployment.deviceanywhere.service;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="getLockedDevicesReturn" type="{http://services.mc.com}ApplicationAPI_GetLockedDevicesReturn"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "getLockedDevicesReturn"
})
@XmlRootElement(name = "getLockedDevicesResponse")
public class GetLockedDevicesResponse {

    @XmlElement(required = true)
    protected ApplicationAPIGetLockedDevicesReturn getLockedDevicesReturn;

    /**
     * Gets the value of the getLockedDevicesReturn property.
     * 
     * @return
     *     possible object is
     *     {@link ApplicationAPIGetLockedDevicesReturn }
     *     
     */
    public ApplicationAPIGetLockedDevicesReturn getGetLockedDevicesReturn() {
        return getLockedDevicesReturn;
    }

    /**
     * Sets the value of the getLockedDevicesReturn property.
     * 
     * @param value
     *     allowed object is
     *     {@link ApplicationAPIGetLockedDevicesReturn }
     *     
     */
    public void setGetLockedDevicesReturn(ApplicationAPIGetLockedDevicesReturn value) {
        this.getLockedDevicesReturn = value;
    }

}
