/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */



/*
 * Attribute.java
 *
 * Created on November 15, 2002, 2:24 PM
 */

package org.netbeans.xtest.pe.xmlbeans;

/**
 *
 * @author  mb115822
 */
public class ModuleError extends XMLBean {
    
    public String xmlat_module;
    public String xmlat_testtype;    
    public String xmlat_logfile;
    
    /** Creates a new instance */
    public ModuleError() {
    }

    /** Creates a new instance */
    public ModuleError(String module, String testtype, String logfile, String error) {
        xmlat_module = module;
        xmlat_testtype = testtype;
        xmlat_logfile = logfile;
        xml_cdata = error;
    }
    
    /** Getter for property moduleErrors.
     * @return Value of property moduleErrors.
     */
    public String getError() {
        return xml_cdata;
    }
    
    /** Setter for property moduleErrors.
     * @param moduleErrors New value of property moduleErrors.
     */
    public void setError(String moduleErrors) {
        xml_cdata = moduleErrors;
    }    
        
    /** Getter for property module.
     * @return Value of property module.
     *
     */
    public String getModule() {
        return this.xmlat_module;
    }
    
    /** Setter for property module.
     * @param module New value of property module.
     *
     */
    public void setModule(String module) {
        this.xmlat_module = module;
    }
    
    /** Getter for property testtype.
     * @return Value of property testtype.
     *
     */
    public String getTesttype() {
        return this.xmlat_testtype;
    }
    
    /** Setter for property testtype.
     * @param testtype New value of property testtype.
     *
     */
    public void setTesttype(String testtype) {
        this.xmlat_testtype = testtype;
    }
    
    /** Getter for property logfile.
     * @return Value of property logfile.
     *
     */
    public String getLogfile() {
        return this.xmlat_logfile;
    }
    
    /** Setter for property logfile.
     * @param logfile New value of property logfile.
     *
     */
    public void setLogfile(String logfile) {
        this.xmlat_logfile = logfile;
    }
    
}
