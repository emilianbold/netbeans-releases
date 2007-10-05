
package com.strikeiron.search;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SearchOutPut complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SearchOutPut">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.strikeiron.com}SIWsOutput">
 *       &lt;sequence>
 *         &lt;element name="StrikeIronWebServices" type="{http://www.strikeiron.com}ArrayOfMarketPlaceService" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SearchOutPut", propOrder = {
    "strikeIronWebServices"
})
public class SearchOutPut
    extends SIWsOutput
{

    @XmlElement(name = "StrikeIronWebServices")
    protected ArrayOfMarketPlaceService strikeIronWebServices;

    /**
     * Gets the value of the strikeIronWebServices property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfMarketPlaceService }
     *     
     */
    public ArrayOfMarketPlaceService getStrikeIronWebServices() {
        return strikeIronWebServices;
    }

    /**
     * Sets the value of the strikeIronWebServices property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfMarketPlaceService }
     *     
     */
    public void setStrikeIronWebServices(ArrayOfMarketPlaceService value) {
        this.strikeIronWebServices = value;
    }

}
