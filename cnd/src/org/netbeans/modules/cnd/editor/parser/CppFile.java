/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.editor.parser;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;

import org.openide.ErrorManager;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;

public class CppFile {

    private static final ErrorManager log =
		ErrorManager.getDefault().getInstance("CppFoldTracer"); // NOI18N

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
    private long state;

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
    // TODO: now we support only the first set of includes
    // needs List to support all
    private CppFoldRecord includesFoldRecord;

    /** record of class/struct/union definition fold information */
//    private ArrayList/*<CppFoldRecord>*/ classFoldRecords;

    /** record of function/method/class/#ifdef/comments fold information */
    private List/*<CppFoldRecord>*/ blockFoldRecords;
    

    public CppFile(String filename) {
	//file = new File(filename);
	state = PARSING_INITIALIZED;
	//this.filename = filename;

//	classFoldRecords = new ArrayList();
	blockFoldRecords = new ArrayList();
    }

    private int parseCount = 0;
    private synchronized int getCount() {
        return ++parseCount;
    }
    
    public void startParsing(Document doc) {
//        int curCount = getCount();
//        System.out.println("CppFile.startParsing: Parsing " + curCount);
        log.log("CppFile.startParsing: Parsing " + getShortName(doc) +
                " [" + Thread.currentThread().getName() + "]"); // NOI18N
        state = PARSING_STARTED;
        //this.doc = doc;
        
        try {
            startParsing(Integer.getInteger("CppFoldFlags", 0).intValue(), doc); // NOI18N
            state = FOLD_PARSING_COMPLETE;
        } catch (NoSuchMethodError er) {
            log.log("CppFile.startParsing: NoSuchMethodError: " + er.getMessage());
        } catch (UnsatisfiedLinkError ule) {
            log.log("CppFile.startParsing: UnsatisfiedLinkError: " + ule.getMessage());
        } finally {
            if (state == PARSING_STARTED) {
                state = PARSING_FAILED;
            } else {
                state = PARSING_COMPLETED;
            }
//            System.out.println("CppFile.startParsing: Finished " + curCount);            
        }
    }
        
    public void startParsing(int flags, Document doc) {
        FoldingParser p = (FoldingParser)Lookup.getDefault().lookup(FoldingParser.class);
        if (p != null) {
//            classFoldRecords.clear();
            blockFoldRecords.clear();
            initialCommentFoldRecord = null;
            includesFoldRecord = null;
            List/**/ folds = null;
            try {
                String name = (String) doc.getProperty(Document.TitleProperty);
                folds = p.parse(name, new StringReader(doc.getText(0, doc.getLength())));
            } catch (BadLocationException ex) {
                assert true;
                ex.printStackTrace();
                return;
            }
            
            for (int i = 0; i < folds.size(); i++) {
                CppFoldRecord fold = (CppFoldRecord) folds.get(i);
                addNewFold((StyledDocument)doc, fold);  
            }
        }
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
    
    public CppFoldRecord getIncludesFold() {
        return includesFoldRecord;
    }
    
    public List getBlockFolds() {
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
                        if (includesFoldRecord == null) {
                            includesFoldRecord = fold;
                        }
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
                log.log("CppFile.addNewFold: Skipping fold record on line " + startLine);
            }
        } catch (IndexOutOfBoundsException ex) {
            // fold was created for old size of document => skip the problem
        }
    }    

}
