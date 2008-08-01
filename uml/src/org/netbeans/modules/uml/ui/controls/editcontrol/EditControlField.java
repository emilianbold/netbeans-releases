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



package org.netbeans.modules.uml.ui.controls.editcontrol;

import java.awt.Color;
import java.awt.Font;
import java.util.StringTokenizer;
import java.util.Vector;

import org.netbeans.modules.uml.core.configstringframework.ConfigStringHelper;
import org.netbeans.modules.uml.core.configstringframework.IConfigStringTranslator;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.support.umlutils.IDataFormatter;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyDefinition;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElement;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElementManager;
import org.netbeans.modules.uml.ui.support.ProductHelper;

/**
 * @author sumitabhk
 *
 */
public class EditControlField implements IEditControlField
{
	// temporary, contains the name of the property definition we wrap
	private String m_Name = "";

	private Color m_TextColor = null;
	private boolean m_Modified = false;
	private boolean m_Selected = false;
	private boolean m_Enabled = true;
	private boolean m_Deleted = false;
	private Font m_Font = null;
	private String m_Text = "";
	private String m_sDefaultText = "";
	private String m_inertSeparators = "";
	private char m_inertStart = 0;
	private char m_inertEnd = 0;
	private boolean m_separatorNavigable = true;
	private boolean m_VisibilityOverride = false;  // override to force visibility off in spite of the visiblity rule

	private int m_FieldStartPos = 0;
	private int m_FieldEndPos = 0;
	private int m_TextStartPos = 0;
	private int m_TextEndPos = 0;

	private String m_ToolTip = "";

	// back pointer to the translator that contains this text field
	private ITranslator m_OwnerTranslator = null;
	// text fields can, instead of managing their text, instead manage a translator that contains more text fields
	private ITranslator m_Translator = null;

	// field appearance is controlled by a propertydefinition
	private IPropertyDefinition m_PropertyDefinition = null;

	// field data is managed by a propertyelement
	private IPropertyElement m_PropertyElement = null;

	private int m_Multiplicity = 1;
	private boolean m_bRequired = false;
	private boolean m_bDefault = false;
	private String m_Visibility = "notEmpty";       // visibility rule, e.g. "true", "notEmpty"
	private String m_Delimitor = "";
	private String m_LeadSep = "";
	private String m_TrailSep = "";

	private int /*TextFieldEditKind*/ m_EditKind = 0;

	
	/**
	 * 
	 */
	public EditControlField()
	{
		super();
	}

	/**
	 * Returns the full text contained by this TextField.
	 *
	 * @param[out] pVal The text
	 * 
	 * @return HRESULT
	 */
	public String getText()
	{
		String retText = "";
		if (m_Translator != null)
		{
			retText = m_Translator.getCurrent();
		}
		else
		{
			retText = m_Text;
		}
		return retText;
	}

	/**
	 * Sets the text for this textfield.
	 *
	 * @param[in] newVal The new text string
	 * 
	 * @return HRESULT
	 * 
	 * @warning If this field is a proxy for a Translator, nothing occurs.  
	 * The proxy Translator is responsible for handling its own textfields.
	 */
	public void setText(String newVal)
	{
//		boolean modified = newVal.equals(m_Text) ? false : true;
		setModified(true);
		m_Text = newVal;
		
		// force recalc of our position based on new text size
		setTextPos( m_TextStartPos );
		setFieldPos( m_FieldStartPos );
	}

	/**
	 * Returns the font used for rendering this textfield.
	 *
	 * @param[out] pVal A FontDisp object describing the font
	 * 
	 * @return HRESULT
	 */
	public Font getFont()
	{
		return m_Font;
	}

	/**
	 * Sets the font used for rendering this textfield.
	 *
	 * @param[in] newVal A FontDisp object describing the font
	 * 
	 * @return HRESULT
	 */
	public void setFont(Font newVal)
	{
		m_Font = newVal;
	}

	/**
	 * Is this field selected?  If so it rendered in the current selected color scheme.
	 *
	 * @param[out] bSelected TRUE or FALSE
	 * 
	 * @return HRESULT
	 */
	public boolean getSelected()
	{
		return m_Selected;
	}

	/**
	 * Is this field selected?  If so it rendered in the current selected color scheme.
	 *
	 * @param[in] bSelected TRUE or FALSE
	 * 
	 * @return HRESULT
	 */
	public void setSelected(boolean newVal)
	{
		m_Selected = newVal;
	}

	/**
	 * Is this field Enabled?  An enabled field is not necessarily visible, depending
	 * on its visibility rules.  However a disabled field is never visible.
	 *
	 * @param[out] bEnabled TRUE or FALSE
	 * 
	 * @return HRESULT
	 */
	public boolean getEnabled()
	{
		return m_Enabled;
	}

	/**
	 * Is this field Enabled?  An enabled field is not necessarily visible, depending
	 * on its visibility rules.  However a disabled field is never visible.
	 *
	 * @param[in] bEnabled TRUE or FALSE
	 * 
	 * @return HRESULT
	 */
	public void setEnabled(boolean newVal)
	{
		m_Enabled = newVal;
		
		if (m_Translator != null)
		{
			m_Translator.enableFields(newVal);
		}
	}

