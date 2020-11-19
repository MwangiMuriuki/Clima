package com.dev.clima.DataClasses

import androidx.fragment.app.Fragment
import java.util.*

data class NavigationDrawerDataClass(
    var item_name: String? = null,
    var image_resource: Int = 0,
    var image_url: String? = null,
    var activity: Boolean = false,
    var fragment: Boolean = false,
    var activityName: Class<*>? = null,
    var fragmentName: Fragment? = null,
    var subMenus: List<NavigationDrawerDataClass?> = ArrayList<NavigationDrawerDataClass?>()
){
}