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


package org.netbeans.modules.uml.ui.products.ad.drawEngineManagers;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.coreapplication.IPreferenceManager2;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityEdge;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityNode;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.ISignalNode;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IValueSpecification;
import org.netbeans.modules.uml.core.metamodel.core.foundation.TypedFactoryRetriever;
import org.netbeans.modules.uml.core.preferenceframework.PreferenceAccessor;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETLabel;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ModelElementChangedKind;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TSLabelKind;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TSLabelPlacementKind;

public class ActivityEdgeLabelManager extends ADLabelManager implements IActivityEdgeLabelManager {

	public ActivityEdgeLabelManager() {
		super();
	}

	/**
	 * Creates the ActivityEdge guard label.
	 *
	 * @param bAssignDefaultName[in] Set this to true to set the label to the default name should
	 * the current text be "".
	 */
	public void createGuardLabel(boolean pAssignDefaultName) {

		// Get the text to display
		ETPairT <String, IExpression> result = getGuardText(pAssignDefaultName);
		
		String sName = (String)result.getParamOne();
		IExpression pExpression = (IExpression)result.getParamTwo();

		if (sName != null && sName.length() > 0 && pExpression != null) {
			createLabelIfNotEmpty(sName, TSLabelKind.TSLK_GUARD_CONDITION, TSLabelPlacementKind.TSLPK_CENTER_ABOVE, pExpression);
		}

	}

	/**
	 * Creates the ActivityEdge name label.
	 *
	 * @param bAssignDefaultName[in] Set this to true to set the label to the default name should
	 * the current text be "".
	 */
	public void createNameLabel(boolean pAssignDefaultName) {

		String sName = getNameText(pAssignDefaultName);

		if (sName != null && sName.length() > 0) {
			// We have an IActivityEdge!
			createLabelIfNotEmpty(sName, TSLabelKind.TSLK_NAME, TSLabelPlacementKind.TSLPK_CENTER_ABOVE, null);
		}
	}

	/**
	 * Creates the interrupting edge icon label (a bolt).
	 */
	public void createInterruptingEdgeLabel() {
		boolean bCurrentlyShown = isDisplayed(TSLabelKind.TSLK_ICON_LABEL);
		if (!bCurrentlyShown && shouldShowInterruptibleEdgeLabel()) {
			// We have an IActivityEdge!
			String unnamedStr = PreferenceAccessor.instance().getDefaultElementName();
			createLabelIfNotEmpty(unnamedStr, TSLabelKind.TSLK_ICON_LABEL, TSLabelPlacementKind.TSLPK_CENTER_ABOVE, null);
		}
	}

	/**
	 * Get the guard condition text of this ActivityEdge.
	 *
	 * @param sText [out] The text of the activity edge guard condition
	 * @param pFoundExpression [out] The current expression on the activity edge
	 * @param bAssignDefaultName [in] Set to true to create a guard condition and assign
	 * a default name.
	 */
	public ETPairT <String, IExpression> getGuardText(boolean pAssignDefaultName) {

		String sText = null;
		IExpression pFoundExpression = null;

		IElement pElement = this.getModelElement();

		String sGuardWithBrackets = "";
		
		IActivityEdge pActivityEdge = pElement instanceof IActivityEdge ? (IActivityEdge) pElement : null;

		if (pActivityEdge != null) {
			IValueSpecification pGuard = pActivityEdge.getGuard();

			IExpression pExpression = (pGuard instanceof IExpression)?(IExpression) pGuard : null;

			if (pExpression != null) {
				sGuardWithBrackets = pExpression.getBody();

				pFoundExpression = pExpression;
			}
		}

		// If the user has told us then assign a default name
		if (pAssignDefaultName) {
			String xName = this.retrieveDefaultName();

			if (xName != null && xName.length() > 0) {
				IValueSpecification pGuard = pActivityEdge.getGuard();

				IExpression pExpression = (IExpression) pGuard;

				if (pExpression != null) {
					pExpression.setBody(xName);
				} else if (pGuard == null) {

					IExpression pNewExpression = new TypedFactoryRetriever <IExpression> ().createType("Expression");

					if (pNewExpression != null) {
						pActivityEdge.setGuard(pNewExpression);
						pNewExpression.setBody(xName);
						sGuardWithBrackets = pNewExpression.getBody();

						pFoundExpression = pNewExpression;
					}
				}
			}
		}

		if (sGuardWithBrackets.length() > 0) {
			String newGuard = "[";
			newGuard += sGuardWithBrackets;
			newGuard += "]";

			sText = newGuard;
		}

		return new ETPairT<String, IExpression>(sText,pFoundExpression);

	}

