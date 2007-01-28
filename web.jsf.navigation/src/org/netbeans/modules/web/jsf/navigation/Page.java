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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.web.jsf.navigation;

import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;
import java.awt.Image;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

import org.netbeans.modules.visualweb.insync.models.FacesModelSet;
import org.netbeans.modules.visualweb.insync.models.FacesModel;


/**
 * graph node
 */
public class Page {

    public static final int BUTTON = 1;
    public static final int HYPERLINK = 2;
    public static final int IMAGE_HYPERLINK = 3;

    private String buttonClass_bh = "com.sun.rave.web.ui.component.Button";
    private String hyperlinkClass_bh = "com.sun.rave.web.ui.component.Hyperlink";
    private String imageHyperlinkClass_bh = "com.sun.rave.web.ui.component.ImageHyperlink";

    private String buttonClass_ws = "com.sun.webui.jsf.component.Button";
    private String hyperlinkClass_ws = "com.sun.webui.jsf.component.Hyperlink";
    private String imageHyperlinkClass_ws = "com.sun.webui.jsf.component.ImageHyperlink";

    private NavigableComponent currentBean = null;


    public Page(String name, FileObject fileObject, NavigationModel doc) {
        this.setName(name);
        projectFileObject = fileObject;
        if (fileObject != null) {
            model = ((FacesModelSet)doc.getOwner()).getFacesModel(fileObject);
            if (model != null) {
                wasModelBusted = model.isBusted();
            }
            try {
                this.dobj = DataObject.find(fileObject);
                restoreLocation();
            } catch (DataObjectNotFoundException e) {
            }
        }
        this.doc = doc;
    }

    private String name;
    private NavigationModel doc;
    private DataObject dobj;
    private FileObject projectFileObject;
    private List<NavigableComponent> beans = new ArrayList(); // Components on this page
    private List<NavigableComponentLink> beanLinks; // Links from beans to ports on this page
    private Image previewImage;
    private Image previewZoomedImage;

    FacesModel model;
    // !EAT TODO  Do we need to update this flag on changes in model, or is Page object rebuilt in those cases anyway ?
    protected boolean wasModelBusted;

    // Layout information - for performance reasons provided here
    // for direct manipulation by the layout algorithms so they don't
    // have to duplicate data structures to annotate elements with
    // layout info and temporary tags etc.

    private int x = -1; // layout position
    private int y = -1;
    private int width;
    private int height;
    private int adjustmentX;
    private int adjustmentY;

    public NavigableComponent getCurrentBean(){
        if(currentBean == null){
            if (!getBeans().isEmpty()){
                return (NavigableComponent)getBeans().get(0);
            }
        }
        return currentBean;
    }

    public void setCurrentBean(NavigableComponent bean){
        currentBean = bean;
    }

    public void moveCurrentBean(int dir){
        if(!getBeans().isEmpty()){
            int currIndex = getBeans().indexOf(getCurrentBean());
            switch(dir){
                case GraphUtilities.DOWN:
                    currIndex++;
                    if(currIndex >= getBeans().size()){
                        currIndex = 0;
                    }
                    break;
                case GraphUtilities.UP:
                    currIndex--;
                    if(currIndex < 0){
                        currIndex = getBeans().size()-1;
                    }
            }
            currentBean = (NavigableComponent)getBeans().get(currIndex);
        }
    }

    public List<NavigableComponent> getBeans(){
//        return (NavigableComponent[]) beans.toArray(new NavigableComponent[beans.size()]);
        return beans;
    }
    
    public void setAdjustmentX(int adjustmentX) {
        this.adjustmentX = adjustmentX;
    }
    public void setAdjustmentY(int adjustmentY) {
        this.adjustmentY = adjustmentY;
    }
    public void setX(int x) {
        if(x < -1) x = 0;
        this.x = x;
    }
    public void setY(int y) {
        if(y < -1) y = 0;
        this.y = y;
    }
    public void setWidth(int w) {
        this.width = w;
    }
    public void setHeight(int h) {
        this.height = h;
    }
    
    public int x() {
        if (adjustmentX == 0) {
            return x;
        } else {
            int adjusted = x-adjustmentX;
            if (adjusted < GraphUtilities.LEFT_OFFSET) {
                return GraphUtilities.LEFT_OFFSET;
            }
            return adjusted;
        }
    }
    
    public int y() {
        if (adjustmentY == 0) {
            return y;
        } else {
            int adjusted = y-adjustmentY;
            if (adjusted < GraphUtilities.TOP_OFFSET) {
                return GraphUtilities.TOP_OFFSET;
            }
            return adjusted;
        }
    }
    
