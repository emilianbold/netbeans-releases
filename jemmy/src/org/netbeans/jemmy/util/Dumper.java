/*
 * Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with
 * the License. A copy of the License is available at http://www.sun.com/
 * 
 * The Original Code is the Jemmy library.
 * The Initial Developer of the Original Code is Alexandre Iline.
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
import org.netbeans.jemmy.operators.*;

/**
 * 
 * @author Alexandre Iline (alexandre.iline@sun.com)
 * 
 */

public class Dumper {

    /**
     * Prints XML DTD information.
     * @param writer
     */
    public static void printDTD(PrintWriter writer) {
	printDTD(writer, "");
    }

    /**
     * Prints XML DTD information.
     * @param writer
     */
    public static void printDTD(PrintStream writer) {
	printDTD(new PrintWriter(writer));
    }

    /**
     * Prints XML DTD information into file.
     * @param fileName
     */
    public static void printDTD(String fileName)
	throws FileNotFoundException {
	printDTD(new PrintWriter(new FileOutputStream(fileName)));
    }

    /**
     * Prints component hierarchy (GUI dump)
     * starting from <code>comp</code> component.
     * @param writer
     */
    public static void dumpComponent(Component comp, PrintWriter writer) {
	QueueTool qt = new QueueTool();
	Component[] comps;
	if(comp != null) {
	    comps = new Component[1];
	    comps[0] = comp;
	} else {
	    comps = Frame.getFrames();
	}
	try {
	    qt.lock();
	    printHeader(writer);
	    dumpSome("dump", comps, writer, "");
	    writer.flush();
	} finally {
	    qt.unlock();
	}
    }

    /**
     * Prints component hierarchy (GUI dump).
     * starting from <code>comp</code> component.
     * @param writer
     */
    public static void dumpComponent(Component comp, PrintStream writer) {
	dumpComponent(comp, new PrintWriter(writer));
    }

    /**
     * Prints component hierarchy (GUI dump) into file.
     * @param fileName
     */
    public static void dumpComponent(Component comp, String fileName) 
	throws FileNotFoundException {
	dumpComponent(comp, new PrintWriter(new FileOutputStream(fileName)));
    }

    /**
     * Prints all component hierarchy (GUI dump).
     * @param writer
     */
    public static void dumpAll(PrintWriter writer) {
	dumpComponent(null, writer);
    }

    /**
     * Prints all component hierarchy (GUI dump).
     * @param writer
     */
    public static void dumpAll(PrintStream writer) {
	dumpAll(new PrintWriter(writer));
    }

    /**
     * Prints component hierarchy (GUI dump) into file.
     * @param fileName
     */
    public static void dumpAll(String fileName)
	throws FileNotFoundException {
	dumpAll(new PrintWriter(new FileOutputStream(fileName)));
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
    private static void dumpSome(String tag, Component[] comps, PrintWriter writer, String tab) {
	if(comps.length > 0) {
	    printTagStart(writer, tag, tab);
	    for(int i = 0; i < comps.length; i++) {
		dumpOne(comps[i], writer, tab + tabIncrease);
	    }
	    printTagEnd(writer, tag, tab);
	}
    }
    private static void dumpOne(Component component, PrintWriter writer, String tab) {
	try {
	    Operator oper = Operator.createOperator(component);
	    Hashtable componentDump = oper.getDump();
	    printTagOpening(writer, "component", tab);
	    writer.print(" operator=\"" + 
			 oper.getClass().getName() + "\"");
	    printTagClosing(writer, "component");
	    Object[] keys = componentDump.keySet().toArray();
	    Arrays.sort(keys);
	    for(int i = 0; i < keys.length; i++) {
		printEmptyTagOpening(writer, "property", tab + tabIncrease);
		writer.print(" name=\"" + 
			     (String)keys[i] + "\" value=\"" +
			     ((String)componentDump.get(keys[i])).replace('"', '\'') + "\"");
		printEmptyTagClosing(writer, "property");
	    }
	} catch(Exception e) {
	    JemmyProperties.getCurrentOutput().printStackTrace(e);
	    printTagStart(writer, "component", tab);
	    printEmptyTagOpening(writer, "exception", tab + tabIncrease);
	    writer.print(" toString=\"" +
			 e.toString().replace('"', '\'') + "\"");
	    printEmptyTagClosing(writer, "exception");
	}
	if(component instanceof Window) {
	    dumpSome("subwindows", ((Window)component).getOwnedWindows(), writer, tab + tabIncrease);
	}
	if(component instanceof Container) {
	    dumpSome("subcomponents", ((Container)component).getComponents(), writer, tab + tabIncrease);
	}
	printTagEnd(writer, "component", tab);
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
}
