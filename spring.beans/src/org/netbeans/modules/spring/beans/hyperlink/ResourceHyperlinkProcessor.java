/*
 * Copyright 2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.netbeans.modules.spring.beans.hyperlink;

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbBundle;

/**
 *
 * @author Rohan Ranade (Rohan.Ranade@Sun.COM)
 */
public class ResourceHyperlinkProcessor implements HyperlinkProcessor {

    public ResourceHyperlinkProcessor() {
    }

    public void process(HyperlinkEnv env) {
        FileObject fo = NbEditorUtilities.getFileObject(env.getDocument());
        FileObject parent = fo.getParent();

        String key = "goto_resource_not_found"; // NOI18N
        String msg = NbBundle.getBundle(ResourceHyperlinkProcessor.class).getString(key);

        FileObject targetFO = parent.getFileObject(env.getValueString());
        if (targetFO != null) {
            try {
                DataObject dObj = DataObject.find(targetFO);
                EditorCookie editorCookie = dObj.getCookie(EditorCookie.class);
                if (editorCookie != null) {
                    editorCookie.open();
                } else {
                    StatusDisplayer.getDefault().setStatusText(MessageFormat.format(msg, new Object[]{env.getValueString()}));
                }
            } catch (DataObjectNotFoundException ex) {
                Logger.getLogger("global").log(Level.SEVERE, ex.getMessage(), ex);
            }
        } else {
            StatusDisplayer.getDefault().setStatusText(MessageFormat.format(msg, new Object[]{env.getValueString()}));
        }
    }
}
