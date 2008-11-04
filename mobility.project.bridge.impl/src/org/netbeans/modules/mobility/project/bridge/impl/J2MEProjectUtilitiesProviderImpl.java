/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.mobility.project.bridge.impl;

import java.io.IOException;
import java.util.Set;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Types;
import javax.swing.text.Document;

import javax.swing.text.StyledDocument;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.editor.BaseDocument;
import org.netbeans.mobility.antext.preprocessor.CommentingPreProcessor.Destination;
import org.netbeans.mobility.antext.preprocessor.CommentingPreProcessor.Source;
import org.openide.filesystems.FileObject;

import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.mobility.project.bridge.J2MEProjectUtilitiesProvider;
import org.netbeans.modules.mobility.project.preprocessor.PPDocumentDestination;
import org.netbeans.modules.mobility.project.preprocessor.PPDocumentSource;

/**
 *
 * @author suchys
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.mobility.project.bridge.J2MEProjectUtilitiesProvider.class)
public class J2MEProjectUtilitiesProviderImpl implements J2MEProjectUtilitiesProvider {


    public FileObject getFileObjectForDocument(Document doc) {
        return NbEditorUtilities.getFileObject(doc);
    }

    public boolean isBaseDocument(StyledDocument doc) {
        if (!(doc instanceof BaseDocument)) 
            return false;
        else 
            return true;
    }

    public Source createPPDocumentSource(StyledDocument doc) {
        return new PPDocumentSource(doc);
    }

    public Destination createPPDocumentDestination(StyledDocument doc) {
        return new PPDocumentDestination((BaseDocument)doc);
    }

    public boolean isFileObjectMIDlet(final FileObject root, final FileObject file, ClassPath boot) {
        final Boolean[] result = new Boolean[]{false};
        ClassPath rtm2 = ClassPath.getClassPath(root, ClassPath.EXECUTE);
        ClassPath rtm1 = ClassPath.getClassPath(root, ClassPath.COMPILE);
        ClassPath rtm = org.netbeans.spi.java.classpath.support.ClassPathSupport.createProxyClassPath(new ClassPath[]{rtm1, rtm2});
        final ClassPath clp =  org.netbeans.spi.java.classpath.support.ClassPathSupport.createProxyClassPath(
                new ClassPath[]{ClassPath.getClassPath(root, ClassPath.SOURCE), rtm1});

        ClasspathInfo cpInfo = ClasspathInfo.create(boot, rtm, clp);
        JavaSource js = JavaSource.create(cpInfo);
        try {

            js.runUserActionTask(new CancellableTask<CompilationController>() {

                public void run(CompilationController control) throws Exception {
                    TypeElement type = control.getElements().getTypeElement("javax.microedition.midlet.MIDlet");
                    if (type == null) {
                        return;
                    }
                    String name = clp.getResourceName(file);
                    if (name == null)
                        return;
                    name = name.substring(0, name.lastIndexOf('.')).replace('/', '.');
                    TypeElement xtype = control.getElements().getTypeElement(name);
                    if (xtype == null) {
                        return;
                    }
                    Set<Modifier> modifiers = xtype.getModifiers();
                    if (modifiers.contains(Modifier.ABSTRACT) || !modifiers.contains(Modifier.PUBLIC) ){
                        return;
                    }
                    Types types = control.getTypes();
                    result[0] = types.isSubtype(types.erasure(xtype.asType()), types.erasure(type.asType()));
                }

                public void cancel() {
                }
            }, true);
        } catch (IOException ioe) {
        }
        
        return result[0];
    }
}
