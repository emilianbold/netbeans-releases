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
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package org.netbeans.lib.uihandler;

import java.awt.Component;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.XMLFormatter;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JMenuItem;
import org.openide.util.Lookup;
import static java.util.Calendar.*;

/**this class uses most of XML Formater code, but it adds cause of throwable
 *
 * @author Jindrich Sedek
 */
class LogFormatter extends XMLFormatter{
    private String javaHome;
    private String userHome;
    private String netbeansUserDir;
    private String netbeansHome;
    private List<String> installDirs;
    /** Creates a new instance of LogFormatter */
    public LogFormatter() {
        javaHome = System.getProperty("java.home", "");
        userHome = System.getProperty("user.home", "");
        netbeansUserDir = System.getProperty("netbeans.user", "");
        netbeansHome = System.getProperty("netbeans.home", "");
        String nbdirsStr = System.getProperty("netbeans.dirs");
        if (nbdirsStr != null){
            String [] fields = nbdirsStr.split(File.pathSeparator);
            installDirs = Arrays.asList(fields);
        }else{
            installDirs = Collections.emptyList();
        }
    }
    
    private void a2(StringBuffer sb, int x) {
        if (x < 10) {
            sb.append('0');
        }
        sb.append(x);
    }
    
    private void escape(StringBuffer sb, String text) {
        if (text == null) {
            text = "<null>";
        }
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '<') {
                sb.append("&lt;");
            } else if (ch == '>') {
                sb.append("&gt;");
            } else if (ch == '&') {
                sb.append("&amp;");
            } else {
                sb.append(ch);
            }
        }
    }
    
    private void printFrame(StackTraceElement frame, StringBuffer sb){
        sb.append("    <frame>\n");
        sb.append("      <class>");
        escape(sb, frame.getClassName());
        sb.append("</class>\n");
        sb.append("      <method>");
        escape(sb, frame.getMethodName());
        sb.append("</method>\n");
        // Check for a line number.
        if (frame.getLineNumber() >= 0) {
            sb.append("      <line>");
            sb.append(frame.getLineNumber());
            sb.append("</line>\n");
        }
        sb.append("      <file>");
        ClassLoader loader = Lookup.getDefault().lookup(ClassLoader.class);
        Class clazz=null;
        URL jarName=null;
        String fileName=null;
        try{
            clazz = loader.loadClass(frame.getClassName());
        }catch(Exception exc){
            Logger.getLogger(LogFormatter.class.getName()).log(Level.FINE, "Class loading error", exc);
        }
        if (clazz != null){
            String[] fields = clazz.getName().split("\\.");
            if (fields.length> 0){
                jarName = clazz.getResource(fields[fields.length-1]+".class");
            }
            if (jarName!= null){
                fileName = jarName.toString();
                int index = fileName.indexOf("!");
                if (index!= -1){
                    fileName = fileName.substring(0, index);
                }
                fileName = fileName.replace("jar:file:", "");
                if (javaHome.length() > 0){
                    fileName = fileName.replace(javaHome, "${java.home}");
                }
                if (netbeansHome.length() > 0){
                    fileName = fileName.replace(netbeansHome, "${netbeans.home}");
                }
                if (netbeansUserDir.length() > 0){
                    fileName = fileName.replace(netbeansUserDir, "${user.dir}");
                }
                for (Iterator<String> it = installDirs.iterator(); it.hasNext();) {
                    String nextDir = it.next();
                    fileName = fileName.replace(nextDir, "${netBeansDir}");
                }
                if (userHome.length() > 0){
                    fileName = fileName.replace(userHome, "${user.home}");
                }
                escape(sb, fileName);
            }
        }
        sb.append("</file>\n");
        sb.append("    </frame>\n");
    }
    
    
    private void printCause(Throwable th, StringBuffer sb, StackTraceElement[] causedTrace){
        sb.append("  <exception>\n");
        sb.append("   <message>");
        escape(sb, th.toString());
        sb.append("</message>\n");
        StackTraceElement[] trace = th.getStackTrace();
        int m = trace.length-1;
        int n = causedTrace.length-1;
        while (m >= 0 && n >=0 && trace[m].equals(causedTrace[n])) {
            m--; n--;
        }
        int framesInCommon = trace.length - 1 - m;
        
        for (int i=0; i <= m; i++) {
            printFrame(trace[i], sb);
        }
        sb.append("   <more>");
        sb.append(framesInCommon);
        sb.append("</more>\n");
        sb.append("  </exception>\n");
        if (th.getCause() != null){
            printCause(th.getCause(), sb, trace);
        }
    }
    
    // Report on the state of the throwable.
    private void printThrown(Throwable th, StringBuffer sb){
        sb.append("  <exception>\n");
        sb.append("    <message>");
        escape(sb, th.toString());
        sb.append("</message>\n");
        StackTraceElement trace[] = th.getStackTrace();
        for (int i = 0; i < trace.length; i++) {
            printFrame(trace[i], sb);
        }
        sb.append("  </exception>\n");
        if (th.getCause() != null){
            printCause(th.getCause(), sb, trace);
        }
    }
    
    // Append the time and date in ISO 8601 format
    private void appendISO8601(StringBuffer sb, long millis) {
        Date date = new Date(millis);
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        sb.append(calendar.get(YEAR));
        sb.append('-');
        a2(sb, calendar.get(MONTH) + 1);
        sb.append('-');
        a2(sb, calendar.get(DAY_OF_MONTH));
        sb.append('T');
        a2(sb, calendar.get(HOUR_OF_DAY));
        sb.append(':');
        a2(sb, calendar.get(MINUTE));
        sb.append(':');
        a2(sb, calendar.get(SECOND));
    }
    
    /**
     * Format the given message to XML.
     * @param record the log record to be formatted.
     * @return a formatted log record
     */
    public String format(LogRecord record) {
        StringBuffer sb = new StringBuffer(1000);
        sb.append("<record>\n");
        
        sb.append("  <date>");
        appendISO8601(sb, record.getMillis());
        sb.append("</date>\n");
        
        sb.append("  <millis>");
        sb.append(record.getMillis());
        sb.append("</millis>\n");
        
        sb.append("  <sequence>");
        sb.append(record.getSequenceNumber());
        sb.append("</sequence>\n");
        
        String name = record.getLoggerName();
        if (name != null) {
            sb.append("  <logger>");
            escape(sb, name);
            sb.append("</logger>\n");
        }
        
        sb.append("  <level>");
        escape(sb, record.getLevel().toString());
        sb.append("</level>\n");
        
        if (record.getSourceClassName() != null) {
            sb.append("  <class>");
            escape(sb, record.getSourceClassName());
            sb.append("</class>\n");
        }

        if (record.getSourceMethodName() != null) {
            sb.append("  <method>");
            escape(sb, record.getSourceMethodName());
            sb.append("</method>\n");
        }
        
        sb.append("  <thread>");
        sb.append(record.getThreadID());
        sb.append("</thread>\n");
        
        // Format the message string and its accompanying parameters.
        String message = formatMessage(record);
        if (record.getMessage() != null) {
            sb.append("  <message>");
            escape(sb, message);
            sb.append("</message>\n");
        }
        
        // If the message is being localized, output the key, resource
        // bundle name, and params.
        ResourceBundle bundle = record.getResourceBundle();
        try {
            if (bundle != null && bundle.getString(record.getMessage()) != null) {
                sb.append("  <key>");
                escape(sb, record.getMessage());
                sb.append("</key>\n");
                sb.append("  <catalog>");
                escape(sb, record.getResourceBundleName());
                sb.append("</catalog>\n");
            }
        } catch (Exception exc) {
            // The message is not in the catalog.  Drop through.
            Logger.getLogger(LogFormatter.class.getName()).log(Level.FINE, "Catalog loading error", exc);
        }
        
        Object parameters[] = record.getParameters();
        //  Check to see if the parameter was not a messagetext format
        //  or was not null or empty
        if ( parameters != null && parameters.length != 0
                && record.getMessage().indexOf("{") == -1 ) {
            for (int i = 0; i < parameters.length; i++) {
                sb.append("  <param>");
                try {
                    escape(sb, paramToString(parameters[i]));
                } catch (Exception ex) {
                    sb.append("???");
                }
                sb.append("</param>\n");
            }
        }
        
        if (record.getThrown() != null) {
            printThrown(record.getThrown(), sb);
        }
        
        sb.append("</record>\n");
        return sb.toString();
    }
    
    private static String paramToString(Object obj) {
        if (obj == null) {
            return "null"; // NOI18N
        }
        
        if (obj instanceof JMenuItem) {
            JMenuItem ab = (JMenuItem)obj;
            Action a = ab.getAction();
            if (a == null) {
                // fall thru to AbstractButton
            } else {
                return ab.getClass().getName() + '[' + paramToString(a) + ']';
            }
        }
        if (obj instanceof AbstractButton) {
            AbstractButton ab = (AbstractButton)obj;
            return ab.getClass().getName() + '[' + ab.getText() + ']';
        }
        if (obj instanceof Action) {
            Action a = (Action)obj;
            if (
                a.getClass().getName().endsWith("$DelegateAction") && // NOI18N
                a.getClass().getName().startsWith("org.openide") // NOI18N
            ) {
                return a.toString().replaceAll("@[0-9a-fA-F]*", "," + a.getValue(Action.NAME)); // NOI18N
            }
            return a.getClass().getName() + '[' + a.getValue(Action.NAME) + ']';
        }
        if (obj instanceof Component) {
            Component c = (Component)obj;
            return c.getClass().getName() + '[' + c.getName() + ']'; // NOI18N
        }
        
        return obj.toString();
    }
}
