/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.wsdlextensions.sap.model;

import java.io.Serializable;

/**
 *
 * @author jqian
 */
public class SapConnection implements Serializable {

    private String name;
    private String systemID;
    private String applicationServer;
    private String systemNumber;
    private String router;
    private String language;
    private String clientNumber;
    private String userName;
    private String password;

    // for testing purpose only
    public static SapConnection getDefault() {
        SapConnection connection = new SapConnection();
        
        connection.setSystemID("U07");
        connection.setApplicationServer("sapuni");
        connection.setSystemNumber("00");
        connection.setLanguage("EN");
        connection.setClientNumber("800");
        connection.setUserName("PS1");
        connection.setPassword("ONLY4RD");

        return connection;
    }

    public SapConnection() {
    }

    public SapConnection(String name, String systemID, String applicationServer,
            String systemNumber, String router, String language,
            String clientNumber, String userName, String password) {
        this.name = name;
        this.systemID = systemID;
        this.applicationServer = applicationServer;
        this.systemNumber = systemNumber;
        this.router = router;
        this.language = language;
        this.clientNumber = clientNumber;
        this.userName = userName;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSystemID() {
        return systemID;
    }

    public void setSystemID(String systemID) {
        this.systemID = systemID;
    }

    public String getApplicationServer() {
        return applicationServer;
    }

    public void setApplicationServer(String applicationServer) {
        this.applicationServer = applicationServer;
    }

    public String getSystemNumber() {
        return systemNumber;
    }

    public void setSystemNumber(String systemNumber) {
        this.systemNumber = systemNumber;
    }

    public String getRouter() {
        return router;
    }

    public void setRouter(String router) {
        this.router = router;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getClientNumber() {
        return clientNumber;
    }

    public void setClientNumber(String clientNumber) {
        this.clientNumber = clientNumber;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "SAPConnection [" +
                "name: " + name +
                ", systemID: " + systemID +
                ", applicationServer: " + applicationServer +
                ", systemNumber: " + systemNumber +
                ", router: " + router +
                ", language: " + language +
                ", clientNumber: " + clientNumber +
                ", userName: " + userName +
                ", password: " + password +
                "]";
    }

}
