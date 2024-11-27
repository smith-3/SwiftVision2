package com.stellaridea.swiftvision.data.sam.model

import com.stellaridea.swiftvision.data.mask.model.Mask

data class CreateProjectResponse(
    val id_project: Integer,
    val id_image: Integer,
    val masks: List<Mask>
)
