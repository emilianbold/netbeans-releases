/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.codemodel.utils;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeFileItemSet;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

/**
 *
 * @author petrk
 */
public class CMUtils {
    
    public static URI getURI(Document doc) {
        FileObject fo = getFileObject(doc);
        return (fo == null) ? null : fo.toURI();
    }    
    
    public static FileObject getFileObject(Document doc) {
        FileObject fo = (FileObject)doc.getProperty(FileObject.class);
        if(fo == null) {
            DataObject dobj = NbEditorUtilities.getDataObject(doc);
            if (dobj != null) {
                fo = dobj.getPrimaryFile();
            }
        }
        return fo;
    }    
        
    public static String getFilePath(Document document) {
        List<String> paths = getFilePaths(getDataObject(document));
        return (paths != null && !paths.isEmpty() ? paths.get(0) : null);
    }
    
    public static Position getPositionFromOffset(Document document, int offset) throws BadLocationException {
        if (offset < 0) {  
            throw new BadLocationException("Can't translate offset to line", -1);  // NOI18N
        } else if (offset > document.getLength()) {  
            throw new BadLocationException("Can't translate offset to line", document.getLength() + 1);  // NOI18N
        } else {  
            Element map = document.getDefaultRootElement();  
            int line = map.getElementIndex(offset);
            int column = offset - map.getElement(line).getStartOffset();
            return new Position(line + 1, column + 1, offset);            
        }
    }
    
    public static Position getPositionFromCoords(Document document, int lineIndex, int colIndex) throws BadLocationException {
        Element map = document.getDefaultRootElement();
        
        if (lineIndex <= 0) {
            throw new BadLocationException("Wrong line", -1);  // NOI18N
        } else if (map.getElementCount() < lineIndex) {
            throw new BadLocationException("Wrong line", document.getLength() + 1);  // NOI18N
        }
        
        int offset = map.getElement(lineIndex - 1).getStartOffset() + (colIndex - 1);
        
        if (offset < 0) {  
            throw new BadLocationException("Wrong column", -1);  // NOI18N
        } else if (offset > document.getLength()) {  
            throw new BadLocationException("Wrong column", document.getLength() + 1);  
        }
        
        return new Position(lineIndex, colIndex, offset);
    }

    public static DataObject getDataObject(JTextComponent component) {
        if (component == null) {
            return null;
        }
        return getDataObject(component.getDocument());
    }
    
    public static DataObject getDataObject(Document document) {
        if (document == null) {
            return null;
        }
        return (DataObject) document.getProperty(Document.StreamDescriptionProperty);
    }    
    
    
    public static class Position {
        
        public final int line;   // one-based line index
        
        public final int column; // one-based column index
        
        public final int offset;
        

        public Position(int line, int column, int offset) {
            this.line = line;
            this.column = column;
            this.offset = offset;
        }

        @Override
        public String toString() {
            return "[" + line + ", " + column + "]"; // NOI18N
        }
    }
    
    
    //<editor-fold defaultstate="collapsed" desc="Private implementation">
    
    private static List<String> getFilePaths(DataObject dobj) {
        if (dobj == null) {
            return null;
        }
        
        NativeFileItemSet set = dobj.getLookup().lookup(NativeFileItemSet.class);
        if (set != null && !set.isEmpty()) {
            List<String> paths = new ArrayList<>();
            
            for (NativeFileItem item : set.getItems()) {
                paths.add(item.getAbsolutePath());
            }
            
            return paths;
        }
        
        if (dobj.getPrimaryFile() != null) {
            return Arrays.asList(dobj.getPrimaryFile().getPath());
        }
        
        return Collections.emptyList();
    }
    
    //</editor-fold>
    
    private CMUtils() {
        throw new AssertionError("Not for instantiation!");  // NOI18N
    }    
}
