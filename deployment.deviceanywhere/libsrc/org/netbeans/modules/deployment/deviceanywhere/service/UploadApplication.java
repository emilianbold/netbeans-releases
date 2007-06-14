
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
 *         &lt;element name="username" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="password" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="appName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="jarFileData" type="{http://www.w3.org/2001/XMLSchema}base64Binary"/>
 *         &lt;element name="jadFileData" type="{http://www.w3.org/2001/XMLSchema}base64Binary"/>
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
    "username",
    "password",
    "appName",
    "jarFileData",
    "jadFileData"
})
@XmlRootElement(name = "uploadApplication")
public class UploadApplication {

    @XmlElement(required = true)
    protected String username;
    @XmlElement(required = true)
    protected String password;
    @XmlElement(required = true)
    protected String appName;
    @XmlElement(required = true)
    protected byte[] jarFileData;
    @XmlElement(required = true)
    protected byte[] jadFileData;

    /**
     * Gets the value of the username property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the value of the username property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUsername(String value) {
        this.username = value;
    }

    /**
     * Gets the value of the password property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the value of the password property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPassword(String value) {
        this.password = value;
    }

    /**
     * Gets the value of the appName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAppName() {
        return appName;
    }

    /**
     * Sets the value of the appName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAppName(String value) {
        this.appName = value;
    }

    /**
     * Gets the value of the jarFileData property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getJarFileData() {
        return jarFileData;
    }

    /**
     * Sets the value of the jarFileData property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setJarFileData(byte[] value) {
        this.jarFileData = ((byte[]) value);
    }

    /**
     * Gets the value of the jadFileData property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getJadFileData() {
        return jadFileData;
    }

    /**
     * Sets the value of the jadFileData property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setJadFileData(byte[] value) {
        this.jadFileData = ((byte[]) value);
    }

}
