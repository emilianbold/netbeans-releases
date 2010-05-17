/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package com.sun.rave.web.ui.model;

import com.sun.data.provider.FieldKey;
import com.sun.data.provider.impl.ObjectArrayDataProvider;
import com.sun.rave.web.ui.util.MessageUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Default date for the <code>Table</code> component. The following behavior is
 * implemented:
 * <ul>
 * <li>Upon component creation, pre-populate the table with some dummy data</li>
 * </ul>
 *
 * @author Winston Prakash
 */
public class DefaultTableDataProvider extends ObjectArrayDataProvider{

    /** Default constructor. */
    public DefaultTableDataProvider() {
        setArray(getDefaultTableData());
    }

    /**
     * Create data that will be displayed when the table is first dropped
     * in the designer
     */
    public Data[] getDefaultTableData(){
        int noRows = 5;
        int noCols = 3;
        Data[] dataSet = new Data[noRows];
        for (int i = 0; i < noRows; i++) {
            String[] dataStrs = new String[noCols];
            for(int j=0; j < noCols; j++){
                dataStrs[j] = getMessage("defaultTblCell", String.valueOf(i + 1), String.valueOf(j + 1));
            }
            dataSet[i] = new Data(dataStrs);
        }
        return dataSet;
    }
    
    /** Return the Field Keys skiiping the 0th index
     *   which is the "class" property
     */
    public FieldKey[] getFieldKeys() {
        FieldKey[] superFieldKeys = super.getFieldKeys();
        FieldKey[] fieldKeys = new FieldKey[superFieldKeys.length - 1];
        for(int i=1; i < superFieldKeys.length; i++){
             fieldKeys[i-1] = superFieldKeys[i];
        }
        return fieldKeys;
    }
    
    /**
     * Get the message substituting the arguments
     */
    public String getMessage(String key, String arg1, String arg2) {
        String bundle = getClass().getPackage().getName() + ".Bundle";
        return MessageUtil.getMessage(bundle, key, new Object[]{arg1, arg2});
    }
    
    /**
     * Data structure that holds data for three columns of a table
     */
    public static class Data {
        private String[] columns = null;
        
        public Data(String[] cols) {
            columns = cols;
        }
        
        /** Get first column. */
        public String getColumn1() {
            return columns[0];
        }
        
        /** Set first column. */
        public void setColumn1(String col) {
            columns[0] = col;
        }
        
        /** Get second column. */
        public String getColumn2() {
            return columns[1];
        }
        
        /** Set second column. */
        public void setColumn2(String col) {
            columns[1] = col;
        }
        
        /** Get third column. */
        public String getColumn3() {
            return columns[2];
        }
        
        /** Set third column. */
        public void setColumn3(String col) {
            columns[2] = col;
        }
    }
    
}
