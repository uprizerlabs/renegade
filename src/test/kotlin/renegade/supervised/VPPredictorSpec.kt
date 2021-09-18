package renegade.supervised

import io.kotest.core.spec.style.FreeSpec
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