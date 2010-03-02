/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.cnd.navigation.overrides;

import org.netbeans.modules.cnd.modelutil.OverridesPopup;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.util.UIDs;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.utils.ui.PopupUtil;
import org.openide.text.Annotation;
import org.openide.text.NbDocument;

/**
 * @author Vladimir Kvashin
 */
/*package*/ abstract class BaseAnnotation extends Annotation {


    public enum AnnotationType {
        IS_OVERRIDDEN,
        OVERRIDES,
        COMBINED
    }

    /*package*/ static final Logger LOGGER = Logger.getLogger("cnd.overrides.annotations.logger"); // NOI18N

    protected final StyledDocument document;
    protected final Position pos;
    protected final AnnotationType type;
    protected final Collection<CsmUID<? extends CsmOffsetableDeclaration>> baseUIDs;
    protected final Collection<CsmUID<? extends CsmOffsetableDeclaration>> descUIDs;
    
    protected BaseAnnotation(StyledDocument document, CsmOffsetableDeclaration decl,
            Collection<? extends CsmOffsetableDeclaration> baseDecls,
            Collection<? extends CsmOffsetableDeclaration> descDecls) {
        assert decl != null;
        this.document = document;
        this.pos = new DeclarationPosition(decl);
        if (baseDecls.isEmpty() && !descDecls.isEmpty()) {
            type = AnnotationType.IS_OVERRIDDEN;
        } else if (!baseDecls.isEmpty() && descDecls.isEmpty()) {
            type = AnnotationType.OVERRIDES;
        } else if (!baseDecls.isEmpty() && !descDecls.isEmpty()) {
            type = AnnotationType.COMBINED;
        } else { //both are empty
            throw new IllegalArgumentException("Either overrides or overridden should be non empty"); //NOI18N
        }
        baseUIDs = new ArrayList<CsmUID<? extends CsmOffsetableDeclaration>>(baseDecls.size());
        for (CsmOffsetableDeclaration d : baseDecls) {
            baseUIDs.add(UIDs.get(d));
        }
        descUIDs = new ArrayList<CsmUID<? extends CsmOffsetableDeclaration>>(descDecls.size());
        for (CsmOffsetableDeclaration d : descDecls) {
            descUIDs.add(UIDs.get(d));
        }
    }
    
    public AnnotationType getType() {
        return type;
    }

    @Override
    public String getAnnotationType() {
        switch(getType()) {
            case IS_OVERRIDDEN:
                return "org-netbeans-modules-cnd-is_overridden"; //NOI18N
            case OVERRIDES:
                return "org-netbeans-modules-cnd-overrides"; //NOI18N
            case COMBINED:
                return "org-netbeans-modules-cnd-override-is-overridden-combined"; //NOI18N
            default:
                throw new IllegalStateException("Currently not implemented: " + type); //NOI18N
        }
    }
    
    public void attach() {
        NbDocument.addAnnotation(document, pos, -1, this);
    }
    
    public void detachImpl() {
        NbDocument.removeAnnotation(document, this);
    }
    
    @Override
    public String toString() {
        return "[IsOverriddenAnnotation: " + getShortDescription() + "]"; //NOI18N
    }

    public Position getPosition() {
        return pos;
    }

    protected abstract CharSequence debugTypeStirng();

    /** for test/debugging purposes */
    public CharSequence debugDump() {
        StringBuilder sb = new StringBuilder();
        int line = NbDocument.findLineNumber(document, getPosition().getOffset()) + 1; // convert to 1-based
        sb.append(line);
        sb.append(':');
        sb.append(debugTypeStirng());
        sb.append(' ');
        boolean first = true;

        Comparator<? super CsmOffsetableDeclaration> comparator = new Comparator<CsmOffsetableDeclaration>() {
            @Override
            public int compare(CsmOffsetableDeclaration o1, CsmOffsetableDeclaration o2) {
                return o1.getQualifiedName().toString().compareTo(o2.getQualifiedName().toString());
            }

        };
        List<? extends CsmOffsetableDeclaration> baseDecls = toDeclarations(baseUIDs);
        Collections.sort(baseDecls, comparator);

        List<? extends CsmOffsetableDeclaration> descDecls = toDeclarations(descUIDs);
        Collections.sort(descDecls, comparator);

        List<CsmOffsetableDeclaration> allDecls = new ArrayList<CsmOffsetableDeclaration>();
        allDecls.addAll(baseDecls);
        allDecls.addAll(descDecls);

        for (CsmOffsetableDeclaration decl : allDecls) {
            int gotoLine = decl.getStartPosition().getLine();
            String gotoFile = decl.getContainingFile().getName().toString();
            if (first) {
                first = false;
            } else {
                sb.append(',');
            }
            sb.append(decl.getQualifiedName());
            sb.append(' ');
            sb.append(gotoFile);
            sb.append(':');
            sb.append(gotoLine);
        }
        return sb;
    }

    void mouseClicked(JTextComponent c, Point p) {
        Point position = new Point(p);
        position.y += c.getFontMetrics(c.getFont()).getHeight();
        SwingUtilities.convertPointToScreen(position, c);        
        performGoToAction(position);
    }

//    public Collection<? extends CsmMethod> getDeclarations() {
//        return methods;
//    }
    
    private void performGoToAction(Point position) {
        if (baseUIDs.size() + descUIDs.size() == 1) {
            CsmUID<? extends CsmOffsetableDeclaration> uid =
                    baseUIDs.isEmpty() ? descUIDs.iterator().next() : baseUIDs.iterator().next();
            CsmOffsetableDeclaration decl = uid.getObject();
            if (decl != null) { // although openSource seems to process nulls ok, it's better to check here
                CsmUtilities.openSource(decl);
            }
        } else if (baseUIDs.size() + descUIDs.size() > 1) {
            String caption = getShortDescription();
            OverridesPopup popup = new OverridesPopup(caption, toDeclarations(baseUIDs), toDeclarations(descUIDs));
            PopupUtil.showPopup(popup, caption, position.x, position.y, true, 0);
        } else {
            throw new IllegalStateException("method list should not be empty"); // NOI18N
        }
    }

    private static List<? extends CsmOffsetableDeclaration> toDeclarations(Collection<CsmUID<? extends CsmOffsetableDeclaration>> uids) {
        List<CsmOffsetableDeclaration> decls = new ArrayList<CsmOffsetableDeclaration>(uids.size());
        for (CsmUID<? extends CsmOffsetableDeclaration> uid : uids) {
            CsmOffsetableDeclaration decl = uid.getObject();
            if (decl != null) {
                decls.add(decl);
            }
        }
        return decls;
    }


    private static class DeclarationPosition implements Position {

        private final int offset;

        public DeclarationPosition(CsmOffsetableDeclaration decl) {
            this.offset = decl.getStartOffset();
        }

        @Override
        public int getOffset() {
            return offset;
        }

    }
}
