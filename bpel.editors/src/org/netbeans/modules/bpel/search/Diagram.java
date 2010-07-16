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
package org.netbeans.modules.bpel.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.JComponent;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.editors.api.EditorUtil;
import org.netbeans.modules.bpel.design.DesignView;
import org.netbeans.modules.bpel.design.model.DiagramModel;
import org.netbeans.modules.bpel.design.model.elements.VisualElement;
import org.netbeans.modules.bpel.design.model.patterns.CompositePattern;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;
import static org.netbeans.modules.xml.misc.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.01.11
 */
public final class Diagram {

    public Diagram(DesignView view) {
        myView = view;
    }

    public JComponent getComponent() {
        return myView;
    }

    public List<Element> getElements(boolean inSelectionOnly) {
        DiagramModel model = myView.getModel();
        List<Element> elements = new ArrayList<Element>();
        travel(getRoot(model, inSelectionOnly), elements, ""); // NOI18N
        return elements;
    }

    private void travel(Pattern pattern, List<Element> elements, String indent) {
        if (pattern == null) {
            return;
        }
        travel(pattern.getElements(), elements, indent);

        if (pattern instanceof CompositePattern) {
            CompositePattern composite = (CompositePattern) pattern;
            travel(composite.getBorder(), elements, indent);
            Collection<Pattern> patterns = composite.getNestedPatterns();

            for (Pattern patern : patterns) {
                travel(patern, elements, indent + "  "); // NOI18N
            }
        }
    }

    private void travel(Collection<VisualElement> visual, List<Element> elements, String indent) {
        for (VisualElement element : visual) {
            travel(element, elements, indent);
        }
    }

    private void travel(VisualElement element, List<Element> elements, String indent) {
        if (element != null) {
//out(indent + " see: " + element);
            elements.add(new Element(element));
        }
    }

    private Pattern getRoot(DiagramModel model, boolean inSelectionOnly) {
        if (inSelectionOnly) {
            return model.getView().getSelectionModel().getSelectedPattern();
        }
        return model.getRootPattern();
    }

    public void clearHighlighting() {
        Decorator.getDecorator(myView).clearHighlighting();
    }

    // --------------------------
    public static class Element {

        Element(VisualElement element) {
            myElement = element;
        }

        public String getName() {
            return myElement.getText();
        }

        public void gotoSource() {
            EditorUtil.goToSource(getBpelEntity());
        }

        public void gotoDesign() {
            Pattern pattern = myElement.getPattern();
            DesignView view = pattern.getModel().getView();

            // select
            view.getSelectionModel().setSelectedPattern(pattern);

            //
            getDecorator().select(getBpelEntity(), myElement);

            // scroll
            myElement.scrollTo();
        }

        public void highlight() {
            getDecorator().doHighlight(getBpelEntity(), true);
        }

        public void unhighlight() {
            getDecorator().doHighlight(getBpelEntity(), false);
        }

        private Decorator getDecorator() {
            return Decorator.getDecorator(myElement.getPattern().getModel().getView());
        }

        public BpelEntity getBpelEntity() {
            return myElement.getPattern().getOMReference();
        }
        private VisualElement myElement;
    }

    private DesignView myView;
}