	public void createInitialLabels() {		
		boolean bIsDisplayed = false;


		ETPairT <String, IExpression> result = getGuardText(false);
		
		String sName = (String)result.getParamOne();
		IExpression pExpression = (IExpression)result.getParamTwo();

		if (sName != null && sName.length() > 0) {
			bIsDisplayed = this.isDisplayed(TSLabelKind.TSLK_GUARD_CONDITION);

			if (!bIsDisplayed) {
				createLabelIfNotEmpty(sName, TSLabelKind.TSLK_GUARD_CONDITION, TSLabelPlacementKind.TSLPK_CENTER_BELOW, pExpression);
			}
			sName = "";
		}

		// Get the name text to be displayed
		sName = getNameText(false);
		if (sName != null && sName.length() > 0) {
			bIsDisplayed = isDisplayed(TSLabelKind.TSLK_NAME);
			if (!bIsDisplayed) {
				createLabelIfNotEmpty(sName, TSLabelKind.TSLK_NAME, TSLabelPlacementKind.TSLPK_CENTER_BELOW, null);
			}
			sName = "";
		}

		// Show the interruptible edge lightning bolt
		IElement pModelElement = getModelElement();

		if (pModelElement != null) {
			IActivityEdge pActivity = pModelElement instanceof IActivityEdge ? (IActivityEdge) pModelElement : null;

			if (pActivity != null) {
				IActivityNode pSource = pActivity.getSource();

				ISignalNode pSourceSignalNode = pSource instanceof ISignalNode ? (ISignalNode) pSource : null;

				if (pSourceSignalNode != null && this.shouldShowInterruptibleEdgeLabel()) {
					bIsDisplayed = isDisplayed(TSLabelKind.TSLK_ICON_LABEL);
					if (!bIsDisplayed) {
						String unnamedStr = PreferenceAccessor.instance().getDefaultElementName();
						createLabelIfNotEmpty(unnamedStr, TSLabelKind.TSLK_ICON_LABEL, TSLabelPlacementKind.TSLPK_CENTER_BELOW, null);
					}
				}
			}
		}

		// Show the stereotype
		bIsDisplayed = isDisplayed(TSLabelKind.TSLK_STEREOTYPE);

		if (!bIsDisplayed) {
			sName = getStereotypeText();

			if (sName != null && sName.length() > 0) {
				createLabelIfNotEmpty(sName, TSLabelKind.TSLK_STEREOTYPE, TSLabelPlacementKind.TSLPK_CENTER_BELOW, null);
			}
		}

		// Make sure the text is ok
		resetLabelsText();
	}

	public boolean isValidLabelKind(int nLabelKind) {
		return nLabelKind == TSLabelKind.TSLK_GUARD_CONDITION || nLabelKind == TSLabelKind.TSLK_NAME || nLabelKind == TSLabelKind.TSLK_ICON_LABEL || nLabelKind == TSLabelKind.TSLK_STEREOTYPE;	
	}

   public void modelElementHasChanged(INotificationTargets pTargets)
   {
      // Get the changed model element and kind
      IElement pChangedElement = pTargets != null ? pTargets.getChangedModelElement() : null;
      
      if (pChangedElement != null)
      {
			int nKind = pTargets.getKind();
         if (nKind == ModelElementChangedKind.MECK_NAMEMODIFIED && pChangedElement instanceof IActivityEdge)
         {
            // If the ActivityEdge name is not showing then show it
            IActivityEdge pActEdge = (IActivityEdge)pChangedElement;
            if (pActEdge == null)
            {
               // This label manager is not managing an activity edge, just return
               ///				 return S_OK;
            }
         }
         // Special handling for Guard labels which are broken in C++ but need fixing here
         else if (nKind == ModelElementChangedKind.MECK_ACTIVITYEDGE_GUARDMODIFIED)
         {
            resetLabelText(TSLabelKind.TSLK_GUARD_CONDITION);
         }

         // Handle the stereotype change through the base class
         super.modelElementHasChanged(pTargets);
      }
   }

   public void resetLabelText(int labelKind)
   {
      boolean bDoLayout = false;

      IElement pElement = getModelElement();

      IActivityEdge pActivityEdge = pElement instanceof IActivityEdge ? (IActivityEdge)pElement : null;
      if (pActivityEdge != null)
      {
         IETLabel pETLabel = this.getETLabelbyKind(labelKind);

         if (pETLabel != null)
         {
            String sText;

            int nLabelKind = pETLabel.getLabelKind();

            if (nLabelKind == TSLabelKind.TSLK_GUARD_CONDITION)
            {
               // Get the ActivityEdge guard
               ETPairT < String, IExpression > result = getGuardText(false);
               sText = (String)result.getParamOne();
               IExpression pExpression = (IExpression)result.getParamTwo();
            }
            else if (nLabelKind == TSLabelKind.TSLK_NAME)
            {
               // Get the ActivityEdge name
               sText = this.getNameText(true);
            }
            else if (nLabelKind == TSLabelKind.TSLK_STEREOTYPE)
            {
               sText = getStereotypeText();
            }
            else if (nLabelKind == TSLabelKind.TSLK_ICON_LABEL)
            {
               sText = PreferenceAccessor.instance().getDefaultElementName();
            }
            else
               sText = "";

            // Here's where we set the text of the label
            String sOldText = pETLabel.getText();

            if (sText != null && sText.length() > 0)
            {
               if (!(sText == sOldText))
               {
                  pETLabel.setText(sText);
                  pETLabel.reposition();
                  bDoLayout = true;
               }
               pETLabel.sizeToContents();
            }
         }
      }

      if (bDoLayout)
      {
         // Relayout the labels
         relayoutLabels();
      }

      invalidate();

   }

