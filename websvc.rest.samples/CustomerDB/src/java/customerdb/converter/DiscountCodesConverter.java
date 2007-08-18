/*
 *  DiscountCodesConverter
 *
 * Created on August 17, 2007, 4:38 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package customerdb.converter;

import customerdb.DiscountCode;
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

@XmlRootElement(name = "discountCodes")
public class DiscountCodesConverter {
    private Collection<DiscountCode> entities;
    private Collection<customerdb.converter.DiscountCodeRefConverter> references;
    private URI uri;
    
    /** Creates a new instance of DiscountCodesConverter */
    public DiscountCodesConverter() {
    }

    /**
     * Creates a new instance of DiscountCodesConverter.
     *
     * @param entities associated entities
     * @param uri associated uri
     */
    public DiscountCodesConverter(Collection<DiscountCode> entities, URI uri) {
        this.entities = entities;
        this.uri = uri;
    }

    /**
     * Returns a collection of DiscountCodeRefConverter.
     *
     * @return a collection of DiscountCodeRefConverter
     */
    @XmlElement(name = "discountCodeRef")
    public Collection<customerdb.converter.DiscountCodeRefConverter> getReferences() {
        references = new ArrayList<DiscountCodeRefConverter>();
        if (entities != null) {
            for (DiscountCode entity : entities) {
                references.add(new DiscountCodeRefConverter(entity, uri, true));
            }
        }
        return references;
    }

    /**
     * Sets a collection of DiscountCodeRefConverter.
     *
     * @param a collection of DiscountCodeRefConverter to set
     */
    public void setReferences(Collection<customerdb.converter.DiscountCodeRefConverter> references) {
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
     * Returns a collection DiscountCode entities.
     *
     * @return a collection of DiscountCode entities
     */
    @XmlTransient
    public Collection<DiscountCode> getEntities() {
        entities = new ArrayList<DiscountCode>();
        if (references != null) {
            for (DiscountCodeRefConverter ref : references) {
                entities.add(ref.getEntity());
            }
        }
        return entities;
    }
}
