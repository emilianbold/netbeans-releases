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
 * The Original Software is the Jemmy library.
 * The Initial Developer of the Original Software is Alexandre Iline.
 * All Rights Reserved.
 *
 * Contributor(s): Alexandre Iline.
 *
 * $Id$ $Revision$ $Date$
 *
 */

package org.netbeans.jemmy.util;

import java.awt.*;
import java.io.*;
import java.util.*;
import org.netbeans.jemmy.*;
import org.netbeans.jemmy.QueueTool.QueueAction;
import org.netbeans.jemmy.operators.*;

/**
 * Allows to "dump" current GUI state into XML file.
 * Uses operators' getDump methods to gather the information.
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 *
 */

public class Dumper {

    /**
     * Prints XML DTD information.
     * @param writer a writer to write to.
     */
    public static void printDTD(PrintWriter writer) {
        printDTD(writer, "");
    }
    
    /**
     * Prints XML DTD information.
     * @param writer a stream to write to.
     */
    public static void printDTD(PrintStream writer) {
        printDTD(new PrintWriter(writer));
    }
    
    /**
     * Prints XML DTD information into file.
     * @param fileName a file to write to.
     * @throws FileNotFoundException
     */
    public static void printDTD(String fileName)
    throws FileNotFoundException {
        printDTD(new PrintWriter(new FileOutputStream(fileName)));
    }
    
    /**
     * Prints component hierarchy (GUI dump)
     * starting from <code>comp</code> component.
     * @param comp a component to get information from.
     * @param writer a writer to write to.
     */
    public static void dumpComponent(Component comp, final PrintWriter writer, final DumpController listener) {
        QueueTool qt = new QueueTool();
        Component[] comps;
        if(comp != null) {
            comps = new Component[1];
            comps[0] = comp;
        } else {
            comps = Frame.getFrames();
        }
        final Component[] comps_final = comps;
        qt.invokeAndWait(new QueueAction("dumpComponent") {
            public Object launch() throws Exception {
                printHeader(writer);
                dumpSome("dump", comps_final, writer, "", listener);
                writer.flush();
                return null;
            }
        });
    }
    
    public static void dumpComponent(Component comp, PrintWriter writer) {
        dumpComponent(comp, writer, new DumpController() {
            public boolean onComponentDump(Component comp) {
                return(true);
            }
            public boolean onPropertyDump(Component comp, String name, String value) {
                return(true);
            }
        });
    }
    
    /**
     * Prints component hierarchy (GUI dump).
     * starting from <code>comp</code> component.
     * @param comp a component to get information from.
     * @param writer a stream to write to.
     */
    public static void dumpComponent(Component comp, PrintStream writer) {
        dumpComponent(comp, new PrintWriter(writer));
    }
    
    public static void dumpComponent(Component comp, PrintStream writer, DumpController listener) {
        dumpComponent(comp, new PrintWriter(writer), listener);
    }
    
    /**
     * Prints component hierarchy (GUI dump) into file.
     * @param comp a component to get information from.
     * @param fileName a file to write to.
     * @throws FileNotFoundException
     */
    public static void dumpComponent(Component comp, String fileName)
    throws FileNotFoundException {
        dumpComponent(comp, new PrintWriter(new FileOutputStream(fileName)));
    }
    
    public static void dumpComponent(Component comp, String fileName, DumpController listener)
    throws FileNotFoundException {
        dumpComponent(comp, new PrintWriter(new FileOutputStream(fileName)), listener);
    }
    
    /**
     * Prints all component hierarchy (GUI dump).
     * @param writer a writer to write to.
     */
    public static void dumpAll(PrintWriter writer) {
        dumpComponent(null, writer);
    }
    
    public static void dumpAll(PrintWriter writer, DumpController listener) {
        dumpComponent(null, writer, listener);
    }
    
    /**
     * Prints all component hierarchy (GUI dump).
     * @param writer a stream to write to.
     */
    public static void dumpAll(PrintStream writer) {
        dumpAll(new PrintWriter(writer));
    }
    
    public static void dumpAll(PrintStream writer, DumpController listener) {
        dumpAll(new PrintWriter(writer), listener);
    }
    
    /**
     * Prints component hierarchy (GUI dump) into file.
     * @param fileName a file to write to.
     * @throws FileNotFoundException
     */
    public static void dumpAll(String fileName)
    throws FileNotFoundException {
        dumpAll(new PrintWriter(new FileOutputStream(fileName)));
    }
    
