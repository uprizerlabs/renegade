package renegade.supervised.vp

import io.kotlintest.specs.FreeSpec
import renegade.datasets.bodyfat.loadBodyfatDataset
import renegade.opt.OptConfig

class VPPredictorSpec : FreeSpec() {
    init {
        "load bodyfat dataset" - {
            val bodyfat = loadBodyfatDataset()

            "build vpr" {
            }

        }
    }
}