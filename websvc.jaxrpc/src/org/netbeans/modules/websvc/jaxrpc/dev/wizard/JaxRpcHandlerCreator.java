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

package org.netbeans.modules.websvc.jaxrpc.dev.wizard;

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.core.HandlerCreator;
import org.netbeans.modules.websvc.jaxrpc.dev.dd.gen.wscreation.Bean;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Milan Kuchtiak
 */
public class JaxRpcHandlerCreator implements HandlerCreator {
    
    private Project project;
    private WizardDescriptor wiz;
    private String wsName;
    
    /** Creates a new instance of JaxRpcServiceCreator */
    public JaxRpcHandlerCreator(Project project, WizardDescriptor wiz) {
        this.project = project;
        this.wiz = wiz;
    }
    
    public void createMessageHandler() throws IOException {
        System.out.println("create JaxRpc MessageHandler");
            WSGenerationUtil wsgenUtil = new WSGenerationUtil();
            final String HANDLER_TEMPLATE = WSGenerationUtil.TEMPLATE_BASE + "MessageHandler.xml"; //NOI18N
            FileObject pkg = Templates.getTargetFolder(wiz);
            String handlerName = Templates.getTargetName(wiz);
            String pkgName = wsgenUtil.getSelectedPackageName(pkg, project);
            Bean b = wsgenUtil.getDefaultBean();
            b.setCommentDataWsName(handlerName);
            b.setClassname(true);
            b.setClassnameName(handlerName);
            if(pkgName != null) {
                b.setClassnamePackage(pkgName);
            }
           wsgenUtil.generateClass(HANDLER_TEMPLATE, b, pkg, true);
        
    }

    public void createLogicalHandler() {
        System.out.println("create Logical Handler - not supported");
    }
}
