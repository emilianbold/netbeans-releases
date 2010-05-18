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

/*
 * PreprocessorEditorContextAction.java
 *
 * Created on July 28, 2005, 3:42 PM
 *
 */
package org.netbeans.modules.mobility.editor.actions;

import java.util.ArrayList;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position.Bias;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.guards.GuardedSection;
import org.netbeans.api.editor.guards.GuardedSectionManager;
import org.netbeans.editor.BaseAction;
import org.netbeans.mobility.antext.preprocessor.PPBlockInfo;
import org.netbeans.mobility.antext.preprocessor.PPLine;
import org.netbeans.modules.mobility.project.ProjectConfigurationsHelper;
import org.openide.text.NbDocument;

/**
 *
 * @author Adam Sotona
 */
public abstract class PreprocessorEditorContextAction extends BaseAction {
    
    /** Creates a new instance of PreprocessorEditorContextAction */
    public PreprocessorEditorContextAction(String name) {
        super(name);
    }
    
    public abstract String getPopupMenuText(ProjectConfigurationsHelper cfgProvider, ArrayList<PPLine> preprocessorLineList, JTextComponent target);
    
    public abstract boolean isEnabled(ProjectConfigurationsHelper cfgProvider, ArrayList<PPLine> preprocessorLineList, JTextComponent target);
    
    protected int getSelectionStartLine(final JTextComponent c) {
        if (c == null || !(c.getDocument() instanceof StyledDocument)) return 0;
        return NbDocument.findLineNumber((StyledDocument)c.getDocument(), c.getSelectionStart()) + 1;
    }
    
    protected int getSelectionEndLine(final JTextComponent c) {
        if (c == null || !(c.getDocument() instanceof StyledDocument)) return 0;
        final int i = NbDocument.findLineNumber((StyledDocument)c.getDocument(), c.getSelectionEnd());
        return c.getSelectionStart() == c.getSelectionEnd() || NbDocument.findLineColumn((StyledDocument)c.getDocument(), c.getSelectionEnd()) > 0 ? i + 1 : i;
    }
    
    protected PPBlockInfo getBlock(final JTextComponent target, final ArrayList<PPLine> preprocessorLineList) {
        final int x = getSelectionStartLine(target);
        if (x >= preprocessorLineList.size()) return null;
        return preprocessorLineList.get(x-1).getBlock();
        
    }
    
    protected boolean overlapsBlockBorders(final JTextComponent c, final ArrayList<PPLine> preprocessorLineList) {
        final int sL = getSelectionStartLine(c), eL = getSelectionEndLine(c);
        if (sL >= preprocessorLineList.size() || eL >= preprocessorLineList.size()) return false;
        final PPBlockInfo b = preprocessorLineList.get(sL - 1).getBlock();
        return b != null && (b.getStartLine() == sL || (b.hasFooter() && b.getEndLine() == eL) || b != preprocessorLineList.get(eL - 1).getBlock());
    }
    
    protected boolean overlapsGuardedBlocks(final JTextComponent c) {
        Document d = c.getDocument();
        if (!(d instanceof StyledDocument)) return false;
        GuardedSectionManager man = GuardedSectionManager.getInstance((StyledDocument)d);
        if (man == null) return false;
        for (GuardedSection s : man.getGuardedSections()) try {
            if (s.contains(NbDocument.createPosition(d, c.getSelectionStart(), Bias.Backward), false)
                || s.contains(NbDocument.createPosition(d, c.getSelectionEnd(), Bias.Forward), false)
                || (s.getStartPosition().getOffset() >= c.getSelectionStart() && s.getEndPosition().getOffset() <= c.getSelectionEnd())) return true;
        } catch (BadLocationException ble) {}
        return false;
    }
}
