/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.worklist.dataloader;

import java.util.List;
import javax.swing.text.StyledDocument;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.modules.wlm.model.api.WLMComponent;
import org.netbeans.modules.wlm.model.api.WLMModel;
import org.netbeans.modules.worklist.editor.designview.DesignView;
import org.netbeans.modules.worklist.editor.designview.DesignerMultiViewDescription;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.openide.cookies.EditCookie;
import org.openide.cookies.LineCookie;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author anjeleevich
 */
public class GoToRunnable implements Runnable {
    private WLMModel model;
    private WLMComponent component;
    private WorklistDataObject dataObject;

    public GoToRunnable(WorklistDataObject dataObject, WLMModel model, 
            WLMComponent component)
    {
        assert (dataObject != null);
        assert (model != null);

        // null component is acceptable
        // will switch to source in this case
        
        this.dataObject = dataObject;
        this.model = model;
        this.component = component;
    }

    public void run() {
        Lookup dataObjectLookup = dataObject.getLookup();
        LineCookie lineCookie = dataObjectLookup.lookup(LineCookie.class);
        EditCookie editCookie = dataObjectLookup.lookup(EditCookie.class);

        if (lineCookie == null || editCookie == null) {
            return ;
        }

        // Opens the editor or brings it into focus
        // and makes it the activated topcomponent.
        editCookie.edit();

        TopComponent topComponent = WindowManager.getDefault().getRegistry()
                .getActivated();
        MultiViewHandler multiViewHandler = (topComponent == null) ? null
                : MultiViews.findMultiViewHandler(topComponent);

        MultiViewPerspective[] perspectives = (multiViewHandler == null) ? null
                : multiViewHandler.getPerspectives();

        if (perspectives == null) {
            return;
        }

        MultiViewPerspective selectedPerspective = multiViewHandler
                .getSelectedPerspective();

        /* If model is broken
         * OR if the resultItem.getComponents() is null which
         * means the resultItem was generated when the model was broken.
         *  In the above cases switch to the source multiview.
         */
        boolean designOrSourcePerspective =
                isDesignPerspective(selectedPerspective)
                        || isSourcePerspective(selectedPerspective);
        
        if ((model.getState() == Model.State.NOT_WELL_FORMED)
                || (component == null)
                || !designOrSourcePerspective)
        {
            MultiViewPerspective sourcePerspective
                    = findSourcePerspective(perspectives);
            multiViewHandler.requestActive(sourcePerspective);
        }

        // update selected perspective
        selectedPerspective = multiViewHandler.getSelectedPerspective();

//        // # 159351
//        if (bpelEntity instanceof Import || bpelEntity instanceof Variable) {
//            Line line = LineUtil.getLine(resultItem);
//
//            if (line != null) {
//                line.show(Line.SHOW_GOTO);
//            }
//            SoaUtil.openActiveMVEditor("bpelsource"); // NOI18N
//            return;
//        }
//
//        if (isDesignPerspective(selectedPerspective)) {
//            List<TopComponent> list = getAssociatedTopComponents();
//
//            for (TopComponent topComponent : list) {
//                // Make sure this is a multiview window, and not just some
//                // window that has our DataObject (e.g. Projects,Files).
//                MultiViewHandler handler = MultiViews.findMultiViewHandler(topComponent);
//
//                if (handler != null && topComponent != null) {
//                    SelectBpelElement selectElement = (SelectBpelElement) topComponent.getLookup().lookup(SelectBpelElement.class);
//
//                    if (selectElement == null)
//                        return;
//                    selectElement.select(bpelEntity);
//                }
//            }
//        }
//        else if (selectedPerspective.preferredID().equals(BPELSourceMultiViewElementDesc.PREFERED_ID)) {
//            Line line = LineUtil.getLine(resultItem);
//
//            if (line != null) {
//                line.show(Line.SHOW_GOTO);
//            }
//        }

        if (isDesignPerspective(selectedPerspective)) {
            DesignView designView = topComponent.getLookup()
                    .lookup(DesignView.class);
            if (designView != null) {
                designView.selectComponent(component);
            }
        } else if (isSourcePerspective(selectedPerspective)) {
            int lineNumber = -1;
            int lineColumn = -1;

            if (component != null) {
                ModelSource modelSource = model.getModelSource();

                DocumentComponent documentComponent = (DocumentComponent)
                        component;
                StyledDocument document = modelSource.getLookup()
                        .lookup(StyledDocument.class);

                int documentComponentPosition = documentComponent.findPosition();

                try {
                    lineNumber = NbDocument.findLineNumber(document,
                        documentComponentPosition);
                    lineColumn = NbDocument.findLineColumn(document,
                        documentComponentPosition);
                } catch (Exception ex) {
                    // do nothing
                    // will use default values
                }
            }

            if (lineNumber >= 0) {
                try {
                    Line.Set lineSet = lineCookie.getLineSet();
                    Line line = lineSet.getCurrent(lineNumber);
                    line.show(Line.ShowOpenType.OPEN,
                           Line.ShowVisibilityType.FOCUS,
                           lineColumn);
                } catch (Exception ex) {
                    // do nothing
                }
            }
        }
    }

    public static MultiViewPerspective findSourcePerspective(
            MultiViewPerspective[] perspectives)
    {
        for (int i = perspectives.length - 1; i >= 0; i--) {
            if (isSourcePerspective(perspectives[i])) {
                return perspectives[i];
            }
        }
        return null;
    }

    public static MultiViewPerspective findDesignPerspective(
            MultiViewPerspective[] perspectives)
    {
        for (int i = perspectives.length - 1; i >= 0; i--) {
            if (isDesignPerspective(perspectives[i])) {
                return perspectives[i];
            }
        }
        return null;
    }

    public static boolean isSourcePerspective(
            MultiViewPerspective perspective)
    {
        return (perspective != null) && WorklistSourceMultiViewDescription
                .PREFERRED_ID.equals(perspective.preferredID());
    }

    public static boolean isDesignPerspective(
            MultiViewPerspective perspective)
    {
        return (perspective != null) && DesignerMultiViewDescription
                .PREFERRED_ID.equals(perspective.preferredID());
    }
}
