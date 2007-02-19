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

package org.netbeans.modules.editor.impl;

import java.util.Collections;
import java.util.List;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.spi.editor.mimelookup.Class2LayerFolder;
import org.netbeans.spi.editor.mimelookup.InstanceProvider;

/**
 *
 * @author Vita Stejskal
 */
public final class PopupMenuActionsProvider extends ActionsList implements Class2LayerFolder, InstanceProvider {

    private static final String POPUP_MENU_ACTIONS_FOLDER_NAME = "Popup"; //NOI18N
    
    public static List getPopupMenuInstances(String mimeType) {
        MimePath mimePath = MimePath.parse(mimeType);
        PopupMenuActionsProvider provider = (PopupMenuActionsProvider) MimeLookup.getLookup(mimePath).lookup(PopupMenuActionsProvider.class);
        return provider == null ? Collections.emptyList() : provider.getAllInstances();
    }
    
    public PopupMenuActionsProvider() {
        super(null);
    }

    private PopupMenuActionsProvider(List keys) {
        super(keys);
    }
    
    public Class getClazz(){
        return PopupMenuActionsProvider.class;
    }

    public String getLayerFolderName(){
        return POPUP_MENU_ACTIONS_FOLDER_NAME;
    }

    public InstanceProvider getInstanceProvider() {
        return new PopupMenuActionsProvider();
    }
    
    public Object createInstance(List fileObjectList) {
        return new PopupMenuActionsProvider(fileObjectList);
    }
}
