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
import java.net.MalformedURLException;
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
        javaHome = convert(System.getProperty("java.home", ""));// NOI18N
        userHome = convert(System.getProperty("user.home", ""));// NOI18N
        netbeansUserDir = convert(System.getProperty("netbeans.user", ""));// NOI18N
        netbeansHome = convert(System.getProperty("netbeans.home", ""));// NOI18N
        String nbdirsStr = System.getProperty("netbeans.dirs");// NOI18N
        if (nbdirsStr != null){
            String [] fields = nbdirsStr.split(File.pathSeparator);
            for (int i = 0; i < fields.length; i++) {
                fields[i] = convert(fields[i]);
            }
            installDirs = Arrays.asList(fields);
        }else{
            installDirs = Collections.emptyList();
        }
    }
    
    private String convert(String str){
        try{
            return new File(str).toURI().toURL().toString();
        }catch(MalformedURLException exc){
            Logger.getLogger(LogFormatter.class.getName()).log(Level.INFO, "unaccessible file", exc);// NOI18N
        }
        return "";
    }
    
    private void a2(StringBuffer sb, int x) {
        if (x < 10) {
            sb.append('0');
        }
        sb.append(x);
    }
    
    private void escape(StringBuffer sb, String text) {
        if (text == null) {
            text = "<null>";// NOI18N
        }
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '<') {
                sb.append("&lt;");// NOI18N
            } else if (ch == '>') {
                sb.append("&gt;");// NOI18N
            } else if (ch == '&') {
                sb.append("&amp;");// NOI18N
            } else {
                sb.append(ch);
            }
        }
    }
    
    private void printFrame(StackTraceElement frame, StringBuffer sb){
        sb.append("    <frame>\n");// NOI18N
        sb.append("      <class>");// NOI18N
        escape(sb, frame.getClassName());
        sb.append("</class>\n");// NOI18N
        sb.append("      <method>");// NOI18N
        escape(sb, frame.getMethodName());
        sb.append("</method>\n");// NOI18N
        // Check for a line number.
        if (frame.getLineNumber() >= 0) {
            sb.append("      <line>");// NOI18N
            sb.append(frame.getLineNumber());
            sb.append("</line>\n");// NOI18N
        }
        sb.append("      <file>");// NOI18N
        ClassLoader loader = Lookup.getDefault().lookup(ClassLoader.class);
        Class clazz=null;
        URL jarName=null;
        String fileName=null;
        try{
            clazz = loader.loadClass(frame.getClassName());
        }catch(Throwable exc){
            Logger.getLogger(LogFormatter.class.getName()).log(Level.FINE, "Class loading error", exc);// NOI18N
        }
        if (clazz != null){
            String[] fields = clazz.getName().split("\\.");// NOI18N
            if (fields.length> 0){
                jarName = clazz.getResource(fields[fields.length-1]+".class");// NOI18N
            }
            if (jarName!= null){
                fileName = jarName.toString();
                int index = fileName.indexOf("!");// NOI18N
                if (index!= -1){
                    fileName = fileName.substring(0, index);
                }
                fileName = fileName.replace("jar:", "");// NOI18N
                if (javaHome.length() > 0){
                    fileName = fileName.replace(javaHome, "${java.home}");// NOI18N
                }
                if (netbeansHome.length() > 0){
                    fileName = fileName.replace(netbeansHome, "${netbeans.home}");// NOI18N
                }
                if (netbeansUserDir.length() > 0){
                    fileName = fileName.replace(netbeansUserDir, "${user.dir}");// NOI18N
                }
                for (Iterator<String> it = installDirs.iterator(); it.hasNext();) {
                    String nextDir = it.next();
                    fileName = fileName.replace(nextDir, "${netBeansDir}");// NOI18N
                }
                if (userHome.length() > 0){
                    fileName = fileName.replace(userHome, "${user.home}");// NOI18N
                }
                escape(sb, fileName);
            }
        }
        sb.append("</file>\n");// NOI18N
        sb.append("    </frame>\n");// NOI18N
    }
    
    
    private void printCause(Throwable th, StringBuffer sb, StackTraceElement[] causedTrace){
        sb.append("  <exception>\n");// NOI18N
        sb.append("   <message>");// NOI18N
        escape(sb, th.toString());
        sb.append("</message>\n");// NOI18N
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
        sb.append("   <more>");// NOI18N
        sb.append(framesInCommon);
        sb.append("</more>\n");// NOI18N
        sb.append("  </exception>\n");// NOI18N
        if (th.getCause() != null){
            printCause(th.getCause(), sb, trace);
        }
    }
    
    // Report on the state of the throwable.
    private void printThrown(Throwable th, StringBuffer sb){
        sb.append("  <exception>\n");// NOI18N
        sb.append("    <message>");// NOI18N
        escape(sb, th.toString());
        sb.append("</message>\n");// NOI18N
        StackTraceElement trace[] = th.getStackTrace();
        for (int i = 0; i < trace.length; i++) {
            printFrame(trace[i], sb);
        }
        sb.append("  </exception>\n");// NOI18N
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
        sb.append("<record>\n");// NOI18N
        
        sb.append("  <date>");// NOI18N
        appendISO8601(sb, record.getMillis());
        sb.append("</date>\n");// NOI18N
        
        sb.append("  <millis>");// NOI18N
        sb.append(record.getMillis());
        sb.append("</millis>\n");// NOI18N
        
        sb.append("  <sequence>");// NOI18N
        sb.append(record.getSequenceNumber());
        sb.append("</sequence>\n");// NOI18N
        
        String name = record.getLoggerName();
        if (name != null) {
            sb.append("  <logger>");// NOI18N
            escape(sb, name);
            sb.append("</logger>\n");// NOI18N
        }
        
        sb.append("  <level>");// NOI18N
        escape(sb, record.getLevel().toString());
        sb.append("</level>\n");// NOI18N
        
        if (record.getSourceClassName() != null) {
            sb.append("  <class>");// NOI18N
            escape(sb, record.getSourceClassName());
            sb.append("</class>\n");// NOI18N
        }
        
        if (record.getSourceMethodName() != null) {
            sb.append("  <method>");// NOI18N
            escape(sb, record.getSourceMethodName());
            sb.append("</method>\n");// NOI18N
        }
        
        sb.append("  <thread>");// NOI18N
        sb.append(record.getThreadID());
        sb.append("</thread>\n");// NOI18N
        
        // Format the message string and its accompanying parameters.
        String message = formatMessage(record);
        if (record.getMessage() != null) {
            sb.append("  <message>");// NOI18N
            escape(sb, message);
            sb.append("</message>\n");// NOI18N
        }
        
        // If the message is being localized, output the key, resource
        // bundle name, and params.
        ResourceBundle bundle = record.getResourceBundle();
        try {
            if (bundle != null && bundle.getString(record.getMessage()) != null) {
                sb.append("  <key>");// NOI18N
                escape(sb, record.getMessage());
                sb.append("</key>\n");// NOI18N
                sb.append("  <catalog>");// NOI18N
                escape(sb, record.getResourceBundleName());
                sb.append("</catalog>\n");// NOI18N
            }
        } catch (Exception exc) {
            // The message is not in the catalog.  Drop through.
            Logger.getLogger(LogFormatter.class.getName()).log(Level.FINE, "Catalog loading error", exc);// NOI18N
        }
        
        Object parameters[] = record.getParameters();
        //  Check to see if the parameter was not a messagetext format
        //  or was not null or empty
        if ( parameters != null && parameters.length != 0
                && record.getMessage().indexOf("{") == -1 ) {
            for (int i = 0; i < parameters.length; i++) {
                sb.append("  <param>");// NOI18N
                try {
                    escape(sb, paramToString(parameters[i]));
                } catch (Exception ex) {
                    sb.append("???");// NOI18N
                }
                sb.append("</param>\n");// NOI18N
            }
        }
        
        if (record.getThrown() != null) {
            printThrown(record.getThrown(), sb);
        }
        
        sb.append("</record>\n");// NOI18N
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
