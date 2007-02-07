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
 *
 * $Id$
 */
package org.netbeans.installer.utils.helper;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author Kirill Sorokin
 */
public class Feature {
    private String id;
    
    private long offset;
    
    private ExtendedUri iconUri;
    
    private Map<Locale, String> displayNames;
    private Map<Locale, String> descriptions;
    
    public Feature(
            final String id,
            final long offset,
            final ExtendedUri iconUri,
            final Map<Locale, String> displayNames,
            final Map<Locale, String> descriptions) {
        this.id = id;
        
        this.offset = offset;
        
        this.iconUri = iconUri;
        
        this.displayNames = new HashMap<Locale, String>();
        this.displayNames.putAll(displayNames);
        
        this.descriptions = new HashMap<Locale, String>();
        this.descriptions.putAll(descriptions);
    }
    
    public String getId() {
        return id;
    }
    
    public long getOffset() {
        return offset;
    }
    
    public ExtendedUri getIconUri() {
        return iconUri;
    }
    
    public String getDisplayName() {
        return getDisplayName(Locale.getDefault());
    }
    
    public String getDisplayName(final Locale locale) {
        return displayNames.get(locale);
    }
    
    public Map<Locale, String> getDisplayNames() {
        return displayNames;
    }
    
    public String getDescription() {
        return getDescription(Locale.getDefault());
    }
    
    public String getDescription(final Locale locale) {
        return descriptions.get(locale);
    }
    
    public Map<Locale, String> getDescriptions() {
        return descriptions;
    }
    
    public boolean equals(final Feature feature) {
        return feature != null ? id.equals(feature.getId()) : false;
    }
}
