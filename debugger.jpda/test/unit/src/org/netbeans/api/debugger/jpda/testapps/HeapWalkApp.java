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

package org.netbeans.api.debugger.jpda.testapps;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Martin Entlicher
 */
public class HeapWalkApp {
    
    private List multiInstanceList = new ArrayList();
    
    /** Creates a new instance of HeapWalkApp */
    public HeapWalkApp() {
    }
    
    public static void main(String[] args) {
        HeapWalkApp app = new HeapWalkApp();
        app.createMultiInstances(10);
        "Stop here to explore multi instances".toString();
    }
    
    private void createMultiInstances(int num) {
        while (num-- > 0) {
            multiInstanceList.add(new MultiInstanceClass());
        }
    }
    
    public static class MultiInstanceClass {
        
    }

}
