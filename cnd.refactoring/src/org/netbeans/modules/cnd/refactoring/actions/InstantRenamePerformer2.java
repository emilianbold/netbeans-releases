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
package org.netbeans.modules.cnd.refactoring.actions;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import javax.swing.event.CaretEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.Position.Bias;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.util.swing.MutablePositionRegion;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceRepository;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceResolver;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.refactoring.api.ui.RefactoringActionsFactory;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * perform instant rename action
 * 
 * @author Jan Lahoda
 * @author Vladimir Voskresensky
 */
public class InstantRenamePerformer2 implements DocumentListener, KeyListener {

    private SyncDocumentRegion region;
    private Document doc;
    private JTextComponent target;
    private int span;
    
    private AttributeSet attribs = null;
    private AttributeSet attribsLeft = null;
    private AttributeSet attribsRight = null;
    private AttributeSet attribsMiddle = null;
    private AttributeSet attribsAll = null;
    
    /** Creates a new instance of InstantRenamePerformer */
    private InstantRenamePerformer2(JTextComponent target,  Collection<CsmReference> highlights, int caretOffset) throws BadLocationException {
	this.target = target;
	this.doc = target.getDocument();
	
	MutablePositionRegion mainRegion = null;
	List<MutablePositionRegion> regions = new ArrayList<MutablePositionRegion>();
        
	for (CsmReference h : highlights) {
	    Position start = NbDocument.createPosition(doc, h.getStartOffset(), Bias.Backward);
	    Position end = NbDocument.createPosition(doc, h.getEndOffset(), Bias.Forward);
	    MutablePositionRegion current = new MutablePositionRegion(start, end);
	    
	    if (isIn(current, caretOffset)) {
		mainRegion = current;
	    } else {
		regions.add(current);
	    }
	    
	}
	
	if (mainRegion == null) {
	    throw new IllegalArgumentException("No highlight contains the caret."); // NOI18N
	}
	
	regions.add(0, mainRegion);
	
	this.region = new SyncDocumentRegion(doc, regions);
	
        if (doc instanceof BaseDocument) {
            ((BaseDocument) doc).addPostModificationDocumentListener(this);
        }
        
	target.addKeyListener(this);
	
	target.putClientProperty(InstantRenamePerformer2.class, this);
	
        requestRepaint();
        
        target.select(mainRegion.getStartOffset(), mainRegion.getEndOffset());
        
        span = region.getFirstRegionLength();
    }
    
    private static String getString(String key) {
        return NbBundle.getMessage(InstantRenamePerformer2.class, key);
    }
    
    public static void invokeInstantRename(JTextComponent target) {
        try {
            final int caret = target.getCaretPosition();   
            Document doc = target.getDocument();
            DataObject dobj = NbEditorUtilities.getDataObject(doc);
            CsmFile file = CsmUtilities.getCsmFile(dobj, false);
            if (file == null) {
                Utilities.setStatusBoldText(target, getString("no-instant-rename")); // NOI18N
                return;
            }
            CsmReference ref = CsmReferenceResolver.getDefault().findReference(file, caret);
            if (ref == null) {
                Utilities.setStatusBoldText(target, getString("no-instant-rename")); // NOI18N
                return;
            }
            
            if (allowInstantRename(ref)) {
                Collection<CsmReference> changePoints = computeChangePoints(ref);
                //String ident = ref.getText();
                doInstantRename(changePoints, target, caret);
            } else {
                doFullRename(dobj, target);
            }
        } catch (BadLocationException e) {
            Exceptions.printStackTrace(e);
        }
    }
    
    private static boolean allowInstantRename(CsmReference ref) {
        CsmReferenceResolver.Scope scope = CsmReferenceResolver.getDefault().fastCheckScope(ref);
        if (scope == CsmReferenceResolver.Scope.LOCAL) {
            return true;
        } else {
            return false;
        }
    }
    
