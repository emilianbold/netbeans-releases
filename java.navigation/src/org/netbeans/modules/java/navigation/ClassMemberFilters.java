/*
 * ClassMemberFilters.java
 *
 * Created on November 9, 2006, 5:40 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.java.navigation;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JToggleButton;
import org.netbeans.modules.java.navigation.ElementNode.Description;
import org.netbeans.modules.java.navigation.actions.SortActionSupport;
import org.netbeans.modules.java.navigation.base.FiltersDescription;
import org.netbeans.modules.java.navigation.base.FiltersManager;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;

/** Creates filtering for the ClassMemberPanel
 *
 * @author phrebejk
 */
public final class ClassMemberFilters {
    
    private ClassMemberPanelUI ui;
    
    /** constants for defined filters */
    private static final String SHOW_NON_PUBLIC = "show_non_public";
    private static final String SHOW_STATIC = "show_static";
    private static final String SHOW_FIELDS = "show_fields";
    private static final String SHOW_INHERITED = "show_inherited";
    
    private static final String SORT_ALPHA = "sort_alpha";
    private static final String SORT_POSITION = "sort_position";
    
    private FiltersManager filters;
    
    private boolean naturalSort = false;
    
    /** Creates a new instance of ClassMemberFilters */
    ClassMemberFilters( ClassMemberPanelUI ui ) {
        this.ui = ui;
        naturalSort = NbPreferences.forModule( ClassMemberFilters.class ).getBoolean( "naturalSort", false ); //NOI18N
    }
    
    public FiltersManager getInstance() {
        if (filters == null) {
            filters = createFilters();
        }
        return filters;
    }
    
    public JComponent getComponent() {        
        FiltersManager f = getInstance();                        
        return f.getComponent( createSortButtons() );
        
    }
    
    public Collection<Description> filter( Collection<Description> original ) {
        
        boolean non_public = filters.isSelected(SHOW_NON_PUBLIC);
        boolean statik = filters.isSelected(SHOW_STATIC);
        boolean fields = filters.isSelected(SHOW_FIELDS);
        boolean inherited = filters.isSelected(SHOW_INHERITED);
        
        ArrayList<Description> result = new ArrayList<Description>(original.size());
        for (Description description : original) {
            
            if ( !inherited && description.isInherited ) {
                continue;
            }
            if ( !non_public && 
                 !description.modifiers.contains(Modifier.PUBLIC)                 
                 /* Fix for #89777 && !description.modifiers.contains(Modifier.PROTECTED) */ ) {
                continue;
            }
            
            if ( !statik && description.modifiers.contains(Modifier.STATIC)) {
                continue;
            }
            
            if ( !fields && description.kind == ElementKind.FIELD ) {
                continue;
            }
            
            // XXX Inherited members
            
            result.add(description);                        
        }
                
        Collections.sort( result, isNaturalSort() ?  Description.POSITION_COMPARATOR : Description.ALPHA_COMPARATOR );
        
        return result;
    }
    
    public boolean isNaturalSort() {
        return naturalSort;        
    }
    
    public void setNaturalSort( boolean naturalSort ) {
        this.naturalSort = naturalSort;
        NbPreferences.forModule( ClassMemberFilters.class ).putBoolean( "naturalSort", naturalSort ); //NOI18N
        if( null != sortByNameButton )
            sortByNameButton.setSelected(!naturalSort);
        if( null != sortByPositionButton )
            sortByPositionButton.setSelected(naturalSort);
        ui.sort();
    }
    
    // Privare methods ---------------------------------------------------------
    
    /** Creates filter descriptions and filters itself */
    private static FiltersManager createFilters () {
        FiltersDescription desc = new FiltersDescription();
        
        desc.addFilter(SHOW_INHERITED,
                NbBundle.getMessage(ClassMemberFilters.class, "LBL_ShowInherited"),     //NOI18N
                NbBundle.getMessage(ClassMemberFilters.class, "LBL_ShowInheritedTip"),     //NOI18N
                false,
                new ImageIcon (ImageUtilities.loadImage("org/netbeans/modules/java/navigation/resources/filterHideInherited.png")), //NOI18N
                null
        );
        desc.addFilter(SHOW_FIELDS,
                NbBundle.getMessage(ClassMemberFilters.class, "LBL_ShowFields"),     //NOI18N
                NbBundle.getMessage(ClassMemberFilters.class, "LBL_ShowFieldsTip"),     //NOI18N
                true,
                new ImageIcon (ImageUtilities.loadImage("org/netbeans/modules/java/navigation/resources/filterHideFields.gif")), //NOI18N
                null
        );
        desc.addFilter(SHOW_STATIC,
                NbBundle.getMessage(ClassMemberFilters.class, "LBL_ShowStatic"),     //NOI18N
                NbBundle.getMessage(ClassMemberFilters.class, "LBL_ShowStaticTip"),     //NOI18N
                true,
                new ImageIcon (ImageUtilities.loadImage("org/netbeans/modules/java/navigation/resources/filterHideStatic.png")), //NOI18N
                null
        );
        desc.addFilter(SHOW_NON_PUBLIC,
                NbBundle.getMessage(ClassMemberFilters.class, "LBL_ShowNonPublic"),     //NOI18N
                NbBundle.getMessage(ClassMemberFilters.class, "LBL_ShowNonPublicTip"),     //NOI18N
                true,
                new ImageIcon (ImageUtilities.loadImage("org/netbeans/modules/java/navigation/resources/filterHideNonPublic.png")), //NOI18N
                null
        );
        
        return FiltersDescription.createManager(desc);
    }
    
    private JToggleButton sortByNameButton;
    private JToggleButton sortByPositionButton;
    
    private JToggleButton[] createSortButtons() {
        JToggleButton[] res = new JToggleButton[2];
        
        if( null == sortByNameButton ) {
            sortByNameButton = new JToggleButton( new SortActionSupport.SortByNameAction(this) );
            sortByNameButton.setToolTipText(sortByNameButton.getText());
            sortByNameButton.setText(null);
            sortByNameButton.setSelected( !naturalSort );
            sortByNameButton.setFocusable( false );
        }
        res[0] = sortByNameButton;
        
        if( null == sortByPositionButton ) {
            sortByPositionButton = new JToggleButton( new SortActionSupport.SortBySourceAction(this) );
            sortByPositionButton.setToolTipText(sortByPositionButton.getText());
            sortByPositionButton.setText(null);
            sortByPositionButton.setSelected( naturalSort );
            sortByPositionButton.setFocusable( false );
        }
        res[1] = sortByPositionButton;
        return res;
    }
        
}
