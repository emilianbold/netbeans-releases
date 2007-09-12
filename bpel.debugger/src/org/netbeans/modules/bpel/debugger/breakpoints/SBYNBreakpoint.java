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