	public void resetLabelsText() {
		boolean done = false;
		int index = 0;
		boolean bDoLayout = false;

		IElement pElement = getModelElement();

		IActivityEdge pActivityEdge = pElement instanceof IActivityEdge ? (IActivityEdge) pElement : null;
		if (pActivityEdge != null) {
			// Go through all the product labels and re-get their text.
			while (!done) {
				IETLabel pETLabel = this.getETLabelbyIndex(index);

				if (pETLabel != null) {
					String sText;

					int nLabelKind  = pETLabel.getLabelKind();

					if (nLabelKind == TSLabelKind.TSLK_GUARD_CONDITION) {
						// Get the ActivityEdge guard
						ETPairT <String, IExpression> result = getGuardText(false);
						sText = (String)result.getParamOne();
						IExpression pExpression = (IExpression)result.getParamTwo();
					} else if (nLabelKind == TSLabelKind.TSLK_NAME) {
						// Get the ActivityEdge name
						sText = this.getNameText(true);
					} else if (nLabelKind == TSLabelKind.TSLK_STEREOTYPE) {
						sText = getStereotypeText();
					} else if (nLabelKind == TSLabelKind.TSLK_ICON_LABEL) {
						sText = PreferenceAccessor.instance().getDefaultElementName();
					}
					else
						sText = "";

					// Here's where we set the text of the label
					String sOldText = pETLabel.getText();

					if (sText != null && sText.length() > 0) {
						if (!(sText == sOldText)) {
							pETLabel.setText(sText);
							pETLabel.reposition();
							bDoLayout = true;
						}
						pETLabel.sizeToContents();
					} else {
						// If there is no text then remove the label
						removeETLabel(index);
					}
				} else {
					done = true;
				}
				index++;
			}
		}

		if (bDoLayout) {
			// Relayout the labels
			relayoutLabels();
		}

		invalidate();

	}

	public void showLabel(int nLabelKind, boolean bShow) {

		// See if it's already shown
		boolean bCurrentlyShown = isDisplayed(nLabelKind);

		if ((bCurrentlyShown && bShow) || (!bCurrentlyShown && !bShow)) {
			// We have nothing to do!
		} else {
			IElement pElement = getModelElement();

			IActivityEdge pActivityEdge = pElement instanceof IActivityEdge ? (IActivityEdge) pElement : null;

			if (pActivityEdge != null) {
				if (nLabelKind == TSLabelKind.TSLK_GUARD_CONDITION) {
					if (bShow) {
						createGuardLabel(true);
					} else {
						discardLabel(nLabelKind);
						invalidate();
					}
				} else if (nLabelKind == TSLabelKind.TSLK_NAME) {
					if (bShow) {
						createNameLabel(true);
					} else {
						discardLabel(nLabelKind);
						invalidate();
					}
				} else if (nLabelKind == TSLabelKind.TSLK_STEREOTYPE) {
					if (bShow) {
						showStereotypeLabel();
					} else {
						discardLabel(nLabelKind);
						invalidate();
					}
				} else if (nLabelKind == TSLabelKind.TSLK_ICON_LABEL) {
					if (bShow && shouldShowInterruptibleEdgeLabel()) {
						createInterruptingEdgeLabel();
					} else {
						discardLabel(nLabelKind);
						invalidate();
					}
				}
			}
		}
	}

	/**
	 * Looks in the preference to see if we should show the lightning bolt icon
	 *
	 * @return true if the preference indicates that we should show the lightning bolt icon
	 * from a signal to an invocation.
	 */
	protected boolean shouldShowInterruptibleEdgeLabel() {
		boolean bShowLightning = false;

		ICoreProduct prod = ProductRetriever.retrieveProduct();
		if (prod != null) {
			IPreferenceManager2 pPrefMgr = prod.getPreferenceManager();
			if (pPrefMgr != null) {
				String sPrefValue = pPrefMgr.getPreferenceValue("Diagrams|ActivityDiagram", "IndicateInterruptibleEdges");
				if (sPrefValue.equals("PSK_YES")) {
					bShowLightning = true;
				}
			}
		}

		return bShowLightning;
	}
}
