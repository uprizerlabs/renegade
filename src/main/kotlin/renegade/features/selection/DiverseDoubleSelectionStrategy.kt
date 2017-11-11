package renegade.features.selection

import java.util.*

/**
 * Selects a double on the ring that is either closest or furthest from previously reported values.
 *
 * Furthest could be attempted - successes.  Closest could be successes-attempted.
 */
class DoubleSelectionStrategy(val type : SelectionStrategy.Type) : SelectionStrategy<Double> {

    private val values = TreeSet<Double>()

    override fun report(selection: Double) {
        values += selection
    }

    override fun choose(): Double {
        when (type) {
            SelectionStrategy.Type.NOVEL -> {
                var pval : Double? = null
                for (value in values) {
                    if (pval != null) {

                    }
                }
            }
            SelectionStrategy.Type.FAMILIAR -> {

            }
        }
        TODO()
    }
}