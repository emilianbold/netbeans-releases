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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.soa.pojo.anttasks.util;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;
import java.util.StringTokenizer;

/**
 *
 * @author Sreenivasan Genipudi
 */
public class GeneratorUtil {

    public static final String POJO_SERVICE_SUFFIX="Service";//NOI18N
    public static final String POJO_INTERFACE_SUFFIX="Interface";//NOI18N
    public static final String POJO_OUT_MESSAGE_SUFFIX="OperationResponse";//NOI18N

    public static boolean createFile(InputStream is, File targetFile) {
        FileWriter fw = null;
        boolean bReturn =true;
        try {
            fw = new FileWriter(targetFile);
            int c = -1;
            BufferedInputStream bis = new BufferedInputStream(is);
            while ((c = bis.read()) != -1) {
                fw.write((char)c);
            }
            fw.flush();

        } catch (Exception ex) {
           ex.printStackTrace();
           bReturn = false;
        } finally {
            try {
                fw.close();
            
            } catch (IOException ee) {

            }
            fw = null;
        }
        return bReturn;
    }
    
    
    /**
     * Get the namespace formatted for POJO wizard.
     * @param pkg
     * @return String
     */
 public static String getNamespace(String pkg, String endpointName) {
        Stack<String> dotName = new Stack<String>();

        StringTokenizer stk = new StringTokenizer(pkg, ".");
        while (stk.hasMoreTokens()){
            dotName.push(stk.nextToken());
        }

        StringBuilder sb = new StringBuilder();
        sb.append("http://");
        boolean first = true;
        while (!dotName.empty()){
            if (!first){
                sb.append("."); //NOI18N
            } else {
                first = false;
            }
            sb.append(dotName.pop());
        }
        sb.append("/");
        sb.append(endpointName);
        sb.append("/");               
                
        return sb.toString();
    }       
}
