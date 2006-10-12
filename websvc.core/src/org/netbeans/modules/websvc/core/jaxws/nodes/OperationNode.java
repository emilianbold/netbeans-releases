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
package org.netbeans.modules.websvc.core.jaxws.nodes;

import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlOperation;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.text.ActiveEditorDrop;
import org.openide.util.HelpCtx;
import org.openide.util.Utilities;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/** Node representing WS operation
 *
 * @author mkuchtiak
 */
public class OperationNode extends AbstractNode {
    WsdlOperation operation;
    FileObject srcRoot;
    OperationEditorDrop editorDrop;
    
    public OperationNode(WsdlOperation operation) {
        this(operation, new InstanceContent());
    }
    
    private OperationNode(WsdlOperation operation, InstanceContent content) {
        super(Children.LEAF,new AbstractLookup(content));
        this.operation=operation;
        setName(operation.getName());
        setDisplayName(operation.getName());
        content.add(this);
        content.add(operation);
        editorDrop = new OperationEditorDrop(this);
    }
    
    public Image getIcon(int type){
        return Utilities.loadImage("org/netbeans/modules/websvc/core/webservices/ui/resources/wsoperation.png"); //NOI18N
    }
    
    public Image getOpenedIcon(int type){
        return getIcon( type);
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    // Handle deleting:
    public boolean canDestroy() {
        return false;
    }

    public Transferable clipboardCopy() throws IOException {

        ExTransferable t = ExTransferable.create( super.clipboardCopy() );
        ActiveEditorDropTransferable s = new ActiveEditorDropTransferable(editorDrop);
        t.put(s);

        return t;
    }

    private static class ActiveEditorDropTransferable extends ExTransferable.Single {
        
        private ActiveEditorDrop drop;

        ActiveEditorDropTransferable(ActiveEditorDrop drop) {
            super(ActiveEditorDrop.FLAVOR);
            
            this.drop = drop;
        }
               
        public Object getData () {
            return drop;
        }
        
    }
}
