// "Create property 'foo'" "true"
// ERROR: Property must be initialized or be abstract

class A<T>(val n: T) {
    class object {

    }
}

fun test() {
    val a: Int = A.<caret>foo
}
