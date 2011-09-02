/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.e2e.api.wsdl.wsdl2java;

import java.net.URL;
import java.util.List;

/**
 *
 * @author Michal Skvor
 */
public interface WSDL2Java {
        
    /**
     *  Processes wsdl and writes output files 
     */
    public boolean generate();    
    
    /**
     * Returns validation result from validation
     */
    public List<ValidationResult> validate();
    
    /**
     * Configuration for the WSDL2Java
     */
    public static class Configuration {
        
        public static final short TYPE_JAVA_BEANS   = 1;
        public static final short TYPE_STRUCTURES   = 2;
        
        private String wsdlFileName;
        private String outputDirectory;
        private boolean overwriteExisting;
        private String packageName;
        private short generateType;
        private URL wsdlUrl;
        
        private boolean generateDataBinding;
                        
        public Configuration() {
            generateType = TYPE_JAVA_BEANS;

            packageName = ""; // NOI18N
            overwriteExisting = true;
            generateDataBinding = false;
        } 

        /**
         * Sets the wsdl file
         *
         * @param file path to file
         */
        public void setWSDLFileName(String file) {
            this.wsdlFileName = file;
        }

        public String getWSDLFileName() {
            return wsdlFileName;
        }

        public void setOutputDirectory(String directoryName) {
            this.outputDirectory = directoryName;
        }

        public String getOutputDirectory() {
            return outputDirectory;
        }

        public void setOverwriteExistingFiles(boolean overwrite) {
            this.overwriteExisting = overwrite;
        }

        public boolean getOverwriteExistingFiles() {
            return overwriteExisting;
        }

        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }

        public String getPackageName() {
            return packageName;
        }

        public void setGenerateType(short type) {
            this.generateType = type;
        }

        public short getGenerateType() {
            return generateType;
        }

        public void setGenerateDataBinding(boolean value) {
            this.generateDataBinding = value;
        }

        public boolean getGenerateDataBinding() {
            return generateDataBinding;
        }

        public URL getOriginalWSDLUrl() {
            return wsdlUrl;
        }

        public void setOriginalWSDLUrl(URL url) {
            wsdlUrl = url;
        }
    }

    /**
     * Result of the validation
     */
    public static final class ValidationResult {

        public enum ErrorLevel {

            FATAL, WARNING, NOTIFY
        };
        private ErrorLevel errorLevel;
        private String message;

        public ValidationResult(ErrorLevel errorLevel, String message) {
            this.errorLevel = errorLevel;
            this.message = message;
        }

        public ErrorLevel getErrorLevel() {
            return errorLevel;
        }

        public String getMessage() {
            return message;
        }
    }

}
