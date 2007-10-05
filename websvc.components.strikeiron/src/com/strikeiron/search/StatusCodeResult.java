
package com.strikeiron.search;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for StatusCodeResult complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="StatusCodeResult">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.strikeiron.com}SIWsResult">
 *       &lt;sequence>
 *         &lt;element name="Statuses" type="{http://www.strikeiron.com}ArrayOfSIWsStatus" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StatusCodeResult", propOrder = {
    "statuses"
})
public class StatusCodeResult
    extends SIWsResult
{

    @XmlElement(name = "Statuses")
    protected ArrayOfSIWsStatus statuses;

    /**
     * Gets the value of the statuses property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfSIWsStatus }
     *     
     */
    public ArrayOfSIWsStatus getStatuses() {
        return statuses;
    }

    /**
     * Sets the value of the statuses property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfSIWsStatus }
     *     
     */
    public void setStatuses(ArrayOfSIWsStatus value) {
        this.statuses = value;
    }

}
