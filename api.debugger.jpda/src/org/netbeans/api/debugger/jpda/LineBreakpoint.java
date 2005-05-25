/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.debugger.jpda;


/**
 * Notifies about line breakpoint events.
 *
 * <br><br>
 * <b>How to use it:</b>
 * <pre style="background-color: rgb(255, 255, 153);">
 *    DebuggerManager.addBreakpoint (LineBreakpoint.create (
 *        "examples.texteditor.Ted",
 *        27
 *    ));</pre>
 * This breakpoint stops in Ted class on 27 line number.
 *
 * @author Jan Jancura
 */
public final class LineBreakpoint extends JPDABreakpoint {

    /** Property name constant */
    public static final String          PROP_LINE_NUMBER = new String ("lineNumber"); // NOI18N
    /** Property name constant */
    public static final String          PROP_URL = new String ("url"); // NOI18N
    /** Property name constant. */
    public static final String          PROP_CONDITION = new String ("condition"); // NOI18N
    /** Property name constant. */
    public static final String          PROP_SOURCE_NAME = new String ("sourceName"); // NOI18N
    /** Property name constant. */
    public static final String          PROP_SOURCE_PATH = new String ("sourcePath"); // NOI18N
    /** Property name constant. */
    public static final String          PROP_STRATUM = new String ("stratum"); // NOI18N
    
    private String                      url = "";
    private int                         lineNumber;
    private String                      condition = ""; // NOI18N
    private String                      sourceName = null; // NOI18N
    private String                      sourcePath = null; // NOI18N
    private String                      stratum = "Java"; // NOI18N

    
    private LineBreakpoint () {
    }
    
    /**
     * Creates a new breakpoint for given parameters.
     *
     * @param url a url
     * @param lineNumber a line number
     * @return a new breakpoint for given parameters
     */
    public static LineBreakpoint create (
        String url,
        int lineNumber
    ) {
        LineBreakpoint b = new LineBreakpoint ();
        b.setURL (url);
        b.setLineNumber (lineNumber);
        return b;
    }

    /**
     * Gets name of class to stop on.
     *
     * @return name of class to stop on
     */
    public String getURL () {
        return url;
    }
    
    /**
     * Sets name of class to stop on.
     *
     * @param cn a new name of class to stop on
     */
    public void setURL (String url) {
        String old;
        synchronized (this) {
            if (url == null) url = "";
            if ( (url == this.url) ||
                 ((url != null) && (this.url != null) && url.equals (this.url))
            ) return;
            old = url;
            this.url = url;
        }
        firePropertyChange (PROP_URL, old, url);
    }
    
    /**
     * Gets number of line to stop on.
     *
     * @return line number to stop on
     */
    public int getLineNumber () {
        return lineNumber;
    }
    
    /**
     * Sets number of line to stop on.
     *
     * @param ln a line number to stop on
     */
    public void setLineNumber (int ln) {
        int old;
        synchronized (this) {
            if (ln == lineNumber) return;
            old = lineNumber;
            lineNumber = ln;
        }
        firePropertyChange (
            PROP_LINE_NUMBER,
            new Integer (old),
            new Integer (ln)
        );
    }
    
    /**
     * Returns condition.
     *
     * @return cond a condition
     */
    public String getCondition () {
        return condition;
    }
    
    /**
     * Sets condition.
     *
     * @param c a new condition
     */
    public void setCondition (String c) {
        String old;
        synchronized (this) {
            if (c == null) c = "";
            c = c.trim ();
            if ( (c == condition) ||
                 ((c != null) && (condition != null) && condition.equals (c))
            ) return;
            old = condition;
            condition = c;
        }
        firePropertyChange (PROP_CONDITION, old, c);
    }
    
    /**
     * Returns stratum.
     *
     * @return a stratum
     */
    public String getStratum () {
        return stratum;
    }
    
    /**
     * Sets stratum.
     *
     * @param s a new stratum
     */
    public void setStratum (String s) {
        String old;
        synchronized (this) {
            if (s == null) s = "";
            s = s.trim ();
            if ( (s == stratum) ||
                 ((s != null) && (stratum != null) && stratum.equals (s))
            ) return;
            old = stratum;
            stratum = s;
        }
        firePropertyChange (PROP_CONDITION, old, s);
    }
    
    /**
     * Returns source name.
     *
     * @return a source name
     */
    public String getSourceName () {
        return sourceName;
    }
    
    /**
     * Sets source name.
     *
     * @param c a new source name
     */
    public void setSourceName (String sn) {
        String old;
        synchronized (this) {
            if (sn == null) sn = "";
            sn = sn.trim ();
            if ( (sn == sourceName) ||
                 ((sn != null) && (sourceName != null) && sourceName.equals (sn))
            ) return;
            old = sourceName;
            sourceName = sn;
        }
        firePropertyChange (PROP_SOURCE_NAME, old, sn);
    }

    /**
     * Returns source path.
     *
     * @return a source path
     *
     * @since 1.3
     */
    public String getSourcePath() {
        return sourcePath;
    }

    /**
     * Sets source path.
     *
     * @param sp a new source path
     *
     * @since 1.3
     */
    public void setSourcePath (String sp) {
        String old;
        synchronized (this) {
            if (sp != null) sp = sp.trim();
            if (sp == sourcePath || (sp != null && sp.equals(sourcePath))) {
                return ;
            }
            old = sourcePath;
            sourcePath = sp;
        }
        firePropertyChange (PROP_SOURCE_PATH, old, sp);
    }
    
    /**
     * Returns a string representation of this object.
     *
     * @return  a string representation of the object
     */
    public String toString () {
        return "LineBreakpoint " + url + " : " + lineNumber;
    }
}