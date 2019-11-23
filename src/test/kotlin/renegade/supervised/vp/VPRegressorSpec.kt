package renegade.supervised.vp

import io.kotlintest.specs.FreeSpec
import renegade.datasets.bodyfat.loadBodyfatDataset
import renegade.opt.OptConfig

class VPRegressorSpec : FreeSpec() {
    init {
        "load bodyfat dataset" - {
            val bodyfat = loadBodyfatDataset()

            "build vpr" {
                val vpr = VPRegressor(OptConfig(), bodyfat.data, bodyfat.createBuilders())
                vpr.insetSize
            }

        }
    }
}