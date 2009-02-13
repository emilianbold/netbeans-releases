package renameClass;

public class RenameMethod {

    public int  test() {
        return 1;
    }

    public int usage() {
        test();
        new Integer(test());
        return test();
    }

    {
        test();
    }

    int x = test();

}
