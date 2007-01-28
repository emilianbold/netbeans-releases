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
 *
 * $Id$
 */
package org.netbeans.installer.product;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.TreePath;
import org.netbeans.installer.product.filters.RegistryFilter;
import org.netbeans.installer.product.filters.TrueFilter;
import org.netbeans.installer.utils.FileProxy;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.XMLUtils;
import org.netbeans.installer.utils.exceptions.DownloadException;
import org.netbeans.installer.utils.exceptions.FinalizationException;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.exceptions.ParseException;
import org.netbeans.installer.utils.helper.ExtendedUri;
import org.netbeans.installer.utils.helper.PropertyContainer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author Kirill Sorokin
 */
public abstract class RegistryNode implements PropertyContainer {
    protected RegistryNode          parent        = null;
    
    protected String                uid           = null;
    
    protected ExtendedUri           iconUri       = null;
    protected Icon                  icon          = null;
    
    protected long                  offset        = 0;
    protected boolean               expand        = false;
    protected boolean               visible       = true;
    
    protected Date                  built         = new Date();
    
    protected Map<Locale, String>   displayNames  = new HashMap<Locale, String>();
    protected Map<Locale, String>   descriptions  = new HashMap<Locale, String>();
    
    protected List<RegistryNode>    children      = new ArrayList<RegistryNode>();
    
    protected Properties            properties    = new Properties();
    
    public String getUid() {
        return uid;
    }
    
    public String getDisplayName() {
        return getDisplayName(Locale.getDefault());
    }
    
    public String getDisplayName(final Locale locale) {
        return displayNames.get(locale);
    }
    
    public Map<Locale, String> getDisplayNames() {
        return displayNames;
    }
    
    public void setDisplayName(final String displayName) {
        setDisplayName(Locale.getDefault(), displayName);
    }
    
    public void setDisplayName(final Locale locale, final String displayName) {
        displayNames.put(locale, displayName);
    }
    
    public String getDescription() {
        return getDescription(Locale.getDefault());
    }
    
    public String getDescription(final Locale locale) {
        return descriptions.get(locale);
    }
    
    public Map<Locale, String> getDescriptions() {
        return descriptions;
    }
    
    public void setDescription(final String description) {
        setDescription(Locale.getDefault(), description);
    }
    
    public void setDescription(final Locale locale, final String description) {
        descriptions.put(locale, description);
    }
    
    public ExtendedUri getIconUri() {
        return iconUri;
    }
    
    public Icon getIcon() {
        return icon;
    }
    
    public long getOffset() {
        return offset;
    }
    
    public boolean isVisible() {
        return visible;
    }
    
    public void setVisible(final boolean visible) {
        this.visible = visible;
    }
    
    public boolean getExpand() {
        return expand;
    }
    
    public Date getBuildDate() {
        return built;
    }
    
    // tree /////////////////////////////////////////////////////////////////////////
    public RegistryNode getParent() {
        return parent;
    }
    
    public void setParent(final RegistryNode parent) {
        this.parent = parent;
    }
    
    public List<RegistryNode> getChildren() {
        return children;
    }
    
    public List<RegistryNode> getVisibleChildren() {
        List<RegistryNode> visibleChildren = new LinkedList<RegistryNode>();
        
        for (RegistryNode child: children) {
            if (child.isVisible()) {
                visibleChildren.add(child);
            }
        }
        
        return visibleChildren;
    }
    
