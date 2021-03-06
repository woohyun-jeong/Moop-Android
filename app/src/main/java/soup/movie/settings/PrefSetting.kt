package soup.movie.settings

import android.content.SharedPreferences

import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import soup.movie.util.lazyFast

abstract class PrefSetting<T>(
    private val preferences: SharedPreferences
) : Setting<T> {

    private val settingSubject: BehaviorSubject<T> by lazyFast {
        BehaviorSubject.createDefault(getDefaultValue(preferences))
    }

    internal abstract fun getDefaultValue(preferences: SharedPreferences): T

    internal abstract fun saveValue(preferences: SharedPreferences, value: T)

    override fun set(value: T) {
        settingSubject.onNext(value)
        saveValue(preferences, value)
    }

    override fun get(): T = settingSubject.value ?: getDefaultValue(preferences)

    override fun asObservable(): Observable<T> = settingSubject
}
