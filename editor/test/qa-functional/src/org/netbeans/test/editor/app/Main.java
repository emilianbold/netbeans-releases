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

package org.netbeans.test.editor.app;

import org.netbeans.modules.java.editor.options.JavaOptions;
import org.netbeans.test.editor.app.core.*;
import org.netbeans.test.editor.app.util.*;
import org.netbeans.test.editor.app.gui.actions.*;
import org.netbeans.test.editor.app.gui.*;
import org.netbeans.test.editor.app.core.actions.*;
import org.netbeans.test.editor.app.core.cookies.*;

import org.openide.loaders.XMLDataObject;
import org.openide.xml.XMLUtil;

import org.openide.options.SystemOption;

import java.beans.*;
import java.io.*;
import java.net.URL;
import javax.swing.SwingUtilities;
import java.util.*;
import javax.xml.parsers.*;  //!!!

import org.w3c.dom.*;
import org.w3c.dom.Document;
import org.apache.xerces.parsers.DOMParser;
import org.netbeans.modules.editor.html.HTMLKit;
import org.netbeans.modules.editor.java.JavaKit;
import org.netbeans.modules.editor.options.BaseOptions;
import org.netbeans.modules.html.editor.options.HTMLOptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.ProxyLookup;
import org.xml.sax.InputSource;



/** Static class for execution of Editor Test Application.
 * @author ehucka
 * @version 2.0
 */

public class Main extends java.lang.Object {
    
    public static Test test;
    public static TestEditorFrame frame;
    private static boolean changed=false;
    
    private static String  fileName = null;
    
    public  static PrintStream log;
    
    private static PrintStream oldErr;
    
    private static boolean debug = true;
    
    private static boolean compAutoPopup;
    private static boolean docAutoPopup;
    private static boolean htmlAutoPopup;
    
    private static boolean succeded = false;
    
    public static Thread mainThread=null;
    
    private static void registerActions() {
        ActionRegistry.clear();
        ActionRegistry.getDefault().addAction(PerformCookie.class, new TestExecuteAction());
        ActionRegistry.getDefault().addAction(LoggingCookie.class, new TestStartLoggingAction());
        ActionRegistry.getDefault().addAction(LoggingCookie.class, new TestStopLoggingAction());
        ActionRegistry.getDefault().addAction(PackCookie.class, new TestPackAction());
        TestGroup.createNewTypes();
    }
    
    public static boolean getSucceded() {
        return succeded;
    }
    
    private static void performTest(String[] args) {
        int count = args.length;
        String lastOpened = null;
        
        if (count % 2 == 1)
            count--;
        for (int cntr = 0; cntr < count; cntr += 2) {
            if (args[cntr] == null) {
                Main.log("Supposed to open *null* file - impossible!");
                succeded = false;
                return;
            }
            if (!args[cntr].equals(lastOpened)) {
                if (!openTest(args[cntr])) {
                    Main.log("While trying to open file: " + args[0] + " an unexcpected error occurs.");
                    succeded = false;
                    return;
                } else {
                    lastOpened = args[cntr];
                }
            }
            if (args[cntr + 1].length() > 0) {
                Test.setTesting();
                test.perform(args[cntr + 1]);
            }
        }
        succeded = true;
    }
    
