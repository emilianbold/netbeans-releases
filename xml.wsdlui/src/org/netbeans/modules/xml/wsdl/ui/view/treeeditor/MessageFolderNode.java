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

package org.netbeans.modules.xml.wsdl.ui.view.treeeditor;

import java.util.Collection;

import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.MessageNewType;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.datatransfer.NewType;


/**
 *
 * @author Ritesh Adval
 *
 * 
 */
public class MessageFolderNode extends FolderNode {

    private Definitions mDef = null;
    
    public MessageFolderNode(Definitions element) {
        super(new MessageFolderChildren(element), element, Message.class);
        BADGE_ICON = Utilities.loadImage
        ("org/netbeans/modules/xml/wsdl/ui/view/resources/folderBadge_envelope.png");
        mDef = element;
        this.setDisplayName(NbBundle.getMessage(MessageFolderNode.class, 
                   "MESSAGE_FOLDER_NODE_NAME"));
    }


    @Override
    public final NewType[] getNewTypes()
    {
        if (isEditable()) {
            return new NewType[] {new MessageNewType(mDef)};
        }
        return new NewType[] {};
    }
    
    public static final class MessageFolderChildren extends GenericWSDLComponentChildren<Definitions> {
        public MessageFolderChildren(Definitions definitions) {
            super(definitions);
        }

        @Override
        public final Collection<Message> getKeys() {
            Definitions def = getWSDLComponent();
            return def.getMessages();
        }
    }

    @Override
    public Class getType() {
        return Message.class;
    }

}
