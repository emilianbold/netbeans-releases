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

/*
 * CssCloneableEditor.java
 *
 * Created on January 26, 2005, 9:37 PM
 */

package org.netbeans.modules.css.editor;
import org.netbeans.modules.css.visual.model.CssMetaModel;
import org.netbeans.modules.css.visual.model.CssStyleData;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditor;
import org.openide.util.HelpCtx;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * CSS Cloneable Editor TopComponent
 * @author Winston Prakash
 * @version 1.0
 */
public class CssCloneableEditor extends CloneableEditor{

    DataObject dataObject = null;

    public CssCloneableEditor() {
        super();
    }

    public CssCloneableEditor(CssEditorSupport support) {
        super(support);
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx("projrave_ui_elements_editors_about_css_editor") ; // NOI18N
    }

    public void setDataObject(DataObject dObject){
        if(dataObject == null){
            dataObject = dObject;
            CssMetaModel.setDataObject(dataObject);
        }
    }

    public void componentOpened(){
        super.componentOpened();
        CssMetaModel.setDataObject(dataObject);
//        TopComponent properties = WindowManager.getDefault().findTopComponent("csspreviewTC"); // NOI18N
//        if (properties != null) {
//            properties.open();
//            properties.requestVisible();
//        }
    }

    public void componentActivated(){
        super.componentActivated();
        CssMetaModel.setDataObject(dataObject);
//        TopComponent properties = WindowManager.getDefault().findTopComponent("csspreviewTC"); // NOI18N
//        if (properties != null) {
//            properties.requestVisible();
//        }
    }

    public void componentClosed(){
       super.componentClosed();
       CssMetaModel.removeDataObject(dataObject);
       dataObject = null;
    }

    public void setCssStyleData(CssStyleData cssStyleData){
//        CssSelectorNode cssSelectorNode = new CssSelectorNode(dataObject,cssStyleData);
//        setActivatedNodes(new Node[]{cssSelectorNode});
    }
}