    private static void doFullRename(DataObject dobj, JTextComponent target) {
        EditorCookie ec = dobj.getCookie(EditorCookie.class);
        Node n = dobj.getNodeDelegate();
        if (n == null) {
             Utilities.setStatusBoldText(target, getString("no-instant-rename")); // NOI18N
             return;
        }
        InstanceContent ic = new InstanceContent();
        if (ec != null) {
            ic.add(ec);
        }
        ic.add(n);
        Lookup actionContext = new AbstractLookup(ic);
        
        Action a = RefactoringActionsFactory.renameAction().createContextAwareInstance(actionContext);
        a.actionPerformed(RefactoringActionsFactory.DEFAULT_EVENT);
    }
    
    private static void doInstantRename(Collection<CsmReference> changePoints, JTextComponent target, int caret) throws BadLocationException {
        performInstantRename(target, changePoints, caret);
    }
    
    static Collection<CsmReference> computeChangePoints(CsmReference ref) {
        CsmObject resolved = ref.getReferencedObject();
        if (resolved == null) {
            return Collections.<CsmReference>emptyList();
        }
        CsmFile file = ref.getContainingFile();
        Collection<CsmReference> out = CsmReferenceRepository.getDefault().getReferences(resolved, file, CsmReferenceKind.ALL, null);
        return out;
    }
    
    public static void performInstantRename(JTextComponent target, Collection<CsmReference> highlights, int caretOffset) throws BadLocationException {
	new InstantRenamePerformer2(target, highlights, caretOffset);
    }

    private boolean isIn(MutablePositionRegion region, int caretOffset) {
	return region.getStartOffset() <= caretOffset && caretOffset <= region.getEndOffset();
    }
    
    private boolean inSync;
    
    public synchronized void insertUpdate(DocumentEvent e) {
	if (inSync)
	    return ;
        
        //check for modifications outside the first region:
        if (e.getOffset() < region.getFirstRegionStartOffset() || (e.getOffset() + e.getLength()) > region.getFirstRegionEndOffset()) {
            release();
            return;
        }
        
	inSync = true;
	region.sync(0);
        span = region.getFirstRegionLength();
//        getHighlightsBag(doc).setHighlights(bag);
	inSync = false;
	
        requestRepaint();
    }

    public synchronized void removeUpdate(DocumentEvent e) {
	if (inSync)
	    return ;
        
        if (e.getLength() == 1) {
            if ((e.getOffset() < region.getFirstRegionStartOffset() || e.getOffset() > region.getFirstRegionEndOffset())) {
                release();
                return;
            }

            if (e.getOffset() == region.getFirstRegionStartOffset() && region.getFirstRegionLength() > 0 && region.getFirstRegionLength() == span) {
//                if (LOG.isLoggable(Level.FINE)) {
//                    LOG.fine("e.getOffset()=" + e.getOffset());
//                    LOG.fine("region.getFirstRegionStartOffset()=" + region.getFirstRegionStartOffset());
//                    LOG.fine("region.getFirstRegionEndOffset()=" + region.getFirstRegionEndOffset());
//                    LOG.fine("span= " + span);
//                }
//                JavaDeleteCharAction jdca = (JavaDeleteCharAction) target.getClientProperty(JavaDeleteCharAction.class);
//                
//                if (jdca != null && !jdca.getNextChar()) {
//                    undo();
//                } else {
//                    release();
//                }
                
                return;
            }
            
            if (e.getOffset() == region.getFirstRegionEndOffset() && region.getFirstRegionLength() > 0 && region.getFirstRegionLength() == span) {
//                if (LOG.isLoggable(Level.FINE)) {
//                    LOG.fine("e.getOffset()=" + e.getOffset());
//                    LOG.fine("region.getFirstRegionStartOffset()=" + region.getFirstRegionStartOffset());
//                    LOG.fine("region.getFirstRegionEndOffset()=" + region.getFirstRegionEndOffset());
//                    LOG.fine("span= " + span);
//                }
            //XXX: moves the caret anyway:
//                JavaDeleteCharAction jdca = (JavaDeleteCharAction) target.getClientProperty(JavaDeleteCharAction.class);
//
//                if (jdca != null && jdca.getNextChar()) {
//                    undo();
//                } else {
                    release();
//                }

                return;
            }
        } else {
            //selection/multiple characters removed:
            int removeSpan = e.getLength() + region.getFirstRegionLength();
            
            if (span < removeSpan) {
                release();
                return;
            }
        }
        
        //#89997: do not sync the regions for the "remove" part of replace selection,
        //as the consequent insert may use incorrect offset, and the regions will be synced
        //after the insert anyway.
        if (doc.getProperty(BaseKit.DOC_REPLACE_SELECTION_PROPERTY) != null) {
            return ;
        }
        
	inSync = true;
	region.sync(0);
        span = region.getFirstRegionLength();
//        getHighlightsBag(doc).setHighlights(bag);
	inSync = false;
        
	requestRepaint();
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
	target.putClientProperty(InstantRenamePerformer2.class, null);
        if (doc instanceof BaseDocument) {
            ((BaseDocument) doc).removePostModificationDocumentListener(this);
        }
	target.removeKeyListener(this);
//        getHighlightsBag(doc).clear();
	target = null;
        attribs = null;
	region = null;
        requestRepaint();
	doc = null;
    }

