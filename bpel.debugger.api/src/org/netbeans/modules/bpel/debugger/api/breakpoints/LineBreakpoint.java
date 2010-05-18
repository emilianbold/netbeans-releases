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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.bpel.debugger.api.breakpoints;

/**
 *
 * @author Alexander Zgursky
 */
public final class LineBreakpoint extends BpelBreakpoint {

    /** Property name constant. */
    public static final String PROP_LINE_NUMBER = "lineNumber"; // NOI18N
    /** Property name constant. */
    public static final String PROP_URL = "url"; // NOI18N
    /** Property name constant. */
    public static final String PROP_XPATH = "xpath"; // NOI18N

    private String myUrl;
    private String myXpath;
    private int myLineNumber;

    private LineBreakpoint() {
        // does nothing
    }

    public static LineBreakpoint create(
            final String url,
            final String xpath,
            final int lineNumber) {
        
        final LineBreakpoint breakpoint = new LineBreakpoint();
        
        breakpoint.setURL(url);
        breakpoint.setXpath(xpath);
        breakpoint.setLineNumber(lineNumber);
        
        return breakpoint;
    }

    /**
     * Gets full path of a source file to stop on.
     *
     * @return full path of a source file to stop on
     */
    public String getURL() {
        return myUrl;
    }
    
    /**
     * Sets full path of a source file to stop on.
     *
     * @param newUrl full path of a source file to stop on
     */
    public void setURL(
            final String newUrl) {
        
        final String oldUrl = myUrl;
        myUrl = newUrl;
        firePropertyChange(PROP_URL, oldUrl, newUrl);
    }
    
    /**
     * 
     * @param newXpath
     */
    public void setXpath(
            final String newXpath) {
        
        final String oldXpath = myXpath;
        myXpath = newXpath;
        firePropertyChange(PROP_XPATH, oldXpath, newXpath);
    }
    
    /**
     * 
     * @return
     */
    public String getXpath() {
        return myXpath;
    }
    
    /**
     * 
     * @param lineNumber
     */
    public void setLineNumber(
            final int lineNumber) {
        
        final int oldLineNumber = myLineNumber;
        myLineNumber = lineNumber;
        firePropertyChange(PROP_LINE_NUMBER, oldLineNumber, myLineNumber);
    }
    
    /**
     * Gets line number to stop on.
     *
     * @return line number to stop on
     */
    public int getLineNumber() {
        return myLineNumber;
    }
    
    //TODO:remove this method sometimes: it's a hack to notify the views
    //that the information, associated with the breakpoint has changed
    /**
     * Notify all the property change listeners
     * about "change"
     */
    public void touch() {
        firePropertyChange(null, null, null);
    }
    
    @Override
    public String toString() {
        return "LineBreakpoint " + myUrl + ":" + myLineNumber + // NOI18N
                (myXpath != null ? " (" + myXpath + ")" : ""); // NOI18N
    }
}
