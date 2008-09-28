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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.vmd.io.editor;

import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.modules.vmd.api.io.DataEditorView;
import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.netbeans.modules.vmd.api.io.ProjectTypeInfo;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

import java.awt.*;
import java.io.Serializable;

/**
 * @author David Kaspar
 */
public class EditorViewDescription implements MultiViewDescription, Serializable {

    private static final long serialVersionUID = 3317521754395749572L;

    private DataObjectContext context;
    private DataEditorView view;

    public EditorViewDescription (DataObjectContext context, DataEditorView view) {
        this.context = context;
        this.view = view;
    }

    public int getPersistenceType () {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    public String getDisplayName () {
        return view.getDisplayName ();
    }

    public Image getIcon () {
        ProjectTypeInfo projectTypeInfo = ProjectTypeInfo.getProjectTypeInfoFor (context.getProjectType ());
        return projectTypeInfo != null ? ImageUtilities.loadImage (projectTypeInfo.getIconResource ()) : null;
    }

    public HelpCtx getHelpCtx () {
        return view.getHelpCtx ();
    }

    public String preferredID () {
        return view.preferredID ();
    }

    public MultiViewElement createElement () {
        return new EditorViewElement (context, view);
    }

    public DataEditorView getView () {
        return view;
    }

}