    private void requestRepaint() {
        if (region == null) {
            OffsetsBag bag = getHighlightsBag(doc);
            bag.clear();
        } else {
            // Compute attributes
            if (attribs == null) {
                attribs = getSyncedTextBlocksHighlight();
                Color foreground = (Color) attribs.getAttribute(StyleConstants.Foreground);
                Color background = (Color) attribs.getAttribute(StyleConstants.Background);
                attribsLeft = AttributesUtilities.createImmutable(
                        StyleConstants.Background, background,
                        EditorStyleConstants.LeftBorderLineColor, foreground, 
                        EditorStyleConstants.TopBorderLineColor, foreground, 
                        EditorStyleConstants.BottomBorderLineColor, foreground
                );
                attribsRight = AttributesUtilities.createImmutable(
                        StyleConstants.Background, background,
                        EditorStyleConstants.RightBorderLineColor, foreground, 
                        EditorStyleConstants.TopBorderLineColor, foreground, 
                        EditorStyleConstants.BottomBorderLineColor, foreground
                );
                attribsMiddle = AttributesUtilities.createImmutable(
                        StyleConstants.Background, background,
                        EditorStyleConstants.TopBorderLineColor, foreground, 
                        EditorStyleConstants.BottomBorderLineColor, foreground
                );
                attribsAll = AttributesUtilities.createImmutable(
                        StyleConstants.Background, background,
                        EditorStyleConstants.LeftBorderLineColor, foreground, 
                        EditorStyleConstants.RightBorderLineColor, foreground,
                        EditorStyleConstants.TopBorderLineColor, foreground, 
                        EditorStyleConstants.BottomBorderLineColor, foreground
                );
            }
            
            OffsetsBag nue = new OffsetsBag(doc);
            int startOffset = region.getFirstRegionStartOffset();
            int endOffset = region.getFirstRegionEndOffset();
            int size = region.getFirstRegionLength();
            if (size == 1) {
                nue.addHighlight(startOffset, endOffset, attribsAll);
            } else if (size > 1) {
                nue.addHighlight(startOffset, startOffset + 1, attribsLeft);
                nue.addHighlight(endOffset - 1, endOffset, attribsRight);
                if (size > 2) {
                    nue.addHighlight(startOffset + 1, endOffset - 1, attribsMiddle);
                }
            }

            OffsetsBag bag = getHighlightsBag(doc);
            bag.setHighlights(nue);
        }
    }
    
    private static final AttributeSet defaultSyncedTextBlocksHighlight = AttributesUtilities.createImmutable(StyleConstants.Foreground, Color.red);
    
    private static AttributeSet getSyncedTextBlocksHighlight() {
        FontColorSettings fcs = MimeLookup.getLookup(MimePath.EMPTY).lookup(FontColorSettings.class);
        AttributeSet as = fcs != null ? fcs.getFontColors("synchronized-text-blocks-ext") : null; //NOI18N
        return as == null ? defaultSyncedTextBlocksHighlight : as;
    }
    
    public static OffsetsBag getHighlightsBag(Document doc) {
        OffsetsBag bag = (OffsetsBag) doc.getProperty(InstantRenamePerformer2.class);
        if (bag == null) {
            doc.putProperty(InstantRenamePerformer2.class, bag = new OffsetsBag(doc));
        }
        return bag;
    }
    
}
