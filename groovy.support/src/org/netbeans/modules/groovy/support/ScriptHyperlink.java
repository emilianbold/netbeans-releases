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

package org.netbeans.modules.groovy.support;

import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Annotatable;
import org.openide.text.Annotation;
import org.openide.text.Line;
import org.openide.text.Line.ShowOpenType;
import org.openide.text.Line.ShowVisibilityType;
import org.openide.util.Exceptions;
import org.openide.util.WeakSet;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;

/**
 * Represents a linkable line (appears in red in Output Window).
 * Line and column numbers start at 1, and -1 means an unknown value.
 * Careful since org.openide.text seems to assume 0-based line and column numbers.
 *
 * @author Jesse Glick
 */
public final class ScriptHyperlink extends Annotation implements OutputListener, PropertyChangeListener {
    
    // #14804: detach everything before uninstalling module.
    private static final Set<ScriptHyperlink> hyperlinks = new WeakSet<ScriptHyperlink>(); // Set<Hyperlink>
    public static void detachAllAnnotations() {
        synchronized (hyperlinks) {
            Iterator<ScriptHyperlink> it = hyperlinks.iterator();
            while (it.hasNext()) {
                it.next().destroy();
            }
        }
    }

    private final URL url;
    private final String message;
    private final int line1;
    private int col1;
    private final int line2;
    private final int col2;
    
    private boolean dead = false;
    
