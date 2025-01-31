package com.android.doctorapp.ui.userdashboard.userfragment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.doctorapp.R
import com.android.doctorapp.databinding.FragmentUserAppointmentBinding
import com.android.doctorapp.di.AppComponentProvider
import com.android.doctorapp.di.base.BaseFragment
import com.android.doctorapp.di.base.toolbar.FragmentToolbar
import com.android.doctorapp.repository.local.IS_ENABLED_DARK_MODE
import com.android.doctorapp.repository.local.IS_NEW_USER_TOKEN
import com.android.doctorapp.repository.local.USER_TOKEN
import com.android.doctorapp.repository.models.UserDataResponseModel
import com.android.doctorapp.ui.userdashboard.userfragment.adapter.UserAppoitmentItemAdapter
//import com.android.doctorapp.util.GpsUtils
import com.android.doctorapp.util.constants.ConstantKey
//import com.android.doctorapp.util.extension.isGPSEnabled
import com.android.doctorapp.util.extension.toast
import com.android.doctorapp.util.permission.RuntimePermission
//import com.google.android.gms.location.FusedLocationProviderClient
//import com.google.android.gms.location.LocationCallback
//import com.google.android.gms.location.LocationResult
//import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


class UserAppointmentFragment :
    BaseFragment<FragmentUserAppointmentBinding>(R.layout.fragment_user_appointment) {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: UserAppointmentViewModel by viewModels { viewModelFactory }
    private lateinit var adapter: UserAppoitmentItemAdapter

//    private lateinit var fusedLocationClient: FusedLocationProviderClient
//    private var latitude: Double = 0.0
//    private var longitude: Double = 0.0
    private lateinit var requestNotificationPermissionLauncher: ActivityResultLauncher<String>

    override fun builder(): FragmentToolbar {
        return FragmentToolbar.Builder()
            .withId(R.id.toolbar)
            .withToolbarColorId(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            .withTitleString(viewModel.locationCity.value!!)
            .withTitleColorId(ContextCompat.getColor(requireContext(), R.color.white))
            .build()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestNotificationPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                if (it) {
                    requireActivity().toast(resources.getString(R.string.notifications_enabled))
                }
            }
//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        (requireActivity().application as AppComponentProvider).getAppComponent().inject(this)
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        lifecycleScope.launch {
            viewModel.session.getBoolean(IS_ENABLED_DARK_MODE).collectLatest {
                viewModel.isDarkThemeEnable.value = it == true
            }
        }

        val layoutBinding = binding {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@UserAppointmentFragment.viewModel
        }
        RuntimePermission.askPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ).onAccepted {
            requestLocationUpdates()
        }.onDenied {
            context?.toast(getString(R.string.location_permission))
        }.onForeverDenied {
            viewModel.setShowProgress(false)
        }.ask()
        return layoutBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpWithViewModel(viewModel)
        registerObserver(binding)
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun requestLocationUpdates() {
        viewModel.setShowProgress(true)
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permissions are not granted, request them using the launcher
            requestLocationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
//        if (requireContext().isGPSEnabled()) {
//            fusedLocationClient.requestLocationUpdates(
//                GpsUtils(requireContext()).locationRequest,
//                locationCallback,
//                null // Looper can be provided for the callback thread
//            )
//        } else {
//            viewModel.setShowProgress(false)
//            GpsUtils(requireContext()).turnGPSOn(object : GpsUtils.onGpsListener {
//                override fun gpsStatus(isGPSEnable: Boolean) {
//                }
//            })
//
//        }
    }

//    private val locationCallback = object : LocationCallback() {
//
//        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
//        override fun onLocationResult(p0: LocationResult) {
//            p0.lastLocation.let { location ->
//                // Handle the location update here
//                latitude = location!!.latitude
//                longitude = location.longitude
//                if (viewModel.doctorList.value == null) {
//                    checkNotificationPermission()
//                } else
//                    viewModel.setShowProgress(false)
//                stopLocationUpdates()
//            }
//        }
//    }
//
//    private fun stopLocationUpdates() {
//        fusedLocationClient.removeLocationUpdates(locationCallback)
//    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val requestLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true &&
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            // Permissions are granted, proceed with location updates
            requestLocationUpdates()
        } else {
            // Handle permission denial
            context?.toast(getString(R.string.location_permission))
        }
    }


    private fun registerObserver(layoutBinding: FragmentUserAppointmentBinding) {
        setAdapter(emptyList())
        layoutBinding.recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        layoutBinding.recyclerView.adapter = adapter

        viewModel.doctorList.observe(viewLifecycleOwner) { it1 ->
            if (!it1.isNullOrEmpty()) {
                adapter.filterList(it1)
                viewModel.dataFound.value = false
                viewModel.setShowProgress(false)
            } else {
                viewModel.dataFound.value = true
                adapter.filterList(emptyList())
            }
        }

        viewModel.viewModelScope.launch {
            viewModel.session.getBoolean(IS_NEW_USER_TOKEN).collectLatest {
                if (it == true) {
                    viewModel.session.getString(USER_TOKEN).collectLatest { it1 ->
                        viewModel.updateUserData(it1)
                    }
                }
            }
        }

    }


    private fun setAdapter(items: List<UserDataResponseModel>) {
        adapter = UserAppoitmentItemAdapter(
            items,
            object : UserAppoitmentItemAdapter.OnItemClickListener {
                override fun onItemClick(item: UserDataResponseModel, position: Int) {

                    val bundle = Bundle()
                    bundle.putString(
                        ConstantKey.BundleKeys.BOOK_APPOINTMENT_DATA,
                        Gson().toJson(item)
                    )
                    findNavController().navigate(
                        R.id.action_user_appointment_to_bookAppointment,
                        bundle
                    )
                    binding.searchEt.text?.clear()
                }

                override fun onRatingClick(item: UserDataResponseModel, position: Int) {
                    val bundle = Bundle()
                    bundle.putString(
                        ConstantKey.BundleKeys.BOOK_APPOINTMENT_DATA,
                        Gson().toJson(item)
                    )
                    findNavController().navigate(
                        R.id.action_user_appointment_to_allFeedback,
                        bundle
                    )
                }
            }
        )
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun checkNotificationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        ) requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)

    }


}