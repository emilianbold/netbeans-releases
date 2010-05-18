/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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


package org.netbeans.modules.uml.ui.controls.editcontrol;

import java.awt.Font;

import org.netbeans.modules.uml.core.eventframework.IEventDispatcher;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import java.awt.Color;

public interface IEditControl
{
	/**
	 *
	*/
	public void setAutoSize(boolean value);

	/**
	 *
	*/
	public boolean getAutoSize();

	/**
	 *
	*/
	public void setrefFont(Font value);

	/**
	 *
	*/
	public void setFont(Font value);

	/**
	 * 
	*/
	public Font getFont();

	/**
	 * Sets the control's font via an HFONT (in-process only)
	*/
	public void setHFont(/* long */
	int value);

	/**
	 * 
	*/
	public void getBackColor();

	/**
	 * 
	*/
	public void setrefBackColor(long value);

	/**
	 * 
	*/
	public void setBackColor(long value);

	/**
	 * 
	*/
	public Color getForeColor();

	/**
	 * 
	*/
	public void setForeColor(Color value);

	/**
	 * 
	*/
	public void setrefForeColor(long value);

	/**
	 * 
	*/
	public String getText();

	/**
	 * 
	*/
	public void setText(String value);

	/**
	 * Sets input focus to the control, optionally passing a keycode to be used as the first input character.
	*/
	public void activate(int KeyCode, int nPos);

	/**
	 * Terminates input focus to the control.
	*/
	public void deactivate();

	/**
	 * Gets/Sets the control's translator
	*/
	public ITranslator getTranslator();

	/**
	 * Gets/Sets the control's translator
	*/
	public void setTranslator(ITranslator value);

	/**
	 * Gets if the edit control is a multiline edit
	*/
	public boolean isMultiline();

	/**
	 * Gets/Sets the range of selected characters
	*/
	public long getSel(int nStartChar, int nEndChar);

	/**
	 * Gets/Sets the range of selected characters
	*/
	public long setSel(int nStartChar, int nEndChar);

	/**
	 * Replaces the selected range with text
	*/
	public void replaceSel(String sText);

	/**
	 * Establishes the control's event dispatcher
	*/
	public void putEventDispatcher(IEventDispatcher pDispatcher);

	/**
	 * Establishes the control's event dispatcher
	*/
	public IEventDispatcher getEventDispatcher();

	/**
	 * Sets/Returns the control's modified status.
	*/
	public boolean getModified();

	/**
	 * Sets/Returns the control's modified status.
	*/
	public void setModified(boolean value);

	/**
	 * Returns the length of the current or specified line in the edit window
	*/
	public int lineLength(int nLine);

	/**
	 * Returns the character index for the current or specified line.
	*/
	public int lineIndex(int nLine);

	/**
	 * Gets/Sets the Edit Style of the control.
	*/
	public int getStyle();

	/**
	 * Gets/Sets the Edit Style of the control.
	*/
	public void setStyle(long value);

	/**
	 * Forces a text read from the attached translator.
	*/
	public void refresh();

	/**
	 * Gets/Sets the overstrike mode of the control.
	*/
	public boolean getOverstrike();

	/**
	 * Gets/Sets the overstrike mode of the control.
	*/
	public void setOverstrike(boolean value);

	/**
	 * The text for the tooltip.  The subject portion is highlighted.
	*/
	public long getTooltipText(StringBuffer sLeft, StringBuffer sSubject, StringBuffer sRight);

	/**
	 * The text for the tooltip.  The subject portion is highlighted.
	*/
	public void setTooltipText(String sLeft, String sSubject, String sRight);

	/**
	 * Enable/disable the popup tooltip when the edit control has focus.
	*/
	public void setEnableTooltip(boolean value);

	/**
	 * Reload the tooltip's data.
	*/
	public void updateToolTip();

	/**
	 * Get/Sets the logical input position (does not update the currently selected text.
	*/
	public void setCurrentPosition(int value);

	/**
	 * Get/Sets the logical input position (does not update the currently selected text.
	*/
	public int getCurrentPosition();

	/**
	 * If TRUE the control resizes itself to fit its contents.
	*/
	public boolean getAutoExpand();

	/**
	 * If TRUE the control resizes itself to fit its contents.
	*/
	public void setAutoExpand(boolean value);

	/**
	 * Causes the hintbar to shown at the specified character position.
	*/
	public void showHintBar(int nPos);

	/**
	 * Hides the hintbar.
	*/
	public void hideHintBar();

	/**
	 * Invokes the hint control.
	*/
	public void handleHint();

	/**
	 * Displays a dropdown list or combobox at the specified position.
	*/
	public void displayList(boolean bList, IStrings pList, int nStart, String sInitialText);

	/**
	 * Returns the text field that the caret is currently in.
	*/
	public IEditControlField getCurrentField();

	/**
	 * Registers an accelerator key + control key on behalf of a translator.
	*/
	public void registerAccelerator(int nChar, long nModifier);

	/**
	 * Sets the model element to be edited.
	*/
	public void setElement(IElement value);

	/**
	 * A list of separator characters used by the fields.
	*/
	public String getSeparatorList();

	/**
	 * A list of separator characters used by the fields.
	*/
	public void setSeparatorList(String value);

	/**
	 * Calculates the new caret position when changing lines in a multiline edit
	*/
	public int calcNewPos(int changeLineBy);

	//these two methods are used when we are removing characters from names.
	public int getSelEndPos();
	public int getSelStartPos();

	public boolean isShiftDown();
	public boolean isControlDown();

	/**
	 * Returns the object associated with the edit control, generally the compartment being edited.
	*/
	public Object getAssociatedParent();
}
