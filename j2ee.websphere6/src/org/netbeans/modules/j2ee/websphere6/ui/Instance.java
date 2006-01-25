/*
 * Instance.java
 *
 * Created on 24 январь 2006 г., 12:53
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.j2ee.websphere6.ui;

/**
 *
 * @author dlm198383
 */

/**
     * A model for the server instance. It contains all the critical properties
     * for the plugin: name, host, port, profile path.
     * 
     * @author Kirill Sorokin, redisigned by Dmitry Lipin
     */

 public class Instance {
        /**
         * Instance's name, it is used a the parameter to the startup/shutdown 
         * scripts
         */
        private String name;
        
        /**
         * Instance's host
         */
        private String host;
        
        /**
         * Instance's port
         */
        private String port;
        
        /**
         * Instance's profile directory
         */
        private String domainPath;
        
        /**
         * Path to the server.xml file
         */
        private String configXmlPath;
        
        /**
         * Creates a new instance of Instance
         * 
         * @param name the instance's name
         * @param host the instance's host
         * @param port the instance's port
         * @param domainPath the instance's profile path
         * @param configXmlPath path to the server.xml file
         */
        public Instance(String name, String host, String port, 
                String domainPath, String configXmlPath) {
            // save the properties
            this.name = name;
            this.host = host;
            this.port = port;
            this.domainPath = domainPath;
            this.configXmlPath = configXmlPath;
        }
        
        /**
         * Getter for the instance's name
         * 
         * @return the instance's name
         */
        public String getName() {
            return this.name;
        }
        
        /** 
         * Setter for the instance's name
         * 
         * @param the new instance's name
         */
        public void setName(String name) {
            this.name = name;
        }
        
        /**
         * Getter for the instance's host
         * 
         * @return the instance's host
         */
        public String getHost() {
            return this.host;
        }
        
        /** 
         * Setter for the instance's host
         * 
         * @param the new instance's host
         */
        public void setHost(String host) {
            this.host = host;
        }
        
        /**
         * Getter for the instance's port
         * 
         * @return the instance's port
         */
        public String getPort() {
            return this.port;
        }
        
        /** 
         * Setter for the instance's port
         * 
         * @param the new instance's port
         */
        public void setPort(String port) {
            this.port = port;
        }
        
        /**
         * Getter for the instance's profile path
         * 
         * @return the instance's profile path
         */
        public String getDomainPath() {
            return this.domainPath;
        }
        
        /** 
         * Setter for the instance's profile path
         * 
         * @param the new instance's profile path
         */
        public void setDomainPath(String domainPath) {
            this.domainPath = domainPath;
        }
        
        /**
         * Getter for the path to server's config xml file
         * 
         * @return the server's config xml file
         */
        public String getConfigXmlPath() {
            return this.configXmlPath;
        }
        
        /** 
         * Setter for the server's config xml file
         * 
         * @param the new server's config xml file
         */
        public void setConfigXmlPath(String configXmlPath) {
            this.configXmlPath = configXmlPath;
        }
        
        /**
         * An overriden version of the Object's toString() so that the 
         * instance is displayed properly in the combobox
         */
        public String toString() {
            return name + " [" + host + ":" + port + "]"; // NOI18N
        }
    }
 
 