/*
 * TestReadWrite.java
 *
 * Created on December 1, 2005, 4:59 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.model.readwrite;

/**
 *
 * @author Nam Nguyen
 */
public interface TestReadWrite {
    String getTestResourcePath();
    /**
     * Test reading in the schema specified by #getSchemaResourcePath.
     * Verifying the resulted model using a visitor or 
     * FindSchemaComponentFromDOM#findComponent method.
     */
    void testRead() throws Exception;
    
    //void testFidelity() throws Exception;
    
    /**
     * Reconstruct the schema pointed by #getSchemaResourcePath from empty schema.
     */
    void testWrite() throws Exception;
  
}
