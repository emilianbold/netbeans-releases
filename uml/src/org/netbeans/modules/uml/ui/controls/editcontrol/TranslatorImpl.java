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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collection;
import java.util.Vector;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.eventframework.IEventDispatcher;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConfigManager;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationSignatureChangeContextManager;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.OperationSignatureChangeContextManager;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.core.support.umlsupport.Log;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlsupport.Strings;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.IDataFormatter;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyDefinition;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyDefinitionFactory;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElement;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElementManager;
import org.netbeans.modules.uml.core.support.umlutils.PropertyDefinitionFactory;
import org.netbeans.modules.uml.core.support.umlutils.PropertyElementManager;
import org.netbeans.modules.uml.core.typemanagement.IPickListManager;
import org.netbeans.modules.uml.core.typemanagement.ITypeManager;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProduct;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductProjectManager;

/**
 * @author sumitabhk
 *
 */
public class TranslatorImpl implements ITranslator
{
   // state attributes, used with AxEditCtrl
   private String m_RawString = "";
   private String m_FormattedString = "";
   private IEditControl m_EditControl = null;
   private IElement m_Element = null;
   private Vector m_TextFields = null;
   private boolean m_Modified = false;
   
   /// The formatter object that will eventually replace all these other members
   private IPropertyDefinitionFactory m_DefinitionFactory = null;
   private IPropertyElementManager m_ElementManager = null;
   private IEditControlField m_ParentField = null;
   private IPropertyElement m_PropertyElement = null;
   
   private String m_TooltipLeftText = "";
   private String m_TooltipSubjectText = "";
   private String m_TooltipRightText = "";
   private boolean m_IsGetToolTipText = false;
   
   //	prevent recursion
   private boolean m_InSet = false;
   
   /**
    *
    */
   public TranslatorImpl()
   {
      super();
   }
   
   /**
    * Gets/Sets the model element.
    *
    * @param pElement[out]
    */
   public IElement getElement()
   {
      return m_Element;
   }
   
   /**
    * Gets/Sets the model element.
    *
    * @param pElement[in]
    */
   public void setElement(IElement pElement)
   {
      m_Element = pElement;
   }
   
   /**
    * Returns the EditControl reference.
    *
    * @param pControl[out] The edit control associated with this editing session.
    */
   public IEditControl getEditControl()
   {
      IEditControl retObj = null;
      
      if (m_EditControl == null)
      {
         if (m_ParentField != null)
         {
            ITranslator pTranslator = m_ParentField.getOwnerTranslator();
            if (pTranslator != null)
            {
               m_EditControl = pTranslator.getEditControl();
               if (m_EditControl != null)
               {
                  // Since m_EditControl is a raw pointer to avoid deadly
                  // embraces we do a manual release so we don't leak
                  // a refcount.
                  //m_EditControl.removeReference();
               }
            }
         }
      }
      
      if (m_EditControl != null)
      {
         retObj = m_EditControl;
         // Since m_EditControl is a raw pointer we do a manual
         // addref so the out parameter is safely returned, as would
         // be the case if m_EditControl was a CComPtr and we called
         // CopyTo.
         //			_VH(m_EditControl->AddRef());
      }
      
      return retObj;
   }
   
   /**
    * Sets the AxEditCtrl, called prior to editing.  The text fields are initialized and the
    * edit control's window set to the current contents of this Translator.
    *
    * @param pControl A pointer to the edit control that uses this Translator.  If NULL or 0
    * the fields are initialized anyway (assuming an Element has been attached).
    */
   public void setEditControl(IEditControl pControl)
   {
      m_EditControl = pControl;
      
      setCurrentPosition(0);
      
      try
      {
         // here's where we should perform the parsing:
         initTextFields();
         
         // update the control's display
         refreshEditControl();
      }
      catch (Exception e)
      {
         Log.stackTrace(e);
      }
   }
   
   /**
    * Sets the AxEditCtrl only, does not perform any initialization.
    *
    * @param pControl A pointer to the edit control that uses this Translator..
    */
   public void setEditControl2(IEditControl pControl)
   {
      m_EditControl = pControl;
      
      if (m_EditControl != null)
      {
         IEditControlField pField = null;
         while ((pField=getNextField(pField)) != null)
         {
            ITranslator pTranslator = pField.getTranslator();
            if (pTranslator != null)
            {
               pTranslator.setEditControl2(m_EditControl);
            }
         }
      }
   }
   
   /**
    * Get a formatted string.  The data is from the model and may not reflect any
    * un-saved changes made via editing.
    *
    * C++ Specifications : visibility return-type-expression name ( parameter-list )
    * UML Specifications : visibility name ( parameter-list ) : return-type-expression { property-string }
    *
    * For the sake of the simple format the optional property string will not be returned.
    */
   public String getSimple()
   {
      String retStr = "";
      if (m_Element != null)
      {
         IDataFormatter pFormatter = ProductHelper.getDataFormatter();
         if (pFormatter != null)
         {
            retStr = pFormatter.formatElement(m_Element);
         }
      }
      return retStr;
   }
   
   /**
    * Builds up the display string by concatenating each visible field.
    * You must call InitTextFields() before calling get_Current() to
    * create and fill each text field.  InitTextFields() a protected
    * member of TranslatorImpl, if you need to call InitTextFields()
    * via COM call put_EditControl().
    *
    * @param pVal[out]
    */
   public String getCurrent()
   {
      String retStr = "";
      String sDelimiter = null;
      if (m_ParentField != null)
      {
         sDelimiter = m_ParentField.getDelimitor();
      }
      
      boolean bFirst = false;
      IEditControlField pField = getNextVisibleField(null);
      while (pField != null)
      {
         boolean bDeleted = pField.getDeleted();
         String str = "";
         if (!bDeleted)
         {
            if (bFirst)
            {
               if (sDelimiter != null)
               {
                  str = sDelimiter;
               }
            }
            else
            {
               bFirst = true;
            }
            
            String sTemp = "";
            if (pField.getLeadSeparator() != null)
            {
               sTemp = pField.getLeadSeparator();
            }
            str += sTemp;
            
            sTemp = "";
            if (pField.getText() != null)
            {
               sTemp = pField.getText();
            }
            str += sTemp;
            
            sTemp="";
            if (pField.getTrailSeparator() != null)
            {
               sTemp = pField.getTrailSeparator();
            }
            str += sTemp;
            
            retStr += str;
         }
         pField = getNextVisibleField(pField);
      }
      return retStr;
   }
   
        /*
         * Makes the field visible, fills with default data if available.
         *
         * @param pField[in]
         */
   private void insureVisibleAndNotEmpty( IEditControlField pField )
   {
      if (pField != null)
      {
         String str = pField.getText();
         if (str == null || str.length() == 0)
         {
            // no text, get the default text since we're insuring not empty
            pField.setDefaultText();
         }
         setFieldVisible(pField, true);
      }
   }
   
   /**
    * Returns a TextField collection representing each field in the translated string.
    *
    * @param pFields[out]
    */
   public Vector getTextFields()
   {
      if (m_TextFields == null)
      {
         m_TextFields = new Vector();
      }
      return m_TextFields;
   }
   
   /**
    * Gets the caret position, called by the edit control.
    *
    * @param nPos[in]
    */
   public int getPosition()
   {
      if (m_EditControl != null)
      {
         return m_EditControl.getCurrentPosition();
      }
      return 0;
   }
   
   /**
    * Sets the caret position, called by the edit control.
    *
    * @param nPos[out]
    */
   public void setPosition(int pos)
   {
      setPosition(pos, pos);
   }
   
   /**
    * Returns the starting and ending position of the editable text contained by this Translator.  The
    * position is the offset within the edit control.
    *
    * @param[out] nStartPos The offset from the first character in the edit control to the first
    * character in the first textfield contained by this Translator.
    * @param[out] nEndPos The offset from the first character in the edit control to the last
    * character in the last textfield contained by this Translator.
    *
    * @return HRESULT
    */
   public void getTextPos(int nStartPosition, int nEndPosition)
   {
      int dummy = 0;
      // read first field, obtain starting position
      IEditControlField pField = getNextField(null);
      if (pField != null)
      {
         nStartPosition = pField.getTextStartPos();
         nEndPosition = pField.getTextEndPos();
      }
      
      // loop until no more visible fields, updating ending position
      while ((pField = getNextField(pField)) != null)
      {
         nEndPosition = pField.getTextEndPos();
      }
   }
   
   /**
    * Returns the starting and ending position of the textfields contained by this Translator.  The
    * position is the offset within the edit control.
    *
    * @param[out] nStartPos The offset from the first character in the edit control to the first
    * character in the first textfield contained by this Translator.
    * @param[out] nEndPos The offset from the first character in the edit control to the last
    * character in the last textfield contained by this Translator.
    *
    * @return HRESULT
    */
   public void getFieldPos(int nStartPosition, int nEndPosition)
   {
      IEditControlField pField = getNextField(null);
      if (pField != null)
      {
         nStartPosition = pField.getFieldStartPos();
         nEndPosition = pField.getFieldEndPos();
         // hidden fields will return nStartPos == nEndPos so account for no text width
         int dummy = 0;
         while ((pField=getNextField(pField)) != null )
         {
            nEndPosition = pField.getFieldEndPos();
         }
      }
   }
   
   /**
    * Sets the position of the fields contained by this translator to the edit control position indicated.
    * Ordinarily a Translator's first field always starts at edit position 0, however in the case
    * of sub-fields they could be different.
    *
    * @param[in] nStartPos The new starting position.  All fields will be moved accordingly
    *
    * @return HRESULT
    */
   public void setFieldPos(int nStartPosition)
   {
      String delimiter = "";
      if (m_ParentField != null)
      {
         delimiter = m_ParentField.getDelimitor();
      }
      
      boolean bFirst = false;
      int nDelimSpace = 0;
      
      IEditControlField pField = getNextField(null);
      while (pField != null)
      {
         if (bFirst)
         {
            if (delimiter != null)
            {
               nDelimSpace = delimiter.length();
            }
         }
         else
         {
            bFirst = true;
         }
         
         int dummy = 0;
         boolean deleted = pField.getDeleted();
         if (!deleted)
         {
            pField.setFieldPos(nStartPosition + nDelimSpace);
         }
         else
         {
            pField.setFieldPos(nStartPosition);
         }
         
         nStartPosition = pField.getFieldEndPos();
         pField = getNextField(pField);
      }
   }
   
