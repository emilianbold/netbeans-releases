/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.nbbuild;

import java.io.*;
import java.lang.reflect.*;

import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.*;


/**
 * Utility class collecting library methods related to XML processing.
 *
 * @author  Petr Kuzel
 */
final class XMLUtil extends Object {

    public static Document parse (
            InputSource input, 
            boolean validate, 
            boolean namespaceAware,
            ErrorHandler errorHandler,             
            EntityResolver entityResolver
        ) throws IOException, SAXException {

	// Workaround because of Xerces parser - it didn't allow java encoding
	// by default. There was not problem found in other parsers.
	// If no exerces parser we'll use standard JAXP
	Class xercesParser = null;
	try {
	    xercesParser = Class.forName("org.apache.xerces.parsers.DOMParser");
	}
	catch( Exception ex ) {
	    // Class don't exists - we have no xerces parser....
	}
	if (xercesParser != null) {

	    // We don't like it, but we have to use it, so deal with using reflection API
	    try {
		// Create DOMParser using default constructor
		Object parser = xercesParser.newInstance();
		Class[] paramT1 = {String.class, Boolean.TYPE};
		Object[] param1 = {"http://apache.org/xml/features/allow-java-encodings",Boolean.TRUE};
		// parser.setFeature("http://apache.org/xml/features/allow-java-encodings", true);
		xercesParser.getMethod("setFeature",paramT1).invoke(parser,param1);

		if (errorHandler != null) {
		    Class[] paramT2 = {ErrorHandler.class};
		    Object[] param2 = {errorHandler};
		    //parser.setErrorHandler(errorHandler);
		    xercesParser.getMethod("setErrorHandler",paramT2).invoke(parser,param2);
		    
		}
        
		if (entityResolver != null) {
		    Class[] paramT3 = {EntityResolver.class};
		    Object[] param3 = {entityResolver};
		    //parser.setEntityResolver(entityResolver);
		    xercesParser.getMethod("setEntityResolver",paramT3).invoke(parser,param3);
		}
		Class[] paramT4 = {InputSource.class};
		Object[] param4 = {input};
		//parser.parse( input );
		xercesParser.getMethod("parse",paramT4).invoke(parser,param4);

		//return parser.getDocument();
		return 
		    (Document) xercesParser.getMethod("getDocument",null).invoke(parser,null);
	    } 
	    catch (Exception e) {
		System.err.println("could not set parser feature");
		e.printStackTrace();
	    }
	}

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();        
        factory.setValidating(validate);
        factory.setNamespaceAware(namespaceAware);            
        DocumentBuilder builder = null;
        try {
             builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            throw new SAXException("Cannot create parser satisfying configuration parameters", ex);  //NOI18N
        }
            
        if (errorHandler != null) {
            builder.setErrorHandler(errorHandler);
        }
        
        if (entityResolver != null) {
            builder.setEntityResolver(entityResolver);
        }

        return builder.parse(input);            
    }
    
    public static Document createDocument(String rootQName) throws DOMException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        org.w3c.dom.Document doc = null;
        
        try {
            doc = factory.newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException ex) {
            throw new DOMException(DOMException.NOT_SUPPORTED_ERR , "Cannot create parser satisfying configuration parameters");  //NOI18N
        }        
        
        return doc;
    }
    
    private static DOMImplementation getDOMImplementation() throws DOMException { //can be made public
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    
        try {
            return factory.newDocumentBuilder().getDOMImplementation();
        } catch (ParserConfigurationException ex) {
            throw new DOMException(DOMException.NOT_SUPPORTED_ERR , "Cannot create parser satisfying configuration parameters");  //NOI18N
        }        
    }
    
    static void write(Document doc, OutputStream out) throws IOException {
        write( doc, out, "UTF-8" );
    }
        
    private static void write(Document doc, OutputStream out, String encoding) throws IOException {
        
        Class dock = doc.getClass();
                        
        // no implementation neutral write exist
        try {
            if ("com.sun.xml.tree.XmlDocument".equals(dock.getName())           //NOI18N
            || "org.apache.crimson.tree.XmlDocument".equals(dock.getName())) {  //NOI18N
                
                // these DOM implementations are self writing
                Method write = dock.getDeclaredMethod("write", new Class[] {Writer.class});//NOI18N
                write.invoke(doc,new Object[] {out});            
                
            } else {
                
                Class serka = 
                    Class.forName("org.apache.xml.serialize.XMLSerializer");   //NOI18N

                Class forka =
                    Class.forName("org.apache.xml.serialize.OutputFormat");    //NOI18N
                
                Object serin = serka.newInstance();                
                Object forin = forka.newInstance();

                // hopefully it could improve output readability
                
                Method setmet = null;
                
                setmet = forka.getMethod("setMethod", new Class[] {String.class}); //NOI18N                
                setmet.invoke(forin, new Object[] {"xml"});                        //NOI18N                
                
                setmet = forka.getMethod("setIndenting", new Class[] {Boolean.TYPE}); //NOI18N                
                setmet.invoke(forin, new Object[] {Boolean.TRUE});                    //NOI18N

                setmet = forka.getMethod("setLineWidth", new Class[] {Integer.TYPE}); //NOI18N                
                setmet.invoke(forin, new Object[] {new Integer(0)});                  //NOI18N                
                
                Method init = serka.getMethod("setOutputByteStream", new Class[] {OutputStream.class});  //NOI18N
                init.invoke(serin, new Object[] {out});                                            
                    
                Method setenc = forka.getMethod("setEncoding", new Class[] {String.class});  //NOI18N              
                setenc.invoke(forin, new Object[] {encoding} );                
                
                Method setout = serka.getMethod("setOutputFormat", new Class[] {forka});     //NOI18N
                setout.invoke(serin, new Object[] {forin});                
                
                Method asDOM = serka.getMethod("asDOMSerializer", new Class[0]);//NOI18N
                Object impl = asDOM.invoke(serin, new Object[0]);

                Method serialize = impl.getClass().getMethod("serialize", new Class[] {Document.class}); //NOI18N
                serialize.invoke(impl, new Object[] {doc});
                  
            }
            
        } catch (IllegalAccessException ex) {
            handleImplementationException(ex);
        } catch (InstantiationException ex) {
            handleImplementationException(ex);
        } catch (IllegalArgumentException ex) {
            handleImplementationException(ex);
        } catch (NoSuchMethodException ex) {
            handleImplementationException(ex);
        } catch (ClassNotFoundException ex) {
            handleImplementationException(ex);
        } catch (InvocationTargetException ex) {
            handleTargetException(ex);
        }
    
    }
    
    
    /** TargetException handler */
    private static void handleTargetException(InvocationTargetException ex) throws IOException {
        Throwable t = ex.getTargetException();
        if (t instanceof IOException) {
            throw (IOException) t;
        } else if (t instanceof RuntimeException) {
            throw (RuntimeException) t;
        } else if (t instanceof Error) {
            throw (Error) t;
        } else {
            throw new Error(ex.toString());
        }
    }
    
    private static void handleImplementationException(Exception ex) throws IOException {
        StringWriter wr = new StringWriter();
        ex.printStackTrace(new PrintWriter(wr));  //jessie could you provide the [catch] code
        throw new IOException("Unsupported DOM Document implementation!\n" + wr.toString() ); // NOI18N        
    }

    
}
