/*
 * Util.java
 *
 * Created on October 4, 2005, 7:48 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.model;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Collection;
import javax.swing.text.Document;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.xam.AbstractModel;

/**
 *
 * @author nn136682
 */
public class Util {
    public static final String EMPTY_XSD = "resources/Empty.wsdl";
    
    public static Document getResourceAsDocument(String path) throws Exception {
        InputStream in = Util.class.getResourceAsStream(path);
        return loadDocument(in);
    }
    
    public static Document loadDocument(InputStream in) throws Exception {
	Document sd = new org.netbeans.editor.BaseDocument(
            org.netbeans.modules.xml.text.syntax.XMLKit.class, false);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        StringBuffer sbuf = new StringBuffer();
        try {
            String line = null;
            while ((line = br.readLine()) != null) {
                sbuf.append(line);
                sbuf.append(System.getProperty("line.separator"));
            }
        } finally {
            br.close();
        }
        sd.insertString(0,sbuf.toString(),null);
        return sd;
    }
    
    public static int count = 0;
    public static WSDLModel loadWSDLModel(String resourcePath) throws Exception {
        NamespaceLocation nl = NamespaceLocation.valueFromResourcePath(resourcePath);
        if (nl != null) {
            return TestResolver.getDefault().getWSDLModel(nl);
        }
        String location = resourcePath.substring(resourcePath.lastIndexOf('/')+1);
        URI locationURI = new URI(location);
        TestResolver.getDefault().addURI(locationURI, getResourceURI(resourcePath));
        return TestResolver.getDefault().getWSDLModel(locationURI);
    }
    
    public static WSDLModel createEmptyWSDLModel() throws Exception {
        return loadWSDLModel(EMPTY_XSD);
    }
    
    /*public static WSDLModel loadWSDLModel(Document doc) throws Exception {
        return WSDLModelFactory.getDefault().getModel(doc);
    }*/
    
    public static void dumpToStream(Document doc, OutputStream out) throws Exception{
        PrintWriter w = new PrintWriter(out);
        w.print(doc.getText(0, doc.getLength()));
        w.close();
        out.close();
    }
    
    public static void dumpToFile(Document doc, File f) throws Exception {
        if (! f.exists()) {
            f.createNewFile();
        }
        OutputStream out = new BufferedOutputStream(new FileOutputStream(f));
        PrintWriter w = new PrintWriter(out);
        w.print(doc.getText(0, doc.getLength()));
        w.close();
        out.close();
    }
    
    public static File dumpToTempFile(Document doc) throws Exception {
        File f = File.createTempFile("xsm", "xsd");
        dumpToFile(doc, f);
        return f;
    }
    
    public static WSDLModel dumpAndReloadModel(Document doc) throws Exception {
        File f = dumpToTempFile(doc);
        URI dumpURI = new URI("dummyDump" + count++);
        TestResolver.getDefault().addURI(dumpURI, f.toURI());
        return TestResolver.getDefault().getWSDLModel(dumpURI);
    }
    
    public static Document loadDocument(File f) throws Exception {
        InputStream in = new BufferedInputStream(new FileInputStream(f));
        return loadDocument(in);
    }
    
    public static URI getResourceURI(String path) throws RuntimeException {
        try {
            return Util.class.getResource(path).toURI();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public static File getTempDir(String path) throws Exception {
        File tempdir = new File(System.getProperty("java.io.tmpdir"), path);
        tempdir.mkdirs();
        return tempdir;
    }

    public static GlobalSimpleType getPrimitiveType(String typeName){
        SchemaModel primitiveModel = SchemaModelFactory.getDefault().getPrimitiveTypesModel();
        Collection<GlobalSimpleType> primitives = primitiveModel.getSchema().getSimpleTypes();
        for(GlobalSimpleType ptype: primitives){
            if(ptype.getName().equals(typeName)){
                return ptype;
            }
        }
        return null;
    }

    public static Document setDocumentContentTo(Document doc, InputStream in) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        StringBuffer sbuf = new StringBuffer();
        try {
            String line = null;
            while ((line = br.readLine()) != null) {
                sbuf.append(line);
                sbuf.append(System.getProperty("line.separator"));
            }
        } finally {
            br.close();
        }
        doc.remove(0, doc.getLength());
        doc.insertString(0,sbuf.toString(),null);
        return doc;
    }
    
    public static Document setDocumentContentTo(Document doc, String resourcePath) throws Exception {
        return setDocumentContentTo(doc, Util.class.getResourceAsStream(resourcePath));
    }

    public static void setDocumentContentTo(WSDLModel model, String resourcePath) throws Exception {
        Document doc = ((AbstractModel)model).getBaseDocument();
        setDocumentContentTo(doc, Util.class.getResourceAsStream(resourcePath));
    }
}
