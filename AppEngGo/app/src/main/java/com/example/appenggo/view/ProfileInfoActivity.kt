package com.example.appenggo.view

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.appenggo.R
import com.example.appenggo.RetrofitClient
import com.example.appenggo.model.UserUpdateRequest
import com.example.appenggo.repository.UserRepository
import com.example.appenggo.viewmodel.ProfileResult
import com.example.appenggo.viewmodel.ProfileViewModel
import com.example.appenggo.viewmodel.ProfileViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class ProfileInfoActivity : BaseActivity() {

    private lateinit var viewModel: ProfileViewModel
    private lateinit var edtFullName: EditText
    private lateinit var edtEmail: EditText
    private lateinit var edtBio: EditText
    private lateinit var btnSave: MaterialButton
    private lateinit var btnBack: ImageView
    private lateinit var imgAvatar: ImageView

    // URI dùng khi chụp ảnh từ camera
    private var cameraImageUri: Uri? = null

    // Lưu URI ảnh người dùng đã chọn, chưa upload
    private var pendingAvatarUri: Uri? = null

    // Launcher: chọn ảnh từ thư viện
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data ?: return@registerForActivityResult
            previewAvatar(uri)
        }
    }

    // Launcher: chụp ảnh từ camera
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraImageUri?.let { previewAvatar(it) }
        }
    }

    // Launcher: xin quyền camera
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) launchCamera()
        else Toast.makeText(this, "Cần quyền camera để chụp ảnh", Toast.LENGTH_SHORT).show()
    }

    // Launcher: xin quyền đọc thư viện (Android < 13)
    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) launchGallery()
        else Toast.makeText(this, "Cần quyền truy cập thư viện ảnh", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_info)

        setupViewModel()
        initViews()
        setupListeners()
        observeViewModel()
        loadCurrentUserInfo()
    }

    private fun setupViewModel() {
        val repository = UserRepository(RetrofitClient.api)
        val factory = ProfileViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[ProfileViewModel::class.java]
    }

    private fun initViews() {
        edtFullName = findViewById(R.id.edt_full_name)
        edtEmail = findViewById(R.id.edt_email)
        edtBio = findViewById(R.id.edt_bio)
        btnSave = findViewById(R.id.btn_save)
        btnBack = findViewById(R.id.btn_back)
        imgAvatar = findViewById(R.id.img_avatar)
    }

    private fun loadCurrentUserInfo() {
        val token = getToken()
        if (token != null) {
            viewModel.fetchProfileData(token)
        }
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }
        btnSave.setOnClickListener { handleSaveAll() }

        imgAvatar.setOnClickListener { showAvatarBottomSheet() }
        findViewById<com.google.android.material.card.MaterialCardView>(R.id.btn_change_avatar)
            .setOnClickListener { showAvatarBottomSheet() }
    }

    private fun observeViewModel() {
        viewModel.userInfo.observe(this) { result ->
            if (result is ProfileResult.Success) {
                val user = result.data
                user?.let {
                    edtFullName.setText(it.fullName)
                    edtEmail.setText(it.email)
                    edtBio.setText(it.bio ?: "")

                    // Chỉ load avatar từ server nếu người dùng chưa chọn ảnh mới
                    if (pendingAvatarUri == null && !it.avatarUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(it.avatarUrl)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .placeholder(R.drawable.ic_default_avatar)
                            .circleCrop()
                            .into(imgAvatar)
                    }
                }
            }
        }

        viewModel.updateUserResult.observe(this) { result ->
            when (result) {
                is ProfileResult.Loading -> showLoading("Đang cập nhật...")
                is ProfileResult.Success -> {
                    hideLoading()
                    // Nếu có ảnh chờ upload thì upload tiếp
                    val uri = pendingAvatarUri
                    if (uri != null) {
                        uploadPendingAvatar(uri)
                    } else {
                        Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show()
                        setResult(RESULT_OK)
                        finish()
                    }
                }
                is ProfileResult.Error -> {
                    hideLoading()
                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.uploadAvatarResult.observe(this) { result ->
            when (result) {
                is ProfileResult.Loading -> showLoading("Đang tải ảnh lên...")
                is ProfileResult.Success -> {
                    hideLoading()
                    pendingAvatarUri = null
                    Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }
                is ProfileResult.Error -> {
                    hideLoading()
                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // ===== Nhấn nút Lưu =====

    private fun handleSaveAll() {
        val fullName = edtFullName.text.toString().trim()
        val email = edtEmail.text.toString().trim()
        val bio = edtBio.text.toString().trim()

        if (fullName.isEmpty()) {
            edtFullName.error = "Họ tên không được để trống"
            return
        }
        if (email.isEmpty()) {
            edtEmail.error = "Email không được để trống"
            return
        }

        val token = getToken() ?: run {
            Toast.makeText(this, "Phiên làm việc hết hạn", Toast.LENGTH_SHORT).show()
            return
        }

        // Luôn cập nhật thông tin text trước.
        // Nếu có ảnh chờ, sẽ upload sau khi updateUser thành công (trong observer).
        val request = UserUpdateRequest(email, fullName, bio)
        viewModel.updateUser(token, request)
    }

    // ===== Upload ảnh sau khi updateUser thành công =====

    private fun uploadPendingAvatar(uri: Uri) {
        val token = getToken() ?: return
        showLoading("Đang tải ảnh lên...")
        lifecycleScope.launch(Dispatchers.IO) {
            val file = uriToFile(uri)
            withContext(Dispatchers.Main) {
                hideLoading()
                if (file == null) {
                    Toast.makeText(this@ProfileInfoActivity, "Không thể đọc file ảnh", Toast.LENGTH_SHORT).show()
                    return@withContext
                }
                val requestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val filePart = MultipartBody.Part.createFormData("file", file.name, requestBody)
                viewModel.uploadAvatar(token, filePart)
            }
        }
    }

    // ===== Preview ảnh (chưa upload) =====

    private fun previewAvatar(uri: Uri) {
        pendingAvatarUri = uri
        Glide.with(this)
            .load(uri)
            .circleCrop()
            .placeholder(R.drawable.ic_default_avatar)
            .into(imgAvatar)
        Toast.makeText(this, "Nhấn Lưu để cập nhật ảnh đại diện", Toast.LENGTH_SHORT).show()
    }

    // ===== Avatar Bottom Sheet =====

    private fun showAvatarBottomSheet() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_avatar, null)
        dialog.setContentView(view)

        view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_camera)
            .setOnClickListener {
                dialog.dismiss()
                checkCameraPermissionAndLaunch()
            }

        view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_gallery)
            .setOnClickListener {
                dialog.dismiss()
                checkGalleryPermissionAndLaunch()
            }

        view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_cancel)
            .setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    // ===== Camera =====

    private fun checkCameraPermissionAndLaunch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            launchCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun launchCamera() {
        val photoFile = File.createTempFile("avatar_", ".jpg", cacheDir)
        val uri = FileProvider.getUriForFile(
            this,
            "${packageName}.provider",
            photoFile
        )
        cameraImageUri = uri
        cameraLauncher.launch(uri)
    }

    // ===== Gallery =====

    private fun checkGalleryPermissionAndLaunch() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            launchGallery()
        } else {
            val permission = Manifest.permission.READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
                launchGallery()
            } else {
                storagePermissionLauncher.launch(permission)
            }
        }
    }

    private fun launchGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        pickImageLauncher.launch(intent)
    }

    // ===== Tiện ích =====

    private fun uriToFile(uri: Uri): File? {
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val tempFile = File.createTempFile("upload_avatar_", ".jpg", cacheDir)
            tempFile.outputStream().use { inputStream.copyTo(it) }
            inputStream.close()
            tempFile
        } catch (e: Exception) {
            null
        }
    }

    private fun getToken(): String? {
        return getSharedPreferences("app_prefs", MODE_PRIVATE).getString("TOKEN", null)
    }
}