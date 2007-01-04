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

package org.netbeans.modules.cnd.builds;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.beans.PropertyEditorSupport;
import java.util.Collection;
import java.util.HashSet;
import org.netbeans.modules.cnd.settings.MakeSettings;

/**
 *  Provide a reasonable method for users to change the default property editor
 *  for ErrorExpressions.
 */
public class ErrorExpressionEditor extends PropertyEditorSupport {

    /** shared list of error expressions in the system */
    private static Collection sharedList;

    static {
	sharedList = new HashSet();
	sharedList.add(MakeSettings.SUN_COMPILERS);
	sharedList.add(MakeSettings.GNU_COMPILERS);
    }


    /** list to use for error expressions */
    private Collection list;


    /** value to edit */
    private ErrorExpression value;


    /**
     *  Constructs property editor with shared array of registered expressions.
     */
    public ErrorExpressionEditor() {
	this(sharedList);
    }


    /**
     *  Constructs property editor given list of ErrorExpression. This list will be
     * presented to the user when the editor is used. Also the list is modified when
     * user adds a new ErrorExpression.
     *
     * @param list modifiable collection of <CODE>ErrorExpression</CODE>s
     */
    public ErrorExpressionEditor(Collection list) {
	this.list = list;
    }


    public Object getValue() {
	return value;
    }


    public void setValue(Object value) {
	synchronized (this) {
	    this.value = (ErrorExpression) value;
	    list.add(value);
	}
	firePropertyChange();
    }

    public String getAsText() {
	return "";//value.getName(); // FIXUP - TRUNK - THP
    }


    public void setAsText(String string) {
	ErrorExpression[] exprs = getExpressions();

	for (int i = 0; i < exprs.length; i++) {
	    /* // FIXUP - TRUNK - THP
	    if (string.equals(exprs[i].getName())) {
		setValue(exprs[i]);
		break;
	    }
	    */ // FIXUP - TRUNK - THP
	}
    }


    public String getJavaInitializationString() {
	return "new ErrorExpression (" + // NOI18N
	       //value.getName() + ", " + // NOI18N // FIXUP - TRUNK - THP
	       //value.getErrorExpression() + ", " + // NOI18N // FIXUP - TRUNK - THP
	       //value.getFilePos() + ", " + // NOI18N // FIXUP - TRUNK - THP
	       //value.getLinePos() + ", " + // NOI18N // FIXUP - TRUNK - THP
	       //value.getColumnPos() + ", " + // NOI18N // FIXUP - TRUNK - THP
	       //value.getDescriptionPos() + // FIXUP - TRUNK - THP
	       ")"; // NOI18N
    }


    public String[] getTags() {
	ErrorExpression[] exprs = getExpressions();
	String[] tags = new String [exprs.length];

	/* // FIXUP - TRUNK - THP
	for (int i = 0; i < exprs.length; i++) {
	    tags[i] = exprs[i].getName();
	}
	*/ // FIXUP - TRUNK - THP

	return tags;
    }


    public boolean isPaintable() {
	return false;
    }


    public void paintValue(Graphics g, Rectangle rectangle) {
    }


    public boolean supportsCustomEditor() {
	return true;
    }


    public Component getCustomEditor() {
	return new ErrorExpressionPanel(this);
    }


    synchronized ErrorExpression[] getExpressions() {
	return (ErrorExpression[]) list.toArray(new ErrorExpression[list.size()]);
    }


    Collection getExpressionsVector() {
	return list;
    }
}

