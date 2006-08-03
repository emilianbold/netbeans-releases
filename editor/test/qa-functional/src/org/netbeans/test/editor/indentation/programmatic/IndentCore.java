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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.test.editor.indentation.programmatic;

import java.io.Writer;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;

import javax.swing.JEditorPane;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import javax.swing.text.EditorKit;

import org.openide.text.CloneableEditorSupport;
import org.openide.text.IndentEngine;
import java.util.Map;
import java.lang.reflect.Method;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
//import org.netbeans.modules.editor.java.JavaIndentEngine;//SPLIT-TEMP

/**<FONT COLOR="#CC3333" FACE="Courier New, Monospaced" SIZE="+1">
 * <B>                                                                             
 * <BR> Editor module API test: indentation - programmatic
 * </B>                                                                            
 * </FONT>                                                                         
 *
 * <P>
 * <B>What it tests:</B><BR>
 * This test should test internals of indentation engine. It should not test
 * indentation from user view. The principle is like java module is inserting
 * the text.
 * </P>
 *
 * <P>
 * <B>How it works:</B><BR>
 * An document with proper MIME type is created, it's indent engine is found,
 * and text is written to it.
 * </P>
 *
 * <P>
 * <B>Settings:</B><BR>
 * This test is not complete test, it's only stub, so for concrete test instance
 * it's necessary to provide file with text and MIME type. There are no else
 * settings needed, when executing on clean build.
 * </P>
 *
 * <P>
 * <B>Output:</B><BR>
 * The output should be formatted text. If an exception occurs, result is this
 * exception (it should not match any golden file!). If there is not enough
 * parametrs, the error message is the result.
 * </P>
 *
 * <P>
 * <B>Possible reasons of failure:</B><BR>
 * An exception when obtaining indent engine (for example if it doesn't exist).
 * An exception when writting to indent engine.
 * Possibly unrecognized MIME type.
 * Indent engine error.
 * </P>
 *
 * @author  Jan Lahoda
 * @version 1.0
 */
public class IndentCore extends java.lang.Object {

    /** Creates new IndentCore */
    public IndentCore() {
    }

    private static void setProperties(IndentEngine engine, Map indentationProperties) throws Exception {
        System.err.println(engine.getClass());

        BeanInfo info = Introspector.getBeanInfo(engine.getClass());
        PropertyDescriptor[] descriptors = info.getPropertyDescriptors();

        System.err.println(descriptors.length);

        for (int cntr = 0; cntr < descriptors.length; cntr++) {
            System.err.println("testing property=" + descriptors[cntr].getName());

            Object value = indentationProperties.get(descriptors[cntr].getName());
	    
	    if (value == null)
		continue;
            
            if (value.getClass() == String.class && descriptors[cntr].getPropertyType() != String.class) {
                PropertyEditor editor = PropertyEditorManager.findEditor(descriptors[cntr].getPropertyType());
                
                editor.setAsText((String )value);
                value = editor.getValue();
            }

	    System.err.println("setting property=" + descriptors[cntr].getName());
	    Method write = descriptors[cntr].getWriteMethod();
	    
	    write.invoke(engine, new Object[] {value});
        }

//        System.err.println("spacesPerTab=" + ((JavaIndentEngine) engine).getSpacesPerTab());  //SPLIT-TEMP
    }
    
    /** Finds the appropriate indentation writer for the java sources.
     * @param doc The document where it will be inserted in
     * @param offset The position in the document
     * @param writer The encapsulated writer
     */
    private static Writer findIndentWriter(Document doc, int offset, Writer writer, Map indentationProperties) throws Exception {
        IndentEngine engine = IndentEngine.find(doc); // NOI18N
//        IndentEngine engineClone = (IndentEngine) engine.createClone();

        setProperties(engine/*Clone*/, indentationProperties);

        Writer indentWriter = engine/*Clone*/.createWriter(doc, offset, writer);

        return indentWriter;
    }
    
    private static EditorKit createEditorKit(String mimeType) {
        return CloneableEditorSupport.getEditorKit(mimeType);
    }
    
