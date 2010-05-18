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
package com.sun.rave.web.ui.theme;

/**
 * <p> This class contains constants for style class names.</p>
 * TODO: Eventually these need to move to a theme-based
 * resource file.
 */

public class ThemeStyles {

    /**
     * A properties file key whose value is a space separated list
     * of style sheet keys to include on every page.
     */
    public static final String GLOBAL		    = "global";

    /**
     * A master stylesheet to be included on every page.
     */
    public static final String MASTER		    = "master";

    /**
     * Body Styles
     */
    public static final String DEFAULT_BODY		    = "DefBdy";

    /**
     * Hidden Style
     */
    public static final String HIDDEN                       = "hidden";

    /**
     * Link Styles
     */
    public static final String LINK_DISABLED                = "disabled";

    /**
     *	Block Type Styles
     */
    public static final String FLOAT			    = "float";
  
    /** 
     * Table Styles 
     */
    public static final String TABLE                           = "Tbl";
    public static final String TABLE_LITE                      = "TblLt";
    public static final String TABLE_ACTION_LINK               = "TblActLnk";
    public static final String TABLE_ACTION_TD                 = "TblActTd";
    public static final String TABLE_ACTION_TD_LASTROW         = "TblActTdLst";
    public static final String TABLE_HEADER                    = "TblColHdr";
    public static final String TABLE_HEADER_LINK               = "TblHdrLnk";
    public static final String TABLE_HEADER_LINK_IMG           = "TblHdrImgLnk";
    public static final String TABLE_HEADER_SORTNUM            = "TblHdrSrtNum";
    public static final String TABLE_HEADER_SORT               = "TblColHdrSrt";
    public static final String TABLE_HEADER_SORT_DISABLED      = "TblColHdrSrtDis";
    public static final String TABLE_HEADER_SELECTCOL          = "TblColHdrSel";
    public static final String TABLE_HEADER_SELECTCOL_DISABLED = "TblColHdrSelDis";
    public static final String TABLE_HEADER_SELECTCOL_SORT     = "TblColHdrSrtSel";
    public static final String TABLE_HEADER_TABLE              = "TblHdrTbl";
    public static final String TABLE_HEADER_TEXT               = "TblHdrTxt";
    public static final String TABLE_MARGIN                    = "TblMgn";
    public static final String TABLE_MESSAGE_TEXT              = "TblMsgTxt";
    public static final String TABLE_MULTIPLE_HEADER_ROOT      = "TblMultHdr";
    public static final String TABLE_MULTIPLE_HEADER           = "TblMultColHdr";
    public static final String TABLE_MULTIPLE_HEADER_SORT      = "TblMultColHdrSrt";
    public static final String TABLE_MULTIPLE_HEADER_TEXT      = "TblMultHdrTxt";
    public static final String TABLE_NAVIGATION_LINK           = "TblNavLnk";
    public static final String TABLE_PAGINATION_TEXT           = "TblPgnTxt";
    public static final String TABLE_PAGINATION_TEXT_BOLD      = "TblPgnTxtBld";
    public static final String TABLE_PAGINATION_LEFT_BUTTON    = "TblPgnLftBtn";
    public static final String TABLE_PAGINATION_RIGHT_BUTTON   = "TblPgnRtBtn";
    public static final String TABLE_PAGINATION_SUBMIT_BUTTON  = "TblPgnGoBtn";
    public static final String TABLE_TD_ALARM                  = "TblTdAlm";
    public static final String TABLE_TD_LAYOUT                 = "TblTdLyt";
    public static final String TABLE_TD_SELECTCOL              = "TblTdSel";
    public static final String TABLE_TD_SELECTCOL_SORT         = "TblTdSrtSel";
    public static final String TABLE_TD_SORT                   = "TblTdSrt";
    public static final String TABLE_TD_SPACER                 = "TblTdSpc";
    public static final String TABLE_TITLE_TEXT                = "TblTtlTxt";
    public static final String TABLE_TITLE_TEXT_SPAN           = "TblTtlTxtSpn";
    public static final String TABLE_TITLE_MESSAGE_SPAN        = "TblTtlMsgSpn";
    public static final String TABLE_SELECT_ROW                = "TblSelRow";
    public static final String TABLE_HOVER_ROW                 = "TblHovRow";
    public static final String TABLE_CUSTOM_FILTER_MENU        = "TblCstFltMnu";

