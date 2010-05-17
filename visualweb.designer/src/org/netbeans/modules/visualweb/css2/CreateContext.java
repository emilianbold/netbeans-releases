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
package org.netbeans.modules.visualweb.css2;

import java.awt.FontMetrics;
import java.util.ArrayList;
import java.util.List;

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
//    private ArrayList visitedForms;
    private List<WebForm> visitedForms;

    /** Construct a box-creation context for the given webform. */
    public CreateContext() {
        // There will almost never be many page fragments in a page so don't need
        // to use heavy datastructures here -- just a simple list will do
        visitedForms = new ArrayList<WebForm>(4);
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
        return visitedForms.remove(visitedForms.size() - 1);
    }
}
