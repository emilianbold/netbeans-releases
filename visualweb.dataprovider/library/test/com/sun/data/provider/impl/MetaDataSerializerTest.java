/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.data.provider.impl;

import junit.framework.TestCase;
         
/**
 * A Test based on NbTestCase. It is a NetBeans extension to JUnit TestCase
 * which among othres allows to compare files via assertFile methods, create
 * working directories for testcases, write to log files, compare log files
 * against reference (golden) files, etc.
 * 
 * More details here http://xtest.netbeans.org/NbJUnit/NbJUnit-overview.html.
 * 
 * @author John Baker
 */
public class MetaDataSerializerTest extends TestCase {

    /** Default constructor.
     * @param testName name of particular test case
    */
    public MetaDataSerializerTest(String testName) {        
        super(testName);
    }                
    
    public void testGenerateFilenameI18n() throws Exception {
        System.out.println(new MetaDataSerializer().generateFilename("????", "SELECT \n * \n FROM ????.????"));        
        assertTrue(new MetaDataSerializer().generateFilename("????", "SELECT \n * \n FROM ????.????").equals("1725849164"));
    } 
    public void testGenerateFilename() throws Exception {
        System.out.println(new MetaDataSerializer().generateFilename("Apache_TRAVEL", "SELECT ALL \r TRAVEL.TRIP.TRIPID, TRAVEL.TRIP.PERSONID, TRAVEL.TRIP.DEPDATE, TRAVEL.TRIP.DESTCITY, TRAVEL.TRIP.TRIPTYPEID, TRAVEL.TRIP.LASTUPDATED  FROM TRAVEL.TRIP WHERE TRAVEL.TRIP.PERSONID = ?"));
        assertTrue(new MetaDataSerializer().generateFilename("Apache_TRAVEL", "SELECT ALL \r TRAVEL.TRIP.TRIPID, TRAVEL.TRIP.PERSONID, TRAVEL.TRIP.DEPDATE, TRAVEL.TRIP.DESTCITY, TRAVEL.TRIP.TRIPTYPEID, TRAVEL.TRIP.LASTUPDATED  FROM TRAVEL.TRIP WHERE TRAVEL.TRIP.PERSONID = ?").equals("1084670897"));
    }  
}
