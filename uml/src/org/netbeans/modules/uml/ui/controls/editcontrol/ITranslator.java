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

import java.util.Vector;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyDefinition;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElement;

public interface ITranslator
{
	/**
	 * Gets/Sets the Model Element
	*/
	public IElement getElement();

	/**
	 * Gets/Sets the Model Element
	*/
	public void setElement( IElement value );

	/**
	 * Sets/Returns the AxEdit control (if setting, re-initializes the translator).
	*/
	public IEditControl getEditControl();

	/**
	 * Sets/Returns the AxEdit control (if setting, re-initializes the translator).
	*/
	public void setEditControl( IEditControl value );

	/**
	 * Sets the AxEdit control, does not re-initialize.
	*/
	public void setEditControl2( IEditControl value );

	/**
	 * Returns the formatted string value for this translator's Model Element
	*/
	public String getSimple();

	/**
	 * Returns the current formatted string value for this translator (after editing)
	*/
	public String getCurrent();

	/**
	 * Gets a TextFields collection breaking down the translated string into its constituents.
	*/
	public Vector getTextFields();

	/**
	 * Get/Sets the caret position.
	*/
	public int getPosition();

	/**
	 * Get/Sets the caret position.
	*/
	public void setPosition( int value );

	/**
	 * Updates the starting and ending positions within the edit control of the editable text contained by this translator.
	*/
	public int getTextStartPos();
	public int getTextEndPos();

	/**
	 * Returns the starting add ending positions within the edit control of the text contained by this translator.
	*/
	public int getFieldStartPos();
	public int getFieldEndPos();

	/**
	 * Returns the starting position within the edit control of the text contained by this translator.
	*/
	public void setFieldPos( int nStartPosition );

	/**
	 * Sets the current selection.
	*/
	public void setPosition( int nStartPos, int nEndPos );

	/**
	 * Processes KeyDown notifications from the edit control.
	*/
	public boolean handleKeyDown( int nKey );

	/**
	 * Processes KeyUp notifications from the edit control.
	*/
	public boolean handleKeyUp( int nKey );

	/**
	 * Processes character keypress notifications from the edit control.
	*/
	public boolean handleChar( String nChangedChar );

	public boolean handleDelete( boolean deleteRightwards );

	/**
	 * Processes a left mouse button down event.
	*/
	public boolean handleLButtonDown( int nPosition );

	/**
	 * Processes a left mouse button double click event.
	*/
	public boolean handleLButtonDblClk( int nPosition );

	/**
	 * Processes a left mouse button triple click event.
	*/
	public boolean handleLButtonTripleClk( int nPosition );

	/**
	 * Called by the edit control with it initially gains focus (except via the mouse)
	*/
	public void onSetFocus();

	/**
	 * Commits this translator's modelelement.
	*/
	public void saveModelElement();

	/**
	 * Sets the internal position pointer without updating the edit control.
	*/
	public void setCurrentPosition( int nPos );

	/**
	 * Hides fields with no data, call after manually updating fields to affect the display.
	*/
	public void updateVisibleFields( IEditControlField pField );

	/**
	 * Cuts the selected text from the edit control to the Clipboard.
	*/
	public void cutToClipboard();

	/**
	 * Pastes the Clipboard contents in the the text field at the current position.
	*/
	public void pasteFromClipboard();

	/**
	 * Copies the selected text into the Clipboard.
	*/
	public void copyToClipboard();

	/**
	 * Returns the tooltip text for translators that contain multiple fields.
	*/
	public String getTooltipLeftText();
	public String getTooltipSubjectText();
	public String getTooltipRightText();
	public String getTooltipText();

	/**
	 * Reload the tooltip's data.
	*/
	public void updateToolTip();
	public void updateHints();

	/**
	 * Reads and formats the internal components of the model element for editing.
	*/
	public void initTextFields();

	/**
	 * Inserts a new text field that is described by a property element.
	*/
	public void addField( IPropertyElement pData, boolean bNoUpdate );

	/**
	 * Helper to save this element's fields. Called by SaveModelElement().
	*/
	public void saveFields();

	/**
	 * Called by the edit control on mouse moves.
	*/
	public void onMouseMove( int x, int y );

	/**
	 * Responds to hint window being clicked.
	*/
	public void handleHint();

	/**
	 * Responds to hint list window being closed.
	*/
	public IEditControlField handleHintText( String sText );

	/**
	 * If the keychar is a separator, performs the appropriate action.
	*/
	public ETPairT<Boolean, Boolean> handleSeparator( int nChar, int nCurrentPos);

	/**
	 * If the keychar is a separator, performs the appropriate action.
	*/
	public boolean handleTopLevelSeparators( int nChar);

	/**
	 * Inserts a new text field that is described by a property definition.
	*/
	public IEditControlField addFieldDefinition( IPropertyDefinition pDefinition, IEditControlField pInsertField );

	/**
	 * Temporary
	*/
	public void dump( String sPad );

	/**
	 * Returns the next text field following pPreviousField, or the first field if pPreviousField is 0.
	*/
	public IEditControlField getNextField( IEditControlField pPreviousField );

	/**
	 * Returns the previous text field preceeding pNextField, or the last field if pNextField is 0.
	*/
	public IEditControlField getPreviousField( IEditControlField pNextField );

	/**
	 * Attaches a collection of property elements to their respective fields.  The fields' data is not reloaded.
	*/
	public void updateFields( Vector pElements );

	/**
	 * Get/Set the textfield that contains this translator, if applicable.
	*/
	public IEditControlField getParentField();

	/**
	 * Get/Set the textfield that contains this translator, if applicable.
	*/
	public void setParentField( IEditControlField value );

	/**
	 * Recalculates field positions rightward from the current field.
	*/
	public void updateFieldPositions( IEditControlField pCurrent );

	/**
	 * For translators with repeating fields, add a new, empty field.
	*/
	public boolean handleDelimitor( int nChar, int m_nCurrentPos );

	/**
	 * Informs the field that editing is complete and it should re-format itself depending on its rules.
	*/
	public void updateField( IEditControlField pField );

	/**
	 * Is this translator deleted?
	*/
	public boolean getDeleted();

	/**
	 * Is this translator deleted?
	*/
	public void setDeleted( boolean value );

	/**
	 * Causes enabled field to be displayed, or hidden.
	*/
	public void enableFields( boolean EnableFields );

	/**
	 * Is this translator modified?
	*/
	public boolean getModified();

	/**
	 * Is this translator modified?
	*/
	public void setModified( boolean value );

	/**
	 * Returns the text field that the caret is currently in.
	*/
	public IEditControlField getCurrentField();

	/**
	 * Tells the translator to register its accelerators with the edit control.
	*/
	public void registerAccelerators();

	/**
	 * Called when an accelerator is pressed.
	*/
	public boolean handleAccelerator( int nChar );

	/**
	 * gets the text position of last visible text field 
	 */
	public int getLastTextStartPos();
}
