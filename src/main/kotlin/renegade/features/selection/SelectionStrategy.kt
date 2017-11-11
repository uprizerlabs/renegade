package renegade.features.selection

interface SelectionStrategy<SelectionType : Any> {
    fun report(selection : SelectionType)

    fun choose() : SelectionType

    enum class Type {
        NOVEL, FAMILIAR
    }
}

