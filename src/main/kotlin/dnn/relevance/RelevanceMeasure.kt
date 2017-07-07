package dnn.relevance

import com.google.common.collect.Multimap
import dnn.util.Two

abstract class RelevanceMeasure<T>(val sampling : Multimap<Two<T>, Double>) {
    abstract val rmse: Double

    abstract fun relevance(values : Two<T>) : Double
}