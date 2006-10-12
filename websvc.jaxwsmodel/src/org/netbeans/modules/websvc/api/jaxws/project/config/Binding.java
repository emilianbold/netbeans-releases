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
/*
 * HandlerChain.java
 *
 * Created on March 19, 2006, 9:02 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.api.jaxws.project.config;

/**
 *
 * @author mkuchtiak
 */
public class Binding {
    private org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Binding binding;
    /** Creates a new instance of HandlerChain */
    public Binding(org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Binding binding) {
        this.binding=binding;
    }
    
    Object getOriginal() {
        return binding;
    }
    
    public String getFileName() {
        return binding.getFileName().trim();
    }
    
    public String getOriginalFileUrl(){
        return binding.getOriginalFileUrl().trim();
    }
   
    public void setFileName(String value) {
        binding.setFileName(value.trim());
    }
    
    public void setOriginalFileUrl(String value){
        binding.setOriginalFileUrl(value.trim());
    }
}
