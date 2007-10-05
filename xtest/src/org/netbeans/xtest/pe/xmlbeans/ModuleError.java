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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
