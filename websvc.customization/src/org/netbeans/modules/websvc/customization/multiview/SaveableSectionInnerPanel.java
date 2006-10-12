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

package org.netbeans.modules.websvc.customization.multiview;

import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;

/**
 *
 * @author rico
 */
public abstract class SaveableSectionInnerPanel extends SectionInnerPanel {
    
    /** Creates a new instance of SaveableSectionInnerPanel */
    public SaveableSectionInnerPanel(SectionView view) {
        super(view);
    }
    
    protected boolean isClient(Node node){
        Client client = (Client)node.getLookup().lookup(Client.class);
        if(client != null){
            return true;
        }
        return false;
    }
    
    protected void setModelDirty(WSDLModel model){
        try{
            ModelSource ms = model.getModelSource();
            FileObject fo = (FileObject)ms.getLookup().lookup(FileObject.class);
            DataObject wsdlDO = DataObject.find(fo);
            if(!wsdlDO.isModified()){
                wsdlDO.setModified(true);
            }
        }catch(Exception e){
            ErrorManager.getDefault().notify(e);
        }
    }
    
    /**
     * Perform anything here other than saving the wsdl
     */
    public abstract void save();
    
    /**
     * Does the jaxws model need to be saved?
     */
    public boolean jaxwsIsDirty(){
        return false;
    }
    
    /**
     * Has the wsdl been changed?
     */
    public abstract boolean wsdlIsDirty();
}
