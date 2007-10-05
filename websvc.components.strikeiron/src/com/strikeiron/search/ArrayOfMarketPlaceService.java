
package com.strikeiron.search;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ArrayOfMarketPlaceService complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ArrayOfMarketPlaceService">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="MarketPlaceService" type="{http://www.strikeiron.com}MarketPlaceService" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfMarketPlaceService", propOrder = {
    "marketPlaceService"
})
public class ArrayOfMarketPlaceService {

    @XmlElement(name = "MarketPlaceService", nillable = true)
    protected List<MarketPlaceService> marketPlaceService;

    /**
     * Gets the value of the marketPlaceService property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the marketPlaceService property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMarketPlaceService().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MarketPlaceService }
     * 
     * 
     */
    public List<MarketPlaceService> getMarketPlaceService() {
        if (marketPlaceService == null) {
            marketPlaceService = new ArrayList<MarketPlaceService>();
        }
        return this.marketPlaceService;
    }

}
