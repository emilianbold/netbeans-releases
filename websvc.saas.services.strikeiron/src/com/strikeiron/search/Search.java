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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package com.strikeiron.search;

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
 *         &lt;element name="SearchTerm" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="SortBy" type="{http://www.strikeiron.com}SORT_BY"/>
 *         &lt;element name="UseCustomWSDL" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="AuthenticationStyle" type="{http://www.strikeiron.com}AUTHENTICATION_STYLE"/>
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
    "searchTerm",
    "sortBy",
    "useCustomWSDL",
    "authenticationStyle"
})
@XmlRootElement(name = "Search")
public class Search {

    @XmlElement(name = "SearchTerm")
    protected String searchTerm;
    @XmlElement(name = "SortBy", required = true)
    protected SORTBY sortBy;
    @XmlElement(name = "UseCustomWSDL")
    protected boolean useCustomWSDL;
    @XmlElement(name = "AuthenticationStyle", required = true)
    protected AUTHENTICATIONSTYLE authenticationStyle;

    /**
     * Gets the value of the searchTerm property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSearchTerm() {
        return searchTerm;
    }

    /**
     * Sets the value of the searchTerm property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSearchTerm(String value) {
        this.searchTerm = value;
    }

    /**
     * Gets the value of the sortBy property.
     * 
     * @return
     *     possible object is
     *     {@link SORTBY }
     *     
     */
    public SORTBY getSortBy() {
        return sortBy;
    }

    /**
     * Sets the value of the sortBy property.
     * 
     * @param value
     *     allowed object is
     *     {@link SORTBY }
     *     
     */
    public void setSortBy(SORTBY value) {
        this.sortBy = value;
    }

    /**
     * Gets the value of the useCustomWSDL property.
     * 
     */
    public boolean isUseCustomWSDL() {
        return useCustomWSDL;
    }

    /**
     * Sets the value of the useCustomWSDL property.
     * 
     */
    public void setUseCustomWSDL(boolean value) {
        this.useCustomWSDL = value;
    }

    /**
     * Gets the value of the authenticationStyle property.
     * 
     * @return
     *     possible object is
     *     {@link AUTHENTICATIONSTYLE }
     *     
     */
    public AUTHENTICATIONSTYLE getAuthenticationStyle() {
        return authenticationStyle;
    }

    /**
     * Sets the value of the authenticationStyle property.
     * 
     * @param value
     *     allowed object is
     *     {@link AUTHENTICATIONSTYLE }
     *     
     */
    public void setAuthenticationStyle(AUTHENTICATIONSTYLE value) {
        this.authenticationStyle = value;
    }

}
