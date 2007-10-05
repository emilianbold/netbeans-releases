
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
 *         &lt;element name="GetPricingResult" type="{http://www.strikeiron.com}GetPricingOutPut" minOccurs="0"/>
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
    "getPricingResult"
})
@XmlRootElement(name = "GetPricingResponse")
public class GetPricingResponse {

    @XmlElement(name = "GetPricingResult")
    protected GetPricingOutPut getPricingResult;

    /**
     * Gets the value of the getPricingResult property.
     * 
     * @return
     *     possible object is
     *     {@link GetPricingOutPut }
     *     
     */
    public GetPricingOutPut getGetPricingResult() {
        return getPricingResult;
    }

    /**
     * Sets the value of the getPricingResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link GetPricingOutPut }
     *     
     */
    public void setGetPricingResult(GetPricingOutPut value) {
        this.getPricingResult = value;
    }

}
