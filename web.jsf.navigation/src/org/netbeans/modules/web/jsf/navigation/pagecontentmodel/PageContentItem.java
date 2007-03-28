/*
 * PageContentItem.java
 *
 * Created on March 27, 2007, 5:33 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.web.jsf.navigation.pagecontentmodel;

import java.awt.Image;
import java.util.List;
import javax.swing.Action;

/**
 *
 * @author joelle
 */
public abstract class PageContentItem {
    
    private Image icon;
    private String fromAction;
    private String fromOutcome;
    private String name;
    
    
    /** Creates a new instance of PageContentItem */
    private PageContentItem() {
    }
    
    public abstract List<Action> getActions();
    
    public PageContentItem( String name, String fromAction, Image icon ) {
        this.name = name;
        this.fromAction = fromAction;
        this.icon = icon;
    }   
    
    public PageContentItem( String name, String fromString, Image icon, boolean isOutcome ) {
        this.name = name;
        if( isOutcome )
            this.fromOutcome = fromString;
        else 
            this.fromAction = fromString;
        this.icon = icon;
    }

    
    public Image getIcon() {
        return icon;
    }
    
    public void setIcon(Image icon) {
        this.icon = icon;
    }
    
    public String getFromAction() {
        return fromAction;
    }
    
    public void setFromAction(String fromAction) {
        this.fromAction = fromAction;
    }
    
    public String getFromOutcome() {
        return fromOutcome;
    }
    
    public void setFromOutcome(String fromOutcome) {
        this.fromOutcome = fromOutcome;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    

    
}
