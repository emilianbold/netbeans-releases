package org.netbeans.modules.wsdlextensions.ldap;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ZAZ Development
 */
public abstract class LdapConnectionProperties {

    private static final String CLS_NAME = "org.netbeans.modules.wsdlextensions.ldap.LdapConnectionProperties";
    private String location = "";
    private String principal = "";
    private String credential = "";
    private String ssltype = "";
    private String authentication = "";
    private String protocol = "";
    private String truststore = "";
    private String truststorepassword = "";
    private String truststoretype = "";
    private String keystore = "";
    private String keystorepassword = "";
    private String keystoreusername = "";
    private String keystoretype = "";
    private String tlssecurity = "";

    public String getTlssecurity() {
        return tlssecurity;
    }

    public void setTlssecurity(String tlssecurity) {
        this.tlssecurity = tlssecurity;
    }

    public String getSsltype() {
        return ssltype;
    }

    public void setSsltype(String ssltype) {
        this.ssltype = ssltype;
    }

    public String getTruststoretype() {
        return truststoretype;
    }

    public void setTruststoretype(String truststoretype) {
        this.truststoretype = truststoretype;
    }

    public String getAuthentication() {
        return authentication;
    }

    public void setAuthentication(String authentication) {
        this.authentication = authentication;
    }

    public String getCredential() {
        return credential;
    }

    public void setCredential(String credential) {
        this.credential = credential;
    }

    public String getKeystore() {
        return keystore;
    }

    public void setKeystore(String keystore) {
        this.keystore = keystore;
    }

    public String getKeystorepassword() {
        return keystorepassword;
    }

    public void setKeystorepassword(String keystorepassword) {
        this.keystorepassword = keystorepassword;
    }

    public String getKeystoretype() {
        return keystoretype;
    }

    public void setKeystoretype(String keystoretype) {
        this.keystoretype = keystoretype;
    }

    public String getKeystoreusername() {
        return keystoreusername;
    }

    public void setKeystoreusername(String keystoreusername) {
        this.keystoreusername = keystoreusername;
    }

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getTruststore() {
        return truststore;
    }

    public void setTruststore(String truststore) {
        this.truststore = truststore;
    }

    public String getTruststorepassword() {
        return truststorepassword;
    }

    public void setTruststorepassword(String truststorepassword) {
        this.truststorepassword = truststorepassword;
    }

    public String getLocation() {
        return location;
    }

    public void setUrl(String location) {
        this.location = location;
    }

    public LdapConnectionProperties() {
    }

    public String[] getPropertyNames() {
        String[] ret = null;

        try {
            Class cls = Class.forName(CLS_NAME);
            Field[] flds = cls.getDeclaredFields();
            List<String> list = new ArrayList<String>();
            for (int i = 0; i < flds.length; i++) {
                if (flds[i].getName().equals("CLS_NAME")) {
                    continue;
                }
                list.add(flds[i].getName());
            }
            ret = new String[list.size()];
            for (int i = 0; i < list.size(); i++) {
                ret[i] = list.get(i);
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(LdapConnectionProperties.class.getName()).log(Level.SEVERE, null, ex);
        }

        return ret;
    }

    public abstract Object getProperty(String property);
}
