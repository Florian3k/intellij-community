// "Create parameter 'foo'" "false"
// ACTION: Convert to expression body
// ACTION: Create local variable 'foo'
// ACTION: Create property 'foo'
// ERROR: Unresolved reference: foo

object A {
    val test: Int get() {
        return <caret>foo
    }
}