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

import java.util.ArrayList;
import org.netbeans.modules.mobility.javon.JavonProfileProvider;
import org.netbeans.modules.mobility.javon.JavonSerializer;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.mobility.javon.JavonTemplate;

/**
 *
 * @author Michal Skvor
 */
public class JavonProfileProviderImpl implements JavonProfileProvider {
    
    /** Creates a new instance of JavonProfileProviderImpl */
    public JavonProfileProviderImpl() {
    }
    
    public String getName() {
        return "default";
    }

    public String getDisplayName() {
        return "Default profile";
    }
    
    /**
     * Return list of Javon templates available in this profile
     * @param mapping 
     * @return 
     */
    public List<JavonTemplate> getTemplates( JavonMappingImpl mapping ) {
        List<JavonTemplate> templates = new ArrayList<JavonTemplate>( 1 );
        templates.add( new ClientJavonTemplate( mapping ));
        templates.add( new ServerJavonTemplate( mapping ));
        templates.add( new ClientBeanGeneratorTemplate( mapping ));
        return Collections.unmodifiableList( templates );
    }

    public List<JavonSerializer> getSerializers() {
        List<JavonSerializer> serializers = new ArrayList<JavonSerializer>();
        serializers.add( new PrimitiveTypeSerializer());
        serializers.add( new RealTypeSerializer());
        serializers.add( new ArrayTypeSerializer());
        serializers.add( new CollectionSerializer());
        serializers.add( new GenericTypeSerializer());
        serializers.add( new BeanTypeSerializer());
        return Collections.unmodifiableList( serializers );
    }
}
