
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
 *         &lt;element name="startDownloadScriptReturn" type="{http://services.mc.com}ApplicationAPI_StartDownloadScriptReturn"/>
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
    "startDownloadScriptReturn"
})
@XmlRootElement(name = "startDownloadScriptResponse")
public class StartDownloadScriptResponse {

    @XmlElement(required = true)
    protected ApplicationAPIStartDownloadScriptReturn startDownloadScriptReturn;

    /**
     * Gets the value of the startDownloadScriptReturn property.
     * 
     * @return
     *     possible object is
     *     {@link ApplicationAPIStartDownloadScriptReturn }
     *     
     */
    public ApplicationAPIStartDownloadScriptReturn getStartDownloadScriptReturn() {
        return startDownloadScriptReturn;
    }

    /**
     * Sets the value of the startDownloadScriptReturn property.
     * 
     * @param value
     *     allowed object is
     *     {@link ApplicationAPIStartDownloadScriptReturn }
     *     
     */
    public void setStartDownloadScriptReturn(ApplicationAPIStartDownloadScriptReturn value) {
        this.startDownloadScriptReturn = value;
    }

}