    /** Table footers */
    public static final String TABLE_FOOTER              = "TblFtrRow";
    public static final String TABLE_FOOTER_TEXT         = "TblFtrRowTxt";
    public static final String TABLE_FOOTER_LEFT         = "TblFtrLft";
    public static final String TABLE_FOOTER_MESSAGE_SPAN = "TblFtrMsgSpn";
 
    /** Overall row group headers/footers */
    public static final String TABLE_GROUP_HEADER       = "TblGrpRow";
    public static final String TABLE_GROUP_HEADER_IMAGE = "TblGrpCbImg";
    public static final String TABLE_GROUP_HEADER_LEFT  = "TblGrpLft";
    public static final String TABLE_GROUP_HEADER_RIGHT = "TblGrpRt";
    public static final String TABLE_GROUP_HEADER_TEXT  = "TblGrpTxt";
    public static final String TABLE_GROUP_FOOTER       = "TblGrpFtrRow";
    public static final String TABLE_GROUP_FOOTER_TEXT  = "TblGrpFtrRowTxt";
    
    /** Calendar styles */
    public static final String CALENDAR_DIV = "CalPopDiv";
    public static final String CALENDAR_DIV_SHOW = "CalPopShdDiv";
    public static final String CALENDAR_DIV_SHOW2 = "CalPopShd2Div";
    public static final String CALENDAR_FIELD = "CalPopFld";
    public static final String CALENDAR_FIELD_LABEL = "CalPopFldLbl";
    public static final String CALENDAR_FIELD_IMAGE = "CalPopFldImg";
    public static final String CALENDAR_FOOTER = "CalPopFtr";
    public static final String CALENDAR_FOOTER_DIV = "CalPopFtrDiv";
    public static final String CALENDAR_DAY_TEXT = "CurDayTxt";
    public static final String CALENDAR_CLOSE_LINK = "CalPopClsLnk";
    // <RAVE>
    public static final String CALENDAR_ROOT_TABLE = "CalRootTbl";
    // </RAVE>

    /** Column footers */
    public static final String TABLE_GROUP_COL_FOOTER      = "TblGrpColFtr";
    public static final String TABLE_GROUP_COL_FOOTER_TEXT = "TblGrpColFtrTxt";
    public static final String TABLE_GROUP_COL_FOOTER_SORT = "TblGrpColFtrSrt";

    /** Table column footers */
    public static final String TABLE_COL_FOOTER        = "TblColFtr";
    public static final String TABLE_COL_FOOTER_TEXT   = "TblColFtrTxt";
    public static final String TABLE_COL_FOOTER_SORT   = "TblColFtrSrt";
    public static final String TABLE_COL_FOOTER_SPACER = "TblColFtrSpc";

    /** Embedded panels */
    public static final String TABLE_PANEL_TD          = "TblPnlTd";
    public static final String TABLE_PANEL_LAYOUT_DIV  = "TblPnlLytDiv";
    public static final String TABLE_PANEL_SHADOW3_DIV = "TblPnlShd3Div";
    public static final String TABLE_PANEL_SHADOW2_DIV = "TblPnlShd2Div";
    public static final String TABLE_PANEL_SHADOW1_DIV = "TblPnlShd1Div";
    public static final String TABLE_PANEL_DIV         = "TblPnlDiv";
    public static final String TABLE_PANEL_TITLE       = "TblPnlTtl";
    public static final String TABLE_PANEL_CONTENT     = "TblPnlCnt";
    public static final String TABLE_PANEL_BUTTON_DIV  = "TblPnlBtnDiv";
    public static final String TABLE_PANEL_HELP_TEXT   = "TblPnlHlpTxt";
    public static final String TABLE_PANEL_TABLE       = "TblPnlSrtTbl";

