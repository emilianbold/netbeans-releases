/*
 *  DiscountCodeConverter
 *
 * Created on August 17, 2007, 4:38 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
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
