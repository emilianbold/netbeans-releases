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


/*
 *
 * Created on Jun 19, 2003
 * @author Trey Spiva
 */
package org.netbeans.modules.uml.ui.support.archivesupport;

/**
 *
 * @author Trey Spiva
 */
public interface IProductArchiveDefinitions
{
   // These are the XML names in the prs file for saving diagram information



   // Various Tables
   public final static String RESOURCEFONTTABLE_STRING         = "FontsTable";
   public final static String RESOURCECOLORTABLE_STRING        = "ColorsTable";
   public final static String COMPARTMENTNAMETABLE_STRING      = "CompartmentNameTable";
   public final static String COMPARTMENTFONTTABLE_STRING      = "CompartmentFontTable";
   public final static String COMPARTMENTFONTCOLORTABLE_STRING = "CompartmentForeColorTable";
   public final static String TABLE_ENTRY_DELETED              = "EntryDeleted";
   
   // Used by the resource manager to store information
   public final static String STRINGS_TABLE = "StringsTable";
   public final static String COLORIDS_TABLE = "ColorIDs";
   public final static String COLOR_TABLE = "ColorTable";
   public final static String FONTIDS_TABLE = "FontIDs";
   public final static String FONTHOLDERIDS_TABLE = "FontHolderIDs";
   public final static String FONTHOLDER_TABLE = "FontHolderTable";
   public final static String TABLE_ENTRY = "entry";
   public final static String STRING_ATTR = "string";
   public final static String STRINGID_ENTRY = "stringid";
   public final static String COMBINEDSTRINGID_ATTR = "combinedstringid";
   public final static String FONTID_ENTRY = "fontid";
   public final static String COLORID_ENTRY = "colorid";
   public final static String COLORREF_ENTRY = "colorref";
   public final static String FONTHOLDERID_ENTRY = "fontholderid";

   // Used by the diagram
   public final static String DIAGRAMINFO_STRING         = "diagramInfo";
   public final static String DIAGRAMVERSION_STRING      = "diagramVersion";
   public final static String DIAGRAMNAME_STRING         = "name";
   public final static String DIAGRAMALIAS_STRING        = "alias";
   public final static String DRAWINGKIND_STRING         = "kind";
   public final static String DRAWINGKIND2_STRING        = "diagramKind";
   public final static String DIAGRAMNAME_DOCS           = "docs";
   public final static String DIAGRAM_ZOOM               = "zoom";
   public final static String DIAGRAM_XPOS               = "xPos";
   public final static String DIAGRAM_YPOS               = "yPos";
   public final static String DIAGRAM_XMIID              = "diagramXMIID";
   public final static String NAMESPACE_MEID             = "namespaceMEID";
   public final static String NAMESPACE_TOPLEVELID       = "namespaceToplevelID";
   public final static String DLLINFO_STRING             = "DLLInfo";
   public final static String DLLNAME_STRING             = "DLLName";
   public final static String DLLVERSION_STRING          = "DLLVersion";
   public final static String DLLDATE_STRING             = "DLLDate";
   public final static String DLLSIZE_STRING             = "DLLSize";
   public final static String ASSOCIATED_DIAGRAMS_STRING = "AssociatedDiagrams";
   public final static String ASSOCIATED_ELEMENTS_STRING = "AssociatedElements";
   public final static String LAST_SHOWALIAS_STATE       = "lastShowAliasState";
   public final static String DIAGRAM_ISSTUB_STRING 	 = "isStub";
   public final static String DIAGRAM_CDFS_STRING 		 = "elementsToCDFS";
   public final static String DIAGRAM_IGNOREFORCDFS_STRING = "ignoreForCDFS";
   
   // Used by the label views
   public final static String LABELVIEW_TSLABELKIND = "TSLabelKind";
   public final static String LABELVIEW_TSLABELPLACEMENTKIND = "TSLabelPlacementKind";
   
   // This attribute marks a presentation element as deleted
   public final static String PE_DELETED = "PE_Deleted";
   
   // Used by the node/edge views
   public final static String MEID_STRING                  = "MEID";
   public final static String TOPLEVELID_STRING            = "toplevelMEID";
   public final static String PRESENTATIONELEMENTID_STRING = "PEID";
   public final static String OWNER_PRESENTATIONELEMENT    = "ownerPEID";
   public final static String INITIALIZATIONSTRING_STRING  = "initString";
   public final static String BRIDGEKIND                   = "bridgeKind";
   public final static String REFERREDELEMENTS_STRING      = "referredElements";
   
   // Used by the engine
   public final static String ENGINENAMEELEMENT_STRING   = "engine";
   public final static String ENGINENAMEATTRIBUTE_STRING = "name";
   public final static String EDITLOCKED_STRING = "editLocked";
   
   // Used by the IGraphicDrawEngine
   public final static String GRAPHIC_SHAPE_STRING = "graphicShape";
   public final static String GRAPHIC_CONTAINMENTENABLED_STRING = "containmentEnabled";
   
   // Used by the IMessageEdgeDrawEngine
   public final static String LIFELINEENGINE_CREATEMESSAGE_STRING = "createMessageID";
   
   // Used by the ILifelineDrawEngine
   public final static String MESSAGEEDGEENGINE_ISSELFTOMESSAGE_BOOL = "isMessageToSelf";
   public final static String MESSAGEEDGEENGINE_ISDESTROYED_BOOL     = "isDestroyed";
   
