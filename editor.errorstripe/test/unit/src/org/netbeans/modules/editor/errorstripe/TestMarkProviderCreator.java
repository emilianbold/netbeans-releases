/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.errorstripe;

import javax.swing.text.Document;
import org.netbeans.modules.editor.errorstripe.spi.MarkProvider;
import org.netbeans.modules.editor.errorstripe.spi.MarkProviderCreator;

/**
 *
 * @author Jan Lahoda
 */
public class TestMarkProviderCreator implements MarkProviderCreator {
    
    private MarkProvider provider;
    
    /** Creates a new instance of TestMarkProviderCreator */
    public TestMarkProviderCreator(MarkProvider provider) {
        this.provider = provider;
    }

    public MarkProvider createMarkProvider(Document document) {
        return provider;
    }
    
}