    /** 
     * Drop Down Menu Styles.
     */
    public static final String MENU_JUMP                      = "MnuJmp";
    public static final String MENU_JUMP_OPTION               = "MnuJmpOpt";
    public static final String MENU_JUMP_OPTION_DISABLED      = "MnuJmpOptDis";
    public static final String MENU_JUMP_OPTION_GROUP         = "MnuJmpOptGrp";
    public static final String MENU_JUMP_OPTION_SELECTED      = "MnuJmpOptSel";
    public static final String MENU_JUMP_OPTION_SEPARATOR     = "MnuJmpOptSep";
    public static final String MENU_STANDARD                  = "MnuStd";
    public static final String MENU_STANDARD_DISABLED         = "MnuStdDis";
    public static final String MENU_STANDARD_OPTION           = "MnuStdOpt";
    public static final String MENU_STANDARD_OPTION_DISABLED  = "MnuStdOptDis";
    public static final String MENU_STANDARD_OPTION_GROUP     = "MnuStdOptGrp";
    public static final String MENU_STANDARD_OPTION_SELECTED  = "MnuStdOptSel";
    public static final String MENU_STANDARD_OPTION_SEPARATOR = "MnuStdOptSep";

    /** 
     * Selectable List Styles.
     */
    public static final String LIST                    = "Lst";
    public static final String LIST_DISABLED           = "LstDis";
    public static final String LIST_MONOSPACE          = "LstMno";
    public static final String LIST_MONOSPACE_DISABLED = "LstMnoDis";
    public static final String LIST_OPTION             = "LstOpt";
    public static final String LIST_OPTION_DISABLED    = "LstOptDis";
    public static final String LIST_OPTION_GROUP       = "LstOptGrp";
    public static final String LIST_OPTION_SELECTED    = "LstOptSel";
    public static final String LIST_OPTION_SEPARATOR   = "LstOptSep";
    public static final String LIST_ALIGN              = "LstAln";

    /** 
     * Primary Button Styles.
     */
    public static final String BUTTON1               = "Btn1";
    public static final String BUTTON1_HOVER         = "Btn1Hov";
    public static final String BUTTON1_DISABLED      = "Btn1Dis";
    public static final String BUTTON1_DEFAULT       = "Btn1Def";
    public static final String BUTTON1_DEFAULT_HOVER = "Btn1DefHov";
    public static final String BUTTON1_MINI          = "Btn1Mni";
    public static final String BUTTON1_MINI_HOVER    = "Btn1MniHov";
    public static final String BUTTON1_MINI_DISABLED = "Btn1MniDis";

    /** 
     * Secondary Button Styles.
     */
    public static final String BUTTON2               = "Btn2";
    public static final String BUTTON2_HOVER         = "Btn2Hov";
    public static final String BUTTON2_DISABLED      = "Btn2Dis";
    public static final String BUTTON2_MINI          = "Btn2Mni";
    public static final String BUTTON2_MINI_HOVER    = "Btn2MniHov";
    public static final String BUTTON2_MINI_DISABLED = "Btn2MniDis";

    /** 
     * Icon Button Styles.
     */
    public static final String BUTTON3               = "Btn3";
    public static final String BUTTON3_HOVER         = "Btn3Hov";
    public static final String BUTTON3_DISABLED      = "Btn3Dis";
    
    /** 
     * Breadcrumb Styles.
     */
    public static final String BREADCRUMB_WHITE_DIV  = "BcmWhtDiv";
    public static final String BREADCRUMB_GRAY_DIV   = "BcmGryDiv";
    public static final String BREADCRUMB_LINK       = "BcmLnk";
    public static final String BREADCRUMB_TEXT       = "BcmTxt";
    public static final String BREADCRUMB_SEPARATOR  = "BcmSep";

    /** 
     * Page Title Styles. 
     */
    public static final String TITLE_TEXT_DIV   = "TtlTxtDiv";
    public static final String TITLE_LINE       = "TtlLin";
    public static final String TITLE_TEXT       = "TtlTxt";
    public static final String TITLE_HELP_DIV   = "TtlHlpDiv";
    public static final String TITLE_ACTION_DIV = "TtlActDiv";
    public static final String TITLE_VIEW_DIV   = "TtlVewDiv";
    public static final String TITLE_BUTTON_DIV = "TtlBtnDiv";
    public static final String TITLE_BUTTON_BOTTOM_DIV = "TtlBtnBtmDiv";

