package com.app.businesscardshare.activity



import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.app.businesscardshare.R
import com.app.businesscardshare.databinding.ActivityScannerBinding
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class Scanner : AppCompatActivity() {

     private lateinit var binding  : ActivityScannerBinding
     private lateinit var codeScanner : CodeScanner
     private var isUidChecked : Boolean = false

     override fun onCreate(savedInstanceState: Bundle?) {
         super.onCreate(savedInstanceState)

         binding = ActivityScannerBinding.inflate(layoutInflater)
         setContentView(binding.root)

         makeRequest()
         scanner()
     }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    private fun scanner() {
        codeScanner = CodeScanner(this, binding.scannerView)

        codeScanner.apply {
            camera = CodeScanner.CAMERA_BACK
            formats = CodeScanner.ALL_FORMATS
            autoFocusMode = AutoFocusMode.SAFE
            scanMode = ScanMode.CONTINUOUS
            isAutoFocusEnabled = true
            isFlashEnabled = false

            decodeCallback = DecodeCallback {
                runOnUiThread {
                    Log.i("--QR_Data--", it.text)
                    if(!isUidChecked){
                        checkIfUidExist(it.text)
                    }else{
                        finish()
                    }
                }
            }
            errorCallback = ErrorCallback {
                runOnUiThread {
                    it.message?.let { it1 -> Log.i("--QR_Data--", it1) }
                }
            }
        }
    }

    private fun makeRequest(){
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), 100 )
    }


    private fun checkIfUidExist(uid : String){
        isUidChecked = true
        val ref = FirebaseDatabase.getInstance().getReference("user_profiles/$uid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Profile data exists, get the data
                    val intent = Intent(this@Scanner, ScannedProfile::class.java)
                    intent.putExtra("UID", uid)
                    startActivity(intent)
                    finish()

                } else {
                    showToast("Invalid QR")
                    return
                }
            }

            override fun onCancelled(error: DatabaseError) {
                showToast("Invalid QR")
            }
        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun showToast(message : String) {
        val inflater = layoutInflater
        val layout = inflater.inflate(R.layout.custom_toast, null)
        val text = layout.findViewById<TextView>(R.id.toastMessage)
        text.text = message
        val toast = Toast(applicationContext)
        toast.setGravity(Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL, 0, 32)
        toast.duration = Toast.LENGTH_LONG
        toast.view = layout
        toast.show()
    }

}
