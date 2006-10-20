/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.bpel.debugger.api.breakpoints;

/**
 *
 * @author Alexander Zgursky
 */
public final class LineBreakpoint extends BpelBreakpoint implements Comparable {

    /** Property name constant. */
    public static final String PROP_LINE_NUMBER = "lineNumber"; // NOI18N
    /** Property name constant. */
    public static final String PROP_URL = "url"; // NOI18N
    /** Property name constant. */
    public static final String PROP_XPATH = "xpath"; // NOI18N

    private String myUrl;
    private String myXpath;

    private LineBreakpoint() {
    }

    public static LineBreakpoint create(
            String url,
            String xpath)
    {
        LineBreakpoint b = new LineBreakpoint();
        b.setURL(url);
        b.setXpath(xpath);
        return b;
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
    public void setURL(String newUrl) {
        String oldUrl = myUrl;
        myUrl = newUrl;
        firePropertyChange(PROP_URL, oldUrl, newUrl);
    }

    /**
     * Gets line number to stop on.
     *
     * @return line number to stop on
     */
    public int getLineNumber() {
//        return myLineNumber;
        return -1;
    }

    public void setXpath(String newXpath) {
        String oldXpath = myXpath;
        myXpath = newXpath;
        firePropertyChange(PROP_XPATH, oldXpath, newXpath);
    }
    
    public String getXpath() {
        return myXpath;
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

    public String toString() {
        return "LineBreakpoint " + myUrl + " : " + myXpath; // NOI18N
    }

    public int compareTo(Object o) {
        if (o instanceof LineBreakpoint) {
            LineBreakpoint lbthis = this;
            LineBreakpoint lb = (LineBreakpoint) o;
            int uc = getURL().compareTo(lb.getURL());
            if (uc != 0) {
                return uc;
            } else {
                return getXpath().compareTo(lb.getXpath());
            }
        } else {
            //TODO: is this comply with Comparable interface contract?
            //shouldn't we guarantee that a>b => b<a ?
            return -1;
        }
    }
}
