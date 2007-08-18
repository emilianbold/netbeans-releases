/*
 *  CustomerRefConverter
 *
 * Created on August 17, 2007, 4:38 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
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

@XmlRootElement(name = "customerRef")
public class CustomerRefConverter {
    private Customer entity;
    private boolean isUriExtendable;
    private URI uri;
    
    /** Creates a new instance of CustomerRefConverter */
    public CustomerRefConverter() {
    }

    /**
     * Creates a new instance of CustomerRefConverter.
     *
     * @param entity associated entity
     * @param uri associated uri
     * @param isUriExtendable indicates whether the uri can be extended
     */
    public CustomerRefConverter(Customer entity, URI uri, boolean isUriExtendable) {
        this.entity = entity;
        this.uri = uri;
        this.isUriExtendable = isUriExtendable;
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
     * Returns the URI associated with this reference converter.
     *
     * @return the converted uri
     */
    @XmlAttribute
    public URI getUri() {
        if (isUriExtendable) {
            return uri.resolve(getCustomerId().toString() + "/");
        }
        return uri;
    }

    /**
     * Sets the URI for this reference converter.
     *
     */
    public void setUri(URI uri) {
        this.uri = uri;
    }

    /**
     * Returns the Customer entity.
     *
     * @return Customer entity
     */
    @XmlTransient
    public Customer getEntity() {
        CustomerConverter result = UriResolver.getInstance().resolve(CustomerConverter.class, uri);
        if (result != null) {
            return result.getEntity();
        }
        return null;
    }
}
