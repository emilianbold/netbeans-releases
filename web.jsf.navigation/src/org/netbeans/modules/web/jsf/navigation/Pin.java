/*
 * Pin.java
 *
 * Created on January 29, 2007, 10:28 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.web.jsf.navigation;

/**
 *
 * @author joelle
 */
public class Pin {
    
    private Page page;
    private NavigableComponent navComp;
    private boolean inFragment;
        
    
    /** Creates a default Page Pin */
    public Pin( Page fromPage) {
        this.page = fromPage;
        navComp = null;
        inFragment = false;
    }
    
    public Pin( Page page, NavigableComponent navComp) {
        this.page = page;
        this.navComp = navComp;
        inFragment = false;
    }
    
    public Pin( Page page, NavigableComponent navComp, boolean inFragment) {
            this.page = page;
            this.navComp  = navComp;
            this.inFragment = inFragment;
    }
    
    public boolean equals( Pin pin ) {
        if ( pin == null ) {
            return false;
        }
        if ( pin.getPage() != page ){
            return false;
        }
        if ( pin.getNavComp() != navComp) {
            return false;
        }
        if (pin.isInFragment() != inFragment ){
            return false;
        }
        return true;
    }

    public Page getPage() {
        return page;
    }

    public void getPage(Page fromPage) {
        this.page = fromPage;
    }

    public NavigableComponent getNavComp() {
        return navComp;
    }

    public void setNavComp(NavigableComponent navComp) {
        this.navComp = navComp;
    }

    public boolean isInFragment() {
        return inFragment;
    }

    public void setInFragment(boolean inFragment) {
        this.inFragment = inFragment;
    }
            
    
}