   /**
    * Sets the edit control's cursor position and selects text.
    */
   public void setPosition(int nStartPos, int nEndPos)
   {
      if (m_EditControl != null)
      {
         m_EditControl.setSel(nStartPos, nEndPos);
         m_EditControl.getSel(nStartPos, nEndPos);
         m_EditControl.setCurrentPosition(nEndPos);
      }
      setCurrentPosition(nEndPos);
      updateHints();
   }
   
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.editcontrol.ITranslator#handleKeyDown(int)
         */
   public boolean handleKeyDown(int nKey)
   {
      boolean handled = false;
      IEditControlField pField = getCurrentField();
      int nStart = 0;
      int nEnd = 0;
      int nStartPos = 0;
      int nEndPos = 0;
      int nNext = 0;
      
      // get current field and offsets
      if (pField == null)
      {
         return false;
      }
      nStart = pField.getTextStartPos();
      nEnd = pField.getTextEndPos();
      
      // get selected area
      nStartPos = getSelStartPos();
      nEndPos = getSelEndPos();
      
      // if field contains sub-fields, delegate
      ITranslator pTrans = pField.getTranslator();
      if (pTrans != null)
      {
         // don't attempt to catch
         handled = pTrans.handleKeyDown(nKey);
         if (handled)
         {
            return true;
         }
      }
      
      if (nKey == KeyEvent.VK_RIGHT)
      {
         // special situation, when text is selected the edit control always places the caret at the end
         nNext = getCurrentPosition() + 1;
         
         // if text is selected we may need to provide special handling
         if (nStartPos != nEndPos)
         {
            // if shift is not down, dismiss the selection and
            // put the cursor at the right edge
            if (!shiftDown())
            {
               selectToPosition(nEndPos);
               return true;
            }
         }
         
         if (controlDown())
         {
            // control down, doing a jump
            // default is to jump to the end of the current field
            nNext = (nNext > nEnd) ? nNext : nEnd;
         }
         
         if (nNext > nEnd)
         {
            pField = getNextVisibleField(pField);
         }
         if (pField == null)
         {
            // no more fields, we're done
            return false;
         }
         
         // jump to front of next field
         // don't let them land on separator text
         nStart = pField.getTextStartPos();
         nEnd = pField.getTextEndPos();
         if (nNext < nStart)
         {
            nNext = nStart;
         }
         else if (nNext > nEnd)
         {
            int edge = 0;
            
            // we're stepping over a trailing separator
            nStart = pField.getFieldStartPos();
            edge = pField.getFieldEndPos();
            
            // skip over the separator by setting our current position to the
            // end of the separator and recursing
            
            // move past our field, check for another
            IEditControlField pNextField = getNextVisibleField(pField);
            if (pNextField != null)
            {
               edge = pNextField.getTextStartPos();
               nEnd = pNextField.getTextEndPos();
            }
            
            setSel(edge, edge);
            updateHints();
            return true;
         }
         
         // move caret, honoring selection mode
         selectToPosition(nNext);
         updateVisibleFields(null);
         return true;
      }
      else if (nKey == KeyEvent.VK_LEFT)
      {
         // already at left, nothing to do
         if (getCurrentPosition() == 0)
         {
            return true;
         }
         
         // calc position to go to
         nNext = getCurrentPosition() - 1;
         
         // if text is selected we may need to provide special handling
         if (nStartPos != nEndPos)
         {
            // text is selected and we're going left
            // if shift is not down, dismiss the selection and
            // put the cursor at the left edge
            if (!shiftDown())
            {
               selectToPosition(nStartPos);
               return true;
            }
         }
         
         // are we jumping?
         if (controlDown())
         {
            // jump to front of current field unless already there
            nNext = (nNext < nStart) ? nNext : nStart;
         }
         
         // if we've passed our field
         if (nNext < nStart)
         {
            IEditControlField pNextField = getPreviousVisibleField(pField);
            
            // no visible fields to the left?
            // there might be some hidden ones, try getting it
            if (pNextField == null)
            {
               pField = getPreviousField(pField);
            }
            else
            {
               pField = pNextField;
            }
         }
         if (pField == null)
         {
            return false;
         }
         
         // don't let them land on separator text
         nStart = pField.getTextStartPos();
         nEnd = pField.getTextEndPos();
         if (nNext > nEnd)
         {
            nNext = nEnd;
         }
         else if (nNext < nStart)
         {
            int edge = getCurrentPosition();
            
            // we're stepping over a trailing separator
            edge = pField.getFieldStartPos();
            nEnd = pField.getFieldEndPos();
            
            // skip over the separator by setting our current position to the
            // end of the separator and recursing
            
            // move past our field, check for another
            IEditControlField pNextField = getPreviousVisibleField(pField);
            if (pNextField != null)
            {
               nStart = pNextField.getTextStartPos();
               edge = pNextField.getTextEndPos();
            }
            setSel(edge, edge);
            return true;
         }
         else
         {
            // are we jumping?
            if (controlDown())
            {
               // jump to front of current field unless already there
               nNext = (nNext < nStart) ? nNext : nStart;
            }
         }
         
         // move caret, honoring selection mode
         selectToPosition(nNext);
         updateVisibleFields(null);
         return true;
      }
      else if (nKey == KeyEvent.VK_TAB)
      {
         if (shiftDown())
         {
            // going left, if current position is at the end of our field select it
            if (getCurrentPosition() >= nEnd)
            {
               selectField(pField);
            }
            else
            {
               // jumping leftwards, find next editable field
               IEditControlField pPrevField = getPreviousVisibleField(pField);
               if (pPrevField != null)
               {
                  ITranslator pPrevTrans = pPrevField.getTranslator();
                  if (pPrevTrans != null)
                  {
                     // has a translator we want to jump to the last visible field
                     jumpToFieldEnd(pPrevField);
                     handled = handleKeyDown(nKey);
                     return handled;
                  }
                  else
                  {
                     // no translator, select it and we're outta here
                     selectField(pPrevField);
                  }
               }
               else
               {
                  // no more fields, set position then let the control handle it
                  if (getCurrentPosition() > 0)
                  {
                     jumpToFieldBegin(pField);
                     updateVisibleFields(m_ParentField);
                     return true;
                  }
               }
            }
         }
         else
         {
            // jumping rightwards, find next editable field
            IEditControlField pNextField = getNextVisibleField(pField);
            if (pNextField != null)
            {
               selectField(pNextField);
            }
            else
            {
               handled = handleEndKeyDown();
               return handled;
            }
         }
         handled = true;
         updateVisibleFields(m_ParentField);
      }
      else if (nKey == KeyEvent.VK_DELETE)
      {
         handled = handleDelete(true);
      }
      else if (nKey == KeyEvent.VK_BACK_SPACE)
      {
         handled = handleDelete(false);
      }
      return handled;
   }
   
   public boolean handleEndKeyDown()
   {
      boolean handled = false;
      int pos = getCurrentPosition();
      updateVisibleFields(m_ParentField);
      setPosition(pos);
      return handled;
   }
   
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.editcontrol.ITranslator#handleKeyUp(int)
         */
   public boolean handleKeyUp(int nKey)
   {
      // TODO Auto-generated method stub
      return false;
   }
   
   /**
    * Process WM_CHAR for translated input.
    *
    * @param nChar[in]
    * @param bHandled[out]
    */
   public boolean handleChar(String nChangedChar)
   {
      boolean isHandled = false;
      try
      {
         IEditControlField pField = null;
         
         // apply default filtering
         if (nChangedChar.length() > 0 && filterChar(nChangedChar.charAt(0)))
         {
            // character is filtered, ignore it
            isHandled = false;
            return isHandled;
         }
         
         // get current field and offsets
         pField = getCurrentField();
         if (pField != null)
         {
            boolean bVisible = pField.getVisible();
            boolean bEnabled = pField.getEnabled();
            
            // current field may not be enabled if it is empty
            if (!bEnabled)
            {
               // enable all field contained by this translator
               pField.setEnabled(true);
            }
            
            bVisible = pField.getVisible();
            if (!bVisible)
            {
               // we don't want to just set visible b/c this forces the visiblity to TRUE (overrides the field's
               // visibility rules)  Only force it true if this is the active field and it isn't visible currently
               pField.setVisible(true);
            }
            
            // if field contains sub-fields, delegate
            ITranslator pTranslator = pField.getTranslator();
            if (pTranslator != null)
            {
               // don't attempt to catch
               // key down, insert it at the current position
               isHandled = pTranslator.handleChar(nChangedChar);
            }
            
            int nStartPos = getSelStartPos();
            int nEndPos = getSelEndPos();
            
            int nStart = pField.getTextStartPos();
            int nEnd = pField.getTextEndPos();
            
            
            String sText = nChangedChar;
            
            // anything selected?
            if (nStartPos != nEndPos)
            {
               // here we try to determine if the selected area crosses field boundaries
               IEditControlField pNextField = pField;
               while (nEndPos > nEnd)
               {
                  // move past our field, check for another
                  pNextField = getNextVisibleField(pNextField);
                  if (pNextField == null)
                  {
                     // no more fields, insert here
                     nEndPos = nStartPos;
                     break;
                  }
                  else
                  {
                     // fetch the field's boundaries
                     nStart = pNextField.getTextStartPos();
                     nEnd = pNextField.getTextEndPos();
                     
                     if (nEndPos >= nEnd)
                     {
                        pNextField.setText("");
                        pNextField.setVisible(false);
                     }
                     else
                     {
                        break;
                     }
                  }
               }
               // should have our insert position worked out
               setModified(true);
            }
            else
            {
               boolean bOverstrike = false;
               if (m_EditControl != null)
               {
                  bOverstrike = m_EditControl.getOverstrike();
               }
               if (bOverstrike)
               {
                  // nothing selected but in overstrike mode so select
                  // next char to replace
                  nEndPos = nStartPos + 1;
                  
                  if (nEndPos > nEnd)
                  {
                     // move past our field, check for another
                     IEditControlField pNextField = getNextVisibleField( pField );
                     if( pNextField == null )
                     {
                        // no more fields, insert here
                        nEndPos = nStartPos;
                     }
                  }
               }
               else
               {
                  // nothing selected and not in overstrike
                  // just insert at the current position
                  nEndPos = nStartPos;
               }
            }
            
            // replace selection with the char
            // save current position for restore afterwards
            replaceText( pField, nStartPos, nEndPos, sText, true );
            isHandled = true;
         }
      }
      catch (Exception e)
      {
         Log.stackTrace(e);
      }
      return isHandled;
   }
   
   private int getSelStartPos()
   {
      if (m_EditControl != null)
      {
         return m_EditControl.getSelStartPos();
      }
      else
      {
         // can't simulate selection if no edit control
         return getCurrentPosition();
      }
   }
   
   private int getSelEndPos()
   {
      if (m_EditControl != null)
      {
         return m_EditControl.getSelEndPos();
      }
      else
      {
         // can't simulate selection if no edit control
         return getCurrentPosition();
      }
   }
   
   /**
    * Identifies the allowed characters for edit control.
    * @param nChar
    * @return
    */
   private boolean filterChar(char nChar)
   {
      boolean isFiltered = true;
      
      //right now we allow only alpha numeric, _, : and blank
      if (Character.isLetterOrDigit(nChar))
      {
         isFiltered = false;
      }
      else 
      {
         //if control key is pressed then we want to filter the character.
         if (m_EditControl != null && !m_EditControl.isControlDown())
         {
            isFiltered = false;
         }
      }
      return isFiltered;
   }
   
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.editcontrol.ITranslator#handleLButtonDown(int)
         */
   public boolean handleLButtonDown(int nPosition)
   {
      // TODO Auto-generated method stub
      return false;
   }
   
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.editcontrol.ITranslator#handleLButtonDblClk(int)
         */
   public boolean handleLButtonDblClk(int nPosition)
   {
      // TODO Auto-generated method stub
      return false;
   }
   
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.editcontrol.ITranslator#handleLButtonTripleClk(int)
         */
   public boolean handleLButtonTripleClk(int nPosition)
   {
      // TODO Auto-generated method stub
      return false;
   }
   
   /**
    * Called when the control is initially activated.  Selects the first entire field for editing.
    */
   public void onSetFocus()
   {
      IEditControlField pField = getDefaultField();
      if (pField != null)
      {
         selectField(pField);
      }
      else
      {
         int startPos=getTextStartPos();
         int endPos=getTextEndPos();
         //getTextPos(startPos, endPos);
         setPosition(startPos);
      }
   }
   
   /**
    * Save the model element.
    *
    * DO NOT RECURSE!  This member is called by the edit control only.
    */
   public void saveModelElement()
   {
      if (m_EditControl == null)
      {
         return;
      }
      
      boolean proceed = true;
      proceed = getModified();
      
      if (proceed)
      {
         // The event dispatcher
         IEventDispatcher pDispatcher = null;
         IEditEventPayload payload = null;
         
         // try to get the edit control's event dispatcher
         if (m_EditControl != null)
         {
            pDispatcher = m_EditControl.getEventDispatcher();
         }
         
         IEditControlEventDispatcher pEventDispatcher = null;
         // if we got a dispatcher fire precommit
         if (pDispatcher != null && pDispatcher instanceof IEditControlEventDispatcher)
         {
            pEventDispatcher = (IEditControlEventDispatcher)pDispatcher;
            payload = pEventDispatcher.createEventPayload();
            proceed = pEventDispatcher.firePreCommit(payload);
         }
         
         // precommit not fired or fired/return proceed
         if (proceed)
         {
            saveFields();
            m_Modified = false;
         }
         
         if (pEventDispatcher != null)
         {
            pEventDispatcher.firePostCommit(payload);
         }
         m_EditControl = null;
      }
   }
   
