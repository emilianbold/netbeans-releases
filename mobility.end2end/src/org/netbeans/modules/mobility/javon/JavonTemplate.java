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

package org.netbeans.modules.mobility.javon;

import java.util.Set;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.mobility.e2e.mapping.*;
import org.netbeans.modules.mobility.e2e.classdata.ClassDataRegistry;

/**
 *
 * @author Michal Skvor
 */
public abstract class JavonTemplate {
    
    /** Registry of classes */
    protected ClassDataRegistry registry;
    /** Javon mapping */
    protected JavonMapping mapping;
        
    /**
     * 
     * @param mapping 
     */
    public JavonTemplate( JavonMapping mapping ) {
        this.mapping = mapping;
        this.registry = mapping.getRegistry();
    }
    
    @SuppressWarnings( value = "unused" )
    private JavonTemplate() {}
    
    /**
     * Return array of all available targets
     * 
     * @return array of available targets
     */
    public abstract Set<String> getTargets();
    
    /**
     * Generate file for given output
     * 
     * @param ph progress handle 
     * @param target for generation
     * @return true when the operation ended succesfuly
     */
    public abstract boolean generateTarget( ProgressHandle ph, String target );    
}
