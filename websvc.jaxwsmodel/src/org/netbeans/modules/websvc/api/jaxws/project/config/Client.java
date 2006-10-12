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

/** Client information for wsimport utility
 */
public final class Client {
    org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Client  client;
    
    Client(org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Client client) {
        this.client=client;
    }
    
    public void setName(java.lang.String value) {
        client.setName(value);
    }

    public java.lang.String getName() {
        return client.getName();
    }

    public void setWsdlUrl(String value) {
        client.setWsdlUrl(value);
    }
    
    public String getWsdlUrl() {
        return client.getWsdlUrl();
    }
    
    public void setLocalWsdlFile(String value) {
        client.setLocalWsdlFile(value);
    }
    
    public String getLocalWsdlFile() {
        return client.getLocalWsdlFile();
    }

    public void setCatalogFile(String value) {
        client.setCatalogFile(value);
    }

    public String getCatalogFile() {
        return client.getCatalogFile();
    }

    public void setPackageName(String value) {
        client.setPackageName(value);
    }

    public String getPackageName() {
        return client.getPackageName();
    }

    public Binding newBinding(){
        return new Binding(client.newBinding());
    }

    public void setBindings(Binding[] bindings) {
        org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Binding[] origBindings =
                new org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Binding[bindings.length];
        for(int i = 0; i < bindings.length; i++){
            origBindings[i] = (org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Binding)bindings[i].getOriginal();
        }
        client.setBinding(origBindings);
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
        org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Binding[] bindings = client.getBinding();
        Binding[] newBindings = new Binding[bindings.length];
        for(int i = 0; i < bindings.length; i++){
            newBindings[i] = new Binding(bindings[i]);
        }
        return newBindings;
    }
    
    
    public void addBinding(Binding binding){
        client.addBinding((org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Binding)binding.getOriginal());
    }
    
    public void removeBinding(Binding binding){
        client.removeBinding((org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Binding)binding.getOriginal());
    }
    
    public String getHandlerBindingFile(){
        return client.getHandlerBindingFile();
    }
    
    public void setHandlerBindingFile(String file){
        client.setHandlerBindingFile(file);
    }
    
    public void setPackageNameForceReplace(boolean value) {
        if (value) client.setPackageNameForceReplace("true");
        else client.setPackageNameForceReplace(null);
    }
    
    public boolean isPackageNameForceReplace() {
        String value = client.getPackageNameForceReplace();
        return "true".equals(value);
    }
}