    private static void backupAndSettings() {
        OutputStream file = null;
        try {
            log = new PrintStream(new MultipleOutputStream(new OutputStream[] {
                new OutputStream() {
                    char[] buffer=new char[512];
                    int i=0;
                    public void write(int b) {
                        buffer[i++]=(char)b;
                        if (b == '\n') {
                            if (frame != null) {
                                frame.appendHistory(new String(buffer,0,i));
                            }
                            i=0;
                        } else if (i == 512) {
                            if (frame != null) {
                                frame.appendHistory(new String(buffer));
                            }
                            i=0;
                        }
                    }
                }, System.err
            }));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        oldErr = System.err;
        System.setErr(log);
        Main.log("Setting log file finished.");
        
        JavaOptions opts = (JavaOptions)(SystemOption.findObject(JavaOptions.class));
        if (opts == null) {
            System.err.println("Didn't find Java options from SystemOptions. Try Lookup for Base Options...");
            BaseOptions bo;
            bo=(BaseOptions)(Lookup.getDefault().lookup(BaseOptions.class));
            opts = (JavaOptions)(bo.getOptions(JavaKit.class));
            if (opts == null) {
                System.err.println("Base Options don't contain JavaKit Options.");
                return;
            }
        }
        compAutoPopup = opts.getCompletionAutoPopup();
        opts.setCompletionAutoPopup(false);
        docAutoPopup = opts.getJavaDocAutoPopup();
        opts.setJavaDocAutoPopup(false);
        
        HTMLOptions hopts = (HTMLOptions)(SystemOption.findObject(HTMLOptions.class));
        if (hopts == null) {
            System.err.println("Didn't find HTML options from SystemOptions. Try Lookup for Base Options...");
            BaseOptions bo;
            bo=(BaseOptions)(Lookup.getDefault().lookup(BaseOptions.class));
            hopts = (HTMLOptions)(bo.getOptions(HTMLKit.class));
            if (hopts == null) {
                System.err.println("Base Options don't contain JavaKit Options.");
                return;
            }
        }
        htmlAutoPopup = hopts.getCompletionAutoPopup();
        hopts.setCompletionAutoPopup(false);
    }
    
    /** Start Editor Test Application. It must be started by Internal Execurion in IDE.
     * User can starts it either for test design or for automated test execution.
     * For automated test execution <CODE>args</CODE> contains name of the test file
     * and name of called Call Action.
     * @param args test file and call action's name
     */
    public static void main(String args[]) {
        //backup completion autopopup option state
        backupAndSettings();
        frame= new TestEditorFrame();
        
        //Editor kit heve to be set in AWT thread
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    frame.getEditor().setEditorKit(1);
                }
            });
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }        
        if (args.length > 0) {
            frame.show();
            System.err.println("to perform");
            performTest(args);
            Main.log("Performing finished!");
            //            System.err.println(frame.getEditor().getText());
            frame.killFrame();
            closeTest();
            finish();
            return;
        }
        registerActions();
        test = new Test("Default");
        test.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(final java.beans.PropertyChangeEvent p1) {
                changed=true;
            }
        });
        frame.setTest(test);
        test.logger = new Logger(frame.getEditor());
        frame.newRootWindow();
        frame.show();
    }
    
    public static void setFileName(String name) {
        fileName = name;
        if (frame != null) {
            frame.setTitleFileName(name);
        }
    }
    
    public static String getFileName() {
        return fileName;
    }
    
    public static boolean newTest() {
        if (changed) return false;
        test = new Test("Default");
        test.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(final java.beans.PropertyChangeEvent p1) {
                changed=true;
            }
        });
        test.logger = new Logger(frame.getEditor());
        frame.setTest(test);
        setFileName("");
        log("New test created");
        return true;
    }
    
    public static Test loadTest(URL testFile) {
        setFileName(null);
        log("Test "+fileName+" opened.");
        try {
            return loadTest(testFile.openStream());
        } catch( IOException e ) {
            Main.log("Application cannot read test from URL: " + testFile );
            e.printStackTrace();
            return null;
        }
    }
    
    public static Test loadTest(String aFileName) {
        setFileName(aFileName);
        log("Test "+fileName+" opened.");
        try {
            return loadTest(new FileInputStream(aFileName));
        } catch( IOException e ) {
            Main.log("Application cannot read test from file: " + aFileName );
            e.printStackTrace();
            return null;
        }
    }
    
    private static Test loadTest(InputStream is) {
        Test test = null;
        try {
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            try {
                if (debug)
                    Main.log("Loading test:");
                DOMParser parser = new DOMParser();
                parser.parse(new InputSource(new BufferedInputStream(is)));
                Element el = (Element)parser.getDocument().getElementsByTagName("Test").item(0);
                test = new Test(el);
                if (debug)
                    Main.log("Rebuilding loggers:");
                test.rebuidlLoggers();
                if (debug)
                    Main.log("Done.");
            } catch( org.xml.sax.SAXException e ) {
                Main.log("App cannot parse file: " + e );
                e.printStackTrace();
                return null;
            }
        } catch(ParserConfigurationException e) {
            Main.log("DocumentBuilder cannot be created!");
            e.printStackTrace();
            return null;
        } catch (Exception ex) {
            Main.log("Exception during the opening of file.");
            ex.printStackTrace();
        }
        return test;
    }
    
    private static PropertyChangeListener listener = null;
    
    public static boolean openTest(String aFileName) {
        Test wasRead = null;
        URL inIDE = Main.class.getResource(aFileName);
        
        if (inIDE == null) {
            wasRead = loadTest(aFileName);
            if (wasRead == null)
                return false;
        } else {
            wasRead = loadTest(inIDE);
            if (wasRead == null)
                return false;
        }
        
        test = wasRead;
        test.addPropertyChangeListener(listener = new PropertyChangeListener() {
            public void propertyChange(final java.beans.PropertyChangeEvent p1) {
                changed=true;
            }
        });
        test.logger = new Logger(frame.getEditor());
        frame.setTest(test);
        changed=false;
        return true;
    }
    
    public static void closeTest() {
        test.removePropertyChangeListener(listener);
    }
    
    public static boolean saveTest() {
        boolean res = saveTest(test, fileName);
        
        if (res)
            changed=false;
        return res;
    }
    
    public static boolean saveTest(Test test, OutputStream outStream) {
        Main.log("Attempt to write Test to file.");
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            DOMImplementation implementation = builder.getDOMImplementation();
            
            Document document = implementation.createDocument("", "Test",null);
            Element element = (Element) document.getElementsByTagName("Test").item(0);
            
            element = test.toXML(element);
            XMLUtil.write(document,outStream, "UTF-8");
            Main.log("Saved with name: " + fileName + ".");
        } catch (javax.xml.parsers.ParserConfigurationException e) {
            Main.log("XML exception.");
            return false;
        } catch (java.io.UnsupportedEncodingException e) {
            Main.log("Unsupported encoding: UTF-8.");
            return false;
        } catch (java.io.IOException e) {
            Main.log("Unknown IO exception.");
            return false;
        };
        Main.log("End.");
        return true;
    }
    
    public static boolean saveTest(Test test, String aFileName) {
        if (aFileName == null)
            return false;
        try {
            return saveTest(test, new FileOutputStream(aFileName));
        } catch (java.io.IOException e) {
            Main.log("File " + fileName + " not found.");
        }
        return false;
    }
    
    public static boolean saveAsTest(String aFileName) {
        Main.log("saveAsTest: " + aFileName);
        if (aFileName == null)
            return false;
        setFileName(aFileName);
        return saveTest();
    }
    
    public static boolean finish() {
        JavaOptions opts = (JavaOptions)(SystemOption.findObject(JavaOptions.class));
        opts.setCompletionAutoPopup(compAutoPopup);
        opts.setJavaDocAutoPopup(docAutoPopup);
        HTMLOptions hopts = (HTMLOptions)(SystemOption.findObject(HTMLOptions.class));
        hopts.setCompletionAutoPopup(htmlAutoPopup);
        
        Scheduler.finishScheduler();
        if (log != null) {
            log.close();
            System.setErr(oldErr);
        }
        frame=null;
        test=null;
        return true;
    }
    
    public static boolean isChanged() {
        return changed;
    }
    
    public static boolean isNoname() {
        return (fileName == null || fileName.length() == 0);
    }
    
    public static void log(String text) {
        System.err.println(text);
        System.err.flush();
    }
    
    public static boolean question(String text) {
        return frame.quest(text);
    }
}
