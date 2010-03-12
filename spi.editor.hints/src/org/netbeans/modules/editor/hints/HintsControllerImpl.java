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
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.LazyFixList;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.EditorSupport;
import org.openide.text.NbDocument;
import org.openide.text.PositionBounds;
import org.openide.text.PositionRef;

/**
 *
 * @author Jan Lahoda
 */
public final class HintsControllerImpl {
    
    private HintsControllerImpl() {}
    
    public static void setErrors(Document doc, String layer, Collection<? extends ErrorDescription> errors) {
        DataObject od = (DataObject) doc.getProperty(Document.StreamDescriptionProperty);

        if (od == null)
            return ;
        
        try {
            setErrorsImpl(od.getPrimaryFile(), layer, errors);
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
    }
    
    public static void setErrors(FileObject file, String layer, Collection<? extends ErrorDescription> errors) {
        try {
            setErrorsImpl(file, layer, errors);
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
    }
    
    private static void setErrorsImpl(FileObject file, String layer, Collection<? extends ErrorDescription> errors) throws IOException {
        AnnotationHolder holder = AnnotationHolder.getInstance(file);
        
        if (holder != null) {
            holder.setErrorDescriptions(layer,errors);
        }
    }
    
    private static void computeLineSpan(Document doc, int[] offsets) throws BadLocationException {
        String text = doc.getText(offsets[0], offsets[1] - offsets[0]);
        int column = 0;
        int length = text.length();
        
        while (column < text.length() && Character.isWhitespace(text.charAt(column))) {
            column++;
        }
        
        while (length > 0 && Character.isWhitespace(text.charAt(length - 1)))
            length--;
        
        offsets[1]  = offsets[0] + length;
        offsets[0] += column;
        
        if (offsets[1] < offsets[0]) {
            //may happen on lines without non-whitespace characters
            offsets[0] = offsets[1];
        }
    }
    
    static int[] computeLineSpan(Document doc, int lineNumber) throws BadLocationException {
        int lineStartOffset = NbDocument.findLineOffset((StyledDocument) doc, lineNumber - 1);
        int lineEndOffset;
        
        if (doc instanceof BaseDocument) {
            lineEndOffset = Utilities.getRowEnd((BaseDocument) doc, lineStartOffset);
        } else {
            //XXX: performance:
            String lineText = doc.getText(lineStartOffset, doc.getLength() - lineStartOffset);
            
            lineText = lineText.indexOf('\n') != (-1) ? lineText.substring(0, lineText.indexOf('\n')) : lineText;
            lineEndOffset = lineStartOffset + lineText.length();
        }
        
        int[] span = new int[] {lineStartOffset, lineEndOffset};
        
        computeLineSpan(doc, span);
        
        return span;
    }
    
    public static PositionBounds fullLine(Document doc, int lineNumber) {
        DataObject file = (DataObject) doc.getProperty(Document.StreamDescriptionProperty);
        
        if (file == null)
            return null;
        
        try {
            int[] span = computeLineSpan(doc, lineNumber);
            
            return linePart(file.getPrimaryFile(), span[0], span[1]);
        } catch (BadLocationException e) {
            ErrorManager.getDefault().notify(e);
            return null;
        }
    }

    public static PositionBounds linePart(Document doc, final Position start, final Position end) {
        DataObject od = (DataObject) doc.getProperty(Document.StreamDescriptionProperty);
        
        if (od == null)
            return null;
        
        EditorCookie ec = od.getCookie(EditorCookie.class);
        
        if (ec instanceof CloneableEditorSupport) {
            final CloneableEditorSupport ces = (CloneableEditorSupport) ec;
            
            final PositionRef[] refs = new PositionRef[2];
            
            doc.render(new Runnable() {
                public void run() {
                    checkOffsetsAndLog(start.getOffset(), end.getOffset());

                    refs[0] = ces.createPositionRef(start.getOffset(), Position.Bias.Forward);
                    refs[1] = ces.createPositionRef(end.getOffset(), Position.Bias.Backward);
                }
            });
            
            return new PositionBounds(refs[0], refs[1]);
        }
        
        if (ec instanceof EditorSupport) {
            final EditorSupport es = (EditorSupport) ec;
            
            final PositionRef[] refs = new PositionRef[2];
            
            doc.render(new Runnable() {
                public void run() {
                    checkOffsetsAndLog(start.getOffset(), end.getOffset());

                    refs[0] = es.createPositionRef(start.getOffset(), Position.Bias.Forward);
                    refs[1] = es.createPositionRef(end.getOffset(), Position.Bias.Backward);
                }
            });
            
            return new PositionBounds(refs[0], refs[1]);
        }
        
        return null;
    }

    public static PositionBounds linePart(FileObject file, int start, int end) {
        try {
            DataObject od = DataObject.find(file);
            
            if (od == null)
                return null;
            
            EditorCookie ec = od.getCookie(EditorCookie.class);
            
            if (!(ec instanceof CloneableEditorSupport)) {
                return null;
            }
            
            final CloneableEditorSupport ces = (CloneableEditorSupport) ec;
            
            checkOffsetsAndLog(start, end);
            
            return new PositionBounds(ces.createPositionRef(start, Position.Bias.Forward), ces.createPositionRef(end, Position.Bias.Backward));
        } catch (IOException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            return null;
        }
    }
    
    private static void checkOffsetsAndLog(int start, int end) {
        if (start <= end) {
            return ;
        }
        
        Logger.getLogger(HintsControllerImpl.class.getName()).log(Level.INFO, "Incorrect span, please attach your messages.log to issue #112566. start=" + start + ", end=" + end, new Exception());
    }

    private static List<ChangeListener> listeners = new ArrayList<ChangeListener>();
    
    public static synchronized void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }
    
    public static synchronized void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }

