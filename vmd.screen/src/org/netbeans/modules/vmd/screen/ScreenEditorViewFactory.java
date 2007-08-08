/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.vmd.screen;

import org.netbeans.modules.vmd.api.io.DataEditorView;
import org.netbeans.modules.vmd.api.io.DataEditorViewFactory;
import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.netbeans.modules.vmd.api.io.ProjectTypeInfo;
import org.netbeans.modules.vmd.api.screen.ScreenSupport;

/**
 * @author breh
 *
 */
public class ScreenEditorViewFactory implements DataEditorViewFactory {

    public DataEditorView createEditorView (DataObjectContext context) {
        ProjectTypeInfo info = ProjectTypeInfo.getProjectTypeInfoFor (context.getProjectType ());
        if (info.getTags ().contains (ScreenSupport.PROJECT_TYPE_TAG_SCREEN))
            return new ScreenEditorView (context);
        return null;
    }

}
