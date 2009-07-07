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
package org.netbeans.modules.cnd.editor.parser;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;

public class CppFile {

    private static final Logger log = Logger.getLogger(CppFile.class.getName());

    // Parsing types
    public final static int FOLD_PARSING = 1;
    public final static int COMPLETION_PARSING = 2;

    // Parsing states
    public final static int PARSING_INITIALIZED = 0;
    public final static int PARSING_STARTED = 1;
    public final static int FOLD_PARSING_COMPLETE = 2;
    public final static int PARSING_COMPLETED = 3;
    public final static int PARSING_FAILED = 4;

    // Fold types
    public final static int INITIAL_COMMENT_FOLD = 1;
    public final static int BLOCK_COMMENT_FOLD = 2;
    public final static int COMMENTS_FOLD = 3;
    public final static int INCLUDES_FOLD = 4;
    public final static int IFDEF_FOLD = 5;
    public final static int CLASS_FOLD = 6;
    public final static int FUNCTION_FOLD = 7;
    public final static int CONSTRUCTOR_FOLD = 8;
    public final static int DESTRUCTOR_FOLD = 9;
    public static final int NAMESPACE_FOLD = 10;
    /** parsing state information */
    private int state;
    /** the file being parsed */
    //private String filename;

    //private File file;

    //private long mtime;
    /** start of file parse. Track for never ending parses */
    //private long parsingStartTime;

    //private Document doc;

    //private int next = 0;
    /** record of initial comment fold information */
    private CppFoldRecord initialCommentFoldRecord;
    /** record of includes block fold information */
    private List<CppFoldRecord> includesFoldRecords = new ArrayList<CppFoldRecord>();
    /** record of class/struct/union definition fold information */
//    private ArrayList/*<CppFoldRecord>*/ classFoldRecords;
    /** record of function/method/class/#ifdef/comments fold information */
    private List<CppFoldRecord> blockFoldRecords;

    public CppFile(String filename) {
        //file = new File(filename);
        state = PARSING_INITIALIZED;
        //this.filename = filename;

//	classFoldRecords = new ArrayList();
        blockFoldRecords = new ArrayList<CppFoldRecord>();
    }
    private int parseCount = 0;

    private synchronized int getCount() {
        return ++parseCount;
    }

    public void startParsing(Document doc) {
//        int curCount = getCount();
//        System.out.println("CppFile.startParsing: Parsing " + curCount);
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "CppFile.startParsing: Parsing " + getShortName(doc) +
                    " [" + Thread.currentThread().getName() + "]"); // NOI18N
        }
        state = PARSING_STARTED;
        //this.doc = doc;

        try {
            if (startParsing(Integer.getInteger("CppFoldFlags", 0).intValue(), doc)) { // NOI18N
                state = FOLD_PARSING_COMPLETE;
            }
        } catch (NoSuchMethodError er) {
            log.log(Level.FINE, "CppFile.startParsing: NoSuchMethodError: " + er.getMessage());
        } catch (UnsatisfiedLinkError ule) {
            log.log(Level.FINE, "CppFile.startParsing: UnsatisfiedLinkError: " + ule.getMessage());
        } finally {
            if (state != FOLD_PARSING_COMPLETE) {
                state = PARSING_FAILED;
            }
//          System.out.println("CppFile.startParsing: Finished " + curCount);            
        }
    }

    public boolean isParsingFailed() {
        return state == PARSING_FAILED;
    }

    private boolean startParsing(int flags, Document doc) {
        FoldingParser p = Lookup.getDefault().lookup(FoldingParser.class);
        if (p != null) {
//            classFoldRecords.clear();
            blockFoldRecords.clear();
            initialCommentFoldRecord = null;
            includesFoldRecords.clear();
            List<CppFoldRecord> folds = null;
            try {
                String name = (String) doc.getProperty(Document.TitleProperty);
                folds = p.parse(name, new StringReader(doc.getText(0, doc.getLength())));
                if (folds == null) {
                    return false;
                }
            } catch (BadLocationException ex) {
                assert true;
                ex.printStackTrace();
                return false;
            }

            for (CppFoldRecord fold : folds) {
                addNewFold((StyledDocument) doc, fold);
            }
        }
        return true;
    }

    public void waitScanFinished(int type) {
        while (state == PARSING_STARTED) {
//            System.out.println("Waiting for scan of CppFile: " + getShortName(doc));
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
            }
        }
    }

    /** Does the CppFile record need updating? */
    public boolean needsUpdate() {
        // in the current model files only asked for needUpdate() if it was
        // modified. Should be changed after folding refactoring
        return true;
    }

    private String getShortName(Document doc) {
        String longname = (String) doc.getProperty(Document.TitleProperty);
        int slash = longname.lastIndexOf(File.separatorChar);

        if (slash != -1) {
            return longname.substring(slash + 1);
        } else {
            return longname;
        }
    }

    /**
     *  Get the next character from the Document. Any exceptions are treated as EOF and
     *  an 0 character is returned. The parser treats this as EOF.
     */
//    public String getCharString() {
//	String s;
//	
//	try {
//	    s = doc.getText(next++, 1);
//	} catch (Exception ex) {
//	    s = "";	// EOF
//	}
//	return s;
//    }
    public CppFoldRecord getInitialCommentFold() {
        return this.initialCommentFoldRecord;
    }

    public List<CppFoldRecord> getIncludesFolds() {
        return includesFoldRecords;
    }

    public List<CppFoldRecord> getBlockFolds() {
        return blockFoldRecords;
    }

//    public ArrayList getClassFolds() {
//	return classFoldRecords;
//    }   
    /**
     *  Note that we don't do folds if '{' and '}' are on the same
     *  line with less than 5 characters between them. We also decrement startOffset by one
     *  to move the offset before the opening brace (otherwise its following the brace).
     */
    private void addNewFold(StyledDocument doc, CppFoldRecord fold) {
        if (log.isLoggable(Level.FINEST)) {
            log.log(Level.FINEST, "CppFile.addNewFold: " + fold.toString());
        }
        int startOffset = fold.getStartOffset();
        int endOffset = fold.getEndOffset();
        try {
            int startLine = NbDocument.findLineNumber(doc, startOffset);
            int endLine = NbDocument.findLineNumber(doc, endOffset);
            if (startLine != endLine || (startOffset > endOffset + 5)) {
                fold.setLines(startLine, endLine);
                switch (fold.getType()) {
                    case INITIAL_COMMENT_FOLD:
                        if (initialCommentFoldRecord == null) {
                            initialCommentFoldRecord = fold;
                        }
                        break;
                    case INCLUDES_FOLD:
                        includesFoldRecords.add(fold);
                        break;

                    case CLASS_FOLD:
                    case NAMESPACE_FOLD:
//                    classFoldRecords.add(fold);
//                    break;
                    case IFDEF_FOLD:
                    case COMMENTS_FOLD:
                    case BLOCK_COMMENT_FOLD:
                    case CONSTRUCTOR_FOLD:
                    case DESTRUCTOR_FOLD:
                    case FUNCTION_FOLD:
                        blockFoldRecords.add(fold);
                        break;
                }
            } else {
                if (log.isLoggable(Level.FINE)) {
                    log.log(Level.FINE, "CppFile.addNewFold: Skipping fold record on line " + startLine);
                }
            }
        } catch (IndexOutOfBoundsException ex) {
            log.log(Level.FINE, "CppFile.addNewFold: fold was created for old size of document - ignored");
        // fold was created for old size of document => skip the problem
        }
    }
}
