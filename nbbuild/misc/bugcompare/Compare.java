/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
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
public class Compare {
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
