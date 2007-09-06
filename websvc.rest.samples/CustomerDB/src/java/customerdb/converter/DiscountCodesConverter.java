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
