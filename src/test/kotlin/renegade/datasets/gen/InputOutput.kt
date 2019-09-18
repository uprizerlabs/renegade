package renegade.datasets.gen

data class InputOutput<out T : Any>(val input : List<Double>, val classification: T)