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

package org.netbeans.modules.soa.mapper.common.basicmapper.literal;

import java.awt.Component;
import java.awt.Window;

import org.openide.windows.WindowManager;

import org.netbeans.modules.soa.mapper.basicmapper.literal.BasicBooleanLiteralEditor;
import org.netbeans.modules.soa.mapper.basicmapper.literal.BasicCharacterLiteralEditor;
import org.netbeans.modules.soa.mapper.basicmapper.literal.BasicStringLiteralEditor;
import org.netbeans.modules.soa.mapper.basicmapper.literal.JavaNumericLiteralEditor;
import org.netbeans.modules.soa.mapper.basicmapper.literal.StrictNumericLiteralEditor;
import org.netbeans.modules.soa.mapper.common.basicmapper.IBasicMapper;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IFieldNode;


/**
 * Handles creation of each type of literal editor class.
 * The editor is self-manageable, so what little interaction there
 * exists with the editors is placed into IBasicLiteralEditor.
 *
 * @author Josh Sandusky
 */
public class BasicLiteralEditorFactory {
    
    public static ILiteralEditor createBasicBooleanEditor(
            IBasicMapper basicMapper, 
            IFieldNode field,
            ILiteralUpdater updater) {
        Window owner = getWindowOwner(basicMapper);
        return new BasicBooleanLiteralEditor(owner, basicMapper, field, updater);
    }
    
    public static ILiteralEditor createBasicCharacterEditor(
            IBasicMapper basicMapper, 
            IFieldNode field,
            ILiteralUpdater updater) {
        Window owner = getWindowOwner(basicMapper);
        return new BasicCharacterLiteralEditor(owner, basicMapper, field, updater);
    }

    public static ILiteralEditor createBasicStringEditor(
            IBasicMapper basicMapper, 
            IFieldNode field,
            ILiteralUpdater updater) {
        Window owner = getWindowOwner(basicMapper);
        return new BasicStringLiteralEditor(owner, basicMapper, field, updater);
    }

    public static ILiteralEditor createJavaNumericEditor(
            IBasicMapper basicMapper, 
            IFieldNode field,
            ILiteralUpdater updater) {
        Window owner = getWindowOwner(basicMapper);
        return new JavaNumericLiteralEditor(owner, basicMapper, field, updater);
    }

    public static ILiteralEditor createStrictNumericEditor(
            IBasicMapper basicMapper, 
            IFieldNode field,
            ILiteralUpdater updater) {
        Window owner = getWindowOwner(basicMapper);
        return new StrictNumericLiteralEditor(owner, basicMapper, field, updater);
    }
    
    private static Window getWindowOwner(IBasicMapper basicMapper) {
        Window window = null;
        Component parent = 
            basicMapper.getMapperViewManager().getCanvasView().getCanvas().getUIComponent();
        while (
                parent != null &&
                !(parent instanceof Window)) {
            parent = parent.getParent();
        }
        if (parent == null) {
            parent = WindowManager.getDefault().getMainWindow();
        }
        return (Window) parent;
    }
}
