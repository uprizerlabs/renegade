package renegade.distanceModelBuilder.inputTypes.metric

import org.apache.commons.text.similarity.LevenshteinDistance
import java.lang.Math.max

/**
 * Created by ian on 7/11/17.
 */
class StringEditDistanceMetricModelBuilder(label : String? = null) : MetricDistanceModelBuilder<String>(label, { (a, b) ->
    LevenshteinDistance
            .getDefaultInstance()
            .apply(a, b).toDouble() / max(a.length, b.length)
})