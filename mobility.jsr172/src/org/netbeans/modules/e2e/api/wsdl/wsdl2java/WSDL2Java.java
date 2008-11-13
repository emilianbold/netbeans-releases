/*
 * WSDL2Java.java
 *
 * Created on October 30, 2006, 10:33 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
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

            packageName = "";
            overwriteExisting = true;
            generateDataBinding = false;
        } 
        /**
         * Sets the wsdl file
         *
         * @param file path to file
         */
    
        public void setWSDLFileName( String file ) {
            this.wsdlFileName = file;
        }
        
        public String getWSDLFileName() {
            return wsdlFileName;
        }

        public void setOutputDirectory( String directoryName ) {
            this.outputDirectory = directoryName;
        }

        public String getOutputDirectory() {
            return outputDirectory;
        }

        public void setOverwriteExistingFiles( boolean overwrite ) {
            this.overwriteExisting = overwrite;
        }

        public boolean getOverwriteExistingFiles() {
            return overwriteExisting;
        }

        public void setPackageName( String packageName ) {
            this.packageName = packageName;
        }

        public String getPackageName() {
            return packageName;
        }

        public void setGenerateType( short type ) {
            this.generateType = type;
        }

        public short getGenerateType() {
            return generateType;
        }
        
        public void setGenerateDataBinding( boolean value ) {
            this.generateDataBinding = value;
        }
        
        public boolean getGenerateDataBinding() {
            return generateDataBinding;
        }
        
        public URL getOriginalWSDLUrl(){
            return wsdlUrl;
        }
        
        public void setOriginalWSDLUrl( URL url ){
            wsdlUrl = url;
        }
    }
    
    /**
     * Result of the validation
     */
    public static final class ValidationResult {
        
        public enum ErrorLevel { FATAL, WARNING, NOTIFY };
        
        private ErrorLevel errorLevel;
        private String message;
        
        public ValidationResult( ErrorLevel errorLevel, String message ) {
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
