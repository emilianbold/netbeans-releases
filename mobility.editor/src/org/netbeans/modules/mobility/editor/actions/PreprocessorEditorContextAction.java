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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
