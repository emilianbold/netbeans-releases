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
import java.lang.reflect.Constructor;
import java.text.DateFormat;
import java.util.*;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Ant;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;

import org.w3c.dom.*;
import org.xml.sax.*;
import org.apache.xml.serialize.*;

/**
 * Task to process Arch questions & answers document.
 *
 */
public class Arch extends Task implements ErrorHandler {

    /** map from String ids -> Elements */
    private Map answers;
    private Map questions;
    
    public Arch() {
    }

    //
    // itself
    //

    private File questionsFile;
    /** Questions & answers file */
    public void setAnswers (File f) {
        questionsFile = f;
    }
    
    
    private File output;
    /** Output file
     */
    public void setOutput (File f) {
        output = f;
    }
    
    // For use when generating API documentation:
    private String stylesheet = null;
    public void setStylesheet(String s) {
        stylesheet = s;
    }
    private String overviewlink = null;
    public void setOverviewlink(String s) {
        overviewlink = s;
    }
    private String footer = null;
    public void setFooter(String s) {
        footer = s;
    }
    private File xsl = null;
    public void setXSL (File xsl) {
        this.xsl = xsl;
    }
    
    
    /** Run the conversion */
    public void execute () throws BuildException {        
        if ( questionsFile == null ) {
            throw new BuildException ("questions file must be provided");
        }
        
        if ( output == null ) {
            throw new BuildException ("output file must be specified");
        }
        
        boolean generateTemplate = !questionsFile.exists();
        
        if (
            !generateTemplate && 
            output.exists() && 
            questionsFile.lastModified() <= output.lastModified() &&
            this.getProject().getProperty ("arch.generate") == null
        ) {
            // nothing needs to be generated. everything is up to date
            return;
        }
        
        
        org.w3c.dom.Document q;
        try {
            javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance ();
            factory.setValidating(true);
            
            javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setErrorHandler(this);

            if (generateTemplate) {
                q = builder.parse(getClass().getResourceAsStream("Arch-api-questions.xml"));
            } else {
                q = builder.parse (questionsFile);
            }
        } catch (SAXParseException ex) {
            log(ex.getSystemId() + ":" + ex.getLineNumber() + ": " + ex.getLocalizedMessage(), Project.MSG_ERR);
            throw new BuildException(questionsFile.getAbsolutePath() + " is malformed or invalid", ex, location);
        } catch (Exception ex) {
            throw new BuildException ("File " + questionsFile + " cannot be parsed: " + ex.getLocalizedMessage(), ex, location);
        }

        questions = readElements (q, "question");
        
        String questionsVersion;
        {
            NodeList apiQuestions = q.getElementsByTagName("api-questions");
            if (apiQuestions.getLength () != 1) {
                throw new BuildException ("No element api-questions");
            }
            questionsVersion = ((Element)apiQuestions.item (0)).getAttribute ("version");
            if (questionsVersion == null) {
                throw new BuildException ("Element api-questions does not have attribute version");
            }
        }
        
        if (questions.size () == 0) {
            throw new BuildException ("There are no <question> elements in the file!");
        }
        
        if (generateTemplate) {
            log ("Input file " + questionsFile + " does not exists. Generating it filled with skeleton answers.");
            try {
                TreeSet s = new TreeSet (questions.keySet ());
                generateTemplateFile(questionsVersion, s);
            } catch (IOException ex) {
                throw new BuildException (ex);
            }
            
            return;
        }
        
        answers = readElements (q, "answer");
        
        
        {
            //System.out.println("doc:\n" + q.getDocumentElement());
            
            // version of answers and version of questions
            NodeList apiAnswers = q.getElementsByTagName("api-answers");
            
            if (apiAnswers.getLength() != 1) {
                throw new BuildException ("No element api-answers");
            }
            
            String answersVersion = ((Element)apiAnswers.item (0)).getAttribute ("question-version");
            
            if (answersVersion == null) {
                throw new BuildException ("Element api-answers does not have attribute question-version");
            }
            
            if (!answersVersion.equals(questionsVersion)) {
                String msg = questionsFile.getAbsolutePath() + ": answers were created for questions version \"" + answersVersion + "\" but current version of questions is \"" + questionsVersion + "\"";
                if ("true".equals (this.getProject().getProperty("arch.warn"))) {
                    log (msg, Project.MSG_WARN);
                } else {
                    throw new BuildException (msg);
                }
            }
        }
        
        {
            // check all answers have their questions
            TreeSet s = new TreeSet (questions.keySet ());
            s.removeAll (answers.keySet ());
            if (!s.isEmpty()) {
                if ("true".equals (this.getProject().getProperty ("arch.generate"))) {
                    log ("Missing answers to questions: " + s);
                    log ("Generating the answers to end of file " + questionsFile);
                    try {
                        generateMissingQuestions (s);
                    } catch (IOException ex) {
                        throw new BuildException (ex);
                    }
                } else {
                    log (
                        questionsFile.getAbsolutePath() + ": some questions have not been answered: " + s + "\n" + 
                        "Run with -Darch.generate=true to add missing questions into the end of question file"
                    , Project.MSG_WARN);
                }
            }
        }
        
        // apply the transform operation
        try {
            javax.xml.transform.stream.StreamSource ss;
            String file = this.xsl != null ? this.xsl.toString() : getProject().getProperty ("arch.xsl");
            
            if (file != null) {
                log ("Using " + file + " as the XSL stylesheet");
                try {
                    ss = new javax.xml.transform.stream.StreamSource (
                        new java.io.FileInputStream (file)
                    );
                } catch (java.io.IOException ex) {
                    throw new BuildException (ex);
                }
            } else {
                ss = new javax.xml.transform.stream.StreamSource (
                    getClass ().getResourceAsStream ("Arch.xsl")
                );
            }
            
            log("Transforming " + questionsFile + " into " + output);
            
            javax.xml.transform.Transformer t = javax.xml.transform.TransformerFactory.newInstance().newTransformer(ss);
            javax.xml.transform.Source s = new javax.xml.transform.dom.DOMSource (q);
            javax.xml.transform.Result r = new javax.xml.transform.stream.StreamResult (output);
            if (stylesheet != null) {
                t.setParameter("arch.stylesheet", stylesheet);
            }
            if (overviewlink != null) {
                t.setParameter("arch.overviewlink", overviewlink);
            }
            if (footer != null) {
                t.setParameter("arch.footer", footer);
            }
            t.setParameter("arch.answers.date", DateFormat.getDateInstance().format(new Date(questionsFile.lastModified())));
            
            String archTarget = output.toString();
            int slash = archTarget.lastIndexOf(File.separatorChar);
            if (slash > 0) {
                archTarget = archTarget.substring (slash + 1);
            }
            String archPref = getProject ().getProperty ("arch.target");
            if (archPref != null) {
                archTarget = archPref + File.separatorChar + archTarget;
            }
            
            t.setParameter("arch.target", archTarget);

            t.transform(s, r);
        } catch (javax.xml.transform.TransformerConfigurationException ex) {
            throw new BuildException (ex);
        } catch (javax.xml.transform.TransformerException ex) {
            throw new BuildException (ex);
        }
    }
    
