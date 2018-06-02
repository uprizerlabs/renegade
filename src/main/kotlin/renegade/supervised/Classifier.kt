package renegade.supervised

import java.io.Serializable

interface Classifier<InputType : Any, OutputType : Any> : Serializable {

    fun predict(input : InputType) : Map<OutputType, Double>
}