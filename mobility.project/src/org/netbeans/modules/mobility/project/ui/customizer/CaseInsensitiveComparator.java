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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * Created on Nov 30, 2004
 */
package org.netbeans.modules.mobility.project.ui.customizer;
import java.util.Comparator;

/**
 * @author gc149856
 *
 */
public class CaseInsensitiveComparator implements Comparator {
    
    private static CaseInsensitiveComparator instance=null;
    
    private CaseInsensitiveComparator() {
        //to avoid instantiation
    }
    
    private static CaseInsensitiveComparator createInstance() {
        return new CaseInsensitiveComparator();
    }
    
    public static synchronized CaseInsensitiveComparator getInstance() {
        if (instance==null)
            instance = createInstance();
        
        return instance;
    }
    
    public int compare(final Object first, final Object second) {
        return ((String)first).compareToIgnoreCase((String)second);
        
    }
    
}
