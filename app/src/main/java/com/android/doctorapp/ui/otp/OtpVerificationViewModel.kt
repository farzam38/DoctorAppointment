//package com.android.doctorapp.ui.otp
//
//import android.content.Context
//import com.android.doctorapp.repository.local.Session
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import com.android.doctorapp.R
//import com.android.doctorapp.di.base.BaseViewModel
//import com.android.doctorapp.util.SingleLiveEvent
//import com.android.doctorapp.util.extension.asLiveData
//import com.android.doctorapp.util.extension.toast
//import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
//import com.google.firebase.auth.PhoneAuthCredential
//import com.google.firebase.auth.PhoneAuthProvider
//import javax.inject.Inject
//
//
//class OtpVerificationViewModel @Inject constructor(
//    private val context: Context,
//    val session: Session
//) : BaseViewModel() {
//
//    val _otpDigit = MutableLiveData<String>()
//    val otpDigit: LiveData<String> get() = _otpDigit
//    val otpVerificationId: MutableLiveData<String?> = MutableLiveData()
//    val isDoctorOrUser: MutableLiveData<Boolean?> = MutableLiveData(true)
//    val userContactNumber: MutableLiveData<String?> = MutableLiveData()
//
//    private val _navigationListener = SingleLiveEvent<Int>()
//    val navigationListener = _navigationListener.asLiveData()
//
//
//    init {
//        firebaseUser = firebaseAuth.currentUser!!
//    }
//
//    fun isOtpFilled(): Boolean {
//        return _otpDigit.value?.isNotEmpty() == true
//    }
//
//
////    fun otpVerification() {
////        setShowProgress(true)
////        if (otpDigit.value?.isNotEmpty() == true) {
////            val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(
////                otpVerificationId.value.toString(), otpDigit.value.toString()
////            )
////            signInWithPhoneAuthCredential(credential)
////        }
////
////    }
////
////    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
////        firebaseAuth.signInWithCredential(credential)
////            .addOnCompleteListener { task ->
////                if (task.isSuccessful) {
////                    setShowProgress(false)
////                    if (isDoctorOrUser.value == true) {
////                        _navigationListener.value = R.id.action_otpFragment_to_updateDoctorFragment
////                    } else {
////                        _navigationListener.value = R.id.action_otpFragment_to_updateUserFragment
////                    }
////                } else {
////                    setShowProgress(false)
////                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
////                        context.toast("Invalid OTP")
////                    }
////                }
////            }
////    }
//}