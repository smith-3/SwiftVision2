package com.stellaridea.swiftvision.models.masks

data class Predict(
    var points: List<Pair<Int,Int>>,
    var labels: List<Int>
)
