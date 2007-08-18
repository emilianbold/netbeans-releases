/*
 *  CustomersConverter
 *
 * Created on August 17, 2007, 4:38 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package customerdb.converter;

import customerdb.Customer;
import java.net.URI;
import java.util.Collection;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlAttribute;
import java.util.ArrayList;

/**
 *
 * @author Peter Liu
 */

@XmlRootElement(name = "customers")
public class CustomersConverter {
    private Collection<Customer> entities;
    private Collection<CustomerRefConverter> references;
    private URI uri;
    
    /** Creates a new instance of CustomersConverter */
    public CustomersConverter() {
    }

    /**
     * Creates a new instance of CustomersConverter.
     *
     * @param entities associated entities
     * @param uri associated uri
     */
    public CustomersConverter(Collection<Customer> entities, URI uri) {
        this.entities = entities;
        this.uri = uri;
    }

    /**
     * Returns a collection of CustomerRefConverter.
     *
     * @return a collection of CustomerRefConverter
     */
    @XmlElement(name = "customerRef")
    public Collection<CustomerRefConverter> getReferences() {
        references = new ArrayList<CustomerRefConverter>();
        if (entities != null) {
            for (Customer entity : entities) {
                references.add(new CustomerRefConverter(entity, uri, true));
            }
        }
        return references;
    }

    /**
     * Sets a collection of CustomerRefConverter.
     *
     * @param a collection of CustomerRefConverter to set
     */
    public void setReferences(Collection<CustomerRefConverter> references) {
        this.references = references;
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
     * Returns a collection Customer entities.
     *
     * @return a collection of Customer entities
     */
    @XmlTransient
    public Collection<Customer> getEntities() {
        entities = new ArrayList<Customer>();
        if (references != null) {
            for (CustomerRefConverter ref : references) {
                entities.add(ref.getEntity());
            }
        }
        return entities;
    }
}
