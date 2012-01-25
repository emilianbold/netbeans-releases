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
package org.glassfish.appscopedresources;

import java.sql.Connection;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * 
 * Utility class which gets and tests the resources
 * @author Bhakti Mehta
 */
public class Util {
    
    private final static String DEFAULT_DS_NAME = "jdbc/__default";
    
    
    public DataSource getDataSource(String dataSourceName) {
        Connection c = null;
        try {
            InitialContext ic = new InitialContext();
            return (DataSource) ic.lookup(dataSourceName);
        } catch (Exception e) {
            System.out.println("Exception when getting the ds");
        } finally {
            try {
                if (c != null) {
                    c.close();
                }
            } catch (Exception e) {
            }
        }
        return null;
    }

    
    public  void testResource(DataSource ds, String name) {
        boolean usingDefaultResource = false;
        try {
            if (ds == null) {
                ds = getDefaultGlobalResource();
                System.out.println("Test failed");
            } else {
                System.out.println("Actual resource " + name);
            }
           

        } catch (NamingException e) {
            System.out.println("Exception when testing the resource");
        }
    }
    
    public DataSource getDefaultGlobalResource() throws NamingException {
        InitialContext ic = new InitialContext();
        return (DataSource) ic.lookup(DEFAULT_DS_NAME);
    }
    
}