    /** 
     * Add and Remove Styles.
     */
    public static final String ADDREMOVE_LABEL              = "AddRmvLbl";
    public static final String ADDREMOVE_LABEL2             = "AddRmvLbl2";
    public static final String ADDREMOVE_BUTTON_TABLE       = "AddRmvBtnTbl";
    public static final String ADDREMOVE_VERTICAL_FIRST     = "AddRmvVrtFst";
    public static final String ADDREMOVE_VERTICAL_WITHIN    = "AddRmvVrtWin";
    public static final String ADDREMOVE_VERTICAL_BETWEEN   = "AddRmvVrtBwn";
    public static final String ADDREMOVE_VERTICAL_BUTTON    = "AddRmvVrtBtn";
    public static final String ADDREMOVE_HORIZONTAL_WITHIN  = "AddRmvHrzWin";
    public static final String ADDREMOVE_HORIZONTAL_BETWEEN = "AddRmvHrzBwn";
    public static final String ADDREMOVE_HORIZONTAL_ALIGN   = "AddRmvHrzDiv";
    public static final String ADDREMOVE_HORIZONTAL_LAST    = "AddRmvHrzLst";

    /** 
     * Checkbox Styles. 
     */
    public static final String CHECKBOX 	 	     = "Cb";
    public static final String CHECKBOX_DISABLED 	     = "CbDis";
    public static final String CHECKBOX_LABEL 		     = "CbLbl";
    public static final String CHECKBOX_LABEL_DISABLED 	     = "CbLblDis";
    public static final String CHECKBOX_IMAGE 		     = "CbImg";
    public static final String CHECKBOX_IMAGE_DISABLED 	     = "CbImgDis";
    public static final String CHECKBOX_SPAN     	     = "CbSpn";
    public static final String CHECKBOX_SPAN_DISABLED        = "CbSpnDis";

    /**
     * Checkbox Group Styles.
     */
    public static final String CHECKBOX_GROUP 		     = "CbGrp";
    public static final String CHECKBOX_GROUP_CAPTION 	     = "CbGrpCpt";
    public static final String CHECKBOX_GROUP_LABEL   	     = "CbGrpLbl";
    public static final String CHECKBOX_GROUP_LABEL_DISABLED = "CbGrpLblDis";
    public static final String CHECKBOX_GROUP_ROW_EVEN       = "CbGrpRwEv";
    public static final String CHECKBOX_GROUP_ROW_ODD 	     = "CbGrpRwOd";
    public static final String CHECKBOX_GROUP_CELL_EVEN      = "CbGrpClEv";
    public static final String CHECKBOX_GROUP_CELL_ODD 	     = "CbGrpClOd";
	
    /** 
     * Radio Button Styles. 
     */	
    public static final String RADIOBUTTON 	              = "Rb";
    public static final String RADIOBUTTON_DISABLED           = "RbDis";
    public static final String RADIOBUTTON_LABEL 	      = "RbLbl";
    public static final String RADIOBUTTON_LABEL_DISABLED     = "RbLblDis";
    public static final String RADIOBUTTON_IMAGE 	      = "RbImg";
    public static final String RADIOBUTTON_IMAGE_DISABLED     = "RbImgDis";
    public static final String RADIOBUTTON_SPAN     	      = "RbSpn";
    public static final String RADIOBUTTON_SPAN_DISABLED      = "RbSpnDis";

    /**
     * Property sheet styles
     */
    public static final String PROPERTY_SHEET = "PROPERTY_SHEET";

    /**
     * Radio Button Group Styles
     */
    public static final String RADIOBUTTON_GROUP 		= "RbGrp";
    public static final String RADIOBUTTON_GROUP_CAPTION 	= "RbGrpCpt";
    public static final String RADIOBUTTON_GROUP_LABEL 		= "RbGrpLbl";
    public static final String RADIOBUTTON_GROUP_LABEL_DISABLED = "RbGrpLblDis";
    public static final String RADIOBUTTON_GROUP_ROW_EVEN 	= "RbGrpRwEv";
    public static final String RADIOBUTTON_GROUP_ROW_ODD 	= "RbGrpRwOd";
    public static final String RADIOBUTTON_GROUP_CELL_EVEN 	= "RbGrpClEv";
    public static final String RADIOBUTTON_GROUP_CELL_ODD 	= "RbGrpClOd";

