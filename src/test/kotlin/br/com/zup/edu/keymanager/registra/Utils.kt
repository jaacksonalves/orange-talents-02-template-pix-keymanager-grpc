package br.com.zup.edu.keymanager.registra

import org.mockito.Mockito

class Utils {

    object MockitoHelper {
        fun <T> anyObject(): T {
            Mockito.any<T>()
            return uninitialized()
        }
        @Suppress("UNCHECKED_CAST")
        fun <T> uninitialized(): T =  null as T
    }

}