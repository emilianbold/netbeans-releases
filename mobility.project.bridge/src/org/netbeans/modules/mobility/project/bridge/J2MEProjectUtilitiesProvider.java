/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.mobility.project.bridge;

import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.mobility.antext.preprocessor.CommentingPreProcessor.Destination;
import org.netbeans.mobility.antext.preprocessor.CommentingPreProcessor.Source;
import org.openide.filesystems.FileObject;

/**
 *
 * @author suchys
 * This interface provides isolation between Mobility Project and high level features like Editor, Java etc.
 */
public interface J2MEProjectUtilitiesProvider {

    //### editor bridging
    /**
     * Searches for FileObject belonging Document
     * @param doc Document to look for FileObject for
     * @return the FileObject or null if FileObject could not be found
     */
    public FileObject getFileObjectForDocument(Document doc);

    /**
     * Test if the document is instance of BaseDocument (NetBeans extension of StyledDocument). 
     * This happens when the document is opened in NetBeans editor
     * @param doc the document instance to test
     * @return true if document is instance of StyledDocument
     */
    public boolean isBaseDocument(final StyledDocument doc);
    
    /**
     * Creates pre-processor document source
     * @param doc input document
     * @return CommentingPreProcessor.Source where the document to be pre-processed to be found
     */
    public Source createPPDocumentSource(final StyledDocument doc);
    
    /**
     * Creates pre-processor destination
     * @param doc destination document
     * @return CommentingPreProcessor.Destination where the pre-processed document should be saved
     */
    public Destination createPPDocumentDestination(final StyledDocument doc);
    
    
    //### java bridging
    /**
     * Tests if given file object is instanciable javax.microedition.midlet.MIDlet
     * @param root
     * @param file
     * @return
     */
    public boolean isFileObjectMIDlet(FileObject root, FileObject file, ClassPath bootstrap);
}
