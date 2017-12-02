package renegade.output

import renegade.aggregators.Weighted

abstract class OutputProcessor<ItemType>() {
    operator abstract fun invoke(a : ItemType) : Weighted<ItemType>
}