   /**
    * Updates the translator's internal position pointer without affecting the edit control.  To update
    * the position pointer and move the edit control cursor caret use Position();
    *
    * @param[in] nPos The new position of the control insertion point.
    *
    * @return HRESULT
    */
   public void setCurrentPosition(int nPos)
   {
      if (m_EditControl != null)
      {
         m_EditControl.setCurrentPosition(nPos);
      }
   }
   
   /**
    *
    * Turns field visibility override off. Thus the field's visibility within the edit control is dictated by its
    * visibility settings, e.g. notEmpty, enabled, not deleted, etc.
    *
    * @param pField [in] The field to set.  If NULL all fields contained by this translator are set.
    *
    * @return HRESULT
    *
    */
   public void updateVisibleFields(IEditControlField pField)
   {
      if (pField != null)
      {
         // a specific field has been targeted, set its visibility override to false
         pField.setVisible(false);
         
         ITranslator translator = pField.getTranslator();
         if (translator != null)
         {
            translator.updateVisibleFields(null);
         }
      }
      else
      {
         // walk the fields
         pField = getLastField();
         while (pField != null)
         {
            updateVisibleFields(pField);
            pField = getPreviousField(pField);
         }
      }
   }
   
   /**
    *
    * Returns the text field at a given character position.
    *
    * @param nPos[in] The zero-based character position
    *
    * @return The text field at that position
    *
    */
   private IEditControlField getFieldAtPosition( int nPos )
   {
      IEditControlField pField = null;
      int nStart=0, nEnd=0;
      while ((pField = getNextField(pField)) != null)
      {
         nStart = pField.getFieldStartPos();
         nEnd = pField.getFieldEndPos();
         if( nStart <= nPos && nPos <= nEnd )
            break;
      }
      return pField;
   }
   
      /* (non-Javadoc)
       * @see org.netbeans.modules.uml.ui.controls.editcontrol.ITranslator#cutToClipboard()
       */
      public void cutToClipboard()
      {
         if( null == m_EditControl )   return;

         IEditControlField field = getCurrentField();

         // get current field and offsets
         if( field != null )
         {
            int nStartPos = getSelStartPos();
            int nEndPos = getSelEndPos();
            
            int nStart = field.getTextStartPos();
            int nEnd = field.getTextEndPos();

            // can't cut across field boundaries
            if( (nStartPos < nStart) || (nEndPos > nEnd) )
            {
               // TODO messageBeep( MB_ICONASTERISK );
               return;
            }
            else
            {
               if( nStartPos != nEndPos )
               {
                  String str = m_EditControl.getText();

                  Transferable transferable = new StringSelection( str.substring( nStartPos, nEndPos ));

                  Toolkit.getDefaultToolkit().getSystemClipboard().setContents( transferable, null );
                  replaceText( field, nStartPos, nEndPos, "", true );
               }
            }
         }
      }

      /* (non-Javadoc)
       * @see org.netbeans.modules.uml.ui.controls.editcontrol.ITranslator#pasteFromClipboard()
       */
      public void pasteFromClipboard()
      {
         IEditControlField field = getCurrentField();

         // get current field and offsets
         if( field != null )
         {
            int nStartPos = getSelStartPos();
            int nEndPos = getSelEndPos();
            
            int nStart = field.getTextStartPos();
            int nEnd = field.getTextEndPos();

            // can't paste across field boundaries
            if( (nStartPos < nStart) || (nEndPos > nEnd) )
            {
               // TODO messageBeep( MB_ICONASTERISK );
               return;
            }
            else
            {
               Transferable txfer = Toolkit.getDefaultToolkit().getSystemClipboard().getContents( null );
               if( txfer.isDataFlavorSupported( DataFlavor.stringFlavor ) )
               {
                  try
                  {
                     String strPaste = (String)txfer.getTransferData( DataFlavor.stringFlavor );
                     replaceText( field, nStartPos, nEndPos, strPaste, true );
                  }
                  catch (UnsupportedFlavorException e1)
                  {
                     e1.printStackTrace();
                  }
                  catch (IOException e1)
                  {
                     e1.printStackTrace();
                  }
               }
            }
         }
      }

      /* (non-Javadoc)
       * @see org.netbeans.modules.uml.ui.controls.editcontrol.ITranslator#copyToClipboard()
       */
      public void copyToClipboard()
      {
         // TODO Auto-generated method stub
      }
   
   /**
    * Retrieves the text for the edit control's tooltip. The text is retrieved
    * in 3 parts: a left, middle (subject) and right part.  The subject
    * part is highlighted within the tooltip and reflects the tip for the current text field.
    *
    * @param sLeft[out] The left part of the tooltip text
    * @param sSubject[out] The subject part of the tooltip text
    * @param sRight[out] The right part of the tooltip text
    *
    * @return HRESULT
    */
   public String getTooltipText()
   {
      IEditControlField pCurrField = getCurrentField();
      String bstrLeft = "";
      String bstrSubject = "";
      String bstrRight = "";
      
      boolean bLeft = true;
      boolean firstField = true;
      IEditControlField pField = null;
      while ((pField = getNextField(pField)) != null)
      {
         String sName = pField.getName();
         String sLead = pField.getLeadSeparator();
         String sTrail = pField.getTrailSeparator();
         String sText = pField.getToolTipText();
         
         ITranslator pTranslator = pField.getTranslator();
         int nStart=pField.getFieldStartPos();
         int nEnd=pField.getFieldEndPos();
         //nEnd = pField.getFieldPos(nStart);
         
         // here we check for m_nCurrentPos to be greater than the start of field
         // this is b/c the preceeding field's end = this field's start and they will
         // fight over which should be selected.  So preference goes to the preceeding
         // field
         if( pTranslator != null && nStart <= getCurrentPosition() && nEnd >= getCurrentPosition() )
         {
            pTranslator.getTooltipText();
            String bstrLeft1 = pTranslator.getTooltipLeftText();
            String bstrSubject1 = pTranslator.getTooltipSubjectText();
            String bstrRight1 = pTranslator.getTooltipRightText();
            
            if (bLeft)
            {
               ETPairT<String, Boolean> retObj = getDelimitor(firstField);
               bstrLeft += retObj.getParamOne();
               firstField = retObj.getParamTwo().booleanValue();
               if (sLead != null)
               {
                  bstrLeft   += sLead;
               }
               bstrLeft   += bstrLeft1;
               bstrSubject = bstrSubject1;
               bstrRight  += bstrRight1;
               if (sTrail != null)
               {
                  bstrRight  += sTrail;
               }
               if (bstrSubject1 != null && bstrSubject1.length() > 0)
               {
                  bLeft = false;
               }
            }
            else
            {
               ETPairT<String, Boolean> retObj = getDelimitor(firstField);
               bstrRight += retObj.getParamOne();
               firstField = retObj.getParamTwo().booleanValue();
               if (sLead != null)
               {
                  bstrRight  += sLead;
               }
               bstrRight  += bstrLeft1;
               bstrRight  += bstrSubject1;
               bstrRight  += bstrRight1;
               if (sTrail != null)
               {
                  bstrRight  += sTrail;
               }
            }
         }
         else
         {
            if (pField.equals(pCurrField))
            {
               ETPairT<String, Boolean> retObj = getDelimitor(firstField);
               bstrLeft += retObj.getParamOne();
               firstField = retObj.getParamTwo().booleanValue();
               bstrSubject = sText;
               bLeft = false;
            }
            else if (bLeft)
            {
               ETPairT<String, Boolean> retObj = getDelimitor(firstField);
               bstrLeft += retObj.getParamOne();
               firstField = retObj.getParamTwo().booleanValue();
               bstrLeft += sText;
            }
            else if( !bLeft )
            {
               ETPairT<String, Boolean> retObj = getDelimitor(firstField);
               bstrRight += retObj.getParamOne();
               firstField = retObj.getParamTwo().booleanValue();
               bstrRight += sText;
            }
         }
      }
      m_TooltipLeftText = bstrLeft;
      m_TooltipSubjectText = bstrSubject;
      m_TooltipRightText = bstrRight;
      
      return bstrLeft + bstrSubject + bstrRight;
   }
   
   /**
    *
    * Updates the edit tool tip text. Text is retrieved from each text field's TooltipText property
    * and concatenated together.  The currently active field is highlighted.
    *
    * @return HRESULT
    *
    */
   public void updateToolTip()
   {
      if (m_EditControl != null)
      {
         String tooltip = getTooltipText();
         m_EditControl.setTooltipText(m_TooltipLeftText,
         m_TooltipSubjectText,
         m_TooltipRightText);
      }
   }
   
   /**
    * Retrieves the property elements that represent the elements data.
    * the property elements structure will be dictated by the specified
    * property elements.
    *
    * @param pElement [in] The element that is being processed.
    * @param pVal [out] The propety element.
    */
   private IPropertyElement getPropertyElement(IElement pElement)
   {
      IPropertyElement retEle = null;
      
      m_DefinitionFactory = new PropertyDefinitionFactory();
      m_ElementManager = new PropertyElementManager();
      ICoreProduct prod = ProductRetriever.retrieveProduct();
      if (prod != null)
      {
         IConfigManager conMan = prod.getConfigManager();
         if (conMan != null)
         {
            String loc = conMan.getDefaultConfigLocation();
            loc += "JavaLanguage.etc";
            m_DefinitionFactory.setDefinitionFile(loc);
         }
      }
      m_DefinitionFactory.buildDefinitionsUsingFile();
      m_ElementManager.setPDFactory(m_DefinitionFactory);
      m_ElementManager.setModelElement(pElement);
      m_ElementManager.setCreateSubs(true);
      
      IPropertyDefinition pDef = m_DefinitionFactory.getPropertyDefinitionForElement("", pElement);
      if (pDef != null)
      {
         if((pDef.getID() != null) && (pDef.getID().length() > 0))
         {
            try
            {
               Class clazz = Class.forName(pDef.getID());
               Class[] params = null;//{com.embarcadero.com.Dispatch.class};
               Constructor constructor = clazz.getConstructor(params);
               
               Object[] paramInstances =
               {pElement};
               pElement = (IElement)constructor.newInstance(paramInstances);
            } catch (Exception e)
            {}
         }
         retEle = m_ElementManager.buildTopPropertyElement(pDef);
         //			if (retEle != null)
         //			{
         //				retEle = m_ElementManager.buildElement(pElement, pDef, null);
         //			}
      }
      
      return retEle;
   }
   
        /*
         * Puts text fields in order, sets starting and ending positions.
         *
         */
   public void initTextFields()
   {
      removeFields();
      if (m_Element != null)
      {
         // only want to be cocreating this guy once
         IDataFormatter pFormatter = ProductHelper.getDataFormatter();
         if (pFormatter != null)
         {
            // call data formatter to get the proper property element for our model element
            m_PropertyElement = pFormatter.getPropertyElement(m_Element);
            if (m_PropertyElement != null)
            {
               // now iterate through each sub element, these will contain our individual field data
               Vector subEles = m_PropertyElement.getSubElements();
               if (subEles != null)
               {
                  int count = subEles.size();
                  for (int i=0; i<count; i++)
                  {
                     IPropertyElement pEle = (IPropertyElement)subEles.elementAt(i);
                     addField(pEle, false);
                  }
               }
            }
            else
            {
               addField("No Property Element!", "");
            }
         }
      }
      
      // put the text fields in order
      updateFieldPositions(null);
      registerAccelerators();
      
   }
   
