package com.jci2027.lead360.model

import com.google.gson.annotations.SerializedName

data class TemplateModel(
    val id: String,
    val title: String,
    val category: String?,
    val baseImageUrl: String,
    val dimensions: TemplateDimensions?,
    val layers: List<TemplateLayer>?
)

data class TemplateDimensions(
    val width: Int = 1080,
    val height: Int = 1080
)

data class TemplateLayer(
    val id: String?,
    val type: String?,
    val key: String?,
    val label: String?,
    val x: Float = 0f,
    val y: Float = 0f,
    val width: Float = 100f,
    val height: Float = 100f,
    val xPercent: Float = 0f,
    val yPercent: Float = 0f,
    val wPercent: Float = 20f,
    val hPercent: Float = 10f,
    val textColor: String? = "#ffffff",
    val textAlign: String? = "left",
    val fontSize: Int? = 24,
    val fontWeight: String? = "700",
    val hasShadow: Boolean? = true,
    val shape: String? = "rectangle",
    val borderWidth: Int? = 0,
    val borderColor: String? = "#ffffff"
)

data class TemplatesResponse(
    val success: Boolean,
    val count: Int,
    val templates: List<TemplateModel>
)
