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


package org.netbeans.modules.uml.core.metamodel.diagrams;

/**
 *
 * @author Trey Spiva
 */
public interface DiagramEnums
{
   //**************************************************
   // SynchStateKind Enumeration
   //**************************************************
   public final static int SSK_UNKNOWN_SYNCH_STATE = 0;
   public final static int SSK_IN_SYNCH_SHALLOW = 1;
   public final static int SSK_OUT_OF_SYNCH = 2;
   public final static int SSK_IN_SYNCH_DEEP = 3;

   //**************************************************
   // DiagramValidateKind Enumeration
   //**************************************************
   public final static int DVK_VALIDATE_NONE = 0;
   public final static int DVK_VALIDATE_DRAWENGINE = 1;
   public final static int DVK_VALIDATE_LINKENDS = 2;
   public final static int  DVK_VALIDATE_CONNECTIONTOELEMENT = 3;
   public final static int DVK_VALIDATE_BRIDGES = 4;
   public final static int DVK_VALIDATE_RESYNC_DEEP = 5;
   public final static int DVK_VALIDATE_ALL = 6;

   //**************************************************
   // DiagramValidateResponse Enumeration
   //**************************************************
   public final static int DVRSP_NONE = 0;
   public final static int DVRSP_DELETE_INVALID_NODES = 1;
   public final static int DVRSP_DELETE_INVALID_LINKS = 2;
   public final static int DVRSP_RECONNECT_INVALID_LINKS = 3;
   public final static int DVRSP_DELETE_INVALID_DRAW_ENGINES = 4;
   public final static int DVRSP_RESET_INVALID_DRAW_ENGINES = 5;
   public final static int DVRSP_VALIDATE_ALL = 6;

   //**************************************************
   // DiagramValidateResult Enumeration
   //**************************************************
   public final static int DVR_NOT_APPLICABLE = 0;
   public final static int DVR_INVALID = 1;
   public final static int DVR_VALID = 2;

   //**************************************************
   // LayoutKind Enumeration
   //**************************************************
   public final static int LK_NO_LAYOUT = 0;
   public final static int LK_HIERARCHICAL_LAYOUT = 1;
   public final static int LK_CIRCULAR_LAYOUT = 2;
   public final static int LK_SYMMETRIC_LAYOUT = 3;
   public final static int LK_TREE_LAYOUT = 4;
   public final static int LK_ORTHOGONAL_LAYOUT = 5;
   public final static int LK_SEQUENCEDIAGRAM_LAYOUT = 6;
   public final static int LK_GLOBAL_LAYOUT = 7;
   public final static int LK_INCREMENTAL_LAYOUT = 8;
   public final static int LK_UNKNOWN_LAYOUT = 255;

   //**************************************************
   // DiagramKind Enumeration
   //**************************************************
   public final static int DK_UNKNOWN = 0;
   public final static int DK_DIAGRAM = 1;
   public final static int DK_ACTIVITY_DIAGRAM = 2;
   public final static int DK_CLASS_DIAGRAM = 4;
   public final static int DK_COLLABORATION_DIAGRAM = 8;
   public final static int DK_COMPONENT_DIAGRAM = 16;
   public final static int DK_DEPLOYMENT_DIAGRAM = 32;
   public final static int DK_SEQUENCE_DIAGRAM = 64;
   public final static int DK_STATE_DIAGRAM = 128;
   public final static int DK_USECASE_DIAGRAM = 256;
   public final static int DK_ALL = 0xffff;

   //**************************************************
   // DrawingFileCode Enumeration
   //**************************************************
   public final static int DFC_FILE_OK = 0;
   public final static int DFC_FILE_COULD_NOT_OPEN = 2;
   public final static int  DFC_FILE_COULD_NOT_CLOSE = 3;
   public final static int DFC_FILE_ERROR_READING_STREAM = 4;
   public final static int DFC_FILE_ERROR_WRITING_STREAM = 5;
   public final static int DFC_FILE_NULL_INPUT = 6;
   public final static int DFC_FILE_NULL_OUTPUT = 7;

   //**************************************************
   // StackingOrderKind Enumeration
   //**************************************************
   public final static int SOK_MOVEFORWARD = 0;
   public final static int SOK_MOVETOFRONT = 1;
   public final static int SOK_MOVEBACKWARD = 2;
   public final static int SOK_MOVETOBACK = 3;

   //**************************************************
   // DrawingToolKind Enumeration
   //**************************************************
   public final static int DTK_SELECTION = 0;
   public final static int DTK_PAN = 1;
   public final static int DTK_ACCORDION = 2;
   public final static int DTK_ZOOM = 3;
   public final static int DTK_MOUSE_ZOOM = 4;
   public final static int DTK_EDGENAV_MOUSE = 5;

   //**************************************************
   // GridKind Enumeration
   //**************************************************
   public final static int GK_LINES = 0;
   public final static int GK_POINTS = 1;
}