        /*
         * Replaces text in a field, updates the edit control and current position
         * if nStartPos or nEndPos are empty replaces from the beginning/end of the field
         * if sText is empty, deletes the text between nStartPos and nEndPos.
         *
         * @param pField[in]
         * @param nStartPos
         * @param nEndPos
         * @param sText
         * @param pUpdate
         */
   private boolean replaceText(IEditControlField pField,
   int nStartPos, int nEndPos,
   String sText , boolean bUpdate)
   {
      boolean retVal = true;
      if (pField == null)
      {
         return retVal;
      }
      
      // get field's current text and position
      String fieldText = pField.getText();
      int startPos=pField.getTextStartPos();
      int endPos=pField.getTextEndPos();
      //endPos = pField.getTextPos(startPos);
      
      // set defaults
      if (nStartPos == -1 || nStartPos < startPos || nStartPos > endPos)
      {
         nStartPos = startPos;
      }
      if (nEndPos == -1 || nEndPos > endPos || nEndPos < startPos)
      {
         nEndPos = endPos;
      }
      
      // calc offsets within string to insert/replace
      int n1 = nStartPos - startPos;
      int n2 = nEndPos - nStartPos;
      
      //		ITranslator trans = pField.getTranslator();
      //		if (trans == null)
      //		{
      //			//I want to set the proper text only if the translator is
      //			//null, else the fieldText would be correctly calculated.
      //			if (fieldText != null && fieldText.length() == 0)
      //			{
      //				fieldText = sText;
      //			}
      //			else
      //			{
      //				String leftText = "";
      //				String rightText = "";
      //				int nStart = pField.getTextStartPos();
      //				//we might be adding value for a new field in which case
      //				//initText will be "".
      //				if (nStartPos > startPos && fieldText.length() >= (nStartPos - startPos))
      //				{
      //					leftText = fieldText.substring(0, nStartPos - startPos);
      //					if (n2 > 0)
      //					{
      //						rightText = fieldText.substring(n1+n2);
      //					}
      //					else if (n1 == 0)
      //					{
      //						//we are adding text to the beginning
      //						rightText = fieldText;
      //					}
      //					else if (sText.length() > 0)
      //					{
      //						//we are inserting a character
      //						rightText = fieldText.substring(nStartPos-startPos);
      //					}
      //				}
      //				else if (nStartPos == startPos)
      //				{
      //					if (n2 > 0)
      //					{
      //						rightText = fieldText.substring(n1+n2);
      //					}
      //					else if (n1 == 0)
      //					{
      //						//we are adding text to the beginning
      //						rightText = fieldText;
      //					}
      //					//we are adding somthing to the start of this field
      //					//rightText = fieldText;
      //				}
      //				fieldText = leftText + sText + rightText;
      //			}
      //		}
      
      if (n2 > 0)
      {
         //we are replacing some part
         //String strToReplace = fieldText.substring(n1, n2);
         fieldText = fieldText.substring(0, n1) + sText + fieldText.substring(n1+n2);
      }
      else
      {
         //we are just inserting
         fieldText = fieldText.substring(0, n1) + sText + fieldText.substring(n1+n2);
      }
      setCurrentPosition(nStartPos + sText.length());
      
      // by updating the text we assume we want this field visible
      pField.setVisible(true);
      
      pField.setText(fieldText);
      
      // inform parent something's been changed
      setModified(true);
      
      // update edit control
      if (bUpdate)
      {
         refreshEditControl();
      }
      nStartPos += pField.getTextStartPos() - startPos;
      setPosition(nStartPos + sText.length(), nStartPos + sText.length());
      m_Modified = true;
      
      return retVal;
   }
   
        /*
         * Toggles a field on or off, does not refresh the edit control.
         *
         * @param pField[in]
         * @param bVisible
         */
   private void setFieldVisible( IEditControlField pField, boolean bVisible )
   {
      if (pField == null)
      {
         return;
      }
      
      // determine the current state
      boolean visible = pField.getVisible();
      
      // if anything to do change the field's state and reposition trailing fields
      if (visible != bVisible)
      {
         pField.setVisible(bVisible);
         
         // move trailing fields
         updateFieldPositions(null);
      }
   }
   
   /**
    * Updates the edit control with the contents of the fields.
    */
   private void refreshEditControl()
   {
      if (m_EditControl != null)
      {
         m_EditControl.refresh();
         m_EditControl.setSel(getCurrentPosition(), getCurrentPosition());
         updateHints();
      }
   }
   
   /**
    * Selects the entire field in the edit control.
    *
    * @param pField[in]
    */
   private void selectField( IEditControlField pField )
   {
      if (pField == null)
      {
         return;
      }
      
      ITranslator translator = pField.getTranslator();
      if (translator != null)
      {
         // defer to contained translator, since we don't know what to select have it
         // behave as if it has been first selected.
         translator.onSetFocus();
      }
      else
      {
         // KSL this used to test if visible however we now allow the caret to be placed at the beginning of the
         // default field, which could be hidden at present.
         int startPos=pField.getTextStartPos();
         int endPos=pField.getTextEndPos();
         //endPos = pField.getTextPos(startPos);
         setSel(startPos, endPos);
         setCurrentPosition(startPos);
      }
      updateHints();
   }
   
   private IEditControlField addField(IEditControlField pField)
   {
      Vector pFields = getTextFields();
      if (pField != null)
      {
         pFields.add(pField);
      }
      return pField;
   }
   
   public IEditControlField addFieldDefinition(IPropertyDefinition pDef,
   IEditControlField pInsField)
   {
      IEditControlField retField = null;
      Vector pFields = getTextFields();
      if (pFields != null)
      {
         IEditControlField pField = new EditControlField();
         pField.setOwnerTranslator(this);
         pField.setPropertyDefinition(pDef);
         IEditControl editCtrl = getEditControl();
         
         ITranslator translator = pField.getTranslator();
         if (translator != null)
         {
            translator.setEditControl2(editCtrl);
         }
         
         if (pInsField == null)
         {
            // insert at the end
            pFields.add(pField);
         }
         else
         {
            // find our insertion point
            int pos=0;
            for (int i=0; i<pFields.size(); i++)
            {
               IEditControlField field = (IEditControlField)pFields.elementAt(i);
               if (field.equals(pInsField))
               {
                  break;
               }
               pos++;
            }
            pFields.insertElementAt(pField, pos);
         }
         
         retField = pField;
         
         // add our separators and delimitors to the edit control's master list
         if (m_EditControl != null && pField != null)
         {
            String sTemp = m_EditControl.getSeparatorList();
            String separators = sTemp;
            
            String str = pField.getLeadSeparator();
            separators = updateSeparatorList(separators, str);
            
            str = pField.getTrailSeparator();
            separators = updateSeparatorList(separators, str);
            
            str = pField.getDelimitor();
            separators = updateSeparatorList(separators, str);
            
            sTemp = separators;
            m_EditControl.setSeparatorList(sTemp);
         }
      }
      return retField;
   }
   
   private void removeFields()
   {
      if (m_TextFields != null)
      {
         m_TextFields.removeAllElements();
      }
   }
   
   /**
    * Returns the minimum bounding rect around the text given the current font.
    *
    * @param hDC[in]
    * @param size[out]
    */
   private Dimension getTextExtent()
   {
      Dimension retObj = new Dimension();
      Dimension minSize = new Dimension();
      minSize.height = 0;
      minSize.width = 0;
      int count = m_TextFields.size();
      for (int i=0; i<count; i++)
      {
         IEditControlField pField = (IEditControlField)m_TextFields.elementAt(i);
         Dimension sz = getTextExtent(pField);
         if (sz.height > minSize.height)
         {
            minSize.height = sz.height;
         }
         if (sz.width > minSize.width)
         {
            minSize.width = sz.width;
         }
      }
      retObj.height = minSize.height;
      retObj.width = minSize.width;
      return retObj;
   }
   
   /**
    * Internal helper for get_TextExtent.
    *
    * @param pField[in]
    */
   private Dimension getTextExtent(IEditControlField pField)
   {
      Dimension retObj = new Dimension();
      if (pField != null)
      {
         String text = pField.getText();
         Font font = pField.getFont();
         
         //now use this font and text to determine how much should be the width and height.
      }
      return retObj;
   }
   
        /*
         * adds the field for this property element
         */
   public void addField(IPropertyElement pData, boolean bNoUpdate)
   {
      IEditControlField pField = null;
      IPropertyDefinition pDef = null;
      if (pData != null)
      {
         pDef = pData.getPropertyDefinition();
      }
      
      if (pDef != null)
      {
         String name = pDef.getName();
         boolean found = false;
         
         // look for an existing property definition
         IEditControlField pTempField = getNextField(null);
         while (pTempField != null)
         {
            IPropertyElement pTempEle = pTempField.getPropertyElement();
            IPropertyDefinition pTempDef = pTempField.getPropertyDefinition();
            if (pTempDef != null && pTempDef.equals(pDef))
            {
               if (pTempEle == null)
               {
                  // found an existing, empty field using our definition.
                  IDataFormatter pFormatter = ProductHelper.getDataFormatter();
                  if (pFormatter != null)
                  {
                     //pFormatter.processEnumeration(pData);
                  }
                  
                  // Attach ourselves to this field
                  if (bNoUpdate)
                  {
                     pTempField.setPropertyElement2(pData);
                  }
                  else
                  {
                     pTempField.setPropertyElement(pData);
                  }
                  found = true;
                  break;
               }
            }
            pTempField = getNextField(pTempField);
         }
         
         // if no empty fields using this definition, create and append a new field
         if (!found)
         {
            // create new textfield, add to end of the list
            pField = new EditControlField();
            
            // provide the field with a back pointer to this translator
            pField.setOwnerTranslator(this);
            
            // if the raw data contains enumerated values, convert them here
            IDataFormatter pFormatter = ProductHelper.getDataFormatter();
            if (pFormatter != null)
            {
               //pFormatter.processEnumeration(pData);
            }
            
            // add the field to our array
            addField( pField );
            
            // save the data into the field, this reads the formatting information into the field
            pField.setPropertyElement(pData);
            
            // now, if the field contains sub-fields, set their edit control (I don't think this should be necessary)
            ITranslator pTrans = pField.getTranslator();
            if (pTrans != null)
            {
               IEditControl pCtrl = getEditControl();
               pTrans.setEditControl2(pCtrl);
            }
         }
         
         // add our separators and delimitors to the edit control's master list
         if (m_EditControl != null && pField != null)
         {
            String sTemp = m_EditControl.getSeparatorList();
            String separators = sTemp;
            
            sTemp = pField.getLeadSeparator();
            separators = updateSeparatorList(separators, sTemp);
            
            sTemp = pField.getTrailSeparator();
            separators = updateSeparatorList(separators, sTemp);
            
            sTemp = pField.getDelimitor();
            separators = updateSeparatorList(separators, sTemp);
            
            sTemp = separators;
            m_EditControl.setSeparatorList(sTemp);
         }
      }
   }
   
   private String updateSeparatorList(String separators, String str)
   {
      String retVal = separators;
      if (separators != null && str != null)
      {
         int length = str.length();
         for (int i=0; i<length; i++)
         {
            char c = str.charAt(i);
            int pos = separators.indexOf(c);
            if (pos < 0)
            {
               separators += c;
            }
         }
         retVal = separators;
      }
      return retVal;
   }
   
