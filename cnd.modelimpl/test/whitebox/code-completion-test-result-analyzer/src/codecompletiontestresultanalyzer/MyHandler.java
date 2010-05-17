/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package codecompletiontestresultanalyzer;

import java.io.BufferedReader;
import java.io.FileReader;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Code completion test result analyzer
 * 
 * @author Nick Krasilnikov
 */
public class MyHandler extends DefaultHandler {

    private static String projDir;
    private static String indexDir;

    
    private Map<String, Integer> errorStatistic = new HashMap<String, Integer>();

    private String currentFileName;
    private boolean currentCheckPointIsFailed = false;
    private String currentTokenName = "";
    private int currentTokenLine;
    private boolean currentTokenNotFound = false;
    
    private boolean textTag = false;

    /**
     * Main function
     * Collects definitions, declarations and usages and then dumps them as golden data
     * 
     * @param args the command line arguments. There should be project dir, index dir and xml files
     */    
    public static void main(String args[])
            throws Exception {

        projDir = args[0];
//        System.out.println("Project dir: " + projDir);
        indexDir = args[1];
//        System.out.println("Index dir: " + indexDir);

        XMLReader xr = XMLReaderFactory.createXMLReader();
        MyHandler handler = new MyHandler();
        xr.setContentHandler(handler);
        xr.setErrorHandler(handler);

        for (int i = 2; i < args.length; i++) {
            FileReader r = new FileReader(args[i]);
            xr.parse(new InputSource(r));
        }

        handler.printStatisticInHtml();
    }

    /**
     * Prints result
     */
    void printStatisticInHtml() {
        System.out.println("<html>");
        System.out.println("<head><title>Detailed statistics</title></head>");
        System.out.println("<body>");

        Iterator<String> it = errorStatistic.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            System.out.println(key + " " + errorStatistic.get(key) + "<br>");
        }

        System.out.println("</body>");
        System.out.println("</html>");
    }

    /**
     * Constructor
     */
    public MyHandler() {
        super();
    }
    
    @Override
    public void startElement(String uri, String name,
            String qName, Attributes atts) {

        if (name.equals("file")) {
            for (int i = 0; i < atts.getLength(); i++) {
                if (atts.getLocalName(i).equals("fullPath")) {
                    currentFileName = atts.getValue(i);
                }
            }
        }

        if (currentFileName != null && name.equals("checkpoint")) {
            currentTokenNotFound = false;
            currentTokenName = "";
            for (int i = 0; i < atts.getLength(); i++) {
                if (atts.getLocalName(i).equals("resultFull")) {
                    currentCheckPointIsFailed = atts.getValue(i).equals("false");
                }
            }
        }

        if (currentCheckPointIsFailed && name.equals("token")) {
            int lineNumber = -1;
            int columnNumber = -1;
            for (int i = 0; i < atts.getLength(); i++) {
                if (atts.getLocalName(i).equals("lineNumber")) {
                    lineNumber = Integer.parseInt(atts.getValue(i).replaceAll("\"", ""));
                }
                if (atts.getLocalName(i).equals("columnNumber")) {
                    columnNumber = Integer.parseInt(atts.getValue(i).replaceAll("\"", ""));
                }
            }
            if (lineNumber > 0 && columnNumber > 0) {
                String desc = getTokenDescriptionFromIndex(currentFileName, lineNumber, columnNumber);

                if (desc == null) {
                    currentTokenLine = lineNumber;
                    currentTokenNotFound = true;
                    return;
                }

                if (errorStatistic.containsKey(desc)) {
                    errorStatistic.put(desc, errorStatistic.get(desc).intValue() + 1);
                } else {
                    errorStatistic.put(desc, 1);
                }
            }
        }
        if (name.equals("text")) {
            textTag = true;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (localName.equals("text")) {
            textTag = false;
        }

        if (currentTokenNotFound && localName.equals("checkpoint")) {
            String desc = getTokenDescriptionFromIndex(currentFileName, currentTokenLine, currentTokenName);

            if (desc == null) {
                desc = "unknown";
            }
            
            if (errorStatistic.containsKey(desc)) {
                errorStatistic.put(desc, errorStatistic.get(desc).intValue() + 1);
            } else {
                errorStatistic.put(desc, 1);
            }

            currentTokenName = "";
            currentTokenNotFound = false;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (currentTokenNotFound && textTag) {
            for (int i = 0; i < length; i++) {
                char c = ch[start + i];
                currentTokenName += c;
            }
        }
    }

    /**
     * Gets token description from index
     * 
     * @param file - file name
     * @param line - line number
     * @param col - column number
     * @return description of token
     */
    public String getTokenDescriptionFromIndex(String file, int line, int col) {
        String relFileName = file.substring(projDir.length() + 1);
        try {
            BufferedReader in = new BufferedReader(new FileReader(indexDir + "/" + relFileName.replaceAll("/", ".")));

            String s = in.readLine();
            while (s != null) {
                if (s.startsWith(line + ":" + col + " ")) {
                    return s.replaceAll(".* ", "");
                }
                s = in.readLine();
            }
        } catch (IOException ex) {
            Logger.getLogger(MyHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    /**
     * Gets token description from index
     * 
     * @param file - file name
     * @param line - line number
     * @param name - token name
     * @return description of token
     */
    public String getTokenDescriptionFromIndex(String file, int line, String name) {
        String relFileName = file.substring(projDir.length() + 1);
        try {
            BufferedReader in = new BufferedReader(new FileReader(indexDir + "/" + relFileName.replaceAll("/", ".")));

            String s = in.readLine();
            while (s != null) {
                if (s.matches(line + ":.* " + name + " .*")) {
                    return s.replaceAll(".* ", "") + "-in-line-with-macro";
                }
                s = in.readLine();
            }
        } catch (IOException ex) {
            Logger.getLogger(MyHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }
}
