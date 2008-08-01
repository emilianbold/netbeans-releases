package fumethod;

public class User {
    
    /**
     * user of method
     * 
     * @param t
     */
    public void use(Test t) {
        t.method();
        new Test().method();
        ((Test)new Object()).method();
        Iface i = null;
        i.method();        
    }

}