   private IEditControlField addField(String text, String tooltipText)
   {
      IEditControlField pField = null;
      Vector fields = getTextFields();
      if (fields != null)
      {
         pField = new EditControlField();
         pField.init(text, null, null);
         if (tooltipText != null && tooltipText.length() > 0)
         {
            pField.setToolTipText(tooltipText);
         }
         fields.add(pField);
      }
      return pField;
   }
   
   /**
    * Sets the edit control's tooltip and hint bar
    */
   public void updateHints()
   {
      if (m_EditControl != null)
      {
         m_EditControl.hideHintBar();
         IEditControlField pField = getCurrentField();
         if (pField != null)
         {
            int kind = pField.getEditKind();
            
            if( kind == ITextFieldEditKind.TFEK_LIST ||
            kind == ITextFieldEditKind.TFEK_COMBO )
            {
               int nStart=pField.getTextStartPos();
               int nEnd=pField.getTextEndPos();
               //nEnd = pField.getTextPos(nStart);
               m_EditControl.showHintBar(nStart);
            }
         }
         m_EditControl.updateToolTip();
      }
   }
   
   /**
    * Sets the edit control's tooltip and hint bar
    */
   public void handleHint()
   {
      if (m_EditControl != null)
      {
         IEditControlField pField = getCurrentField();
         if (pField != null)
         {
            ITranslator pTrans = pField.getTranslator();
            if (pTrans != null)
            {
               pTrans.handleHint();
            }
            else
            {
               IPropertyElement pEle = pField.getPropertyElement();
               IPropertyDefinition pDef = pField.getPropertyDefinition();
               IStrings list = null;
               
               if (pDef != null && pEle != null)
               {
                  // if we have this information, use it to determine the picklist
                  // we will have this if the item that we are editing has a model element
                  // associated with it
                  list = pDef.getValidValue(pEle);
               }
               else
               {
                  // we do not have a property element, so this is a new element (insert attribute
                  // for example)
                  // in order to get the picklist manager, we need the project, since we have no
                  // model element, we have no way of getting the project
                  IProduct prod = ProductHelper.getProduct();
                  if (prod != null)
                  {
                     IProductProjectManager pMan = prod.getProjectManager();
                     if (pMan != null)
                     {
                        IProject proj = pMan.getCurrentProject();
                        if (proj != null)
                        {
                           ITypeManager typeMan = proj.getTypeManager();
                           if (typeMan != null)
                           {
                              IPickListManager pickMan = typeMan.getPickListManager();
                              if (pickMan != null)
                              {
                                 String filter = "DataType Class Interface";
                                 list = pickMan.getTypeNamesWithStringFilter(filter);
                                 
                                 filter = "ParameterableElement";
                                 Object mElem = null;
                                 IEditControlField field = getCurrentField();
                                 while(mElem == null && field != null) {
                                     IPropertyElement pe = field.getPropertyElement();
                                     if (pe != null) 
                                     {
                                         mElem = pe.getElement();
                                     }
                                     if (mElem == null) 
                                     {
                                         ITranslator translator = field.getOwnerTranslator();
                                         if (translator != null) 
                                         {
                                             field = translator.getParentField();
                                         }
                                         else
                                         {
                                             break;
                                         }
                                     }
                                 }

                                 INamespace space = null;
                                 if (mElem instanceof INamespace) 
                                 {
                                     space = (INamespace)mElem;
                                 }
                                 else if (mElem instanceof INamedElement)
                                 {
                                     space = ((INamedElement)mElem).getNamespace();
                                 }
                                 list.append(pickMan.getTypeNamesWithStringFilterNamespaceVisible
                                             (filter, false, space));                         
                              }
                           }
                        }
                     }
                  }
               }
               if (list != null)
               {
                  list = ensureSorted(list);
                  long count = list.getCount();
                  if (count > 0)
                  {
                     // right now we assume we're dealing with a list
                     int nStart=pField.getTextStartPos();
                     int nEnd=pField.getTextEndPos();
                     //nEnd = pField.getTextPos(nStart);
                     selectField(pField);
                     
                     // fetch seed value from our text
                     String text = pField.getText();
                     
                     // launch the listbox
                     m_EditControl.displayList(true, list, nStart, text);
                  }
               }
            }
         }
      }
   }
   
   private IStrings ensureSorted(IStrings list)
   {
      if (list != null)
      {
         Object[] objs = list.toArray();
         if (objs != null)
         {
            Arrays.sort(objs);
            list.clear();
            for (int i=0; i<objs.length; i++)
            {
               list.add((String)objs[i]);
            }
         }
      }
      return list;
   }
   
   /**
    * Copies the changed data back into our property elements
    *
    */
   public void saveFields()
   {
      if (m_TextFields != null)
      {
         int count = m_TextFields.size();
         if (count > 0)
         {
            if (m_Element instanceof IOperation)
            {
               IOperation oper = (IOperation)m_Element;
               
               // We need to put the dispatch controller into a
               // batch mode, specifically an IOperationSignatureChangeContext
               // mode in order to not process the individual events coming
               // resulting from the various field saves
               
               IOperationSignatureChangeContextManager pSigChange = new OperationSignatureChangeContextManager();
               try
               {
                  pSigChange.startSignatureChange(oper);
                  saveFields(count);
               }
               finally
               {
                  pSigChange.endSignatureChange();
               }
            }
            else
            {
               saveFields(count);
            }
         }
      }
   }
   
   /**
    *
    * Saves all the fields in the m_Fields collection
    *
    * @param numFields[in] The number of fields in m_Fields
    *
    * @return HRESULT
    *
    */
   private void saveFields( int numFields )
   {
      boolean bCancel = false;
      for (int i=0; i<numFields; i++)
      {
         IEditControlField pField = (IEditControlField)m_TextFields.elementAt(i);
         
         // cancel flag gets set if this node gets deleted during the save
         bCancel = pField.save();
         if (bCancel)
         {
            break;
         }
      }
   }
   
   public boolean canNavigateSeparator(int nChar)
   {
      IEditControlField pField = getCurrentField();
      
      boolean isInert = false;
      if(pField != null)
      {
         isInert = pField.checkInertSeparator(nChar, getPosition());
      }
      
      return (isInert == false);
   }
   
   /**
    * Get the value of a text field contained by this translat
    * (cannot be used for separator fields).
    *
    * @param nKind[in]
    * @param sText[out]
    */
   private String getFieldText(IEditControlField pField)
   {
      String retText = "";
      boolean visible = false;
      if (pField != null)
      {
         visible = pField.getVisible();
         if (visible)
         {
            retText = pField.getText();
         }
      }
      return retText;
   }
   
   /**
    *
    * Return the selection range of the edit control.  If no text is selected
    * both return values will indicate the current cursor position.
    *
    * @param nStartPos[out] The first selected character position.
    * @param nEndPos[out] The last selected character position.
    *
    * @return
    *
    */
   private void getSel( int nStartPos, int nEndPos )
   {
      if (m_EditControl != null)
      {
         m_EditControl.getSel(nStartPos, nEndPos);
      }
      else
      {
         // can't simulate selection if no edit control
         nStartPos = getCurrentPosition();
         nEndPos   = getCurrentPosition();
      }
   }
   
   /**
    *
    * Selects a range of text in the edit control.
    *
    * @param nStartPos[in] The first selected character position
    * @param nEndPos[in] The last selected character position
    *
    * @return
    *
    */
   public void setSel( int nStartPos, int nEndPos )
   {
      try
      {
         if( m_EditControl != null )
         {
            m_EditControl.setSel( nStartPos, nEndPos );
         }
      }
      catch ( Exception e )
      {
      }
   }
   
   /**
    *
    * Returns the length of the text in the edit control.
    *
    * @param nLength[out] The string length
    * @param bOverallLength[in] For multi-line edit controls, the function can
    * return the overall length of all text in the control (true), or just the
    * length of the current line (false)
    *
    * @return
    *
    */
   private int getLineLength(boolean bOverallLength)
   {
      int length = 0;
      if (m_EditControl != null)
      {
         int index = 0;
         if (bOverallLength)
         {
            index = m_EditControl.lineIndex(-1);
         }
         length = m_EditControl.lineLength(-1);
         length += index;
      }
      else
      {
         // no edit control, return length of all visible fields
         IEditControlField pField = getNextVisibleField(null);
         while (pField != null)
         {
            String str = pField.getText();
            length += str.length();
            pField = getNextVisibleField(pField);
         }
      }
      return length;
   }
   
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.editcontrol.ITranslator#onMouseMove(int, int)
         */
   public void onMouseMove(int x, int y)
   {
      // TODO Auto-generated method stub
   }
   
   /**
    * Updates text in response to the hint list being closed
    */
   public IEditControlField handleHintText(String sText)
   {
      IEditControlField pField = getCurrentField();
      if (pField != null)
      {
         ITranslator pTrans = pField.getTranslator();
         if (pTrans != null)
         {
            pField = pTrans.handleHintText(sText);
         }
         else
         {
            pField.setText(sText);
            
            int nStart=pField.getTextStartPos();
            int nEnd=pField.getTextEndPos();
            //nEnd = pField.getTextPos(nStart);
            setCurrentPosition(nEnd);
            refreshEditControl();
         }
      }
      
      return pField;
   }
   
   /**
    * Does one of this translator's text fields have a separator for this character?  If so
    * perform the appropriate action.  The default is to select the field, or insert a new
    * one if entering a comma for repeating fields.
    *
    * @param[in] nChar The keycode passed during WM_KEYDOWN message processing.
    * @param[in] nCurrentPos The current caret position.
    * @param[out,retval] bHandled Returns TRUE if this character is a separator for one of the
    * textfields contained by this translator.
    * @param[out,retval] bHandled Returns TRUE if this character is a separator for one of the
    * textfields contained by this translator.
    *
    * @return HRESULT
    */
   public ETPairT<Boolean, Boolean> handleSeparator(int nChar, int nCurrentPos)
   {
      boolean isHandled = false;
      boolean bSelectNextField = false;
      setCurrentPosition(nCurrentPos);
      
      //		//////////////////////////////////////////////////////
      //
      //			  All cases:
      //			  1) Translators typically contain 1 or more textfields. Shortcut characters are special characters
      //				 that the user types to quickly navigate to a field. The character is always a non-space, non-text
      //				 (symbolic) such as ":" or "=".
      //
      //			  2) Always begin searching for the shortcut by looking in all fields TO THE RIGHT of the current
      //				 caret position.  Search visible and hidden fields as the user may be wishing to activate a
      //				 hidden field.  If not found resume the search from the beginning of text.
      //
      //				 If a text field is found for the shortcut, move the caret and select the text.  If the field is
      //				 hidden or empty, place the caret where the text should begin.
      //
      //			  Specific cases
      //			  3) This translator contains normal text fields, in which case the accelerator is one of the
      //				 non-text, non-space characters in either the leading or the trailing separator of one of
      //				 the fields. Search the field list as described in para 2), if found select the field.
      //
      //			  4) This translator contains a single text field that contains another translator (nested fields).
      //				 First look for this field's shortcut in its leading and trailing separator, if found select
      //				 the first text field contained by the nested translator.
      //
      //				 If this field does not have a shortcut pass the search to the nested translator to let it search.
      //				 If it finds a shortcut it handles it appropriately.  If not found it returns FALSE, and we continue
      //				 searching our remaining fields as described in paragraph 2.
      //
      //			  5) If the translator contains repeating fields (multiplicity = 0 or multiplicity > 1) and no shortcut was found
      //				 of the existing fields, check for the delimitor character.  If found the user is attempting to
      //				 navigate among the repeating fields, or insert a new field.  First test for succeeding fields
      //				 past the current caret position (the current field).  If found they will be separated by the
      //				 delimitor character.  Treat this as a regular shortcut, that is move the caret to the first
      //				 text field in the repeating field and select it.  If no more fields follow the current field or the
      //				 current field is empty/hidden, create a new repeating field. If fields already exist place
      //				 the new field after the last field in the list, separated by the delimiter character, otherwise
      //				 the new field becomes the first field in the list.
      //
      //			  6) Consider Ctrl+delimitor to mean to insert a new repeating field at the current caret position.
      //
      //		///////////////////////////////////////////////////////
      
      IEditControlField pField = getCurrentField();
      boolean isDuplicate = isDuplicateSeperator(pField, nChar, nCurrentPos);
      if (isDuplicate)
      {
         isHandled = true;
         return new ETPairT<Boolean, Boolean>(Boolean.valueOf(isHandled), Boolean.valueOf(bSelectNextField));
      }
      
      // first thing we do is check the current field
      if (pField != null)
      {
         // this could return false if the current position is within the
         // current field (check goes from current pos to the right)
         ETPairT<Boolean, Boolean> retObj = handleSeparator( pField, nChar, nCurrentPos);
         isHandled = ((Boolean)retObj.getParamOne()).booleanValue();
         bSelectNextField = ((Boolean)retObj.getParamTwo()).booleanValue();
      }
      
      // no current field or current field did not handle the separator
      if (!isHandled)
      {
         pField = getNextField(pField);
         
         // current field didnt' handle it, now we check successive fields
         while (pField != null)
         {
            ETPairT<Boolean, Boolean> retObj = handleSeparator( pField, nChar, nCurrentPos);
            isHandled = ((Boolean)retObj.getParamOne()).booleanValue();
            bSelectNextField = ((Boolean)retObj.getParamTwo()).booleanValue();
            
            if (isHandled)
            {
               break;
            }
            pField = getNextField(pField);
         }
      }
      
      if (isHandled)
      {
         if (bSelectNextField)
         {
            boolean visible = pField.getVisible();
            if (visible)
            {
               // found trailing separator, select following field
               pField = getNextVisibleField(pField);
               if (pField == null)
               {
                  // no more fields, ask our parent to select it for us
                  bSelectNextField = true;
               }
               else
               {
                  selectField(pField);
               }
            }
         }
      }
      return new ETPairT<Boolean, Boolean>(Boolean.valueOf(isHandled), Boolean.valueOf(bSelectNextField));
   }
   
