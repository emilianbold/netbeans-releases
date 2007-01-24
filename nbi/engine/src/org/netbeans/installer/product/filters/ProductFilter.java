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
import org.netbeans.installer.product.RegistryNode;
import org.netbeans.installer.utils.helper.DetailedStatus;
import org.netbeans.installer.utils.helper.Status;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.helper.Platform;
import org.netbeans.installer.utils.helper.Version;

/**
 *
 * @author Kirill Sorokin
 */
public class ProductFilter implements RegistryFilter {
    private String uid;
    
    private List<Platform> platforms;
    
    private Version versionLower;
    private Version versionUpper;
    
    private Status status;
    private DetailedStatus detailedStatus;
    
    public ProductFilter() {
        platforms = new LinkedList<Platform>();
    }
    
    public ProductFilter(final Status status) {
        this();
        
        this.status = status;
    }
    
    public ProductFilter(final DetailedStatus detailedStatus) {
        this();
        
        this.detailedStatus = detailedStatus;
    }
    
    public ProductFilter(final String uid, final Platform platform) {
        this();
        
        this.uid = uid;
        this.platforms.add(platform);
    }
    
    public ProductFilter(final String uid, final Version version, final Platform platform) {
        this();
        
        this.uid          = uid;
        this.versionLower = version;
        this.versionUpper = version;
        this.platforms.add(platform);
    }
    
    public ProductFilter(final String uid, final List<Platform> platforms) {
        this();
        
        this.uid          = uid;
        this.platforms.addAll(platforms);
    }
    
    public ProductFilter(final String uid, final Version version, final List<Platform> platforms) {
        this();
        
        this.uid          = uid;
        this.versionLower = version;
        this.versionUpper = version;
        this.platforms.addAll(platforms);
    }
    
    public ProductFilter(final String uid, final Version versionLower, final Version versionUpper, final Platform platform) {
        this();
        
        this.uid          = uid;
        this.versionLower = versionLower;
        this.versionUpper = versionUpper;
        this.platforms.add(platform);
    }
    
    public boolean accept(final RegistryNode node) {
        if (node instanceof Product) {
            final Product product = (Product) node;
            
            if (uid != null) {
                if (!product.getUid().equals(uid)) {
                    return false;
                }
            }
            
            if ((versionLower != null) && (versionUpper != null)) {
                if (!product.getVersion().newerOrEquals(versionLower) ||
                        !product.getVersion().olderOrEquals(versionUpper)) {
                    return false;
                }
            }
            
            if (platforms.size() > 0) {
                if (!SystemUtils.intersects(
                        product.getSupportedPlatforms(),
                        platforms)) {
                    return false;
                }
            }
            
            if (status != null) {
                if (product.getStatus() != status) {
                    return false;
                }
            }
            
            if (detailedStatus != null) {
                if (product.getDetailedStatus() != detailedStatus) {
                    return false;
                }
            }
            
            return true;
        }
        
        return false;
    }
}
