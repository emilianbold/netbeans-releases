/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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

package org.netbeans.modules.debugger.delegatingview;

import java.awt.Component;
import java.awt.Container;
import java.awt.Image;
import java.awt.event.*;
import java.net.URL;
import java.util.*;
import java.beans.*;
import javax.swing.border.*;
import javax.swing.*;

import org.openide.TopManager;
import org.openide.awt.ToolbarToggleButton;
import org.openide.util.HelpCtx;
import org.openide.windows.TopComponent;

import org.netbeans.modules.debugger.GUIManager;
import org.netbeans.modules.debugger.GUIManager.View;
import org.netbeans.modules.debugger.support.View2;


