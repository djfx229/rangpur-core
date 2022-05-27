package io.github.iamfacetheflames.rangpur.core.repository

interface Configuration {

    fun getMusicDirectoryLocation(): String
    fun getSyncHost(): String
    fun getSyncPort(): Int

}