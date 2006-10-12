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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.api.jaxws.project.config;

/** Service information for wsimport utility
 */
public final class Service {
    org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Service  service;
    
    Service(org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Service  service) {
        this.service=service;
    }
    
    public void setName(java.lang.String value) {
        service.setName(value);
    }

    public java.lang.String getName() {
        return service.getName();
    }

    public void setImplementationClass(String value) {
        service.setImplementationClass(value);
    }

    public String getImplementationClass() {
        return service.getImplementationClass();
    }

    public void setWsdlUrl(String value) {
        service.setWsdlUrl(value);
    }

    public String getWsdlUrl() {
        return service.getWsdlUrl();
    }
    
    public void setPortName(String value) {
        service.setPortName(value);
    }

    public String getPortName() {
        return service.getPortName();
    }
    
    public void setServiceName(String value) {
        service.setServiceName(value);
    }

    public String getServiceName() {
        return service.getServiceName();
    }
    
    public void setLocalWsdlFile(String value) {
        service.setLocalWsdlFile(value);
    }

    public String getLocalWsdlFile() {
        return service.getLocalWsdlFile();
    }
    
    public void setPackageName(String value) {
        service.setPackageName(value);
    }

    public String getPackageName() {
        return service.getPackageName();
    }
    
     public Binding newBinding(){
        return new Binding(service.newBinding());
    }

    public void setBindings(Binding[] bindings) {
        org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Binding[] origBindings =
                new org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Binding[bindings.length];
        for(int i = 0; i < bindings.length; i++){
            origBindings[i] = (org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Binding)bindings[i].getOriginal();
        }
        service.setBinding(origBindings);
    }
    
    public Binding getBindingByFileName(String fileName){
        Binding[] bindings = getBindings();
        for (int i = 0 ; i < bindings.length; i++){
            Binding binding = bindings[i];
            if(binding.getFileName().equals(fileName)){
                return binding;
            }
        }
        return null;
    }

    public Binding[] getBindings() {
        org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Binding[] bindings = service.getBinding();
        Binding[] newBindings = new Binding[bindings.length];
        for(int i = 0; i < bindings.length; i++){
            newBindings[i] = new Binding(bindings[i]);
        }
        return newBindings;
    }
    
    
    public void addBinding(Binding binding){
        service.addBinding((org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Binding)binding.getOriginal());
    }
    
    public void removeBinding(Binding binding){
        service.removeBinding((org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Binding)binding.getOriginal());
    }

    public void setCatalogFile(String value) {
        service.setCatalogFile(value);
    }

    public String getCatalogFile() {
        return service.getCatalogFile();
    }
    
    public String getHandlerBindingFile(){
        return service.getHandlerBindingFile();
    }
    
    public void setHandlerBindingFile(String file){
        service.setHandlerBindingFile(file);
    }
    
    public void setPackageNameForceReplace(boolean value) {
        if (value) service.setPackageNameForceReplace("true");
        else service.setPackageNameForceReplace(null);
    }
    
    public boolean isPackageNameForceReplace() {
        String value = service.getPackageNameForceReplace();
        return "true".equals(value);
    }
   
}