   /**
    * Helper to test a field for separators
    *
    * @param[in] <Name Description>
    *
    * @return HRESULT
    */
   private ETPairT<Boolean, Boolean> handleSeparator(IEditControlField pField,
   int nChar, int nCurrentPos)
   {
      boolean isHandled = false;
      boolean bSelectNextField = false;
      if (pField != null)
      {
         boolean visible = pField.getVisible();
         
         if(handleLeadSeperator(pField,nChar,nCurrentPos))
         {
            isHandled = true;
         }
         else if( visible )
         {
            ETPairT<Boolean, Boolean> retObj = handleSubFieldSeperator(pField,nChar,nCurrentPos);
            isHandled = ((Boolean)retObj.getParamOne()).booleanValue();
            bSelectNextField = ((Boolean)retObj.getParamTwo()).booleanValue();
            if (isHandled)
            {
               return new ETPairT<Boolean, Boolean>(Boolean.valueOf(isHandled), Boolean.valueOf(bSelectNextField));
            }
            
            if (handleTrailSeperator(pField, nChar))
            {
               isHandled = true;
               IEditControlField pNextField = getNextVisibleField(pField);
               
               if (pNextField == null)
               {
                  pNextField = getNextField(pField);
               }
               
               pField = pNextField;
               if (pField == null)
               {
                  // no more fields, ask our parent to select it for us
                  bSelectNextField = true;
               }
               else
               {
                  selectField(pField);
               }
            }
         }
         
         if (isHandled && pField != null)
         {
            // we have a separator, make the field visible and selected
            // we only need to do the current field
            if (m_EditControl != null)
            {
               IEditControlField pCurrentField = m_EditControl.getCurrentField();
               if (pCurrentField != null && !pCurrentField.equals(pField))
               {
                  updateVisibleFields(pCurrentField);
               }
            }
            pField.setVisible(true);
            refreshEditControl();
            selectField(pField);
         }
      }
      return new ETPairT<Boolean, Boolean>(Boolean.valueOf(isHandled), Boolean.valueOf(bSelectNextField));
   }
   
   /**
    * Inserts a new field in response to the delimitor character having been entered.
    *
    * @param[in] nChar The keycode passed during WM_KEYDOWN message processing.
    *
    * @return HRESULT
    */
   public boolean handleDelimitor( int nChar, int nCurrentPos)
   {
      boolean isHandled = false;
      setCurrentPosition(nCurrentPos);
      
      if(isOnDelimitor(nChar))  // Jump to next field or insert one.
      {
         isHandled = true;
         boolean isControlDown = false; //ctrlDown();
         if (isControlDown)
         {
            IEditControlField pField = getCurrentField();
            if (pField != null)
            {
               createField(pField, true);
            }
            else
            {
               //don't allow this input.
            }
         }
         else
         {
            if( getPreviousVisibleField(null) != null && jumpToNextField() == null)
            {
               // this is the last field, add a new one at the end
               IEditControlField pField = getPreviousField(null);
               createField(pField, false);
            }
         }
      }
      
      return isHandled;
   }
   
   /**
    * Top level separator handler, this routine gets called once by the edit control for the
    * top level translator;
    *
    * @param[in] nChar The keycode passed during WM_KEYDOWN message processing.
    * @param[out,retval] bHandled Returns TRUE if this character is a separator for one of the
    * textfields contained by this translator.
    *
    * @return HRESULT
    */
   public boolean handleTopLevelSeparators(int nChar)
   {
      boolean isHandled = false;
      if (canNavigateSeparator(nChar))
      {
         boolean bSelectNextField = false;
         IEditControlField pField = getCurrentField();
         int nCurrentPos = getCurrentPosition();
         
         // first thing we do is check the current field
         if (pField != null)
         {
            // this could return false if the current position is within the
            // current field (check goes from current pos to the right)
            ETPairT<Boolean, Boolean> retObj = handleSeparator(pField, nChar, nCurrentPos);
            isHandled = ((Boolean)retObj.getParamOne()).booleanValue();
            bSelectNextField = ((Boolean)retObj.getParamTwo()).booleanValue();
         }
         
         // no current field or current field did not handle the separator
         if (!isHandled)
         {
            IEditControlField pStartField = pField;
            pField = getNextField(pField);
            
            // current field didnt' handle it, now we check successive fields
            while (pField != null && !pField.equals(pStartField) && !isHandled)
            {
               ETPairT<Boolean, Boolean> retObj = handleSeparator(pField, nChar, nCurrentPos);
               isHandled = ((Boolean)retObj.getParamOne()).booleanValue();
               bSelectNextField = ((Boolean)retObj.getParamTwo()).booleanValue();
               
               if (isHandled)
               {
                  break;
               }
               pField = getNextField(pField);
            }
         }
         
         if (isHandled)
         {
            if (bSelectNextField)
            {
               boolean visible = pField.getVisible();
               if (visible)
               {
                  // found trailing separator, select following field
                  pField = getNextVisibleField(pField);
                  if (pField != null)
                  {
                     selectField(pField);
                  }
               }
            }
         }
      }
      return isHandled;
   }
   
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.editcontrol.ITranslator#dump(java.lang.String)
         */
   public void dump(String sPad)
   {
      // TODO Auto-generated method stub
   }
   
   /**
    * Returns the field following this field, NULL if none.
    *
    * @param pPrevious The field from which to start the search.  If NULL then the first field is returned;
    */
   public IEditControlField getNextField(IEditControlField pPreviousField)
   {
      IEditControlField retObj = null;
      if (m_TextFields != null)
      {
         int count = m_TextFields.size();
         boolean found = false;
         for (int i=0; i<count; i++)
         {
            retObj = (IEditControlField)m_TextFields.elementAt(i);
            if (found || pPreviousField == null)
            {
               break;
            }
            if (retObj.equals(pPreviousField))
            {
               found = true;
            }
            retObj = null;
         }
      }
      return retObj;
   }
   
   /**
    * Returns the next visible field following this field, NULL if none. The field must be both visible and enabled.
    *
    * @param pPrevious The field from which to begin the search.  If NULL then the first visible field is returned.
    */
   private IEditControlField getNextVisibleField( IEditControlField pPrevious )
   {
      IEditControlField retField = null;
      
      // KSL return the next visible field OR the current field even if not visible
      IEditControlField pCurrentField = getCurrentField();
      
      retField = getNextField(pPrevious);
      while (retField !=  null)
      {
         boolean visible = false;
         visible = retField.getVisible();
         
         // KSL used to be visible AND enabled
         if (visible || retField.equals(pCurrentField))
         {
            break;
         }
         
         retField = getNextField(retField);
      }
      
      return retField;
   }
   
   /**
    * Returns the next visible field preceeding this field, NULL if none.
    *
    * @param pNext[in]
    */
   private IEditControlField getPreviousVisibleField(IEditControlField pNext)
   {
      IEditControlField retField = null;
      retField = getPreviousField(pNext);
      while (retField != null)
      {
         boolean visible = retField.getVisible();
         if (visible)
         {
            break;
         }
         retField = getPreviousField(retField);
      }
      return retField;
   }
   
   /**
    * Returns the field preceeding this field, NULL if none.  If pNext is null returns the last field.
    *
    * @param pNext[in]
    */
   public IEditControlField getPreviousField(IEditControlField pNextField)
   {
      IEditControlField pField = null;
      IEditControlField pTest = null;
      
      if (m_TextFields != null)
      {
         int count = m_TextFields.size();
         if (pNextField != null)
         {
            for (int i=0; i<count; i++)
            {
               pTest = (IEditControlField)m_TextFields.elementAt(i);
               if (pTest.equals(pNextField))
               {
                  break;
               }
               pField = null;
               pField = pTest;
               pTest = null;
            }
         }
         else
         {
            pField = (IEditControlField)m_TextFields.elementAt(count - 1);
         }
      }
      
      return pField;
   }
   
   /**
    * Returns the default field, this is the field that gets initial editing focus.
    *
    * @param pPrevious The field from which to begin the search.  If NULL then the first visible field is returned.
    */
   private IEditControlField getDefaultField()
   {
      IEditControlField retField = null;
      retField = getNextField(null);
      while (retField != null)
      {
         boolean bDefault = retField.getDefault();
         if (bDefault)
         {
            break;
         }
         retField = getNextField(retField);
      }
      if (retField == null)
      {
         retField = getNextField(null);
      }
      
      return retField;
   }
   
   /**
    * Moves a field to the new starting position within the string.
    */
   private void moveField( IEditControlField pField, int nPosition )
   {
      if (pField != null)
      {
         pField.setFieldPos(nPosition);
      }
   }
   
   /**
    * Returns the first field in the list, NULL if none.
    */
   private IEditControlField getFirstField()
   {
      IEditControlField retField = null;
      if (m_TextFields != null)
      {
         int count = m_TextFields.size();
         if (count > 0)
         {
            retField = (IEditControlField)m_TextFields.elementAt(0);
         }
      }
      return retField;
   }
   
   /**
    * Returns the last field in the list, NULL if none
    */
   private IEditControlField getLastField()
   {
      IEditControlField retField = null;
      if (m_TextFields != null)
      {
         int count = m_TextFields.size();
         if (count > 0)
         {
            retField = (IEditControlField)m_TextFields.elementAt(count - 1);
         }
      }
      return retField;
   }
   
