package nbbuild.misc.bugcompare;

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
import java.util.*;
import java.net.*;
import java.io.*;
import org.xml.sax.*;

/**
 *
 * @author  ph97928
 * @version
 */
public class Compare_1 {
    Map release32;
    Map dev;

    public Map fillTable(String name) {
        Map map = new TreeMap();
        try {
            ChangelogRecognizer.parse(new InputSource(name), new ChangelogHandlerImpl(map), new ChangelogParslet());
        }
        catch (Exception exc) {
            exc.printStackTrace();
        }
        return map;
    }

    public List compare(Map source, Map dest) {
        List result = new ArrayList();
        Iterator it = source.keySet().iterator();
        while (it.hasNext()) {
            Integer key = (Integer) it.next();
            if (!dest.containsKey(key)) {
                result.add(key);
            }
        }
        return result;
    }

    public void start() {
        System.out.println("Filling release32");
        release32 = fillTable("d:\\petr\\bugs\\release32.xml");
        System.out.println("Filling dev");
        dev = fillTable("d:\\petr\\bugs\\dev.xml");
        System.out.println("Comparing");
        List notInDev = compare(release32, dev);
        output("d:\\petr\\bugs\\bugs.html", notInDev);
    }
    
    public void output(String name, List list) {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(name));
            writer.println("<html>");
            writer.println("<body>");
            writer.println("Found "+list.size()+" differences");

            writer.println("<table border=\"1\" width=\"100%\">");

            Iterator it = list.iterator();
            while (it.hasNext()) {
                Integer number = (Integer) it.next();
                System.out.println("bug:"+number);
                writer.println("<tr>");
                writer.println("<td width=\"10%\"><a href=\"http://www.netbeans.org/issues/show_bug.cgi?id="+number+"\">"+number+"</a></td>");
                writer.println("<td width=\"10%\">"+moduleName(number.intValue())+"</td>");
                writer.println("<td width=\"80%\">"+release32.get(number)+"</td>");
                writer.println("</tr>");
            }
            writer.println("</table>");
            writer.println("</body>");
            writer.println("</html>");
            writer.close();
        }
        catch (Exception exc) {
           exc.printStackTrace(); 
        }
    }

    String moduleName(int bugnumber) throws Exception {
        URLConnection con = new URL("http://www.netbeans.org/issues/show_bug.cgi?id="+bugnumber).openConnection();
        BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
        int i = 0;
        for (;;) {
            String line = reader.readLine();
            if (line == null)
                break;
            if (i == 0) {
                if (line.indexOf("<TD ALIGN=RIGHT><B>Product:</B></TD>") > 0) {
                    i = 1;
                }
            }
            else if (i == 1) {
                int index = line.indexOf("<OPTION SELECTED VALUE=\"");
                if (index > 0) {
                    String module = line.substring(index + 24);
                    module = module.substring(0, module.indexOf('\"'));
                    reader.close();
                    return module;
                }
            }
        }
        reader.close();
        return "";
    }
    
    /**
    * @param args the command line arguments
    */
    public static void main (String args[]) throws Exception {
        new Compare().start();
    }
}
