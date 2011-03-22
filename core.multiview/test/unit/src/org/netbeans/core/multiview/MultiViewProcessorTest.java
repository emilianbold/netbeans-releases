/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.core.multiview;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.junit.NbTestCase;
import org.openide.awt.UndoRedo;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.TopComponent;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class MultiViewProcessorTest extends NbTestCase {
    
    public MultiViewProcessorTest(String n) {
        super(n);
    }

    public void testMultiViewsCreate() {
        TopComponent mvc = MultiViews.createMultiView("text/figaro", Lookup.EMPTY);
        assertNotNull("MultiViewComponent created", mvc);
        mvc.open();
        mvc.requestActive();
        
        MultiViewHandler handler = MultiViews.findMultiViewHandler(mvc);
        assertNotNull("Handler found", handler);
        MultiViewPerspective[] arr = handler.getPerspectives();
        assertEquals("One perspetive found", 1, arr.length);
        assertEquals("Figaro", arr[0].getDisplayName());
    }

    public void testMultiViewsContextCreate() {
        InstanceContent ic = new InstanceContent();
        Lookup lookup = new AbstractLookup(ic);
        
        TopComponent mvc = MultiViews.createMultiView("text/context", lookup);
        assertNotNull("MultiViewComponent created", mvc);
        MultiViewHandler handler = MultiViews.findMultiViewHandler(mvc);
        assertNotNull("Handler found", handler);
        MultiViewPerspective[] arr = handler.getPerspectives();
        assertEquals("One perspetive found", 1, arr.length);
        assertEquals("Contextual", arr[0].getDisplayName());
        
        mvc.open();
        mvc.requestActive();
        mvc.requestVisible();
        
        handler.requestActive(arr[0]);
        assertNull("No integer now", mvc.getLookup().lookup(Integer.class));
        ic.add(1);
        assertEquals("1 now", Integer.valueOf(1), mvc.getLookup().lookup(Integer.class));
    }

    @MultiViewElement.Registration(
        displayName="Figaro",
        iconBase="none",
        mimeType="text/figaro",
        persistenceType=TopComponent.PERSISTENCE_NEVER,
        preferredID="figaro"
    )
    public static class MVE extends JPanel implements MultiViewElement {
        public MVE() {
        }
        
        @Override
        public JComponent getVisualRepresentation() {
            return this;
        }

        @Override
        public JComponent getToolbarRepresentation() {
            return null;
        }

        @Override
        public Action[] getActions() {
            return new Action[0];
        }

        @Override
        public Lookup getLookup() {
            return Lookup.EMPTY;
        }

        @Override
        public void componentOpened() {
        }

        @Override
        public void componentClosed() {
        }

        @Override
        public void componentShowing() {
        }

        @Override
        public void componentHidden() {
        }

        @Override
        public void componentActivated() {
        }

        @Override
        public void componentDeactivated() {
        }

        @Override
        public UndoRedo getUndoRedo() {
            return UndoRedo.NONE;
        }

        @Override
        public void setMultiViewCallback(MultiViewElementCallback callback) {
        }

        @Override
        public CloseOperationState canCloseElement() {
            return CloseOperationState.STATE_OK;
        }
    } // end of MVE
    
    @MultiViewElement.Registration(
        displayName="Contextual",
        iconBase="none",
        mimeType="text/context",
        persistenceType=TopComponent.PERSISTENCE_NEVER,
        preferredID="context"
    )
    public static class CntxMVE extends MVE {
        private Lookup context;
        public CntxMVE(Lookup context) {
            this.context = context;
        }

        @Override
        public Lookup getLookup() {
            return context;
        }
    } // end of CntxMVE
    
}