	/**
	 * The field's text color.
	 *
	 * @param[out] pVal The current RGB color value
	 * 
	 * @return HRESULT
	 */
	public Color getTextColor()
	{
		return m_TextColor;
	}

	/**
	 * The field's text color.
	 *
	 * @param[in] newVal The new RGB color value
	 * 
	 * @return HRESULT
	 */
	public void setTextColor(Color newVal)
	{
		m_TextColor = newVal;
	}

	/**
	 * A helper function to initialize a TextField with one 
	 * function call. The modified flag is set to false.
	 *
	 * @param[in] sText The text value of this field
	 * @param[in] Font The font in which to display the text
	 * @param[in] textColor The color of the text
	 * 
	 * @return HRESULT
	 */
	public void init(String sText, Font font, Color textColor)
	{
		m_Text = sText;
		m_Font = font;
		m_TextColor = textColor;
		m_Modified = false;
	}

	/**
	 * Returns the default text.  Default text is commonly used when no user-defined 
	 * text has been entered, for example "un-named".  The Default text is 
	 * not displayed unless SetDefaultText() has been called.
	 *
	 * @param[out] pVal The current value of this textfield's default text
	 * 
	 * @return HRESULT
	 */
	public String getDefaultText()
	{
		return m_sDefaultText;
	}

	/**
	 * Sets the default text.  Default text is commonly used when no user-defined 
	 * text has been entered, for example "un-named".  The Default text 
	 * is not displayed unless SetDefaultText() has been called.
	 *
	 * @param[in] pVal The new value of this textfield's default text
	 * 
	 * @return HRESULT
	 */
	public void setDefaultText(String newVal)
	{
		m_sDefaultText = "";
		if (newVal != null && newVal.length() > 0)
		{
			m_sDefaultText = newVal;
		}
	}

	/**
	 * Is this field visible?  DOES NOT SEARCH SUB_FIELDS!
	 *
	 * @param[out] bVisible TRUE or FALSE
	 * 
	 * @return HRESULT
	 */
	public boolean getVisible()
	{
		// DO NOT SEARCH SUB_FIELDS!  CALLER MUST ITERATE THROUGH SUB_FIELDS THEMSELVES!
		return isVisible();
	}

	/**
	 * Forces the field to be visible.
	 *
	 * @param[in] bVisible TRUE or FALSE
	 * 
	 * @return HRESULT
	 */
	public void setVisible(boolean bVisible)
	{
		// if this field contains sub-fields you must set their visibility individually
		if (bVisible)
		{
			m_VisibilityOverride = true;
			m_Deleted = false;
			m_Enabled = true;
			setModified(true);
			
			// populate the field now that it's visible
			if (m_Text.length() == 0)
			{
				setDefaultText();
			}
			
			// HACK!!!  We don't want every field's subfields to be set to visible because it could cause a nasty
			// recursion.  However, we need multiplicity to activate (and mark modified) its subfields
			if (m_Name.equals("Multiplicity"))
			{
				if (m_Translator != null)
				{
					Vector subFields = m_Translator.getTextFields();
					if (subFields != null)
					{
						int count = subFields.size();
						for (int i=0; i<count; i++)
						{
							IEditControlField field = (IEditControlField)subFields.elementAt(i);
							field.setVisible(true);
						}
					}
				}
			}
		}
		else
		{
			m_VisibilityOverride = false;
		}
	}

	/**
	 * Is this field both visible and NOT deleted?  
	 *
	 * @param[out] bVisible TRUE or FALSE
	 * 
	 * @return HRESULT
	 */
	public boolean getVisible2()
	{
		return isVisibleNotDeleted();
	}

	/**
	 * Is this field visible and not deleted?  
	 *
	 * @return TRUE if the field should be visible
	 */
	private boolean isVisibleNotDeleted()
	{
		boolean bVisible = false;
		boolean deleted = getDeleted();
		if (!deleted)
		{
			bVisible = isVisible();
		}
		return bVisible;
	}

	/**
	 * Is this field visible?  Returns TRUE if
	 *
	 *  1. Visibility has been expressed set (via put_Visibility(TRUE))
	 *  2. This field does not contain a translator but no data has been set (PropertyElement is NULL)
	 *  3. The visibility rules are met, e.g. 
	 *        if "true" then the field is always visible
	 *        if "notEmpty" then the field is visible if there is a value in its data or if
	 *           its translator returns a value
	 *        if "previousNotEmpty" then the field is visible if the preceeding field is visible
	 *
	 * @return TRUE if the field should be visible
	 */
	private boolean isVisible()
	{
		boolean bVisible = false;

		// visibilityOverride is set programmatically to show a field that might not
		// otherwise be visible (eg it is empty)
		if (m_VisibilityOverride)
		{
			bVisible = true;
			bVisible = !(m_Visibility.equals("false"));
		}
		else
		{
			// if no data then the field can't be visible
			// can't use the propertyelement test b/c we may be looking at a field that hasn't been 
			// initialized
			if (m_Enabled)
			{
				// process visibility rules

				// always true?
				bVisible = (m_Visibility.equals("true"));
				
				String text = getText();
				
				// visible contingent on our value
				if( !bVisible && m_Visibility.equals("notEmpty"))
				{
					// visible if "notEmpty" and our data has a value
					bVisible = (text.length() > 0);
				}
				
				if ( m_Visibility.equals("previousNotEmpty") )
				{
					// here we have a visibility rule that is based on whether the preceeding field
					// contains text.  Call the translator to get the preceeding field
					if (m_OwnerTranslator != null)
					{
						IEditControlField prevField = m_OwnerTranslator.getPreviousField(this);
						if (prevField != null)
						{
							String prevText = prevField.getText();
							bVisible = (prevText.length() > 0);
							
							if (m_Text.length() == 0)
							{
								setDefaultText();
							}
						}
					}
				}
			}
		}
		
		return bVisible;
	}