    public ScriptHyperlink(URL url, String message, int line1, int col1, int line2, int col2) {
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
    
    void destroy() {
        doDetach();
        dead = true;
    }
    
    public void outputLineAction(OutputEvent ev) {
        if (dead) return;
        FileObject file = URLMapper.findFileObject(url);
        if (file == null) { // #13115
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        try {
            DataObject dob = DataObject.find(file);
            final EditorCookie ed = dob.getCookie(EditorCookie.class);
            if (ed != null && /* not true e.g. for *_ja.properties */file == dob.getPrimaryFile()) {
                if (line1 == -1) {
                    // OK, just open it.
                    ed.open();
                } else {
                    ed.openDocument(); // XXX getLineSet does not do it for you!
                    //AntModule.err.log("opened document for " + file);
                    try {
                        Line l = ed.getLineSet().getOriginal(line1 - 1);
                        if (! l.isDeleted()) {
                            attachAsNeeded(l);
                            if (col1 == -1) {
                                l.show(ShowOpenType.OPEN, ShowVisibilityType.FOCUS);
                            } else {
                                l.show(ShowOpenType.OPEN, ShowVisibilityType.FOCUS, col1 - 1);
                            }
                            //l.show(Line.SHOW_SHOW);
                        }
                    } catch (IndexOutOfBoundsException ioobe) {
                        // Probably harmless. Bogus line number.
                        ed.open();
                    }
                    //moveToFront();
                }
            } else {
                Toolkit.getDefaultToolkit().beep();
            }
        } catch (DataObjectNotFoundException donfe) {
            Exceptions.printStackTrace(donfe);
        } catch (IOException ioe) {
            // XXX see above, should not be necessary to call openDocument at all
            Exceptions.printStackTrace(ioe);
        }
        if (message != null) {
            // Try to do after opening the file, since opening a new file
            // clears the current status message.
            StatusDisplayer.getDefault().setStatusText(message);
        }
    }
    
    public void outputLineSelected(OutputEvent ev) {
        if (dead) return;
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
                    //AntModule.err.log("no document for " + file);
                    return;
                }
                //AntModule.err.log("got document for " + file);
                if (line1 != -1) {
                    Line l = ed.getLineSet().getOriginal(line1 - 1);
                    if (! l.isDeleted()) {
                        attachAsNeeded(l);
                        if (col1 == -1) {
                            l.show(ShowOpenType.NONE, ShowVisibilityType.NONE);
                        } else {
                            l.show(ShowOpenType.NONE, ShowVisibilityType.NONE, col1 - 1);
                        }
                    }
                }
            }
        } catch (DataObjectNotFoundException donfe) {
            Exceptions.printStackTrace(donfe);
        } catch (IndexOutOfBoundsException iobe) {
            // Probably harmless. Bogus line number.
        }
    }
    
    private synchronized void attachAsNeeded(Line l) {
        if (getAttachedAnnotatable() == null) {
            //boolean log = AntModule.err.isLoggable(ErrorManager.INFORMATIONAL);
            Annotatable ann;
            // Text of the line, incl. trailing newline.
            String text = l.getText();
            //if (log) AntModule.err.log("Attaching to line " + l.getDisplayName() + " text=`" + text + "' line1=" + line1 + " line2=" + line2 + " col1=" + col1 + " col2=" + col2);
            if (text != null && (line2 == -1 || line1 == line2) && col1 != -1) {
                int new_col1 = convertTabColumnsToCharacterColumns(text, col1 - 1, 8);
                int new_col2 = convertTabColumnsToCharacterColumns(text, col2 - 1, 8);
                //if (log) AntModule.err.log("\tfits on one line");
                if (new_col2 != -1 && new_col2 >= new_col1 && new_col2 < text.length()) {
                    //if (log) AntModule.err.log("\tspecified section of the line");
                    ann = l.createPart(new_col1, new_col2 - new_col1 + 1);
                } else if (new_col1 < text.length()) {
                    //if (log) AntModule.err.log("\tspecified column to end of line");
                    ann = l.createPart(new_col1, text.length() - new_col1 - 1);
                } else {
                    //if (log) AntModule.err.log("\tcolumn numbers are bogus");
                    ann = l;
                }
            } else {
                //if (log) AntModule.err.log("\tmultiple lines, something wrong with line, or no column given");
                ann = l;
            }
            attach(ann);
            // #17625: detach others however
            Iterator it = hyperlinks.iterator();
            while (it.hasNext()) {
                ScriptHyperlink h = (ScriptHyperlink)it.next();
                if (h != this) {
                    h.doDetach();
                }
            }
            ann.addPropertyChangeListener(this);
        }
    }

    // XXX should be handled in StandardLogger, perhaps?
    private int convertTabColumnsToCharacterColumns(String text, int column, int tabSize) {
        // #16867 - jikes is right now only compiler which reports column of the error.
        // If the text contains 'tab' character, the jikes expects
        // that tab character is defined as 8 spaces, and so it sets the column accordingly.
        // This method converts jikes columns back to character columns
        char[] textChars = text.toCharArray();
        int i;
        int jikes_column = 0;
        for (i=0; i<textChars.length && jikes_column<column; i++) {
            if (textChars[i] == 9) {
                jikes_column += (tabSize-(jikes_column%tabSize));
            } else {
                jikes_column++;
            }
        }
        return i;
    }
    
    private synchronized void doDetach() {
        Annotatable ann = getAttachedAnnotatable();
        if (ann != null) {
            //if (AntModule.err.isLoggable(ErrorManager.INFORMATIONAL)) {
            //    AntModule.err.log("Detaching from " + ann + " `" + ann.getText() + "'");
            //}
            ann.removePropertyChangeListener(this);
            detach();
        }
    }
    
    public void outputLineCleared(OutputEvent ev) {
        doDetach();
    }
    
    public void propertyChange(PropertyChangeEvent ev) {
        if (dead) return;
        String prop = ev.getPropertyName();
        if (prop == null ||
        prop.equals(Annotatable.PROP_TEXT) ||
        prop.equals(Annotatable.PROP_DELETED)) {
            // Affected line has changed.
            // Assume user has edited & corrected the error.
//            if (AntModule.err.isLoggable(ErrorManager.INFORMATIONAL)) {
//                AntModule.err.log("Received Annotatable property change: " + prop);
//            }
            doDetach();
        }
    }
    
    public String getAnnotationType() {
        //return "org-netbeans-modules-coyote-error"; // NOI18N
        return "org-apache-tools-ant-module-error"; // NOI18N
    }
    
    public String getShortDescription() {
        if (message != null) {
            return message;
        } else {
            return null;
        }
    }
    
    @Override
    public String toString() {
        return "Hyperlink[" + url + ":" + line1 + ":" + col1 + ":" + line2 + ":" + col2 + "]"; // NOI18N
    }
    
}
