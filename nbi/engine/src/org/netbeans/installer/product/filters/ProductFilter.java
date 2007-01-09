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
package org.netbeans.installer.product.filters;

import java.util.LinkedList;
import java.util.List;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.product.ProductRegistryNode;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.helper.Platform;
import org.netbeans.installer.utils.helper.Version;

/**
 *
 * @author Kirill Sorokin
 */
public class ProductFilter implements RegistryFilter {
    private String   uid = null;
    
    private List<Platform> platforms = new LinkedList<Platform>();
    
    private Version  versionLower = null;
    private Version  versionUpper = null;
    
    public ProductFilter() {
        // does nothing
    }
    
    public ProductFilter(final String uid, final Version version, final Platform platform) {
        this.uid          = uid;
        this.platforms    = new LinkedList<Platform>();
        this.versionLower = this.versionUpper = version;
        this.platforms.add(platform);
    }
    
    public ProductFilter(final String uid, final Version version, final List<Platform> platforms) {
        this.uid          = uid;
        this.platforms    = new LinkedList<Platform>(platforms);
        this.versionLower = this.versionUpper = version;
    }
    
    public ProductFilter(final String uid, final Version versionLower, final Version versionUpper, final Platform platform) {
        this.uid          = uid;
        this.platforms    = new LinkedList<Platform>();
        this.versionLower = versionLower;
        this.versionUpper = versionUpper;
        this.platforms.add(platform);
    }
    
    public boolean accept(final ProductRegistryNode node) {
        if (node instanceof Product) {
            Product component = (Product) node;
            if (uid != null) {
                if (!component.getUid().equals(uid)) {
                    return false;
                }
            }
            if ((versionLower != null) && (versionUpper != null)) {
                if (!component.getVersion().newerOrEquals(versionLower) || !component.getVersion().olderOrEquals(versionUpper)) {
                    return false;
                }
            }
            
            if (platforms.size() > 0) {
                if (!SystemUtils.intersects(component.getSupportedPlatforms(), platforms)) {
                    return false;
                }
            }
            
            return true;
        }
        
        return false;
    }
}