	/**
	 * Forces the field to be visible AND sets its default text if the current field text is empty.
	 *
	 * @param[in] bVisible TRUE or FALSE
	 * 
	 * @return HRESULT
	 */
	public void setVisible2(boolean bVisible)
	{
		// if this field contains sub-fields you must set their visibility individually
		setVisible(bVisible);
	}

	/**
	 * Returns the relative offsets of the beginning and end of 
	 * the text represented by this textfield.  The
	 * offsets are 0-base, relative to the 1st visible 
	 * character in the edit control.  If the field is not visible nEndPos == nStartPos.
	 *
	 * @param[out] The starting position
	 * @param[out] The ending position
	 * 
	 * @return HRESULT
	 */
	public int getTextPos(int nStartPos)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	public int getTextStartPos()
	{
		int retPos = 0;
		if (m_Translator != null)
		{
			retPos = m_Translator.getTextStartPos();
		}
		else
		{
			retPos = m_TextStartPos;
		}
		return retPos;
	}

	public int getTextEndPos()
	{
		int retPos = 0;
		if (m_Translator != null)
		{
			retPos = m_Translator.getTextEndPos();
		}
		else
		{
			retPos = m_TextEndPos;
		}
		return retPos;
	}

	public int getLastTextStartPos()
	{
		int retPos = 0;
		if (m_Translator != null)
		{
			retPos = m_Translator.getLastTextStartPos();
		}
		else
		{
			retPos = m_TextStartPos;
		}
		return retPos;
	}

