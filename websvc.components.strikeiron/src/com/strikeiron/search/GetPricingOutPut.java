
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
