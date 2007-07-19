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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.editor;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldUtilities;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.SettingsChangeEvent;
import org.netbeans.editor.SettingsChangeListener;
import org.netbeans.modules.cnd.editor.parser.CppFile;
import org.netbeans.modules.cnd.editor.parser.CppFoldRecord;
import org.netbeans.modules.cnd.editor.parser.CppMetaModel;
import org.netbeans.modules.cnd.editor.parser.ParsingEvent;
import org.netbeans.modules.cnd.editor.parser.ParsingListener;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.spi.editor.fold.FoldHierarchyTransaction;
import org.netbeans.spi.editor.fold.FoldManager;
import org.netbeans.spi.editor.fold.FoldManagerFactory;
import org.netbeans.spi.editor.fold.FoldOperation;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.RequestProcessor;

/**
 *  Fold maintainer/manager for C and C++ (not yet supporting Fortran).
 *  This code is derived from the NetBeans 4.1 versions of the NbJavaFoldManager 
 *  in the java/editor module.
 */

final class CppFoldManager extends CppFoldManagerBase
	implements SettingsChangeListener, Runnable, ParsingListener {

    private FoldOperation operation;

    /** Fold for the initial comment in the file */
    private Fold initialCommentFold;

    /** Fold info for code blocks (functions, classes, comments, #ifdef/endif and compound statements) */
    private final HashMap path2FoldInfo = new HashMap();

    // Folding presets
    private boolean foldIncludesPreset;
    private boolean foldCommentPreset;
    private boolean foldCodeBlocksPreset;
    private boolean foldInitialCommentsPreset;
    
    private boolean documentModified;

    private boolean listeningOnParsing;

    private static RequestProcessor cppFoldsRP;

    private static boolean cppgramLoaded = false;

    private static final Logger log = Logger.getLogger(CppFoldManager.class.getName());

    private CppFoldManager() {	// suppress standard creation
    }

    // Helper methods for awhile...
    private static synchronized RequestProcessor getCppFoldsRP() {
	if (cppFoldsRP == null) {
	    cppFoldsRP = new RequestProcessor("CPP-Folds", 1); // NOI18N
	}
	return cppFoldsRP;
    }

    /**
     *  Get the filename associated with this FileManager. Used (currently) only for debugging.
     *  @returns A String representing the absolute path of file
     */
    private String getFilename() {
	FoldHierarchy h = (operation != null) ? operation.getHierarchy() : null;
	javax.swing.text.JTextComponent comp = (h != null) ? h.getComponent() : null;
	Document doc = (comp != null) ? comp.getDocument() : null;
	DataObject dob = (doc != null) ? NbEditorUtilities.getDataObject(doc) : null;
	String path = (dob != null) ?
		    FileUtil.getFileDisplayName(dob.getPrimaryFile()) : null;
	return path;
    }

    private String getShortName() {
	String longname = (String) getDocument().getProperty(Document.TitleProperty);
	int slash = longname.lastIndexOf(File.separatorChar);

	if (slash != -1) {
	    return longname.substring(slash + 1);
	} else {
	    return longname;
	}
    }

    Fold getInitialCommentFold() {
        return initialCommentFold;
    }
    
    void setInitialCommentFold(Fold initialCommentFold) {
        this.initialCommentFold = initialCommentFold;
    }
    
    private IncludesFoldInfo findIncludesFoldInfo(String id) {
	if (id == null) {
	    return null;
	}
        return (IncludesFoldInfo) path2FoldInfo.get(id);
    }
    
    private void removeIncludesFoldInfo(String id) {
	if (id == null) {
	    return;
	}
        path2FoldInfo.remove(id);
    }
    
    private void putIncludesFoldInfo(String id, IncludesFoldInfo info) {
	if (id == null) {
	    return;
	}
        path2FoldInfo.put(id, info);
    }
    
    private BlockFoldInfo findBlockFoldInfo(String id) {
	if (id == null) {
	    return null;
	}
        return (BlockFoldInfo) path2FoldInfo.get(id);
    }
    
    private void removeBlockFoldInfo(String id) {
	if (id == null) {
	    return;
	}
        path2FoldInfo.remove(id);
    }
    
    private void putBlockFoldInfo(String id, BlockFoldInfo info) {
	if (id == null) {
	    return;
	}
        path2FoldInfo.put(id, info);
    }

    private FoldOperation getOperation() {
	return operation;
    }

    private void removeFoldNotify(Fold fold) {
	log.log(Level.FINE, "CppFoldManager.removeFoldNotify"); // NOI18N
    }

    synchronized private void updateFolds() {
	log.log(Level.FINE, "CFM.updateFolds: Processing " + getShortName() + " [" +
			    Thread.currentThread().getName() + "]"); // NOI18N
	final UpdateFoldsRequest request = collectFoldUpdates();

	//assert Thread.currentThread().getName().equals("CPP-Folds");
	Runnable hierarchyUpdate = new Runnable() {
	    public void run() {
		if (!getOperation().isReleased()) {
		    Document doc = getDocument();
		    if (!(doc instanceof AbstractDocument)) {
			return; // can happen (e.g. after component close)
		    }
		    log.log(Level.FINE, "CFM.updateFolds$X1.run: Processing " + getShortName() + " [" +
				    Thread.currentThread().getName() + "]"); // NOI18N
		    
		    AbstractDocument adoc = (AbstractDocument) doc;
		    adoc.readLock();
		    try {
			FoldHierarchy hierarchy = getOperation().getHierarchy();
			hierarchy.lock();
			try {
			    FoldHierarchyTransaction t = getOperation().openTransaction();
			    try {
				log.log(Level.FINE, "CFM.updateFolds$X1.run: Calling " + // NOI18N
					"processUpdateFoldRequest for " + getShortName() + " [" + // NOI18N
					Thread.currentThread().getName() + "]"); // NOI18N
//                                System.out.println("=========== " + getShortName() + " ===========");
//                                System.out.println(hierarchy.toString());
				processUpdateFoldRequest(request, t);
//                                System.out.println("------------ VV -----------");
//                                System.out.println(hierarchy.toString());
			    } finally {
                                t.commit();
			    }
			} finally {
			    hierarchy.unlock();
			}
		    } finally {
			adoc.readUnlock();
		    }
		}
	    }
	};
	// Do fold updates in AWT
	log.log(Level.FINE, "CFM.updateFolds: Starting update for " + getShortName() + " on AWT thread"); // NOI18N
	SwingUtilities.invokeLater(hierarchyUpdate);
    }

    /** Collect all updates into an update request */
    private UpdateFoldsRequest collectFoldUpdates() {
	log.log(Level.FINE, "CFM.collectFoldUpdates: Processing " + getShortName() +
		    " [" + Thread.currentThread().getName() + "]"); // NOI18N
	UpdateFoldsRequest request = new UpdateFoldsRequest();
	Document doc = getDocument();

	if (getOperation().isReleased() || !(doc instanceof AbstractDocument)) {
	    log.log(Level.FINE, "CFM.collectFoldUpdates: No doc found for " + getShortName()); // NOI18N
	    return request;
	}

	CppFile cpf = (CppFile) CppMetaModel.getDefault().
		    get(doc.getProperty(Document.TitleProperty).toString());
	if (cpf == null) {
	    return request;
	}
	cpf.waitScanFinished(CppFile.FOLD_PARSING);

	AbstractDocument adoc = (AbstractDocument) doc;
	adoc.readLock();

	try {
            // initial comment fold
            request.setInitialCommentFoldInfo(cpf.getInitialCommentFold());
            
	    // The Includes sections
            for (CppFoldRecord rec : cpf.getIncludesFolds()) {
                request.addIncludesFoldInfo(rec);
            }

	    // Functions/methods
	    for (CppFoldRecord rec : cpf.getBlockFolds()) {
		request.addBlockFoldInfo(rec);
	    }
	} finally {
	    adoc.readUnlock();
	}
	return request;
    }

    /** Process the fold updates in the request */
    private void processUpdateFoldRequest(UpdateFoldsRequest request, FoldHierarchyTransaction transaction) {
            Map obsoletePath2FoldInfo = (Map) path2FoldInfo.clone();

	if (request.isValid()) {
	    log.log(Level.FINE, "CFM.processUpdateFoldRequest: Processing " + getShortName() +
		    " [" + Thread.currentThread().getName() + "]"); // NOI18N
	    // Process function/method folds from the request

            // initial comment
            Fold origFold = getInitialCommentFold();
            InitialCommentFoldInfo icInfo = request.getInitialCommentFoldInfo();
            if (icInfo != null) {
                if (icInfo.isUpdateNecessary(origFold)) {
                    boolean collapsed = (origFold != null)
                        ? origFold.isCollapsed() : (documentModified ? false : foldInitialCommentsPreset);
                        
                    // Remove original fold first
                    if (origFold != null) {
                        removeFoldFromHierarchy(origFold, transaction);
                        setInitialCommentFold(null);
                    }
                    
                    // Add new fold
                    try {
                        icInfo.updateHierarchy(transaction, collapsed);
                    } catch (BadLocationException e) {
                        ErrorManager.getDefault().notify(e);
                    }
                }
            } else { // no new fold
                // Remove original fold only
                if (origFold != null) {
                    removeFoldFromHierarchy(origFold, transaction);
                    setInitialCommentFold(null);
                }
            }
            
            // The set of includes
            List<IncludesFoldInfo> impsInfo = request.getIncludesFoldInfos();
            if (impsInfo != null) {
                for (IncludesFoldInfo info : impsInfo) {
                    String id = info.getId();
                    IncludesFoldInfo orig = findIncludesFoldInfo(id);   
                    
                    // Remove original fold first
                    if (orig != null) {
                        orig.removeFromHierarchy(transaction);
                    }

                    // Add new fold
                    try {
                        info.updateHierarchy(transaction, orig);
                    } catch (BadLocationException e) {
                        ErrorManager.getDefault().notify(e);
                    }
                }
            }

            List infoList = request.getBlockFoldInfos();
	    if (infoList != null) {
		for (Iterator it = infoList.iterator(); it.hasNext();) {
                    BlockFoldInfo info = (BlockFoldInfo) it.next();
                    String id = info.getId();
                    BlockFoldInfo orig = findBlockFoldInfo(id);

                    // NB style validation don't work for us as we can't
                    // be sure our info got by findBlockFoldInfo() reflects
                    // folds state
                    //if (info.isUpdateNecessary(orig)) {
                        // Remove original folds first
                        if (orig != null) {
                            orig.removeFromHierarchy(transaction);
                        }

                        // Add the new folds
                        try {
                            info.updateHierarchy(transaction, orig);
                        } catch (BadLocationException e) {
                            ErrorManager.getDefault().notify(e);
                        }
                        // Remember the new info
                        putBlockFoldInfo(id, info);
                    //} 
                    
                    if (orig != null) {
                        // Folds with the particular id already existed
                        // and will continue to exist so they are not obsolete
                        obsoletePath2FoldInfo.remove(id);
                    }
                }

                // Remove the obsolete folds
                for (Iterator it = obsoletePath2FoldInfo.entrySet().iterator(); it.hasNext();) {
                    Map.Entry e = (Map.Entry) it.next();
                    String id = (String) e.getKey();
                    BlockFoldInfo foldInfo = (BlockFoldInfo) e.getValue();
                    removeBlockFoldInfo(id);
//                    System.out.println("***** (2) Before removing " + id);
//                    System.out.println(getOperation().getHierarchy().toString());
//                    System.out.println();
                    foldInfo.removeFromHierarchy(transaction);
                }
            } else {
                path2FoldInfo.clear();
		log.log(Level.FINE, "CFM.processUpdateFoldRequest: infoList is null"); // NOI18N
	    }

	}
    }

    private void removeFoldFromHierarchy(final Fold origFold, final FoldHierarchyTransaction transaction) {
        if ((origFold.getParent() != null) || FoldUtilities.isRootFold(origFold)) {
            getOperation().removeFromHierarchy(origFold, transaction);
        }
    }

    Document getDocument() {
        return getOperation().getHierarchy().getComponent().getDocument();
    }
    
    DataObject getDataObject() {
        Document doc = getDocument();
        return (doc != null) ? NbEditorUtilities.getDataObject(doc) : null;
    }

    // Implement Runnable
    public void run() {
	try {
	    if ((new File(getFilename())).exists()) {
		log.log(Level.FINE, "CFM.run: Processing " + getShortName() +
			    " [" + Thread.currentThread().getName() + "]"); // NOI18N
		if (!listeningOnParsing) {
		    log.log(Level.FINE, "CFM.run: Processing " + getShortName() +
			    " [" + Thread.currentThread().getName() + "]"); // NOI18N
		    listeningOnParsing = true;
		    log.log(Level.FINE, "CFM.run: Starting WeakParsingListener [" +
			    Thread.currentThread().getName() + "]"); // NOI18N
		    new WeakParsingListener(this).startListening();
		}
		log.log(Level.FINE, "CFM.run: Calling updateFolds [" +
			    Thread.currentThread().getName() + "]"); // NOI18N
		updateFolds();
	    }
	} catch (ThreadDeath e) {
	    throw e;
	} catch (Throwable t) {
	    ErrorManager.getDefault().notify(t);
	}
    }


    // Implementing FoldManager...
    /** Initialize this manager */
    public void init(FoldOperation operation) {
	this.operation = operation;
    }

    public void initFolds(FoldHierarchyTransaction transaction) {
	if (getFilename() != null && getFilename().length() > 0) {
	    log.log(Level.FINE, "CFM.initFolds: Posting for " + getShortName() +
		    " on Cpp Folds RP [" + Thread.currentThread().getName() + "]"); // NOI18N
	    getCppFoldsRP().post(this, 1000, Thread.MIN_PRIORITY);
	}
    }
    
    private void scheduleParsing(Document doc)
    {
        // we parse only documents assigned to files on disk        
        // TODO: why above?
        log.log(Level.FINE,"TitleProperty: " + doc.getProperty(Document.TitleProperty));
        if (doc.getProperty(Document.TitleProperty)!=null) {
            CppMetaModel.getDefault().scheduleParsing(doc);
        }
    }

    public void insertUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
        log.log(Level.FINE, "FoldManager.insertUpdate: " + evt.getDocument().toString());
        scheduleParsing(evt.getDocument());
        documentModified = true;
    }
    
    public void removeUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
        log.log(Level.FINE, "FoldManager.removeUpdate");
        scheduleParsing(evt.getDocument());
        documentModified = true;
    }
    
    public void changedUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
        log.log(Level.FINE, "FoldManager.changeUpdate");
