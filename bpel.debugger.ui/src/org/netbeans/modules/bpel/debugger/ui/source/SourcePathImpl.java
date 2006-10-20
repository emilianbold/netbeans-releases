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

package org.netbeans.modules.bpel.debugger.ui.source;

import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;
import org.netbeans.modules.bpel.debugger.api.BpelDebugger;
import org.netbeans.modules.bpel.debugger.api.SourcePath;
import org.netbeans.spi.debugger.ContextProvider;

/**
 *
 * @author Alexander Zgursky
 */
public class SourcePathImpl implements SourcePath {
    
    private ContextProvider          myLookupProvider;
    private BpelDebugger             myDebugger;
    
    private Map<QName, String>       mySourceMap =
            new HashMap<QName, String>();

    /** Creates new instance of SourcePath.
     *
     * @param lookupProvider debugger context
     */
    public SourcePathImpl(ContextProvider lookupProvider) {
        myLookupProvider = lookupProvider;
        myDebugger = (BpelDebugger) lookupProvider.lookupFirst
                (null, BpelDebugger.class);
    }
    
    public synchronized String getURL(QName processQName) {
        return mySourceMap.get(processQName);
    }

    public synchronized void setURL(QName processQName, String url) {
        mySourceMap.put(processQName, url);
    }
}
