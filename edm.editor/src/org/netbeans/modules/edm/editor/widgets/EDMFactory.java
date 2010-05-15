/*
 * The contents of this file are subject to the terms of the Common
 * Development and Distribution License (the License). You may not use this 
 * file except in compliance with the License.  You can obtain a copy of the
 *  License at http://www.netbeans.org/cddl.html
 *
 * When distributing Covered Code, include this CDDL Header Notice in each
 * file and include the License. If applicable, add the following below the
 * CDDL Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems, Inc. All Rights Reserved
 *
 */
package org.netbeans.modules.edm.editor.widgets;

import org.netbeans.api.visual.border.Border;

/**
 * Used as a factory class for objects defined in VMD visualization style.
 *
 * @author David Kaspar
 */
public final class EDMFactory {

    private static final Border BORDER_NODE = new EDMNodeBorder ();

    private EDMFactory () {
    }

    /**
     * Creates a border used by VMD node.
     * @return the VMD node border
     */
    public static Border createEDMNodeBorder () {
        return BORDER_NODE;
    }

}
