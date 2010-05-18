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


package org.netbeans.modules.bpel.debugger.breakpoints;

import javax.xml.namespace.QName;
import org.netbeans.modules.bpel.debugger.*;
import org.netbeans.modules.bpel.debugger.api.EditorContextBridge;
import org.netbeans.modules.bpel.debugger.api.breakpoints.LineBreakpoint;


/**
 *
 * @author Joshua Sanduski
 * @author Alexander Zgursky
 */
public abstract class SBYNBreakpoint {

    private LineBreakpoint myBreakpoint;
    private BpelDebuggerImpl myDebugger;
    private String myUrl;
    private String myXpath;
    private QName myProcessQName;

    protected SBYNBreakpoint(LineBreakpoint bpelBreakpoint, BpelDebuggerImpl debugger)
    {
        myBreakpoint = bpelBreakpoint;
        myDebugger = debugger;
        myUrl = bpelBreakpoint.getURL();
        myXpath = bpelBreakpoint.getXpath();
        
        //TODO:ugly hack
        //in this implementation myProcessQName can be null.
        //It's better not to create SBYNBreakpoint at all in such a case.
        //So, for now, just ensure getProcessQName doesn't return null
        myProcessQName = EditorContextBridge.getProcessQName(myUrl);
        if (myProcessQName == null) {
            myProcessQName = new QName("");
        }
        //end of ugly hack
        
    }

    protected BpelDebuggerImpl getDebugger() {
        return myDebugger;
    }
    
    public String getURL() {
        return myUrl;
    }
    
    public QName getProcessQName() {
        return myProcessQName;
    }
    
    public String getXpath() {
        return myXpath;
    }

    public boolean isEnabled () {
        return myBreakpoint.isEnabled();
    }
    
    public boolean isAt(QName processQName, String xpath) {
        if (    getProcessQName().equals(processQName) &&
                getXpath().equals(xpath))
        {
            return true;
        } else {
            return false;
        }
    }
}
