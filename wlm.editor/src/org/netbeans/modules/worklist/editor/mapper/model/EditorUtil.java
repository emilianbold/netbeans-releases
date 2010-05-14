/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.worklist.editor.mapper.model;

import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.text.StyledDocument;

import org.w3c.dom.Element;

import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.Reference;
import org.netbeans.modules.xml.xam.Referenceable;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.wlm.model.api.VariableDeclaration;

/**
 * TODO: This class is copied from WLM Editor API module.
 * A refactoring required for code reusage.
 *
 * @author Nikita Krjukov
 * @version 1.0
 */
public class EditorUtil {

    private EditorUtil() {}
    
    public static void goToReferenceSource(Reference<Referenceable> reference) {
        Referenceable referenceable = reference.get();
        if (referenceable == null) return;
        if (!(referenceable instanceof DocumentComponent)) return;
        goToDocumentComponentSource((DocumentComponent<DocumentComponent>) referenceable);
    }

    public static boolean canGoToDocumentComponentSource(
            DocumentComponent<DocumentComponent> component)
    {
        if (component == null) return false;

        Model model = component.getModel();
        if (model == null) return false;

        ModelSource modelSource = model.getModelSource();
        if (modelSource == null) return false;

        Lookup lookup = modelSource.getLookup();
        if (lookup == null) return false;

        FileObject fileObject = lookup.lookup(FileObject.class);
        if (fileObject == null) return false;

        DataObject dataObject = null;
        try {
            dataObject = DataObject.find(fileObject);
        } catch (DataObjectNotFoundException e) {}
        if (dataObject == null) return false;

        LineCookie lineCookie = dataObject.getCookie(LineCookie.class);
        if (lineCookie == null) return false;

        Line.Set lineSet = lineCookie.getLineSet();
        if (lineSet == null) return false;

        StyledDocument document = lookup.lookup(StyledDocument.class);
        if (document == null) return false;

        Line line = null;
        int column = 0;

        try {
            int pos = component.findPosition();
            line = lineSet.getCurrent(NbDocument.findLineNumber(document, pos));
            column = NbDocument.findLineColumn(document, pos);
        } catch (IndexOutOfBoundsException e) {}

        if (line == null) {
            try {
                line = lineCookie.getLineSet().getCurrent(0);
            } catch (IndexOutOfBoundsException e) {}
        }
        if (line == null) return false;

        return true;
    }

    public static void goToDocumentComponentSource(DocumentComponent<DocumentComponent> component) {
        if (component == null) return;

        Model model = component.getModel();
        if (model == null) return;

        ModelSource modelSource = model.getModelSource();
        if (modelSource == null) return;

        Lookup lookup = modelSource.getLookup();
        if (lookup == null) return;

        FileObject fileObject = lookup.lookup(FileObject.class);
        if (fileObject == null) return;

        DataObject dataObject = null;
        try {
            dataObject = DataObject.find(fileObject);
        } catch (DataObjectNotFoundException e) {}
        if (dataObject == null) return;

        LineCookie lineCookie = dataObject.getCookie(LineCookie.class);
        if (lineCookie == null) return;

        Line.Set lineSet = lineCookie.getLineSet();
        if (lineSet == null) return;

        StyledDocument document = lookup.lookup(StyledDocument.class);
        if (document == null) return;

        Line line = null;
        int column = 0;

        try {
            int pos = component.findPosition();
            line = lineSet.getCurrent(NbDocument.findLineNumber(document, pos));
            column = NbDocument.findLineColumn(document, pos);
        } catch (IndexOutOfBoundsException e) {}

        if (line == null) {
            try {
                line = lineCookie.getLineSet().getCurrent(0);
            } catch (IndexOutOfBoundsException e) {}
        }
        if (line == null) return;

        final Line fLine = line;
        final int fColumn = column;

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // was: fLine.show(Line.SHOW_GOTO, fColumn);
                if (fLine != null) {
                    fLine.show(Line.ShowOpenType.OPEN, Line.ShowVisibilityType
                            .FOCUS, fColumn);
                }
            }
        });
    }

    public static final String getCorrectedHtmlRenderedString(String htmlString) {
        if (htmlString == null) {
            return null;
        }
        htmlString = htmlString.replaceAll("&amp;","&"); // NOI18n
        htmlString = htmlString.replaceAll("&gt;",">;"); // NOI18n
        htmlString = htmlString.replaceAll("&lt;","<"); // NOI18n

        htmlString = htmlString.replaceAll("&","&amp;"); // NOI18n
        htmlString = htmlString.replaceAll(">","&gt;"); // NOI18n
        htmlString = htmlString.replaceAll("<","&lt;"); // NOI18n
        return htmlString;
    }

    public static String getTagName(DocumentComponent component ) {
        if (component == null) {
            return null;
        }

        Element enEl = component.getPeer();
        return enEl == null ? null : enEl.getTagName();
    }

    public static Reference getVariableType(VariableDeclaration variable) {
        NamedComponentReference ref = variable.getTypeRef();
        return ref;
    }

    public static SchemaComponent getVariableSchemaType(
            VariableDeclaration variable) {
        //
        NamedComponentReference ref = variable.getTypeRef();
        if (ref != null) {
            Referenceable obj = ref.get();
            if (obj instanceof SchemaComponent) {
                return SchemaComponent.class.cast(obj);
            }
        }
        return null;
    }

    public static SchemaComponent getPartType(Part part) {
        NamedComponentReference<GlobalElement> elemRef = part.getElement();
        if (elemRef != null) {
            GlobalElement gElem = elemRef.get();
            if (gElem != null) {
                return gElem;
            }
        }
        //
        NamedComponentReference<GlobalType> typeRef = part.getType();
        if (typeRef != null) {
            GlobalType gType = typeRef.get();
            if (gType != null) {
                return gType;
            }
        }
        //
        return null;
    }

    public static boolean equals(ResultItem item1, ResultItem item2){
        if (item1 == item2){
            return true;
        }
        if ( !item1.getDescription().equals(item2.getDescription())) {
            return false;
        }
        
        if ( !item1.getType().equals(item2.getType())) {
            return false;
        }
        return item1.getComponents() == item2.getComponents();
    }

    private static boolean contains(List<ResultItem> list, ResultItem resultItem) {
        for (ResultItem item: list) {
            if (equals(item, resultItem)){
                return true;
            }
        }
        return false;
    }

    public static String getAccentedString(String message) {
        return "<html><b>" + message + "</b></html>";// NOI18N
    }
    
    public static final String ENTITY_SEPARATOR = "."; // NOI18N
}
