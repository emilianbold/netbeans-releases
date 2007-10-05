
package com.strikeiron.search;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for StatusCodeOutput complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="StatusCodeOutput">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.strikeiron.com}SIWsOutput">
 *       &lt;sequence>
 *         &lt;element name="ServiceResult" type="{http://www.strikeiron.com}StatusCodeResult" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StatusCodeOutput", propOrder = {
    "serviceResult"
})
public class StatusCodeOutput
    extends SIWsOutput
{

    @XmlElement(name = "ServiceResult")
    protected StatusCodeResult serviceResult;

    /**
     * Gets the value of the serviceResult property.
     * 
     * @return
     *     possible object is
     *     {@link StatusCodeResult }
     *     
     */
    public StatusCodeResult getServiceResult() {
        return serviceResult;
    }

    /**
     * Sets the value of the serviceResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link StatusCodeResult }
     *     
     */
    public void setServiceResult(StatusCodeResult value) {
        this.serviceResult = value;
    }

}