   /**
    * Updates all contained text fields with the property elements provided.  Does not load the
    * property element data into the field (existing field data is preserved).
    *
    * @param[in] pElements A collection of property elements to load.  Typically textfields containing
    * just PropertyDefinitions already exist, these elements will be attached to their respective fields.
    *
    * @return HRESULT
    */
   public void updateFields(Vector pElements)
   {
      if (pElements != null)
      {
         int count = pElements.size();
         for (int i=0; i<count; i++)
         {
            IPropertyElement subEle = (IPropertyElement)pElements.elementAt(i);
            addField(subEle, true);
         }
      }
   }
   
   /**
    * Helper function to returns the PropertyElement for a translator that
    * contains a single field (these fields usually contain nested fields)
    *
    * @param[out,retval] pElement The propertyElement. Return is NULL if the translator
    * does not contain 1 and only 1 textfield, or if the textfield's PropertyElement
    * has not been set.
    *
    * @return HRESULT
    */
   public IEditControlField getParentField()
   {
      return m_ParentField;
   }
   
   /**
    * Gets the translator that contains this this translator's owner text field. Valid only for
    * "nested" translators, that is translators that contain subfields.
    *
    * @param[out,retval] pTranslator The Translator that owns the textfield that owns this translator.
    *
    * @return HRESULT
    */
   public void setParentField(IEditControlField pField)
   {
      m_ParentField = pField;
   }
   
   /**
    * Shifts all fields following this one, used after resizing this field.
    *
    * @param pCurrent[in]
    */
   public void updateFieldPositions(IEditControlField pCurrent)
   {
      if (pCurrent == null)
      {
         pCurrent = getFirstField();
      }
      
      // if called before initializing this could be null
      if (pCurrent != null)
      {
         int startPos=pCurrent.getFieldStartPos();
         int endPos=pCurrent.getFieldEndPos();
         
         // fetch starting position
         //endPos = pCurrent.getFieldPos(startPos);
         moveField(pCurrent, startPos);
         
         // fetch new ending position
         endPos = pCurrent.getFieldEndPos();
         
         // find the next field
         IEditControlField pField = getNextField(pCurrent);
         while (pField != null)
         {
            moveField(pField, endPos);
            startPos = pField.getFieldStartPos();
            endPos = pField.getFieldEndPos();
            pField = getNextField(pField);
         }
      }
   }
   
   /**
    * Informs the field that editing has finished and it should apply whatever validation it needs to.
    * @param[in] pField The field to update, if NULL the current field is updated.
    *
    * @return HRESULT
    */
   public void updateField(IEditControlField pField)
   {
      if (pField == null)
      {
         pField = getCurrentField();
      }
      if (pField != null)
      {
         pField.update();
      }
   }
   
   /**
    * Is this translator deleted? A translator is deleted if all its contained fields are deleted.
    *
    * @param[out] bDeleted TRUE or FALSE
    *
    * @return HRESULT
    */
   public boolean getDeleted()
   {
      boolean isDeleted = false;
      IEditControlField pField = getNextField(null);
      while (pField != null)
      {
         isDeleted = pField.getDeleted();
         if (isDeleted)
         {
            break;
         }
         pField = getNextField(pField);
      }
      return isDeleted;
   }
   
   /**
    * Set this translator's deleted state. All contained fields are set to the newe deleted state.
    *
    * @param[in] bDeleted TRUE or FALSE
    *
    * @return HRESULT
    */
   public void setDeleted(boolean bDeleted)
   {
      IEditControlField pField = getNextField(null);
      while (pField != null)
      {
         pField.setDeleted(bDeleted);
         pField = getNextField(pField);
      }
   }
   
   /**
    * Set this translator's deleted state. All contained fields are set to the newe deleted state.
    *
    * @param[in] bDeleted TRUE or FALSE
    *
    * @return HRESULT
    */
   public void enableFields(boolean enableFields)
   {
      IEditControlField pField = getNextField(null);
      while (pField != null)
      {
         boolean enabled = pField.getEnabled();
         if (enabled)
         {
            // turn on the field's visibility AND set its text to its default if not empty
            pField.setVisible2(enableFields);
         }
         pField = getNextField(pField);
      }
   }
   
   private boolean handleLeadSeperator(IEditControlField pField,
   int nChar, int nCurrentPos)
   {
      boolean isHandled = false;
      int nStart=0, nEnd=0;
      if (pField != null)
      {
         nStart = pField.getFieldStartPos();
         nEnd = pField.getFieldEndPos();
         String leadSep = pField.getLeadSeparator();
         
         if (leadSep != null)
         {
            // if current pos is left of leading separator check it
            if ( nCurrentPos <= (nStart+leadSep.length()) )
            {
               isHandled = pField.isLeadSeparator( nChar);
            }
         }
      }
      return isHandled;
   }
   
   private ETPairT<Boolean, Boolean> handleSubFieldSeperator(IEditControlField pField,
   int nChar, int nCurrentPos)
   {
      boolean isHandled = false;
      boolean bSelectNextField = false;
      if (pField != null)
      {
         ITranslator pTrans = pField.getTranslator();
         if (pTrans != null)
         {
            ETPairT<Boolean, Boolean> retObj = pTrans.handleSeparator(nChar,nCurrentPos);
            isHandled = ((Boolean)retObj.getParamOne()).booleanValue();
            bSelectNextField = ((Boolean)retObj.getParamTwo()).booleanValue();
            if (isHandled)
            {
               if (bSelectNextField)
               {
                  boolean visible = pField.getVisible();
                  if (visible)
                  {
                     // found trailing separator, select following field
                     pField = getNextVisibleField(pField);
                     if (pField != null)
                     {
                        bSelectNextField = false;
                        selectField(pField);
                     }
                  }
               }
            }
            else
            {
               // look for and handle delimitor
               isHandled = pTrans.handleDelimitor( nChar, nCurrentPos);
            }
         }
      }
      return new ETPairT<Boolean, Boolean>(Boolean.valueOf(isHandled), Boolean.valueOf(bSelectNextField));
   }
   
   private boolean handleTrailSeperator(IEditControlField pField, int nChar)
   {
      boolean bHandled = false;
      
      bHandled = pField.isTrailSeparator( nChar );
      
      return bHandled;
   }
   
   private boolean isDuplicateSeperator(IEditControlField pField,
   int nChar, int nCurrentPos)
   {
      boolean found = false;
      if(pField != null)
      {
         int nStart=pField.getFieldStartPos();
         int nEnd=pField.getFieldEndPos();
         //nEnd = pField.getFieldPos( nStart);
         
         
         if(nStart == nCurrentPos) // only counts at the beginning of a field
         {
            if(m_ParentField != null)
            {
               found = m_ParentField.isDelimitor(nChar);
            }
         }
      }
      return found;
   }
   
   /**
    * Gets the delimitor that should go before the translator's fields.
    * returns "" if the first field is specified.
    */
   private ETPairT<String, Boolean> getDelimitor(boolean firstField)
   {
      String delimiter = "";
      if (!firstField)
      {
         IEditControlField pParentField = getParentField();
         if (pParentField != null)
         {
            delimiter = pParentField.getDelimitor();
            if (delimiter == null)
            {
               delimiter = "";
            }
         }
      }
      firstField = false;
      return new ETPairT<String, Boolean>(delimiter, Boolean.valueOf(firstField));
   }
   
   private int getCurrentPosition()
   {
      int currentPos=0;
      if(m_EditControl != null)
      {
         currentPos = m_EditControl.getCurrentPosition();
      }
      return currentPos;
   }
   
   private void createField(IEditControlField pLikeField, boolean insertBefore)
   {
      if (pLikeField != null)
      {
         IPropertyDefinition pDef = pLikeField.getPropertyDefinition();
         if (pDef != null)
         {
            IEditControlField pField = null;
            if (!insertBefore)
            {
               pLikeField = null;
            }
            
            pField = addFieldDefinition(pDef, pLikeField);
            
            // put_Visible was commented out, needed when inserting a multiplicity range
            pField.setVisible(true);
            
            // put enabled enables all eligible sub-fields
            pField.setEnabled(true);
            pField.setSelected(true);
            
            refreshEditControl();
            int nStart=pField.getTextStartPos();
            int nEnd=pField.getTextEndPos();
            //nEnd = pField.getTextPos(nStart);
            
            setSel(nStart, nStart);
            setCurrentPosition(nStart);
         }
      }
   }
   
   private boolean isOnDelimitor(int nChar)
   {
      boolean isDelimitor = false;
      if( m_ParentField != null )
      {
         isDelimitor = m_ParentField.isDelimitor( nChar);
      }
      return isDelimitor;
   }
   
   /**
    * Has this translator been modified?
    *
    * @param bModified[out]
    */
   public boolean getModified()
   {
      boolean retVal = false;
      
      // if modified return immediately, otherwise check for any nested translators
      if (m_Modified)
      {
         retVal = true;
      }
      else
      {
         IEditControlField pField = null;
         
         // get current field and offsets
         while ((pField = getNextField(pField)) != null)
         {
            // if field contains a translator, check it modified status
            retVal = pField.getModified();
            if (retVal)
            {
               break;
            }
         }
      }
      return retVal;
   }
   
   /**
    * Has this translator been modified?
    *
    * @param[in] bDeleted TRUE or FALSE
    *
    * @return HRESULT
    */
   public void setModified(boolean bModified)
   {
      if (m_ParentField != null)
      {
         m_ParentField.setModified(bModified);
      }
      if (m_EditControl != null)
      {
         m_EditControl.setModified(bModified);
      }
      m_Modified = bModified;
   }
   
   /**
    * Returns the text field that the caret is in.  If the caret is past the last field the return is NULL.
    * If multiple fields exist at the current location, the return will be (in order of priority)
    *   1. The first enabled field. An enabled field can be edited, but may not be "visible" b/c it may be empty.
    *   2. The default field, whether visible or not.
    *   3. The first field, whether visible or not.
    *
    * @param [out,retval] An ITextField object, or NULL if the current position is past all fields contained by this translator.
    */
   public IEditControlField getCurrentField()
   {
      IEditControlField pField = null;
      IEditControlField pFirstFoundField = null;
      IEditControlField pDefaultField = null;
      if (m_TextFields != null)
      {
         int count = m_TextFields.size();
         for (int i=0; i<count; i++)
         {
            pField = (IEditControlField)m_TextFields.elementAt(i);
            int startPos=pField.getFieldStartPos();
            int endPos=pField.getFieldEndPos();
            //endPos = pField.getFieldPos(startPos);
            
            // find a field that encompasses our current position
            if (getCurrentPosition() >= startPos && getCurrentPosition() <= endPos)
            {
               // store the first field found at this location
               if (pFirstFoundField == null)
               {
                  pFirstFoundField = pField;
               }
               
               // store the first default field found at this location
               boolean bVal = false;
               if (pDefaultField == null)
               {
                  bVal = pField.getDefault();
                  if (bVal)
                  {
                     pDefaultField = pField;
                  }
               }
               
               // return this field if enabled
               bVal = pField.getEnabled();
               if (bVal)
               {
                  break;
               }
            }
            pField = null;
         }
         
         if (pField == null)
         {
            if (pDefaultField != null)
            {
               pField = pDefaultField;
            }
            else
            {
               pField = pFirstFoundField;
            }
         }
      }
      return pField;
   }
   
   private IEditControlField jumpToNextField()
   {
      IEditControlField retField = null;
      
      retField = getCurrentField();
      if (retField == null)
      {
         retField = getNextField(null);
      }
      // now look for a succeeding field, if found we select it
      retField = getNextField( retField );
      
      if (retField != null)
      {
         selectField(retField);
      }
      
      return retField;
   }
   
   /**
    * Places the caret at the end of the text field preceeding this one.
    *
    * @param pNext[in]
    */
   private IEditControlField jumpToPreviousField(IEditControlField pNext)
   {
      IEditControlField retField = getPreviousField(pNext);
      
      if (retField != null)
      {
         int startPos=retField.getTextStartPos();
         int endPos=retField.getTextEndPos();
         //endPos = retField.getTextPos(startPos);
         setPosition(endPos);
      }
      
      return retField;
   }
   
