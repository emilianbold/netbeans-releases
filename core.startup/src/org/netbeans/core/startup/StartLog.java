/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.startup;

import java.util.Stack;
import java.util.Arrays;

/** Logger that will enable the logging of important events during the startup
 * annotated with real time and possibly time differences.
 *
 * @author Petr Nejedly, Jesse Glick
 */
public class StartLog {

    private static final StartImpl impl;
    private static final Stack actions = new Stack(); // Stack<String>
    private static final Stack places = new Stack(); // Stack<Throwable>
    private static final boolean DEBUG_NESTING = Boolean.getBoolean("org.netbeans.log.startup.debug"); // NOI18N
    
    private static final String logProp; 
    private static final String logFileProp;
    
    static {
        logProp = System.getProperty( "org.netbeans.log.startup" ); // NOI18N
        //if you want to create file with measured values, we do expect the property org.netbeans.log.startup=print
        logFileProp = System.getProperty( "org.netbeans.log.startup.logfile" ); // NOI18N
        
        if(logProp == null)
            impl = new StartImpl();
        else if("print".equals(logProp))
            impl = new PrintImpl();
        else if("tests".equals(logProp))
            impl = new PerformanceTestsImpl();
        else
            throw new Error("Unknown org.netbeans.log.startup value [" + logProp + "], it should be (print or tests) !"); // NOI18N
    }
    
    /** Start running some interval action.
     * @param action some identifying string
     */
    public static void logStart( String action ) {
        if (willLog()) {
            impl.start(action, System.currentTimeMillis());
            actions.push(action);
            if (DEBUG_NESTING) {
                places.push(new Throwable("logStart called here:")); // NOI18N
            }
        }
    }

    /** Note that something happened, but not an interval.
     * The log will note only the time elapsed since the last interesting event.
     * @param action some identifying string
     */
    public static void logProgress( String note ) {
        if( willLog() ) impl.progress( note, System.currentTimeMillis() );
    }

    /** Stop running some interval action.
     * The log will note the time elapsed since the start of the action.
     * Actions <em>must</em> be properly nested.
     * @param action some identifying string
     */
    public static void logEnd( String action ) {
        if (willLog()) {
            String old = actions.empty() ? null : (String)actions.pop();
            Throwable oldplace = DEBUG_NESTING && !places.empty() ? (Throwable)places.pop() : null;
            if (!action.equals(old)) {
                // Error, not ISE; don't want this caught and reported
                // with ErrorManager, for then you get a wierd cycle!
                if (oldplace != null) {
                    oldplace.printStackTrace();
                } else {
                    System.err.println("Either ending too soon, or no info about caller of unmatched start log."); // NOI18N
                    System.err.println("Try running with -J-Dorg.netbeans.log.startup.debug=true"); // NOI18N
                }
                Error e = new Error("StartLog mismatch: ending '" + action + "' but expecting '" + old + "'; rest of stack: " + actions); // NOI18N
                // Print stack trace since you can get strange situations
                // when ErrorManager tries to report it - may need to initialize
                // ErrorManager, etc.
                e.printStackTrace();
                // Presumably you did want to keep on going at this point.
                // Note that this can only happen when logging is off
                // (which is the default).
                System.exit(1);
            }
            impl.end(action, System.currentTimeMillis());
        }
    }

    public static boolean willLog() {
        return impl.willLog();
    }
    
    /** Logs the startup time. The begining is tracked by this class. 
     *  The end is passed as argument.
     */
    public static void logMeasuredStartupTime(long end){
        ((PerformanceTestsImpl)impl).log("IDE starts t=" + Long.toString(((PerformanceTestsImpl)impl).zero) + "\nIDE is running t=" + Long.toString(end) + "\n");
        ((PerformanceTestsImpl)impl).writeLogs();
    }
    
    
    
    /** The dummy, no-op implementation */
    private static class StartImpl {
        StartImpl() {}
        void start( String action, long time ) {}
        void progress( String note, long time ) {}
        void end( String action, long time ) {}
        boolean willLog() {
            return false;
        }
    }

    private static class PrintImpl extends StartImpl {
        PrintImpl() {}
        long zero = System.currentTimeMillis();
        private Stack starts = new Stack();
        long prog;
        private int indent = 0;
        
        synchronized void start( String action, long time ) {
            starts.push( new Long( time ) );
            prog=time;
            System.err.println( getIndentString(indent) + "@" + 
                    (time - zero) + " - " + action + " started" // NOI18N
            );
            indent += 2;
        }
        
        synchronized void progress( String note, long time ) {
            System.err.println( getIndentString(indent) + "@" + 
                    (time - zero) + " - " + note + " dT=" + (time - prog) // NOI18N
            );
            prog = time;
        }
        
        synchronized void end( String action, long time ) {
            indent -= 2;
            long start = ((Long)starts.pop()).longValue();
            prog = time;
            System.err.println( getIndentString(indent) + "@" + 
                    (time - zero) + " - " + action + " finished, took " + // NOI18N
                    (time - start) + "ms" // NOI18N
            );
        }
        
        boolean willLog() {
            return true;
        }
        
        private char[] spaces = new char[0];
        private String getIndentString( int indent ) {
            if( spaces.length < indent ) {
                spaces = new char[Math.max( spaces.length*2, indent+10 )];
                Arrays.fill( spaces, ' ');
            }
            return new String(spaces,0, indent);
        }
    }

    private static class PerformanceTestsImpl extends StartImpl {
        private StringBuffer logs = new StringBuffer();
        long zero = System.currentTimeMillis();
        private Stack starts = new Stack();
        long prog;
        private int indent = 0;
        
        PerformanceTestsImpl() {}
        
        synchronized void start( String action, long time ) {
            starts.push( new Long( time ) );
            prog=time;
            log(getIndentString(indent) + "@" + 
                    (time - zero) + " - " + action + " started" // NOI18N
            );
            indent += 2;
        }
        
        synchronized void progress( String note, long time ) {
            log(getIndentString(indent) + "@" + 
                    (time - zero) + " - " + note + " dT=" + (time - prog) // NOI18N
            );
            prog = time;
        }
        
        synchronized void end( String action, long time ) {
            indent -= 2;
            long start = ((Long)starts.pop()).longValue();
            prog = time;
            log(getIndentString(indent) + "@" + 
                    (time - zero) + " - " + action + " finished, took " + // NOI18N
                    (time - start) + "ms" // NOI18N
            );
        }
        
        boolean willLog() {
            return true;
        }
        
        private char[] spaces = new char[0];
        private String getIndentString( int indent ) {
            if( spaces.length < indent ) {
                spaces = new char[Math.max( spaces.length*2, indent+10 )];
                Arrays.fill( spaces, ' ');
            }
            return new String(spaces,0, indent);
        }
        
        synchronized void log(String log){
            logs.append("\n" + log);
        }
        
        synchronized void writeLogs(){
            if(logFileProp!=null){
                try{
                    java.io.File logFile = new java.io.File(logFileProp);
                    java.io.FileWriter writer = new java.io.FileWriter(logFile);
                    writer.write(logs.toString());
                    writer.close();
                }catch (Exception exc){
                    System.err.println("EXCEPTION rises during startup logging:");
                    exc.printStackTrace(System.err);
                }
            } else
                throw new IllegalStateException("You are trying to log startup logs to unexisting file. You have to set property org.netbeans.log.startup.logfile.");
        }

    }
}
