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
package org.netbeans.modules.cnd.navigation.overrides;

import java.util.MissingResourceException;
import org.netbeans.modules.cnd.modelutil.OverridesPopup;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
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
import org.openide.util.NbBundle;

/**
 * @author Vladimir Kvashin
 */
public abstract class BaseAnnotation extends Annotation {

    public enum AnnotationType {
        IS_OVERRIDDEN,
        OVERRIDES,
        OVERRIDEN_COMBINED,
        IS_SPECIALIZED,
        SPECIALIZES,
        EXTENDED_IS_SPECIALIZED,
        EXTENDED_SPECIALIZES
    }

    /*package*/ static final Logger LOGGER = Logger.getLogger("cnd.overrides.annotations.logger"); // NOI18N

    protected final StyledDocument document;
    protected final Position pos;
    protected final AnnotationType type;
    protected final Collection<CsmUID<? extends CsmOffsetableDeclaration>> baseUIDs;
    protected final Collection<CsmUID<? extends CsmOffsetableDeclaration>> descUIDs;
    protected final Collection<CsmUID<? extends CsmOffsetableDeclaration>> baseTemplateUIDs;
    protected final Collection<CsmUID<? extends CsmOffsetableDeclaration>> specializationUIDs;
    
    protected BaseAnnotation(StyledDocument document, CsmOffsetableDeclaration decl,
            Collection<? extends CsmOffsetableDeclaration> baseDecls,
            Collection<? extends CsmOffsetableDeclaration> descDecls,
            Collection<? extends CsmOffsetableDeclaration> baseTemplates,
            Collection<? extends CsmOffsetableDeclaration> templateSpecializations) {
        assert decl != null;
        this.document = document;
        this.pos = getPosition(document, decl.getStartOffset());
        if (baseTemplates.isEmpty() && templateSpecializations.isEmpty()) {
            // overrides only 
            if (baseDecls.isEmpty()) {
                type = AnnotationType.IS_OVERRIDDEN;
            } else if (descDecls.isEmpty()) {
                type =  AnnotationType.OVERRIDES;
            } else {
                type = AnnotationType.OVERRIDEN_COMBINED;
            }
        } else if (baseDecls.isEmpty() && descDecls.isEmpty()) {
            // templates only
            if (baseTemplates.isEmpty()) {
                type = AnnotationType.IS_SPECIALIZED;
            } else if (templateSpecializations.isEmpty()) {
                type = AnnotationType.SPECIALIZES;
            } else {
                type = AnnotationType.SPECIALIZES;
            }
        } else {
            assert !baseTemplates.isEmpty() || !templateSpecializations.isEmpty() || !descDecls.isEmpty() || !baseDecls.isEmpty() : "all are empty?";
            if (baseTemplates.isEmpty()) {
                type = AnnotationType.EXTENDED_IS_SPECIALIZED;
            } else if (templateSpecializations.isEmpty()) {
                type = AnnotationType.EXTENDED_SPECIALIZES;
            } else {
                type = AnnotationType.EXTENDED_SPECIALIZES;
            }
        }
        baseUIDs = new ArrayList<CsmUID<? extends CsmOffsetableDeclaration>>(baseDecls.size());
        for (CsmOffsetableDeclaration d : baseDecls) {
            baseUIDs.add(UIDs.get(d));
        }
        descUIDs = new ArrayList<CsmUID<? extends CsmOffsetableDeclaration>>(descDecls.size());
        for (CsmOffsetableDeclaration d : descDecls) {
            descUIDs.add(UIDs.get(d));
        }
        baseTemplateUIDs = new ArrayList<CsmUID<? extends CsmOffsetableDeclaration>>(baseTemplates.size());
        for (CsmOffsetableDeclaration d : baseTemplates) {
            baseTemplateUIDs.add(UIDs.get(d));
        }
        specializationUIDs = new ArrayList<CsmUID<? extends CsmOffsetableDeclaration>>(templateSpecializations.size());
        for (CsmOffsetableDeclaration d : templateSpecializations) {
            specializationUIDs.add(UIDs.get(d));
        }
    }
    
    public AnnotationType getType() {
        return type;
    }

    @Override
    public String getAnnotationType() {
        switch(getType()) {
            case IS_OVERRIDDEN:
                return "org-netbeans-modules-editor-annotations-is_overridden"; //NOI18N
            case OVERRIDES:
                return "org-netbeans-modules-editor-annotations-overrides"; //NOI18N
            case SPECIALIZES:
                return "org-netbeans-modules-cnd-navigation-specializes"; // NOI18N
            case IS_SPECIALIZED:
                return "org-netbeans-modules-cnd-navigation-is_specialized"; // NOI18N
            case OVERRIDEN_COMBINED:
                return "org-netbeans-modules-editor-annotations-override-is-overridden-combined"; //NOI18N
            case EXTENDED_SPECIALIZES:
                return "org-netbeans-modules-cnd-navigation-extended_specializes"; // NOI18N
            case EXTENDED_IS_SPECIALIZED:
                return "org-netbeans-modules-cnd-navigation-extended_is_specialized"; // NOI18N
            default:
                throw new IllegalStateException("Currently not implemented: " + type); //NOI18N
        }
    }
    