    /** 
     * Inline Alert Styles. 
     */
    public static final String ALERT_ERROR_TEXT       = "AlrtErrTxt";
    public static final String ALERT_WARNING_TEXT     = "AlrtWrnTxt";
    public static final String ALERT_INFORMATION_TEXT = "AlrtInfTxt";
    public static final String ALERT_LINK_DIV         = "AlrtLnkDiv";
    public static final String ALERT_LINK             = "AlrtLnk";
    public static final String ALERT_TABLE            = "AlrtTbl";
    public static final String ALERT_MESSAGE_TEXT     = "AlrtMsgTxt";

    /** 
     * Full page Alert Styles.
     */
    public static final String ALERT_MESSAGE_DIV      = "FulAlrtMsgDiv";
    public static final String ALERT_HEADER_DIV       = "FulAlrtHdrDiv";
    public static final String ALERT_HEADER_TXT       = "FulAlrtHdrTxt";
    public static final String ALERT_FORM_DIV         = "FulAlrtFrmDiv";

    /** 
     * Label Styles.
     */
    public static final String LABEL_LEVEL_ONE_TEXT   = "LblLev1Txt";
    public static final String LABEL_LEVEL_TWO_TEXT   = "LblLev2Txt";
    public static final String LABEL_LEVEL_TWO_SMALL_TEXT   = "LblLev2smTxt";
    public static final String LABEL_LEVEL_THREE_TEXT = "LblLev3Txt";
    public static final String LABEL_REQUIRED_DIV     = "LblRqdDiv";

    /** 
     * Text Field Styles.
     */
    public static final String TEXT_FIELD 	    = "TxtFld";
    public static final String TEXT_FIELD_DISABLED  = "TxtFldDis";

    /** 
     * Text Area Styles.
     */
    public static final String TEXT_AREA 	    = "TxtAra";
    public static final String TEXT_AREA_DISABLED   = "TxtAraDis";

    /** 
     * Help Styles.
     */
    public static final String HELP_FIELD_LINK = "HlpFldLnk";
    public static final String HELP_FIELD_TEXT = "HlpFldTxt";
    public static final String HELP_PAGE_LINK  = "HlpPgeLnk";
    public static final String HELP_PAGE_TEXT  = "HlpPgeTxt";
    public static final String HELP_RESULT_DIV = "HlpRltDiv";

    /** 
     * Masthead Styles. 
     */
    public static final String MASTHEAD_BDY                  = "MstBdy";
    public static final String MASTHEAD_DIV                  = "MstDiv";
    public static final String MASTHEAD_LABEL                = "MstLbl";
    public static final String MASTHEAD_TEXT                 = "MstTxt";
    public static final String MASTHEAD_ALARM_DOWN_TEXT      = "MstAlmDwnTxt";
    public static final String MASTHEAD_ALARM_CRITICAL_TEXT  = "MstAlmCrtTxt";
    public static final String MASTHEAD_ALARM_MAJOR_TEXT     = "MstAlmMajTxt";
    public static final String MASTHEAD_ALARM_MINOR_TEXT     = "MstAlmMinTxt";
    public static final String MASTHEAD_TABLE_TOP            = "MstTblTop";
    public static final String MASTHEAD_TABLE_BOTTOM         = "MstTblBot";
    public static final String MASTHEAD_TABLE_END            = "MstTblEnd";
    public static final String MASTHEAD_SECONDARY_TABLE      = "MstSecTbl";
    public static final String MASTHEAD_TD_TITLE             = "MstTdTtl";
    public static final String MASTHEAD_TD_ALARM             = "MstTdAlm";
    public static final String MASTHEAD_TD_LOGO              = "MstTdLogo";
    public static final String MASTHEAD_DIV_TITLE            = "MstDivTtl";
    public static final String MASTHEAD_DIV_SECONDARY_TITLE  = "MstDivSecTtl";
    public static final String MASTHEAD_DIV_USER             = "MstDivUsr";
    public static final String MASTHEAD_LINK                 = "MstLnk";
    public static final String MASTHEAD_LINK_LEFT            = "MstLnkLft";
    public static final String MASTHEAD_LINK_RIGHT           = "MstLnkRt";
    public static final String MASTHEAD_LINK_CENTER          = "MstLnkCen";
    public static final String MASTHEAD_USER_LINK            = "MstUsrLnk";
    public static final String MASTHEAD_ALARM_LINK	     = "MstAlmLnk";
    public static final String MASTHEAD_PROGRESS_LINK        = "MstPrgLnk";
    public static final String MASTHEAD_STATUS_DIV           = "MstStatDiv";
    public static final String MASTHEAD_TIME_DIV             = "MstTmeDiv";
    public static final String MASTHEAD_ALARM_DIV            = "MstAlmDiv";
    public static final String MASTHEAD_SPACER_IMAGE         = "MstSpcImg";

