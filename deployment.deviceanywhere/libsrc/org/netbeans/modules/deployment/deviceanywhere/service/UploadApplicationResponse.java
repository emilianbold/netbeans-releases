
package org.netbeans.modules.deployment.deviceanywhere.service;

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
 *         &lt;element name="uploadApplicationReturn" type="{http://services.mc.com}ApplicationAPI_UploadApplicationReturn"/>
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
    "uploadApplicationReturn"
})
@XmlRootElement(name = "uploadApplicationResponse")
public class UploadApplicationResponse {

    @XmlElement(required = true)
    protected ApplicationAPIUploadApplicationReturn uploadApplicationReturn;

    /**
     * Gets the value of the uploadApplicationReturn property.
     * 
     * @return
     *     possible object is
     *     {@link ApplicationAPIUploadApplicationReturn }
     *     
     */
    public ApplicationAPIUploadApplicationReturn getUploadApplicationReturn() {
        return uploadApplicationReturn;
    }

    /**
     * Sets the value of the uploadApplicationReturn property.
     * 
     * @param value
     *     allowed object is
     *     {@link ApplicationAPIUploadApplicationReturn }
     *     
     */
    public void setUploadApplicationReturn(ApplicationAPIUploadApplicationReturn value) {
        this.uploadApplicationReturn = value;
    }

}
