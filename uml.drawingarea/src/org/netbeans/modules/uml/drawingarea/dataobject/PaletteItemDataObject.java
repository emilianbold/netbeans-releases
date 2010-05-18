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

package org.netbeans.modules.uml.drawingarea.dataobject;

import java.io.IOException;
import org.netbeans.modules.uml.drawingarea.palette.NodeInitializer;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.text.DataEditorSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class PaletteItemDataObject extends MultiDataObject
{
    static final String XML_ROOT = "palette_item"; // NOI18N
    static final String ATTR_VERSION = "version"; // NOI18N
    static final String TAG_COMPONENT = "element"; // NOI18N
    static final String ATTR_CLASSNAME = "name"; // NOI18N
    
    static final String TAG_DESCRIPTION = "description"; // NOI18N
    static final String ATTR_BUNDLE = "localizing-bundle"; // NOI18N
    static final String ATTR_DISPLAY_NAME_KEY = "display-name-key"; // NOI18N
    static final String ATTR_TOOLTIP_KEY = "tooltip-key"; // NOI18N
    
    static final String TAG_ICON16 = "icon16"; // NOI18N
    static final String ATTR_URL = "urlvalue"; // NOI18N
    static final String TAG_ICON32 = "icon32"; // NOI18N
    
    static final String STEREOTYPE = "stereotype";
    static final String ATTR_VALUE = "value";
    
    static final String VIEW = "view";
    
    private PaletteItem paletteItem = null;
    
    // some raw data read from the file (other passed to PaletteItem)
    private String displayName_key;
    private String tooltip_key;
    private String bundleName;
    private String icon16URL;
    private String icon32URL;
    private String stereotype;
    private String view;
    
    public PaletteItemDataObject(FileObject pf, PaletteItemDataLoader loader) 
            throws DataObjectExistsException, IOException
    {
        super(pf, loader);
        CookieSet cookies = getCookieSet();
        cookies.add((Node.Cookie) DataEditorSupport.create(this, getPrimaryEntry(), cookies));
        
        NodeInitializer initializer = (NodeInitializer) pf.getAttribute("initializer");
        
        loadFile();
        
        if ((paletteItem != null) && (initializer instanceof NodeInitializer))
        {
            paletteItem.setInitializer((NodeInitializer) initializer);

        }

        
        // I am adding the palettedItem to the cookie set so the lookup will
        // find the item.
        getCookieSet().add(paletteItem);
    }
    
    @Override
    protected Node createNodeDelegate()
    {
        return new PaletteItemDataNode(this, getLookup());
    }
    
    public @Override Lookup getLookup()
    {
        return getCookieSet().getLookup();
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public Node.Cookie getCookie(Class cookieClass)
    {
        if (PaletteItem.class.equals(cookieClass))
        {
            return paletteItem;
        }
        return super.getCookie(cookieClass);
    }

    ///////////////////////////////////////////////////////////////
    // Data Access
    
    String getDisplayNameKey()
    {
        return displayName_key;
    }
    
    String getTooltipKey()
    {
        return tooltip_key;
    }
    
    String getBundleName()
    {
        return bundleName;
    }
    
    String getSmallIconURL()
    {
        return icon16URL;
    }
    
    String getLargeIconURL()
    {
        return icon32URL;
    }
    
    ///////////////////////////////////////////////////////////////
    // Helper Methods
    
    boolean isItemValid() 
    {
        return paletteItem != null;
    }
    
    

    ///////////////////////////////////////////////////////////////
    // Loading Data.
    private void loadFile()
    {
        FileObject file = getPrimaryFile();
        if (file.getSize() == 0L)
        {
            // item file is empty
            // just derive the component class name from the file name
            return;
        }

        // parse the XML file
        try
        {
            XMLReader reader = XMLUtil.createXMLReader();
            PaletteItemHandler handler = new PaletteItemHandler();
            reader.setContentHandler(handler);
            InputSource input = new InputSource(getPrimaryFile().getURL().toExternalForm()); 
            reader.parse(input);
            
            if (handler.componentClassName != null)
            {              
                paletteItem = new PaletteItem(this, handler.componentClassName);
                
                if((stereotype != null) && (stereotype.length() > 0))
                {
                    paletteItem.setStereotype(stereotype);
                }
                
                if((view != null) && (view.length() > 0))
                {
                    paletteItem.setDefaultViewName(view);
                }
            }
        }
        catch (SAXException saxex)
        {
            Exceptions.printStackTrace(saxex);
        }
        catch (IOException ioex)
        {
            Exceptions.printStackTrace(ioex);
        }
    }

    private class PaletteItemHandler extends DefaultHandler
    {

        String componentClassName;

        @Override
        public void startDocument() throws SAXException
        {
            componentClassName = null;
        }

        @Override
        public void startElement(String uri, 
                                 String localName, 
                                 String qName, 
                                 Attributes attributes) 
                                 throws SAXException
        {
            if (XML_ROOT.equals(qName))
            {
                String version = attributes.getValue(ATTR_VERSION);
                if (version == null)
                {
                    String message = NbBundle.getBundle(PaletteItemDataObject.class).getString("MSG_UnknownPaletteItemVersion"); // NOI18N
                    throw new SAXException(message);
                }
                else if (!version.startsWith("1."))
                {
                    // NOI18N
                    String message = NbBundle.getBundle(PaletteItemDataObject.class).getString("MSG_UnsupportedPaletteItemVersion"); // NOI18N
                    throw new SAXException(message);
                }
                // TODO item ID (for now we take the class name as the ID)
            }
            else if (TAG_COMPONENT.equals(qName))
            {
                String className = attributes.getValue(ATTR_CLASSNAME);
                componentClassName = className;
            }
            else if(STEREOTYPE.equals(qName))
            {
                stereotype = attributes.getValue(ATTR_VALUE);
            }
            else if(VIEW.equals(qName))
            {
                view = attributes.getValue(ATTR_VALUE);
            }
            else if (TAG_DESCRIPTION.equals(qName))
            {
                String bundle = attributes.getValue(ATTR_BUNDLE);
                if (bundle != null)
                {
                    PaletteItemDataObject.this.bundleName = bundle;
                }
                String displayNameKey = attributes.getValue(ATTR_DISPLAY_NAME_KEY);
                if (displayNameKey != null)
                {
                    PaletteItemDataObject.this.displayName_key = displayNameKey;
                }
                String tooltipKey = attributes.getValue(ATTR_TOOLTIP_KEY);
                if (tooltipKey != null)
                {
                    PaletteItemDataObject.this.tooltip_key = tooltipKey;
                }
            }
            else if (TAG_ICON16.equals(qName))
            {
                String url = attributes.getValue(ATTR_URL);
                if (url != null)
                {
                    PaletteItemDataObject.this.icon16URL = url;
                }
                // TODO support also class resource name for icons
            }
            else if (TAG_ICON32.equals(qName))
            {
                String url = attributes.getValue(ATTR_URL);
                if (url != null)
                {
                    PaletteItemDataObject.this.icon32URL = url;
                }
                // TODO support also class resource name for icons
            }
        }
    }

}