    private static final Map<Fix, Iterable<? extends Fix>> fix2Subfixes = new WeakHashMap<Fix, Iterable<? extends Fix>>();

    public static void attachSubfixes(Fix fix, Iterable<? extends Fix> subfixes) {
        fix2Subfixes.put(fix, subfixes);
    }

    public static Iterable<? extends Fix> getSubfixes(Fix fix) {
        Iterable<? extends Fix> ret = fix2Subfixes.get(fix);

        return ret != null ? ret : Collections.<Fix>emptyList();
    }
    
    public static class CompoundLazyFixList implements LazyFixList, PropertyChangeListener {
        
        final List<LazyFixList> delegates;
        
        private List<Fix> fixesCache;
        private Boolean computedCache;
        private Boolean probablyContainsFixesCache;
        
        private PropertyChangeSupport pcs;
        
        public CompoundLazyFixList(List<LazyFixList> delegates) {
            this.delegates = delegates;
            this.pcs = new PropertyChangeSupport(this);
            
            for (LazyFixList l : delegates) {
                l.addPropertyChangeListener(this);
            }
        }
        
        public void addPropertyChangeListener(PropertyChangeListener l) {
            pcs.addPropertyChangeListener(l);
        }

        public void removePropertyChangeListener(PropertyChangeListener l) {
            pcs.removePropertyChangeListener(l);
        }

        public synchronized boolean probablyContainsFixes() {
            if (probablyContainsFixesCache == null) {
                boolean result = false;
                
                for (LazyFixList l : delegates) {
                    result |= l.probablyContainsFixes();
                }
                
                probablyContainsFixesCache = Boolean.valueOf(result);
            }
            
            return probablyContainsFixesCache;
        }

        public synchronized List<Fix> getFixes() {
            if (fixesCache == null) {
                fixesCache = new ArrayList<Fix>();
                
                for (LazyFixList l : delegates) {
                    fixesCache.addAll(l.getFixes());
                }
            }
            
            return fixesCache;
        }

        public synchronized boolean isComputed() {
            if (computedCache == null) {
                boolean result = true;
                
                for (LazyFixList l : delegates) {
                    result &= l.isComputed();
                }
                
                computedCache = Boolean.valueOf(result);
            }
            
            return computedCache;
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (PROP_FIXES.equals(evt.getPropertyName())) {
                synchronized (this) {
                    fixesCache = null;
                }
                pcs.firePropertyChange(PROP_FIXES, null, null);
                return;
            }
                
            if (PROP_COMPUTED.equals(evt.getPropertyName())) {
                synchronized (this) {
                    computedCache = null;
                }
                pcs.firePropertyChange(PROP_COMPUTED, null, null);
            }
        }
        
    }
    
}
