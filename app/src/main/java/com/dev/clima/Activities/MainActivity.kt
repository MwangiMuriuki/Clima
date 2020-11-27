package com.dev.clima.Activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.ui.AppBarConfiguration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dev.clima.Adapters.NavigationDrawerAdapter
import com.dev.clima.DataClasses.NavigationDrawerDataClass
import com.dev.clima.DataClasses.UserDetailsDataClass
import com.dev.clima.Fragments.FragmentHome
import com.dev.clima.Fragments.FragmentProfile
import com.dev.clima.R
import com.dev.clima.Utilities.PreferenceManager
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private var navDrawerAdapter: NavigationDrawerAdapter? = null
    var recyclerView: RecyclerView? = null
    var toggle: ActionBarDrawerToggle? = null
    private var mAuth = FirebaseAuth.getInstance()
    var firebaseFirestore = FirebaseFirestore.getInstance()
    var userLoggedIn = mAuth.currentUser
    var preferenceManager: PreferenceManager? = null
    var currentUser: String? = null
    var currentUserID: String? = null

    var toolBarText: TextView? = null

    val myList: MutableList<UserDetailsDataClass> = ArrayList<UserDetailsDataClass>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        toolBarText = toolbar.findViewById<TextView>(R.id.toolbar_title)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        preferenceManager = PreferenceManager(applicationContext)

        val creditBalance: String? = preferenceManager!!.getCreditBalance()

        if (creditBalance.isNullOrEmpty()){
            credBalance.text = "0.00"
        }else{
            credBalance.text = creditBalance
        }

        recyclerView = findViewById(R.id.navItemslist)

        mAuth = FirebaseAuth.getInstance()
        userLoggedIn = mAuth.currentUser

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)

         toggle = ActionBarDrawerToggle(
             this,
             drawerLayout,
             toolbar,
             R.string.navigation_drawer_open,
             R.string.navigation_drawer_close
         )
        drawerLayout.addDrawerListener(toggle!!)
        toggle!!.syncState()

        showMainFragment()
        setupNavDrawerMenu()

        //Fetch user details from FirebaseFirestore
        if (userLoggedIn != null) {
            val userID: String = userLoggedIn!!.uid

            firebaseFirestore.collection("Users").document(userID).get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val documentSnapshot = task.result
                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            val userData = UserDetailsDataClass(
                                documentSnapshot.getString("full_Name"),
                                documentSnapshot.getString("email"),
                                documentSnapshot.getString("user_id"),
                                documentSnapshot.getString("display_picture")
                            )
                            myList.add(userData)

                            val fname = documentSnapshot.getString("full_Name")
                            val userID = documentSnapshot.getString("user_id")
                            val pic = documentSnapshot.getString("display_picture")
                            profileUserName.text = fname
                            currentUser = fname
                            currentUserID = userID
                            preferenceManager?.setFullName(fname)
                            preferenceManager?.setUserId(userID)

                            if (pic != null) {
                                val imageUri = Uri.parse(documentSnapshot.getString("display_picture"))
                                Glide.with(this).load(imageUri).into(userProfilePicture)
                            }

                            getAllScans(currentUser, currentUserID)
                        }
                        else {
                            Log.e("SNAPSHOT_ERROR", "User Does not exist")
                        }
                    }
                    else {

                        Log.e("TASK_ERROR", task.exception?.message.toString())
                    }
                }
                .addOnFailureListener {
                    Log.e("TASK_ERROR", it.message.toString())

                }
        }

        //User Logout
        logout.setOnClickListener {
            val logout = Intent(applicationContext, ActivityLogin::class.java)
            mAuth.signOut()
            preferenceManager?.setFullName("")
            preferenceManager?.setCreditBalance("")
            preferenceManager?.setTotalScans("")
            startActivity(logout)
            finish()
        }

    }

     private fun getAllScans(currentUser: String?, currentUserID: String?) {
         //currentUser = preferenceManager?.getFullName()

        firebaseFirestore.collection("Scanned Plastics")
                .document(currentUserID!!)
                .collection("Barcodes")
                .get()
                .addOnCompleteListener {
                    if (it.isSuccessful){
                        val itemNumber: String? = it.result!!.size().toString()
                        val creditVal: Double? = 0.25
                        val prod = it.result!!.size() * creditVal!!
                        credBalance.text = prod.toString()

                        preferenceManager?.setCreditBalance(prod.toString())
                        preferenceManager?.setTotalScans(itemNumber)

                    }
                    else{

                        Toast.makeText(
                                this,
                                "Error Getting info:" + it.exception?.message.toString(),
                                Toast.LENGTH_LONG
                        ).show()
                        Log.e("article_ERROR_TAG", it.exception?.message.toString())
                    }
                }
    }

    fun setActionBarTitle(title: String?) {
        toolBarText?.text = title
    }
    /*SET UP DASHBOARD AS DEFAULT SREEN*/
    private fun showMainFragment() {
        val fragment: Fragment = FragmentHome()
        val fragmentManager = supportFragmentManager
        fragmentManager.beginTransaction()
            .replace(R.id.content_main, fragment)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE) //.addToBackStack(getString(R.string.revenue_fragment))
            .commit()
    }

    /*ADD NAVIGATION DRAWER ITEMS*/
    private fun setupNavDrawerMenu() {
        val navDrawerItems: MutableList<NavigationDrawerDataClass> = ArrayList<NavigationDrawerDataClass>()

        /*DASHBOARD*/
        val dashboard = NavigationDrawerDataClass()
        dashboard.item_name = (getString(R.string.dashboard))
        dashboard.fragment = true
        dashboard.fragmentName = FragmentHome()
        dashboard.image_resource = (R.drawable.dashboard_icon)
        navDrawerItems.add(dashboard)

        /*PROFILE*/
        val myProfile = NavigationDrawerDataClass()
        myProfile.item_name = (getString(R.string.profile))
        myProfile.fragment = true
        myProfile.fragmentName = FragmentProfile()
        myProfile.image_resource = (R.drawable.profile_icon)
        navDrawerItems.add(myProfile)

        /*MY TASKS*/
//        val myTasks = NavigationDrawerDataClass()
//        myTasks.item_name = (getString(R.string.tasks))
//        myTasks.fragment = true
//        myTasks.fragmentName = FragmentMyTasks()
//        myTasks.image_resource = (R.drawable.tasks_icon)
//        navDrawerItems.add(myTasks)

        /*MY SCANS*/
        val myscans= NavigationDrawerDataClass()
        myscans.item_name = (getString(R.string.scans))
        myscans.activity = true
        myscans.activityName = ActivityMyScans::class.java
        myscans.image_resource = (R.drawable.tasks_icon)
        navDrawerItems.add(myscans)

        /*SCANNER*/
//        val myScanner = NavigationDrawerDataClass()
//        myScanner.item_name = (getString(R.string.scanner))
//        myScanner.fragment = true
//        myScanner.fragmentName = FragmentScanner()
//        myScanner.image_resource = (R.drawable.bar_code_icon)
//        navDrawerItems.add(myScanner)

        /*SCANNER*/
        val scanner = NavigationDrawerDataClass()
        scanner.item_name = (getString(R.string.scanner))
        scanner.activity = true
        scanner.activityName = ActivityScanner::class.java
        scanner.image_resource = (R.drawable.bar_code_icon)
        navDrawerItems.add(scanner)

        val layoutManager = LinearLayoutManager(this)

        recyclerView?.layoutManager = layoutManager
        recyclerView?.adapter = NavigationDrawerAdapter(this@MainActivity, navDrawerItems,
            object : NavigationDrawerAdapter.NavigationDrawerListener {
                override fun onNavMenuItemSelected(navDrawerDataClass: NavigationDrawerDataClass?) {
                    if (navDrawerDataClass != null) {
                        val fragment: Fragment
                        /*DASHBOARD*/
                        if (navDrawerDataClass.item_name.equals(getString(R.string.dashboard))) {
                            fragment = navDrawerDataClass.fragmentName!!
                            val ft = supportFragmentManager.beginTransaction()
                            ft.replace(R.id.content_main, fragment)
                            ft.commit()
                        } else if (navDrawerDataClass.item_name.equals(getString(R.string.profile))) {
                            fragment = navDrawerDataClass.fragmentName!!
                            val ft = supportFragmentManager.beginTransaction()
                            ft.replace(R.id.content_main, fragment)
                            ft.commit()
//                        } else if (navDrawerDataClass.item_name.equals(getString(R.string.tasks))) {
//                            fragment = navDrawerDataClass.fragmentName!!
//                            val ft = supportFragmentManager.beginTransaction()
//                            ft.replace(R.id.content_main, fragment)
//                            ft.commit()
                        }
                        else if (navDrawerDataClass.item_name.equals(getString(R.string.scanner))){
                            val intent = Intent(this@MainActivity, navDrawerDataClass.activityName)
                            startActivity(intent)
                        }
                        else if (navDrawerDataClass.item_name.equals(getString(R.string.scans))){

                            val intent = Intent(this@MainActivity, navDrawerDataClass.activityName)
                            startActivity(intent)

                        }
//                        else if (navDrawerDataClass.item_name.equals(getString(R.string.scanner))) {
//                            fragment = navDrawerDataClass.fragmentName!!
//                            val ft = supportFragmentManager.beginTransaction()
//                            ft.replace(R.id.content_main, fragment)
//                            ft.commit()
//                        }

                        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
                        drawer.closeDrawer(GravityCompat.START)
                    }
                }
            })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        //menuInflater.inflate(R.menu.main, menu)
        return true
    }

}