    /** 
     * Tab Navigation Styles.
     */
    public static final String TAB1_DIV               = "Tab1Div";
    public static final String TAB1_TABLE_NEW         = "Tab1TblNew";
    public static final String TAB1_TABLE2_NEW        = "Tab1Tbl2New";
    public static final String TAB1_TABLE3_NEW        = "Tab1Tbl3New";
    public static final String TAB1_LINK              = "Tab1Lnk";
    public static final String TAB1_TABLE_SPACER_TD   = "Tab1TblSpcTd";
    public static final String TAB1_TABLE_SELECTED_TD = "Tab1TblSelTd";
    public static final String TAB1_SELECTED_TEXT_NEW = "Tab1SelTxtNew";
    public static final String TAB2_DIV               = "Tab2Div";
    public static final String TAB2_TABLE_NEW         = "Tab2TblNew";
    public static final String TAB2_TABLE3_NEW        = "Tab2Tbl3New";
    public static final String TAB2_LINK              = "Tab2Lnk";
    public static final String TAB2_SELECTED_TEXT     = "Tab2SelTxt";
    public static final String TAB2_TABLE_SELECTED_TD = "Tab2TblSelTd";
    public static final String TAB3_DIV               = "Tab3Div";
    public static final String TAB3_TABLE_NEW         = "Tab3TblNew";
    public static final String TAB3_LINK              = "Tab3Lnk";
    public static final String TAB3_SELECTED_TEXT     = "Tab3SelTxt";
    public static final String TAB3_TABLE_SELECTED_TD = "Tab3TblSelTd";
    public static final String TABGROUP               = "TabGrp";
    public static final String TABGROUPBOX            = "TabGrpBox";
    public static final String TAB_PADDING            = "TabPad";

    /** 
     * Mini Tabs Styles. 
     */
    public static final String MINI_TAB_DIV               = "MniTabDiv";    
    public static final String MINI_TAB_TABLE             = "MniTabTbl";
    public static final String MINI_TAB_LINK              = "MniTabLnk";
    public static final String MINI_TAB_SELECTED_TEXT     = "MniTabSelTxt";
    public static final String MINI_TAB_TABLE_SELECTED_TD = "MniTabTblSelTd";

    /** 
     * Properties Page Jump Links Styles.
     */
    public static final String JUMP_LINK     = "JmpLnk";
    public static final String JUMP_TOP_LINK = "JmpTopLnk";

    /** 
     * Content Pages Styles.
     */ 
    public static final String CONTENT_DEFAULT_TEXT         = "ConDefTxt";
    public static final String CONTENT_LIN		    = "ConLin";
    public static final String CONTENT_FIELDSET_DIV         = "ConFldSetDiv";
    public static final String CONTENT_FIELDSET             = "ConFldSet";
    public static final String CONTENT_FIELDSET_LEGEND      = "ConFldSetLgd";
    public static final String CONTENT_FIELDSET_LEGEND_DIV  = "ConFldSetLgdDiv";
    public static final String CONTENT_SUBSECTION_DIV       = "ConSubSecDiv";
    public static final String CONTENT_SUBSECTION_TITLE_TEXT = 
        "ConSubSecTtlTxt";
    public static final String CONTENT_TABLE_COL1_DIV       = "ConTblCl1Div";
    public static final String CONTENT_TABLE_COL2_DIV       = "ConTblCl2Div";
    public static final String CONTENT_JUMP_SECTION_DIV     = "ConJmpScnDiv";
    public static final String CONTENT_JUMP_LINK_DIV        = "ConJmpLnkDiv";
    public static final String CONTENT_REQUIRED_DIV         = "ConRqdDiv";
    public static final String CONTENT_REQUIRED_TEXT        = "ConRqdTxt";
    public static final String CONTENT_JUMP_TOP_DIV         = "ConJmpTopDiv";
    public static final String CONTENT_ERROR_LABEL_TEXT     = "ConErrLblTxt";
    public static final String CONTENT_EMBEDDED_TABLE_COL1_DIV =
	"ConEmbTblCl1Div";
    public static final String CONTENT_EMBEDDED_TABLE_COL2_DIV =
	"ConEmbTblCl2Div";

