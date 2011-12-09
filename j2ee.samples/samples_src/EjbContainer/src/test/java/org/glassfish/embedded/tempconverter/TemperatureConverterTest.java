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
package org.glassfish.embedded.tempconverter;

import javax.ejb.embeddable.EJBContainer;
import javax.naming.Context;
import javax.naming.NamingException;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * EjbContainer Unit test for simple TemeeratureConverter EJB.
 * @author Bhakti Mehta
 */
public class TemperatureConverterTest 
    extends TestCase {
    
     private Context  ctx;
     private EJBContainer    ejbContainer;
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public TemperatureConverterTest( String testName ) {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
    
        return new TestSuite( TemperatureConverterTest.class );
    }

    @BeforeClass
    @Override
    public  void setUp() {
        ejbContainer = EJBContainer.createEJBContainer();
        System.out.println("Opening the container" );
        ctx = ejbContainer.getContext();
    }

    @AfterClass
    @Override
    public  void tearDown() {
        ejbContainer.close();
        System.out.println("Closing the container" );
    }
    
    public void testApp() {
    
        
        try {
            TemperatureConverter converter = (TemperatureConverter) ctx.lookup("java:global/classes/TemperatureConverter");
            assertNotNull(converter);
            double f = converter.getFarenheitFromCelcius(10);
            assertEquals(50.0, f);
            System.out.println("Converting temperature 10 degress celcuis = " + f +" degrees farenheit" );
            
            double c = converter.getCelciusFromFarenheit(50.0);
            assertEquals(10.0, c);
            System.out.println("Converting temperature 50 degress farenheit = " + c +" degrees celcius" );
            
            
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    
}
