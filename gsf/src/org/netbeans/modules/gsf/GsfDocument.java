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

package org.netbeans.modules.gsf;

import org.netbeans.modules.gsf.api.GsfLanguage;
import org.netbeans.editor.DrawLayer;
import org.netbeans.editor.DrawLayerFactory;
import org.netbeans.editor.Formatter;
import org.netbeans.editor.Settings;
import org.netbeans.editor.ext.ExtFormatter;
import org.netbeans.modules.editor.NbEditorDocument;

/**
 *
 * @author Jan Lahoda
 * @author Tor Norbye
 */
public class GsfDocument extends NbEditorDocument {
    private Language language;
    private Formatter formatter;
    
    public GsfDocument(Class kitClass, Language language) {
        super(kitClass);
        if (language.getGsfLanguage() != null) {
            putProperty(org.netbeans.api.lexer.Language.class, language.getGsfLanguage().getLexerLanguage());
        }
        
        this.language = language;
    }

    @Override
    public boolean isIdentifierPart(char ch) {
        GsfLanguage gsfLanguage = language.getGsfLanguage();
        if (gsfLanguage != null) {
            return gsfLanguage.isIdentifierChar(ch);
        }
        
        return super.isIdentifierPart(ch);
    }
    
    @Override
    public boolean addLayer(DrawLayer layer, int visibility) {
        if (DrawLayerFactory.SyntaxLayer.class.equals(layer.getClass()))
            return false;
        
        return super.addLayer(layer, visibility);
    }

    /** Get the formatter for this document. */
    public Formatter getFormatter() {
        if (formatter == null) {
            // NbDocument will go looking in the settings map for a formatter - let's make
            // sure it finds one with -MY- kit associated with it (without this it finds
            // a BaseKit one which has the wrong formatting defaults!
            Settings.setValue(getKitClass(), FORMATTER, 
                    new ExtFormatter(GsfEditorKitFactory.GsfEditorKit.class));
            formatter = super.getFormatter();
        }
        
        return formatter;
    }
    
    @Override
    public int getShiftWidth() {
        org.netbeans.modules.gsf.api.Formatter f = language.getFormatter();
        if (f != null) {
            return f.indentSize();
        }
        return super.getShiftWidth();
    }
}