    /** 
     * File Chooser Styles. 
     */
    public static final String FILECHOOSER_CONMGN            = "ConMgn";
    public static final String FILECHOOSER_LABEL_TXT         = "ChoLblTxt";
    public static final String FILECHOOSER_NAME_TXT          = "ChoSrvTxt";
    public static final String FILECHOOSER_CONTROL_BTN       = "BtnAryDiv";

    /** 
     * Tree Styles. 
     */
    public static final String TREE			    = "Tree";
    public static final String TREE_CONTENT		    = "TreeContent";
    public static final String TREE_HAS_SELECTED_CHILD_LINK = "TreeParentLink";
    public static final String TREE_LINK		    = "TreeLink";
    public static final String TREE_LINK_SPACE		    = "TreeLinkSpace";
    public static final String TREE_NODE_IMAGE		    = "TreeImg";
    public static final String TREE_NODE_IMAGE_HEIGHT	    = "TreeImgHeight";
    public static final String TREE_ROOT_ROW		    = "TreeRootRow";
    public static final String TREE_ROOT_ROW_HEADER	    =
	"TreeRootRowHeader";
    public static final String TREE_ROW			    = "TreeRow";
    public static final String TREE_SELECTED_LINK	    = "TreeSelLink";
    public static final String TREE_SELECTED_ROW	    = "TreeSelRow";
    public static final String TREE_SELECTED_TEXT	    = "TreeSelText";

    /** 
     * Version Styles. 
     */
    public static final String VERSION_BODY                 = "VrsBdy";
    public static final String VERSION_TEXT 		    = "VrsTxt";
    public static final String VERSION_MASTHEAD_BODY	    = "VrsMstBdy";
    public static final String VERSION_BUTTON_BODY	    = "VrsBtnBdy";
    public static final String VERSION_PRODUCT_DIV	    = "VrsPrdDiv";
    public static final String VERSION_PRODUCT_TD           = "VrsPrdTd";
    public static final String VERSION_LOGO_TD              = "VrsLgoTd";
    public static final String VERSION_BUTTON_MARGIN_DIV    = "VrsBtnAryDiv";
    public static final String VERSION_MARGIN               = "VrsMgn";
    public static final String VERSION_HEADER_TEXT 	    = "VrsHdrTxt";

    /**
     * Skip navigation links Styles.
     */
    public static final String SKIP_WHITE            = "SkpWht";
    public static final String SKIP_MEDIUM_GREY1     = "SkpMedGry1";