    private void generateMissingQuestions (Set missing) throws IOException {
        Writer w = new OutputStreamWriter (new FileOutputStream (questionsFile.toString (), true));
        
        w.write("<!-- Copy this above the </api-answers> tag! -->\n\n");
        
        writeQuestions (w, missing);
        
        w.close();
    }

    private void writeQuestions (Writer w, Set missing) throws IOException {
        java.util.Iterator it = missing.iterator();

        ElementToString convertor;
        try {
            Class c = Class.forName (getClass ().getName () + "$XercesE2S");
            Constructor cc = c.getDeclaredConstructor(new Class[] {});
            cc.setAccessible(true);
            convertor = (ElementToString)cc.newInstance(null);
            convertor.convertElement(null);
        } catch (Throwable ex) {
            log ("Cannot initialize xerces to print out DOM elements: " + ex + ". Trying org.w3c.dom.Node.toString() which might work as well");
            convertor = new ToStringE2S ();
        }
        
        boolean useXerces = true;
        while (it.hasNext()) {
            String s = (String)it.next ();
            Element n = (Element)questions.get (s);
            
            //w.write("\n\n<!-- Question: " + s + "\n");
            w.write("\n\n<!--\n        ");
            
            w.write (convertor.convertElement (n));
            
            w.write("\n-->\n");
            w.write("<answer id=\"" + s + "\">\nNo answer\n</answer>\n\n");
        }
    }
        
    
    private static String findNbRoot (File f) {
        StringBuffer result = new StringBuffer ();
        f = f.getParentFile();
        
        while (f != null) {
            File x = new File (f, 
                "nbbuild" + File.separatorChar + 
                "antsrc" + File.separatorChar + 
                "org" + File.separatorChar + 
                "netbeans" + File.separatorChar +
                "nbbuild" + File.separatorChar +
                "Arch.dtd"
            );
            if (x.exists ()) {
                return result.toString();
            }
            result.append("../"); // URI, so pathsep is /
            f = f.getParentFile();
        }
        return "${nbroot}/";
    }
    
