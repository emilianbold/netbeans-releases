
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
 *         &lt;element name="GetAllStatusesResult" type="{http://www.strikeiron.com}StatusCodeOutput" minOccurs="0"/>
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
    "getAllStatusesResult"
})
@XmlRootElement(name = "GetAllStatusesResponse")
public class GetAllStatusesResponse {

    @XmlElement(name = "GetAllStatusesResult")
    protected StatusCodeOutput getAllStatusesResult;

    /**
     * Gets the value of the getAllStatusesResult property.
     * 
     * @return
     *     possible object is
     *     {@link StatusCodeOutput }
     *     
     */
    public StatusCodeOutput getGetAllStatusesResult() {
        return getAllStatusesResult;
    }

    /**
     * Sets the value of the getAllStatusesResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link StatusCodeOutput }
     *     
     */
    public void setGetAllStatusesResult(StatusCodeOutput value) {
        this.getAllStatusesResult = value;
    }

}