    protected final String addTemplateAnnotation(String baseDescr) throws MissingResourceException {
        if (baseTemplateUIDs.isEmpty() && !specializationUIDs.isEmpty()) {
            CharSequence text = "..."; //NOI18N
            if (specializationUIDs.size() == 1) {
                CsmOffsetableDeclaration obj = specializationUIDs.iterator().next().getObject();
                if (obj != null) {
                    text = obj.getQualifiedName();
                }
            }
            if (baseDescr.isEmpty()) {
                baseDescr = NbBundle.getMessage(getClass(), "LAB_Specialization", text);
            } else {
                baseDescr = NbBundle.getMessage(getClass(), "LAB_Specialization2", baseDescr, text);
            }
        } else if (!baseTemplateUIDs.isEmpty() && specializationUIDs.isEmpty()) {
            CharSequence text = "..."; //NOI18N
            if (baseTemplateUIDs.size() == 1) {
                CsmOffsetableDeclaration obj = baseTemplateUIDs.iterator().next().getObject();
                if (obj != null) {
                    text = obj.getQualifiedName();
                }
            }
            if (baseDescr.isEmpty()) {
                baseDescr = NbBundle.getMessage(getClass(), "LAB_BaseTemplate", text);
            } else {
                baseDescr = NbBundle.getMessage(getClass(), "LAB_BaseTemplate2", baseDescr, text);
            }
        } else if (!baseTemplateUIDs.isEmpty() && !specializationUIDs.isEmpty()) {
            if (baseDescr.isEmpty()) {
                baseDescr = NbBundle.getMessage(getClass(), "LAB_BaseTemplateAndSpecialization");
            } else {
                baseDescr = NbBundle.getMessage(getClass(), "LAB_BaseTemplateAndSpecialization2", baseDescr);
            }
        }
        return baseDescr;
    }
    
    public boolean attach() {
        if(pos.getOffset() == -1 || pos.getOffset() >= document.getEndPosition().getOffset()) {
            return false;
        }
         NbDocument.addAnnotation(document, pos, -1, this);
         return true;
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

    private static Position getPosition(final StyledDocument doc, final int offset) {
        class Impl implements Runnable {

            private Position pos;

            @Override
            public void run() {
                if (offset < 0 || offset >= doc.getLength()) {
                    return;
                }

                try {
                    pos = doc.createPosition(offset - NbDocument.findLineColumn(doc, offset));
                } catch (BadLocationException ex) {
                    //should not happen?
                    Logger.getLogger(BaseAnnotation.class.getName()).log(Level.FINE, null, ex);
                }
            }
        }

        Impl i = new Impl();

        doc.render(i);
        
        if (i.pos == null) {
            i.pos = new Position() {
                @Override
                public int getOffset() {
                    return -1;
                }
            };
        }

        return i.pos;
    }
    
    protected abstract CharSequence debugTypeString();

    /** for test/debugging purposes */
    public CharSequence debugDump() {
        StringBuilder sb = new StringBuilder();
        int line = NbDocument.findLineNumber(document, getPosition().getOffset()) + 1; // convert to 1-based
        sb.append(line);
        sb.append(':');
        sb.append(debugTypeString());
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

        List<? extends CsmOffsetableDeclaration> baseTemplateDecls = toDeclarations(baseTemplateUIDs);
        Collections.sort(baseTemplateDecls, comparator);

        List<? extends CsmOffsetableDeclaration> specializationDecls = toDeclarations(specializationUIDs);
        Collections.sort(specializationDecls, comparator);
        
        List<CsmOffsetableDeclaration> allDecls = new ArrayList<CsmOffsetableDeclaration>();
        allDecls.addAll(baseDecls);
        allDecls.addAll(descDecls);
        allDecls.addAll(baseTemplateDecls);
        allDecls.addAll(specializationDecls);

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
    
    private void performGoToAction(Point position) {
        if (baseUIDs.size() + descUIDs.size() + baseTemplateUIDs.size() + specializationUIDs.size() == 1) {
            Collection<CsmUID<? extends CsmOffsetableDeclaration>> all = new ArrayList<CsmUID<? extends CsmOffsetableDeclaration>> (1);
            all.addAll(baseUIDs);
            all.addAll(descUIDs);
            all.addAll(baseTemplateUIDs);
            all.addAll(specializationUIDs);
            CsmUID<? extends CsmOffsetableDeclaration> uid = all.iterator().next();
            CsmOffsetableDeclaration decl = uid.getObject();
            if (decl != null) { // although openSource seems to process nulls ok, it's better to check here
                CsmUtilities.openSource(decl);
            }
        } else if (baseUIDs.size() + descUIDs.size() + baseTemplateUIDs.size() + specializationUIDs.size() > 1) { 
            String caption = getShortDescription();
            OverridesPopup popup = new OverridesPopup(caption, toDeclarations(baseUIDs), toDeclarations(descUIDs), 
                    toDeclarations(baseTemplateUIDs), toDeclarations(specializationUIDs));
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
