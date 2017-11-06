package renegade.features.selection

import java.util.*

class DiverseDoubleSelectionStrategy<SelectionType : Any> : SelectionStrategy<SelectionType> {
    private data class LowerHigherDistance(var lower : Double, var higher : Double)

    private val values : SortedMap<Double, LowerHigherDistance> = TreeMap()

    override fun report(selection: SelectionType) {

    }

    override fun choose(): SelectionType {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}