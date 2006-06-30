/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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
