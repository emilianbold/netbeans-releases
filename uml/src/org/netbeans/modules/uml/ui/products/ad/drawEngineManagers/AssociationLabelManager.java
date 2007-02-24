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

import java.util.List;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.common.generics.ETTripleT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.NodeEndKindEnum;
import org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETLabel;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ModelElementChangedKind;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TSLabelKind;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TSLabelPlacementKind;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;

public class AssociationLabelManager extends ADLabelManager {

	// Get the end name of this end
	public String getAssociationEndNameLabel(IAssociationEnd pEnd, boolean bAssignDefaultName) {

		String retValue = null;

		if (pEnd != null) {
			retValue = pEnd.getNameWithAlias();

			// If the user has told us then assign a default name
			if ((retValue == null || retValue.length() == 0) && bAssignDefaultName) {
				String xName = retrieveDefaultName();
				pEnd.setNameWithAlias(xName);
				retValue = pEnd.getNameWithAlias();
			}
		}

		return retValue;
	}

	// Get the association of this end
	public String getAssociationEndMultiplicityLabel(IAssociationEnd pEnd, boolean bAssignDefaultValue) {

		String retValue = null;

		IMultiplicity pMult = pEnd.getMultiplicity();

		if (pMult != null) {
			retValue = pMult.getRangeAsString(false);
		}

		if ((retValue == null || retValue.length() == 0) && bAssignDefaultValue) {
			{
				pMult.setRange2("1", "1");

				retValue = "";
				retValue = pMult.getRangeAsString(false);
			}
		}
		return retValue;
	}

	public void createAssociationEndNameLabel(IAssociationEnd pEnd, boolean bAssignDefaultName) {

		// Get the IElement from the parent IETGraphObject (the node or edge view)
		IEdgePresentation pThisEdgePresentation = TypeConversions.getEdgePresentation(m_rawParentETGraphObject);

		if (pThisEdgePresentation != null) {
			int nLabelToShow = TSLabelKind.TSLK_UNKNOWN;
			int nLocation = TSLabelPlacementKind.TSLPK_FROM_NODE_BELOW;

			ETPairT <Integer, Integer> result = this.getEndAndLocation(pEnd, false);
			nLabelToShow = ((Integer)result.getParamOne()).intValue();
			nLocation = ((Integer)result.getParamTwo()).intValue();

			// Get the text to show
			String sName = getAssociationEndNameLabel(pEnd, bAssignDefaultName);

			// Now create the label if we've found it.
			if (nLabelToShow != TSLabelKind.TSLK_UNKNOWN) {
				// See if it's already shown
				boolean bCurrentlyShown = false;

				bCurrentlyShown = isDisplayed(nLabelToShow);

				if (!bCurrentlyShown) {
					createLabelIfNotEmpty(sName, nLabelToShow, nLocation, pEnd);
				} else if (sName == null || sName.length() == 0) {
					showLabel(nLabelToShow, false);
				} else {
					IETLabel pETLabel = this.getETLabelbyKind(nLabelToShow);
					if (pETLabel != null) {
						// Reset the label text to account for any changes
						this.setLabelString(pETLabel, sName);
					}
				}
			}
		}
	}

