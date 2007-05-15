/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 * 
 *     "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 */

package org.netbeans.installer.wizard.ui;

import org.netbeans.installer.wizard.containers.SwingContainer;

/**
 * This class represents the UI of a wizard component. It is an abstraction over the
 * set of possible UI modes for the wizard and provides factory methods which create
 * objects representing component's UI for a concrete wizard UI mode, such as 
 * {@link SwingUi}.
 * 
 * @see org.netbeans.installer.utils.helper.UiMode
 * 
 * @author Kirill Sorokin
 * @since 1.0
 */
public interface WizardUi {
    /**
     * Creates an instance of {@link SwingUi} and initializes it with the specified 
     * {@link SwingContainer} object, thus initializaing the component's UI for 
     * {@link org.netbeans.installer.utils.helper.UiMode#SWING}.
     * 
     * @param container Instance of {@link SwingContainer} which will "contain" the
     *      resulting UI.
     * @return Instance of {@link SwingUi} which represents the component's UI for 
     *      the swing UI mode.
     */
    SwingUi getSwingUi(final SwingContainer container);
}
