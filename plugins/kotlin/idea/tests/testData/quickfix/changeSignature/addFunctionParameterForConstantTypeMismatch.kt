// "Add 1st parameter to function 'foo'" "true"

fun foo(name: String) = Unit

fun test() {
    val foo = foo(<caret>1, "name")
}
// FUS_QUICKFIX_NAME: org.jetbrains.kotlin.idea.quickfix.AddFunctionParametersFix
// FUS_K2_QUICKFIX_NAME: org.jetbrains.kotlin.idea.k2.refactoring.changeSignature.quickFix.ChangeSignatureFixFactory$ParameterQuickFix