	public void modelElementHasChanged(INotificationTargets pTargets) {

		int nKind = ModelElementChangedKind.MECK_UNKNOWN;

		IElement pChangedElement = pTargets.getChangedModelElement();
		IElement pSecondaryChangedME = pTargets.getSecondaryChangedModelElement();
		
		IFeature pChangedFeature = null;
		
		if (pSecondaryChangedME != null && pSecondaryChangedME instanceof IFeature){
			pChangedFeature = (IFeature) pSecondaryChangedME;
		}

		nKind = pTargets.getKind();

		if (pChangedFeature != null || pChangedElement != null) {
			if (nKind == ModelElementChangedKind.MECK_NAMEMODIFIED) {
				
				IAssociation pAss = (pChangedElement instanceof IAssociation) ? (IAssociation)pChangedElement : null;
				
				IAssociationEnd pEnd = (pChangedFeature instanceof IAssociationEnd) ? (IAssociationEnd)pChangedFeature : null;

				// If the association name is not showing then show it
				if (pAss != null) {
					boolean bCurrentlyShown = false;

					bCurrentlyShown = isDisplayed(TSLabelKind.TSLK_ASSOCIATION_NAME);
					if (!bCurrentlyShown) {
						// Show the name label
						showLabel(TSLabelKind.TSLK_ASSOCIATION_NAME, true);
					} else {					
						// Reset the name label text
						resetLabelText(TSLabelKind.TSLK_ASSOCIATION_NAME);
					}
				} else if (pEnd != null) {
					// Display the end name label or reset it's text if something
					// has changed.
					showNameLabel(pEnd);
				}
			} else if (
				nKind == ModelElementChangedKind.MECK_MULTIPLICITYMODIFIED
					|| nKind == ModelElementChangedKind.MECK_LOWERMODIFIED
					|| nKind == ModelElementChangedKind.MECK_UPPERMODIFIED
					|| nKind == ModelElementChangedKind.MECK_RANGEADDED
					|| nKind == ModelElementChangedKind.MECK_RANGEREMOVED
					|| nKind == ModelElementChangedKind.MECK_ORDERMODIFIED) {
				IAssociationEnd pEnd = (IAssociationEnd) pChangedElement;

				if (pEnd != null) {
					// Display the end multiplicity label or reset it's text if something
					// has changed.
					showMultiplicityLabel(pEnd);
				}
			}
		}

		// Handle the stereotype change through the base class
		super.modelElementHasChanged(pTargets);
	}

	public void showNameLabel(IAssociationEnd pEnd) {
		createAssociationEndNameLabel(pEnd, true);
	}

	public void showMultiplicityLabel(IAssociationEnd pEnd) {
		createAssociationEndMultiplicityLabel(pEnd, true);
	}

	// Create the various labels
	public void createAssociationNameLabel(boolean bAssignDefaultName) {

		String sName;
		boolean bCurrentlyShown = false;

		bCurrentlyShown = isDisplayed(TSLabelKind.TSLK_ASSOCIATION_NAME);

		if (!bCurrentlyShown) {

			// Get the text to display
			sName = getNameText(bAssignDefaultName);

			if (sName != null && sName.length() > 0) {

				// We have an IAssociation!
				createLabelIfNotEmpty(sName, TSLabelKind.TSLK_ASSOCIATION_NAME, TSLabelPlacementKind.TSLPK_CENTER_ABOVE, null);
			}
		}
	}

	public void createAssociationEndMultiplicityLabel(IAssociationEnd pEnd, boolean bAssignDefaultValue) {

		IEdgePresentation pThisEdgePresentation = TypeConversions.getEdgePresentation(m_rawParentETGraphObject);

		if (pThisEdgePresentation != null) {
			int nLabelToShow = TSLabelKind.TSLK_UNKNOWN;
			int nLocation = TSLabelPlacementKind.TSLPK_FROM_NODE_BELOW;

			ETPairT <Integer, Integer> result = this.getEndAndLocation(pEnd, true);
			nLabelToShow = ((Integer)result.getParamOne()).intValue();
			nLocation = ((Integer)result.getParamTwo()).intValue();

			// Get the text to show
			String sName = getAssociationEndMultiplicityLabel(pEnd, bAssignDefaultValue);

			// Now create the label if we've found it.
			if (nLabelToShow != TSLabelKind.TSLK_UNKNOWN) {
				// See if it's already shown
				boolean bCurrentlyShown = false;

				bCurrentlyShown = isDisplayed(nLabelToShow);

				// Get the multiplicity
				IMultiplicity pMult = pEnd.getMultiplicity();

				if (!bCurrentlyShown) {
					if (pMult != null) {
						createLabelIfNotEmpty(sName, nLabelToShow, nLocation, pMult);
					}
				} else if (pMult == null || sName.length() == 0) {
					showLabel(nLabelToShow, false);
				} else {
					IETLabel pETLabel = this.getETLabelbyKind(nLabelToShow);
					if (pETLabel != null) {
						// Reset the label text to account for any changes
						setLabelString(pETLabel, sName);
					}
				}
			}
		}
	}

