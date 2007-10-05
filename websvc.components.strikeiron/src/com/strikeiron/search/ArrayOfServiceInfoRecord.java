
package com.strikeiron.search;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ArrayOfServiceInfoRecord complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ArrayOfServiceInfoRecord">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ServiceInfoRecord" type="{http://www.strikeiron.com}ServiceInfoRecord" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfServiceInfoRecord", propOrder = {
    "serviceInfoRecord"
})
public class ArrayOfServiceInfoRecord {

    @XmlElement(name = "ServiceInfoRecord", nillable = true)
    protected List<ServiceInfoRecord> serviceInfoRecord;

    /**
     * Gets the value of the serviceInfoRecord property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the serviceInfoRecord property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getServiceInfoRecord().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ServiceInfoRecord }
     * 
     * 
     */
    public List<ServiceInfoRecord> getServiceInfoRecord() {
        if (serviceInfoRecord == null) {
            serviceInfoRecord = new ArrayList<ServiceInfoRecord>();
        }
        return this.serviceInfoRecord;
    }

}
