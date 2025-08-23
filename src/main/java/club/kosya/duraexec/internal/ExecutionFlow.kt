package club.kosya.duraexec.internal

class ExecutionFlow(
    val id: String,
) {
    val actions = mutableListOf<ExecutedAction>()
}

class ExecutedAction(
    val id: String,
) {
    val childActions = mutableListOf<ExecutedAction>()
}
