/*
 * Copyright (c) 2011, Oracle. All rights reserved.
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
 * * Neither the name of Oracle nor the names of its contributors
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
package embedded.telephonedirectory.server;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * Represents a telephone directory entry in the database for a person.
 */
@Entity
@NamedQueries({
        @NamedQuery(name = "allentries",
                query = "SELECT x FROM PersonEntity x"),
        @NamedQuery(name = "sortedentries",
                query = "SELECT x FROM PersonEntity x order by x.name"),
        @NamedQuery(name = "searchbyname",
                query = "SELECT x FROM PersonEntity x WHERE x.name=:somename"),
        @NamedQuery(name = "searchbyloc",
                query = "SELECT x FROM PersonEntity x WHERE x.location=:someplace")
})
@Table(name = "TELEPHONE_DIRECTORY")
public class PersonEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    private String phoneNumber;
    private String name;
    private String address, location, country;

    /**
     * Gets the address of the person.
     * @return Address of the person.
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets the address of the person.
     * @param address Address of the person.
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Gets the country of the person.
     * @return Country of the person.
     */
    public String getCountry() {
        return country;
    }

    /**
     * Sets country of the person.
     * @param country Country of the person.
     */
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * Gets the location of the person.
     * @return Location of the person.
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets the location of the person.
     * @param location Location of the person.
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Gets the name of the person.
     * @return Name of the person.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the person.
     * @param name Name of the person.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the phone number of the person.
     * @return Phone number of the person.
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Sets the phone numberof the person.
     * @param phoneNumber Phone number of the person.
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
