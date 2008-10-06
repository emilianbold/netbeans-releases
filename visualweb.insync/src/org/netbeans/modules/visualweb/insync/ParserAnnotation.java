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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.visualweb.insync;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;
import org.openide.filesystems.FileObject;
import org.openide.text.Annotatable;
import org.openide.text.Annotation;
import org.openide.text.Line;
import org.openide.text.Line.Set;

/**
 * A source error; annotation which can be attached to a particular line in a source document.
 * <p>Based on some similar code in the java module.
 */
public class ParserAnnotation extends Annotation implements PropertyChangeListener {

    public static final ParserAnnotation[] EMPTY_ARRAY = {};

    private FileObject fobj;
    private final String message;
    private final int line;
    private final int column;
    private Line docline;
    private ParserAnnotation chained;
    private Icon icon;

    /**
     * Creates a new annotation.
     *
     * @param message The error message produced by the compiler/parser
     * @param dataObject The data object this error is associated with
     * @param line The line number where the error occurred
     * @param column The column number on the line where the error occurred
     */
    public ParserAnnotation(String message, FileObject fobj, int line, int column) {
        this.fobj = fobj;
        this.message = message;
        this.line = line;
        this.column = column;
    }

    /**
     * Annotation type, which allows the editor to look up annotation type information (defined in
     * the layer file) such as the icon and highlight type to use for this annotation. See
     * parser_annotation.xml for the definition of this type.
     */
    public String getAnnotationType() {
        return "source-error"; // NOI18N
    }

    /**
     * Tooltip shown for this annotation. If this annotation is chained, return a tooltip showing
     * the descriptions for all the chained annotations.
     *
     * @return the parser error message
     */
    public String getShortDescription() {
        // Localize this with NbBundle:
        if (chained != null)
            return message + "\n\n" + chained.getShortDescription();
        return message;
    }

    /**
     * Return the data object whose source file this error is associated with
     *
     * @return the data object associated with this error
     */
    public FileObject getFileObject() {
        return fobj;
    }

    /**
     * Return the line number where the error occurred
     *
     * @return the line number - 1-based.
     */
    public int getLine() {
        return line;
    }

    /**
     * Return the column number where the error occurred.
     *
     * @return the column number, or 0 if none is known.
     */
    public int getColumn() {
        return column;
    }

    /**
     * Return the error message reported by the compiler/parser
     *
     * @return the localized error message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Return a suitable icon for this error
     *
     * @return an icon
     */
    public Icon getIcon() {
        if (icon == null) {
            java.awt.Image image = ImageUtilities.loadImage("org/netbeans/modules/visualweb/insync/error-glyph.gif");
            if (image != null)
                icon = image instanceof Icon ? (Icon)image : new ImageIcon(image);
        }
        return icon;
    }

    /**
     * Chaining allows multiple annotations to appear on the same line using a single annotation -
     * with a combined tooltip. This is useful since especially with java, it's not unlikely to
     * encounter many errors on the same line, and you don't want a separate glyph/underline for
     * each one, instead we have a single one.
     */
    public void chain(ParserAnnotation anno) {
        if (chained != null)
            chained.chain(anno);
        else
            chained = anno;
    }

    /**
     * Whenever the annotation is attached to the editor, listen for line edits so we can detach the
     * annotation.
     */
    protected void notifyAttached(final Annotatable toAnno) {
        super.notifyAttached(toAnno);
        docline.addPropertyChangeListener(this);
    }

    /**
     * Whenever the annotation is detached from the editor we can stop listening for line edits.
     */
    protected void notifyDetached(Annotatable fromAnno) {
        super.notifyDetached(fromAnno);
        docline.removePropertyChangeListener(this);
    }

    /**
     *  Only underline the part of the line that has text on it.
     */
    public void attachToLineSet(Set lines) {
        docline = lines.getCurrent(line-1);
        char[] string = docline.getText().toCharArray();
        int start = 0;
        int end = string.length-1;
        while (start <= end && string[start] <= ' ')
            start++;
        while (start <= end && string[end] <= ' ')
            end--;

        // XXX shouldn't we use the column??? But if so, chaining
        // gets trickier...
        Line.Part part;
        if (start <= end)
            part = docline.createPart(start, end-start+1);
        else
            part = docline.createPart(0, string.length);

        attach(part);
    }

    /**
     * When the line containing the annotation is edited, remove the annotation immediately.
     */
    public void propertyChange(PropertyChangeEvent ev) {
        String type = ev.getPropertyName();
        if (type == null || type == Annotatable.PROP_TEXT) {
            // User edited the line, assume error should be cleared.
            detach();
        }
    }

    public String toString() {
        return "ParserAnnotation[" + fobj + "," + line + ":" + message + "]";
    }
}
