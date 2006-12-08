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

package org.netbeans.modules.websvc.core.dev.wizard;

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.core.HandlerCreator;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Rico, Milan Kuchtiak
 */
public class JaxWsHandlerCreator implements HandlerCreator {
    private Project project;
    private WizardDescriptor wiz;
    /**
     * Creates a new instance of WebServiceClientCreator
     */
    public JaxWsHandlerCreator(Project project, WizardDescriptor wiz) {
        this.project = project;
        this.wiz = wiz;
    }
        
    public void createMessageHandler() throws IOException {
        String handlerName = Templates.getTargetName(wiz);
        FileObject pkg = Templates.getTargetFolder(wiz);
        DataFolder df = DataFolder.findFolder(pkg);
        FileObject template = Templates.getTemplate(wiz);
        DataObject dTemplate = DataObject.find(template);
        DataObject dobj = dTemplate.createFromTemplate(df, handlerName);

        //open in the editor
        final EditorCookie ec = (EditorCookie) dobj.getCookie(EditorCookie.class);
        RequestProcessor.getDefault().post(new Runnable(){
            public void run(){
                ec.open();
            }
        }, 1000);
    }
    
    public void createLogicalHandler() throws IOException {
        String handlerName = Templates.getTargetName(wiz);
        FileObject pkg = Templates.getTargetFolder(wiz);
        DataFolder df = DataFolder.findFolder(pkg);
        FileObject template = Templates.getTemplate(wiz);
        DataObject dTemplate = DataObject.find(template);
        DataObject dobj = dTemplate.createFromTemplate(df, handlerName);

        //open in the editor
        final EditorCookie ec = (EditorCookie) dobj.getCookie(EditorCookie.class);
        RequestProcessor.getDefault().post(new Runnable(){
            public void run(){
                ec.open();
            }
        }, 1000);
    }  
}
