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

import java.util.*;
import java.text.*;

/**
 * Customers Datastore.
 *
 * @author Pierre Delisle
 * @version $Revision$ $Date$
 */

public class Customers {
    
    //*********************************************************************
    // Instance variables
    
    private static Vector customers = new Vector();
    private static int nextSeqNo = 0;
    
    //*********************************************************************
    // Datastore operations
    
    public static void create(
    String lastName,
    String firstName,
    String birthDate,
    String line1,
    String line2,
    String city,
    String state,
    String zip,
    String country) {
        create(lastName, firstName, birthDate, line1, line2, city, state, zip,
        country, null, null);
    }
    
    /**
     *  Create new customer
     */
    public static void create(
    String lastName,
    String firstName,
    String birthDate,
    String line1,
    String line2,
    String city,
    String state,
    String zip,
    String country,
    String phoneHome,
    String phoneCell) {
        Customer customer =
        new Customer(++nextSeqNo, lastName, firstName,
        genDate(birthDate), genAddress(line1, line2, city, state, zip, country),
        phoneHome, phoneCell);
        customers.add(customer);
    }
    
    /**
     * Find all customers
     */
    public static Collection findAll() {
        return customers;
    }
    
    //*********************************************************************
    // Utility methods
    
    private static Date genDate(String dateString) {
        DateFormat df = new SimpleDateFormat("M/d/y");
        Date date;
        try {
            date = df.parse(dateString);
        } catch (Exception ex) {
            date = null;
        }
        return date;
    }
    
    private static Address genAddress(String line1, String line2, String city,
    String state, String zip, String country) {
        return new Address(line1, line2, city, state, zip, country);
    }
}
