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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.soa.pojo.resources;

import javax.swing.text.JTextComponent;

import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.modules.soa.pojo.wizards.POJOBindingConsumerPalleteWizardIterator;
import org.netbeans.modules.xml.wsdl.ui.wsdl.util.BindingUtils;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.TemplateWizard;
import org.openide.text.ActiveEditorDrop;
import org.openide.util.Exceptions;


/**
 * Drop the POJO Consumer pallete in Java Editor.
 * @author Sreenivasan Genipudi
 */
public class POJOBindingConsumerDrop extends DestinationPaletteDrop implements ActiveEditorDrop {
    /**
     * Constructor
     */
    public POJOBindingConsumerDrop() {
        destType = DestinationType.BINDINGCONSUMER;
    }

    /**
    * A method called from the drop target that supports the artificial DataFlavor.
    * @param target a Component where drop operation occured
    * @return true if implementor allowed a drop operation into the targetComponent
    */
    public boolean handleTransfer(JTextComponent target) {
        POJOBindingConsumerPalleteWizardIterator powiz = null;//new POJOBindingConsumerPalleteWizardIterator();
        TemplateWizard td= new TemplateWizard();
        
        FileObject repoFs = FileUtil.getConfigRoot();
        DataObject dObj = null;
        FileObject foForBC = repoFs.getFileObject("POJOConsumer/ESB/pojoconsumerbinding.wsdl"); //NOI18N

            try {
                Object projectObj =  Templates.getProject(td);
                if ( projectObj == null) {
                    JavaSource javaSource = JavaSource.forDocument(target.getDocument());
                    FileObject fo = javaSource.getFileObjects().iterator().next();
                    projectObj = FileOwnerQuery.getOwner(fo);
                }
                td.putProperty("project", projectObj);//NOI18N
                if ( Templates.getProject(td) == null) {
                    td.putProperty(BindingUtils.PROJECT_INSTANCE, projectObj);
                }
                dObj = DataObject.find(foForBC);
                td.setTemplate(dObj);
                    

            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        
        powiz = (POJOBindingConsumerPalleteWizardIterator) td.getIterator(dObj);
        this.mWizDesc = td;
        this.wizTempItr = powiz;
        destinationAction(target);
        return true;
    }

}
