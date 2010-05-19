package org.netbeans.modules.bpel.model.xam;

/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.URI;
import java.nio.channels.FileChannel;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.xml.text.syntax.XMLKit;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Exit;
import org.netbeans.modules.bpel.model.api.PatternedCorrelation;
import org.netbeans.modules.bpel.model.api.PatternedCorrelationContainer;
import org.netbeans.modules.bpel.model.api.ReThrow;
import org.netbeans.modules.bpel.model.impl.BpelModelImpl;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @deprecated try using org.netbeans.modules.soa.ui.TestUtils instead.
 */
public class Util {

    static final String DATA = "data";

    static final String PROCESS = "process.bpel";

    static final String WORKDIR = System.getProperty("xtest.workdir");

    static final String MY_DATA = WORKDIR + File.separator + "data";


    public static Document loadDocument() throws Exception {
        return loadDocument(Util.class.getResourceAsStream("data/process.bpel"));
    }

//    public static BpelModel loadModel() throws Exception {
//        return null;
//    }

    public static BpelModel loadBpelModel(Class clazz, String relativePath) 
            throws Exception
    {
        Document doc = loadDocument(clazz.getResourceAsStream(relativePath));
        BpelModelImpl model = new BpelModelImpl(doc, null);
        model.sync();
        return model;
    }

    public static BpelModelImpl loadModel() throws Exception {
        Document doc = loadDocument();
        BpelModelImpl model = new BpelModelImpl( doc , null );
        model.sync();
        return model;
    }

    public static BpelModelImpl loadModel( String string ) throws Exception {
        Document doc = loadDocument( string );
        BpelModelImpl model = new BpelModelImpl( doc ,  null);
        model.sync();
        return model;
    }


    public static Document getResourceAsDocument(String path) throws Exception {
        InputStream in = Util.class.getResourceAsStream(path);
        return loadDocument(in);
    }

    public static Document loadDocument(InputStream in) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuffer sbuf = new StringBuffer();
        try {
            String line = null;
            while ((line = reader.readLine()) != null) {
                sbuf.append(line);
                sbuf.append(System.getProperty("line.separator"));
            }
        } finally {
            reader.close();
        }
        return loadDocument( sbuf.toString() );
    }

    public static Document loadDocument( String string ) throws Exception {
        Document document = new BaseDocument(
                XMLKit.class, false);
        document.insertString(0, string ,null);
        return document;
    }



    public static void dumpToStream(Document doc, OutputStream out) throws Exception{
        PrintWriter w = new PrintWriter(out);
        w.print(doc.getText(0, doc.getLength()));
        w.close();
        out.close();
    }

    public static void dumpToFile(Document doc, File f) throws Exception {
        OutputStream out = new BufferedOutputStream(new FileOutputStream(f));
        PrintWriter w = new PrintWriter(out);
        w.print(doc.getText(0, doc.getLength()));
        w.close();
        out.close();
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

    static void createData(){
        URI uri = getResourceURI( DATA );
        File file = new File( uri );
        File[] files = file.listFiles();

        File my_Data = new File( MY_DATA );
        if ( my_Data.exists() && !my_Data.isDirectory() ) {
            my_Data.delete();
        }
        if ( !my_Data.exists() ) {
            my_Data.mkdir();
        }

        for (File fil : files) {
            try {
                FileChannel srcChannel = new FileInputStream( fil ).getChannel();

                FileChannel dstChannel =
                    new FileOutputStream( my_Data.getAbsolutePath() +
                            File.separator +fil.getName() ).getChannel();

                dstChannel.transferFrom(srcChannel, 0, srcChannel.size());

                srcChannel.close();
                dstChannel.close();
            } catch (IOException e) {
            }
        }
    }

    public static void debug(String str) {
        try {
            BufferedWriter writer = new BufferedWriter( new FileWriter( LOG, true) );
            writer.write( str + "\n");
            writer.flush();
            writer.close();
        }
        catch (IOException e) {
        }
    }

    public static org.w3c.dom.Document flush( BpelModelImpl model ) {
        return flush( model, null);
    }

    public static org.w3c.dom.Document flush( BpelModelImpl model ,
            StringBuilder content )
    {
        //model.getAccess().flush();

        Exception exception = null;
        try {
            String str = model.getBaseDocument().
            getText( 0, model.getBaseDocument().getLength() );
            if ( content!= null ) {
                content.append( str );
            }
            DocumentBuilder builder =
                DocumentBuilderFactory.newInstance().newDocumentBuilder();
            return builder.parse( new InputSource( new StringReader( str )) );
        }
        catch (ParserConfigurationException e) {
            exception = e;
        }
        catch (BadLocationException e) {
            assert false;
        }
        catch (SAXException e) {
            exception = e;
        }
        catch (IOException e) {
            exception = e;
        }
        if ( exception != null ) {
            AssertionError error = new AssertionError( );
            error.initCause( exception );
            throw error;
        }
        return null;
    }

    public static String getTagName( BpelEntity entity ){
        if ( entity instanceof PatternedCorrelation ){
            return "correlation";
        }
        else if ( entity instanceof PatternedCorrelationContainer ) {
            return "correlations";
        }
        else if ( entity instanceof Exit ){
            return "exit";
        }
        else if ( entity instanceof ReThrow ){
            return "rethrow";
        }
        /*else if ( entity instanceof VariableContainer ){
            return "variables";
        }
        else if ( entity instanceof CorrelationSetContainer ){
            return "correlationSets";
        }*/
        String name = entity.getClass().getSimpleName();
        int index = name.indexOf("ContainerImpl");
        /* here we handle some set of entitis that have "s" on the end
         * of his tag name and they implements interface with Container
         * word at the end.
         */
        if ( index!= -1 ) {
            name = name.substring( 0 , index );
            char ch = name.charAt(0);
            return Character.toLowerCase( ch ) +name.substring( 1 )+"s";
        }
        index = name.indexOf("Impl");
        name = name.substring( 0 , index );
        char ch = name.charAt(0);
        return Character.toLowerCase( ch ) +name.substring( 1 );
    }

    private static  String LOG = WORKDIR +File.separator+"bpeltests.log";

    static {
        registerXMLKit();
    }

    public static void registerXMLKit() {
        String[] path = new String[] { "Editors", "text", "xml" };
        FileObject target = Repository.getDefault().getDefaultFileSystem().getRoot();
        try {
            for (int i=0; i<path.length; i++) {
                FileObject f = target.getFileObject(path[i]);
                if (f == null) {
                    f = target.createFolder(path[i]);
                }
                target = f;
            }
            String name = "EditorKit.instance";
            if (target.getFileObject(name) == null) {
                FileObject f = target.createData(name);
                f.setAttribute("instanceClass", "org.netbeans.modules.xml.text.syntax.XMLKit");
            }
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