    private void generateTemplateFile (String versionOfQuestions, Set missing) throws IOException {
        String nbRoot = findNbRoot (questionsFile).replace (File.separatorChar, '/');
        
        Writer w = new FileWriter (questionsFile);
        
        w.write ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        w.write ("<!DOCTYPE api-answers PUBLIC \"-//NetBeans//DTD Arch Answers//EN\" \""); w.write (nbRoot); w.write ("nbbuild/antsrc/org/netbeans/nbbuild/Arch.dtd\" [\n");
        w.write ("  <!ENTITY api-questions SYSTEM \""); w.write (nbRoot); w.write ("nbbuild/antsrc/org/netbeans/nbbuild/Arch-api-questions.xml\">\n");
        w.write ("]>\n");
        w.write ("\n");
        w.write ("<api-answers\n");
        w.write ("  question-version=\""); w.write (versionOfQuestions); w.write ("\"\n");
        w.write ("  module=\"name of your module\"\n");
        w.write ("  author=\"yourname@netbeans.org\"\n");
        w.write (">\n\n");
        w.write ("  &api-questions;\n");        
        
        writeQuestions (w, missing);
        
        w.write ("</api-answers>\n");
        
        w.close ();
    }

    private static HashMap readElements (Document q, String name) {
        HashMap map = new HashMap ();
       
        NodeList list = q.getElementsByTagName(name);
        for (int i = 0; i < list.getLength(); i++) {
            Node n = list.item (i).getAttributes().getNamedItem("id");
            if (n == null) {
                throw new BuildException ("Question without id tag");
            }
            String id = n.getNodeValue();

            map.put (id, list.item (i));
        }
        
        return map;
    }

    public void error(SAXParseException exception) throws SAXException {
        log(exception.getSystemId() + ":" + exception.getLineNumber() + ": " + exception.getLocalizedMessage(), Project.MSG_ERR);
    }
    
    public void fatalError(SAXParseException exception) throws SAXException {
        throw exception;
    }
    
    public void warning(SAXParseException exception) throws SAXException {
        if (exception.getLocalizedMessage().startsWith("Using original entity definition for")) {
            // Pointless message, always logged when using XHTML. Ignore.
            return;
        }
        log(exception.getSystemId() + ":" + exception.getLineNumber() + ": " + exception.getLocalizedMessage(), Project.MSG_WARN);
    }
    
    private static interface ElementToString {
        public String convertElement (Element e) throws BuildException;
    }
    
    private static final class XercesE2S implements ElementToString {
        
        public String convertElement(Element n) throws BuildException {
            XMLSerializer ser = new XMLSerializer();
            StringWriter wr = new StringWriter();
            ser.setOutputCharStream(wr);
            OutputFormat fmt = new OutputFormat();
            fmt.setIndenting(false);
            fmt.setOmitXMLDeclaration(true);
            fmt.setOmitDocumentType(true);
            fmt.setPreserveSpace(true);
            fmt.setOmitComments(true);
            ser.setOutputFormat(fmt);
            try {
                if (n != null) {
                    ser.serialize(n);
                }
            } catch (IOException ex) {
                throw new BuildException (ex);
            }
            /*
            DocumentFragment frag = n.getOwnerDocument().createDocumentFragment();
            NodeList l = n.getChildNodes();
            for (int i = 0; i < l.getLength(); i++) {
                frag.appendChild(l.item(i));
            }
            ser.serialize(frag);
             */

            return wr.toString ();
        }
        
    }
    
    private static final class ToStringE2S implements ElementToString {
        public String convertElement(Element e) throws BuildException {
            String str = e.toString ();
            if (str == null || str.length() == 0) {
                // will not work anyway
                throw new BuildException ("DOM model does not support Element.toString conversion");
            }
            return str;
        }
        
    }
}
