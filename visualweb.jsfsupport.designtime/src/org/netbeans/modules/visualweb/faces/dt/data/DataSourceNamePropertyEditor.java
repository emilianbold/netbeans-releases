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
