package com.stellaridea.swiftvision.data.image.model

data class Predict(
    var points: List<Pair<Int,Int>>,
    var labels: List<Int>
)
