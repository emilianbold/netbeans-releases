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

/**
 * Object that represents a Customer.
 *
 * @author Pierre Delisle
 * @version $Revision$ $Date$
 */

public class Address {
    
    //*********************************************************************
    // Instance variables
    
    /** Holds value of property line1. */
    private String line1;
    
    /** Holds value of property line2. */
    private String line2;
    
    /** Holds value of property city. */
    private String city;
    
    /** Holds value of property zip. */
    private String zip;

    /** Holds value of property state. */
    private String state;
    
    /** Holds value of property country. */
    private String country;
    
    //*********************************************************************
    // Constructor
    
    public Address(String line1, String line2, String city,
    String state, String zip, String country) {
        setLine1(line1);
        setLine2(line2);
        setCity(city);
        setState(state);
        setZip(zip);
        setCountry(country);
    }
    
    //*********************************************************************
    // Accessors
    
    /** Getter for property line1.
     * @return Value of property line1.
     */
    public String getLine1() {
        return line1;
    }
    
    /** Setter for property line1.
     * @param line1 New value of property line1.
     */
    public void setLine1(String line1) {
        this.line1 = line1;
    }
    
    /** Getter for property line2.
     * @return Value of property line2.
     */
    public String getLine2() {
        return line2;
    }
    
    /** Setter for property line2.
     * @param line2 New value of property line2.
     */
    public void setLine2(String line2) {
        this.line2 = line2;
    }
    
    /** Getter for property city.
     * @return Value of property city.
     */
    public String getCity() {
        return city;
    }
    
    /** Setter for property city.
     * @param city New value of property city.
     */
    public void setCity(String city) {
        this.city = city;
    }
    
    /** Getter for property zip.
     * @return Value of property zip.
     */
    public String getZip() {
        return zip;
    }
    
    /** Setter for property zip.
     * @param zip New value of property zip.
     */
    public void setZip(String zip) {
        this.zip = zip;
    }
    
    /** Getter for property country.
     * @return Value of property country.
     */
    public String getCountry() {
        return country;
    }
    
    /** Setter for property country.
     * @param country New value of property country.
     */
    public void setCountry(String country) {
        this.country = country;
    }
    
    /** Getter for property state.
     * @return Value of property state.
     */
    public String getState() {
        return state;
    }
    
    /** Setter for property state.
     * @param state New value of property state.
     */
    public void setState(String state) {
        this.state = state;
    }
    
    //*********************************************************************
    // Utility Methods
    
    /**
     * Return a String representation of this object.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(line1).append(" ");
        sb.append(city).append(" ");
        sb.append(country);
        return (sb.toString());
    } 
}
