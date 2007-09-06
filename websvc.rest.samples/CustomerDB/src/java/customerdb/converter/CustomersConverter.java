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
