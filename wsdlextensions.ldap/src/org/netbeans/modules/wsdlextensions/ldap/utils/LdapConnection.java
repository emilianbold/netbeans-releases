/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.wsdlextensions.ldap.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.StartTlsRequest;
import javax.naming.ldap.StartTlsResponse;
import javax.swing.JOptionPane;
import org.netbeans.modules.wsdlextensions.ldap.LdapConnectionProperties;
import org.netbeans.modules.wsdlextensions.ldap.ldif.LdifObjectClass;
import org.openide.util.Exceptions;

/**
 *
 * @author Gary Zheng
 */
public class LdapConnection extends LdapConnectionProperties {

    public static String SSL_TYPE_NONE = "None";
    public static String SSL_TYPE_SSL = "Enable SSL";
    public static String SSL_TYPE_TLS = "TLS on demand";
//    public static int CONNECTION_CREATED_COUNT = 0;
//    public static int CONNECTION_CLOSED_COUNT = 0;
    private LdapContext connection;
    private String dn;
    private String curConnectionType = "";
    private boolean connectionReconnect = true;

    public LdapConnection() {
    }

    public String getDn() {
        return dn;
    }

    public void setDn(String dn) {
        this.dn = dn;
    }

    public Object getProperty(String property) {
        try {
            Class cls = this.getClass();
            property = property.substring(0, 1).toUpperCase() + property.substring(1);
            Method method = cls.getMethod("get" + property);
            return method.invoke(this, (java.lang.Object[]) null);
        } catch (NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
        } catch (SecurityException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }

        return "";
    }

    private static boolean isEmpty(String str) {
        if (null == str) {
            return true;
        }

        if (str.length() == 0) {
            return true;
        }

        return false;
    }

