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

package org.netbeans.installer.cluster;

import com.installshield.product.SoftwareObject;
import com.installshield.product.SoftwareVersion;
import com.installshield.product.service.registry.RegistryService;
import com.installshield.util.Log;
import com.installshield.wizard.CancelableWizardAction;
import com.installshield.wizard.WizardBeanEvent;
import com.installshield.wizard.WizardBuilderSupport;
import com.installshield.wizard.service.ServiceException;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import org.netbeans.installer.Util;

public class NbSearchAction extends CancelableWizardAction {
    
    private static Vector nbHomeList = new Vector();
    
    private String [] nbUIDArray = new String[0];
    
    private static final String BUNDLE = "$L(org.netbeans.installer.cluster.Bundle,";
    
    public NbSearchAction() {
    }
    
    public void build(WizardBuilderSupport support) {
        try {
            support.putClass(Util.class.getName());
            support.putClass(NbSearchAction.SoftwareObjectComparator.class.getName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public void execute(WizardBeanEvent evt) {
        //Return if nbHomeList is not empty. This is an work around
        //to make the action not to search the NBs if the user click
        //back button.
        if (!nbHomeList.isEmpty()) {
            return;
        }
        
        //Handle silent mode where getUserInterface returns null
        if (evt.getUserInterface() != null) {
            String searchMsg = resolveString(BUNDLE + "NbSearchAction.searchMessage)");
            evt.getUserInterface().setBusy(searchMsg);
        }
        
        initNbUIDArray();
        findNb();
    }
    
    private void initNbUIDArray () {
        int arrLength = 0;
        String s = resolveString(BUNDLE + "NetBeans.UIDLength)");
        try {
            arrLength = Integer.parseInt(s);
        } catch (NumberFormatException exc) {
            logEvent(this, Log.ERROR,"Incorrect number for NetBeans.UIDLength: " + s);
        }
        
        //No order is defined.
        if (arrLength == 0) {
            return;
        }
        nbUIDArray = new String[arrLength];
        for (int i = 0; i < arrLength; i++) {
            nbUIDArray[i] = resolveString(BUNDLE + "NetBeans.UID" + i + ")");
            logEvent(this, Log.DBG,"nbUIDArray[" + i + "]: " + nbUIDArray[i]);
        }
    }
    
    private boolean acceptNbUID (String nbUID) {
        for (int i = 0; i < nbUIDArray.length; i++) {
            if (nbUID.equals(nbUIDArray[i])) {
                return true;
            }
        }
        return false;
    }
    
    /** Look for all installation of NetBeans IDE using vpd.properties.
     */
    void findNb () {
        try {
            // Get the instance of RegistryService
            RegistryService regserv = (RegistryService) getService(RegistryService.NAME);  
            String [] arr = regserv.getAllSoftwareObjectUIDs();
            /*for (int i = 0; i < arr.length; i++) {
               logEvent(this, Log.DBG,"arr[" + i + "]: " + arr[i]);
            }*/
            
            //Look for any NetBeans IDE installation
            SoftwareObject [] soArr = null;
            //System.out.println("substring:" + nbUID.substring(26,32));
            for (int i = 0; i < arr.length; i++) {
                if (acceptNbUID(arr[i])) {
                    soArr = regserv.getSoftwareObjects(arr[i]);
                    System.out.println("so.length:" + soArr.length);
                    for (int j = 0; j < soArr.length; j++) {
                        SoftwareVersion version = soArr[j].getKey().getVersion();
                        logEvent(this, Log.DBG,"so[" + j + "]:"
                        + " uid: " + soArr[j].getKey().getUID()
                        + " displayName: " + soArr[j].getDisplayName()
                        + " name: " + soArr[j].getName()
                        + " productNumber: " + soArr[j].getProductNumber()
                        + " installLocation: " + soArr[j].getInstallLocation()
                        + " version:" + version);
                        nbHomeList.add(soArr[j]);
                    }
                }
            }
            orderList(nbHomeList);
        } catch (ServiceException exc) {
            logEvent(this, Log.ERROR, exc);
        }
    }
    
    private static void orderList (Vector nbHomeList) {
        Collections.sort(nbHomeList, new SoftwareObjectComparator());
    }
    
    /** Collection of SoftwareObject instances is ordered in ascending order.
     * It means that latest version is last.
     */
    public static int getLatestVersionIndex() {
        return nbHomeList.size() - 1;
    }
    
    public static Vector getNbHomeList () {
        return nbHomeList;
    }
    
    /** Used to sort collection of SoftwareObject instances
     */
    private static class SoftwareObjectComparator implements Comparator {
    
        public int compare(Object o1, Object o2) {
            if ((o1 instanceof SoftwareObject) && (o2 instanceof SoftwareObject)) {
                SoftwareObject so1 = (SoftwareObject) o1;
                SoftwareObject so2 = (SoftwareObject) o2;
                String s1 = so1.getKey().getUID().substring(0,29);
                String s2 = so2.getKey().getUID().substring(0,29);
                return s1.compareTo(s2);
            } else {
                return 0;
            }
        }
    }
 }
