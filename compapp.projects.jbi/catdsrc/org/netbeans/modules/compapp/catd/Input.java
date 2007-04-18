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

/*
 * Input.java
 *
 * Created on March 25, 2005, 10:58 AM
 */

package org.netbeans.modules.compapp.catd;

import org.netbeans.modules.compapp.catd.util.Util;
import java.io.*;
import java.util.*;

/**
 *
 * ================Example dataFile =====================
 * #comments start with #
 * symbol,price,amount,action
 * SBYN,10.12,25000,BOUGHT
 * SBYN,10.13,4000,SOLD
 * ....
 * IBM,20.12,200,SOLD
 * ===============End of example dataFile ===============
 *
 * ===============Example templateFile: batchSize=2 ===================
 * <SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/"
 *              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 *              xmlns:xsd="http://www.w3.org/2001/XMLSchema"
 *              xmlns:tns="http://seebeyond.com/test_iep/"
 *              xmlns="test_iep">
 *   <SOAP-ENV:Header/>
 *   <SOAP-ENV:Body>
 *     <StreamInput0>
 *       <evts>
 *         <evt>
 *           <name>$symbol.0</name>        
 *           <price>$price.0</price>       
 *           <act>$action.0<act>           
 *         </evt>
 *         <evt>
 *           <name>$symbol.1</name>        
 *           <price>$price.1</price>       
 *           <act>$action.1<act>           
 *         </evt>
 *       </evts>
 *     </StreamInput0>
 *   </SOAP-ENV:Body>
 * </SOAP-ENV:Envelope>
 * ===============End of Example templateFile============
 *
 *
 * @author blu
 */
public class Input {
    private String mName;
    private String mAction;
    private String mTemplate;
    private String[] mColumnNames;
    private List mRowList;
    private int mRowCount;
    private int mCurrentRow;
    private int mBatchSize;

    private List loadData(String dataFileName) {
        // read and cache all input
        List rowList = new ArrayList();
        BufferedReader fileIn = null;
        int colCnt = 0;
        try {
            fileIn = new BufferedReader(new InputStreamReader(new FileInputStream(dataFileName)));
            String record = fileIn.readLine();
            while (record.startsWith("#")) {
                record = fileIn.readLine(); // skip comments
            }
            StringTokenizer st = new StringTokenizer(record, ",");
            colCnt = st.countTokens();
            mColumnNames = new String[colCnt];
            for (int i = 0; i < colCnt; i++) {
                mColumnNames[i] = st.nextToken();
            }
            while (true) {
                record = fileIn.readLine();
                if (record == null) {
                    break;
                }
                if (record.startsWith("#")) {
                    continue;  // skip comments
                }
                Object[] row = new Object[colCnt];
                st = new StringTokenizer(record, ",");
                for (int i = 0; i < colCnt; i++) {
                    if (st.hasMoreTokens()) {
                        row[i] = st.nextToken();
                    } else {
                        row[i] = "";
                    }
                }
                rowList.add(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileIn != null) {
                    fileIn.close();
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        return rowList;
    }

    private void incrementRow() {
        if (mCurrentRow < mRowCount - 1) {
            mCurrentRow++;
        } else {
            mCurrentRow = 0;
        }
    }
    
    public Input(String name, String action, String templateFile, String dataFile, int batchSize) {
        mName = name;
        mAction = action;
        mTemplate = Util.getFileContent(new File(templateFile));
        mRowList = loadData(dataFile);
        mRowCount = mRowList.size();
        mCurrentRow = 0;
        mBatchSize = batchSize;
//        System.out.println("templateFile: " + templateFile);
//        System.out.println("dataFile: " + dataFile);
    }
    
    public String getName() {
        return mName;
    }
    
    public String getAction() {
        return mAction;
    }
    
    public String nextData() {
        String ret = null;
        String s = mTemplate;
        for (int i = 0; i < mBatchSize; i++) {
            Object[] row = (Object[])mRowList.get(mCurrentRow);
            incrementRow();
            for (int j = 0, J = row.length; j < J; j++) {
                String data = (String)row[j];
                String placeHolder = "${" + mColumnNames[j] + "." + i + "}";
                s = Util.replaceAll(s, placeHolder, data);
            }
        }
        return s;
    }
        
}