	// ILabelManager Overrides
	// Creates the initial labels on a newly created node or edge
	public void createInitialLabels() {

		// Create the labels
		createAssociationNameLabel(false);

		// Create the end labels
		IElement pElement = getModelElement();

		//IAssociation pAssociation = (IAssociation) pElement;

		//if (pAssociation != null) {
      if(pElement instanceof IAssociation)
      {
         IAssociation pAssociation = (IAssociation) pElement;
			ETList < IAssociationEnd > pEnds = null;
			int numEnds = 0;

			pEnds = pAssociation.getEnds();

			numEnds = pAssociation.getNumEnds();

			if (pEnds != null && numEnds == 2) {
				IAssociationEnd pEnd0 = pEnds.get(0);
				IAssociationEnd pEnd1 = pEnds.get(1);

				if (pEnd0 != null) {
					createAssociationEndNameLabel(pEnd0, false);
					createAssociationEndMultiplicityLabel(pEnd0, false);
				}
				if (pEnd1 != null) {
					createAssociationEndNameLabel(pEnd1, false);
					createAssociationEndMultiplicityLabel(pEnd1, false);
				}
			}
		}

		// Make sure the text is ok
		resetLabelsText();
	}

	public void resetLabelsText() {

		boolean done = false;
		int index = 0;
		boolean bDoLayout = false;

		IElement pElement = getModelElement();

		

      if(pElement instanceof IAssociation) {
         IAssociation pAssociation = (IAssociation) pElement;
			ETList < IAssociationEnd > pEnds = null;

			int numEnds = 0;

			pEnds = pAssociation.getEnds();
			numEnds = pAssociation.getNumEnds();

			// Go through all the product labels and re-get their text.
			while (!done) {
				IETLabel pETLabel = this.getETLabelbyIndex(index);

				if (pETLabel != null) {
					String sText = "";
					int nLabelKind = TSLabelKind.TSLK_UNKNOWN;

					nLabelKind = pETLabel.getLabelKind();

					if (nLabelKind == TSLabelKind.TSLK_ASSOCIATION_NAME) {
						// Get the association name, make sure there is a default name so the label is not deleted
						sText = getNameText(true);
					} else if (nLabelKind == TSLabelKind.TSLK_ASSOCIATION_END0_ROLE_NAME) {
						IAssociationEnd pEnd0 = null;
						if (pEnds != null && numEnds > 0) {
							pEnd0 = pEnds.get(0);

							if (pEnd0 != null) {
								// Get the end name, make sure there is a default name so the label is not deleted
								sText = getAssociationEndNameLabel(pEnd0, true);
							}
						}
					} else if (nLabelKind == TSLabelKind.TSLK_ASSOCIATION_END0_MULTIPLICITY) {
						IAssociationEnd pEnd0 = null;

						if (pEnds != null && numEnds > 0) {
							pEnd0 = pEnds.get(0);

							if (pEnd0 != null) {
								// Since the value is the same with or without alias,
								// don't force a default value, see Fix W3311.
								sText = getAssociationEndMultiplicityLabel(pEnd0, false);
							}
						}
					} else if (nLabelKind == TSLabelKind.TSLK_ASSOCIATION_END1_ROLE_NAME) {
						IAssociationEnd pEnd1 = null;
						if (pEnds != null && numEnds > 1) {
							pEnd1 = pEnds.get(1);
							if (pEnd1 != null) {
								// Get the end name, make sure there is a default name so the label is not deleted
								sText = getAssociationEndNameLabel(pEnd1, true);
							}
						}
					} else if (nLabelKind == TSLabelKind.TSLK_ASSOCIATION_END1_MULTIPLICITY) {
						IAssociationEnd pEnd1 = null;
						if (numEnds > 1) {
							pEnd1 = pEnds.get(1);
							if (pEnd1 != null) {
								// Since the value is the same with or without alias,
								// don't force a default value, see Fix W3311.
								sText = getAssociationEndMultiplicityLabel(pEnd1, false);
							}
						}
					} else if (nLabelKind == TSLabelKind.TSLK_STEREOTYPE) {
						sText = getStereotypeText();
					}

					// Here's where we set the text of the label
					String sOldText = pETLabel.getText();

					if (sText != null && sText.length() > 0) {
						if (!(sText.equals(sOldText))) {
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
		boolean bCurrentlyShown = false;

		bCurrentlyShown = isDisplayed(nLabelKind);

		if ((bCurrentlyShown && bShow) || (!bCurrentlyShown && !bShow)) {
			// We have nothing to do!
		} else {

			ETTripleT < IAssociationEnd, IAssociationEnd, Integer > result = this.getAssociationEnds();

			IAssociationEnd pEnd0 = result.getParamOne();
			IAssociationEnd pEnd1 = result.getParamTwo();
			Integer pNumEnds = result.getParamThree();

			int numEnds = pNumEnds.intValue();

			if (numEnds == 2) {
				if (nLabelKind == TSLabelKind.TSLK_ASSOCIATION_NAME) {
					if (bShow) {
						createAssociationNameLabel(true);
					} else {
						discardLabel(nLabelKind);
						invalidate();
					}
				} else if (nLabelKind == TSLabelKind.TSLK_ASSOCIATION_END0_ROLE_NAME) {
					if (bShow) {
						createAssociationEndNameLabel(pEnd0, true);
					} else {
						discardLabel(nLabelKind);
						invalidate();
					}
				} else if (nLabelKind == TSLabelKind.TSLK_ASSOCIATION_END1_ROLE_NAME) {
					if (bShow) {
						createAssociationEndNameLabel(pEnd1, true);
					} else {
						discardLabel(nLabelKind);
						invalidate();
					}
				} else if (nLabelKind == TSLabelKind.TSLK_ASSOCIATION_END0_MULTIPLICITY) {
					if (bShow) {
						createAssociationEndMultiplicityLabel(pEnd0, true);
					} else {
						discardLabel(nLabelKind);
						invalidate();
					}
				} else if (nLabelKind == TSLabelKind.TSLK_ASSOCIATION_END1_MULTIPLICITY) {
					if (bShow) {
						createAssociationEndMultiplicityLabel(pEnd1, true);
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
				}
			}
		}
	}

	// Does this label manager know how to display this label?
	public boolean isValidLabelKind(int nLabelKind) {

		boolean bIsValid = false;
		if (nLabelKind == TSLabelKind.TSLK_ASSOCIATION_NAME
			|| nLabelKind == TSLabelKind.TSLK_ASSOCIATION_END0_ROLE_NAME
			|| nLabelKind == TSLabelKind.TSLK_ASSOCIATION_END1_ROLE_NAME
			|| nLabelKind == TSLabelKind.TSLK_ASSOCIATION_END0_MULTIPLICITY
			|| nLabelKind == TSLabelKind.TSLK_ASSOCIATION_END1_MULTIPLICITY
			|| nLabelKind == TSLabelKind.TSLK_STEREOTYPE) {
			bIsValid = true;
		}
		return bIsValid;
	}

	// Used to restore the edit text for multiplicities which my look like 1..1 back to just '1'
	public void handleEditNoChange(IETLabel pLabel, String sNewString) {

		int nLabelKind = TSLabelKind.TSLK_UNKNOWN;

		nLabelKind = pLabel.getLabelKind();

		if (nLabelKind == TSLabelKind.TSLK_ASSOCIATION_END0_MULTIPLICITY || nLabelKind == TSLabelKind.TSLK_ASSOCIATION_END1_MULTIPLICITY) {
			resetLabelsText();
		}

	}

	public void handleEditChange(IETLabel pLabel, String sNewString) {

		int nLabelKind = TSLabelKind.TSLK_UNKNOWN;

		nLabelKind = pLabel.getLabelKind();

		if (nLabelKind == TSLabelKind.TSLK_ASSOCIATION_END0_MULTIPLICITY || nLabelKind == TSLabelKind.TSLK_ASSOCIATION_END1_MULTIPLICITY) {
			resetLabelsText();
		}
	}

	// Resets the text for an individual label type
	public void resetLabelText(int nLabelKind) {

		if (nLabelKind == TSLabelKind.TSLK_ASSOCIATION_NAME) {
			// Reset the name text

			String sText = getNameText(false);
			IETLabel pETLabel = this.getETLabelbyKind(nLabelKind);

			if (pETLabel != null) {
				if (sText != null && sText.length() > 0) {
					setLabelString(pETLabel, sText);
				} else {
					showLabel(nLabelKind, false);
				}
			}
		} else {
			super.resetLabelText(nLabelKind);
		}
	}

	//	// Returns the location and end for a specific IAssociationEnd
	//	STDMETHOD(GetEndAndLocation)(IAssociationEnd* pEnd,
	//								 VARIANT_BOOL bMultiplicity,
	//								 TSLabelKind& nLabelToShow,
	//								 TSLabelPlacementKind& nLocation);

	//TODO NL The above method was split into the following two. Still need to refactor to remove redundancies

	// Returns the location and end for a specific IAssociationEnd
	protected ETPairT<Integer, Integer> getEndAndLocation(IAssociationEnd pEnd, boolean bMultiplicity) {

		int nLabelToShow = TSLabelKind.TSLK_UNKNOWN;
		int nLocation = TSLabelPlacementKind.TSLPK_FROM_NODE_BELOW;

		IEdgePresentation pThisEdgePresentation = TypeConversions.getEdgePresentation(m_rawParentETGraphObject);

		if (pThisEdgePresentation != null) {
			int nEndKind = NodeEndKindEnum.NEK_UNKNOWN;

			nEndKind = pThisEdgePresentation.getNodeEnd(pEnd);

			if (nEndKind != NodeEndKindEnum.NEK_UNKNOWN) {
				if (pEnd != null) {
					if (nEndKind == NodeEndKindEnum.NEK_FROM || nEndKind == NodeEndKindEnum.NEK_TO) {
						if (nEndKind == NodeEndKindEnum.NEK_FROM) {
							nLocation = (bMultiplicity) ? TSLabelPlacementKind.TSLPK_FROM_NODE_BELOW : TSLabelPlacementKind.TSLPK_FROM_NODE_ABOVE;
						} else if (nEndKind == NodeEndKindEnum.NEK_TO) {
							nLocation = (bMultiplicity) ? TSLabelPlacementKind.TSLPK_TO_NODE_BELOW : TSLabelPlacementKind.TSLPK_TO_NODE_ABOVE;
						}

						// Now get the end index the argument end is associated with
						IElement pElement = getModelElement();

						IAssociation pAssociation = (IAssociation) pElement;

						if (pAssociation != null) {
							int index = 0;

							index = pAssociation.getEndIndex(pEnd);

							if (index == 0) {
								nLabelToShow = (bMultiplicity) ? TSLabelKind.TSLK_ASSOCIATION_END0_MULTIPLICITY : TSLabelKind.TSLK_ASSOCIATION_END0_ROLE_NAME;

							} else {
								nLabelToShow = (bMultiplicity) ? TSLabelKind.TSLK_ASSOCIATION_END1_MULTIPLICITY : TSLabelKind.TSLK_ASSOCIATION_END1_ROLE_NAME;
							}
						}
					} else if (nEndKind == NodeEndKindEnum.NEK_BOTH) {
						IElement pElement = getModelElement();

						IAssociation pAssociation = (IAssociation) pElement;

						if (pAssociation != null) {
							int index = 0;
							index = pAssociation.getEndIndex(pEnd);
							if (index == 0) {
								// If it's ambiguous the association draw engine uses end #0 as
								// the from(source) node.

								nLabelToShow = (bMultiplicity) ? TSLabelKind.TSLK_ASSOCIATION_END0_MULTIPLICITY : TSLabelKind.TSLK_ASSOCIATION_END0_ROLE_NAME;
								nLocation = (bMultiplicity) ? TSLabelPlacementKind.TSLPK_FROM_NODE_BELOW : TSLabelPlacementKind.TSLPK_FROM_NODE_ABOVE;

							} else if (index == 1) {
								nLabelToShow = (bMultiplicity) ? TSLabelKind.TSLK_ASSOCIATION_END1_MULTIPLICITY : TSLabelKind.TSLK_ASSOCIATION_END1_ROLE_NAME;
								nLocation = (bMultiplicity) ? TSLabelPlacementKind.TSLPK_TO_NODE_BELOW : TSLabelPlacementKind.TSLPK_FROM_NODE_ABOVE;
							}
						}
					}
				}
			}
		}
		return new ETPairT<Integer, Integer>(new Integer(nLabelToShow), new Integer(nLocation));
	}


	// Returns the assocation ends 0 and 1.
	protected ETTripleT < IAssociationEnd, IAssociationEnd, Integer > getAssociationEnds() {

		Integer numEnds = null;
		IAssociationEnd pEnd0 = null;
		IAssociationEnd pEnd1 = null;

		IElement pElement = getModelElement();
		IAssociation pAssociation = (IAssociation) pElement;

		if (pAssociation != null) {

			ETList < IAssociationEnd > pEnds = pAssociation.getEnds();

			if (pEnds != null) {
				numEnds = new Integer(pEnds.size());

				if (numEnds.intValue() > 0) {
					pEnd0 = pEnds.get(0);
				}
				if (numEnds.intValue() > 1) {
					pEnd1 = pEnds.get(1);
				}
			}
		}

		return new ETTripleT < IAssociationEnd, IAssociationEnd, Integer > (pEnd0, pEnd1, numEnds);
	}

	// Returns a list of labels whose layout kind doesn't agree with the end index.  This happens
	// when a label is created with index 1 (this it's TSLK_ASSOCIATION_END1_ROLE_NAME), then end
	// #0 gets transformed, thus making a new one #1 and the old #1 #0!
	protected ETList < IPresentationElement > getInvalidLabels() {

		ETList < IPresentationElement > pFoundBadOnes = new ETArrayList();

		int index = 0;
		boolean done = false;

		ETTripleT < IAssociationEnd, IAssociationEnd, Integer > pEnds = this.getAssociationEnds();

		IAssociationEnd pEnd0 = pEnds.getParamOne();
		IAssociationEnd pEnd1 = pEnds.getParamTwo();
		Integer pNumEnds = pEnds.getParamThree();
						
		int numEnds = pNumEnds.intValue();

		if (numEnds == 2) {

			while (!done) {

				IETLabel pETLabel = this.getETLabelbyIndex(index);

				if (pETLabel != null) {

					int nThisLabelKind = TSLabelKind.TSLK_UNKNOWN;

					nThisLabelKind = pETLabel.getLabelKind();

					if (nThisLabelKind == TSLabelKind.TSLK_ASSOCIATION_END0_MULTIPLICITY
						|| nThisLabelKind == TSLabelKind.TSLK_ASSOCIATION_END1_MULTIPLICITY
						|| nThisLabelKind == TSLabelKind.TSLK_ASSOCIATION_END0_ROLE_NAME
						|| nThisLabelKind == TSLabelKind.TSLK_ASSOCIATION_END1_ROLE_NAME) {

						// Verify that this label is correct.  Do it by the placement.
						int nCurrentPlacement = TSLabelPlacementKind.TSLPK_UNKNOWN;

						nCurrentPlacement = pETLabel.getLabelPlacement();

						int nLabelToShow = TSLabelKind.TSLK_UNKNOWN;
						int nLocation = TSLabelPlacementKind.TSLPK_FROM_NODE_BELOW;

						ETPairT <Integer, Integer> result = null;
						
						if (nThisLabelKind == TSLabelKind.TSLK_ASSOCIATION_END0_MULTIPLICITY) {

							result = this.getEndAndLocation(pEnd0, true);
							if (result != null) {
								nLabelToShow = ((Integer) result.getParamOne()).intValue();
								nLocation = ((Integer) result.getParamTwo()).intValue();
							}

						} else if (nThisLabelKind == TSLabelKind.TSLK_ASSOCIATION_END0_ROLE_NAME) {

							result = this.getEndAndLocation(pEnd0, false);
							if (result != null) {
								nLabelToShow = ((Integer) result.getParamOne()).intValue();
								nLocation = ((Integer) result.getParamTwo()).intValue();
							}

						} else if (nThisLabelKind == TSLabelKind.TSLK_ASSOCIATION_END1_MULTIPLICITY) {

							result = this.getEndAndLocation(pEnd1, true);
							if (result != null) {
								nLabelToShow = ((Integer) result.getParamOne()).intValue();
								nLocation = ((Integer) result.getParamTwo()).intValue();
							}

						} else if (nThisLabelKind == TSLabelKind.TSLK_ASSOCIATION_END1_ROLE_NAME) {

							result = this.getEndAndLocation(pEnd1, false);
							if (result != null) {
								nLabelToShow = ((Integer) result.getParamOne()).intValue();
								nLocation = ((Integer) result.getParamTwo()).intValue();
							}
						}

						if (nCurrentPlacement != nLocation || nLabelToShow != nThisLabelKind) {

							IPresentationElement pPE = TypeConversions.getPresentationElement(pETLabel);

							if (pPE != null) {
								pFoundBadOnes.add(pPE);
							}
						}
					}
				}
			}
		}
		return pFoundBadOnes;
	}
}
