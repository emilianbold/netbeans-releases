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
import java.util.*;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Ant;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;

import org.w3c.dom.*;
import org.apache.xml.serialize.*;

/**
 * Task to process Arch questions & answers document.
 *
 */
public class Arch extends Task implements org.xml.sax.EntityResolver {

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
    
    
    /** Run the conversion */
    public void execute () throws BuildException {        
        if ( questionsFile == null ) {
            throw new BuildException ("questions file must be provided");
        }
        
        if ( output == null ) {
            throw new BuildException ("output file must be specified");
        }
        
        boolean generateTemplate = !questionsFile.exists();
        
        if (!generateTemplate && output.exists() && questionsFile.lastModified() <= output.lastModified()) {
            // nothing needs to be generated. everything is up to date
            return;
        }
        
        
        org.w3c.dom.Document q;
        try {
            javax.xml.parsers.DocumentBuilder builder = javax.xml.parsers.DocumentBuilderFactory.newInstance ().newDocumentBuilder();
            builder.setEntityResolver(this);

            if (generateTemplate) {
                q = builder.parse(getClass().getResourceAsStream("Arch-api-questions.xml"));
            } else {
                q = builder.parse (questionsFile);
            }
        } catch (Exception ex) {
            throw new BuildException ("File " + questionsFile + " cannot be parsed: " + ex.getLocalizedMessage(), ex);
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
                generateTemplateFile (removeRevisionTags (questionsVersion), s);
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
            
            if (!removeRevisionTags (answersVersion).equals (removeRevisionTags (questionsVersion))) {
                throw new BuildException ("Answers were created for questions version \"" + answersVersion + "\" but current version of questions is \"" + questionsVersion + "\"");
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
                        "Some questions have not been answered" + s + "\n" + 
                        "Run with -Darch.generate=true to add missing questions into the end of question file"
                    );
                }
            }
        }
        
        // apply the transform operation
        try {
            javax.xml.transform.Transformer t = javax.xml.transform.TransformerFactory.newInstance().newTransformer(
                new javax.xml.transform.stream.StreamSource (
                    getClass ().getResourceAsStream ("Arch.xsl")
                )
            );
            javax.xml.transform.Source s = new javax.xml.transform.dom.DOMSource (q);
            javax.xml.transform.Result r = new javax.xml.transform.stream.StreamResult (output);

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
        while (it.hasNext()) {
            String s = (String)it.next ();
            Element n = (Element)questions.get (s);
            
            //w.write("\n\n<!-- Question: " + s + "\n");
            w.write("\n\n<!--\n        ");
            //w.write("\n     " + n); #30529 - does not work with all DOM parsers
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
            ser.serialize(n);
            /*
            DocumentFragment frag = n.getOwnerDocument().createDocumentFragment();
            NodeList l = n.getChildNodes();
            for (int i = 0; i < l.getLength(); i++) {
                frag.appendChild(l.item(i));
            }
            ser.serialize(frag);
             */
            w.write(wr.toString());
            w.write("\n-->\n");
            w.write("<answer id=\"" + s + "\">\nNo answer\n</answer>\n\n");
        }
    }
    
    private void generateTemplateFile (String versionOfQuestions, Set missing) throws IOException {
        Writer w = new FileWriter (questionsFile);
        
        w.write ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        w.write ("<!DOCTYPE api-answers [\n");
        w.write ("  <!ENTITY api-questions SYSTEM \"api-questions.xml\">\n");
        w.write ("]>\n");
        w.write ("\n");
        w.write ("<api-answers\n");
        w.write ("  version=\"$Revision$\" date=\"$date$\"\n");
        w.write ("  question-version=\""); w.write (versionOfQuestions); w.write ("\"\n");
        w.write ("  module=\"name of your module\"\n");
        w.write ("  author=\"yourname@netbeans.org\"\n");
        w.write (">\n\n");
        w.write ("  &api-questions;\n");        
        
        writeQuestions (w, missing);
        
        w.write ("</api-answers>\n");
        
        w.close ();
    }

    private static String removeRevisionTags (String s) {
        if (s.startsWith ("$Revision: ")) {
            s = s.substring ("$Revision: ".length());
        }
        if (s.endsWith (" $")) {
            s = s.substring (0, s.length() - 2);
        }
        return s;
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

    /** Entity resolver to map publicId to valid data */
    public org.xml.sax.InputSource resolveEntity(String publicId, String systemId) 
    throws org.xml.sax.SAXException, IOException {
        if (systemId != null && systemId.endsWith ("api-questions.xml")) {
            return new org.xml.sax.InputSource (getClass ().getResourceAsStream("Arch-api-questions.xml"));
        }
        System.out.println("resolve: " + publicId);
        System.out.println("      s: " + systemId);
        return null;
    }
    
}
