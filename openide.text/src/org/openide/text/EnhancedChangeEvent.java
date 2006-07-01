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
package org.openide.text;

import javax.swing.event.ChangeEvent;
import javax.swing.text.StyledDocument;


/** Extension of ChangeEvent with additional information
 * about document and the state of the document. This
 * event is fired from CloneableEditorSupport
 *
 * @author  David Konecny, Jaroslav Tulach
 */
final class EnhancedChangeEvent extends ChangeEvent {
    /** Whether document is being closed */
    private boolean closing;

    /** Reference to document */
    private StyledDocument doc;

    public EnhancedChangeEvent(Object source, StyledDocument doc, boolean closing) {
        super(source);
        this.doc = doc;
        this.closing = closing;
    }

    /** Whether document is being closed */
    public boolean isClosingDocument() {
        return closing;
    }

    /** Getter for document */
    public StyledDocument getDocument() {
        return doc;
    }
}
