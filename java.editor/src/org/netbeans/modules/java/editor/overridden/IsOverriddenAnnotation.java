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
package org.netbeans.modules.java.editor.overridden;

import java.awt.Point;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import org.netbeans.api.java.source.UiUtils;
import org.openide.filesystems.FileObject;
import org.openide.text.Annotation;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
class IsOverriddenAnnotation extends Annotation {
    
    private StyledDocument document;
    private Position pos;
    private String shortDescription;
    private AnnotationType type;
    private List<ElementDescription> declarations;
    
    public IsOverriddenAnnotation(StyledDocument document, Position pos, AnnotationType type, String shortDescription, List<ElementDescription> declarations) {
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

                return "org-netbeans-modules-editor-java-is_overridden";
            case HAS_IMPLEMENTATION:

                return "org-netbeans-modules-editor-java-has_implementations";
            case IMPLEMENTS:

                return "org-netbeans-modules-editor-java-implements";
            case OVERRIDES:

                return "org-netbeans-modules-editor-java-overrides";
            default:

                throw new IllegalStateException("Currently not implemented: " + type);
        }
    }
    
    public void attach() {
        NbDocument.addAnnotation(document, pos, -1, this);
    }
    
    public void detachImpl() {
        NbDocument.removeAnnotation(document, this);
    }
    
    public String toString() {
        return "[IsOverriddenAnnotation: " + shortDescription + "]";
    }
    
    public Position getPosition() {
        return pos;
    }
    
    public String debugDump() {
        List<String> elementNames = new ArrayList<String>();
        
        for(ElementDescription desc : declarations) {
            elementNames.add(desc.getDisplayName());
        }
        
        Collections.sort(elementNames);
        
        return "IsOverriddenAnnotation: type=" + type.name() + ", elements:" + elementNames.toString();
    }
    
    void mouseClicked(JTextComponent c, Point p) {
        if (type == AnnotationType.IMPLEMENTS || type == AnnotationType.OVERRIDES) {
            if (declarations.size() == 1) {
                ElementDescription desc = declarations.get(0);
                FileObject file = desc.getSourceFile();
                
                if (file != null) {
                    UiUtils.open(file, desc.getHandle());
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
                throw new IllegalStateException("Currently not implemented: " + type);
        }
        
        Point position = new Point(p);
        
        SwingUtilities.convertPointToScreen(position, c);
        
        PopupUtil.showPopup(new IsOverriddenPopup(caption, declarations), caption, position.x, position.y, true, 0);
    }
}