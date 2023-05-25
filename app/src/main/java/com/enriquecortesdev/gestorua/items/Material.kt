package com.enriquecortesdev.gestorua.items

data class Material (
    val id: String,
    val aula: String,
    val tipo: String,
    val disponible: Boolean,
    var asignado: Boolean,
    val actual: String)