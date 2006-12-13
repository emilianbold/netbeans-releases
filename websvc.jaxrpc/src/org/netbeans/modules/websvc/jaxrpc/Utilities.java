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

package org.netbeans.modules.websvc.jaxrpc;

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;
import org.openide.WizardDescriptor;

/** Utility methods shared for web service modules.
 *
 * @author Peter Williams
 */
public final class Utilities {
    
    private Utilities() {
    }
    
    /** This method ensures the list of steps displayed in the left hand panel
     *  of the wizard is correct for any given displayed panel.
     *
     *  Taken from web/core
     */
    public static String[] createSteps(String[] before, WizardDescriptor.Panel[] panels) {
        //assert panels != null;
        // hack to use the steps set before this panel processed
        int diff = 0;
        if (before == null) {
            before = new String[0];
        } else if (before.length > 0) {
            diff = ("...".equals(before[before.length - 1])) ? 1 : 0; // NOI18N
        }
        String[] res = new String[ (before.length - diff) + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (before.length - diff)) {
                res[i] = before[i];
            } else {
                res[i] = panels[i - before.length + diff].getComponent().getName();
            }
        }
        return res;
    }
    
    /** Class/Identifier validation
     */
    public static boolean isJavaIdentifier(String id) {
        boolean result = true;
        
        if(id == null || id.length() == 0 || !Character.isJavaIdentifierStart(id.charAt(0))) {
            result = false;
        } else {
            for(int i = 1, idlength = id.length(); i < idlength; i++) {
                if(!Character.isJavaIdentifierPart(id.charAt(i))) {
                    result = false;
                    break;
                }
            }
        }
        
        return result;
    }
    
    /** Package name validation
     */
    public static boolean isJavaPackage(String pkg) {
        boolean result = false;
        
        if(pkg != null && pkg.length() > 0) {
            int state = 0;
            for(int i = 0, pkglength = pkg.length(); i < pkglength && state < 2; i++) {
                switch(state) {
                    case 0:
                        if(Character.isJavaIdentifierStart(pkg.charAt(i))) {
                            state = 1;
                        } else {
                            state = 2;
                        }
                        break;
                    case 1:
                        if(pkg.charAt(i) == '.') {
                            state = 0;
                        } else if(!Character.isJavaIdentifierPart(pkg.charAt(i))) {
                            state = 2;
                        }
                        break;
                }
            }
            
            if(state == 1) {
                result = true;
            }
        }
        
        return result;
    }
    
    /** Retrieve the canonical version of a File instance, converting the possible
     *  IOException into a null (presumably for error presentation purposes).
     */
    public static File getCanonicalFile(File f) {
        File f1;
        try {
            f1 = f.getCanonicalFile();
        } catch (IOException e) {
            f1 = null;
        }
        return f1;
    }
    
    public static String removeSpacesFromServiceName(String serviceName) {
        if (serviceName!=null) {
            String result = ""; //NOI18N
            if (serviceName.indexOf(" ") > -1) {  //NOI18N
                StringTokenizer serviceNameTokenizer = new StringTokenizer(serviceName, " ", false); //NOI18N
                while (serviceNameTokenizer.hasMoreTokens()) {
                    StringBuffer token = new StringBuffer(serviceNameTokenizer.nextToken());
                    if (token != null) {
                        token.setCharAt(0, Character.toUpperCase(token.charAt(0)));
                        result = result.concat(token.toString());
                    }
                }
                return result;
            } else if (serviceName.length()>0) {
                result = Character.toUpperCase(serviceName.charAt(0))+serviceName.substring(1);
            }
            // find the digits and change the following letters to upper case
            StringBuffer buf = new StringBuffer(result);
            for (int i=0;i<buf.length();i++) {
                if (Character.isDigit(buf.charAt(i))) {
                    if ((i+1)<buf.length() && !Character.isDigit(buf.charAt(i+1))) {
                        buf.setCharAt(i+1, Character.toUpperCase(buf.charAt(i+1)));
                        ++i;
                    }
                }
                result = buf.toString();
            }
            return result;
        }
        return null;
    }   
}
