package a

class SpreadJavaFieldOperator {

    def test() {
        ["abc", "def"]*.@b
    }
}
