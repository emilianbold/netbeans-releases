/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package customerdb.converter;

import java.net.URI;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlAttribute;
import javax.ws.rs.core.UriBuilder;
import customerdb.Customer;
import customerdb.service.PersistenceService;


/**
 *
 * @author __USER__
 */

@XmlRootElement(name = "customer")
public class CustomerConverter {
    private Customer entity;
    private URI uri;
    private int expandLevel;
    
    /** Creates a new instance of CustomerConverter */
    public CustomerConverter() {
    }

    /**
     * Creates a new instance of CustomerConverter.
     *
     * @param entity associated entity
     * @param uri associated uri
     * @param expandLevel indicates the number of levels the entity graph should be expanded@param isUriExtendable indicates whether the uri can be extended
     */
    public CustomerConverter(Customer entity, URI uri, int expandLevel, boolean isUriExtendable) {
        this.entity = entity;
        this.uri = (isUriExtendable) ? UriBuilder.fromUri(uri).path(entity.getCustomerId() + "/").build() : uri;
        this.expandLevel = expandLevel;
    }

    /**
     * Creates a new instance of CustomerConverter.
     *
     * @param entity associated entity
     * @param uri associated uri
     * @param expandLevel indicates the number of levels the entity graph should be expanded
     */
    public CustomerConverter(Customer entity, URI uri, int expandLevel) {
        this(entity, uri, expandLevel, false);
    }

    /**
     * Getter for customerId.
     *
     * @return value for customerId
     */
    @XmlElement
    public Integer getCustomerId() {
        return (expandLevel > 0) ? getEntity().getCustomerId() : null;
    }

    /**
     * Setter for customerId.
     *
     * @param value the value to set
     */
    public void setCustomerId(Integer value) {
        getEntity().setCustomerId(value);
    }

    /**
     * Getter for zip.
     *
     * @return value for zip
     */
    @XmlElement
    public String getZip() {
        return (expandLevel > 0) ? getEntity().getZip() : null;
    }

    /**
     * Setter for zip.
     *
     * @param value the value to set
     */
    public void setZip(String value) {
        getEntity().setZip(value);
    }

    /**
     * Getter for name.
     *
     * @return value for name
     */
    @XmlElement
    public String getName() {
        return (expandLevel > 0) ? getEntity().getName() : null;
    }

    /**
     * Setter for name.
     *
     * @param value the value to set
     */
    public void setName(String value) {
        getEntity().setName(value);
    }

    /**
     * Getter for addressline1.
     *
     * @return value for addressline1
     */
    @XmlElement
    public String getAddressline1() {
        return (expandLevel > 0) ? getEntity().getAddressline1() : null;
    }

    /**
     * Setter for addressline1.
     *
     * @param value the value to set
     */
    public void setAddressline1(String value) {
        getEntity().setAddressline1(value);
    }

    /**
     * Getter for addressline2.
     *
     * @return value for addressline2
     */
    @XmlElement
    public String getAddressline2() {
        return (expandLevel > 0) ? getEntity().getAddressline2() : null;
    }

    /**
     * Setter for addressline2.
     *
     * @param value the value to set
     */
    public void setAddressline2(String value) {
        getEntity().setAddressline2(value);
    }

    /**
     * Getter for city.
     *
     * @return value for city
     */
    @XmlElement
    public String getCity() {
        return (expandLevel > 0) ? getEntity().getCity() : null;
    }

    /**
     * Setter for city.
     *
     * @param value the value to set
     */
    public void setCity(String value) {
        getEntity().setCity(value);
    }

    /**
     * Getter for state.
     *
     * @return value for state
     */
    @XmlElement
    public String getState() {
        return (expandLevel > 0) ? getEntity().getState() : null;
    }

    /**
     * Setter for state.
     *
     * @param value the value to set
     */
    public void setState(String value) {
        getEntity().setState(value);
    }

    /**
     * Getter for phone.
     *
     * @return value for phone
     */
    @XmlElement
    public String getPhone() {
        return (expandLevel > 0) ? getEntity().getPhone() : null;
    }

    /**
     * Setter for phone.
     *
     * @param value the value to set
     */
    public void setPhone(String value) {
        getEntity().setPhone(value);
    }

    /**
     * Getter for fax.
     *
     * @return value for fax
     */
    @XmlElement
    public String getFax() {
        return (expandLevel > 0) ? getEntity().getFax() : null;
    }

    /**
     * Setter for fax.
     *
     * @param value the value to set
     */
    public void setFax(String value) {
        getEntity().setFax(value);
    }

    /**
     * Getter for email.
     *
     * @return value for email
     */
    @XmlElement
    public String getEmail() {
        return (expandLevel > 0) ? getEntity().getEmail() : null;
    }

    /**
     * Setter for email.
     *
     * @param value the value to set
     */
    public void setEmail(String value) {
        getEntity().setEmail(value);
    }

    /**
     * Getter for creditLimit.
     *
     * @return value for creditLimit
     */
    @XmlElement
    public Integer getCreditLimit() {
        return (expandLevel > 0) ? getEntity().getCreditLimit() : null;
    }

    /**
     * Setter for creditLimit.
     *
     * @param value the value to set
     */
    public void setCreditLimit(Integer value) {
        getEntity().setCreditLimit(value);
    }

    /**
     * Getter for discountCode.
     *
     * @return value for discountCode
     */
    @XmlElement
    public DiscountCodeConverter getDiscountCode() {
        if (expandLevel > 0) {
            if (getEntity().getDiscountCode() != null) {
                return new DiscountCodeConverter(getEntity().getDiscountCode(), uri.resolve("discountCode/"), expandLevel - 1, false);
            }
        }
        return null;
    }

    /**
     * Setter for discountCode.
     *
     * @param value the value to set
     */
    public void setDiscountCode(DiscountCodeConverter value) {
        getEntity().setDiscountCode((value != null) ? value.resolveEntity() : null);
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
     * Sets the URI for this reference converter.
     *
     */
    public void setUri(URI uri) {
        this.uri = uri;
    }

    /**
     * Returns the Customer entity.
     *
     * @return an entity
     */
    @XmlTransient
    public Customer getEntity() {
        if (entity == null) {
            entity = new Customer();
        }
        return entity;
    }

    /**
     * Returns the resolved Customer entity.
     *
     * @return an resolved entity
     */
    public Customer resolveEntity() {
        if (entity != null) {
            return PersistenceService.getInstance().resolveEntity(Customer.class, entity.getCustomerId());
        } else {
            return (Customer) UriResolver.getInstance().resolve(CustomerConverter.class, uri);
        }
    }
}
