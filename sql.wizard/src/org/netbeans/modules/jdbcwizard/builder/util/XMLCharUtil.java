/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.jdbcwizard.builder.util;

/**
 * @author M.S.Veerendra
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public final class XMLCharUtil {
	
	public static final char[] specialChars = { '~','!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '+', '|', '}', '{', '"', ':', '?', '>', '<', '`', '-', '=', '\\', ']', '[', '\'', ';', '/', '.',','};
	public static final String[] replaceChars={ "_T_","_E_","_R_","_ASH_","_D_","_P_","_C_","_A_","_S_","_OP_","_CL_","_PL_","_OR_","_CB_","_OB_","_DQ_","_CLN_","_Q_","_GT_","_LT_","_QO_","_MI_","_EQ_","_BS_","_BC_","_BO_","_AP_","_SCL_","_FS_","_DOT_","_COM_"};
	public static final String[] appendCols={"_C","_CN","_FD","_FS","_MF","_M","_MD","_MR","_Q","_RS","_RC","_RT","_R","_RSA","_T","_UC","_W"};
	public static final String[] reservedCols={"Concurrency","CursorName","FetchDirection","FetchSize","MaxFiledSize","MaxRows","MetaData","MoreResults","QueryTimeout","ResultSet","ResultSetConcurrency","ResultSetType","Row","RSAgent","Type","UpdateCount","Warnings"};
	
	public static String getReplacement(char c)
	{
		
	    for (int i=0;i<specialChars.length;i++)
		{
			System.out.println(specialChars[i]+"  " + replaceChars[i]);
		}

		for (int i=0;i<specialChars.length;i++)
		{
			if(specialChars[i]==c)
			{
				return replaceChars[i];
			}
		}

		System.out.println(specialChars.length);
		System.out.println(specialChars.length);
		
		return null;
		
	}

	
	public static String makeValidNCName(String name) {
		StringBuffer nCName = new StringBuffer();
		if (name == null)
			name = "";

		name = name.trim();
		int size = name.length();

		char ncChars[] = name.toCharArray();

		int i = 0;

		for (i = 0; i < size; i++) {

			char ch = ncChars[i];

			if (((i == 0)
					&& !(Character.isJavaIdentifierStart(ch) && (ch != '$')) && !Character
					.isDigit(ch))
					|| ((i > 0) && !(Character.isJavaIdentifierPart(ch) && (ch != '$')))) {
				String replace = getReplacement(ch);
				if (replace != null) {
					nCName.insert(nCName.length(), replace);

				} else {
					nCName.insert(nCName.length(), "_Z_");

				}
			} else {
				nCName.append(ncChars[i]);
			}

		}

		if ((i > 0) && Character.isDigit(nCName.charAt(0))) {
			nCName.insert(0, "X_");
		}
		if ((i > 0) && nCName.charAt(0) == '_') {
			nCName.insert(0, "X");
		}

		return nCName.toString();

	}

	public static boolean isJavaName(String name) {
        if (name == null) {
            name = "";
        }
        name = name.trim();

        int   size      = name.length();
        char  ncChars[] = name.toCharArray();
        int   i         = 0;

        for (i = 1; i < size; i++) {
            char ch = ncChars[i];
            char ch0=ncChars[0];
            if ( !Character.isJavaIdentifierStart(ch0)
            		|| Character.isDigit(ch0)
					|| !Character.isJavaIdentifierPart(ch)){
        					return false;
	 				}
        }
        return true;
	 }
	public static String validColName(String name){

		StringBuffer nCName = new StringBuffer();
		nCName=nCName.insert(0,name);
		for (int k=0;k<reservedCols.length;k++)
		{
			String temp=reservedCols[k];
			if (temp.equals(name))
			{
				nCName = nCName.insert(nCName.length(),appendCols[k]);
				break;
			}
		}
		return nCName.toString();
	}

}
