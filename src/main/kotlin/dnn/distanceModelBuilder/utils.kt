package dnn.distanceModelBuilder

/**
 * Created by ian on 7/8/17.
 */


fun <InputType : Any> List<DistanceModel<InputType>>.wrap() = DistanceModelList(this)
