package com.jpardogo.lce

import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PRIVATE
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

fun MutableLceLiveDataCompletable.asImmutableLiveData() = this as LceLiveData<Unit>
fun <C> MutableLceLiveData<C>.asImmutableLiveData() = this as LceLiveData<C>
fun <C, E> MutableLceLiveData2<C, E>.asImmutableLiveData() = this as LceLiveData2<C, E>
fun <L, C, E> MutableLceLiveData3<L, C, E>.asImmutableLiveData() = this as LceLiveData3<L, C, E>

class MutableLceLiveDataCompletable : LceLiveData<Unit>() {

    fun isLoading(isLoading: Boolean, threadStrategy: LiveDataThreadStrategy = DefaultThread) {
        super.loading(isLoading, threadStrategy)
    }

    fun complete(threadStrategy: LiveDataThreadStrategy = DefaultThread) {
        super.complete(Unit, threadStrategy)
    }

    public override fun error(error: LceErrorViewEntity, threadStrategy: LiveDataThreadStrategy) {
        super.error(error, threadStrategy)
    }
}

class MutableLceLiveData<C> : LceLiveData<C>() {

    fun isLoading(isLoading: Boolean, threadStrategy: LiveDataThreadStrategy = DefaultThread) {
        super.loading(isLoading, threadStrategy)
    }

    public override fun content(content: C, threadStrategy: LiveDataThreadStrategy) {
        super.content(content, threadStrategy)
    }

    public override fun error(error: LceErrorViewEntity, threadStrategy: LiveDataThreadStrategy) {
        super.error(error, threadStrategy)
    }
}

class MutableLceLiveData2<C, E> : LceLiveData2<C, E>() {
    fun isLoading(isLoading: Boolean, threadStrategy: LiveDataThreadStrategy = DefaultThread) {
        super.loading(isLoading, threadStrategy)
    }

    public override fun content(content: C, threadStrategy: LiveDataThreadStrategy) {
        super.content(content, threadStrategy)
    }

    public override fun error(error: E, threadStrategy: LiveDataThreadStrategy) {
        super.error(error, threadStrategy)
    }
}

class MutableLceLiveData3<L, C, E> : LceLiveData3<L, C, E>() {

    fun isLoading(isLoading: L, threadStrategy: LiveDataThreadStrategy = DefaultThread) {
        super.loading(isLoading, threadStrategy)
    }

    public override fun content(content: C, threadStrategy: LiveDataThreadStrategy) {
        super.content(content, threadStrategy)
    }

    public override fun error(error: E, threadStrategy: LiveDataThreadStrategy) {
        super.error(error, threadStrategy)
    }
}

open class LceLiveData<C> : LceLiveData2<C, LceErrorViewEntity>()
open class LceLiveData2<C, E> : LceLiveData3<Boolean, C, E>()
open class LceLiveData3<L, C, E>(
    @VisibleForTesting(otherwise = PRIVATE) val loadingLiveData: MutableLiveData<L> = MutableLiveData(),
    @VisibleForTesting(otherwise = PRIVATE) val contentLiveData: MutableLiveData<C> = MutableLiveData(),
    @VisibleForTesting(otherwise = PRIVATE) val errorLiveData: MutableLiveData<E> = MutableLiveData()
) {

    /**
     * FIXME https://youtrack.jetbrains.com/issue/KT-31946
     * = DefaultThread cannot be added as a default parameter until the above kotlin bug is resolved
     *
     */
    protected open fun loading(isLoading: L, threadStrategy: LiveDataThreadStrategy) {
        send(loadingLiveData, isLoading, threadStrategy)
    }

    protected open fun content(content: C, threadStrategy: LiveDataThreadStrategy = DefaultThread) {
        send(this.contentLiveData, content, threadStrategy)
    }

    protected open fun error(error: E, threadStrategy: LiveDataThreadStrategy = DefaultThread) {
        send(this.errorLiveData, error, threadStrategy)
    }

    protected open fun complete(
        completeEvent: C,
        threadStrategy: LiveDataThreadStrategy = DefaultThread
    ) {
        send(contentLiveData, completeEvent, threadStrategy)
    }

    protected open fun <T> send(
        liveData: MutableLiveData<T>,
        data: T?,
        threadStrategy: LiveDataThreadStrategy = DefaultThread
    ) {
        liveData.run {
            when (threadStrategy) {
                is ForceMainThread -> postValue(data)
                is DefaultThread -> value = data
            }
        }
    }

    fun observeLce(
        lifecycleOwner: LifecycleOwner,
        onContent: (C) -> Unit,
        onError: (E) -> Unit,
        onLoading: ((L) -> Unit)? = null
    ) {
        contentLiveData.observe(lifecycleOwner, Observer { content ->
            onContent(content)
        })

        errorLiveData.observe(lifecycleOwner, Observer { error ->
            onError(error)
        })

        onLoading?.let {
            loadingLiveData.observe(lifecycleOwner, Observer { loadingLiveData ->
                it(loadingLiveData)
            })
        }
    }
}

sealed class LiveDataThreadStrategy
object ForceMainThread : LiveDataThreadStrategy()
object DefaultThread : LiveDataThreadStrategy()