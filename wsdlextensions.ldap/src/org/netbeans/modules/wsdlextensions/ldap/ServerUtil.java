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
package org.netbeans.modules.wsdlextensions.ldap;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import org.netbeans.modules.wsdlextensions.ldap.ldif.LdifObjectClass;

/**
 *
 * @author Gary
 */
public class ServerUtil {

    private static LdapContext mConnection = null;
    private static String ldapUrl = null;

    private ServerUtil() {
    }

    @SuppressWarnings("unchecked")
    public static void getConnection(String url) {
        try {
            Hashtable env = new Hashtable();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(Context.PROVIDER_URL, url);
            mConnection = new InitialLdapContext(env, null);
            ldapUrl = url;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static List getObjectNames() throws NamingException {
        List ret = new ArrayList();
        DirContext top = mConnection.getSchema("");
        NamingEnumeration ne = top.list("ClassDefinition");

        while (ne.hasMore()) {
            NameClassPair pair = (NameClassPair) ne.next();
            String clsName = pair.getName();
            ret.add(clsName);
        }

        return ret;
    }

    public static LdifObjectClass getObjectClass(String objName) throws NamingException {
        LdifObjectClass objClass = new LdifObjectClass();

        DirContext top = mConnection.getSchema("");
        DirContext schema = (DirContext) top.lookup("ClassDefinition/" + objName);
        Attributes atrs = schema.getAttributes("");
        Attribute name = atrs.get("NAME");
        NamingEnumeration nameValue = name.getAll();
        objClass.setName((String) nameValue.next());
        objClass.setLdapUrl(ldapUrl);

        Attribute sup = atrs.get("SUP");
        if (sup != null) {
            NamingEnumeration supValue = sup.getAll();
            String sups = "";
            while (supValue.hasMore()) {
                String a = (String) supValue.next();
                sups += a + ", ";
            }
            if (sups.length() > 1) {
                sups = sups.substring(0, sups.length() - 2);
            }
            objClass.setSuper(sups);
        }

        Attribute may = atrs.get("MAY");
        if (may != null) {
            NamingEnumeration mayValue = may.getAll();
            while (mayValue.hasMore()) {
                String a = (String) mayValue.next();
                objClass.addMay(a);
            }
        }

        Attribute must = atrs.get("MUST");
        if (must != null) {
            NamingEnumeration mustValue = must.getAll();
            String musts = "";
            while (mustValue.hasMore()) {
                String a = (String) mustValue.next();
                objClass.addMust(a);
            }
        }
        objClass.setSelected(new ArrayList());
        return objClass;
    }

    @SuppressWarnings("unchecked")
    public static ArrayList getObjectAttributes(String objName) throws NamingException {
        ArrayList ret = new ArrayList();

        DirContext top = mConnection.getSchema("");
        DirContext schema = (DirContext) top.lookup("ClassDefinition/" + objName);
        Attributes atrs = schema.getAttributes("");

        Attribute must = atrs.get("MUST");
        if (must != null) {
            NamingEnumeration mustValue = must.getAll();
            String musts = "";
            while (mustValue.hasMore()) {
                String a = (String) mustValue.next();
                ret.add(a);
            }
        }

        Attribute may = atrs.get("MAY");
        if (may != null) {
            NamingEnumeration mayValue = may.getAll();
            while (mayValue.hasMore()) {
                String a = (String) mayValue.next();
                ret.add(a);
            }
        }

        return ret;
    }
}
