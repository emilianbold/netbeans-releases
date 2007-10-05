
package com.strikeiron.search;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ServiceInfoRecord complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ServiceInfoRecord">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="InfoKey" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="InfoValue" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ServiceInfoRecord", propOrder = {
    "infoKey",
    "infoValue"
})
public class ServiceInfoRecord {

    @XmlElement(name = "InfoKey")
    protected String infoKey;
    @XmlElement(name = "InfoValue")
    protected String infoValue;

    /**
     * Gets the value of the infoKey property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInfoKey() {
        return infoKey;
    }

    /**
     * Sets the value of the infoKey property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInfoKey(String value) {
        this.infoKey = value;
    }

    /**
     * Gets the value of the infoValue property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInfoValue() {
        return infoValue;
    }

    /**
     * Sets the value of the infoValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInfoValue(String value) {
        this.infoValue = value;
    }

}