	/**
	 * Sets the relative offsets for the beginning and end of the text 
	 * represented by this textfield.  Call this function when 
	 * re-ordering fields, or after characters have been added or 
	 * removed from previous fields. If the field is not visible, 
	 * the field's length is set to zero.
	 *
	 * @param[in] The new starting offset
	 * 
	 * @return HRESULT
	 */
	public long setTextPos(int nStartPos)
	{
		if( m_Translator != null)
		{
		   m_Translator.setFieldPos(nStartPos );
		}
		else
		{
		   m_TextStartPos = nStartPos;
		   if( isVisibleNotDeleted() )
		   {
		   		int textOff = 0;
		   		if (m_Text != null)
		   		{
		   			textOff = m_Text.length();
		   		}
				m_TextEndPos = nStartPos + textOff;
		   }
		   else
		   {
				m_TextEndPos = nStartPos;
		   }
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControlField#getFieldPos(int)
	 */
	public int getFieldPos(int nStartPos)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	public int getFieldStartPos()
	{
		return m_FieldStartPos;
	}

	public int getFieldEndPos()
	{
		return m_FieldEndPos;
	}

	/**
	 * Sets the relative offsets for the beginning and end of the text 
	 * represented by this textfield.  Call this function when 
	 * re-ordering fields, or after characters have been added or 
	 * removed from previous fields. If the field is not visible, 
	 * the field's length is set to zero.
	 *
	 * @param[in] The new starting offset
	 * 
	 * @return HRESULT
	 */
	public long setFieldPos(int nStartPos)
	{
		m_FieldStartPos = nStartPos;
		if (m_Translator != null)
		{
			if (isVisible())
			{
				int leadOff = 0;
				int trailOff = 0;
				if (m_LeadSep != null)
				{
					leadOff = m_LeadSep.length();
				}
				if (m_TrailSep != null)
				{
					trailOff = m_TrailSep.length();
				}
				m_Translator.setFieldPos(nStartPos + leadOff);
				int nStart = m_Translator.getFieldStartPos();
				int nEnd = m_Translator.getFieldEndPos();
				m_FieldEndPos = nEnd + trailOff;
			}
			else
			{
				m_Translator.setFieldPos(nStartPos);
				m_FieldEndPos = nStartPos;
			}
		}
		else
		{
			if( isVisibleNotDeleted() )
			{
				int leadOff = 0;
				int textOff = 0;
				int trailOff = 0;
				if (m_LeadSep != null)
				{
					leadOff = m_LeadSep.length();
				}
				if (m_Text != null)
				{
					textOff = m_Text.length();
				}
				if (m_TrailSep != null)
				{
					trailOff = m_TrailSep.length();
				}
			    m_FieldEndPos = nStartPos + leadOff + textOff + trailOff;
			    setTextPos( nStartPos + leadOff );
			}
			else
			{
			   m_FieldEndPos = nStartPos;
			   setTextPos( nStartPos );
			}
		}
		return 0;
	}

	/**
	 * Fills the field's value with the default text.  Some fields 
	 * may, if empty, upon activation be asked to fill
	 * themselves with their default value, for example 
	 * "un-named".  The application calls this method to cause the
	 * field to set its value to its default value.  The field is not
	 * repositioned based on the new text length.
	 * 
	 * @return HRESULT
	 */
	public long setDefaultText()
	{
		if (!m_Text.equals(m_sDefaultText))
		{
			m_Text = m_sDefaultText;
			setModified(true);
		}
		return 0;
	}

	/**
	 * Returns the translator hosted by this text field. A text 
	 * field normally wraps text, however in some cases it is
	 * desireable for the text field to contain a translator, 
	 * which in turn hosts a number of "sub" text fields.
	 *
	 * @param[out] pTranslator The Translator, if one has been set
	 * 
	 * @return HRESULT
	 */
	public ITranslator getTranslator()
	{
		return m_Translator;
	}

	/**
	 * Sets the translator hosted by this text field. A text field 
	 * normally wraps text, however in some cases it is
	 * desireable for the text field to contain a translator, which 
	 * in turn hosts a number of "sub" text fields.
	 *
	 * @param[in] pTranslator The Translator to delegate all text activity to
	 * 
	 * @return HRESULT
	 */
	public void setTranslator(ITranslator pTranslator)
	{
		m_Translator = pTranslator;
	}

	/**
	 * Returns the translator that contains this text field. All text fields
	 * are owned by a translator, which provides navigation and other
	 * services to the edit control by manipulating its fields.
	 *
	 * @param[out] pTranslator The Owner Translator.
	 * 
	 * @return HRESULT
	 */
	public ITranslator getOwnerTranslator()
	{
		return m_OwnerTranslator;
	}

	/**
	 * Sets the translator that contains this text field. 
	 *
	 * @param[in] pTranslator The Translator that owns this textfield.
	 *
	 * @return HRESULT
	 */
	public void setOwnerTranslator(ITranslator pTranslator)
	{
		m_OwnerTranslator = pTranslator;
	}

	/**
	 *
	 * Has this field's text been modified?
	 *
	 * @param bModified[out]
	 *
	 * @return HRESULT
	 *
	 */
	public boolean getModified()
	{
		boolean pModified = false;
		if (m_Translator != null)
		{
			pModified = m_Translator.getModified();
		}
		else
		{
			pModified = m_Modified;
		}
		return pModified;
	}

	/**
	 *
	 * Force's this field's modified status.
	 *
	 * @param bModified[in] The new modified value.
	 *
	 * @return HRESULT
	 *
	 */
	public void setModified(boolean bModified)
	{
		m_Modified = bModified;
		
		// notify parent only if we are becoming modified
		if (m_OwnerTranslator != null && m_Modified)
		{
			m_OwnerTranslator.setModified(bModified);
		}
	}

	/**
	 *
	 * The value of this text field's tooltip.
	 *
	 * @param pVal[out] Tooltip.
	 *
	 * @return HRESULT
	 *
	 */
	public String getToolTipText()
	{
		return m_ToolTip;
	}

	/**
	 *
	 * The value of this text field's tooltip.
	 *
	 * @param pVal[in] Tooltip.
	 *
	 * @return HRESULT
	 *
	 */
	public void setToolTipText(String newVal)
	{
		if (m_Translator == null)
		{
			m_ToolTip = newVal;
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControlField#getMultiplicity()
	 */
	public boolean getMultiplicity()
	{
		boolean retVal = false;
		if (m_Multiplicity != 0)
		{
			retVal = true;
		}
		return retVal;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControlField#setMultiplicity(boolean)
	 */
	public void setMultiplicity(boolean newVal)
	{
		if (newVal)
		{
			m_Multiplicity = -1;
		}
		else
		{
			m_Multiplicity = 1;
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControlField#getRequired()
	 */
	public boolean getRequired()
	{
		return m_bRequired;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControlField#setRequired(boolean)
	 */
	public void setRequired(boolean newVal)
	{
		m_bRequired = newVal;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControlField#getDefault()
	 */
	public boolean getDefault()
	{
		return m_bDefault;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControlField#setDefault(boolean)
	 */
	public void setDefault(boolean newVal)
	{
		m_bDefault = newVal;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControlField#getVisibility()
	 */
	public String getVisibility()
	{
		return m_Visibility;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControlField#setVisibility(java.lang.String)
	 */
	public void setVisibility(String newVal)
	{
		m_Visibility = newVal;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControlField#getLeadSeparator()
	 */
	public String getLeadSeparator()
	{
		return m_LeadSep;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControlField#setLeadSeparator(java.lang.String)
	 */
	public void setLeadSeparator(String newVal)
	{
		m_LeadSep = newVal;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControlField#getTrailSeparator()
	 */
	public String getTrailSeparator()
	{
		return m_TrailSep;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControlField#setTrailSeparator(java.lang.String)
	 */
	public void setTrailSeparator(String newVal)
	{
		m_TrailSep = newVal;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControlField#getDelimitor()
	 */
	public String getDelimitor()
	{
		return m_Delimitor;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControlField#setDelimitor(java.lang.String)
	 */
	public void setDelimitor(String newVal)
	{
		m_Delimitor = newVal;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControlField#getPropertyElement()
	 */
	public IPropertyElement getPropertyElement()
	{
		return m_PropertyElement;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControlField#setPropertyElement(org.netbeans.modules.uml.core.support.umlutils.IPropertyElement)
	 */
	public void setPropertyElement(IPropertyElement pEle)
	{
		m_PropertyElement = pEle;
		m_Deleted = false;
		
		// enable by virtue that we have a propelement, but setting the definition
		// below might disable
		m_Enabled = true;
		
		// read the property element's data
		if (pEle != null)
		{
			// apply formatting to this value
			IPropertyDefinition pDef = null; 
			if (m_PropertyDefinition == null)
			{
				pDef = m_PropertyElement.getPropertyDefinition();
				if (pDef != null)
				{
					setPropertyDefinition(pDef);
				}
			}
			
			// fetch the data value if any
			String sValue = pEle.getValue();
			if (sValue != null && sValue.length() > 0)
			{
				if (pDef != null)
				{
					String validVals = pDef.getValidValues();
					if (validVals != null)
					{
						int pos = validVals.indexOf("|");
						if (pos >= 0)
						{
							StringTokenizer tokenizer = new StringTokenizer(validVals, "|");
							int count = tokenizer.countTokens();
							try
							{
								int val = Integer.valueOf(sValue).intValue();
								int index = 0;
								while (tokenizer.hasMoreTokens())
								{
									String token = tokenizer.nextToken();
									if (index == val)
									{
										IConfigStringTranslator translator = ConfigStringHelper.instance().getTranslator();
										sValue = translator.translate(pDef, token);
										break;
									}
									index++;
								}
							}
							catch (NumberFormatException e)
							{
							}
						}
					}
				}
				m_Text = sValue;

				// enable if there's a value.  We might need to enable regardless since we have data
				// NOTE this doesn't work b/c we might be a container for subelements, thus our
				// value might be empty but we need to be enabled anyway.
				m_Enabled = true;
			}
			
			// enable since we have data
			m_Enabled         = true;

			// At this point we are done with a normal text field.  But for fields that contain subfields
			// we need to process each subfield.  A subfield PropertyDefinition should already exist for
			// these fields.

			// if we have subelements, nest them within a new translator (created during put_PropertyDefinition())
			Vector subEles = pEle.getSubElements();
			if (subEles != null)
			{
				int count = subEles.size();
				if (count > 0)
				{
					// we have a nested field situation here, our "text" doesn't mean anything
					m_Text = "";
					
					// m_pTranslator is for holding the sub-definitions
					if (m_Translator != null)
					{
						Object obj = m_PropertyElement.getElement();
						if (obj instanceof IElement)
						{
							IElement pME = (IElement)obj;
							m_Translator.setElement(pME);
						}
						
						for (int i=0; i<count; i++)
						{
							IPropertyElement subEle = (IPropertyElement)subEles.elementAt(i);
							m_Translator.addField(subEle, false);
						}
						
						// retrieve our built up text for determination of visibility status
						//sText = m_Translator.getCurrent();
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControlField#setPropertyElement2(org.netbeans.modules.uml.core.support.umlutils.IPropertyElement)
	 */
	public void setPropertyElement2(IPropertyElement pEle)
	{
		m_PropertyElement = pEle;
		
		// read the property element's data
		if (m_PropertyElement != null)
		{
			// enable if there's a value.  We might need to enable regardless since we have data
			m_Enabled = true;
			
			// At this point we are done with a normal text field.  But for fields that contain subfields
			// we need to process each subfield.  A subfield PropertyDefinition should already exist for
			// these fields.

			// if we have subelements, nest them within a new translator (created during put_PropertyDefinition())
			Vector subEles = m_PropertyElement.getSubElements();
			if (subEles != null)
			{
				int count = subEles.size();
				if (count > 0)
				{
					// m_pTranslator is for holding the sub-definitions
					if (m_Translator != null)
					{
						m_Translator.updateFields(subEles);
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControlField#getPropertyDefinition()
	 */
	public IPropertyDefinition getPropertyDefinition()
	{
		return m_PropertyDefinition;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControlField#setPropertyDefinition(org.netbeans.modules.uml.core.support.umlutils.IPropertyDefinition)
	 */
	public void setPropertyDefinition(IPropertyDefinition pDef)
	{
		m_PropertyDefinition = pDef;
		
		// read the property element's data
		if (pDef != null)
		{
			String sName = pDef.getName();
			m_Name = sName;

			String sRequired = pDef.getFromAttrMap("required");
			String sDefault = pDef.getFromAttrMap("default");
			String sVisible = pDef.getFromAttrMap("visible");
			String sMultiplicity = pDef.getFromAttrMap("multiplicity");
			String sDelimitor = pDef.getFromAttrMap("delimitor");
			String sLead = pDef.getFromAttrMap("leadSeparator");
			String sTrail = pDef.getFromAttrMap("trailSeparator");
			String sTooltip = pDef.getFromAttrMap("toolTip");
			String sType = pDef.getFromAttrMap("controlType");
			String sEnabled = pDef.getFromAttrMap("enabled");
			String separatorNavigable = pDef.getFromAttrMap("separatorNavigable");
			String inertSeparators = pDef.getFromAttrMap("inertSeparators");
			String inertStart = pDef.getFromAttrMap("inertStart");
			String inertEnd = pDef.getFromAttrMap("inertEnd");
			
			// temporary until default values are worked out
			sName = pDef.getFromAttrMap("dummyDefaultValue");
			if (sName != null)
			{
				m_sDefaultText = sName;
			}
			
			if (pDef.isDefaultExisting())
			{
				sName = pDef.getDefaultValue();
				m_sDefaultText = sName;
			}
			// don't set default text here otherwise the field might become visible
//			  m_sText = m_sDefaultText;

			if (sMultiplicity != null)
			{
				try {
					m_Multiplicity   = Integer.valueOf(sMultiplicity).intValue();
				}
				catch (NumberFormatException e)
				{
					m_Multiplicity = 0;
				}
			}
			
			if (sRequired != null)
			{
				m_bRequired       = (sRequired.equals("true"));
			}

			if (sEnabled != null)
			{
				m_Enabled        = (sEnabled.equals("true"));
			}
			
			if (sDefault != null)
			{
				m_bDefault        = (sDefault.equals("true"));
			}

			m_Delimitor      =  sDelimitor;
			m_LeadSep        = sLead;
			m_TrailSep       = sTrail;
			m_Visibility     = sVisible;
			m_inertSeparators= inertSeparators;
                        m_inertStart = (inertStart != null && inertStart.length() > 0) ? inertStart.charAt(0) : 0;
                        m_inertEnd = (inertEnd != null && inertEnd.length() > 0) ? inertEnd.charAt(0) : 0;
                        
			if (separatorNavigable != null)
			{
				m_separatorNavigable = !(separatorNavigable.equals("false"));
			}
			
			if (m_Visibility == null || m_Visibility.length() == 0)
			{
				m_Visibility = "notEmpty";
			}
			
			m_EditKind = ITextFieldEditKind.TFEK_DEFAULT;
			if( sType != null && sType.equals("read-only") )
			{
			   m_EditKind = ITextFieldEditKind.TFEK_READONLY;
			}
			else if(  sType != null && sType.equals("list") )
			{
			   m_EditKind = ITextFieldEditKind.TFEK_LIST;
			}
			else if(  sType != null && sType.equals("combo") )
			{
			   m_EditKind = ITextFieldEditKind.TFEK_COMBO;
			}
			
			// need to convert tt to verbose text
			IConfigStringTranslator pTranslator = ConfigStringHelper.instance().getTranslator();
			String sValue = pTranslator.translate(pDef, sTooltip);
			
			m_ToolTip = "";
			if (sLead != null)
			{
				m_ToolTip  = sLead;
			}
			if (sValue != null)
			{
				m_ToolTip += sValue;
			}
			if (m_Multiplicity != 1)
			{
				if (m_Delimitor != null)
				{
					m_ToolTip += m_Delimitor;
					m_ToolTip += "...";
				}
				
				if (m_Multiplicity > 1)
				{
					//String str = StringUtilities.Format( _T("[%d]"), m_nMultiplicity );
					m_ToolTip += "0.." + m_Multiplicity;
				}
			}
			if (sTrail != null)
			{
				m_ToolTip += sTrail;
			}
			
			// Check for subdefinitions, these will define nested fields
			Vector subDefs = pDef.getSubDefinitions();
			if (subDefs != null)
			{
				int count = subDefs.size();
				if (count > 0)
				{
					m_Translator = new TranslatorImpl();
					m_Translator.setParentField(this);
					for (int i=0; i< count; i++)
					{
						IPropertyDefinition subDef = (IPropertyDefinition)subDefs.elementAt(i);
						m_Translator.addFieldDefinition(subDef, null);
					}
				}
			}
		}
	}

	/**
	 *
	 * Saves the textfield's value, if modified.
	 *
	 *
	 * @return 
	 *
	 */
	public boolean save()
	{
		boolean bCancel = false;
		
		boolean bDeleted = getDeleted2();
		boolean bModified = getModified();

		// here we determine what actions we're going to take
		// special case, what if it's not modified, required and empty?
		// if a propelem exists it should be whacked.

		// if it's modified, no propelem exists, but it's required and empty?
		// the propelem should not be invented.
		boolean bDelete = false;
		boolean bSave   = true;
		
		// test if this field is tagged "required" but is empty
		String sText = getText();
		if (m_bRequired && (sText == null || sText.length() == 0))
		{
			bDeleted = true;
		}
		
		// if it's deleted do we need to whack the property element
		if (bDeleted)
		{
			bSave = false;
			if (m_PropertyElement != null)
			{
				bDelete = true;
			}
		}
		
		// perform the save      
		if (bDelete)
		{
			// caution here!  this guy was tagged as "required" by the parent, he's empty so we whack the parent
			if (m_OwnerTranslator != null)
			{
				IEditControlField pField = m_OwnerTranslator.getParentField();
				if (pField != null)
				{
					IPropertyElement pOwner = pField.getPropertyElement();
					if (pOwner != null)
					{
						pOwner.remove();
					}
				}
			}
			else
			{
				// should never occur
				if (m_PropertyElement != null)
				{
					m_PropertyElement.remove();
				}
			}
		}
		else if (bModified && bSave)
		{
			// can we have a situation where we should save even when not modified?
        
			// test if ever saved before.  This part creates a new property element for a 
			// element that is new, e.g. a parameter, multiplicity range, etc.
			if (m_PropertyElement == null)
			{
				// new field, create a property element for it
				if (m_PropertyDefinition != null)
				{
					// never saved before, create a new propertyelement
					if (m_OwnerTranslator != null)
					{
						IDataFormatter pFormatter = ProductHelper.getDataFormatter();
						if (pFormatter != null)
						{
							IPropertyElementManager pManager = pFormatter.getElementManager();
							if (pManager != null)
							{
								String name = m_PropertyDefinition.getName();

								// this builds the property elements for this definition
								m_PropertyElement = pManager.buildTopPropertyElement(m_PropertyDefinition);
								if (m_PropertyElement != null)
								{
//									m_PropertyElement = pManager.buildElement(null, m_PropertyDefinition, null);
									// tell our new Property Element about its parent
									if (m_OwnerTranslator != null)
									{
										IEditControlField pField = m_OwnerTranslator.getParentField();
										if (pField != null)
										{
											IPropertyElement propEle = pField.getPropertyElement();
											if (propEle != null)
											{
												m_PropertyElement.setParent(propEle);
											}
										}
									}
									
									// calling Save() now will actually create model elements
									boolean saved = m_PropertyElement.save();
									
									// attach all placeholder property subdefinitions with their subelements
									setPropertyElement2(m_PropertyElement);
								}
							}
						}
					}
				}
			}
			
			// this part writes the new data
			if (m_Translator != null)
			{
				String str = "";
				IElement pEle = m_Translator.getElement();
				if (pEle != null)
				{
					str = pEle.getElementType();
				}
				
				m_Translator.saveFields();
				if (m_PropertyElement != null)
				{
					m_PropertyElement.save();
				}
			}
			else
			{
				if (m_PropertyElement != null)
				{
					String name = m_PropertyElement.getName();
					String value = m_PropertyElement.getValue();
					String newVal = m_Text;
					
					//if this is one of the property elements which have
					//to be one of the valid values, m_Text will be the display
					//value and not one of the values to store. So convert back
					if (m_PropertyDefinition != null)
					{
						String vals = m_PropertyDefinition.getValidValues();
						if (vals != null)
						{
							int pos = vals.indexOf("|");
							if (pos >= 0)
							{
								StringTokenizer tokenizer = new StringTokenizer(vals, "|");
								int index = 0;
								while (tokenizer.hasMoreTokens())
								{
									String token = tokenizer.nextToken();
									IConfigStringTranslator trans = ConfigStringHelper.instance().getTranslator();
									String transToken = trans.translate(m_PropertyDefinition, token);
									if (transToken.equals(m_Text))
									{
										newVal = token;
										break;
									}
									index++;
								}
							}
						}
					}
					
					if (!newVal.equals(value))
					{
						value = newVal;
						if (value == null || value.length() == 0)
						{
							value = "";
						}
						m_PropertyElement.setValue(value);
						m_PropertyElement.save();
					}
					// HACK!!!
					// MultiplicityRanges needs to be able to understand what an empty value means.
					else if (name.equals("MultiplicityRanges"))
					{
						m_PropertyElement.setValue("*");
						m_PropertyElement.save();
					}
				}
			}
		}
		
		setModified(false);
		
		return bCancel;
	}

	/**
	 *
	 * Returns a list of valid values for a list or combo type edit kind.  The list must be released by the
	 * caller
	 *
	 * @param [out] A list of strings
	 *
	 * @return HRESULT
	 *
	 */
	public Vector getValidValues()
	{
		Vector list = new Vector();
		if (m_PropertyElement != null)
		{
			// apply formatting to this value 
			IPropertyDefinition pDef = m_PropertyElement.getPropertyDefinition();
			if (pDef != null)
			{
				String validVals = pDef.getValidValues();
				if (validVals != null && validVals.indexOf("#") >= 0)
				{
					validVals = pDef.getValidValues2();
				}
				
				if (validVals.indexOf("|") >= 0)
				{
					StringTokenizer tokenizer = new StringTokenizer(validVals, "|");
					while (tokenizer.hasMoreTokens())
					{
						String str = tokenizer.nextToken();
						list.add(str);
					}
				}
			}
		}
		return list;
	}

	/**
	 *
	 * Determines if the character matches the leading separator, thereby becoming a "shortcut" to this field.
	 *
	 * @param nChar [in] The character keycode.
	 * @param bIsSeparator [out, retval] TRUE is the character is a separator.
	 *
	 * @return HRESULT
	 *
	 */
	public boolean isLeadSeparator(int nChar)
	{
		boolean bIsSeparator = false;
		if (m_LeadSep != null)
		{
			int pos = m_LeadSep.indexOf(nChar);
			if (pos >= 0)
			{
				bIsSeparator = true;
			}
		}
		return bIsSeparator;
	}

	/**
	 *
	 * Determines if the character matches the trailing separator, thereby becoming a "shortcut" to this field.
	 *
	 * @param nChar [in] The character keycode.
	 * @param bIsSeparator [out, retval] TRUE is the character is a separator.
	 *
	 * @return HRESULT
	 *
	 */
	public boolean isTrailSeparator(int nChar)
	{
		boolean bIsSeparator = false;
		if (m_TrailSep != null)
		{
			int pos = m_TrailSep.indexOf(nChar);
			if (pos >= 0)
			{
				bIsSeparator = true;
			}
		}
		return bIsSeparator;
	}

	/**
	 * Is the character a delimitor for repeating fields?
	 *
	 * @param nChar [in] The character to check.
	 * @param bIsDelimitor [out,retval] TRUE if the field contains sub-fields and the
	 * character is one of the delimitor characters.
	 * 
	 * @return HRESULT
	 */
	public boolean isDelimitor(int nChar)
	{
		boolean bIsDelimitor = false;

		// if a repeating field, check for the delimitor
		if( isVisibleNotDeleted() && m_Multiplicity != 1 )
		{
			// could be tricky here, need to know where the caret is
			// <leadSep><1st Field><delim><2nd Field><delim><3rd Field><trailSep>
			if (m_Delimitor != null)
			{
				int pos = m_Delimitor.indexOf(nChar);
				if (pos >= 0)
				{
					bIsDelimitor = true;
				}
			}
		}
		
		return bIsDelimitor;
	}

	/**
	 * Is this field deleted?
	 *
	 * @param[out] bDeleted TRUE or FALSE
	 * 
	 * @return HRESULT
	 */
	public boolean getDeleted()
	{
		return m_Deleted;
	}

	/**
	 * Is this field deleted?
	 *
	 * @param[in] bDeleted TRUE or FALSE
	 * 
	 * @return HRESULT
	 */
	public void setDeleted(boolean value)
	{
		if (m_Translator != null)
		{
			m_Translator.setDeleted(value);
		}
		else
		{
			m_Deleted = value;
			if (m_Deleted)
			{
				setText("");
			}
		}
	}

	/**
	 * Is this field and all sub-fields deleted?
	 *
	 * @param[out] bDeleted TRUE or FALSE
	 * 
	 * @return HRESULT
	 */
	public boolean getDeleted2()
	{
		boolean deleted = false;
		// can't call translator's get_deleted b/c we might be a container that
		// has no fields or all deleted fields, but that doesn't make us deleted.
		if (m_Translator != null)
		{
			deleted = m_Translator.getDeleted();
		}
		else
		{
			deleted = getDeleted();
		}
		return deleted;
	}

	/**
	 * Updates the field according to its visibility and validation rules.
	 *
	 * @param[in] <Name Description>
	 * 
	 * @return HRESULT
	 */
	public long update()
	{
		if (m_Translator != null)
		{
			m_Translator.updateField(null);
		}
		else
		{
			m_VisibilityOverride = false;
			if( m_bRequired && m_Text.length() == 0 )
			{
			   setDeleted( true);
			}
		}
		return 0;
	}

	/**
	 *
	 * Retrieves this textfields edit kind, e.g. default (stringified text), list (a list of values) or combo
	 *
	 * @param [out] The TextFieldEditKind
	 *
	 * @return HRESULT
	 *
	 */
	public int getEditKind()
	{
		return m_EditKind;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControlField#dump(java.lang.String)
	 */
	public long dump(String sPad)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControlField#getName()
	 */
	public String getName()
	{
		return m_Name;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.editcontrol.IEditControlField#checkInertSeparator(int)
	 */
	public boolean checkInertSeparator(int nChar, int absolutePosition)
	{
		boolean pInert = false;
		if (m_inertSeparators != null)
		{
			if (m_inertSeparators.equals("*"))
			{
				pInert = true;
				return pInert;
			}
                        
                        if (inInertBracket(absolutePosition)) {
                            int pos = m_inertSeparators.indexOf(nChar);
                            if (pos >= 0)
                            {
                                    pInert = true;
                                    return pInert;
                            }
                        }
		}
		
		if (m_Translator != null)
		{
			IEditControlField subField = m_Translator.getCurrentField();
			if (subField != null)
			{
				pInert = subField.checkInertSeparator(nChar, absolutePosition);
			}
		}
		return pInert;
	}

        private boolean inInertBracket(int absolutePosition) {
            // no inert start/end char defined assume in bracket if inertSeparators is defined
            if (m_inertStart == 0 || m_inertEnd == 0) {
                return true;
            }
            
            int startCount = 0;
            int endCount = 0;
            int pos = absolutePosition - getFieldStartPos() - 1;
            char[] chars = m_Text.toCharArray();
            for (int i=pos-1; i>=0; i--) {
                char c = chars[i];
                if (c == m_inertStart) {
                    startCount++;
                } else if (c == m_inertEnd) {
                    endCount++;
                }
            }
            return (startCount - endCount) > 0;
        }
}



