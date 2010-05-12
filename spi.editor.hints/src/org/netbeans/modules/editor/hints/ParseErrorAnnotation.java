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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.editor.hints;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.text.Annotation;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 *
 * @author Jan Lahoda
 */
public class ParseErrorAnnotation extends Annotation implements PropertyChangeListener {

    private final Severity severity;
    private final FixData fixes;
    private final String description;
    private final String shortDescription;
    private final Position lineStart;
    private final AnnotationHolder holder;
    
    /** Creates a new instance of ParseErrorAnnotation */
    public ParseErrorAnnotation(Severity severity, FixData fixes, String description, Position lineStart, AnnotationHolder holder) {
        this.severity = severity;
        this.fixes = fixes;
        this.description = description;
        this.shortDescription = description + NbBundle.getMessage(ParseErrorAnnotation.class, "LBL_shortcut_promotion"); //NOI18N
        this.lineStart = lineStart;
        this.holder = holder;
        
        if (!fixes.isComputed()) {
            fixes.addPropertyChangeListener(WeakListeners.propertyChange(this, fixes));
        }
    }

    public String getAnnotationType() {
        boolean hasFixes = fixes.isComputed() && !fixes.getFixes().isEmpty();
        
        switch (severity) {
            case ERROR:
                if (hasFixes)
                    return "org-netbeans-spi-editor-hints-parser_annotation_err_fixable";
                else
                    return "org-netbeans-spi-editor-hints-parser_annotation_err";
                
            case WARNING:
                if (hasFixes)
                    return "org-netbeans-spi-editor-hints-parser_annotation_warn_fixable";
                else
                    return "org-netbeans-spi-editor-hints-parser_annotation_warn";
            case VERIFIER:
                if (hasFixes)
                    return "org-netbeans-spi-editor-hints-parser_annotation_verifier_fixable";
                else
                    return "org-netbeans-spi-editor-hints-parser_annotation_verifier";
            case HINT:
                if (hasFixes)
                    return "org-netbeans-spi-editor-hints-parser_annotation_hint_fixable";
                else
                    return "org-netbeans-spi-editor-hints-parser_annotation_hint";
            default:
                throw new IllegalArgumentException(String.valueOf(severity));
        }
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (fixes.isComputed()) {
            try {
                holder.detachAnnotation(this);
                holder.attachAnnotation(lineStart, this);
            } catch (BadLocationException ex) {
                throw new IllegalStateException(ex);
            }
        }
    }
    
    public FixData getFixes() {
        return fixes;
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getLineNumber() {
        return holder.lineNumber(lineStart);
    }
    
    Severity getSeverity() {
        return severity;
    }
}
