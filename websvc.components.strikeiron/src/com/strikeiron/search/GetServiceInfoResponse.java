
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
 *         &lt;element name="GetServiceInfoResult" type="{http://www.strikeiron.com}ServiceInfoOutput" minOccurs="0"/>
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
    "getServiceInfoResult"
})
@XmlRootElement(name = "GetServiceInfoResponse")
public class GetServiceInfoResponse {

    @XmlElement(name = "GetServiceInfoResult")
    protected ServiceInfoOutput getServiceInfoResult;

    /**
     * Gets the value of the getServiceInfoResult property.
     * 
     * @return
     *     possible object is
     *     {@link ServiceInfoOutput }
     *     
     */
    public ServiceInfoOutput getGetServiceInfoResult() {
        return getServiceInfoResult;
    }

    /**
     * Sets the value of the getServiceInfoResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link ServiceInfoOutput }
     *     
     */
    public void setGetServiceInfoResult(ServiceInfoOutput value) {
        this.getServiceInfoResult = value;
    }

}
