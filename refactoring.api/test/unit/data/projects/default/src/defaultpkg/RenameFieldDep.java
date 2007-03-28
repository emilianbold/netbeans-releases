
package defaultpkg;

public class RenameFieldDep extends RenameField {
    
    /** Creates a new instance of RenameFieldDep */
    public RenameFieldDep() {
        super.field = 3;
        
        new RenameField().field = 1;
    }
    
}
