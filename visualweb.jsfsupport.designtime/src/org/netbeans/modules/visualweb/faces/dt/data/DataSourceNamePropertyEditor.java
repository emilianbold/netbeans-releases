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
package org.netbeans.modules.visualweb.faces.dt.data;

import java.beans.PropertyEditorSupport;
import java.util.ArrayList;
import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class DataSourceNamePropertyEditor extends PropertyEditorSupport {

    private static final String DS_SUBCTX = "java:comp/env/jdbc"; // NOI18N

    public String[] getTags() {
        try {
            Context ctx = new InitialContext();
            ArrayList dsNameList = getDataSources(ctx, DS_SUBCTX, "" ) ;
            return (String[])dsNameList.toArray(new String[dsNameList.size()]);
        } catch (NamingException e) {
            return null;
        }
    }

    public ArrayList getDataSources( Context curCtx, String startName, String dsNamePrefix )
        throws NamingException {
            String nodeName = null;
            ArrayList retVal = new ArrayList() ;

            NamingEnumeration list = curCtx.listBindings(startName);
            while (list.hasMore()) {
                Binding binding = (Binding)list.next();
                String name = binding.getName();
                if (binding.isRelative()) {
                    // append the name to the startimg name.
                    name = startName + "/" + name; // NOI18N
                }
                nodeName = name.substring(name.lastIndexOf("/") + 1);
                if (binding.getObject() instanceof DataSource) {

                    String dsNodeName = ("".equals(dsNamePrefix) ? "" : dsNamePrefix + "/") + nodeName ;
                    retVal.add( dsNodeName ) ;

                }
                else if ( binding.getObject() instanceof Context ) {
                    // It's a subcontext, so search that.
                    retVal.addAll(getDataSources((Context)binding.getObject(), "", dsNamePrefix + ("".equals(dsNamePrefix) ? "" : "/" ) + nodeName ) );
                }
            }
            list.close();
            return retVal ;
    }

    public void setAsText(String text) {
        /* setAsText should hand me the tag.  That is, if the user selects the tag
         * "Travel" from the dropdown, netbeans should hand me "Travel" as the arg
         * to setAsText.  For the most part, netbeans does this; but, if "Travel"
         * is already selected in the drop down and I select it again (i.e., I'm not
         * looking to change anything, netbeans hands me "java:comp/env/jdbcTravel"
         * as the argument to setAsText.  As a workaround, check for and strip
         * the DS_SUBCTX
         */
        setValue(DS_SUBCTX + "/" + stripSubContext(text)); //NOI18N
    }

    public String getAsText() {
        return stripSubContext((String)getValue());
    }

    public String getJavaInitializationString() {
        return getAsText().equals("null") ? "null" : //NOI18N
            "\"" + DS_SUBCTX + "/" + getAsText() + "\""; //NOI18N
    }

    private String stripSubContext(String name) {
        return name == null ? name : name.replaceFirst(DS_SUBCTX + "/", ""); //NOI18N
    }
}
