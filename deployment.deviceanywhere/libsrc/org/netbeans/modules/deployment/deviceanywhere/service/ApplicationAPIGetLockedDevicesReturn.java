
package org.netbeans.modules.deployment.deviceanywhere.service;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ApplicationAPI_GetLockedDevicesReturn complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ApplicationAPI_GetLockedDevicesReturn">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="deviceWrappers" type="{http://services.mc.com}ArrayOfApplicationAPI_DeviceWrapper"/>
 *         &lt;element name="returnCode" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ApplicationAPI_GetLockedDevicesReturn", propOrder = {
    "deviceWrappers",
    "returnCode"
})
public class ApplicationAPIGetLockedDevicesReturn {

    @XmlElement(required = true, nillable = true)
    protected ArrayOfApplicationAPIDeviceWrapper deviceWrappers;
    protected int returnCode;

    /**
     * Gets the value of the deviceWrappers property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfApplicationAPIDeviceWrapper }
     *     
     */
    public ArrayOfApplicationAPIDeviceWrapper getDeviceWrappers() {
        return deviceWrappers;
    }

    /**
     * Sets the value of the deviceWrappers property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfApplicationAPIDeviceWrapper }
     *     
     */
    public void setDeviceWrappers(ArrayOfApplicationAPIDeviceWrapper value) {
        this.deviceWrappers = value;
    }

    /**
     * Gets the value of the returnCode property.
     * 
     */
    public int getReturnCode() {
        return returnCode;
    }

    /**
     * Sets the value of the returnCode property.
     * 
     */
    public void setReturnCode(int value) {
        this.returnCode = value;
    }

}