    private LdapContext createSSLConnection() {
        LdapContext ret = null;
        try {
            if (!isEmpty(this.getTruststore())) {
                System.setProperty("javax.net.ssl.trustStore", this.getTruststore());
            }
            if (!isEmpty(this.getTruststoretype())) {
                System.setProperty("javax.net.ssl.trustStoreType", this.getTruststoretype());
            }
            if (!isEmpty(this.getTruststorepassword())) {
                System.setProperty("javax.net.ssl.trustStorePassword", this.getTruststorepassword());
            }
            if (!isEmpty(this.getKeystore())) {
                System.setProperty("javax.net.ssl.keyStore", this.getKeystore());
            }
            if (!isEmpty(this.getKeystorepassword())) {
                System.setProperty("javax.net.ssl.keyStorePassword", this.getKeystorepassword());
            }
            if (!isEmpty(this.getKeystoreusername())) {
                System.setProperty("javax.net.ssl.keyStoreUsername", this.getKeystoreusername());
            }
            if (!isEmpty(this.getKeystoretype())) {
                System.setProperty("javax.net.ssl.keyStoreType", this.getKeystoretype());
            }

            Hashtable<String, String> env = new Hashtable<String, String>();
            if (!isEmpty(this.getPrincipal())) {
                env.put(Context.SECURITY_PRINCIPAL, this.getPrincipal());
            }
            if (!isEmpty(this.getCredential())) {
                env.put(Context.SECURITY_CREDENTIALS, this.getCredential());
            }
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(Context.PROVIDER_URL, this.getLocation());
            if (!isEmpty(this.getAuthentication())) {
                env.put(Context.SECURITY_AUTHENTICATION, this.getAuthentication());
            }
            if (!isEmpty(this.getProtocol())) {
                env.put(Context.SECURITY_PROTOCOL, this.getProtocol());
            }

            ret = new InitialLdapContext(env, null);
            if (this.getTlssecurity().toUpperCase().equals("YES") ||
                    LdapConnection.SSL_TYPE_TLS.equals(this.getSsltype())) {
                StartTlsResponse tls = (StartTlsResponse) ret.extendedOperation(new StartTlsRequest());
                tls.negotiate();
            }
            connection = ret;
            curConnectionType = this.getSsltype();
            connectionReconnect = false;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return ret;
    }

    private LdapContext creatSimpleConnection() {
        LdapContext ret = null;
        try {
            Hashtable<String, String> env = new Hashtable<String, String>();
            if (!isEmpty(this.getPrincipal())) {
                env.put(Context.SECURITY_PRINCIPAL, this.getPrincipal());
            }
            if (!isEmpty(this.getCredential())) {
                env.put(Context.SECURITY_CREDENTIALS, this.getCredential());
            }
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(Context.PROVIDER_URL, this.getLocation());
            if (!isEmpty(this.getAuthentication())) {
                env.put(Context.SECURITY_AUTHENTICATION, this.getAuthentication());
            }
            if (!isEmpty(this.getProtocol())) {
                env.put(Context.SECURITY_PROTOCOL, this.getProtocol());
            }

            ret = new InitialLdapContext(env, null);
            connection = ret;
            curConnectionType = this.getSsltype();
            connectionReconnect = false;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ret;
    }

    public LdapContext getConnection() {
        if (null != connection && curConnectionType.equals(this.getSsltype()) && !connectionReconnect) {
            return connection;
        }
        closeConnection();
//        CONNECTION_CREATED_COUNT++;
        if (this.getSsltype().equals("") || this.getSsltype().equals(LdapConnection.SSL_TYPE_NONE)||
                !this.getTlssecurity().toUpperCase().equals("YES")) {
            return creatSimpleConnection();
        }
        return createSSLConnection();
//        LdapContext ret = null;
//        try {
//            JOptionPane.showMessageDialog(null, "start!", "Warning", JOptionPane.WARNING_MESSAGE);
//            if (!isEmpty(this.getTruststore())) {
//                System.setProperty("javax.net.ssl.trustStore", this.getTruststore());
//                JOptionPane.showMessageDialog(null, "truststore!", "Warning", JOptionPane.WARNING_MESSAGE);
//            }
//            if (!isEmpty(this.getTruststoretype())) {
//                System.setProperty("javax.net.ssl.trustStoreType", this.getTruststoretype());
//                JOptionPane.showMessageDialog(null, "truststore type!", "Warning", JOptionPane.WARNING_MESSAGE);
//            }
//            if (!isEmpty(this.getTruststorepassword())) {
//                System.setProperty("javax.net.ssl.trustStorePassword", this.getTruststorepassword());
//                JOptionPane.showMessageDialog(null, "truststorePassword!", "Warning", JOptionPane.WARNING_MESSAGE);
//            }
//            if (!isEmpty(this.getKeystore())) {
//                System.setProperty("javax.net.ssl.keyStore", this.getKeystore());
//                JOptionPane.showMessageDialog(null, "keyStore!", "Warning", JOptionPane.WARNING_MESSAGE);
//            }
//            if (!isEmpty(this.getKeystorepassword())) {
//                System.setProperty("javax.net.ssl.keyStorePassword", this.getKeystorepassword());
//                JOptionPane.showMessageDialog(null, "keyStorePassword!", "Warning", JOptionPane.WARNING_MESSAGE);
//            }
//            if (!isEmpty(this.getKeystoreusername())) {
//                System.setProperty("javax.net.ssl.keyStoreUsername", this.getKeystoreusername());
//                JOptionPane.showMessageDialog(null, "keystoreUsername!", "Warning", JOptionPane.WARNING_MESSAGE);
//            }
//            if (!isEmpty(this.getKeystoretype())) {
//                System.setProperty("javax.net.ssl.keyStoreType", this.getKeystoretype());
//                JOptionPane.showMessageDialog(null, "keyStoreType!", "Warning", JOptionPane.WARNING_MESSAGE);
//            }
//
////            System.setProperty("javax.net.ssl.trustStore", "E:\\test\\sslnew\\ldaps.jks");
////
////            System.setProperty("javax.net.ssl.trustStorePassword",
////                    "openldap");
////
////            System.setProperty("javax.net.ssl.keyStore", "E:\\test\\sslnew\\ldaps.jks");
////
////            System.setProperty("javax.net.ssl.keyStorePassword",
////                    "openldap");
//            Hashtable<String, String> env = new Hashtable<String, String>();
//            if (!isEmpty(this.getPrincipal())) {
//                env.put(Context.SECURITY_PRINCIPAL, this.getPrincipal());
//            }
//            if (!isEmpty(this.getCredential())) {
//                env.put(Context.SECURITY_CREDENTIALS, this.getCredential());
//            }
//            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
//            env.put(Context.PROVIDER_URL, this.getLocation());
//            if (!isEmpty(this.getAuthentication())) {
//                env.put(Context.SECURITY_AUTHENTICATION, this.getAuthentication());
//            }
//            if (!isEmpty(this.getProtocol())) {
//                env.put(Context.SECURITY_PROTOCOL, this.getProtocol());
//            }
//            JOptionPane.showMessageDialog(null, "finishi Env!", "Warning", JOptionPane.WARNING_MESSAGE);
//
//            ret = new InitialLdapContext(env, null);
//            JOptionPane.showMessageDialog(null, "connection Created!", "Warning", JOptionPane.WARNING_MESSAGE);
//            if (this.getTlssecurity().toUpperCase().equals("YES") ||
//                    LdapConnection.SSL_TYPE_TLS.equals(this.getSsltype())) {
//                StartTlsResponse tls = (StartTlsResponse) ret.extendedOperation(new StartTlsRequest());
//                tls.negotiate();
//            }
//            connection = ret;
//            curConnectionType = this.getSsltype();
//            connectionReconnect = false;
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//
//        return ret;
    }

    public ArrayList getDNs() {
        ArrayList<String> list = new ArrayList<String>();
        try {
            DirContext ctx = getConnection();
            SearchControls constraints = new SearchControls();
            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
            NamingEnumeration results = ctx.search(this.getDn(), "(ObjectClass=*)", constraints);
            while (null != results && results.hasMore()) {
                SearchResult sr = (SearchResult) results.next();
                list.add(sr.getNameInNamespace());
                sr = null;
            }
        } catch (NamingException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List getObjectNames() throws NamingException {
        List<String> ret = new ArrayList<String>();
        DirContext top = getConnection().getSchema("");
        NamingEnumeration ne = top.list("ClassDefinition");

        while (ne.hasMore()) {
            NameClassPair pair = (NameClassPair) ne.next();
            String clsName = pair.getName();
            ret.add(clsName);
        }

        return ret;
    }

    public LdifObjectClass getObjectClass(String objName) throws NamingException {
        LdifObjectClass objClass = new LdifObjectClass();

        DirContext top = getConnection().getSchema("");
        DirContext schema = (DirContext) top.lookup("ClassDefinition/" + objName);
        Attributes atrs = schema.getAttributes("");
        Attribute name = atrs.get("NAME");
        NamingEnumeration nameValue = name.getAll();
        objClass.setName((String) nameValue.next());
        objClass.setLdapUrl(this.getLocation());

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
            while (mustValue.hasMore()) {
                String a = (String) mustValue.next();
                objClass.addMust(a);
            }
        }
        objClass.setSelected(new ArrayList());
        return objClass;
    }

    public boolean isSingleValue(String attrName) {
        try {
            DirContext schema = getConnection().getSchema("");
            DirContext attrSchema = (DirContext) schema.lookup("AttributeDefinition/" + attrName);
            Attributes a = attrSchema.getAttributes("");
            NamingEnumeration ane = a.getAll();

            while (ane.hasMore()) {
                Attribute attr = (Attribute) ane.next();
                String attrType = attr.getID();
                NamingEnumeration values = attr.getAll();

                while (values.hasMore()) {
                    Object oneVal = values.nextElement();
                    if (oneVal instanceof String) {
                        if (attrType.equals("SINGLE-VALUE")) {
                            return Boolean.parseBoolean(oneVal.toString());
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean validateParam() {
        if(!connectionReconnect){
            return true;
        }
        try {
            if (!isEmpty(this.getTruststore())) {
                System.setProperty("javax.net.ssl.trustStore", this.getTruststore());
            }
            if (!isEmpty(this.getTruststoretype())) {
                System.setProperty("javax.net.ssl.trustStoreType", this.getTruststoretype());
            }
            if (!isEmpty(this.getTruststorepassword())) {
                System.setProperty("javax.net.ssl.trustStorePassword", this.getTruststorepassword());
            }
            if (!isEmpty(this.getKeystore())) {
                System.setProperty("javax.net.ssl.keyStore", this.getKeystore());
            }
            if (!isEmpty(this.getKeystorepassword())) {
                System.setProperty("javax.net.ssl.keyStorePassword", this.getKeystorepassword());
            }
            if (!isEmpty(this.getKeystoreusername())) {
                System.setProperty("javax.net.ssl.keyStoreUsername", this.getKeystoreusername());
            }
            if (!isEmpty(this.getKeystoretype())) {
                System.setProperty("javax.net.ssl.keyStoreType", this.getKeystoretype());
            }

            Hashtable<String, String> env = new Hashtable<String, String>();
            if (!isEmpty(this.getPrincipal())) {
                env.put(Context.SECURITY_PRINCIPAL, this.getPrincipal());
            }
            if (!isEmpty(this.getCredential())) {
                env.put(Context.SECURITY_CREDENTIALS, this.getCredential());
            }
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(Context.PROVIDER_URL, this.getLocation());
            if (!isEmpty(this.getAuthentication())) {
                env.put(Context.SECURITY_AUTHENTICATION, this.getAuthentication());
            }
            if (!isEmpty(this.getProtocol())) {
                env.put(Context.SECURITY_PROTOCOL, this.getProtocol());
            }

            LdapContext ret = new InitialLdapContext(env, null);
            if (this.getTlssecurity().toUpperCase().equals("YES") || LdapConnection.SSL_TYPE_TLS.equals(this.getSsltype())) {
                StartTlsResponse tls = (StartTlsResponse) ret.extendedOperation(new StartTlsRequest());
                tls.negotiate();
                tls.close();
            }
//            CONNECTION_CREATED_COUNT++;
            if (ret != null) {
                ret.close();
//                CONNECTION_CLOSED_COUNT++;
                ret = null;
            }
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    public void closeConnection() {

        try {

            if (connection != null) {

                connection.close();
//                CONNECTION_CLOSED_COUNT++;
                connection = null;

            }


        } catch (Exception ex) {

            ex.printStackTrace();

        }

    }

    public boolean isConnectionReconnect() {
        return connectionReconnect;
    }

    public void setConnectionReconnect(boolean connectionReconnect) {
        this.connectionReconnect = connectionReconnect;
    }

//    public String toString() {
//        String ret = "";
//        ret += "localtion: " + this.getLocation() +
//                "\n principal: " + this.getPrincipal() +
//                "\n credential: " + this.getCredential() +
//                "\n ssltype: " + this.getSsltype() +
//                "\n authentication: " + this.getAuthentication() +
//                "\n protocol: " + this.getProtocol() +
//                "\n truststore: " + this.getTruststore() +
//                "\n truststorepassword: " + this.getTruststorepassword() +
//                "\n truststoretype: " + this.getTruststoretype() +
//                "\n keystorepassword: " + this.getKeystorepassword() +
//                "\n keystoreusername: " + this.getKeystoreusername() +
//                "\n keystore: " + this.getKeystore() +
//                "\n keystoretype: " + this.getKeystoretype() +
//                "\n tlssecurity: " + this.getTlssecurity()+
//                "\n isReconnect"+this.connectionReconnect;
//        
//        return ret;
//    }
}
