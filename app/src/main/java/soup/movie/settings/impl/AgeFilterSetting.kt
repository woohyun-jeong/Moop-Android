package soup.movie.settings.impl

import android.content.SharedPreferences
import soup.movie.settings.PrefSetting
import soup.movie.settings.model.AgeFilter

class AgeFilterSetting(
    preferences: SharedPreferences
) : PrefSetting<AgeFilter>(preferences) {

    override fun getDefaultValue(preferences: SharedPreferences): AgeFilter {
        return AgeFilter(preferences.getInt(KEY, DEFAULT_VALUE))
    }

    override fun saveValue(preferences: SharedPreferences, value: AgeFilter) {
        preferences.edit().putInt(KEY, value.toFlags()).apply()
    }

    companion object {

        private const val KEY = "age_filter"
        private const val DEFAULT_VALUE: Int = AgeFilter.FLAG_AGE_DEFAULT
    }
}
