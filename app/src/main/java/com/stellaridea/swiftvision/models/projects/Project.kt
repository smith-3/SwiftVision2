package com.stellaridea.swiftvision.models.projects

import com.stellaridea.swiftvision.models.images.Image

data class Project(
    val id: Int,
    val name: String,
    val images: List<Image> = emptyList(),
)
