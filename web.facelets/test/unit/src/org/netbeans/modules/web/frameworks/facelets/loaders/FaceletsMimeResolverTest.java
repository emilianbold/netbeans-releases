/*
 * FaceletsMimeResolverTest.java
 * JUnit based test
 *
 * Created on November 28, 2006, 8:09 PM
 */

package org.netbeans.modules.web.frameworks.facelets.loaders;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import junit.framework.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;
import org.openide.util.Exceptions;

/**
 *
 * @author petr
 */
public class FaceletsMimeResolverTest  extends FaceletLocalFileSystem {

    public FaceletsMimeResolverTest(String testName) {
        super(testName);
    }


    public void testResolver(){

        
        try {
            FaceletDataObject facelet = findDataObject("template01.xhtml");
            FileObject fileObject = facelet.getPrimaryFile();

            FaceletsMimeResolver resolver = new FaceletsMimeResolver();
            String mimeType = resolver.findMIMEType(fileObject);
            assertEquals("mimeType","text/x-facelet+x-jsp",mimeType);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }


    /**
     * Test of findFirstTag method, of class org.netbeans.modules.web.frameworks.facelets.loaders.FaceletsMimeResolver.
     */
//    public void testFindFirstTag() throws Exception {
//        System.out.println("findFirstTag");
//
//        StringReader sr = new StringReader("<html><head></head></html>");
//        FaceletsMimeResolver instance = new FaceletsMimeResolver();
//        String expResult = "<html>";
//
//        String result = instance.findFirstTag(sr);
//        assertEquals(expResult, result);
//
//        sr = new StringReader("<!-- comment -->\r\n\r\n\r\n<html><head></head></html>");
//        result = instance.findFirstTag(sr);
//        assertEquals("Case 1", expResult, result);
//
//        sr = new StringReader("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"><html><head></head></html>");
//        result = instance.findFirstTag(sr);
//        assertEquals("Case 2", expResult, result);
//
//        sr = new StringReader("<?xml version='1.0' encoding='UTF-8' ?><!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"><html><head></head></html>");
//        result = instance.findFirstTag(sr);
//        assertEquals("Case 3", expResult, result);
//
//        sr = new StringReader("<?xml version='1.0' encoding='UTF-8' ?><!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"><!-- commnet --><html><head></head></html>");
//        result = instance.findFirstTag(sr);
//        assertEquals("Case 4", expResult, result);
//
//        sr = new StringReader("<?xml version='1.0' encoding='UTF-8' ?><!-- <body> --><!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"><!-- commnet --><html><head></head></html>");
//        result = instance.findFirstTag(sr);
//        assertEquals("Case 5", expResult, result);
//
//
//    }
//
}
