/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.domdiff;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.html.editor.lib.api.HtmlSource;
import org.netbeans.modules.html.editor.lib.api.elements.Attribute;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;

public class DiffElement {

    public static enum ChangeType {
        NONE,
        ADDED,
        REMOVED,
        ADDED_BY_MOVE,
        REMOVED_BY_MOVE,
    }
    
    private OpenTag e1;
    private OpenTag e2;
    private ChangeType change;
    private List<DiffElement> children = new ArrayList<DiffElement>();
    private List<DiffAttribute> attributes = new ArrayList<DiffAttribute>();
    private List<DiffText> text = new ArrayList<DiffText>();
    private boolean containsMovedItems = false;
    private DiffElement parent;
    private Origin2 origin;

    public DiffElement(OpenTag e1, OpenTag e2, ChangeType change) {
        assert (change == ChangeType.REMOVED ? e1 != null && e2 == null : true);
        assert (change == ChangeType.ADDED ? e1 == null && e2 != null : true);
        assert (change == ChangeType.NONE ? e1 != null && e2 != null : true);
        this.e1 = e1;
        this.e2 = e2;
        this.change = change;
    }
    
    public ChangeType getChange() {
        return change;
    }

    public OpenTag getPreviousElement() {
        return e1;
    }

    public OpenTag getCurrentElement() {
        return e2;
    }

    public void addChild(OpenTag e1, OpenTag e2, DiffElement.ChangeType change) {
        children.add(new DiffElement(e1, e2, change));
    }
    
    void setChildren(List<DiffElement> children) {
        this.children = children;
        for (DiffElement de : children) {
            de.setParent(this);
        }
    }

    private void setParent(DiffElement parent) {
        this.parent = parent;
    }

    public DiffElement getParent() {
        return parent;
    }
    
    /*
     * This change is not of type ADDED but MOVED:
     */
    public void markAsMovedFrom(DiffElement r) {
        assert this.getChange() == ChangeType.ADDED;
        assert r.getChange() == ChangeType.REMOVED;
        this.change = ChangeType.ADDED_BY_MOVE;
        r.change = ChangeType.REMOVED_BY_MOVE;
        // do I need to store link between them here?
    }
    
    public List<DiffElement> getChildren() {
        return children;
    }
    
    public void addAttribute(Attribute a1, Attribute a2, DiffAttribute.ChangeType change) {
        attributes.add(new DiffAttribute(a1, a2, change));
    }

    public List<DiffAttribute> getAttributes() {
        return attributes;
    }
    
    void setAttribute(List<DiffAttribute> attributes) {
        this.attributes = attributes;
    }

    void setText(List<DiffText> text) {
        this.text = text;
    }

    public List<DiffText> getText() {
        return text;
    }

    public boolean hasMovedItems() {
        return containsMovedItems;
    }
    
    public void setHasMovedItems() {
        containsMovedItems = true;
    }

    void setOrigin(int previousDiffIndex) {
        origin = new Origin2(previousDiffIndex);
    }

    void setOrigin(Origin2 o) {
        this.origin = o;
    }

    Origin2 getOrigin() {
        return origin;
    }

    @Override
    public String toString() {
        return "DiffElement{" + "e1=" + e1 + ", e2=" + e2 + ", change=" + change + '}';
    }
    
    public void dump(HtmlSource s1, HtmlSource s2) {
        dump(s1, s2, "", this);
    }
    
    private static void dump(HtmlSource s1, HtmlSource s2, String prefix, DiffElement element) {
        System.out.println(prefix+element);
        for (DiffAttribute da : element.attributes) {
            System.out.println(prefix+" "+da);
        }
        for (DiffElement de : element.children) {
            dump(s1, s2, prefix+" ", de);
        }
        for (DiffText dt : element.text) {
            System.out.println(prefix+" "+dt);
        }
    }
}
