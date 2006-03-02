/*
 * TestSchemaReadWrite.java
 *
 * Created on November 14, 2005, 5:19 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.model.readwrite;

import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;

/**
 *
 * @author nn136682
 */
public interface TestSchemaReadWrite {
    String getSchemaResourcePath();
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