   /**
    * Places the caret at the beginnning of this field.
    *
    * @param pField[in]
    */
   private void jumpToFieldBegin(IEditControlField pField)
   {
      if (pField != null)
      {
         int startPos=pField.getTextStartPos();
         int endPos=pField.getTextEndPos();
         //endPos = pField.getTextPos(startPos);
         setPosition(startPos);
      }
   }
   
   /**
    * Places the caret at the end of this field.
    *
    * @param pField[in]
    */
   private void jumpToFieldEnd( IEditControlField pField )
   {
      if (pField != null)
      {
         int startPos=pField.getTextStartPos();
         int endPos=pField.getTextEndPos();
         //endPos = pField.getTextPos(startPos);
         setPosition(endPos);
      }
   }
   
   /**
    * Registers acclerators for our delimitors
    *
    * @return HRESULT
    */
   public void registerAccelerators()
   {
      if (m_EditControl != null)
      {
         // let the current field have first crack at it, this may be a delimitor for it
         if (m_ParentField != null)
         {
            // we have a delimitor! Extract the character
            String sDelim = m_ParentField.getDelimitor();
            if (sDelim != null && sDelim.length() > 0)
            {
               int length = sDelim.length();
               for (int i=0; i<length; i++)
               {
                  char c = sDelim.charAt(i);
                  if (c != ' ')
                  {
                     // create an accelerator with Ctrl + character
                     m_EditControl.registerAccelerator(c, KeyEvent.VK_CONTROL);
                  }
               }
            }
         }
         
         // for each sub-field, ask it to handle delimitors
         IEditControlField pField = null;
         while ((pField = getNextField(pField)) != null)
         {
            ITranslator pTrans = pField.getTranslator();
            if (pTrans != null)
            {
               pTrans.registerAccelerators();
            }
         }
      }
   }
   
   /**
    * Handles an accelerator detected by the edit control.  Accelerators are registered by calling
    * RegisterAccelerators().
    *
    * @param[in] nID The application-defined identifier for this accelerator.
    *
    * @return HRESULT
    */
   public boolean handleAccelerator(int nChar)
   {
      boolean isHandled = false;
      isHandled = handleDelimitor(nChar, getCurrentPosition());
      
      if (!isHandled)
      {
         // for each sub-field, ask it to handle delimitors
         IEditControlField pField = null;
         while( (pField = getNextField( pField )) != null &&
         !isHandled )
         {
            ITranslator pTrans = pField.getTranslator();
            if (pTrans != null)
            {
               isHandled = pTrans.handleDelimitor(nChar, getCurrentPosition());
            }
         }
      }
      return isHandled;
   }
   
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.editcontrol.ITranslator#getTextStartPos()
         */
   public int getTextStartPos()
   {
      int retStart = 0;
      // read first field, obtain starting position
      IEditControlField pField = getNextField(null);
      if (pField != null)
      {
         retStart = pField.getTextStartPos();
      }
      
      return retStart;
   }
   
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.editcontrol.ITranslator#getTextEndPos()
         */
   public int getTextEndPos()
   {
      int retEnd = 0;
      // read first field, obtain starting position
      IEditControlField pField = getNextField(null);
      if (pField != null)
      {
         retEnd = pField.getTextEndPos();
      }
      
      // loop until no more visible fields, updating ending position
      while ((pField = getNextField(pField)) != null)
      {
         retEnd = pField.getTextEndPos();
      }
      return retEnd;
   }
   
   public int getLastTextStartPos()
   {
      int retStart = 0;
      // read first field, obtain starting position
      IEditControlField pField = getLastField();
      while (pField != null)
      {
         if (pField != null && pField.getVisible())
         {
            retStart = pField.getTextStartPos();
            break;
         }
         pField = getPreviousField(pField);
      }
      
      //if none of the fields are visible yet, we need to pass back the start position of current field
      if (retStart == 0)
      {
         pField = getFirstField();
         retStart = pField.getTextStartPos();
      }
      
      return retStart;
   }
   
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.editcontrol.ITranslator#getFieldStartPos()
         */
   public int getFieldStartPos()
   {
      int retPos = 0;
      IEditControlField pField = getNextField(null);
      if (pField != null)
      {
         retPos = pField.getFieldStartPos();
      }
      return retPos;
   }
   
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.editcontrol.ITranslator#getFieldEndPos()
         */
   public int getFieldEndPos()
   {
      int retPos = 0;
      IEditControlField pField = getNextField(null);
      if (pField != null)
      {
         retPos = pField.getFieldEndPos();
         // hidden fields will return nStartPos == nEndPos so account for no text width
         int dummy = 0;
         while ((pField=getNextField(pField)) != null )
         {
            retPos = pField.getFieldEndPos();
         }
      }
      return retPos;
   }
   
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.editcontrol.ITranslator#getTooltipLetText()
         */
   public String getTooltipLeftText()
   {
      if (!m_IsGetToolTipText)
      {
         getTooltipText();
         m_IsGetToolTipText = true;
      }
      return m_TooltipLeftText;
   }
   
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.editcontrol.ITranslator#getTooltipSubjectText()
         */
   public String getTooltipSubjectText()
   {
      if (!m_IsGetToolTipText)
      {
         getTooltipText();
         m_IsGetToolTipText = true;
      }
      return m_TooltipSubjectText;
   }
   
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.controls.editcontrol.ITranslator#getTooltipRightText()
         */
   public String getTooltipRightText()
   {
      if (!m_IsGetToolTipText)
      {
         getTooltipText();
         m_IsGetToolTipText = true;
      }
      return m_TooltipRightText;
   }
   
   public boolean handleDelete(boolean deleteRightwards)
   {
      boolean retVal = false;
      try
      {
         IEditControlField pField = null;
         int nStart = 0;
         int nEnd = 0;
         
         // get selected area
         int nStartPos = getSelStartPos();
         int nEndPos = getSelEndPos();
         
         // test if anything's currently selected
         if (nStartPos == nEndPos)
         {
            // nothing is selected, delete only 1 text character
            pField = getCurrentField();
            if (pField != null)
            {
               // sanity check, we should never enter this routine for a field that contains sub-fields
               ITranslator pTrans = pField.getTranslator();
               if (pTrans != null)
               {
                  // if we are on one of the separators, handle it as an arrow key
                  boolean handled = handleKeyDown(deleteRightwards ? KeyEvent.VK_RIGHT : KeyEvent.VK_LEFT);
                  return true;
               }
               
               // can only delete within text boundaries
               nStart = pField.getTextStartPos();
               nEnd = pField.getTextEndPos();
               if (nStartPos >= nStart && nStartPos <= nEnd)
               {
                  if (deleteRightwards)
                  {
                     // going right, delete up to end of field
                     nEndPos = (nEnd < nEndPos + 1) ? nEnd : nEndPos + 1;
                  }
                  else
                  {
                     // special case, field is empty and they're deleting the leading separator
                     if (nStart == nEnd)
                     {
                        pField.setVisible(false);
                        nStart = pField.getFieldStartPos();
                        nEnd = pField.getFieldEndPos();
                        updateVisibleFields(m_ParentField);
                        refreshEditControl();
                        setPosition(nStart);
                        retVal = true;
                     }
                     // going left, delete up to beginning of field
                     nStartPos = (nStart > nStartPos-1) ? nStart : nStartPos-1;
                  }
               }
               // do the delete
               if (!retVal)
               {
                  retVal = true;
                  if (nStartPos != nEndPos)
                  {
                     replaceText(pField, nStartPos, nEndPos, "", true);
                  }
               }
            }
         }
         else
         {
            // here we try to determine if the selected area crosses field boundaries
            // if crossing a boundary(s) all text within the visible fields must be selected
            
            // find our starting and ending fields
            IEditControlField pFirstField = getFieldAtPosition(nStartPos);
            IEditControlField pLastField = getFieldAtPosition(nEndPos);
            
            // if either field is null we can't handle here, in which case it goes back up to the parent
            // translator
            if (pFirstField != null && pLastField != null)
            {
               boolean bDelete = (pFirstField.equals(pLastField));
               if (bDelete)
               {
                  // selected area is within a single field, handle normally
                  replaceText(pFirstField, nStartPos, nEndPos, "", true);
                  setCurrentPosition(nStartPos);
                  retVal = true;
               }
               else
               {
                  // spanning multiple fields, make sure each is entirely selected
                  nStart = pFirstField.getTextStartPos();
                  nEnd = pFirstField.getTextEndPos();
                  
                  // test if any part of the field is selected (might not be)
                  if (nStartPos >= nEnd || nEndPos <= nStart)
                  {
                     pFirstField = getNextVisibleField(pFirstField);
                     
                  }
                  
                  // assume the last field can't have this happen
                  bDelete = true;
                  pField = pFirstField;
                  while (pField != null)
                  {
                     nStart = pField.getTextStartPos();
                     nEnd = pField.getTextEndPos();
                     
                     // test if all of the field is selected
                     if ( (nStartPos < nEnd && nStartPos > nStart) ||
                     (nEndPos > nStart && nEndPos < nEnd) )
                     {
                        // nope, don't allow delete
                        bDelete = false;
                        break;
                     }
                     if (pField.equals(pLastField))
                     {
                        break;
                     }
                     pField = getNextVisibleField(pField);
                  }
                  
                  // spanning multiple fields, make sure each is entirely selected
                  bDelete = true;
                  pField = pFirstField;
                  while (pField != null)
                  {
                     nStart = pField.getTextStartPos();
                     nEnd = pField.getTextEndPos();
                     
                     // test if any part of the field is selected (might not be)
                     if (nStartPos >= nEnd || nEndPos <= nStart)
                     {
                        // field is not selected, toss it from our survey
                     }
                     if ( (nStartPos < nEnd && nStartPos > nStart) ||
                     (nEndPos > nStart && nEndPos < nEnd) )
                     {
                        bDelete = false;
                        break;
                     }
                     if (pField.equals(pLastField))
                     {
                        break;
                     }
                     pField = getNextVisibleField(pField);
                  }
                  
                  // if all fields are selected delete them
                  if (bDelete)
                  {
                     pField = pFirstField;
                     while (pField != null)
                     {
                        pField.setDeleted(true);
                        if (pField.equals(pLastField))
                        {
                           break;
                        }
                        pField = getNextVisibleField(pField);
                     }
                     
                     setCurrentPosition(nStartPos);
                     refreshEditControl();
                     setModified(true);
                     retVal = true;
                  }
                  else
                  {
                     
                  }
               }
            }
         }
      }
      catch (Exception e)
      {
         Log.stackTrace(e);
      }
      return retVal;
   }
   
   /**
    * Moves caret and selects text from either the current position
    * or the currently selected text.  Text is selected if ShiftDown is TRUE.
    */
   private void selectToPosition( int nPos )
   {
      try
      {
         int nStartPos = getSelStartPos();
         int nEndPos = getSelEndPos();
         
         // use control key status to determine if selecting
         if (shiftDown())
         {
            if (getCurrentPosition() == nStartPos)
            {
               // moving left side
               nStartPos = nPos;
            }
            else
            {
               // moving right side
               nEndPos = nPos;
            }
         }
         else
         {
            // no shift, just move cursor
            nStartPos = nPos;
            nEndPos = nPos;
         }
         setCurrentPosition(nPos);
         setSel(nStartPos, nEndPos);
         if (m_EditControl != null)
         {
            updateHints();
         }
      }
      catch (Exception e)
      {
         Log.stackTrace(e);
      }
   }
   
   private boolean shiftDown()
   {
      return m_EditControl.isShiftDown();
   }
   
   private boolean controlDown()
   {
      return m_EditControl.isControlDown();
   }
}



