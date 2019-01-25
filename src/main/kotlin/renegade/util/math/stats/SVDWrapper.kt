package renegade.util.math.stats

import org.apache.commons.lang3.ArrayUtils
import org.apache.commons.math3.linear.*
import java.util.*

/**
 * Wraps an SVD object and provide some useful utility methods
 */
class SVDWrapper(trainingData: List<List<Double>>) {

    val svd: SingularValueDecomposition
    private val w: RealMatrix

    init {
        val inputArray = Array2DRowRealMatrix(NArrays.listsToArray(trainingData))
        this.svd = SingularValueDecomposition(inputArray)
        this.w = svd.v.multiply(invertedSigma())
    }

    private fun invertedSigma(): RealMatrix {
        val sigma = svd.s

        val inverted = OpenMapRealMatrix(sigma.rowDimension, sigma.columnDimension)

        for (x in 0 until sigma.rowDimension) {
            inverted.setEntry(x, x, 1.0 / sigma.getEntry(x, x))
        }

        return inverted
    }

    fun transformVector(input: List<Double>): List<Double> {
        val inputAsArray = Array2DRowRealMatrix(NArrays.listsToArray(listOf(input)))
        val outputMatrix = inputAsArray.multiply(w)
        assert(outputMatrix.getRowDimension() == 1)
        val outputArray = outputMatrix.getRow(0)
        return Arrays.asList(*ArrayUtils.toObject(outputArray))
    }

}

object NArrays {
    fun listsToArray(input: List<List<Double>>): Array<DoubleArray> =
            input.map { a -> DoubleArray(a.size) {a[it]} }.toTypedArray()


    /*{
        val ret = arrayOfNulls<DoubleArray>(input.size)
        var ix = 0
        for (row in input) {
            ret[ix] = row.stream().mapToDouble { x -> x }.toArray()
            ix++
        }
        return ret
    }*/
}
