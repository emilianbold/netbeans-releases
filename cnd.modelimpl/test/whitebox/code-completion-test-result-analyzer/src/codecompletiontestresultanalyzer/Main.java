/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
import org.xml.sax.XMLReader;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author nk220367
 */
public class Main extends DefaultHandler {

    Map<String, Integer> errorStatistic = new HashMap<String, Integer>();
    static String projDir;
    static String indexDir;

    public static void main(String args[])
            throws Exception {

        projDir = args[0];
//        System.out.println("Project dir: " + projDir);
        indexDir = args[1];
//        System.out.println("Index dir: " + indexDir);

        XMLReader xr = XMLReaderFactory.createXMLReader();
        Main handler = new Main();
        xr.setContentHandler(handler);
        xr.setErrorHandler(handler);

        for (int i = 2; i < args.length; i++) {
            FileReader r = new FileReader(args[i]);
            xr.parse(new InputSource(r));
        }

        Iterator<String> it = handler.errorStatistic.keySet().iterator();
        while (it.hasNext()) 
        {
            String key = it.next();
            System.out.println(key + " " + handler.errorStatistic.get(key));
        }
    }

    public Main() {
        super();
    }

    String currentFileName;
    boolean currentCheckPointIsFailed = false;

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
                
//                if(desc.equals("unknown")) {
//                    System.out.println(currentFileName + " " + lineNumber + " " + columnNumber);                    
//                }

                if (errorStatistic.containsKey(desc)) {
                    errorStatistic.put(desc, errorStatistic.get(desc).intValue() + 1);
                } else {
                    errorStatistic.put(desc, 1);
                }
            }
        }
    }

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
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

        return "unknown";
    }
}