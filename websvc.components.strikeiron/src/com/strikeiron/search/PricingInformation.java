
package com.strikeiron.search;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PricingInformation complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PricingInformation">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="PricingID" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="PricingDescription" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="PricingType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Price" type="{http://www.w3.org/2001/XMLSchema}decimal"/>
 *         &lt;element name="Hits" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="OverageAllowed" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="OverageFeePerHit" type="{http://www.w3.org/2001/XMLSchema}decimal"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PricingInformation", propOrder = {
    "pricingID",
    "pricingDescription",
    "pricingType",
    "price",
    "hits",
    "overageAllowed",
    "overageFeePerHit"
})
public class PricingInformation {

    @XmlElement(name = "PricingID")
    protected int pricingID;
    @XmlElement(name = "PricingDescription")
    protected String pricingDescription;
    @XmlElement(name = "PricingType")
    protected String pricingType;
    @XmlElement(name = "Price", required = true)
    protected BigDecimal price;
    @XmlElement(name = "Hits")
    protected int hits;
    @XmlElement(name = "OverageAllowed")
    protected boolean overageAllowed;
    @XmlElement(name = "OverageFeePerHit", required = true)
    protected BigDecimal overageFeePerHit;

    /**
     * Gets the value of the pricingID property.
     * 
     */
    public int getPricingID() {
        return pricingID;
    }

    /**
     * Sets the value of the pricingID property.
     * 
     */
    public void setPricingID(int value) {
        this.pricingID = value;
    }

    /**
     * Gets the value of the pricingDescription property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPricingDescription() {
        return pricingDescription;
    }

    /**
     * Sets the value of the pricingDescription property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPricingDescription(String value) {
        this.pricingDescription = value;
    }

    /**
     * Gets the value of the pricingType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPricingType() {
        return pricingType;
    }

    /**
     * Sets the value of the pricingType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPricingType(String value) {
        this.pricingType = value;
    }

    /**
     * Gets the value of the price property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getPrice() {
        return price;
    }

    /**
     * Sets the value of the price property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setPrice(BigDecimal value) {
        this.price = value;
    }

    /**
     * Gets the value of the hits property.
     * 
     */
    public int getHits() {
        return hits;
    }

    /**
     * Sets the value of the hits property.
     * 
     */
    public void setHits(int value) {
        this.hits = value;
    }

    /**
     * Gets the value of the overageAllowed property.
     * 
     */
    public boolean isOverageAllowed() {
        return overageAllowed;
    }

    /**
     * Sets the value of the overageAllowed property.
     * 
     */
    public void setOverageAllowed(boolean value) {
        this.overageAllowed = value;
    }

    /**
     * Gets the value of the overageFeePerHit property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getOverageFeePerHit() {
        return overageFeePerHit;
    }

    /**
     * Sets the value of the overageFeePerHit property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setOverageFeePerHit(BigDecimal value) {
        this.overageFeePerHit = value;
    }

}
