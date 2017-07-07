package dnn.search.destinationSampling

import dnn.search.MetricSpaceIndex.Result

class Result<out ItemType : Any>(override val item: ItemType, override val distance: Double, val toRemove : () -> Unit) : Result<ItemType> {
    override fun remove() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}