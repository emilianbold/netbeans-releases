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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.websvc.rest.component.palette;

import java.util.List;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.websvc.rest.codegen.WADLResourceCodeGenerator;
import org.netbeans.modules.websvc.rest.codegen.WSDLResourceCodeGenerator;
import org.netbeans.modules.websvc.rest.component.palette.RestComponentData.Method;
import org.netbeans.modules.websvc.rest.component.palette.RestComponentData.Service;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.ActiveEditorDrop;
import org.openide.util.Lookup;

/**
 *
 * @author Owner
 */
public class RestComponentHandler implements ActiveEditorDrop {
    
    public RestComponentHandler() {
    }
    
    public boolean handleTransfer(JTextComponent targetComponent) {
        if (targetComponent == null)
            return false;
        
        Lookup pItem = RestPaletteFactory.getCurrentPaletteItem();
        Node n = pItem.lookup(Node.class);
        
        RestComponentData data = (RestComponentData) n.getValue("RestComponentData");
        String componentName = data.getName();
        String path = data.getCategoryPath();
        Service service = data.getService();
        String serviceName = service.getName();
        List<Method> mList = service.getMethods();
        String type = "";
        String typeUrl = "";
        String url = "";
        if(!mList.isEmpty()) {
            Method m = mList.get(0);
            type = m.getType();
            url = m.getUrl();
        }
        
        String message = "Generating REST Service code for\n"+
                "Component: "+componentName+"\n"+
                "Path: "+path+"\n"+
                "Type: "+type+"\n"+
                type+" URL: "+url+"\n";
        NotifyDescriptor descriptor = new NotifyDescriptor.Message(message);
        DialogDisplayer.getDefault().notify(descriptor);
        
        DataObject d = NbEditorUtilities.getDataObject(targetComponent.getDocument());
        FileObject targetFolder = d.getPrimaryFile().getParent();
        if(RestComponentData.isWSDL(type)) {
            FileObject targetFO = getTargetFile(targetComponent);
            WSDLResourceCodeGenerator codegen = new WSDLResourceCodeGenerator(targetFO, data);
            codegen.generate();
        } else if(RestComponentData.isWADL(type)) {
            FileObject targetFO = getTargetFile(targetComponent);
            WADLResourceCodeGenerator codegen = new WADLResourceCodeGenerator(targetFO, data);
            codegen.generate();
        }
        return true;
    }
    
    public static FileObject getTargetFile(JTextComponent targetComponent) {
        if (targetComponent == null)
            return null;
        
        DataObject d = NbEditorUtilities.getDataObject(targetComponent.getDocument());
        if (d == null)
            return null;
        
        EditorCookie ec = (EditorCookie) d.getCookie(EditorCookie.class);
        if(ec == null || ec.getOpenedPanes() == null)
            return null;
        
        return d.getPrimaryFile();
    }
}
