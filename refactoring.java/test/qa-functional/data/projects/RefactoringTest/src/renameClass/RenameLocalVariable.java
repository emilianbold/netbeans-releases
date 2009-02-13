package renameClass;

public class RenameLocalVariable {

    public int test() {
        int local = 3;
        System.out.println(local);
        if(local<local) {
            System.out.println(local);
        }
        return local;
    }

}
