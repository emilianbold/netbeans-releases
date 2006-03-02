/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

/*
 * XDMModelUndoableEdit.java
 *
 * Created on August 11, 2005, 2:52 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.modules.xml.xdm;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import org.netbeans.modules.xml.xdm.nodes.Document;


/**
 *
 * @author Chris Webster
 */
class XDMModelUndoableEdit extends AbstractUndoableEdit {
	// Even though AbstractUndoableEdit is serializable this class is not. The
	// UndoableEdit interface is not Serializable, so this is not required but
	// just an implementation detail.

	private static final long serialVersionUID = -4513245871320808368L;

	public XDMModelUndoableEdit(Document oldDoc, Document newDoc, XDMModel model) {
		oldDocument = oldDoc;
		newDocument = newDoc;
		this.model = model;
	}
	
	@Override
	public void redo() throws CannotRedoException {
		super.redo();
        try {
            model.resetDocument(newDocument);
        } catch (java.io.IOException ioe) {
            throw new CannotRedoException();
        }
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
        try {
            model.resetDocument(oldDocument);
        } catch (java.io.IOException ioe) {
            throw new CannotRedoException();
        }
	}
	
	private Document oldDocument;
	private Document newDocument;
	private XDMModel model;
	
}
