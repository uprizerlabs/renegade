package renegade.features.selection

interface SelectionStrategy<SelectionType : Any> {
    fun report(selection : SelectionType)

    fun choose() : SelectionType
}