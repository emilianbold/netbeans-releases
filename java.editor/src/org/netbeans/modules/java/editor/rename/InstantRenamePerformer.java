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
package org.netbeans.modules.java.editor.rename;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.swing.event.CaretEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.Position.Bias;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Coloring;
import org.netbeans.lib.editor.codetemplates.SyncDocumentRegion;
import org.netbeans.lib.editor.util.swing.MutablePositionRegion;
import org.netbeans.lib.editor.util.swing.PositionRegion;
import org.netbeans.modules.editor.highlights.spi.Highlight;
import org.netbeans.modules.editor.highlights.spi.Highlighter;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.NbDocument;

/**
 *
 * @author Jan Lahoda
 */
public class InstantRenamePerformer implements DocumentListener, KeyListener {
    
    private SyncDocumentRegion region;
    private Document doc;
    private JTextComponent target;
    private MutablePositionRegion mainRegion;
    
    /** Creates a new instance of InstantRenamePerformer */
    private InstantRenamePerformer(JTextComponent target, Set<Highlight> highlights, int caretOffset) throws BadLocationException {
	this.target = target;
	doc = target.getDocument();
	
	MutablePositionRegion mainRegion = null;
	List<MutablePositionRegion> regions = new ArrayList<MutablePositionRegion>();
	List<Highlight> newHighlights = new ArrayList<Highlight>();
	
	for (Highlight h : highlights) {
	    Position start = NbDocument.createPosition(doc, h.getStart(), Bias.Backward);
	    Position end = NbDocument.createPosition(doc, h.getEnd(), Bias.Forward);
	    MutablePositionRegion current = new MutablePositionRegion(start, end);
	    
	    if (isIn(current, caretOffset)) {
		mainRegion = current;
	    } else {
		regions.add(current);
	    }
	    
	    newHighlights.add(new RenameHighlight(current));
	}
	
	if (mainRegion == null) {
	    throw new IllegalArgumentException("No highlight contains the caret.");
	}
	
	regions.add(0, mainRegion);
	
	region = new SyncDocumentRegion(doc, regions);
	
        if (doc instanceof BaseDocument) {
            ((BaseDocument) doc).setPostModificationDocumentListener(this);
        }
        
	target.addKeyListener(this);
	
	target.putClientProperty(InstantRenamePerformer.class, this);
	
	Highlighter.getDefault().setHighlights(getFileObject(), "instant-rename", newHighlights);
        
        target.select(mainRegion.getStartOffset(), mainRegion.getEndOffset());
    }
    
    private FileObject getFileObject() {
	DataObject od = (DataObject) doc.getProperty(Document.StreamDescriptionProperty);
	
	if (od == null)
	    return null;
	
	return od.getPrimaryFile();
    }
    
    public static void performInstantRename(JTextComponent target, Set<Highlight> highlights, int caretOffset) throws BadLocationException {
	new InstantRenamePerformer(target, highlights, caretOffset);
    }

    private boolean isIn(MutablePositionRegion region, int caretOffset) {
	return region.getStartOffset() <= caretOffset && caretOffset <= region.getEndOffset();
    }
    
    private boolean inSync;
    
    public synchronized void insertUpdate(DocumentEvent e) {
	if (inSync)
	    return ;
	
	inSync = true;
	region.sync(0);
	inSync = false;
	target.repaint();
    }

    public synchronized void removeUpdate(DocumentEvent e) {
	if (inSync)
	    return ;
	
	inSync = true;
	region.sync(0);
	inSync = false;
	target.repaint();
    }

    public void changedUpdate(DocumentEvent e) {
    }

    public void caretUpdate(CaretEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
	if (   (e.getKeyCode() == KeyEvent.VK_ESCAPE && e.getModifiers() == 0) 
            || (e.getKeyCode() == KeyEvent.VK_ENTER  && e.getModifiers() == 0)) {
	    release();
	    e.consume();
	}
    }

    public void keyReleased(KeyEvent e) {
    }

    private void release() {
	target.putClientProperty(InstantRenamePerformer.class, null);
        if (doc instanceof BaseDocument) {
            ((BaseDocument) doc).setPostModificationDocumentListener(null);
        }
	target.removeKeyListener(this);
	Highlighter.getDefault().setHighlights(getFileObject(), "instant-rename", Collections.emptyList());

	region = null;
	doc = null;
	target = null;
	mainRegion = null;
    }

    private static final class RenameHighlight implements Highlight {

	private PositionRegion region;
	private static final Coloring COLORING = new Coloring(null, null, new Color(138, 191, 236));
	
	public RenameHighlight(PositionRegion region) {
	    this.region = region;
	}
	
        public int getStart() {
	    return region.getStartOffset();
        }

        public int getEnd() {
	    return region.getEndOffset();
        }

        public Coloring getColoring() {
	    return COLORING;
        }
    }
    
}