   // Used by the IStateDrawEngine
   public final static String SHOWEVENTSANDTRANSITIONS_BOOL = "showEventsAndTransitions";
   
   // Used by the ILifelineDrawEngine
   public final static String INTERACTIONFRAGMENTENGINE_LABEL_STRING        = "label";
   public final static String INTERACTIONFRAGMENTENGINE_NAMELOCATION_STRING = "nameLocation";
   public final static String INTERACTIONFRAGMENTENGINE_ISHOLLOW_STRING     = "isHollow";
   
   // Used by the compartment
   public final static String COMPARTMENTNAMEELEMENT_STRING             = "compartment";
   public final static String COMPARTMENTNAMETABLEINDEXATTRIBUTE_STRING = "name";
   public final static String COMPARTMENTFIELD_STRING                   = "field";
   public final static String COMPARTMENTXMIIDATTRIBUTE_STRING          = "MEID";
   public final static String COMPARTMENTTEXTATTRIBUTE_STRING           = "value";
   public final static String COMPARTMENTALIASATTRIBUTE_STRING          = "alias";
   public final static String COMPARTMENTFONTATTRIBUTE_STRING           = "font";
   public final static String COMPARTMENTFOREATTRIBUTE_STRING           = "foreColor";
   public final static String COMPARTMENTBACKATTRIBUTE_STRING           = "backColor";
   public final static String COMPARTMENTATTRIBUTEINTERNALID_STRING     = "internalID";
   public final static String COMPARTMENTCOLLAPSED_STRING               = "collapsed";
   public final static String COMPARTMENTSHOWNAME_STRING                = "showName";
   public final static String COMPARTMENTTEXTSTYLE_STRING               = "textStyles";

   public final static String TABLEINDEXATTRIBUTE_STRING = "TableIndex";
   
   // Used by IADNameCompartment
   public final static String ADNAMECOMPARTMENTADDITIONALDRAWINGKIND_STRING     = "additionalDrawingKind";
   public final static String ADNAMECOMPARTMENTNAMECOMPARTMENTBORDERKIND_STRING = "nameCompartmentBorderKind";
   public final static String ADNAMECOMPARTMENTISSTATIC_STRING                  = "isStatic";
   public final static String ADNAMECOMPARTMENTISABSTRACT_STRING                = "isAbstract";
   
   // Used by CPart
   public final static String PART_LIFEINE = "lifeline";
   // Used by ILifelineCompartmentPiece
   public final static String ADLIFELINECOMPARTMENTPIECE_Y_STRING      = "y";
   public final static String ADLIFELINECOMPARTMENTPIECE_DY_STRING     = "dy";
   public final static String ADLIFELINECOMPARTMENTPIECE_LEFT_STRING   = "left";
   public final static String ADLIFELINECOMPARTMENTPIECE_HEIGHT_STRING = "height";
   public final static String ADLIFELINECONNECTORLIST_STRING           = "ConnectorList";
   public final static String ADLIFELINECONNECTOR_STRING               = "Connector";
   public final static String ADLIFELINECONNECTOR_LOCATION_STRING      = "location";
   public final static String ADLIFELINECONNECTOR_SOURCE_STRING        = "source";
   public final static String ADLIFELINECONNECTOR_TARGET_STRING        = "target";
   
   // Used by IADInteractionOperand
   public final static String INTERACTIONOPERAND_HEIGHT_STRING = "height";
   
   // Used by resource user
   public final static String RESOURCEFONTNAME_STRING               = "name";
   public final static String RESOURCECOLORVALUE_STRING             = "value";
   public final static String RESOURCEELEMENT_STRING                = "resourceID";
   public final static String RESOURCEKIND_STRING                   = "kind";
   public final static String RESOURCENAME_STRING                   = "internalName";
   public final static String RESOURCEFONT_STRING                   = "CustomFont";
   public final static String RESOURCECOLOR_STRING                  = "CustomColor";
   public final static String RESOURCEKIND_INHERITED_STRING         = "inherited";
   public final static String RESOURCES_STRING                     = "resources";
   public final static String RESOURCE_STRING                      = "resource";
   public final static String FONT_STRING                          = "font";
   public final static String COLOR_STRING                         = "color";
   public final static String TYPE_STRING                          = "type";
   public final static String RESOURCESTRINGID_STRING              = "resourceStringID";
   public final static String FONTID_STRING                        = "fontID";
   public final static String COLORID_STRING                       = "colorID";
   
   public final static String RESOURCEFONTCHARSET_STRING            = "CharSet";
   public final static String RESOURCEFONTHEIGHT_STRING             = "Height";
   public final static String RESOURCEFONTITALIC_STRING             = "Italic";
   public final static String RESOURCEFONTSTRIKEOUT_STRING          = "Strikeout";
   public final static String RESOURCEFONTUNDERLINE_STRING          = "Underline";
   public final static String RESOURCEFONTWEIGHT_STRING             = "Weight";
   public final static String RESOURCEFONTCOLOR_STRING              = "Color";

   //used for getting the header to be used in etlp files.   
   public static final String IDR_ARCHIVE_HEADER = "ArchiveSupport.xml";
   
   
   // Used by the trackbar
   public final static String ELEMENT_TRACKBAR = "Embarcadero.AxTrackBar";
   
   //Used for the new .diagram file
   public static final String UML_DIAGRAM_STRING = "UML:Diagram";
   
}
