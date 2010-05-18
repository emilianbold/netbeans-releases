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


package org.netbeans.modules.edm.editor.widgets.property.editor;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.util.logging.Logger;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.netbeans.modules.edm.model.SQLDefinition;
import org.netbeans.modules.edm.editor.dataobject.MashupDataObject;
import org.netbeans.modules.edm.editor.utils.ResponseTypes;
import org.netbeans.modules.edm.editor.widgets.property.PropertyNode;
import org.openide.windows.WindowManager;
import org.openide.nodes.Node;

public class ResponseTypeMenuEditor extends PropertyEditorSupport implements PropertyEditor {

    private static Logger logger = Logger.getLogger(ResponseTypeMenuEditor.class.getName());
    private PropertyNode node;

    private MashupDataObject mObj;

    private PropertyEnv env;

    public String respType;

    public static String type = "";
    public Node[] nodes;

    public ResponseTypeMenuEditor() {
        initialize();
    }

    @Override
    public String[] getTags() {
        //TBD : i18n
        String[] tags = {ResponseTypes.WEBROWSET, ResponseTypes.RELATIONALMAP, ResponseTypes.JSON};
        return tags;
    }

    @Override
    public Object getValue() {

        // respType="";
        if (node == null) {
            initialize();
        }
        String responseType = node.getMashupDataObject().getModel().getEDMDefinition().getSQLDefinition().getResponseType();
        logger.fine("--------------------gotprop response code value" + responseType);
        return responseType;
    }

    @Override
    public String getAsText() {
        return (String) getValue();
    }

    @Override
    public void setValue(Object object) {
//        node.getMashupDataObject().getModel().getEDMDefinition().getSQLDefinition().setResponseType(object.toString());
//        logger.fine("in webrowset setting the values after" + object.toString());
//        mObj.getMashupDataEditorSupport().synchDocument();
    }

    @Override
    public void setAsText(String text) {
//        if (node == null) {
//            initialize();
//        }
//        setValue(text);
    }

    private void initialize() {
        // Node[] nodes = WindowManager.getDefault().getRegistry().getActivatedNodes();
        nodes = WindowManager.getDefault().getRegistry().getActivatedNodes();
        for (Node node : nodes) {
            if (node instanceof PropertyNode) {
                this.mObj = ((PropertyNode) node).getMashupDataObject();
                this.node = (PropertyNode) node;
                break;
            }
        }
    }

    public void attachEnv(PropertyEnv env) {
        this.env = env;
    }
    
}
