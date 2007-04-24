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

package org.apache.tools.ant.module.run;

import java.awt.Toolkit;
import java.io.IOException;
import java.net.URL;
import java.util.Set;
import org.apache.tools.ant.module.AntModule;
import org.openide.ErrorManager;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.util.WeakSet;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;

/**
 * Represents a linkable line (appears in red in Output Window).
 * Line and column numbers start at 1, and -1 means an unknown value.
 * Careful since org.openide.text seems to assume 0-based line and column numbers.
 * @author Jesse Glick
 */
public final class Hyperlink implements OutputListener {

    static final Set<Hyperlink> hyperlinks = new WeakSet<Hyperlink>();

    private final URL url;
    private final String message;
    private final int line1;
    private int col1;
    private final int line2;
    private final int col2;
    private Line liveLine;
    
    public Hyperlink(URL url, String message, int line1, int col1, int line2, int col2) {
        this.url = url;
        this.message = message;
        this.line1 = line1;
        this.col1 = col1;
        this.line2 = line2;
        this.col2 = col2;
        synchronized (hyperlinks) {
            hyperlinks.add(this);
        }
    }
    
    /**
     * Enables the column number of the hyperlink to be changed after the fact.
     * If it is already set, this is ignored.
     */
    public void setColumn1(int col1) {
        if (this.col1 == -1) {
            this.col1 = col1;
        }
    }
    
    public void outputLineAction(OutputEvent ev) {
        FileObject file = URLMapper.findFileObject(url);
        if (file == null) { // #13115
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        try {
            DataObject dob = DataObject.find(file);
            EditorCookie ed = dob.getCookie(EditorCookie.class);
            if (ed != null && /* not true e.g. for *_ja.properties */file == dob.getPrimaryFile()) {
                if (line1 == -1) {
                    // OK, just open it.
                    ed.open();
                } else {
                    ed.openDocument(); // XXX getLineSet does not do it for you!
                    AntModule.err.log("opened document for " + file);
                    try {
                        Line line = updateLines(ed);
                        if (!line.isDeleted()) {
                            if (col1 == -1) {
                                line.show(Line.SHOW_REUSE);
                            } else {
                                line.show(Line.SHOW_REUSE, col1 - 1);
                            }
                        }
                    } catch (IndexOutOfBoundsException ioobe) {
                        // Probably harmless. Bogus line number.
                        ed.open();
                    }
                }
            } else {
                Toolkit.getDefaultToolkit().beep();
            }
        } catch (DataObjectNotFoundException donfe) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, donfe);
        } catch (IOException ioe) {
            // XXX see above, should not be necessary to call openDocument at all
            ErrorManager.getDefault().notify(ErrorManager.WARNING, ioe);
        }
        if (message != null) {
            // Try to do after opening the file, since opening a new file
            // clears the current status message.
            StatusDisplayer.getDefault().setStatusText(message);
        }
    }

    /**
     * #62623: record positions in document at time first hyperlink was clicked for this file.
     * Otherwise an intervening save action can mess up line numbers.
     */
    private Line updateLines(EditorCookie ed) {
        Line.Set lineset = ed.getLineSet();
        synchronized (hyperlinks) {
            assert line1 != -1;
            boolean ran = false;
            boolean encounteredThis = false;
            boolean modifiedThis = false;
            if (liveLine == null) {
                ran = true;
                for (Hyperlink h : hyperlinks) {
                    if (h == this) {
                        encounteredThis = true;
                    }
                    if (h.liveLine == null && h.url.equals(url) && h.line1 != -1) {
                        Line l = lineset.getOriginal(h.line1 - 1);
                        assert l != null : h;
                        h.liveLine = l;
                        if (h == this) {
                            modifiedThis = true;
                        }
                    }
                }
            }
            assert liveLine != null : "this=" + this + " ran=" + ran +
                    " encounteredThis=" + encounteredThis + " modifiedThis=" + modifiedThis +
                    " hyperlinks=" + hyperlinks + " hyperlinks.contains(this)=" + hyperlinks.contains(this);
            return liveLine;
        }
    }
    
    public void outputLineSelected(OutputEvent ev) {
        FileObject file = URLMapper.findFileObject(url);
        if (file == null) {
            return;
        }
        try {
            DataObject dob = DataObject.find(file);
            EditorCookie ed = dob.getCookie(EditorCookie.class);
            if (ed != null) {
                if (ed.getDocument() == null) {
                    // The document is not opened, don't bother with it.
                    // The Line.Set will be corrupt anyway, currently.
                    AntModule.err.log("no document for " + file);
                    return;
                }
                AntModule.err.log("got document for " + file);
                if (line1 != -1) {
                    Line line = updateLines(ed);
                    if (!line.isDeleted()) {
                        if (col1 == -1) {
                            line.show(Line.SHOW_TRY_SHOW);
                        } else {
                            line.show(Line.SHOW_TRY_SHOW, col1 - 1);
                        }
                    }
                }
            }
        } catch (DataObjectNotFoundException donfe) {
            ErrorManager.getDefault().notify(ErrorManager.WARNING, donfe);
        } catch (IndexOutOfBoundsException iobe) {
            // Probably harmless. Bogus line number.
        }
    }
    
    public void outputLineCleared(OutputEvent ev) {
        synchronized (hyperlinks) {
            liveLine = null;
        }
    }
    
    @Override
    public String toString() {
        return "Hyperlink[" + url + ":" + line1 + ":" + col1 + ":" + line2 + ":" + col2 + "]"; // NOI18N
    }
    
}
