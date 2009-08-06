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

package org.netbeans.modules.cnd.debugger.common;

import java.beans.PropertyChangeListener;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

/** 
 * Defines bridge to editor and src hierarchy. It allows use of different 
 * source viewers for debugger.
 * @author Jan Jancura and Gordon Prieur
 */
public abstract class EditorContext {
    
    public static final String BREAKPOINT_ANNOTATION_TYPE = "Breakpoint"; //NOI18N
    public static final String DISABLED_BREAKPOINT_ANNOTATION_TYPE =  "DisabledBreakpoint"; //NOI18N
    public static final String CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE =  "CondBreakpoint"; //NOI18N
    public static final String DISABLED_CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE =  "DisabledCondBreakpoint"; //NOI18N
    public static final String CURRENT_LINE_ANNOTATION_TYPE =  "CurrentPC"; //NOI18N
    public static final String CALL_STACK_FRAME_ANNOTATION_TYPE =  "CallSite"; //NOI18N
    public static final String PROP_LINE_NUMBER = "lineNumber"; //NOI18N
    public static final String FUNCTION_BREAKPOINT_ANNOTATION_TYPE = "FunctionBreakpoint"; //NOI18N
    public static final String DISABLED_FUNCTION_BREAKPOINT_ANNOTATION_TYPE =  "DisabledFunctionBreakpoint"; //NOI18N
    public static final String CONDITIONAL_FUNCTION_BREAKPOINT_ANNOTATION_TYPE =  "CondFuncBreakpoint"; //NOI18N
    public static final String DISABLED_CONDITIONAL_FUNCTION_BREAKPOINT_ANNOTATION_TYPE =  "DisabledCondFuncBreakpoint"; //NOI18N
    public static final String ADDRESS_BREAKPOINT_ANNOTATION_TYPE = "AddressBreakpoint"; //NOI18N
    public static final String DISABLED_ADDRESS_BREAKPOINT_ANNOTATION_TYPE =  "DisabledAddressBreakpoint"; //NOI18N
    public static final String CONDITIONAL_ADDRESS_BREAKPOINT_ANNOTATION_TYPE =  "CondAddrBreakpoint"; //NOI18N
    public static final String DISABLED_CONDITIONAL_ADDRESS_BREAKPOINT_ANNOTATION_TYPE =  "DisabledCondAddrBreakpoint"; //NOI18N

    /**
     * Shows source with given url on given line number.
     *
     * @param url a url of source to be shown
     * @param lineNumber a number of line to be shown
     * @param timeStamp a time stamp to be used
     */
    public abstract boolean showSource(String url,  int lineNumber, Object timeStamp);
    
    public abstract boolean showSource(DataObject dobj,  int lineNumber, Object timeStamp);

    /**
     * Creates a new time stamp.
     *
     * @param timeStamp a new time stamp
     */
    public abstract void createTimeStamp(Object timeStamp);

    /**
     * Disposes given time stamp.
     *
     * @param timeStamp a time stamp to be disposed
     */
    public abstract void disposeTimeStamp(Object timeStamp);
    
    /**
     * Updates timeStamp for gived url.
     *
     * @param timeStamp time stamp to be updated
     * @param url an url
     */
    public abstract void updateTimeStamp(Object timeStamp, String url);

    /**
     * Adds annotation to given url on given line.
     *
     * @param url a url of source annotation should be set into
     * @param lineNumber a number of line annotation should be set into
     * @param annotationType a type of annotation to be set
     * @param timeStamp a time stamp to be used
     *
     * @return annotation or <code>null</code>, when the annotation can not be
     *         created at the given URL or line number.
     */
    public abstract Object annotate(String url, int lineNumber, String annotationType, Object timeStamp);
    
    public abstract Object annotate(DataObject dobj, int lineNumber, String annotationType, Object timeStamp);

    /**
     * Returns line number given annotation is associated with.
     *
     * @param annotation a annotation
     * @param timeStamp a time stamp to be used
     *
     * @return line number given annotation is associated with
     */
    public abstract int getLineNumber (
        Object annotation,
        Object timeStamp
    );

    /**
     * Removes given annotation.
     */
    public abstract void  removeAnnotation(Object annotation );

    /**
     * Returns number of line currently selected in editor or <code>-1</code>.
     *
     * @return number of line currently selected in editor or <code>-1</code>
     */
    public abstract int getCurrentLineNumber();

    /**
     * Returns number of line most recently selected in editor or <code>-1</code>.
     *
     * @return number of line most recently selected in editor or <code>-1</code>
     */
    public abstract int getMostRecentLineNumber();

    /**
     * Returns URL of source currently selected in editor or empty string.
     *
     * @return URL of source currently selected in editor or empty string
     */
    public abstract String getCurrentURL();

    /**
     * Returns file object of source currently selected in editor or null
     *
     * @return file object of source currently selected in editor or null
     */
    public abstract FileObject getCurrentFileObject();
    
    /**
     *  Return the most recent URL or empty string. The difference between this and getCurrentURL()
     *  is that this one will return a URL when the editor has lost focus.
     *
     *  @return url in string form
     */
    public abstract String getMostRecentURL();

    /**
     * Returns name of function currently selected in editor or empty string.
     *
     * @return name of function currently selected in editor or empty string
     */
    public abstract String getCurrentFunctionName();

    /**
     * Returns method name currently selected in editor or empty string.
     *
     * @return method name currently selected in editor or empty string
     */
    public abstract String getSelectedFunctionName();
    
    /**
     * Returns line number of given field in given class.
     *
     * @param url the url of file the class is deined in
     * @param className the name of class (or innerclass) the field is 
     *                  defined in
     * @param fieldName the name of field
     *
     * @return line number or -1
     */
    public abstract int getFieldLineNumber(String url, String className, String fieldName);
    
    /**
     * Get the MIME type of the current file.
     *
     * @return The MIME type of the current file
     */
    public abstract String getCurrentMIMEType();
    
    public abstract DataObject getCurrentDataObject();

    public abstract DataObject getMostRecentDataObject();

    public abstract FileObject getMostRecentFileObject();
    
    /**
     * Get the MIME type of the most recently selected file.
     *
     * @return The MIME type of the most recent selected file
     */
    public abstract String getMostRecentMIMEType();
    
    /**
     * Adds a property change listener.
     *
     * @param l the listener to add
     */
    public abstract void addPropertyChangeListener(PropertyChangeListener l);
    
    /**
     * Removes a property change listener.
     *
     * @param l the listener to remove
     */
    public abstract void removePropertyChangeListener(PropertyChangeListener l);
    
}
