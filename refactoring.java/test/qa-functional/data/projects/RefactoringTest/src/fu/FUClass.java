/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fu;

/**
 * 
 * @author jp159440
 */
public class FUClass {
    FUClass field;
    
    public FUClass retunType(FUClass param) {
        return null;
    }
    
    java.util.List<FUClass> list;
    
    class Inner<T extends FUClass> {
        
    }
    
    public void method() {
        this.<FUClass>generics(this);
    }
    
    public <T extends FUClass> T generics(T in) {
        return in;
    }
}
