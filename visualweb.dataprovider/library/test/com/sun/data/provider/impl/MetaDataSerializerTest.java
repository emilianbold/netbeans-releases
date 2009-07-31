/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.data.provider.impl;

import junit.framework.TestCase;
         
/**
 * @author John Baker
 */
public class MetaDataSerializerTest extends TestCase {

    /** Default constructor.
     * @param testName name of particular test case
    */
    public MetaDataSerializerTest(String testName) {        
        super(testName);
    }                
    
    public void testGenerateFilenameI18n_allCols() throws Exception {
        String dataSourceName = "あおうえ";
        String command = "SELECT \n * \n FROM あおうえ.あおうえ";
        System.out.println(new MetaDataSerializer().generateFilename(dataSourceName, command));        
        assertTrue(new MetaDataSerializer().generateFilename(dataSourceName, command).equals(new MetaDataSerializer().generateFilename(dataSourceName, command)));  // local result
    } 
    
    public void testGenerateFilenameI18n() throws Exception {
        String dataSourceName = "あおうえ";
        String command = "SELECT あおう, あうえ, おうえ, あおうえ, あ, う, うえ, お, え FROM あおうえ.あおうえ";
        System.out.println(new MetaDataSerializer().generateFilename(dataSourceName, command));        
        assertTrue(new MetaDataSerializer().generateFilename(dataSourceName, command).equals(new MetaDataSerializer().generateFilename(dataSourceName, command)));  // local result
    } 
    
    public void testGenerateFilename() throws Exception {
        String dataSourceName = "Apache_TRAVEL";
        String command = "SELECT ALL \r TRAVEL.TRIP.TRIPID, TRAVEL.TRIP.PERSONID, TRAVEL.TRIP.DEPDATE, TRAVEL.TRIP.DESTCITY, TRAVEL.TRIP.TRIPTYPEID, TRAVEL.TRIP.LASTUPDATED  FROM TRAVEL.TRIP WHERE TRAVEL.TRIP.PERSONID = ?";
        System.out.println(new MetaDataSerializer().generateFilename(dataSourceName, command));
        assertTrue(new MetaDataSerializer().generateFilename(dataSourceName, command).equals(new MetaDataSerializer().generateFilename(dataSourceName, command)));  //local result
    }  
}
