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
 *
 * JSFTestViewFactory.java
 *
 * Created on February 7, 2007, 6:20 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.web.jsf.navigation;

//import org.netbeans.modules.jsfmultiviewtest.*;
import org.netbeans.modules.web.jsf.navigation.JSFPageFlowMultiviewDescriptor;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.modules.web.jsf.api.editor.JSFConfigEditorContext;
import org.netbeans.modules.web.jsf.api.editor.JSFConfigEditorViewFactory;

/**
 *
 * @author Joelle Lam
 */
public class JSFPageFlowViewFactory implements  JSFConfigEditorViewFactory {
    
    /**
     * Creates teh MultiViewDescriptor
     * @param facesContext 
     * @return MultiViewDescription
     */
    public MultiViewDescription createMultiViewDescriptor(JSFConfigEditorContext facesContext){
        return new JSFPageFlowMultiviewDescriptor(facesContext);
    }
    
}
