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

import customerdb.DiscountCode;
import java.math.BigDecimal;
import java.net.URI;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlAttribute;

/**
 *
 * @author Peter Liu
 */

@XmlRootElement(name = "discountCode")
public class DiscountCodeConverter {
    private DiscountCode entity;
    private URI uri;
    
    /** Creates a new instance of DiscountCodeConverter */
    public DiscountCodeConverter() {
        entity = new DiscountCode();
    }

    /**
     * Creates a new instance of DiscountCodeConverter.
     *
     * @param entity associated entity
     * @param uri associated uri
     */
    public DiscountCodeConverter(DiscountCode entity, URI uri) {
        this.entity = entity;
        this.uri = uri;
    }

    /**
     * Getter for discountCode.
     *
     * @return value for discountCode
     */
    @XmlElement
    public String getDiscountCode() {
        return entity.getDiscountCode();
    }

    /**
     * Setter for discountCode.
     *
     * @param value the value to set
     */
    public void setDiscountCode(String value) {
        entity.setDiscountCode(value);
    }

    /**
     * Getter for rate.
     *
     * @return value for rate
     */
    @XmlElement
    public BigDecimal getRate() {
        return entity.getRate();
    }

    /**
     * Setter for rate.
     *
     * @param value the value to set
     */
    public void setRate(BigDecimal value) {
        entity.setRate(value);
    }

    /**
     * Getter for customerCollection.
     *
     * @return value for customerCollection
     */
    @XmlElement(name = "customers")
    public CustomersConverter getCustomerCollection() {
        if (entity.getCustomerCollection() != null) {
            return new CustomersConverter(entity.getCustomerCollection(), uri.resolve("customers/"));
        }
        return null;
    }

    /**
     * Setter for customerCollection.
     *
     * @param value the value to set
     */
    public void setCustomerCollection(CustomersConverter value) {
        if (value != null) {
            entity.setCustomerCollection(value.getEntities());
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
     * Returns the DiscountCode entity.
     *
     * @return an entity
     */
    @XmlTransient
    public DiscountCode getEntity() {
        return entity;
    }

    /**
     * Sets the DiscountCode entity.
     *
     * @param entity to set
     */
    public void setEntity(DiscountCode entity) {
        this.entity = entity;
    }
}