    public int w() { // be consistent with setWidth - use setW() or getWidth()
        if (adjustmentX == 0) {
            return width;
        } else {
            return width + 2*adjustmentX;
        }
    }
    
    public int h() { // ditto
        if (adjustmentY == 0) {
            return height;
        } else {
            return height + 2*adjustmentY;
        }
    }
    
    /**
     * Return adjust height based on number of beans in the page
     */
    public int h(boolean adjust) { // ditto
        int adjustedHeight = 0;
        
        int n = getBeans().size();
        for (int i = 0; i < n; i++) {
            NavigableComponent pageBean = (NavigableComponent)getBeans().get(i);
            if (adjustedHeight < pageBean.ly){
                adjustedHeight = pageBean.ly + pageBean.lh;
            }
        }
        adjustedHeight += 10;
        if (adjustedHeight < height) adjustedHeight = height;
        if (adjustmentY == 0) {
            return adjustedHeight;
        } else {
            return adjustedHeight + 2*adjustmentY;
        }
    }
    
    public FacesModel getModel() {
        return model;
    }
    
    public boolean wasModelBusted() {
        return wasModelBusted;
    }
    
    // Try to get the location from project using GenericItem
    public void restoreLocation() {
        // Dont think I want to store it there ?
        String locationProperty = (String) projectFileObject.getAttribute("NavigationPageLocation");
        if (locationProperty != null) {
            String[] values = locationProperty.split(":");
            try {
                setX(Integer.parseInt(values[0]));
                setY(Integer.parseInt(values[1]));
            } catch (Exception exc) {
                exc.printStackTrace();
            }
        } else {
            setX(-1);
            setY(-1);
        }
    }
    
    // Save the location to project using GenericItem
    public void saveLocation() {
        try {
            projectFileObject.setAttribute("NavigationPageLocation", x() + ":" + y());
        } catch (IOException e) {
        }
    }
    
    int num; // leftmost page is 0, page next to it 1, ....
    // Only for temporary/scratch use during layout computations
    int nextPortnum;
    // incident from:
    List pointedTo; // list of links pointing to this page
    // incident to:
    List pointsTo; // list of links originating at this page
    boolean used; // mark during searches to avoid circularity etc.
    
    int beanHeight; // When showing beans, use this height if > h()
    // this allows a component to have a large number of beans
    // where the page scales
    
    public String toString() {
        return "Page[" + getName() +","+x+","+y+","+width+","+height+","+num+
                //",pointsTo="+pointsTo+",pointedTo="+pointedTo+","+used+
                "]";
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public DataObject getDataObject() {
        return getDobj();
    }
    
    public void setDataObject(DataObject dob) {
        dobj = dob;
    }
    
    public int getBeanCount(){
        return getBeans().size();
    }
    
    public boolean hasBeans(){
        return !getBeans().isEmpty();
    }
    
    public NavigableComponent getBean(int index){
        return (NavigableComponent) getBeans().get(index);
    }
    
    public String getNameExt(){
        return getDobj().getPrimaryFile().getNameExt();
    }
    
    String getBeanClassName(int type) {
        String javaeePlatform = JsfProjectUtils.getJ2eePlatformVersion(getDoc().getProject());
        switch (type){
            case BUTTON:
                if ((javaeePlatform != null) && JsfProjectUtils.JAVA_EE_5.equals(javaeePlatform)){
                    return buttonClass_ws;
                }else{
                    return buttonClass_bh;
                }
            case HYPERLINK:
                if ((javaeePlatform != null) && JsfProjectUtils.JAVA_EE_5.equals(javaeePlatform)){
                    return hyperlinkClass_ws;
                }else{
                    return hyperlinkClass_bh;
                }
            case IMAGE_HYPERLINK:
                if ((javaeePlatform != null) && JsfProjectUtils.JAVA_EE_5.equals(javaeePlatform)){
                    return imageHyperlinkClass_ws;
                }else{
                    return imageHyperlinkClass_bh;
                }
        }
        return null;
    }

    protected NavigationModel getDoc() {
        return doc;
    }

    protected DataObject getDobj() {
        return dobj;
    }

    protected void setBeans(List<NavigableComponent> beans) {
        this.beans = beans;
    }

    public List getBeanLinks() {
        return beanLinks;
    }

    public void setBeanLinks(List beanLinks) {
        this.beanLinks = beanLinks;
    }

    protected Image getPreviewImage() {
        return previewImage;
    }

    protected void setPreviewImage(Image previewImage) {
        this.previewImage = previewImage;
    }

    protected Image getPreviewZoomedImage() {
        return previewZoomedImage;
    }

    protected void setPreviewZoomedImage(Image previewZoomedImage) {
        this.previewZoomedImage = previewZoomedImage;
    }
}
