
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
