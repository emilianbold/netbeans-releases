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

package org.netbeans.modules.web.core.syntax;

import javax.swing.text.Document;
import org.netbeans.spi.editor.errorstripe.UpToDateStatusProvider;
import org.netbeans.spi.editor.errorstripe.UpToDateStatusProviderFactory;
 
/**
 *
 * @author Jan Lahoda, Marek Fukala
 */
public final class JspUpToDateStatusProviderFactory implements UpToDateStatusProviderFactory {
    
    /** Creates a new instance of AnnotationMarkProviderCreator */
    public JspUpToDateStatusProviderFactory() {
    }

    public UpToDateStatusProvider createUpToDateStatusProvider(Document doc) {
        if (doc.getProperty(Document.StreamDescriptionProperty) != null)
            return JspUpToDateStatusProvider.get(doc);
        else
            return null;
    }
    
}
