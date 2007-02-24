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

package org.netbeans.modules.uml.project.ui.nodes;

import java.io.IOException;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementImport;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem;
import org.openide.nodes.Children;
import org.openide.util.Lookup;

/**
 *
 * @author Trey Spiva
 */
public class ImportedElementNode extends UMLModelElementNode
{
    private IElementImport importedElement = null;
    
    /** Creates a new instance of ImportedElementNode */
    public ImportedElementNode()
    {
    }
    
    public ImportedElementNode(IElementImport imported)
    {
        super();
        setImportedElement(imported);
    }
    
    public ImportedElementNode(Lookup lookup, IElementImport imported)
    {
        super(lookup);
        setImportedElement(imported);
    }
    
    public ImportedElementNode(Children ch, Lookup lookup, IElementImport imported)
    {
        super(ch, lookup);
        setImportedElement(imported);
    }
    
    public IElementImport getImportedElement()
    {
        return importedElement;
    }

    public void setImportedElement(IElementImport importedElement)
    {
        this.importedElement = importedElement;
    }
    
    public void destroy() throws IOException
    {
        importedElement.delete();
    }
}