    /** 
     * Wizard Styles.
     */
    public static final String WIZARD                   = "Wiz";
    public static final String WIZARD_STEP_LINK         = "WizStpLnk";
    public static final String WIZARD_BODY              = "WizBdy";
    public static final String WIZARD_BUTTON            = "WizBtn";
    public static final String WIZARD_BUTTON_DIV        = "WizBtnDiv";
    public static final String WIZARD_CONTENT_HELP_TEXT = "WizCntHlpTxt";
    public static final String WIZARD_HELP_DIV          = "WizHlpDiv";
    public static final String WIZARD_HELP_TEXT         = "WizHlpTxt";
    public static final String WIZARD_STEP              = "WizStp";
    public static final String WIZARD_STEP_ARROW_DIV    = "WizStpArwDiv";
    public static final String WIZARD_STEP_CURRENT_TEXT = "WizStpCurTxt";
    public static final String WIZARD_STEP_NUMBER_DIV   = "WizStpNumDiv";
    public static final String WIZARD_STEP_TABLE        = "WizStpTbl";
    public static final String WIZARD_STEP_TAB          = "WizStpTab";
    public static final String WIZARD_STEP_TEXT         = "WizStpTxt";
    public static final String WIZARD_STEP_TEXT_DIV     = "WizStpTxtDiv";
    public static final String WIZARD_SUB_TITLE_DIV     = "WizSubTtlDiv";
    public static final String WIZARD_SUB_TITLE_TEXT    = "WizSubTtlTxt";
    public static final String WIZARD_SUBSTEP_TITLE_DIV = "WizSubStpTtlDiv";
    public static final String WIZARD_SUBSTEP_TITLE_TEXT = "WizSubStpTtlTxt";
    public static final String WIZARD_TASK              = "WizTsk";
    public static final String WIZARD_TITLE             = "WizTtl";

    /**
     * Date/Time Styles.
     */
    public static final String DATE_TIME_DAY_HEADER      = "DatDayHdrTxt";
    public static final String DATE_TIME_LABEL_TEXT      = "DatLblTxt";
    public static final String DATE_TIME_LINK            = "DatLnk";
    public static final String DATE_TIME_OTHER_BOLD_LINK = "DatOthBldLnk";
    public static final String DATE_TIME_OTHER_LINK      = "DatOthLnk";
    public static final String DATE_TIME_BOLD_LINK       = "DatBldLnk";
    public static final String DATE_TIME_TODAY_LINK      = "DatCurLnk";   
    public static final String DATE_TIME_ZONE_TEXT       = "DatZonTxt";
    public static final String DATE_TIME_SELECT_DIV      = "DatSelDiv";
    public static final String DATE_TIME_CALENDAR_DIV    = "DatCalDiv";
    public static final String DATE_TIME_CALENDAR_TABLE  = "DatCalTbl";


    /**
     * EditableList styles 
     */ 
    public static final String EDITABLELIST_TABLE   = "EdtLstTbl";
    public static final String EDITABLELIST_FIELD_LABEL = "EdtLstAddLblTd"; 
    public static final String EDITABLELIST_FIELD = "EdtLstAddTxtTd"; 
    public static final String EDITABLELIST_ADD_BUTTON = "EdtLstAddBtnTd"; 
    public static final String EDITABLELIST_LIST_LABEL = "EdtLstRmvLblTd";
    public static final String EDITABLELIST_LIST = "EdtLstRmvLstTd"; 
    public static final String EDITABLELIST_REMOVE_BUTTON = "EdtLstRmvBtnTd"; 

    /**
     * Message and Message Group Styles.
     */
    public static final String MESSAGE_FIELD_SUMMARY_TEXT = "MsgFldSumTxt";
    public static final String MESSAGE_FIELD_TEXT = "MsgFldTxt";

    public static final String MESSAGE_GROUP_TABLE = "MsgGrpTbl";
    public static final String MESSAGE_GROUP_TABLE_TITLE = "MsgGrpTblTtl";
    public static final String MESSAGE_GROUP_DIV = "MsgGrpDiv";
    public static final String MESSAGE_GROUP_SUMMARY_TEXT = "MsgGrpSumTxt";
    public static final String MESSAGE_GROUP_TEXT = "MsgGrpTxt";
    public static final String MESSAGE_INFO = "MsgInfo";
    public static final String MESSAGE_ERROR = "MsgError";
    public static final String MESSAGE_FATAL = "MsgFatal";
    public static final String MESSAGE_WARN = "MsgWarn";
    public static final String MESSAGE_GROUP_INFO = "MsgGrpInfo";
    public static final String MESSAGE_GROUP_ERROR = "MsgGrpError";
    public static final String MESSAGE_GROUP_FATAL = "MsgGrpFatal";
    public static final String MESSAGE_GROUP_WARN = "MsgGrpWarn";

    /**
     * This private constructor prevents this class from being instantiated
     * directly as its only purpose is to provide image constants.
     */
    private ThemeStyles() {
	// do nothing
    }
}


