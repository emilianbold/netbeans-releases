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

package org.netbeans.modules.mobility.e2e.mapping;

import org.netbeans.modules.mobility.javon.JavonProfileProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.mobility.javon.JavonTemplate;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Michal Skvor
 */
public class Javon {
    
    private JavonMappingImpl mapping;
    
    /** 
     * Creates a new instance of Javon 
     * 
     * @param mapping 
     */
    public Javon( JavonMappingImpl mapping ) {
        this.mapping = mapping;
    }
    
    /**
     * Generate output
     */
    public void generate( ProgressHandle ph ) {
        // Get providers
        Lookup.Result<JavonProfileProvider> providersResult = 
                Lookup.getDefault().lookup( new Lookup.Template<JavonProfileProvider>(
                    JavonProfileProvider.class ));
        Map<String, JavonProfileProvider> providers = new HashMap<String, JavonProfileProvider>();
        for( JavonProfileProvider provider : providersResult.allInstances()) {
            providers.put( provider.getName(), provider );
        }
        if( providers.size() == 0 ) {
            // No providers
            return;
        }
        
        // TODO: Hack for default provider. Name should be set from the dialog
        JavonProfileProvider provider = providers.get( "default" ); // NOI18N
        
        List<JavonTemplate> templates = provider.getTemplates( mapping );
        
        // Run templates
        for( JavonTemplate template : templates ) {
            Set<String> targets = template.getTargets();
            for( String target : targets ) {
                template.generateTarget( ph, target );
            }
        }
        
        ph.finish();        
    }    
}