//        scheduleParsing(evt.getDocument());
//        documentModified = true;
    }
    
    public void removeEmptyNotify(Fold emptyFold) {
        removeFoldNotify(emptyFold);
    }
    
    public void removeDamagedNotify(Fold damagedFold) {
        removeFoldNotify(damagedFold);
    }
    
    public void expandNotify(Fold expandedFold) {
    }

    public void release() {
    }

    // Implementing ParsingListener
    public void objectParsed(ParsingEvent evt) {
	DataObject dob = (DataObject) evt.getSource();
	String path = getFilename();

	if (dob != null) {
	    FileObject primaryFile = dob.getPrimaryFile();

	    if (primaryFile != null) {
		String pfile = FileUtil.getFileDisplayName(primaryFile);
		if (pfile.equals(path)) {
		    log.log(Level.FINE, "CFM.objectParsed: Calling updateFolds for " + getShortName()); // NOI18N
		    updateFolds();
		} else {
		    log.log(Level.FINE, "CFM.objectParsed: Skipping updateFolds"); // NOI18N
		}
	    }
	}
    }

    public void settingsChange(SettingsChangeEvent evt) {
        // TODO: Get folding presets
//        foldInitialCommentsPreset = getSetting(CCSettingsNames.CODE_FOLDING_COLLAPSE_INITIAL_COMMENT);
//        foldIncludesPreset = getSetting(CCSettingsNames.CODE_FOLDING_COLLAPSE_IMPORT);
//        foldCodeBlocksPreset = getSetting(CCSettingsNames.CODE_FOLDING_COLLAPSE_METHOD);
//        foldInnerClassesPreset = getSetting(CCSettingsNames.CODE_FOLDING_COLLAPSE_INNERCLASS);
//        foldCommentPreset = getSetting(CCSettingsNames.CODE_FOLDING_COLLAPSE_JAVADOC);       
        foldInitialCommentsPreset = false;
        foldIncludesPreset = false;
        foldCodeBlocksPreset = false;
        foldCommentPreset = false;  
    }

    // Worker classes...

    /** Gather update information in this class */
    private final class UpdateFoldsRequest {

	private Document creationTimeDoc;
        
        /** Fold for the initial comment in the file */
        private InitialCommentFoldInfo initialCommentFoldInfo;
        
        /** Folds for includes sections */
        private List<IncludesFoldInfo> includesFoldInfos;

        /** List of the code block folds (methods, functions, compound statements etc.) */
        private List<BlockFoldInfo> blockFoldInfos;
        
        UpdateFoldsRequest() {
            creationTimeDoc = getDocument();
        }
        
        boolean isValid() {
            // Check whether request creation time document
            // is still in use by the fold hierarchy
            return (creationTimeDoc != null && creationTimeDoc == getDocument());
        }
        
        InitialCommentFoldInfo getInitialCommentFoldInfo() {
            return initialCommentFoldInfo;
        }
        
        void setInitialCommentFoldInfo(CppFoldRecord initialCommentFold) {
            BaseDocument bdoc = (BaseDocument)creationTimeDoc;      
            try {
            this.initialCommentFoldInfo = initialCommentFold == null ? null :
                    new InitialCommentFoldInfo(
                    bdoc.createPosition(initialCommentFold.getStartOffset()), 
                    bdoc.createPosition(initialCommentFold.getEndOffset()));
            } catch (BadLocationException ex) {
                // skip it
            }
        }
        
        List getIncludesFoldInfos() {
            return includesFoldInfos;
        }
        
        void addIncludesFoldInfo(CppFoldRecord foldInfo) {
            if (includesFoldInfos == null) {
                includesFoldInfos = new ArrayList();
            }
	    try {
		includesFoldInfos.add(new IncludesFoldInfo(foldInfo, (AbstractDocument) creationTimeDoc));
	    } catch (BadLocationException ex) {
		log.log(Level.FINE, "CFM.addIncludesFoldInfo: Got BadLocationException\n    " + // NOI18N
			 ex.getMessage());
	    }
        }
        
        List getBlockFoldInfos() {
            return blockFoldInfos;
        }
        
        void addBlockFoldInfo(CppFoldRecord foldInfo) {
            if (blockFoldInfos == null) {
                blockFoldInfos = new ArrayList();
            }
	    try {
		blockFoldInfos.add(new BlockFoldInfo(foldInfo, (AbstractDocument) creationTimeDoc));
	    } catch (BadLocationException ex) {
		log.log(Level.FINE, "CFM.addBlockFoldInfo: Got BadLocationException\n    " + // NOI18N
			 ex.getMessage());
	    }
        }
    }

    private final class InitialCommentFoldInfo {
        
        private Position initialCommentStartPos;
        private Position initialCommentEndPos;
        
        InitialCommentFoldInfo(Position initialCommentStartPos, Position initialCommentEndPos) {
            this.initialCommentStartPos = initialCommentStartPos;
            this.initialCommentEndPos = initialCommentEndPos;
        }
        
        public boolean isUpdateNecessary(Fold origInitialCommentFold) {
            return (origInitialCommentFold == null
                || origInitialCommentFold.getStartOffset() != initialCommentStartPos.getOffset()
                || origInitialCommentFold.getEndOffset() != initialCommentEndPos.getOffset()
            );
        }

        public void updateHierarchy(FoldHierarchyTransaction transaction,
			boolean collapsed) throws BadLocationException {
            int startOffset = initialCommentStartPos.getOffset();
            int endOffset = initialCommentEndPos.getOffset();

            if (FoldOperation.isBoundsValid(startOffset, endOffset,
			CppFoldManager.INITIAL_COMMENT_FOLD_TEMPLATE.getStartGuardedLength(),
			CppFoldManager.INITIAL_COMMENT_FOLD_TEMPLATE.getEndGuardedLength())) {
                Fold fold = getOperation().addToHierarchy(
			CppFoldManager.INITIAL_COMMENT_FOLD_TEMPLATE.getType(),
			CppFoldManager.INITIAL_COMMENT_FOLD_TEMPLATE.getDescription(), 
			collapsed,
			startOffset,  endOffset,
			CppFoldManager.INITIAL_COMMENT_FOLD_TEMPLATE.getStartGuardedLength(),
			CppFoldManager.INITIAL_COMMENT_FOLD_TEMPLATE.getEndGuardedLength(),
			this,
			transaction
                );
                setInitialCommentFold(fold);
            }
        }
    }

    private final class CommentFoldInfo {
        
        private Position startPos;
        private Position endPos;
        
        CommentFoldInfo(Position startPos, Position endPos) {
            this.startPos = startPos;
            this.endPos = endPos;
        }
        
        public boolean isUpdateNecessary(Fold origInitialCommentFold) {
            return (origInitialCommentFold == null
                || origInitialCommentFold.getStartOffset() != startPos.getOffset()
                || origInitialCommentFold.getEndOffset() != endPos.getOffset()
            );
        }

        public void updateHierarchy(FoldHierarchyTransaction transaction,
			boolean collapsed) throws BadLocationException {
            int startOffset = startPos.getOffset();
            int endOffset = endPos.getOffset();

            if (FoldOperation.isBoundsValid(startOffset, endOffset,
			CppFoldManager.INITIAL_COMMENT_FOLD_TEMPLATE.getStartGuardedLength(),
			CppFoldManager.INITIAL_COMMENT_FOLD_TEMPLATE.getEndGuardedLength())) {
                Fold fold = getOperation().addToHierarchy(
			CppFoldManager.INITIAL_COMMENT_FOLD_TEMPLATE.getType(),
			CppFoldManager.INITIAL_COMMENT_FOLD_TEMPLATE.getDescription(), 
			collapsed,
			startOffset,  endOffset,
			CppFoldManager.INITIAL_COMMENT_FOLD_TEMPLATE.getStartGuardedLength(),
			CppFoldManager.INITIAL_COMMENT_FOLD_TEMPLATE.getEndGuardedLength(),
			this,
			transaction
                );
            }
        }
    }
    
    private final class IncludesFoldInfo {
        
        private Fold fold;
        private Position includesStartPos;
        private Position includesEndPos;
	private String id;

        IncludesFoldInfo(CppFoldRecord fi, AbstractDocument doc) throws BadLocationException {
            includesStartPos = doc.createPosition(fi.getStartOffset());
            includesEndPos = doc.createPosition(fi.getEndOffset());
            
            // TODO: our lexer don't provide enough information about folds
            // it produces. NB java ids based on MOFID are more reliable.
            id = "" + fi.getType() + ":" + fi.getStartOffset(); // NOI18N
        }

        public boolean isUpdateNecessary(IncludesFoldInfo orig) {
            boolean update = false;
            
            if (orig == null) {
                update = true;
            } else {
                Fold origFold = orig.getFold();
                if (includesStartPos != null && (origFold == null
                            || includesStartPos.getOffset() != origFold.getStartOffset()
                            || includesEndPos.getOffset() != origFold.getEndOffset())) {
                    update = true;
                }
            }
            return update;
        }

        public void updateHierarchy(FoldHierarchyTransaction transaction, IncludesFoldInfo origInfo)
                        throws BadLocationException {
            int startOffset = includesStartPos.getOffset();
            int endOffset = includesEndPos.getOffset();

            if (FoldOperation.isBoundsValid(startOffset, endOffset,
			CppFoldManager.INCLUDES_FOLD_TEMPLATE.getStartGuardedLength(),
			CppFoldManager.INCLUDES_FOLD_TEMPLATE.getEndGuardedLength())) {
                fold = getOperation().addToHierarchy(
                    CppFoldManager.INCLUDES_FOLD_TEMPLATE.getType(),
                    CppFoldManager.INCLUDES_FOLD_TEMPLATE.getDescription(), 
                    false, startOffset,  endOffset,
                    CppFoldManager.INCLUDES_FOLD_TEMPLATE.getStartGuardedLength(),
                    CppFoldManager.INCLUDES_FOLD_TEMPLATE.getEndGuardedLength(),
                    this,
                    transaction
                );
            }
        }

	public String getId() {
	    return id;
	}

	public void removeFromHierarchy(FoldHierarchyTransaction transaction) {
            if (fold != null) {
                FoldOperation fo = getOperation();
                if (fo.isAddedOrBlocked(fold))
                    fo.removeFromHierarchy(fold, transaction);
            }
        }
            
        public Fold getFold() {
            return fold;
        }

	public void removeFoldNotify(Fold removedFold) {
            if (removedFold == fold) {
                fold = null;
            } else {
                assert false; // Invalid fold supplied
            }
        }

	public String toString() {
            return "fold=" + fold; // NOI18N
        }
    }

    private final class BlockFoldInfo {
        
        private Fold fold;
        private FoldTemplate template;
	private String id;
        
        private Position blockStartPos;
        private Position blockEndPos;
        
        public BlockFoldInfo(CppFoldRecord fi, AbstractDocument doc) throws BadLocationException {
            blockStartPos = doc.createPosition(fi.getStartOffset());
            blockEndPos = doc.createPosition(fi.getEndOffset());
            switch (fi.getType()) {
                case CppFoldRecord.FUNCTION_FOLD:
                case CppFoldRecord.CONSTRUCTOR_FOLD:
                case CppFoldRecord.DESTRUCTOR_FOLD:
                case CppFoldRecord.CLASS_FOLD:
                case CppFoldRecord.NAMESPACE_FOLD:
                    template = CODE_BLOCK_FOLD_TEMPLATE;
                    break;
                case CppFoldRecord.BLOCK_COMMENT_FOLD:
                    template = COMMENT_FOLD_TEMPLATE;
                    break;
                case CppFoldRecord.COMMENTS_FOLD:
                    template = LINE_COMMENT_FOLD_TEMPLATE;
                    break;
                case CppFoldRecord.IFDEF_FOLD:
                    template = IFDEF_FOLD_TEMPLATE;
                    break;
                default:
                    assert (false) : "unsupported block type " + fi; // NOI18N
            }
            // TODO: our lexer don't provide enough information about folds
            // it produces. NB java ids based on MOFID are more reliable.
            id = "" + fi.getType() + ":" + fi.getStartOffset(); // NOI18N
	}

	public boolean isUpdateNecessary(BlockFoldInfo orig) {
            boolean update = false;

            if (orig == null) {
                update = true;
            } else { // original info already exists -> compare
		Fold origFold = orig.getFold();
		if (blockStartPos != null && (origFold == null
			|| blockStartPos.getOffset() != origFold.getStartOffset()
			|| blockEndPos.getOffset() != origFold.getEndOffset())) {
		    update = true;
		}
            }
            return update;
        }

	public String getId() {
	    return id;
	}

	public void updateHierarchy(FoldHierarchyTransaction transaction, BlockFoldInfo origInfo)
                        throws BadLocationException {
            
            if (blockStartPos != null) {
                int startOffset = blockStartPos.getOffset();
                int endOffset = blockEndPos.getOffset();

                if (FoldOperation.isBoundsValid(startOffset, endOffset,
			    template.getStartGuardedLength(), template.getEndGuardedLength())) {
                    // Determine whether the fold should be collapsed or expanded
                    Fold origFold;
                    boolean collapsed = false;
                    //	(origInfo != null && (origFold = origInfo.getFold()) != null)
                    //  ? origFold.isCollapsed() : documentModified;
                            
		    log.log(Level.FINE, "CFM.FunctionFoldInfo.updateHierarchy: Creating fold at (" +  // NOI18N
			    startOffset + ", " + endOffset + ")"); // NOI18N
                    this.fold = getOperation().addToHierarchy(
                        template.getType(), template.getDescription(), collapsed,
                        startOffset, endOffset,
                        template.getStartGuardedLength(), template.getEndGuardedLength(),
                        this,
                        transaction
                    );
                }
            } else {
		log.log(Level.FINE, "CFM.FunctionFoldInfo.updateHierarchy: No functionStartPos, skipping"); // NOI18N
	    }
	}

	public void removeFromHierarchy(FoldHierarchyTransaction transaction) {
            if (fold != null) {
                FoldOperation fo = getOperation();
                if (fo.isAddedOrBlocked(fold))
                    fo.removeFromHierarchy(fold, transaction);
            }
        }
            
        public Fold getFold() {
            return fold;
        }

	public void removeFoldNotify(Fold removedFold) {
            if (removedFold == fold) {
                fold = null;
            } else {
                assert false; // Invalid fold supplied
            }
        }

	public String toString() {
            return "fold=" + fold; // NOI18N
        }
    }


    private static final class WeakParsingListener implements ParsingListener {
        
        private WeakReference ref;
        
        WeakParsingListener(ParsingListener listener) {
            ref = new WeakReference(listener);
        }
        
        public void startListening() {
            CppMetaModel.getDefault().addParsingListener(this);
        }
        
        public void objectParsed(ParsingEvent evt) {
            ParsingListener listener = (ParsingListener)ref.get();
            if (listener != null) {
                listener.objectParsed(evt);
            } else {
                CppMetaModel.getDefault().removeParsingListener(this);
            }
        }
    }


    /**
     *  The factory class to create the CppFoldManager. It gets installed via
     *  an entry in the layer file.
     */
    public static final class Factory implements FoldManagerFactory {
        
        public Factory(){
        }
        
        public FoldManager createFoldManager() {
            return new CppFoldManager();
        }
    }
}
