/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
/*
 * ActiveEditorDropCodeClipProvider.java
 *
 * Created on July 27, 2006, 10:16 AM
 *
 * Creates a customized activeEditorDrop type for items like codeclips
 *
 * @author Joelle Lam <joelle.lam@sun.com>
 * @version %I%, %G%
 */

package org.netbeans.modules.visualweb.palette.codeclips;

import java.util.ArrayList;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.openide.text.ActiveEditorDrop;
import org.openide.util.NbBundle;
import org.openide.util.lookup.InstanceContent;

/**
 * <p>Provides an ActiveEditorDropCodeClip of type ActiveEditorDrop.</p>
 *
 * @author Original Author: Libor Kotouc
 * @author Modifier: Joelle Lam <joelle.lam@sun.com>
 */
class ActiveEditorDropCodeClipProvider implements InstanceContent.Convertor {

    private static ActiveEditorDropCodeClipProvider instance = new ActiveEditorDropCodeClipProvider();

    /**
     * Creates a new instance of ActiveEditorDropCodeClipProvider
     */
    private ActiveEditorDropCodeClipProvider() {
    }

    static ActiveEditorDropCodeClipProvider getInstance() {
        return instance;
    }

    /* This method must be modified in order to change the object assigned to the
     * converter. If you do not modify this you will get an isAssignableFrom
     * exception.
     */
    public Class type(Object obj) {
        if (obj instanceof ArrayList)
            return ActiveEditorDrop.class;

        return null;
    }

    public String id(Object obj) {
        return (String)((ArrayList)obj).get(1);
    }

    public String displayName(Object obj) {
        return id(obj);
    }

/*
 *    This is converting the object to a string but the object was
 *    received as if it were the object name.
 *    Although it is not actually the object, this provider just casts the object to a
 *    string
 **/
    public Object convert(Object obj) {

        ArrayList codeclipArray = (ArrayList)obj;

        String bundleName = (String)codeclipArray.get(0);
        String body = (String)codeclipArray.get(1);
        String displayNameKey = (String)codeclipArray.get(2);

        Object drop = getActiveEditorDrop(bundleName, displayNameKey, body);

        return drop;
    }


    private ActiveEditorDrop getActiveEditorDrop(String bundleName, String displayNameKey, String body) {
        ResourceBundle ccbundle;
        String title;
        // I need to do this because I need the title name.
        if (bundleName != null) {

            ccbundle = NbBundle.getBundle(bundleName);
            try {
                title = ccbundle.getString(displayNameKey);
            } catch (MissingResourceException mre ){
                title = displayNameKey;
            }
            body = CodeClipUtilities.fillFromBundle(body, bundleName);
        } else {
            title = displayNameKey;
        }

        ActiveEditorDropCodeClip drop = new ActiveEditorDropCodeClip(title, body);
        return drop;
    }

    private static class ActiveEditorDropCodeClip implements ActiveEditorDrop {

        String body;
        String title;

        public ActiveEditorDropCodeClip(String title, String body) {
            this.body = body;
            this.title = title;
        }

        public boolean handleTransfer(JTextComponent targetComponent) {
            if (targetComponent == null)
                return false;

            String finalBody = CodeClipUtilities.parseClipForParams(title, body);

            try {
                Document doc = targetComponent.getDocument();
                Caret caret = targetComponent.getCaret();
                int p0 = Math.min(caret.getDot(), caret.getMark());
                int p1 = Math.max(caret.getDot(), caret.getMark());
                doc.remove(p0, p1 - p0);
                
                //replace selected text by the inserted one
                int start = caret.getDot();
                doc.insertString(start, finalBody, null);
            } catch (BadLocationException ble) {
                return false;
            }
            
            return true;
        }
    }
    
}
