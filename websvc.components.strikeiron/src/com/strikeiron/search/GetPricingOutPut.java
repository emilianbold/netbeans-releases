/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package com.strikeiron.search;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for GetPricingOutPut complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetPricingOutPut">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.strikeiron.com}SIWsOutput">
 *       &lt;sequence>
 *         &lt;element name="Pricings" type="{http://www.strikeiron.com}ArrayOfPricingInformation" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetPricingOutPut", propOrder = {
    "pricings"
})
public class GetPricingOutPut
    extends SIWsOutput
{

    @XmlElement(name = "Pricings")
    protected ArrayOfPricingInformation pricings;

    /**
     * Gets the value of the pricings property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfPricingInformation }
     *     
     */
    public ArrayOfPricingInformation getPricings() {
        return pricings;
    }

    /**
     * Sets the value of the pricings property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfPricingInformation }
     *     
     */
    public void setPricings(ArrayOfPricingInformation value) {
        this.pricings = value;
    }

}
