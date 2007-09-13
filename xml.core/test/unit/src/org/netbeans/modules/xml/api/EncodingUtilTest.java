/*
 * EncodingUtilTest.java
 * JUnit based test
 *
 * Created on September 13, 2007, 4:10 PM
 */

package org.netbeans.modules.xml.api;

import junit.framework.TestCase;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Samaresh
 */
public class EncodingUtilTest extends TestCase {
    
    public EncodingUtilTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testIsValidEncoding() {
        //try null
        String encoding = null;
        boolean result = EncodingUtil.isValidEncoding(encoding);
        assert(false == result);
        
        //try some junk
        encoding = "junk";
        result = EncodingUtil.isValidEncoding(encoding);
        assert(false == result);
        
        //try valid encoding string
        encoding = "UTF-8";
        result = EncodingUtil.isValidEncoding(encoding);
        assert(true == result);
    } /* Test of isValidEncoding method, of class EncodingUtil. */

    public void testGetProjectEncoding() throws Exception {
        //TODO
        FileObject file = null;
        assert(true);
    }
    
}
