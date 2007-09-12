/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
