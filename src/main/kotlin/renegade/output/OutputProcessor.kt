package renegade.output

import renegade.aggregators.ItemWithDistance

abstract class OutputProcessor<ItemType>() {
    operator abstract fun invoke(a : ItemType) : ItemWithDistance<ItemType>
}