    public void addChild(final RegistryNode child) {
        child.setParent(this);
        
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i).getOffset() > child.getOffset()) {
                children.add(i, child);
                return;
            }
        }
        children.add(child);
    }
    
    public void removeChild(final RegistryNode child) {
        children.remove(child);
    }
    
    public boolean isAncestor(final RegistryNode candidate) {
        for (RegistryNode node: getChildren()) {
            if ((node == candidate) || node.isAncestor(candidate)) {
                return true;
            }
        }
        
        return false;
    }
    
    public boolean isAncestor(final List<? extends RegistryNode> candidates) {
        for (RegistryNode node: candidates) {
            if (isAncestor(node)) {
                return true;
            }
        }
        
        return false;
    }
    
    public boolean hasChildren() {
        return hasChildren(new TrueFilter());
    }
    
    public boolean hasChildren(RegistryFilter filter) {
        for (RegistryNode child: children) {
            if (filter.accept(child)) {
                return true;
            }
            
            if (child.hasChildren(filter)) {
                return true;
            }
        }
        
        return false;
    }
    
    public TreePath getTreePath() {
        List<RegistryNode> nodes = new LinkedList<RegistryNode>();
        
        RegistryNode node = this;
        while (node != null) {
            nodes.add(0, node);
            node = node.getParent();
        }
        
        return new TreePath(nodes.toArray());
    }
    
    // properties ///////////////////////////////////////////////////////////////////
    public Properties getProperties() {
        return properties;
    }
    
    public String getProperty(final String name) {
        return properties.getProperty(name);
    }
    
    public void setProperty(final String name, final String value) {
        properties.setProperty(name, value);
    }
    
    // node <-> dom /////////////////////////////////////////////////////////////////
    public Element saveToDom(Document document, RegistryFilter filter) throws FinalizationException {
        final boolean hasChilren = hasChildren(filter);
        
        if (filter.accept(this) || hasChilren) {
            Element element = saveToDom(document.createElement(getTagName()));
            
            if (hasChilren) {
                element.appendChild(saveChildrenToDom(document, filter));
            }
            
            return element;
        } else {
            return null;
        }
    }
    
    public Element saveChildrenToDom(Document document, RegistryFilter filter) throws FinalizationException {
        Element components = null;
        
        if (hasChildren(filter)) {
            components = document.createElement("components");
            
            for (RegistryNode child: children) {
                if (filter.accept(child) || child.hasChildren(filter)) {
                    components.appendChild(child.saveToDom(document, filter));
                }
            }
        }
        
        return components;
    }
    
    protected String getTagName() {
        return "node";
    }
    
    protected Element saveToDom(Element element) throws FinalizationException {
        Document document = element.getOwnerDocument();
        
        element.setAttribute("uid", getUid());
        element.setAttribute("offset", Long.toString(getOffset()));
        element.setAttribute("expand", Boolean.toString(getExpand()));
        
        element.setAttribute("built", Long.toString(built.getTime()));
        
        Element displayNameNode = document.createElement("display-name");
        
        Element defaultNameNode = document.createElement("default");
        defaultNameNode.setTextContent(getDisplayName());
        displayNameNode.appendChild(defaultNameNode);
        
        for (Locale locale: getDisplayNames().keySet()) {
            String localized = getDisplayName(locale);
            
            if (!localized.equals(getDisplayName())) {
                Element localeNode = document.createElement("localized");
                localeNode.setAttribute("locale", locale.toString());
                localeNode.setTextContent(StringUtils.convertToAscii(localized));
                displayNameNode.appendChild(localeNode);
            }
        }
        element.appendChild(displayNameNode);
        
        Element descriptionNode = document.createElement("description");
        
        Element defaultDescriptionNode = document.createElement("default");
        defaultDescriptionNode.setTextContent(getDescription());
        descriptionNode.appendChild(defaultDescriptionNode);
        
        for (Locale locale: getDescriptions().keySet()) {
            String localized = getDescription(locale);
            
            if (!localized.equals(getDescription())) {
                Element localeNode = document.createElement("localized");
                localeNode.setAttribute("locale", locale.toString());
                localeNode.setTextContent(StringUtils.convertToAscii(localized));
                descriptionNode.appendChild(localeNode);
            }
        }
        element.appendChild(descriptionNode);
        
        Element iconNode = document.createElement("icon");
        iconNode.setAttribute("size", Long.toString(iconUri.getSize()));
        iconNode.setAttribute("md5", iconUri.getMd5());
        
        Element defaultUriNode = document.createElement("default-uri");
        if (iconUri.getLocal() != null) {
            defaultUriNode.setTextContent(iconUri.getLocal().toString());
        } else {
            defaultUriNode.setTextContent(iconUri.getRemote().toString());
        }
        iconNode.appendChild(defaultUriNode);
        element.appendChild(iconNode);
        
        if (getProperties().size() > 0) {
            Element propertiesNode = document.createElement("properties");
            
            for (Object key: getProperties().keySet()) {
                String name = (String) key;
                
                Element propertyNode = document.createElement("property");
                
                propertyNode.setAttribute("name", name);
                propertyNode.setTextContent(getProperty(name));
                
                propertiesNode.appendChild(propertyNode);
            }
            
            element.appendChild(propertiesNode);
        }
        
        return element;
    }
    
    public RegistryNode loadFromDom(Element element) throws InitializationException {
        List<Node> nodes;
        
        try {
            uid = element.getAttribute("uid");
            
            iconUri = XMLUtils.parseExtendedUri(XMLUtils.getChild(element, "icon"));
            
            final File iconFile = 
                    FileProxy.getInstance().getFile(iconUri.getRemote());
            
            icon = new ImageIcon(iconFile.getPath());
            iconUri.setLocal(iconFile.toURI());
            
            offset = Long.parseLong(element.getAttribute("offset"));
            visible = Boolean.parseBoolean(element.getAttribute("visible"));
            expand = Boolean.parseBoolean(element.getAttribute("expand"));
            
            built = new Date(Long.parseLong(element.getAttribute("built")));
            
            String displayName = XMLUtils.getChildNodeTextContent(element, "./display-name/default");
            displayNames.put(Locale.getDefault(), displayName);
            
            nodes = XMLUtils.getChildList(element, "./display-name/localized");
            for (Node localized: nodes) {
                Locale locale = StringUtils.parseLocale(
                        XMLUtils.getAttribute(localized, "locale"));
                String localizedName = StringUtils.parseAscii(
                        XMLUtils.getTextContent(localized));
                
                displayNames.put(locale, localizedName);
            }
            
            String description = XMLUtils.getChildNodeTextContent(element, "./description/default");
            descriptions.put(Locale.getDefault(), description);
            
            nodes = XMLUtils.getChildList(element, "./description/localized");
            for (Node localized: nodes) {
                Locale locale = StringUtils.parseLocale(
                        XMLUtils.getAttribute(localized, "locale"));
                String localizedDescription = StringUtils.parseAscii(
                        XMLUtils.getTextContent(localized));
                
                descriptions.put(locale, localizedDescription);
            }
            
            nodes = XMLUtils.getChildList(element, "./properties/property");
            for (Node node: nodes) {
                String name = XMLUtils.getAttribute(node, "name");
                String value = XMLUtils.getTextContent(node);
                
                properties.setProperty(name, value);
            }
        } catch (ParseException e) {
            throw new InitializationException("Cannot deserialize product tree node", e);
        } catch (DownloadException e) {
            throw new InitializationException("Cannot deserialize product tree node", e);
        } catch (NumberFormatException e) {
            throw new InitializationException("Cannot deserialize product tree node", e);
        }
        
        return this;
    }
    
    // node -> string ///////////////////////////////////////////////////////////////
    public String toString() {
        return getDisplayName();
    }
}
