/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2003 The Apache Software Foundation.  All rights 
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:  
 *       "This product includes software developed by the 
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written 
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */ 

package org.apache.taglibs.standard.examples.beans;

import java.util.Date;
import java.text.*;

/**
 * Object that represents a Customer.
 *
 * @author Pierre Delisle
 * @version $Revision$ $Date$
 */

public class Customer {
    
    //*********************************************************************
    // Instance variables
    
    /** Holds value of property key. */
    int key;
    
    /** Holds value of property lastName. */
    private String lastName;
    
    /** Holds value of property firstName. */
    private String firstName;
    
    /** Holds value of property birthDate. */
    private Date birthDate;
    
    /** Holds value of property address. */
    private Address address;
       
    /** Holds value of property phoneHome. */
    private String phoneHome;
    
    /** Holds value of property phoneCell. */
    private String phoneCell;
    
    static DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
    
    //*********************************************************************
    // Constructors
    
    public Customer() {}
    
    public Customer(int key,
    String lastName,
    String firstName,
    Date birthDate,
    Address address,
    String phoneHome,
    String phoneCell) {
        init(key, lastName, firstName, birthDate, address, phoneHome, phoneCell);
    }
    
    public void init(int key,
    String lastName,
    String firstName,
    Date birthDate,
    Address address,
    String phoneHome,
    String phoneCell) {
        setKey(key);
        setLastName(lastName);
        setFirstName(firstName);
        setBirthDate(birthDate);
        setAddress(address);
        setPhoneHome(phoneHome);
        setPhoneCell(phoneCell);
    }
    
    //*********************************************************************
    // Properties
    
    /**
     * Getter for property key.
     * @return Value of property key.
     */
    public int getKey() {
        return key;
    }
    
    /**
     * Setter for property key.
     * @param key New value of property key.
     */
    public void setKey(int key) {
        this.key = key;
    }
    
    /**
     * Getter for property lastName.
     * @return Value of property lastName.
     */
    public String getLastName() {
        return lastName;
    }
    
    /**
     * Setter for property lastName.
     * @param lastName New value of property lastName.
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    /**
     * Getter for property firstName.
     * @return Value of property firstName.
     */
    public String getFirstName() {
        return firstName;
    }
    
    /**
     * Setter for property firstName.
     * @param firstName New value of property firstName.
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    /**
     * Getter for property birthDate.
     * @return Value of property birthDate.
     */
    public Date getBirthDate() {
        return birthDate;
    }
    
    /**
     * Setter for property birthDate.
     * @param birthDate New value of property birthDate.
     */
    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }
    
    /**
     * Getter for property address.
     * @return Value of property address.
     */
    public Address getAddress() {
        return address;
    }
    
    /**
     * Setter for property address.
     * @param address New value of property address.
     */
    public void setAddress(Address address) {
        this.address = address;
    }
    
    /**
     * Getter for property phoneHome.
     * @return Value of property phoneHome.
     */
    public String getPhoneHome() {
        return phoneHome;
    }
    
    /**
     * Setter for property phoneHome.
     * @param phoneHome New value of property phoneHome.
     */
    public void setPhoneHome(String phoneHome) {
        this.phoneHome = phoneHome;
    }
    
    /**
     * Getter for property phoneCell.
     * @return Value of property phoneCell.
     */
    public String getPhoneCell() {
        return phoneCell;
    }
    
    /**
     * Setter for property phoneCell.
     * @param phoneCell New value of property phoneCell.
     */
    public void setPhoneCell(String phoneCell) {
        this.phoneCell = phoneCell;
    }
    
    //*********************************************************************
    // Utility Methods
    
    /**
     * Return a String representation of this object.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[").append(key).append("] ");
        sb.append(getLastName()).append(", ");
        sb.append(getFirstName()).append("  ");
        sb.append(df.format(getBirthDate())).append("  ");
        sb.append(getAddress()).append("  ");
        if(getPhoneHome() != null) sb.append(getPhoneHome()).append("  ");
        if(getPhoneCell() != null) sb.append(getPhoneCell());
        return (sb.toString());
    }
}

