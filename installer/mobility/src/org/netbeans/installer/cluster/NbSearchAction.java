/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.installer.cluster;

import com.installshield.product.SoftwareObject;
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
    
    public NbSearchAction() {
    }
    
    public void build(WizardBuilderSupport support) {
        try {
            support.putClass(Util.class.getName());
            support.putClass(NbSearchAction.SoftwareObjectComparator.class.getName());
        } 
        catch (Exception ex) {
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
        
        String searchMsg = resolveString
        ("$L(org.netbeans.installer..cluster.Bundle, NbSearchAction.searchMessage)");
        evt.getUserInterface().setBusy(searchMsg);
        
        findNb();
    }
    
    /** Look for all installation of NetBeans IDE using vpd.properties.
     */
    void findNb () {
        try {
            // Get the instance of RegistryService
            String nbUID = resolveString
            ("$L(org.netbeans.installer.cluster.Bundle,NetBeans.productUID)");
            RegistryService regserv = (RegistryService) getService(RegistryService.NAME);  
            String [] arr = regserv.getAllSoftwareObjectUIDs();
            /*for (int i = 0; i < arr.length; i++) {
               System.out.println("arr[" + i + "]: " + arr[i]);
            }*/
            //Look for any profiler installation
            SoftwareObject [] soArr = null;
            //System.out.println("substring:" + nbUID.substring(26,32));
            for (int i = 0; i < arr.length; i++) {
                if (arr[i].equals(nbUID)) {
                    soArr = regserv.getSoftwareObjects(arr[i]);
                    System.out.println("so.length:" + soArr.length);
                    for (int j = 0; j < soArr.length; j++) {
                        logEvent(this, Log.DBG,"so[" + j + "]:"
                        + " displayName: " + soArr[j].getDisplayName()
                        + " name: " + soArr[j].getName()
                        + " productNumber: " + soArr[j].getProductNumber()
                        + " installLocation: " + soArr[j].getInstallLocation());
                        //Fix: Due to unresolved product properties for NB 4.0 and NB 4.1
                        //we must check UID not ProductNumber.
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
        // Sort anagram groups according to size
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
