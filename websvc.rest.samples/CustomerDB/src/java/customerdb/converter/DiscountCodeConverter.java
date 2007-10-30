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
