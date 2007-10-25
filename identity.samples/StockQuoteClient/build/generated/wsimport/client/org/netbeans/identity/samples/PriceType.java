
package org.netbeans.identity.samples;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PriceType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PriceType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Last" type="{http://www.w3.org/2001/XMLSchema}float"/>
 *         &lt;element name="Open" type="{http://www.w3.org/2001/XMLSchema}float"/>
 *         &lt;element name="DayHigh" type="{http://www.w3.org/2001/XMLSchema}float"/>
 *         &lt;element name="DayLow" type="{http://www.w3.org/2001/XMLSchema}float"/>
 *         &lt;element name="YearRange" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PriceType", propOrder = {
    "last",
    "open",
    "dayHigh",
    "dayLow",
    "yearRange"
})
public class PriceType {

    @XmlElement(name = "Last")
    protected float last;
    @XmlElement(name = "Open")
    protected float open;
    @XmlElement(name = "DayHigh")
    protected float dayHigh;
    @XmlElement(name = "DayLow")
    protected float dayLow;
    @XmlElement(name = "YearRange", required = true)
    protected String yearRange;

    /**
     * Gets the value of the last property.
     * 
     */
    public float getLast() {
        return last;
    }

    /**
     * Sets the value of the last property.
     * 
     */
    public void setLast(float value) {
        this.last = value;
    }

    /**
     * Gets the value of the open property.
     * 
     */
    public float getOpen() {
        return open;
    }

    /**
     * Sets the value of the open property.
     * 
     */
    public void setOpen(float value) {
        this.open = value;
    }

    /**
     * Gets the value of the dayHigh property.
     * 
     */
    public float getDayHigh() {
        return dayHigh;
    }

    /**
     * Sets the value of the dayHigh property.
     * 
     */
    public void setDayHigh(float value) {
        this.dayHigh = value;
    }

    /**
     * Gets the value of the dayLow property.
     * 
     */
    public float getDayLow() {
        return dayLow;
    }

    /**
     * Sets the value of the dayLow property.
     * 
     */
    public void setDayLow(float value) {
        this.dayLow = value;
    }

    /**
     * Gets the value of the yearRange property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getYearRange() {
        return yearRange;
    }

    /**
     * Sets the value of the yearRange property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setYearRange(String value) {
        this.yearRange = value;
    }

}
