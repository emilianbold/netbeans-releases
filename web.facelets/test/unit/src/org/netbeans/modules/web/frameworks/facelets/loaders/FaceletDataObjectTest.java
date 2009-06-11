/*
 * FaceletDataObjectTest.java
 * JUnit based test
 *
 * Created on December 1, 2006, 9:35 PM
 */

package org.netbeans.modules.web.frameworks.facelets.loaders;

import java.io.File;
import java.util.Enumeration;
import javax.swing.text.StyledDocument;
import junit.framework.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import org.netbeans.api.xml.cookies.CheckXMLCookie;
import org.netbeans.modules.web.frameworks.facelets.editor.FaceletsAnnotationManager;
import org.netbeans.modules.web.frameworks.facelets.editor.FaceletsEditorErrors;
import org.netbeans.modules.web.frameworks.facelets.editor.FaceletsEditorSupport;
import org.netbeans.spi.xml.cookies.CheckXMLSupport;
import org.netbeans.spi.xml.cookies.DataObjectAdapters;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataLoaderPool;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.text.DataEditorSupport;
import org.openide.util.Enumerations;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 *
 * @author Petr Pisl
 */
public class FaceletDataObjectTest extends FaceletLocalFileSystem {
    
    public FaceletDataObjectTest(String testName) {
        super(testName);
    }
    
    /**
     * Test of updateNode method, of class org.netbeans.modules.web.frameworks.facelets.loaders.FaceletDataObject.
     */
    public void testUpdateNode() throws Exception {
        
        String resource = "template01.xhtml";
        FaceletDataObject facelet = findDataObject(resource);
        InputStream is = facelet.getPrimaryFile().getInputStream();
        
        SAXException expResult = null;
        SAXException result = facelet.updateNode(is);
        assertNull(resource, result);
        
        resource = "template02.xhtml";
        facelet = findDataObject(resource);
        is = facelet.getPrimaryFile().getInputStream();
        result = facelet.updateNode(is);
        assertNull(resource, result);
        assertNotNull(resource, facelet.error);
        assertEquals(resource + " - line", 20, facelet.error.getLine());
        assertEquals(resource + " - column", 13, ((FaceletsEditorErrors.ParseError)facelet.error).getColumn());
        
    } 
    
    
    public static StyledDocument getDocument (FaceletDataObject facelet){
        facelet.getEditorSupport().edit();
        
        return facelet.getEditorSupport().getDocument();
    }
}
