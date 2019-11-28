package renegade.supervised

import io.kotlintest.specs.FreeSpec
import renegade.datasets.bodyfat.loadBodyfatDataset

class VPPredictorSpec : FreeSpec() {
    init {
        "load bodyfat dataset" - {
            val bodyfat = loadBodyfatDataset()

            "build vpr" {
            }

        }
    }
}