    public static void dumpAll(String fileName, DumpController listener)
    throws FileNotFoundException {
        dumpAll(new PrintWriter(new FileOutputStream(fileName)), listener);
    }
    
    private static final String tabIncrease = "  ";
    private static void printTagStart(PrintWriter writer, String tag, String tab) {
        writer.println(tab + "<" + tag + ">");
    }
    private static void printTagOpening(PrintWriter writer, String tag, String tab) {
        writer.print(tab + "<" + tag);
    }
    private static void printTagClosing(PrintWriter writer, String tag) {
        writer.println(">");
    }
    private static void printTagEnd(PrintWriter writer, String tag, String tab) {
        writer.println(tab + "</" + tag + ">");
    }
    private static void printEmptyTagOpening(PrintWriter writer, String tag, String tab) {
        writer.print(tab + "<" + tag);
    }
    private static void printEmptyTagClosing(PrintWriter writer, String tag) {
        writer.println("/>");
    }
    private static void dumpSome(String tag, Component[] comps, PrintWriter writer, String tab, DumpController listener) {
        if(comps.length > 0) {
            printTagStart(writer, tag, tab);
            for(int i = 0; i < comps.length; i++) {
                dumpOne(comps[i], writer, tab + tabIncrease, listener);
            }
            printTagEnd(writer, tag, tab);
        }
    }
    private static void dumpOne(Component component, PrintWriter writer, String tab, DumpController listener) {
        //whether to dump at all
        boolean toDump = listener.onComponentDump(component);
        if(toDump) {
            try {
                Operator oper = Operator.createOperator(component);
                Hashtable componentDump = oper.getDump();
                printTagOpening(writer, "component", tab);
                writer.print(" operator=\"" +
                        oper.getClass().getName() + "\"");
                printTagClosing(writer, "component");
                Object[] keys = componentDump.keySet().toArray();
                Arrays.sort(keys);
                String name, value;
                for(int i = 0; i < keys.length; i++) {
                    name = (String)keys[i];
                    value = ((String)componentDump.get(keys[i]));
                    if(listener.onPropertyDump(component, name, value)) {
                        printEmptyTagOpening(writer, "property", tab + tabIncrease);
                        writer.print(" name=\"" +
                                escape(name) + "\" value=\"" +
                                escape(value) + "\"");
                        printEmptyTagClosing(writer, "property");
                    }
                }
            } catch(Exception e) {
                JemmyProperties.getCurrentOutput().printStackTrace(e);
                printTagStart(writer, "component", tab);
                printEmptyTagOpening(writer, "exception", tab + tabIncrease);
                writer.print(" toString=\"" +
                        escape(e.toString()) + "\"");
                printEmptyTagClosing(writer, "exception");
            }
        }
        if(component instanceof Window) {
            dumpSome("subwindows", ((Window)component).getOwnedWindows(), writer, tab + tabIncrease, listener);
        }
        if(component instanceof Container) {
            dumpSome("subcomponents", ((Container)component).getComponents(), writer, tab + tabIncrease, listener);
        }
        if(toDump) {
            printTagEnd(writer, "component", tab);
        }
    }
    private static void printHeader(PrintWriter writer) {
        writer.println("<?xml version=\"1.0\"?>");
        writer.println("<!DOCTYPE dump [");
        printDTD(writer, tabIncrease);
        writer.println("]>");
    }
    private static void printDTD(PrintWriter writer, String tab) {
        writer.println(tab + "<!ELEMENT dump (component*)>");
        writer.println(tab + "<!ELEMENT component (property+, subcomponents?, subwindows?, exception?)>");
        writer.println(tab + "<!ELEMENT subcomponents (component+)>");
        writer.println(tab + "<!ELEMENT subwindows (component+)>");
        writer.println(tab + "<!ELEMENT property EMPTY>");
        writer.println(tab + "<!ELEMENT exception EMPTY>");
        writer.println(tab + "<!ATTLIST component");
        writer.println(tab + "          operator CDATA #IMPLIED>");
        writer.println(tab + "<!ATTLIST exception");
        writer.println(tab + "          toString CDATA #REQUIRED>");
        writer.println(tab + "<!ATTLIST property");
        writer.println(tab + "          name  CDATA #REQUIRED");
        writer.println(tab + "          value CDATA #REQUIRED>");
    }
    
    public static String escape(String str) {
        return str.replaceAll("&", "&amp;").replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;").replaceAll("\"", "&quot;");
    }
}
