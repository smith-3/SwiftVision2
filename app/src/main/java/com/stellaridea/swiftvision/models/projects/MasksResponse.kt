package com.stellaridea.swiftvision.models.projects

import com.stellaridea.swiftvision.models.masks.Mask

data class CreateProjectResponse(
    val idProject: Int,
    val idImage: Int,
    val masks: List<Mask>
)
