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

package org.netbeans.installer;

import com.installshield.util.Log;

/**
 * Add/remove string to/from productid string.
 * 
 */

public class PatchProductID {
    
    public static final String PACK_ID_PROFILER = "PROF";
    public static final String PACK_ID_MOBILITY = "MOB";
    public static final String PACK_ID_CDC = "CDC";
    public static final String PACK_ID_CND = "CND";
    public static final String PACK_ID_ENTERPISE = "ENT";
    public static final String PACK_ID_CREATOR = "CRE";
    
    public static final String NB_ID_IDE = "NB";
    
    private PatchProductID () {
    }

    /**
     * Adds packID to productID to keep ordering. Nothing is done when packID is
     * already present in original productID ie. original productID is returned unchanged.
     * 
     * @param productID original productID. It cannot be empty.
     * @param packID packID to be removed
     * @return modified productID
     */
    public static String add (String productID, String packID, Log log) {
        if (productID.length() == 0) {
            log.logEvent(PatchProductID.class,Log.ERROR,"Error: Input productID cannot be empty.");
            return productID;
        }
        if (productID.indexOf(packID) != -1) {
            log.logEvent(PatchProductID.class,Log.WARNING,"Warning: PackID:'" + packID 
            + "' is already present in productID:'" + productID + "'.");
            return productID;
        }
        String [] arr = productID.split("_");
        StringBuilder sb = new StringBuilder();
        sb.append(arr[0]);
        if (arr.length == 1) {
            sb.append("_");
            sb.append(packID);
        } else {
            boolean added = false;
            for (int i = 1; i < arr.length; i++) {
                if (!added && (packID.compareTo(arr[i]) < 0)) {
                    sb.append("_");
                    sb.append(packID);
                    added = true;
                }
                sb.append("_");
                sb.append(arr[i]);
            }
            if (!added) {
                sb.append("_");
                sb.append(packID);
            }
        }
        return sb.toString();
    }
    
    /**
     * Removes packID from productID. Nothing is done when packID is not
     * present in original productID ie. original productID is returned unchanged.
     * First token is never removed.
     * 
     * @param productID original productID
     * @param packID packID to be removed
     * @return modified productID
     */
    public static String remove (String productID, String packID, Log log) {
        if (productID.length() == 0) {
            log.logEvent(PatchProductID.class,Log.ERROR,"Error: Input productID cannot be empty.");
            return productID;
        }
        if (productID.indexOf(packID) == -1) {
            log.logEvent(PatchProductID.class,Log.WARNING,"Warning: PackID:'" + packID 
            + "' is not present in productID:'" + productID + "'.");
            return productID;
        }
        String [] arr = productID.split("_");
        StringBuilder sb = new StringBuilder();
        sb.append(arr[0]);
        for (int i = 1; i < arr.length; i++) {
            if (!packID.equals(arr[i])) {
                sb.append("_");
                sb.append(arr[i]);
            }
        }
        return sb.toString();
    }

}
