/*
 * Copyright (c) 2007, Sun Microsystems, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of Sun Microsystems, Inc. nor the names of its contributors
 *   may be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package customerdb.converter;

import customerdb.Customer;
import java.net.URI;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlAttribute;

/**
 *
 * @author Peter Liu
 */

@XmlRootElement(name = "customer")
public class CustomerConverter {
    private Customer entity;
    private URI uri;
    
    /** Creates a new instance of CustomerConverter */
    public CustomerConverter() {
        entity = new Customer();
    }

    /**
     * Creates a new instance of CustomerConverter.
     *
     * @param entity associated entity
     * @param uri associated uri
     */
    public CustomerConverter(Customer entity, URI uri) {
        this.entity = entity;
        this.uri = uri;
    }

    /**
     * Getter for customerId.
     *
     * @return value for customerId
     */
    @XmlElement
    public Integer getCustomerId() {
        return entity.getCustomerId();
    }

    /**
     * Setter for customerId.
     *
     * @param value the value to set
     */
    public void setCustomerId(Integer value) {
        entity.setCustomerId(value);
    }

    /**
     * Getter for zip.
     *
     * @return value for zip
     */
    @XmlElement
    public String getZip() {
        return entity.getZip();
    }

    /**
     * Setter for zip.
     *
     * @param value the value to set
     */
    public void setZip(String value) {
        entity.setZip(value);
    }

    /**
     * Getter for name.
     *
     * @return value for name
     */
    @XmlElement
    public String getName() {
        return entity.getName();
    }

    /**
     * Setter for name.
     *
     * @param value the value to set
     */
    public void setName(String value) {
        entity.setName(value);
    }

    /**
     * Getter for addressline1.
     *
     * @return value for addressline1
     */
    @XmlElement
    public String getAddressline1() {
        return entity.getAddressline1();
    }

    /**
     * Setter for addressline1.
     *
     * @param value the value to set
     */
    public void setAddressline1(String value) {
        entity.setAddressline1(value);
    }

    /**
     * Getter for addressline2.
     *
     * @return value for addressline2
     */
    @XmlElement
    public String getAddressline2() {
        return entity.getAddressline2();
    }

    /**
     * Setter for addressline2.
     *
     * @param value the value to set
     */
    public void setAddressline2(String value) {
        entity.setAddressline2(value);
    }

    /**
     * Getter for city.
     *
     * @return value for city
     */
    @XmlElement
    public String getCity() {
        return entity.getCity();
    }

    /**
     * Setter for city.
     *
     * @param value the value to set
     */
    public void setCity(String value) {
        entity.setCity(value);
    }

    /**
     * Getter for state.
     *
     * @return value for state
     */
    @XmlElement
    public String getState() {
        return entity.getState();
    }

    /**
     * Setter for state.
     *
     * @param value the value to set
     */
    public void setState(String value) {
        entity.setState(value);
    }

    /**
     * Getter for phone.
     *
     * @return value for phone
     */
    @XmlElement
    public String getPhone() {
        return entity.getPhone();
    }

    /**
     * Setter for phone.
     *
     * @param value the value to set
     */
    public void setPhone(String value) {
        entity.setPhone(value);
    }

    /**
     * Getter for fax.
     *
     * @return value for fax
     */
    @XmlElement
    public String getFax() {
        return entity.getFax();
    }

    /**
     * Setter for fax.
     *
     * @param value the value to set
     */
    public void setFax(String value) {
        entity.setFax(value);
    }

    /**
     * Getter for email.
     *
     * @return value for email
     */
    @XmlElement
    public String getEmail() {
        return entity.getEmail();
    }

    /**
     * Setter for email.
     *
     * @param value the value to set
     */
    public void setEmail(String value) {
        entity.setEmail(value);
    }

    /**
     * Getter for creditLimit.
     *
     * @return value for creditLimit
     */
    @XmlElement
    public Integer getCreditLimit() {
        return entity.getCreditLimit();
    }

    /**
     * Setter for creditLimit.
     *
     * @param value the value to set
     */
    public void setCreditLimit(Integer value) {
        entity.setCreditLimit(value);
    }

    /**
     * Getter for discountCode.
     *
     * @return value for discountCode
     */
    @XmlElement(name = "discountCodeRef")
    public DiscountCodeRefConverter getDiscountCode() {
        if (entity.getDiscountCode() != null) {
            return new DiscountCodeRefConverter(entity.getDiscountCode(), uri.resolve("discountCode/"), false);
        }
        return null;
    }

    /**
     * Setter for discountCode.
     *
     * @param value the value to set
     */
    public void setDiscountCode(DiscountCodeRefConverter value) {
        if (value != null) {
            entity.setDiscountCode(value.getEntity());
        }
    }

    /**
     * Returns the URI associated with this converter.
     *
     * @return the uri
     */
    @XmlAttribute
    public URI getUri() {
        return uri;
    }

    /**
     * Returns the Customer entity.
     *
     * @return an entity
     */
    @XmlTransient
    public Customer getEntity() {
        return entity;
    }

    /**
     * Sets the Customer entity.
     *
     * @param entity to set
     */
    public void setEntity(Customer entity) {
        this.entity = entity;
    }
}
