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
package org.netbeans.modules.mashup.db.ui.wizard;

import java.awt.Toolkit;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import net.java.hulp.i18n.Logger;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import org.netbeans.modules.etl.logger.Localizer;


/**
 * Used in ColumnMetadata table to render scale and precision
 * 
 * @author Jonathan Giron
 * @version $Revision$
 */
public class ColumnSizeTextField extends JTextField {

    private static transient final Logger mLogger = Logger.getLogger(ColumnSizeTextField.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

    /**
     * class IntegerDocument extends a plain document.
     */
    protected class IntegerDocument extends PlainDocument {

        /**
         * method insertString inserts a string into the text field.
         * 
         * @param offs is the offset to insert
         * @param str is the string to insert
         * @param a is the attribute.
         * @throws BadLocationException if the string cannot be inserted.
         */
        public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
            char[] source = str.toCharArray();
            char[] result = new char[source.length];
            int j = 0;

            for (int i = 0; i < result.length; i++) {
                if (Character.isDigit(source[i])) {
                    result[j++] = source[i];
                } else {
                    toolkit.beep();
                    mLogger.infoNoloc(mLoc.t("EDIT073: insertString:{0}" + source[i], getClass().getName()));
                }
            }
            super.insertString(offs, new String(result, 0, j), a);
        }
    }
    private NumberFormat integerFormatter;
    private Toolkit toolkit;

    /**
     * Creates a new instance of IntegerField.
     * 
     * @param columns number of columns used to calculate preferred width
     */
    public ColumnSizeTextField(int columns) {
        super(columns);
        setHorizontalAlignment(SwingConstants.RIGHT);

        toolkit = Toolkit.getDefaultToolkit();
        integerFormatter = NumberFormat.getNumberInstance(Locale.US);
        integerFormatter.setParseIntegerOnly(true);
    }

    /**
     * Creates a new instance of IntegerField.
     * 
     * @param value is the initial value to display
     * @param columns number of columns used to calculate preferred width
     */
    public ColumnSizeTextField(int value, int columns) {
        this(columns);
        setValue(value);
    }

    /**
     * Gets an integer value from the text field.
     * 
     * @return int value retrieved
     */
    public int getValue() {
        int retVal = 0;
        try {
            retVal = integerFormatter.parse(getText()).intValue();
        } catch (ParseException e) {
            toolkit.beep();
        }
        return retVal;
    }

    /**
     * Sets the given integer value into the text field.
     * 
     * @param value is the value to use.
     */
    public void setValue(int value) {
        setText(integerFormatter.format(value));
    }

    /**
     * Creates an IntegerDocument as the default model.
     * 
     * @return Document that is created.
     */
    protected Document createDefaultModel() {
        return new IntegerDocument();
    }
}

