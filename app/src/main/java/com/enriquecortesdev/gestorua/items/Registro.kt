package com.enriquecortesdev.gestorua.items

data class Registro (
    val id: String,
    val aula: String,
    val fecha: String,
    val horaini: String,
    val horafin: String,
    val profesor: String,
    val activo: Boolean)