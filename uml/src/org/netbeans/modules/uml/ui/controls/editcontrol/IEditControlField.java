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

import java.awt.Color;
import java.awt.Font;
import java.util.Vector;

import org.netbeans.modules.uml.core.support.umlutils.IPropertyDefinition;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElement;

public interface IEditControlField
{
	/**
	 * The value of this text field.
	*/
	public String getText();

	/**
	 * The value of this text field.
	*/
	public void setText( String value );

	/**
	 * The font to display this field, if NULL uses the compartment's default font.
	*/
	public Font getFont();

	/**
	 * The font to display this field, if NULL uses the compartment's default font.
	*/
	public void setFont( Font value );

	/**
	 * Is this field currently selected?
	*/
	public boolean getSelected();

	/**
	 * Is this field currently selected?
	*/
	public void setSelected( boolean value );

	/**
	 * Is this field currently enabled?
	*/
	public boolean getEnabled();

	/**
	 * Is this field currently enabled?
	*/
	public void setEnabled( boolean value );

	/**
	 * The text color.
	*/
	public Color getTextColor();

	/**
	 * The text color.
	*/
	public void setTextColor( Color value );

	/**
	 * Initialize the text field's properties.
	*/
	public void init( String sText, Font Font, Color TextColor );

	/**
	 * The default value of this text field.
	*/
	public String getDefaultText();

	/**
	 * The default value of this text field.
	*/
	public void setDefaultText( String value );

	/**
	 * Is this field currently visible?
	*/
	public boolean getVisible();

	/**
	 * Is this field currently visible?
	*/
	public void setVisible( boolean value );

	/**
	 * Is this field currently visible and NOT deleted?
	*/
	public boolean getVisible2();

	/**
	 * Is this field currently visible and NOT deleted?
	*/
	public void setVisible2( boolean value );

	/**
	 * Get the field's editable text position within the overall character string (nStartPos=left, nEndPos=right).
	*/
	public int getTextPos( int nStartPos );
	public int getTextStartPos();
	public int getTextEndPos();

	/**
	 * Set the field's editable text starting position within the overall character string.
	*/
	public long setTextPos( int nStartPos );

	/**
	 * Get the field's position within the overall character string (nStartPos=left, nEndPos=right).
	*/
	public int getFieldPos( int nStartPos );
	public int getFieldStartPos();
	public int getFieldEndPos();

	/**
	 * Set the field's starting position within the overall character string.
	*/
	public long setFieldPos( int nStartPos );

	/**
	 * Initializes the value of this text field to it's default value.
	*/
	public long setDefaultText();

	/**
	 * Gets/Sets a translator containing sub-textfields.
	*/
	public ITranslator getTranslator();

	/**
	 * Gets/Sets a translator containing sub-textfields.
	*/
	public void setTranslator( ITranslator value );

	/**
	 * Gets/Sets the translator that contains this textfield.
	*/
	public ITranslator getOwnerTranslator();

	/**
	 * Gets/Sets the translator that contains this textfield.
	*/
	public void setOwnerTranslator( ITranslator value );

	/**
	 * Has the text been changed?
	*/
	public boolean getModified();

	/**
	 * Has the text been changed?
	*/
	public void setModified( boolean value );

	/**
	 * The value of this text field's tooltip.
	*/
	public String getToolTipText();

	/**
	 * The value of this text field's tooltip.
	*/
	public void setToolTipText( String value );

	/**
	 * Is this field repeating (TRUE) or singular (FALSE)?
	*/
	public boolean getMultiplicity();

	/**
	 * Is this field repeating (TRUE) or singular (FALSE)?
	*/
	public void setMultiplicity( boolean value );

	/**
	 * Can this field be left empty?
	*/
	public boolean getRequired();

	/**
	 * Can this field be left empty?
	*/
	public void setRequired( boolean value );

	/**
	 * Is this the default field for editing?
	*/
	public boolean getDefault();

	/**
	 * Is this the default field for editing?
	*/
	public void setDefault( boolean value );

	/**
	 * The field's visibility rule, either 'yes', 'no' or 'notEmpty'.
	*/
	public String getVisibility();

	/**
	 * The field's visibility rule, either 'yes', 'no' or 'notEmpty'.
	*/
	public void setVisibility( String value );

	/**
	 * The leading separator that is inserted according to the field's visibility rule.
	*/
	public String getLeadSeparator();

	/**
	 * The leading separator that is inserted according to the field's visibility rule.
	*/
	public void setLeadSeparator( String value );

	/**
	 * The trailing separator that is appended according to the field's visibility rule.
	*/
	public String getTrailSeparator();

	/**
	 * The trailing separator that is appended according to the field's visibility rule.
	*/
	public void setTrailSeparator( String value );

	/**
	 * The delimitor that separates repeating fields.
	*/
	public String getDelimitor();

	/**
	 * The delimitor that separates repeating fields.
	*/
	public void setDelimitor( String value );

	/**
	 * The property element used to format and save this field's data.
	*/
	public IPropertyElement getPropertyElement();

	/**
	 * The property element used to format and save this field's data.
	*/
	public void setPropertyElement( IPropertyElement value );

	/**
	 * The property element used to format and save this field's data. DOES NOT LOAD VALUES FROM THE ELEMENT INTO THE FIELD!
	*/
	public void setPropertyElement2( IPropertyElement value );

	/**
	 * The property Definition used to format and save this field's data.
	*/
	public IPropertyDefinition getPropertyDefinition();

	/**
	 * The property Definition used to format and save this field's data.
	*/
	public void setPropertyDefinition( IPropertyDefinition value );

	/**
	 * Writes changed data out to the text field's PropertyElement.
	*/
	public boolean save();

	/**
	 * Get the picklist values.
	*/
	public Vector getValidValues();

	/**
	 * Is the passed character a separator for this field?
	*/
	public boolean isLeadSeparator( int nChar );

	/**
	 * Is the passed character a separator for this field?
	*/
	public boolean isTrailSeparator( int nChar );

	/**
	 * Is the passed character a delimitor, separating sub-fields of this field?
	*/
	public boolean isDelimitor( int nChar );

	/**
	 * Is this field currently deleted?
	*/
	public boolean getDeleted();

	/**
	 * Is this field currently deleted?
	*/
	public void setDeleted( boolean value );

	/**
	 * Is this field and all sub-fields currently deleted?
	*/
	public boolean getDeleted2();

	/**
	 * Informs the field that editing is complete and it should re-format itself depending on its rules.
	*/
	public long update();

	/**
	 * What's the field's editing method?
	*/
	public int getEditKind();

	/**
	 * Temporary
	*/
	public long dump( String sPad );

	/**
	 * Temporary
	*/
	public String getName();

	/**
	 * Return true if the char is an inert separator
         * @param nChar char in question
         * @param position position of the char
	*/
	public boolean checkInertSeparator( int nChar, int positon );
	
	/**
	 * Gets the last visible text's start position
	 */
	public int getLastTextStartPos();
}
