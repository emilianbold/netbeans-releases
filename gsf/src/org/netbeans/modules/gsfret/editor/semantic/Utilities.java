/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.gsfret.editor.semantic;

import java.awt.Color;
import java.util.Collection;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.modules.gsf.api.ColoringAttributes;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.modules.gsf.api.PositionManager;
import org.netbeans.modules.editor.highlights.spi.Highlight;
import org.netbeans.modules.gsf.Language;

/**
 * This file is originally from Retouche, the Java Support 
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible. 
 *
 *
 * @author Jan Lahoda
 */
public class Utilities {
    private static Highlight createHighlightImpl(Language language, Document doc, int startOffset, int endOffset, Collection<ColoringAttributes> c, Color es) {
        try {
            if (startOffset > doc.getLength() || endOffset > doc.getLength()) {
                return null;
            }
            
            return new HighlightImpl(language, doc, startOffset, endOffset, c, es);
        } catch (BadLocationException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Highlight createHighlight(final Language language, final Document doc, final int startOffset, final int endOffset, final Collection<ColoringAttributes> c, final Color es) {
        final Highlight[] result = new Highlight[1];
        
        doc.render(new Runnable() {
            public void run() {
                result[0] = createHighlightImpl(language, doc, startOffset, endOffset, c, es);
            }
        });
        
        return result[0];
    }
    
    private static Highlight createHighlightImpl(Language language, ParserResult pr, PositionManager positions, Document doc, int startOffset, int endOffset, Collection<ColoringAttributes> c, Color es) {
        try {
            if (startOffset > doc.getLength() || endOffset > doc.getLength()) {
                return null;
            }
            
            return new HighlightImpl(language, doc, startOffset, endOffset, c, es);
        } catch (BadLocationException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static Highlight createHighlight(final Language language, final ParserResult pr, final PositionManager positions, final Document doc, final int startOffset, final int endOffset, final Collection<ColoringAttributes> c, final Color es) {
        final Highlight[] result = new Highlight[1];
        
        doc.render(new Runnable() {
            public void run() {
                result[0] = createHighlightImpl(language, pr, positions, doc, startOffset, endOffset, c, es);
            }
        });
        
        return result[0];
    }
}
