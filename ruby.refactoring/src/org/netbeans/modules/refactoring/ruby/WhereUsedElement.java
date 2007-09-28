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
package org.netbeans.modules.refactoring.ruby;


import java.util.Collections;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.text.Position.Bias;
import org.netbeans.api.gsf.Modifier;

import org.netbeans.api.gsf.OffsetRange;
import org.netbeans.api.retouche.source.CompilationInfo;
import org.netbeans.api.retouche.source.UiUtils;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.refactoring.ruby.ui.tree.ElementGripFactory;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.netbeans.modules.ruby.AstUtilities;
import org.netbeans.modules.ruby.lexer.LexUtilities;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.PositionBounds;
import org.openide.text.PositionRef;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * An element in the refactoring preview list which holds information about the find-usages-match
 * 
 * @author Tor Norbye
 */

public class WhereUsedElement extends SimpleRefactoringElementImplementation {
    private PositionBounds bounds;
    private String displayText;
    private FileObject parentFile;

    public WhereUsedElement(PositionBounds bounds, String displayText, FileObject parentFile, String name,
        OffsetRange range, Icon icon) {
        this.bounds = bounds;
        this.displayText = displayText;
        this.parentFile = parentFile;
        ElementGripFactory.getDefault().put(parentFile, name, range, icon);
    }

    public String getDisplayText() {
        return displayText;
    }

    public Lookup getLookup() {
        Object composite =
            ElementGripFactory.getDefault().get(parentFile, bounds.getBegin().getOffset());

        if (composite == null) {
            composite = parentFile;
        }

        return Lookups.singleton(composite);
    }

    public PositionBounds getPosition() {
        return bounds;
    }

    public String getText() {
        return displayText;
    }

    public void performChange() {
    }

    public FileObject getParentFile() {
        return parentFile;
    }

    public static WhereUsedElement create(RubyElementCtx tree) {
        CompilationInfo info = tree.getInfo();
        OffsetRange range = AstUtilities.getNameRange(tree.getNode());
        assert range != OffsetRange.NONE;

        range = LexUtilities.getLexerOffsets(info, range);
        assert range != OffsetRange.NONE;
        
        Set<Modifier> modifiers = Collections.emptySet();
        if (tree.getElement() != null) {
            modifiers = tree.getElement().getModifiers();
        }
        Icon icon = UiUtils.getElementIcon(tree.getKind(), modifiers);
        return create(info, tree.getName(), range, icon);
    }
    
    public static WhereUsedElement create(CompilationInfo info, String name, OffsetRange range, Icon icon) {
        FileObject fo = info.getFileObject();
        int start = range.getStart();
        int end = range.getEnd();
        
        int sta = start;
        int en = start; // ! Same line as start
        String content = null;
        
        try {
            BaseDocument bdoc = RetoucheUtils.getDocument(info, info.getFileObject());
            // I should be able to just call tree.getInfo().getText() to get cached
            // copy - but since I'm playing fast and loose with compilationinfos
            // for for example find subclasses (using a singly dummy FileInfo) I need
            // to read it here instead
            content = bdoc.getText(0, bdoc.getLength());
            sta = Utilities.getRowFirstNonWhite(bdoc, start);

            if (sta == -1) {
                sta = Utilities.getRowStart(bdoc, start);
            }

            en = Utilities.getRowLastNonWhite(bdoc, start);

            if (en == -1) {
                en = Utilities.getRowEnd(bdoc, start);
            } else {
                // Last nonwhite - left side of the last char, not inclusive
                en++;
            }

            // Sometimes the node we get from the AST is for the whole block
            // (e.g. such as the whole class), not the argument node. This happens
            // for example in Find Subclasses out of the index. In this case
            if (end > en) {
                end = start + name.length();

                if (end > bdoc.getLength()) {
                    end = bdoc.getLength();
                }
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(RetoucheUtils.getHtml(content.subSequence(sta, start).toString()));
        sb.append("<b>"); // NOI18N
        sb.append(content.subSequence(start, end));
        sb.append("</b>"); // NOI18N
        sb.append(RetoucheUtils.getHtml(content.subSequence(end, en).toString()));

        CloneableEditorSupport ces = RetoucheUtils.findCloneableEditorSupport(info);
        PositionRef ref1 = ces.createPositionRef(start, Bias.Forward);
        PositionRef ref2 = ces.createPositionRef(end, Bias.Forward);
        PositionBounds bounds = new PositionBounds(ref1, ref2);

        return new WhereUsedElement(bounds, sb.toString().trim(), fo, name, 
                new OffsetRange(start, end), icon);
    }


    public static WhereUsedElement create(CompilationInfo info, String name, String html, OffsetRange range, Icon icon) {
        FileObject fo = info.getFileObject();
        int start = range.getStart();
        int end = range.getEnd();

        CloneableEditorSupport ces = RetoucheUtils.findCloneableEditorSupport(info);
        PositionRef ref1 = ces.createPositionRef(start, Bias.Forward);
        PositionRef ref2 = ces.createPositionRef(end, Bias.Forward);
        PositionBounds bounds = new PositionBounds(ref1, ref2);

        return new WhereUsedElement(bounds, html, fo, name, 
                new OffsetRange(start, end), icon);
    }
}
