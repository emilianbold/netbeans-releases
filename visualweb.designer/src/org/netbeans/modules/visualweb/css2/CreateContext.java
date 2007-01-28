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
package org.netbeans.modules.visualweb.css2;

import java.awt.FontMetrics;
import java.util.ArrayList;

import org.netbeans.modules.visualweb.designer.WebForm;


/**
 * Context used during when creating the box hierarchy.
 * Contains information that cannot simply be passed to the
 * children since the children may unpredictably side-effect
 * it.
 * @todo Move linebox management (addToLineBox, finishLineBox)
 *       into this method?
 * @author Tor Norbye
 */
public class CreateContext {
    /** Current linebox being constructed; made be null */
    public LineBoxGroup lineBox;
    public FontMetrics metrics; // carries the font too -- metrics.getFont()
    public BoxList fixedBoxes;
    public ContainerBox prevChangedBox; // used during updates when we modify boxes around target
    public ContainerBox nextChangedBox;
    private ArrayList visitedForms;

    /** Construct a box-creation context for the given webform. */
    public CreateContext() {
        // There will almost never be many page fragments in a page so don't need
        // to use heavy datastructures here -- just a simple list will do
        visitedForms = new ArrayList(4);
    }

    /** Construct a nested box-creation context */
    public CreateContext(CreateContext outer) {
        visitedForms = outer.visitedForms;
    }

    public String toString() {
        return super.toString() + "[doc=" + visitedForms.toString() + ", " + "lineBox=" + lineBox + "]";
    }

    /** Add a fixed box to the list of fixed boxes in this view hierarchy */
    public void addFixedBox(CssBox box) {
        if (fixedBoxes == null) {
            fixedBoxes = new BoxList(3); // usually very few of these
            fixedBoxes.setKeepSorted(true);
        }

        fixedBoxes.add(box, null, null);
    }

    public BoxList getFixedBoxes() {
        return fixedBoxes;
    }

    /** Return true iff we've already visited the given webform in this box creation phase */
    public boolean isVisitedForm(WebForm webform) {
        if (visitedForms == null) {
            return false;
        }

        return visitedForms.contains(webform);
    }

    /** Add a page to be traversed during box creation */
    public void pushPage(WebForm webform) {
        visitedForms.add(webform);
    }

    /** Pop a page from the page visit stack */
    public WebForm popPage() {
        return (WebForm)visitedForms.remove(visitedForms.size() - 1);
    }
}
