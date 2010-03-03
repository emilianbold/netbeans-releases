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
package org.netbeans.modules.csl.editor.overridden;

import java.awt.Point;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.csl.api.DeclarationFinder.AlternativeLocation;
import org.netbeans.modules.csl.api.UiUtils;
import org.netbeans.modules.csl.editor.hyperlink.PopupUtil;
import org.openide.filesystems.FileObject;
import org.openide.text.Annotation;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public class IsOverriddenAnnotation extends Annotation {
    
    private final StyledDocument document;
    private final Position pos;
    private final String shortDescription;
    private final AnnotationType type;
    private final Collection<? extends AlternativeLocation> declarations;
    
    public IsOverriddenAnnotation(StyledDocument document, Position pos, AnnotationType type, String shortDescription, Collection<? extends AlternativeLocation> declarations) {
        //#166351 -- null pos for some reason
        assert pos != null;

        this.document = document;
        this.pos = pos;
        this.type = type;
        this.shortDescription = shortDescription;
        this.declarations = declarations;
    }
    
    public String getShortDescription() {
        return shortDescription;
    }

    public String getAnnotationType() {
        switch(type) {
            case IS_OVERRIDDEN:

                return "org-netbeans-modules-editor-annotations-is_overridden"; //NOI18N
            case HAS_IMPLEMENTATION:

                return "org-netbeans-modules-editor-annotations-has_implementations"; //NOI18N
            case IMPLEMENTS:

                return "org-netbeans-modules-editor-annotations-implements"; //NOI18N
            case OVERRIDES:

                return "org-netbeans-modules-editor-annotations-overrides"; //NOI18N
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
    
    public String toString() {
        return "[IsOverriddenAnnotation: " + shortDescription + "]"; //NOI18N
    }
    
    public Position getPosition() {
        return pos;
    }
    
//    public String debugDump() {
//        List<String> elementNames = new ArrayList<String>();
//
//        for(ElementDescription desc : declarations) {
//            elementNames.add(desc.getDisplayName());
//        }
//
//        Collections.sort(elementNames);
//
//        return "IsOverriddenAnnotation: type=" + type.name() + ", elements:" + elementNames.toString(); //NOI18N
//    }
    
    void mouseClicked(JTextComponent c, Point p) {
        Point position = new Point(p);
        
        SwingUtilities.convertPointToScreen(position, c);
        
        performGoToAction(type, declarations, position, shortDescription);
    }

    public AnnotationType getType() {
        return type;
    }

    public Collection<? extends AlternativeLocation> getDeclarations() {
        return declarations;
    }
    
    static void performGoToAction(AnnotationType type, Collection<? extends AlternativeLocation> declarations, Point position, String shortDescription/*XXX*/) {
        if (type == AnnotationType.IMPLEMENTS || type == AnnotationType.OVERRIDES) {
            if (declarations.size() == 1) {
                AlternativeLocation desc = declarations.iterator().next();
                FileObject file = desc.getLocation().getFileObject();
                
                if (file != null) {
                    UiUtils.open(file, desc.getLocation().getOffset());
                } else {
                    Toolkit.getDefaultToolkit().beep();
                }
                
                return ;
            }
        }
        
        String caption;
        
        switch (type) {
            case IMPLEMENTS:
                caption = NbBundle.getMessage(IsOverriddenAnnotation.class, "CAP_Implements");
                break;
            case OVERRIDES:
                caption = NbBundle.getMessage(IsOverriddenAnnotation.class, "CAP_Overrides");
                break;
            case HAS_IMPLEMENTATION:
            case IS_OVERRIDDEN:
                caption = shortDescription;
                break;
            default:
                throw new IllegalStateException("Currently not implemented: " + type); //NOI18N
        }
        
        PopupUtil.showPopup(new IsOverriddenPopup(caption, new ArrayList<AlternativeLocation>(declarations)), caption, position.x, position.y, true, 0);
    }
}
