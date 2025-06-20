package com.joaomgcd.taskerpluginlibrary

sealed class SimpleResult<TSuccess, TError> {
    class Success<TSuccess, TError>(val value: TSuccess) : SimpleResult<TSuccess, TError>()
    class Error<TSuccess, TError>(val value: TError) : SimpleResult<TSuccess, TError>()

    val successValueOrNull get() = if (this is Success) value else null
    val isSuccess get() = this is Success
    val errorValueOrNull get() = if (this is Error) value else null
}
