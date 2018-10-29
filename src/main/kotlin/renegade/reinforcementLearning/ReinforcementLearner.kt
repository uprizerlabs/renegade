package renegade.reinforcementLearning

interface ReinforcementLearner<OptionsType> {
    fun choose(context : Map<String, Any>, options : List<OptionsType>) : OptionsType

    fun reward(value : Double)
}
