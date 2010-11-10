/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.editor.lib2.view;

import java.util.logging.Logger;
import javax.swing.text.JTextComponent;
import org.netbeans.lib.editor.util.ListenerList;


/**
 * View hierarchy associated with a particular text component.
 * <br/>
 * It fully works once the text component's root view is an instance of DocumentView.
 * <br/>
 * Otherwise it attempts to delegate to non-DocumentView implementation where possible.
 * 
 * @author Miloslav Metelka
 */

public final class ViewHierarchy {

    // -J-Dorg.netbeans.modules.editor.lib2.view.EditorViewHierarchy.level=FINE
    private static final Logger LOG = Logger.getLogger(ViewHierarchy.class.getName());
    
    private final JTextComponent component;
    
    private final ListenerList<ViewHierarchyListener> listenerList;

    public static synchronized ViewHierarchy get(JTextComponent component) {
        ViewHierarchy viewHierarchy = (ViewHierarchy) component.getClientProperty(ViewHierarchy.class);
        if (viewHierarchy == null) {
            viewHierarchy = new ViewHierarchy(component);
            component.putClientProperty(ViewHierarchy.class, viewHierarchy);
        }
        return viewHierarchy;
    }
    
    private ViewHierarchy(JTextComponent component) {
        this.component = component;
        this.listenerList = new ListenerList<ViewHierarchyListener>();
    }

    public void addViewHierarchyListener(ViewHierarchyListener l) {
        listenerList.add(l); // synced
    }

    public void removeViewHierarchyListener(ViewHierarchyListener l) {
        listenerList.remove(l); // synced
    }

    void fireViewHierarchyEvent(ViewHierarchyEvent evt) {
        for (ViewHierarchyListener l : listenerList.getListeners()) {
            l.viewHierarchyChanged(evt);
        }
    }

}
