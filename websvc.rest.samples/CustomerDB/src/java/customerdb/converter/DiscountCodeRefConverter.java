/*
 *  DiscountCodeRefConverter
 *
 * Created on August 17, 2007, 4:38 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package customerdb.converter;

import customerdb.DiscountCode;
import java.net.URI;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlAttribute;

/**
 *
 * @author Peter Liu
 */

@XmlRootElement(name = "discountCodeRef")
public class DiscountCodeRefConverter {
    private DiscountCode entity;
    private boolean isUriExtendable;
    private URI uri;
    
    /** Creates a new instance of DiscountCodeRefConverter */
    public DiscountCodeRefConverter() {
    }

    /**
     * Creates a new instance of DiscountCodeRefConverter.
     *
     * @param entity associated entity
     * @param uri associated uri
     * @param isUriExtendable indicates whether the uri can be extended
     */
    public DiscountCodeRefConverter(DiscountCode entity, URI uri, boolean isUriExtendable) {
        this.entity = entity;
        this.uri = uri;
        this.isUriExtendable = isUriExtendable;
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
     * Returns the URI associated with this reference converter.
     *
     * @return the converted uri
     */
    @XmlAttribute
    public URI getUri() {
        if (isUriExtendable) {
            return uri.resolve(getDiscountCode().toString() + "/");
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
     * Returns the DiscountCode entity.
     *
     * @return DiscountCode entity
     */
    @XmlTransient
    public DiscountCode getEntity() {
        DiscountCodeConverter result = UriResolver.getInstance().resolve(DiscountCodeConverter.class, uri);
        if (result != null) {
            return result.getEntity();
        }
        return null;
    }
}