    private static StyledDocument createDocument(EditorKit kit) {
        Document doc = kit.createDefaultDocument();
        
        if (doc instanceof StyledDocument) {
            return (StyledDocument)doc;
        } else {
            return new org.openide.text.FilterDocument(doc);
        }
    }
    
    private static String formatText(StyledDocument doc, int pos, String text, Map indentationProperties) throws Exception {
        StringWriter stringWriter = new StringWriter();
        Writer indentWriter = findIndentWriter(doc, pos, stringWriter, indentationProperties);
        indentWriter.write(text);
        indentWriter.close();
        
        return stringWriter.toString();
    }
    
    /**This method does text formatting.
     *
     * @param what to format
     * @param mimeType MIME type of the text - how it should be formated
     */
    public static String indent(String what, String mimeType, Map indentationProperties) throws Exception {
        Map oldProperties = saveIndentEngineProperties(mimeType);
        EditorKit kit = createEditorKit(mimeType);
        StyledDocument doc = createDocument(kit);
        
        String result = formatText(doc, 0, what, indentationProperties);
        
        loadIndentEngineProperties(mimeType, oldProperties);
        return result;
    }
    
    private static Map saveIndentEngineProperties(String mimeType) throws Exception {
        IndentEngine engine = IndentEngine.find(createDocument(createEditorKit(mimeType)));
        
        if (engine == null)
            throw new NullPointerException("No indent engine for MIMEType=" + mimeType + " found.");
        
        BeanInfo info = Introspector.getBeanInfo(engine.getClass());
        PropertyDescriptor[] descriptor = info.getPropertyDescriptors();
        Map result = new HashMap();
        
        for (int cntr = 0; cntr < descriptor.length; cntr++) {
            Method read = descriptor[cntr].getReadMethod();
            Object value = read.invoke(engine, new Object[] {});
            
//            System.err.println("getPropertyType()=" + descriptor[cntr].getPropertyType() + ", value.getClass()=" + value.getClass());
            result.put(descriptor[cntr].getName(), value);
        }
        
        return result;
    }
    
    private static void loadIndentEngineProperties(String mimeType, Map properties) throws Exception {
        IndentEngine engine = IndentEngine.find(createDocument(createEditorKit(mimeType)));
        
        if (engine == null)
            throw new NullPointerException("No indent engine for MIMEType=" + mimeType + " found.");
        
        setProperties(engine, properties);
    }

    /**Entry point for test testing. Log is created fromm System.err, ref is
     * created from System.out, agruments are passed to run directly. @see run
     *
     * @param args arguments for method run.
     */
    public static void main(String[] args) throws Exception {
        IndentCore eng = new IndentCore();

        Map indentationProperties = new HashMap();
        indentationProperties.put("expandTabs", "true");
        indentationProperties.put("spacesPerTab", "11");
        indentationProperties.put("javaFormatSpaceBeforeParenthesis", "true");
        indentationProperties.put("javaFormatLeadingStarInComment", "true");
        indentationProperties.put("javaFormatNewlineBeforeBrace", "true");

        eng.run(new PrintWriter(System.err),
                new PrintWriter(System.out),
                "data/testfiles/wholeMethod.txt",
                "text/x-java",
                indentationProperties);
        System.err.flush();
        System.out.flush();
    }
    
    /**Runs test. Test data are given via args.
     *
     * @param args in args[0] is given file with text to format (if the file 
     *                        is not found, this string is treated instead of it)
     *             in args[1] is given MIME type to format
     * @param log log writer
     * @param ref ref writer
     * @return formatted text.
     */
    public void run(PrintWriter log,
                    PrintWriter ref,
                    String      testFileName,
                    String      testFileMIMEType,
                    Map         indentationProperties) throws Exception {
        try {
            StringBuffer result = new StringBuffer();
            InputStream in = null;
            
            try {
                int read;
                
                in = this.getClass().getResourceAsStream(testFileName);
                
                while ((read = in.read()) != (-1)) {
                    result.append((char)read);
                }
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                    }
                }
            }
            ref.print(indent(result.toString(), testFileMIMEType, indentationProperties));
        } catch (Exception e) {
            e.printStackTrace(log);
            throw e;
        }
    }

}
