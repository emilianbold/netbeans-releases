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
 * Microsystems, Inc. 
 * Portions Copyrighted 2006 Ricoh Corporation 
 * All Rights Reserved.
 */
/*
 * DOMHandler.java
 *
 * Created on October 18, 2006, 12:49 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ricoh.util.dom;

import java.io.File;
import org.w3c.dom.*;
import javax.xml.parsers.*;

/**
 *
 * @author esanchez
 */
abstract public class DOMHandler 
{
    private boolean compress;
    protected Document document;
    
    /** Creates a new instance of DOMHandler */
    public DOMHandler() 
    {
        compress = true;
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try
        {
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.newDocument();
        }
        catch(ParserConfigurationException pce)
        {
            pce.printStackTrace();
            System.out.println(pce.getMessage());
        }
    }
    
    public void setCompression(boolean choice)
    {
        compress = true;
    }
    
    public boolean getCompression()
    {
        return compress;
    }    
    
    abstract public Document makeDocument();
    
    abstract public Document loadDocument(File xmlFile);
}
