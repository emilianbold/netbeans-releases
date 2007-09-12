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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
