/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fu;

/**
 * 
 * @author jp159440
 */
public class FindUsagesClass {
    FindUsagesClass field;
    
    public FindUsagesClass retunType(FindUsagesClass param) {
        return null;
    }
    
    java.util.List<FindUsagesClass> list;
    
    class Inner<T extends FindUsagesClass> {
        
    }
    
    public void method() {
        this.<FindUsagesClass>generics(this);
    }
    
    public <T extends FindUsagesClass> T generics(T in) {
        return in;
    }
}
