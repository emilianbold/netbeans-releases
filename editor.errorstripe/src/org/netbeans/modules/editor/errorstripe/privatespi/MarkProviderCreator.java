/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.errorstripe.privatespi;
import javax.swing.text.JTextComponent;

/**A creator for MarkProviders. Should register an instance under <code>Editors/text/base/Services</code>
 * or <code>Editors/&lt;MIME-Type&gt;/Services</code>.
 *
 * @author Jan Lahoda
 */
public interface MarkProviderCreator {
    
    /**Create an instance of {@link MarkProvider} for a given {@link Document}.
     *
     * @return an instance {@link MarkProvider} for a given {@link Document}
     */
    public MarkProvider createMarkProvider(JTextComponent document);
    
}
