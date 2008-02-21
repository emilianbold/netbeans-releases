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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javascript.editing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.fpi.gsf.ElementKind;
import org.netbeans.fpi.gsf.Modifier;
import org.netbeans.fpi.gsf.OffsetRange;
import org.netbeans.fpi.gsf.ParserFile;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.javascript.editing.lexer.LexUtilities;
import org.netbeans.sfpi.gsf.DefaultParserFile;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Tor Norbye
 */
public class IndexedFunction implements FunctionElement {
    private ElementKind kind;
    protected String signature;
    private String[] args;
    private String name;
    private String in;
    private List<String> parameters;
    private JsIndex index;
    protected String fileUrl;
    private Document document;
    private FileObject fileObject;
    private int flags;
    private String attributes;
    private EnumSet<BrowserVersion> compatibility;
    
    IndexedFunction(String name, String in, JsIndex index, String fileUrl, String attributes, int flags, ElementKind kind) {
        this.name = name;
        this.in = in;
        this.index = index;
        this.fileUrl = fileUrl;
        this.attributes = attributes;
        this.flags = flags;
        this.kind = kind;
    }
    
    @Override
    public String toString() {
        return getSignature() + ":" + getFilenameUrl();
    }

    public JsIndex getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }


    public String getIn() {
        return in;
    }
    
    public String getSignature() {
        if (signature == null) {
            StringBuilder sb = new StringBuilder();
            if (in != null) {
                sb.append(in);
                sb.append('.');
            }
            sb.append(name);
            sb.append("(");
            List<String> parameterList = getParameters();
            if (parameterList.size() > 0) {
                for (int i = 0, n = parameterList.size(); i < n; i++) {
                    if (i > 0) {
                        sb.append(",");
                    }
                    sb.append(parameterList.get(i));
                }
            }
            sb.append(")");
            signature = sb.toString();
        }

        return signature;
    }

    private int getAttributeSection(int section) {
        assert section != 0; // Obtain directly, and logic below (+1) is wrong
        int attributeIndex = 0;
        for (int i = 0; i < section; i++) {
            attributeIndex = attributes.indexOf(';', attributeIndex+1);
        }
        
        assert attributeIndex != -1;
        return attributeIndex + 1;
    }
    
    public String[] getArgs() {
        if (args == null) {
            int argIndex = getAttributeSection(ARG_INDEX);
            int endIndex = attributes.indexOf(';', argIndex);
            if (endIndex > argIndex) {
                String argsPortion = attributes.substring(argIndex, endIndex);
                args = argsPortion.split(","); // NOI18N
            } else {
                args = new String[0];
            }
        }

        return args;
    }

    public List<String> getParameters() {
        if (parameters == null) {
            String[] a = getArgs();

            if ((a != null) && (a.length > 0)) {
                parameters = new ArrayList<String>(a.length);

                for (String arg : a) {
                    parameters.add(arg);
                }
            } else {
                parameters = Collections.emptyList();
            }
        }

        return parameters;
    }

    public boolean isDeprecated() {
        return false;
    }

    public ElementKind getKind() {
        return kind;
    }

    public Set<Modifier> getModifiers() {
        return Collections.emptySet();
    }

    public String getFilenameUrl() {
        return fileUrl;
    }

    public Document getDocument() throws IOException {
        if (document == null) {
            FileObject fo = getFileObject();

            if (fo == null) {
                return null;
            }

            document = NbUtilities.getBaseDocument(fileObject, true);
        }

        return document;
    }

    public ParserFile getFile() {
        boolean platform = false; // XXX FIND OUT WHAT IT IS!

        return new DefaultParserFile(getFileObject(), null, platform);
    }

    public FileObject getFileObject() {
        if ((fileObject == null) && (fileUrl != null)) {
            fileObject = JsIndex.getFileObject(fileUrl);

            if (fileObject == null) {
                // Don't try again
                fileUrl = null;
            }
        }

        return fileObject;
    }

    List<String> getComments() {
        int docOffsetIndex = getAttributeSection(DOC_INDEX);
        if (docOffsetIndex != -1) {
            int endIndex = attributes.indexOf(';', docOffsetIndex);
            assert endIndex != -1;
            if (endIndex == docOffsetIndex) {
                return null;
            }
            String str = attributes.substring(docOffsetIndex, endIndex);
            try {
                int docOffset = Integer.parseInt(str);
                BaseDocument doc = (BaseDocument) getDocument();
                if (doc == null) {
                    return null;
                }
                if (docOffset < doc.getLength()) {
                    //return LexUtilities.gatherDocumentation(null, doc, docOffset);
                    OffsetRange range = LexUtilities.getCommentBlock(doc, docOffset, false);
                    if (range != OffsetRange.NONE) {
                        String comment = doc.getText(range.getStart(), range.getLength());
                        String[] lines = comment.split("\n");
                        List<String> comments = new ArrayList<String>();
                        for (int i = 0, n = lines.length; i < n; i++) {
                            String line = lines[i];
                            if (i == n-1 && line.trim().endsWith("*/")) {
                                line = line.substring(0,line.length()-2);
                            }
                            if (line.startsWith("/**")) {
                                comments.add(line.substring(3));
                            } else if (line.startsWith("/*")) {
                                comments.add(line.substring(2));
                            } else if (line.startsWith("//")) {
                                comments.add(line.substring(2));
                            } else if (line.startsWith("*")) {
                                comments.add(line.substring(1));
                            } else {
                                comments.add(line);
                            }
                        }
                        return comments;
                    }
                    return Collections.emptyList();
                }
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            } catch (NumberFormatException nfe) {
                Exceptions.printStackTrace(nfe);
                return null;
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
                return null;
            }
        }
            
        return null;
    }
    
    public EnumSet<BrowserVersion> getCompatibility() {
        if (compatibility == null) {
            int flagIndex = getAttributeSection(BROWSER_INDEX);
            if (flagIndex != -1) {
                int endIndex = attributes.indexOf(';', flagIndex);
                assert endIndex != -1;
                if (endIndex == flagIndex) {
                    return BrowserVersion.ALL;
                }
                String compat = attributes.substring(flagIndex, endIndex);
                compatibility = BrowserVersion.fromCompactFlags(compat);
            } else {
                compatibility = BrowserVersion.ALL;
            }
        }
        
        return compatibility;
    }

    private static final int NAME_INDEX = 0;
    private static final int IN_INDEX = 1;
    private static final int ARG_INDEX = 2;
    private static final int DOC_INDEX = 3;
    private static final int BROWSER_INDEX = 4;
}
